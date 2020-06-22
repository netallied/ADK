/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLFacet;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.AbstractDocumentElementChange;
import org.automationml.internal.Change;
import org.automationml.internal.IdentifierManager;
import org.automationml.internal.ReadOnlyIterable;

public class AMLFacetImpl extends AMLElementImpl implements AMLFacet {

	protected static class DeleteFacetChange extends AbstractDeleteDocumentElementChange<AMLFacetImpl> {
		private UUID id;
		private String name;

		public DeleteFacetChange(AMLFacetImpl element) {
			super(element);
			id = element.getId();
			name = element.getName();
		}

		@Override
		public void undo() throws Exception {
			AMLDocumentElement container = (AMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLFacetImpl clazz = (AMLFacetImpl) ((AMLInternalElementImpl) container)._createFacet(name, id);
			identifier.setDocumentElement(clazz);
		}
	}

	private static class ModifyFacetNameChange extends AbstractDocumentElementChange<AMLFacetImpl> {
		private String oldName;
		private String newName;

		public ModifyFacetNameChange(AMLFacetImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			AMLFacetImpl internalLink = (AMLFacetImpl) getDocumentElement();
			this.oldName = internalLink.getName();
		}

		public void setNewName(String newName) {
			this.newName = newName;
		}

		@Override
		public void undo() throws Exception {
			AMLFacetImpl facet = (AMLFacetImpl) getDocumentElement();
			if (newName == null)
				newName = facet.getName();
			facet._setName(oldName);
		}

		@Override
		public void redo() throws Exception {
			AMLFacetImpl facet = (AMLFacetImpl) getDocumentElement();
			facet._setName(newName);
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyFacetNameChange))
				return false;
			ModifyFacetNameChange change = (ModifyFacetNameChange) _change;
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

	private static class ModifyFacetAddAttributeChange extends AbstractDocumentElementChange<AMLFacetImpl> {
		protected String attributeName;

		public ModifyFacetAddAttributeChange(AMLFacetImpl element) {
			super(element);
		}

		@Override
		public void undo() throws Exception {
			AMLFacetImpl facet = (AMLFacetImpl) getDocumentElement();
			facet._removeAttribute(attributeName);
		}

		@Override
		public void redo() throws Exception {
			AMLFacetImpl facet = (AMLFacetImpl) getDocumentElement();
			AMLInternalElementImpl parent = facet.internalElement;
			AMLAttribute attribute = parent.getAttribute(attributeName);
			facet._addAttribute(attribute);
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyFacetAddAttributeChange))
				return false;
			ModifyFacetAddAttributeChange change = (ModifyFacetAddAttributeChange) _change;
			change.setAttributeName(attributeName);
			return true;
		}

		protected void setAttributeName(String attributeName) {
			this.attributeName = attributeName;
		}

		@Override
		protected void _delete() {
			// this is not really required, but helpful for debugging
			attributeName = null;
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
			if (getDocumentElement() != null) {
				AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
				document.notifyElementModified(getDocumentElement());
			}
		}

		@Override
		protected void notifyChangeListenersAfterRedo() {
			if (!getDocumentElement().isDeleted()) {
				AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
				document.notifyElementModified(getDocumentElement());
			}
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
		}

	}
	
	private static class ModifyFacetRemoveAttributeChange extends ModifyFacetAddAttributeChange {

		public ModifyFacetRemoveAttributeChange(AMLFacetImpl element) {
			super(element);
		}

		@Override
		public void undo() throws Exception {
			super.redo();
		}

		@Override
		public void redo() throws Exception {
			super.undo();
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyFacetRemoveAttributeChange))
				return false;
			ModifyFacetRemoveAttributeChange change = (ModifyFacetRemoveAttributeChange) _change;
			change.setAttributeName(attributeName);
			return true;
		}

	}

	private static class ModifyFacetAddExternalInterfaceChange extends AbstractDocumentElementChange<AMLFacetImpl> {
		protected UUID externalInterfaceId;

		public ModifyFacetAddExternalInterfaceChange(AMLFacetImpl element) {
			super(element);
		}

		@Override
		public void undo() throws Exception {
			AMLFacetImpl facet = (AMLFacetImpl) getDocumentElement();
			facet._removeExternalInterface(externalInterfaceId);
		}

		@Override
		public void redo() throws Exception {
			AMLFacetImpl facet = (AMLFacetImpl) getDocumentElement();
			AMLInternalElementImpl parent = facet.internalElement;
			AMLExternalInterface externalInterface = parent.getExternalInterface(externalInterfaceId);
			facet._addExternalInterface(externalInterface);
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyFacetAddExternalInterfaceChange))
				return false;
			ModifyFacetAddExternalInterfaceChange change = (ModifyFacetAddExternalInterfaceChange) _change;
			change.setExternalInterfaceId(externalInterfaceId);
			return true;
		}

		protected void setExternalInterfaceId(UUID externalInterfaceId) {
			this.externalInterfaceId = externalInterfaceId;
		}

		@Override
		protected void _delete() {
			// this is not really required, but helpful for debugging
			externalInterfaceId = null;
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
			if (getDocumentElement() != null) {
				AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
				document.notifyElementModified(getDocumentElement());
			}
		}

		@Override
		protected void notifyChangeListenersAfterRedo() {
			if (!getDocumentElement().isDeleted()) {
				AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
				document.notifyElementModified(getDocumentElement());
			}
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
		}

	}
	
	private static class ModifyFacetRemoveExternalInterfaceChange extends ModifyFacetAddExternalInterfaceChange {

		public ModifyFacetRemoveExternalInterfaceChange(AMLFacetImpl element) {
			super(element);
		}

		@Override
		public void undo() throws Exception {
			super.redo();
		}

		@Override
		public void redo() throws Exception {
			super.undo();
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyFacetRemoveExternalInterfaceChange))
				return false;
			ModifyFacetRemoveExternalInterfaceChange change = (ModifyFacetRemoveExternalInterfaceChange) _change;
			change.setExternalInterfaceId(externalInterfaceId);
			return true;
		}

	}

	private AMLInternalElementImpl internalElement;
	private String name;
	private UUID id;
	private Map<String, AMLAttribute> attributes = new LinkedHashMap<String, AMLAttribute>();
	private Map<UUID, AMLExternalInterface> externalInterfaces = new LinkedHashMap<UUID, AMLExternalInterface>();

	public AMLFacetImpl(AMLInternalElementImpl internalElement, String facetName, UUID id) {
		this.internalElement = internalElement;
		this.name = facetName;
		this.id = id;
	}

	private void _removeExternalInterface(UUID externalInterfaceId) {
		externalInterfaces.remove(externalInterfaceId);
	}

	private void _removeAttribute(String attributeName) {
		attributes.remove(attributeName);
	}

	@Override
	public AMLDocumentElement getParent() {
		return internalElement;
	}

	@Override
	public void delete() throws AMLValidationException {
		if (isDeleted())
			return;

		AMLValidationResultList validationResult = validateDelete();
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			DeleteFacetChange change = new DeleteFacetChange(this);
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
	}

	@Override
	public UUID getId() {
		assertNotDeleted();
		return id;
	}

	@Override
	public AMLDocumentImpl getDocument() {
		return (AMLDocumentImpl) internalElement.getDocument();
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
//		for (AMLAttribute attribute : getAttributes()) {
//			_removeAttribute(attribute.getName());
//		}
//		for (AMLExternalInterface externalInterface : getExternalInterfaces()) {
//			_removeExternalInterface(externalInterface.getId());
//		}
		internalElement.removeFacet(this);
		getDocumentManager().removeUniqueId(getDocument(), id);
	}

	@Override
	public String getName() {
		assertNotDeleted();
		return name == null ? "" : name;
	}

	@Override
	public void setName(String newName) throws AMLValidationException {
		assertNotDeleted();
		if (newName != null && newName.equals(this.name))
			return;

		AMLValidationResultList resultList = validateNameChange(newName);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyFacetNameChange change = new ModifyFacetNameChange(this);
			change.setNewName(newName);
			getSavepointManager().addChange(change);
		}
		_setName(newName);
		getDocument().notifyElementModified(this);
	}

	private void _setName(String newName) {
		internalElement.removeFacet(this);
		this.name = newName;
		internalElement.addFacet(this);
	}

	public AMLValidationResultList validateNameChange(String newName) {
		assertNotDeleted();

		if (newName != null && newName.equals(this.name))
			return new AMLValidationResultListImpl(this, Severity.OK, "");
		if (newName.isEmpty())
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name must not be empty");

		if (internalElement.getFacet(newName) != null)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name already exists");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateFacetSetName(this, newName);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public void addAttribute(AMLAttribute attribute) throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList resultList = validateFacetAddAttribute(attribute);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyFacetAddAttributeChange change = new ModifyFacetAddAttributeChange(this);
			change.setAttributeName(attribute.getName());
			getSavepointManager().addChange(change);
		}
		_addAttribute(attribute);
		getDocument().notifyElementModified(this);
	}

	private void _addAttribute(AMLAttribute attribute) {
		attributes.put(attribute.getName(), attribute);
	}

	@Override
	public AMLValidationResultList validateFacetAddAttribute(AMLAttribute attribute) {
		AMLAttribute testAttribute = internalElement.getAttribute(attribute.getName());
		if (testAttribute == null || testAttribute != attribute)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Attribute is not owned by parent of facet");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateFacetAddAttribute(this, attribute);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public Iterable<AMLAttribute> getAttributes() {
		assertNotDeleted();
		return AMLAttributeContainerHelper.getAttributes(attributes);
	}

	@Override
	public AMLAttribute getAttribute(String name) {
		assertNotDeleted();
		return attributes.get(name);
	}

	@Override
	public void addExternalInterface(AMLExternalInterface externalInterface) throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList resultList = validateFacetAddExternalInterface(externalInterface);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyFacetAddExternalInterfaceChange change = new ModifyFacetAddExternalInterfaceChange(this);
			change.setExternalInterfaceId(externalInterface.getId());
			getSavepointManager().addChange(change);
		}
		_addExternalInterface(externalInterface);
		getDocument().notifyElementModified(this);
	}

	@Override
	public AMLValidationResultList validateFacetAddExternalInterface(AMLExternalInterface externalInterface) {
		AMLExternalInterface testExternalInterface = internalElement.getExternalInterface(externalInterface.getId());
		if (testExternalInterface == null || testExternalInterface != externalInterface)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "ExternalInterface is not owned by parent of facet");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateFacetAddExternalInterface(this, externalInterface);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	private void _addExternalInterface(AMLExternalInterface externalInterface) {
		externalInterfaces.put(externalInterface.getId(), externalInterface);
	}

	@Override
	public Iterable<AMLExternalInterface> getExternalInterfaces() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLExternalInterface>(externalInterfaces.values());
	}

	@Override
	public AMLExternalInterface getExternalInterface(UUID id) {
		AMLExternalInterface externalInterface = externalInterfaces.get(id);
		return externalInterface;
	}

	@Override
	public void removeAttribute(AMLAttribute attribute) throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList resultList = validateFacetRemoveAttribute(attribute);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyFacetRemoveAttributeChange change = new ModifyFacetRemoveAttributeChange(this);
			change.setAttributeName(attribute.getName());
			getSavepointManager().addChange(change);
		}
		_removeAttribute(attribute.getName());
		getDocument().notifyElementModified(this);
	}

	@Override
	public AMLValidationResultList validateFacetRemoveAttribute(AMLAttribute attribute) {
		AMLAttribute testAttribute = internalElement.getAttribute(attribute.getName());
		if (testAttribute == null || testAttribute != attribute)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Attribute is not owned by parent of facet");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateFacetRemoveAttribute(this, attribute);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public void removeExternalInterface(AMLExternalInterface externalInterface) throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList resultList = validateFacetRemoveExternalInterface(externalInterface);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyFacetRemoveExternalInterfaceChange change = new ModifyFacetRemoveExternalInterfaceChange(this);
			change.setExternalInterfaceId(externalInterface.getId());
			getSavepointManager().addChange(change);
		}
		_removeExternalInterface(externalInterface.getId());
		getDocument().notifyElementModified(this);
		
	}

	@Override
	public AMLValidationResultList validateFacetRemoveExternalInterface(AMLExternalInterface externalInterface) {
		AMLExternalInterface testExternalInterface = internalElement.getExternalInterface(externalInterface.getId());
		if (testExternalInterface == null || testExternalInterface != externalInterface)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "ExternalInterface is not owned by parent of facet");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateFacetRemoveExternalInterface(this, externalInterface);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {

		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateFacetDelete(this);
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
		return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "A facet can't be reparented");
	}

	@Override
	protected void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
	}

	@Override
	protected AMLDocumentElement _getBefore() {
		return null;
	}

	@Override
	protected AMLDocumentElement _getAfter() {
		return null;
	}

	@Override
	public void validateIfIsInDocumentScope(
			AMLDocumentElement elementToReparent, AMLDocumentImpl document,
			AMLValidationResultListImpl result,
			Set<AMLDocumentElement> checkedElements) {
		if (checkedElements.contains(this))
			return;
		checkedElements.add(this);
		
		AMLRoleClass facet = document.getRoleClassByPath(AUTOMATION_ML_ROLE_FACET_PATH);
		if (facet == null)
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "RoleClass \"Facet\" is not available"));
		
		if (!isDescendantOf(elementToReparent) &&!getDocumentManager().getDocumentScope(document).isInDocumentScope(this))
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "role requirements is not part of document scope"));
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkFrom)
			throws AMLValidationException {
	
	}

	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		
		mapping.put(original, this);

		for (AMLAttribute attribute	: ((AMLFacet) original).getAttributes()) {
			AMLAttribute newBaseAttribute =  (AMLAttributeImpl) mapping.get(attribute);
			if (newBaseAttribute == null)
				newBaseAttribute = attribute;
			addAttribute(newBaseAttribute);
		}		
		for (AMLExternalInterface externalInterface	: ((AMLFacet) original).getExternalInterfaces()) {
			AMLExternalInterface newExternalInterface =  (AMLExternalInterface) mapping.get(externalInterface);
			if (newExternalInterface == null)
				newExternalInterface = externalInterface;
			addExternalInterface(newExternalInterface);
		}
	}

	@Override
	protected void _doDeepDelete(AMLDocumentElement baseElement)
			throws AMLValidationException {
		AMLValidationResultListImpl validationResultList = new AMLValidationResultListImpl();
		doValidateDeepDelete(null, baseElement, validationResultList);
		if (validationResultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResultList);
		
		while (getAttributes().iterator().hasNext()) {
			AMLAttribute attribute = ((AMLAttribute)getAttributes().iterator().next());
			removeAttribute(attribute);
		}
		
		while (getExternalInterfaces().iterator().hasNext()) {
			AMLExternalInterface externalInterface = ((AMLExternalInterface)getExternalInterfaces().iterator().next());
			removeExternalInterface(externalInterface);
		}	
		
		delete();
	}

	@Override
	public int getAttributesCount() {
		assertNotDeleted();
		return attributes.size();
	}
}
