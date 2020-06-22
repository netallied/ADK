/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLCOLLADAInterface;
import org.automationml.aml.AMLCOLLADAInterface.RefType;
import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLExternalInterfaceContainer;
import org.automationml.aml.AMLFacet;
import org.automationml.aml.AMLFrameAttribute;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalElementContainer;
import org.automationml.aml.AMLInternalLink;
import org.automationml.aml.AMLMappingObject;
import org.automationml.aml.AMLMirrorObject;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLRoleRequirements;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLSupportedRoleClassContainer;
import org.automationml.aml.AMLSystemUnitClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractCreateDocumentElementChange;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.AbstractDocumentElementChange;
import org.automationml.internal.Change;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;
import org.automationml.internal.ReadOnlyIterable;

public class AMLInternalElementImpl extends AMLElementImpl implements AMLInternalElement, AMLInternalAttributeContainer, AMLInternalInternalElementContainer,
	AMLInternalExternalInterfaceContainer, AMLInternalSupportedRoleClassContainer, AMLInternalFrameAttributeContainer, 
	AMLInternalCOLLADAInterfaceContainer, AMLInternalMirrorContainer, AMLInternalInternalLinkContainer {

	private static class CreateFacetChange extends AbstractCreateDocumentElementChange {
		private UUID id;
		private String name;

		public CreateFacetChange(AMLFacetImpl documentElement) {
			super(documentElement);
			this.id = documentElement.getId();
			this.name = documentElement.getName();
		}

		@Override
		public void redo() throws Exception {
			AMLInternalElementImpl container = (AMLInternalElementImpl) parentIdentifier.getDocumentElement();
			AMLFacetImpl clazz = (AMLFacetImpl) container._createFacet(name, id);
			identifier.setDocumentElement(clazz);
		}
	}

	

	private static class CreateRoleRequirementsChange extends AbstractCreateDocumentElementChange {

		private Identifier<AMLSupportedRoleClassImpl> supportedRoleClass;

		public CreateRoleRequirementsChange(AMLRoleRequirementsImpl documentElement) {
			super(documentElement);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLSupportedRoleClassImpl supportedRoleClass = (AMLSupportedRoleClassImpl) ((AMLRoleRequirements) getDocumentElement()).getSupportedRoleClass();
			this.supportedRoleClass = (Identifier<AMLSupportedRoleClassImpl>) identifierManager.getIdentifier(supportedRoleClass);
		}

		@Override
		public void redo() throws Exception {
			AMLInternalElementImpl internalElement = (AMLInternalElementImpl) parentIdentifier.getDocumentElement();
			AMLSupportedRoleClass supportedRoleClass = this.supportedRoleClass.getDocumentElement();
			AMLRoleRequirementsImpl roleRequirements = internalElement._createRoleRequierments(supportedRoleClass);
			identifier.setDocumentElement(roleRequirements);
		}

		@Override
		protected void _delete() {
			super._delete();
			if (supportedRoleClass != null)
				supportedRoleClass.release();
			supportedRoleClass = null;
		}
	}

	protected static class DeleteInternalElementChange extends AbstractDeleteDocumentElementChange<AMLInternalElementImpl> {
		private UUID id;
		private String name;
		
		public DeleteInternalElementChange(AMLInternalElementImpl element) {
			super(element);
			id = element.getId();
			name = element.getName();
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement documentElement = (AbstractAMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLInternalInternalElementContainer parent = (AMLInternalInternalElementContainer) documentElement;

			AMLInternalElementImpl newCreatedInternalElement = (AMLInternalElementImpl) AMLInternalElementContainerHelper._createInternalElement(parent, id);
			newCreatedInternalElement.setName(name);
			identifier.setDocumentElement(newCreatedInternalElement);
		}
	}

	private static class ModifyInternalElementNameChange extends AbstractDocumentElementChange<AMLInternalElementImpl> {
		private String oldName;
		private String newName;

		public ModifyInternalElementNameChange(AMLInternalElementImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			AMLInternalElementImpl internalElement = (AMLInternalElementImpl) getDocumentElement();
			this.oldName = internalElement.getName();
		}

		public void setNewName(String newName) {
			this.newName = newName;
		}

		@Override
		public void undo() throws Exception {
			AMLInternalElementImpl internalElement = (AMLInternalElementImpl) getDocumentElement();
			if (newName == null)
				newName = internalElement.getName();
			internalElement._setName(oldName);
		}

		@Override
		public void redo() throws Exception {
			AMLInternalElementImpl internalElement = (AMLInternalElementImpl) getDocumentElement();
			internalElement._setName(newName);
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyInternalElementNameChange))
				return false;
			ModifyInternalElementNameChange change = (ModifyInternalElementNameChange) _change;
			change.setNewName(newName);
			return true;
		}

		@Override
		protected void _delete() {
			// this is not really required, but helpful for debugging
			oldName = null;
			newName = null;
		}

		@Override
		protected void notifyChangeListenersBeforeUndo() {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
			document.notifyElementModified(getDocumentElement());
		}

		@Override
		protected void notifyChangeListenersAfterUndo() {
			if (getDocumentElement() != null) {
				AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
				document.notifyElementModified(getDocumentElement());
			}
		}

		@Override
		protected void notifyChangeListenersBeforeRedo() {
		}

		@Override
		protected void notifyChangeListenersAfterRedo() {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
			document.notifyElementModified(getDocumentElement());
		}

	}

	private static class ModifyInternalElementClassChange extends AbstractDocumentElementChange<AMLInternalElementImpl> {
		private Identifier<AMLSystemUnitClassImpl> oldClass;
		private Identifier<AMLSystemUnitClassImpl> newClass;

		public ModifyInternalElementClassChange(AMLInternalElementImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			AMLInternalElementImpl internalElement = (AMLInternalElementImpl) getDocumentElement();
			this.oldClass = (Identifier<AMLSystemUnitClassImpl>) identifierManager.getIdentifier((AMLSystemUnitClassImpl) internalElement.getBaseSystemUnitClass());
		}

		public void setNewBaseClass(Identifier<AMLSystemUnitClassImpl> newClass) {
			this.newClass = newClass;
		}

		@Override
		public void undo() throws Exception {
			AMLInternalElementImpl internalElement = (AMLInternalElementImpl) getDocumentElement();
			AMLSessionImpl session = (AMLSessionImpl) getDocumentElement().getSession();
			IdentifierManager identifierManager = session.getIdentifierManager();
			if (newClass == null)
				newClass = (Identifier<AMLSystemUnitClassImpl>) identifierManager.getIdentifier((AMLSystemUnitClassImpl) internalElement.getBaseSystemUnitClass());
			internalElement._setBaseSystemUnitClass(oldClass == null ? null : oldClass.getDocumentElement());
		}

		@Override
		public void redo() throws Exception {
			AMLInternalElementImpl internalElement = (AMLInternalElementImpl) getDocumentElement();
			internalElement._setBaseSystemUnitClass(newClass == null ? null : newClass.getDocumentElement() );
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyInternalElementClassChange))
				return false;
			ModifyInternalElementClassChange change = (ModifyInternalElementClassChange) _change;
			change.setNewBaseClass(newClass);
			return true;
		}

		@Override
		protected void _delete() {
			// this is not really required, but helpful for debugging
			if (oldClass != null)
				oldClass.release();
			if (newClass != null)
				newClass.release();
		}

		@Override
		protected void notifyChangeListenersBeforeUndo() {
		}

		@Override
		protected void notifyChangeListenersAfterUndo() {
			if (getDocumentElement() != null) {
				AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
				document.notifyElementModified(getDocumentElement());
			}
		}

		@Override
		protected void notifyChangeListenersBeforeRedo() {
		}

		@Override
		protected void notifyChangeListenersAfterRedo() {
			if (!getDocumentElement().isDeleted()) {
				AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
				document.notifyElementModified(getDocumentElement());	
			}
		}

	}

	private UUID id = null;
	private String name = null;
	private AMLFrameAttribute frameAttribute;
	private AMLCOLLADAInterface colladaInterface;
	private AMLInternalInternalElementContainer internalElementContainer = null;
	private Map<String, AMLAttribute> attributes = new LinkedHashMap<String, AMLAttribute>();
	private LinkedHashMap<UUID, AMLInternalElement> internalElements = new LinkedHashMap<UUID, AMLInternalElement>();
	private AMLSystemUnitClass baseSystemUnitClass = null;
	private Map<UUID, AMLExternalInterface> externalInterfaces = new LinkedHashMap<UUID, AMLExternalInterface>();
	private Map<AMLRoleClass, AMLSupportedRoleClass> supportedRoleClasses = new LinkedHashMap<AMLRoleClass, AMLSupportedRoleClass>();
	private AMLRoleRequirements roleRequirements;
	private Set<AMLInternalLink> internalLinks = new HashSet<AMLInternalLink>();
	private Set<AMLMirrorObject> mirrorers = new HashSet<AMLMirrorObject>();
	private LinkedHashMap<UUID, AMLMirrorObject> mirrors = new LinkedHashMap<UUID, AMLMirrorObject>();
	private Map<String, AMLFacet> facets = new LinkedHashMap<String, AMLFacet>();

	public AMLInternalElementImpl(AMLInternalInternalElementContainer container, UUID id) {
		this.internalElementContainer = container;
		this.id = id;
	}

	@Override
	public AMLDocumentElement getParent() {
		assertNotDeleted();
		return internalElementContainer;
	}

	@Override
	public Iterable<AMLAttribute> getAttributes() {
		assertNotDeleted();
		return AMLAttributeContainerHelper.getAttributes(attributes);
	}
	
	@Override
	public int getAttributesCount() {
		assertNotDeleted();
		return attributes.size();
	}

	@Override
	public AMLAttribute getAttribute(String name) {
		assertNotDeleted();
		return AMLAttributeContainerHelper.getAttribute(this, name);
	}

	@Override
	public AMLAttribute createAttribute(String name) throws AMLValidationException {
		return createAttribute(name, null);
	}
	
	@Override
	public AMLAttribute createAttribute(String name, AMLAttribute attribute)
			throws AMLValidationException {
		assertNotDeleted();
		AMLAttributeImpl copy = AMLAttributeContainerHelper.createAttribute(this, name);
		
		if (attribute != null) 
			copy.deepCopy(attribute, copy, attribute, new HashMap<AMLDocumentElement, AMLDocumentElement>());
		
		return copy;
	}

	@Override
	public void delete() throws AMLValidationException {
		if (isDeleted())
			return;

		AMLValidationResultList validationResult = AMLInternalElementContainerHelper.validateDeleteInternalElement(this);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			DeleteInternalElementChange change = new DeleteInternalElementChange(this);
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
	}

	@Override
	public AMLInternalElement createInternalElement() throws AMLValidationException {
		return createInternalElement((AMLInternalElement)null);
	}

	@Override
	public Iterable<AMLInternalElement> getInternalElements() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLInternalElement>(internalElements.values());
	}

	@Override
	public int getInternalElementsCount() {
		assertNotDeleted();
		return internalElements.size();
	}

	@Override
	public void setBaseSystemUnitClass(AMLSystemUnitClass baseclass) throws AMLValidationException {

		assertNotDeleted();
		assertSameSession(baseclass);

		if (this.baseSystemUnitClass == baseclass)
			return;

		AMLValidationResultList resultList = validateInternalElementSetBaseSystemUnitClass(baseclass);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		getDocument().notifyElementValidated(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyInternalElementClassChange change = new ModifyInternalElementClassChange(this);
			getSavepointManager().addChange(change);
		}

		_setBaseSystemUnitClass(baseclass);
		getDocument().notifyElementModified(this);
	}

	@Override
	public AMLValidationResultList validateInternalElementSetBaseSystemUnitClass(AMLSystemUnitClass systemUnitClass) {
		return _validateInternalElementSetBaseSystemUnitClass(systemUnitClass);
	}

	@Override
	public AMLValidationResultList validateInternalElementUnsetBaseSystemUnitClass(AMLSystemUnitClass systemUnitClass) throws AMLValidationException {
		return _validateInternalElementSetBaseSystemUnitClass(systemUnitClass);
	}

	private AMLValidationResultList _validateInternalElementSetBaseSystemUnitClass(AMLSystemUnitClass systemUnitClass) {
		if (systemUnitClass != null && !isClassDocumentReferenced(systemUnitClass))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Document of the base class must be referenced");
		
		if (isInBaseClassHierarchy(systemUnitClass))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "systemUnitClass must not be contained in Base Class Hierarchy (circular reference)");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInternalElementSetBaseSystemUnitClass(this,
					systemUnitClass);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	private boolean isClassDocumentReferenced(AMLSystemUnitClass clazz) {
		AMLDocument documentA = this.getDocument();
		AMLDocument documentB = clazz.getDocument();
		
		if (documentA.equals(documentB))
			return true;
		
		AMLSessionImpl sessionA = (AMLSessionImpl)documentA.getSession();		
		AMLSessionImpl sessionB = (AMLSessionImpl)documentB.getSession();
		if (!sessionA.equals(sessionB))
			return false;
		
		AMLDocumentManager docManager = sessionA.getDocumentManager();
		
		return docManager.isDocumentReferencedBy(documentA, documentB, null);
	}


	private void _setBaseSystemUnitClass(AMLSystemUnitClass baseclass) throws AMLValidationException {

		if ((AMLSystemUnitClassImpl) this.baseSystemUnitClass != null)
			((AMLSystemUnitClassImpl) baseSystemUnitClass).removeReferrer(this);

		if (baseclass != null) {
			((AMLSystemUnitClassImpl) baseclass).addReferrer(this);
		}

		this.baseSystemUnitClass = baseclass;
	}

	public boolean isInBaseClassHierarchy(AMLSystemUnitClass clazz) {
		if (clazz == null)
			return false;
		AMLDocumentElement nextClass = this;
		while (nextClass != null) {
			if (nextClass == clazz)
				return true;
			nextClass = nextClass.getParent();
		}

		return false;
	}

	@Override
	public void unsetBaseSystemUnitClass() throws AMLValidationException {
		assertNotDeleted();

		if (this.baseSystemUnitClass == null)
			return;

		AMLValidationResultList resultList = validateInternalElementUnsetBaseSystemUnitClass(null);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		getDocument().notifyElementValidated(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyInternalElementClassChange change = new ModifyInternalElementClassChange(this);
			getSavepointManager().addChange(change);
		}

		_setBaseSystemUnitClass(null);
		getDocument().notifyElementModified(this);
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public AMLSystemUnitClass getBaseSystemUnitClass() {
		assertNotDeleted();
		return baseSystemUnitClass;
	}

	@Override
	public String getName() {
		assertNotDeleted();
		return name;
	}

	@Override
	public void setName(String newName) throws AMLValidationException {
		assertNotDeleted();
		if (newName != null && newName.equals(this.name))
			return;

		AMLValidationResultList resultList = _validateNameChange(newName);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyInternalElementNameChange change = new ModifyInternalElementNameChange(this);
			change.setNewName(newName);
			getSavepointManager().addChange(change);
		}
		_setName(newName);
		getDocument().notifyElementModified(this);
	}
	
	@Override
	public AMLValidationResultList validateNameChange(String newName) {
		return _validateNameChange(newName);
	}

	
	protected AMLValidationResultList _validateNameChange(String newName) {
		assertNotDeleted();

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInternalElementSetName(this, newName);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	private void _setName(String newName) {
		this.name = newName;
	}

	@Override
	public AMLDocumentImpl getDocument() {
		assertNotDeleted();
		return (AMLDocumentImpl) internalElementContainer.getDocument();
	}

	@Override
	protected void _doDelete() throws AMLDocumentScopeInvalidException {
		AMLInternalElementContainerHelper._removeInternalElement(internalElementContainer, this);
		getDocumentManager().removeUniqueId(getDocument(), id);
	}

	@Override
	public Map<String, AMLAttribute> _getAttributes() {
		return attributes;
	}

	@Override
	public LinkedHashMap<UUID, AMLInternalElement> _getInternalElements() {
		return internalElements;
	}

	@Override
	public AMLValidationResultList validateCreateAttribute(String newName) {
		return AMLAttributeContainerHelper._validateCreateAttribute(this, newName);
	}

	@Override
	public AMLValidationResultList validateInternalElementCreate() {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.validateCreateInternalElement(this);
	}

	@Override
	public AMLValidationResultList validateInternalElementCreate(UUID id) throws AMLValidationException {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.validateCreateInternalElement(this, id);
	}

	@Override
	public AMLInternalElement createInternalElement(UUID id) throws AMLValidationException {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.createInternalElement(this, id);
	}

	@Override
	public AMLExternalInterface createExternalInterface(AMLInterfaceClass interfaceClass) throws AMLValidationException {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.createExternalInterface(this, interfaceClass, true);
	}

	@Override
	public Iterable<AMLExternalInterface> getExternalInterfaces() {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.getExternalInterfaces(externalInterfaces);
	}

	@Override
	public int getExternalInterfacesCount() {
		assertNotDeleted();
		return externalInterfaces.size();
	}

	@Override
	public AMLValidationResultList validateCreateExternalInterface(AMLInterfaceClass interfaceClass) {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.validateCreateExternalInterface(this, interfaceClass);
	}

	@Override
	public AMLExternalInterface getExternalInterface(UUID id) {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.getExternalInterface(this, id);
	}

	@Override
	public Map<UUID, AMLExternalInterface> _getExternalInterfaces() {
		return externalInterfaces;
	}

	@Override
	public AMLExternalInterface createExternalInterface(UUID id, AMLInterfaceClass interfaceClass) throws AMLValidationException {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.createExternalInterface(this, interfaceClass, id, true);
	}

	@Override
	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}

	@Override
	public AMLMappingObject createMappingObject(AMLRoleClass roleClass) {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.createMappingObject(this, roleClass);
	}

	@Override
	public AMLValidationResultList validateCreateSupportedRoleClass(AMLRoleClass roleClass) {
		assertNotDeleted();
		return AMLSupportedRoleClassContainerHelper.validateCreateSupportedRoleClass(this, roleClass);
	}

//	@Override
//	public AMLSupportedRoleClass createSupportedRoleClass(AMLRoleClass roleClass, AMLMappingObject mappings) throws AMLValidationException {
//		assertNotDeleted();
//		return AMLSupportedRoleClassContainerHelper.createSupportedRoleClass(this, roleClass, mappings);
//	}

	@Override
	public AMLSupportedRoleClass getSupportedRoleClass(AMLRoleClass roleClass) {
		assertNotDeleted();
		return supportedRoleClasses.get(roleClass);
	}

	@Override
	public Iterable<AMLSupportedRoleClass> getSupportedRoleClasses() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLSupportedRoleClass>(supportedRoleClasses.values());
	}

	@Override
	public int getSupportedRoleClassesCount() {
		assertNotDeleted();
		return supportedRoleClasses.size();
	}

	@Override
	public Map<AMLRoleClass, AMLSupportedRoleClass> _getSupportedRoleClasses() {
		return supportedRoleClasses;
	}

	@Override
	public AMLRoleRequirements createRoleRequirements(AMLSupportedRoleClass supportedRoleClass) throws AMLValidationException {
		assertNotDeleted();
		AMLDocumentImpl document = (AMLDocumentImpl) getDocument();

		AMLValidationResultList resultList = validateCreateRoleRequirements(supportedRoleClass);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		document.notifyElementValidated(resultList);

		roleRequirements = _createRoleRequierments(supportedRoleClass);

		if (((AMLSessionImpl) getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateRoleRequirementsChange change = new CreateRoleRequirementsChange((AMLRoleRequirementsImpl) roleRequirements);
			((AMLSessionImpl) getSession()).getSavepointManager().addChange(change);
		}

		document.notifyElementCreated(roleRequirements, this);

		return roleRequirements;
	}

	AMLRoleRequirementsImpl _createRoleRequierments(AMLSupportedRoleClass supportedRoleClass) {
		AMLRoleRequirementsImpl roleRequirements = new AMLRoleRequirementsImpl(this, supportedRoleClass);
		this.roleRequirements = roleRequirements;
		return roleRequirements;
	}

	public AMLValidationResultList validateCreateRoleRequirements(AMLSupportedRoleClass supportedRoleClass) {
		if (roleRequirements != null)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "InternalElement already has a RoleRequirements element");

		if (supportedRoleClass.getParent() != this)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "SupportedRoleClass not owned by InternalElement");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateRoleRequirementCreate(supportedRoleClass);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public AMLRoleRequirements getRoleRequirements() {
		assertNotDeleted();
		return roleRequirements;
	}

	void removeRoleRequirements() {
		this.roleRequirements = null;
	}

	@Override
	public AMLSupportedRoleClass createSupportedRoleClass(AMLRoleClass roleClass) throws AMLValidationException {
		assertNotDeleted();
		return AMLSupportedRoleClassContainerHelper.createSupportedRoleClass(this, roleClass);
	}

	@Override
	public AMLInternalLink createInternalLink(String linkName, AMLExternalInterface refPartnerSideA, AMLExternalInterface refPartnerSideB)
			throws AMLValidationException {
		
		assertNotDeleted();
		return AMLInternalLinkContainerHelper.createInternalLink(this, linkName, refPartnerSideA, refPartnerSideB);
	}

	public AMLInternalLinkImpl _createInternalLink(String linkName, AMLExternalInterface refPartnerSideA, AMLExternalInterface refPartnerSideB) {
		AMLInternalLinkImpl internalLink = new AMLInternalLinkImpl(this, linkName, refPartnerSideA, refPartnerSideB);
		AMLExternalInterfaceImpl externalInterface = (AMLExternalInterfaceImpl) refPartnerSideA;
		externalInterface._addInternalLink(internalLink);
		externalInterface = (AMLExternalInterfaceImpl) refPartnerSideB;
		externalInterface._addInternalLink(internalLink);
		this.internalLinks.add(internalLink);
		return internalLink;
	}

	@Override
	public AMLValidationResultList validateCreateInternalLink(String linkName, AMLExternalInterface refPartnerSideA, AMLExternalInterface refPartnerSideB) {
		return AMLInternalLinkContainerHelper.validateCreateInternalLink(this, linkName, refPartnerSideA, refPartnerSideB);
		
	}

	@Override
	public Iterable<AMLInternalLink> getInternalLinks() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLInternalLink>(internalLinks);
	}

	public int getInternalLinkCount() {
		return internalLinks.size();
	}

	public void removeInternalLink(AMLInternalLink internalLink) {
		internalLinks.remove(internalLink);
	}

	public int getMirrorCount() {
		return mirrorers.size();
	}

	void addMirrorObject(AMLMirrorObjectImpl mirror) {
		mirrorers.add(mirror);
	}

	void removeMirrorObject(AMLMirrorObject mirror) {
		mirrorers.remove(mirror);
	}

	@Override
	public AMLFacet createFacet(String facetName) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) getDocument();

		UUID newId = UUID.randomUUID();
		while (document.getDocumentManager().isUniqueIdDefined(this, newId))
			newId = UUID.randomUUID();

		return createFacet(facetName, newId);
	}

	@Override
	public AMLValidationResultList validateCreateFacet(String facetName, UUID newId) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) getDocument();
		if (document.getDocumentManager().isUniqueIdDefined(this, newId))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Id already exists");

		return validateCreateFacet(facetName);
	}

	AMLFacetImpl _createFacet(String facetName, UUID id) {
		AMLFacetImpl facet = new AMLFacetImpl(this, facetName, id);
		addFacet(facet);
		return facet;
	}

	@Override
	public AMLValidationResultList validateCreateFacet(String facetName){
		AMLRoleClass facet = getDocument().getRoleClassByPath(AMLFacet.AUTOMATION_ML_ROLE_FACET_PATH);
		if (facet == null)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "RoleClass \"Facet\" is not available");

		if (getFacet(facetName) != null)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Facet name already exists");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateFacetCreate(this, facetName);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public Iterable<AMLFacet> getFacets() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLFacet>(facets.values());
	}

	@Override
	public AMLFacet getFacet(String facetName) {
		assertNotDeleted();
		AMLFacet facet = facets.get(facetName);
		return facet;
	}

	void removeFacet(AMLFacet facet) {
		facets.remove(facet.getName());
	}

	public int getFacetsCount() {
		return facets.size();
	}

	@Override
	public AMLFacet createFacet(String facetName, UUID id) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) getDocument();

		AMLValidationResultList resultList = validateCreateFacet(facetName, id);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		document.notifyElementValidated(resultList);

		AMLFacetImpl facet = _createFacet(facetName, id);

		if (((AMLSessionImpl) getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateFacetChange change = new CreateFacetChange(facet);
			((AMLSessionImpl) getSession()).getSavepointManager().addChange(change);
		}

		document.notifyElementCreated(facet, this);

		return facet;
	}

	void addFacet(AMLFacet facet) {
		facets.put(facet.getName(), facet);
	}

	@Override
	public AMLFrameAttribute getFrameAttribute()  throws AMLValidationException {
		assertNotDeleted();
		if (frameAttribute == null)
			frameAttribute = AMLAttributeContainerHelper.createFrameAttribute(this);
		return frameAttribute;
	}

	@Override
	public AMLValidationResultList validateCreateFrameAttribute() {
		return AMLAttributeContainerHelper._validateCreateFrameAttribute(this);
	}

	@Override
	public void _setFrameAttribute(AMLFrameAttributeImpl frameAttribute) {
		this.frameAttribute = frameAttribute;
	}

	@Override
	public boolean hasFrameAttribute() {
		return frameAttribute != null;
	}

	@Override
	public AMLCOLLADAInterface createCOLLADAInterface(RefType refType) throws AMLValidationException {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.createCOLLADAInterface(this, refType);
	}

	@Override
	public AMLCOLLADAInterface getCOLLADAInterface() {
		assertNotDeleted();
		return colladaInterface;
	}

	@Override
	public void _setCOLLADAInterface(AMLCOLLADAInterfaceImpl colladaInterface) {
		this.colladaInterface = colladaInterface;
	}

	@Override
	public AMLValidationResultList validateCreateCOLLADAInterface(RefType refType) {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.validateCreateCOLLADAInterface(this, refType);
	}
	
	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {

		AMLValidationResultList validationResult = AMLInternalElementContainerHelper.validateDeleteInternalElement(this);
		if (validationResult.isAnyOperationNotPermitted())
			return validationResult;

		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateInternalElementDelete(this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		
		if (!(newParentElement instanceof AMLInternalElementContainer))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "new parent is not a container for InternalElements");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateInternalElementReparent((AMLInternalElementContainer)oldParentElement, (AMLInternalElementContainer)newParentElement, this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}
		
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		AMLInternalInternalElementContainer oldParent = (AMLInternalInternalElementContainer)oldParentElement;
		AMLInternalInternalElementContainer newParent = (AMLInternalInternalElementContainer)newParentElement;
		AMLInternalElementContainerHelper._removeInternalElement(oldParent, this);
		this.internalElementContainer = newParent;
		AMLInternalElementContainerHelper._addInternalElement(newParent, this, (AMLInternalElement)beforeElement, (AMLInternalElement)afterElement);
	}

	@Override
	protected AMLDocumentElement _getBefore() {
		AMLInternalInternalElementContainer internalElementContainer = (AMLInternalInternalElementContainer) getParent();
		return AMLInternalElementContainerHelper._getInternalElementBefore(internalElementContainer, this);
	}

	@Override
	public void validateIfIsInDocumentScope(AMLDocumentElement elementToReparent, AMLDocumentImpl document,
			AMLValidationResultListImpl result, Set<AMLDocumentElement> checkedElements) {
		if (checkedElements.contains(this))
			return;
		checkedElements.add(this);
		
		if (!isDescendantOf(elementToReparent) &&!getDocumentManager().getDocumentScope(document).isInDocumentScope(this))
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "internal element is not part of document scope"));
				
		for (AMLInternalElement internalElement : internalElements.values()) {
			((AMLInternalElementImpl)internalElement).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		for (AMLMirrorObject mirrorObject : mirrors.values()) {
			((AMLMirrorObjectImpl)mirrorObject).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		for (AMLSupportedRoleClass roleClass : supportedRoleClasses.values()) {
			((AMLSupportedRoleClassImpl)roleClass).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		if (roleRequirements != null) {
			((AMLRoleRequirementsImpl)roleRequirements).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		for (AMLExternalInterface externalInterface : externalInterfaces.values()) {
			((AMLExternalInterfaceImpl)externalInterface).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		for (AMLFacet facet : facets.values()) {
			((AMLFacetImpl)facet).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		if (baseSystemUnitClass != null) {
			((AMLSystemUnitClassImpl)baseSystemUnitClass).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		for (AMLAttribute attribute : attributes.values()) {
			((AMLAttributeImpl)attribute).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		
		for (AMLDocumentElement documentElement : mirrorers) {
			// falscher test!!
			//((AbstractAMLDocumentElement)documentElement).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
			// richtig ist
			if (checkedElements.contains(documentElement))
				continue;
			AMLDocumentImpl referrerDocumentScope = (AMLDocumentImpl)documentElement.getDocument();
			if (!getDocumentManager().getDocumentScope(referrerDocumentScope).isInDocumentScope(document))
				result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "base class is not part of referrer's document scope"));
		}
	}

	@Override
	public boolean isReferencedByDocumentElements(AMLDocument document, AMLDocument documentToRemove,
			AMLValidationResultListImpl result) {
		return false;		
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkForm)
			throws AMLValidationException {
		if (baseSystemUnitClass != null) {
			if (!((AbstractAMLDocumentElement)baseSystemUnitClass).isDescendantOf(unlinkForm))
				unsetBaseSystemUnitClass();
		}
		for (AMLInternalElement internalElement : internalElements.values()) {
			((AbstractAMLDocumentElement)internalElement)._doUnlink(unlinkForm);
		}
		
		for (AMLMirrorObject mirrorObject : mirrors.values()) {
			((AbstractAMLDocumentElement) mirrorObject)._doUnlink(unlinkForm);
		}
		
		Iterator<AMLMirrorObject> iter = mirrorers.iterator();
		while (iter.hasNext()) {
			AMLMirrorObject mirrorObject = iter.next();
			//if (!((AbstractAMLDocumentElement)mirrorObject).isDescendantOf(unlinkForm)) 
			{
				mirrorObject.delete();
				iter = mirrorers.iterator();
			}
		}

		boolean retry = true;
		while (retry) {
			retry = false;
			for (AMLExternalInterface externalInterface : getExternalInterfaces()) {
				((AbstractAMLDocumentElement)externalInterface)._doUnlink(unlinkForm);
				if (externalInterface.isDeleted()) {
					retry = true;
					break;
				}
			}
		}
		if (roleRequirements != null) {
			((AbstractAMLDocumentElement)roleRequirements)._doUnlink(unlinkForm);
		}
		retry = true;
		while (retry) {
			retry = false;
			for (AMLSupportedRoleClass supportedRoleClass : getSupportedRoleClasses()) {
				((AbstractAMLDocumentElement)supportedRoleClass)._doUnlink(unlinkForm);
				if (supportedRoleClass.isDeleted()) {
					retry = true;
					break;
				}
			}
		}
		for (AMLInternalLink internalLink : getInternalLinks()) {
			((AbstractAMLDocumentElement)internalLink)._doUnlink(unlinkForm);
		}
		
	}

	@Override
	public void _doDeepDelete(AMLDocumentElement baseElement) throws AMLValidationException {
		
		AMLValidationResultListImpl validationResultList = new AMLValidationResultListImpl();
		doValidateDeepDelete(null, baseElement, validationResultList);
		if (validationResultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResultList);
		
		if (baseSystemUnitClass != null) {
			unsetBaseSystemUnitClass();
		}	
		while (getInternalElements().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getInternalElements().iterator().next())._doDeepDelete(baseElement);
		}
		while (getMirrorObjects().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getMirrorObjects().iterator().next())._doDeepDelete(baseElement);
		}	
		while (getFacets().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getFacets().iterator().next())._doDeepDelete(baseElement);
		}
		while (getExternalInterfaces().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getExternalInterfaces().iterator().next())._doDeepDelete(baseElement);
		}	
		if (roleRequirements != null) {
			((AbstractAMLDocumentElement)roleRequirements)._doDeepDelete(baseElement);
		}
		while (getSupportedRoleClasses().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getSupportedRoleClasses().iterator().next())._doDeepDelete(baseElement);
		}		
		while (getInternalLinks().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getInternalLinks().iterator().next())._doDeepDelete(baseElement);
		}
		
		while (getAttributes().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getAttributes().iterator().next())._doDeepDelete(baseElement);
		}
		
		if (frameAttribute != null)
			((AbstractAMLDocumentElement)getFrameAttribute())._doDeepDelete(baseElement);	
		
		if (colladaInterface != null)
			((AbstractAMLDocumentElement)getCOLLADAInterface())._doDeepDelete(baseElement);
		
		delete();
	}

	@Override
	protected void doValidateDeepDelete(
			AMLValidator validator, AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInternalElementDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
		
		for (AMLInternalElement internalElement : getInternalElements()) {
			((AbstractAMLDocumentElement)internalElement).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		for (AMLMirrorObject mirrorObject : getMirrorObjects()) {
			((AbstractAMLDocumentElement)mirrorObject).doValidateDeepDelete(validator, baseElement, validationResultList);
		}		
		for (AMLExternalInterface externalInterface : getExternalInterfaces()) {
			((AbstractAMLDocumentElement)externalInterface).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		if (roleRequirements != null) {
			((AbstractAMLDocumentElement)roleRequirements).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		for (AMLSupportedRoleClass supportedRoleClass : getSupportedRoleClasses()) {
			((AbstractAMLDocumentElement)supportedRoleClass).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		for (AMLInternalLink internalLink : getInternalLinks()) {
			((AbstractAMLDocumentElement)internalLink).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		for (AMLAttribute attribute	: getAttributes()) {
			((AbstractAMLDocumentElement)attribute).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement, AMLDocumentElement rootCopyElement, 
			AMLDocumentElement original, Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		
		mapping.put(original, this);
		
		setName(((AMLInternalElementImpl) original).getName());
		
		AMLSystemUnitClass systemUnitClass = ((AMLInternalElement) original).getBaseSystemUnitClass();
		if (systemUnitClass != null && ((AbstractAMLDocumentElement) systemUnitClass).isDescendantOf(rootElement.getDocument()))
			setBaseSystemUnitClass(((AMLInternalElement) original).getBaseSystemUnitClass());
		
		for (AMLInternalElement internalElement : ((AMLInternalElementContainer) original).getInternalElements()) {
			if (internalElement.equals(rootCopyElement))
				continue;
			AMLInternalElement copyInternalElement = createInternalElement();
			((AbstractAMLDocumentElement) copyInternalElement).deepCopy(rootElement, rootCopyElement, internalElement, mapping);
		}
		
				
		for (AMLExternalInterface externalInterface : ((AMLExternalInterfaceContainer) original).getExternalInterfaces()) {
			AMLExternalInterface copyExternalInterface = AMLExternalInterfaceContainerHelper.createExternalInterface(this, externalInterface.getInterfaceClass(), false);
			((AbstractAMLDocumentElement) copyExternalInterface).deepCopy(rootElement, rootCopyElement, externalInterface, mapping);
		}
		
		
		for (AMLInternalLink internalLink : ((AMLInternalElement) original).getInternalLinks()) {
			AMLExternalInterface a = internalLink.getRefPartnerSideA();
			AMLExternalInterface b = internalLink.getRefPartnerSideB();
			if (mapping.containsKey(a))
				a = (AMLExternalInterface) mapping.get(a);
			else if (!((AbstractAMLDocumentElement) a).isDescendantOf(original.getDocument()))
				a = null;
			if (mapping.containsKey(b))
				b = (AMLExternalInterface) mapping.get(b);
			else if (!((AbstractAMLDocumentElement) b).isDescendantOf(original.getDocument()))
				b = null;
			if (a != null && b != null) {
				AMLInternalLink copyInternalLink = createInternalLink(internalLink.getName(), a, b);
				((AbstractAMLDocumentElement) copyInternalLink).deepCopy(rootElement, rootCopyElement, internalLink, mapping);
			}
		}
		for (AMLAttribute attribute	: ((AMLInternalElement) original).getAttributes()) {
			AMLAttribute copyAttribute = createAttribute(attribute.getName());
			((AbstractAMLDocumentElement)copyAttribute).deepCopy(rootElement, rootCopyElement, attribute, mapping);
		}
		
		for (AMLFacet facet : ((AMLInternalElement) original).getFacets()) {
			AMLFacet copyFacet = createFacet(facet.getName());
			((AbstractAMLDocumentElement) copyFacet).deepCopy(rootElement, rootCopyElement, facet, mapping);
		}
		
		for (AMLSupportedRoleClass supportedRoleClass : ((AMLSupportedRoleClassContainer) original).getSupportedRoleClasses()) {
			AMLSupportedRoleClass copySupportedRoleClass = createSupportedRoleClass(supportedRoleClass.getRoleClass());
			((AbstractAMLDocumentElement) copySupportedRoleClass).deepCopy(rootElement, rootCopyElement, supportedRoleClass, mapping);
		}
		
		if (((AMLInternalElement) original).getRoleRequirements() != null) {
			AMLSupportedRoleClass newSupportedRoleClass = (AMLSupportedRoleClass) mapping.get(((AMLInternalElement) original).getRoleRequirements().getSupportedRoleClass());
			if (newSupportedRoleClass != null) {
				createRoleRequirements(newSupportedRoleClass);
				((AbstractAMLDocumentElement)roleRequirements).deepCopy(rootElement, rootCopyElement, ((AMLInternalElement) original).getRoleRequirements(), mapping);
			}
		}
		
		AMLFrameAttribute frameAttribute = ((AMLInternalElement)original).getFrameAttribute();
		((AMLFrameAttributeImpl)getFrameAttribute()).deepCopy(rootElement, rootCopyElement, frameAttribute, mapping);
		
		AMLCOLLADAInterface colladaInterface = ((AMLInternalElement)original).getCOLLADAInterface();
		if (colladaInterface != null) {
			createCOLLADAInterface(colladaInterface.getRefType());
			((AMLCOLLADAInterfaceImpl)getCOLLADAInterface()).deepCopy(rootElement, rootCopyElement, colladaInterface, mapping);
		}
	}

	public void deepCopyMirrors(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		for (AMLMirrorObject mirrorObject : ((AMLInternalElement) original).getMirrorObjects()) {
			AMLInternalElement internalElement = mirrorObject.getInternalElement();
			if (mapping.containsKey(internalElement))
				internalElement = (AMLInternalElement) mapping.get(internalElement);
			AMLMirrorObject copyMirror = createMirror(internalElement);
			((AbstractAMLDocumentElement)mirrorObject).deepCopy(rootElement, rootCopyElement, copyMirror, mapping);
		}
		
		for (AMLInternalElement internalElement : getInternalElements()) {
			((AMLInternalElementImpl) internalElement).deepCopyMirrors(rootElement, rootCopyElement, internalElement, mapping);
		}
	}

	@Override
	public AMLInternalElement createInternalElement(
			AMLInternalElement internalElement) throws AMLValidationException {
		assertNotDeleted();
	
		return _createInternalElement(internalElement, null);
	}
	
	AMLInternalElement _createInternalElement(
			AMLInternalElement internalElement, Map<AMLDocumentElement, AMLDocumentElement> mapping) throws AMLValidationException {
		
		AMLInternalElementImpl copy = AMLInternalElementContainerHelper.createInternalElement(this);
		
		if (internalElement != null) {
			if (mapping == null)
				mapping = new HashMap<AMLDocumentElement, AMLDocumentElement>();
			copy.deepCopy(internalElement, copy, internalElement, mapping);
			copy.deepCopyMirrors(internalElement, copy, internalElement, mapping);
		}
		return copy;
	}

	@Override
	protected AMLDocumentElement _getAfter() {
		AMLInternalInternalElementContainer internalElementContainer = (AMLInternalInternalElementContainer) getParent();
		return AMLInternalElementContainerHelper._getInternalElementAfter(internalElementContainer, this);
	}

	@Override
	public void _removeFrameAttribute() {
		frameAttribute = null;		
	}

	@Override
	public void _addFrameAttribute(AMLFrameAttributeImpl amlFrameAttributeImpl) {
		this.frameAttribute = amlFrameAttributeImpl;
	}

	@Override
	public AMLMirrorObject createMirror(UUID id,
			AMLInternalElement internalElement) throws AMLValidationException {
		assertNotDeleted();
		return AMLMirrorContainerHelper.createMirror(this, id, internalElement);
	}

	@Override
	public AMLMirrorObject createMirror(AMLInternalElement internalElement)
			throws AMLValidationException {
		assertNotDeleted();
		return AMLMirrorContainerHelper.createMirror(this, internalElement);
	}

	@Override
	public Iterable<AMLMirrorObject> getMirrorObjects() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLMirrorObject>(mirrors.values());
	}

	@Override
	public AMLValidationResultList validateMirrorCreate(UUID id,
			AMLInternalElement internalElement) throws AMLValidationException {
		assertNotDeleted();
		return AMLMirrorContainerHelper.validateMirrorCreate(this, id, internalElement);	
	}

	@Override
	public AMLValidationResultList validateMirrorCreate(
			AMLInternalElement internalElement) {
		assertNotDeleted();
		return AMLMirrorContainerHelper.validateMirrorCreate(this, internalElement);	
	}

	@Override
	public int getMirrorObjectsCount() {
		assertNotDeleted();
		return mirrors.size();
	}

	@Override
	public LinkedHashMap<UUID, AMLMirrorObject> _getMirrors() {
		return mirrors;
	}

	@Override
	public AMLInternalElement createInternalElement(
			AMLSystemUnitClass systemUnitClass) throws AMLValidationException {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.createInternalElement(this, systemUnitClass);		
	}

	@Override
	public AMLValidationResultList validateInternalElementCreate(
			AMLSystemUnitClass systemUnitClass) {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.validateCreateInternalElement(this, systemUnitClass);		
	}

	@Override
	public Set<AMLInternalLink> _getInternalLinks() {
		return internalLinks;
	}

	@Override
	public AMLInternalLinkImpl _createInternalLink(String name, AMLExternalInterfaceImpl refPartnerSideA,
			AMLExternalInterfaceImpl refPartnerSideB) {
		return AMLInternalLinkContainerHelper._createInternalLink(this, name, refPartnerSideA, refPartnerSideB);
	}
	
	@Override
	public int getReferrerCount() {
		assertNotDeleted();
		return mirrorers.size();
	}

	@Override
	public Iterable<AMLMirrorObject> getReferrers() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLMirrorObject>(mirrorers);
	}

	@Override
	public int getInternalLinksCount() {
		assertNotDeleted();
		return internalLinks.size();
	}

}
