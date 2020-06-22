/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLGroup;
import org.automationml.aml.AMLGroupContainer;
import org.automationml.aml.AMLInstanceHierarchy;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalElementContainer;
import org.automationml.aml.AMLMirrorContainer;
import org.automationml.aml.AMLMirrorObject;
import org.automationml.aml.AMLSystemUnitClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.AbstractDocumentElementChange;
import org.automationml.internal.Change;
import org.automationml.internal.IdentifierManager;
import org.automationml.internal.ReadOnlyIterable;

public class AMLInstanceHierarchyImpl extends AMLElementImpl implements AMLInstanceHierarchy, AMLInternalInternalElementContainer, 
	AMLInternalGroupContainer, AMLInternalMirrorContainer {

	private static class ModifyInstanceHierarchyChange extends AbstractDocumentElementChange<AMLInstanceHierarchyImpl> {
		private String oldName;
		private String newName;

		public ModifyInstanceHierarchyChange(AMLInstanceHierarchyImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			AMLInstanceHierarchyImpl instanceHierarchy = (AMLInstanceHierarchyImpl) getDocumentElement();
			this.oldName = instanceHierarchy.getName();
		}

		public void setNewName(String newName) {
			this.newName = newName;
		}

		@Override
		public void undo() throws Exception {
			AMLInstanceHierarchyImpl instanceHierarchy = (AMLInstanceHierarchyImpl) getDocumentElement();
			if (newName == null)
				newName = instanceHierarchy.getName();
			instanceHierarchy._setName(oldName);
		}

		@Override
		public void redo() throws Exception {
			AMLInstanceHierarchyImpl instanceHierarchy = (AMLInstanceHierarchyImpl) getDocumentElement();
			instanceHierarchy._setName(newName);
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyInstanceHierarchyChange))
				return false;
			ModifyInstanceHierarchyChange change = (ModifyInstanceHierarchyChange) _change;
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
		}

		@Override
		protected void notifyChangeListenersAfterUndo() {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
			document.notifyElementModified(getDocumentElement());
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

	private static class DeleteInstanceHierarchyChange extends AbstractDeleteDocumentElementChange<AbstractAMLDocumentElement> {
		private String instanceHierarchyName;

		public DeleteInstanceHierarchyChange(AMLInstanceHierarchyImpl instanceHierarchy) {
			super(instanceHierarchy);
			instanceHierarchyName = instanceHierarchy.getName();
		}

		@Override
		public void undo() throws Exception {
			AMLDocumentImpl document = (AMLDocumentImpl) parentIdentifier.getDocumentElement();
			AbstractAMLDocumentElement documentElement = document._createInstanceHierarchy(instanceHierarchyName);
			identifier.setDocumentElement(documentElement);
		}
	}
	

	private AMLDocumentImpl document;
	protected String name;
	private LinkedHashMap<UUID, AMLInternalElement> internalElements = new LinkedHashMap<UUID, AMLInternalElement>();
	private LinkedHashMap<UUID, AMLGroup> groups = new LinkedHashMap<UUID, AMLGroup>();
	private LinkedHashMap<UUID, AMLMirrorObject> mirrors = new LinkedHashMap<UUID, AMLMirrorObject>();

	AMLInstanceHierarchyImpl(AMLDocumentImpl amlDocument, String name) {
		this.document = amlDocument;
		this.name = name;
	}

	protected AMLDocumentElement _getBefore() {
		AMLDocumentImpl document = (AMLDocumentImpl) getParent();
		return document._getInstanceHierarchyBefore(this);
	}

	@Override
	public AMLDocumentElement getParent() {
		assertNotDeleted();
		return getDocument();
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
			DeleteInstanceHierarchyChange change = new DeleteInstanceHierarchyChange(this);
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
	}

	@Override
	public AMLInternalElement createInternalElement() throws AMLValidationException {
		return createInternalElement((AMLInternalElement)null);
	}

	public AMLValidationResultList validateNameChange(String newName) {
		return _validateNameChange(newName);
	}

	private AMLValidationResultList _validateNameChange(String newName) {
		if (newName != null && newName.equals(getName()))
			return AMLValidationResultList.EMPTY;

		if (newName.isEmpty())
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name must not be empty");

		boolean isClassAlreadyDefined = getDocument()._hasInstanceHierarchy(newName);
		if (isClassAlreadyDefined)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name already exists");

		AMLValidator validator = getSession().getValidator();
		if (validator == null)
			return new AMLValidationResultListImpl(this, Severity.OK, "");

		AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInstanceHierarchySetName(this, newName);
		validationResult.assertNotInternalValidationSeverity();

		return validationResult;
	}

	@Override
	public void setName(String newName) throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList validationResult = _validateNameChange(newName);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyInstanceHierarchyChange change = new ModifyInstanceHierarchyChange(this);
			change.setNewName(newName);
			getSavepointManager().addChange(change);
		}

		_setName(newName);

		getDocument().notifyElementModified(this);
	}

	protected void _setName(String newName) throws AMLValidationException {
		getDocument()._renameInstanceHierarchy(this, newName);
		this.name = newName;
	}

	@Override
	public String getName() {
		return name;
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
	public AMLDocumentImpl getDocument() {
		assertNotDeleted();
		return document;
	}

	@Override
	protected void _doDelete() throws AMLDocumentScopeInvalidException {
		getDocument()._removeInstanceHierarchy(name);
	}

	@Override
	public LinkedHashMap<UUID, AMLInternalElement> _getInternalElements() {
		return internalElements;
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {

		if (getInternalElementsCount() != 0)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "InstanceHierarchy has InternalElements.");

		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInstanceHierarchyDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
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
	public AMLGroup createGroup() throws AMLValidationException {
		assertNotDeleted();
		return AMLGroupContainerHelper.createGroup(this);
	}

	@Override
	public Iterable<AMLGroup> getGroups() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLGroup>(groups.values());
	}
	
	@Override
	public int getGroupsCount() {
		assertNotDeleted();
		return groups.size();
	}

	@Override
	public LinkedHashMap<UUID, AMLGroup> _getGroups() {
		return groups;
	}

	@Override
	public AMLGroup createGroup(UUID id) throws AMLValidationException {
		assertNotDeleted();
		return AMLGroupContainerHelper.createGroup(this, id);
	}


	protected void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		AMLDocumentImpl oldParent = (AMLDocumentImpl)oldParentElement;
		AMLDocumentImpl newParent = (AMLDocumentImpl)newParentElement;
		oldParent._removeInstanceHierarchy(getName());
		this.document = newParent;
		newParent._addInstanceHierarchy(this, (AMLInstanceHierarchyImpl)beforeElement, (AMLInstanceHierarchyImpl)afterElement);		
	}

	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		
		if (!(newParentElement instanceof AMLDocument))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "new parent is not a document");
		
		AMLDocumentImpl newParent = (AMLDocumentImpl)newParentElement;
		if (!oldParentElement.equals(newParentElement) && newParent.getInstanceHierarchy(getName()) != null)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "an instance hierarchy with name " + getName() + " already exists");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateInstanceHierarchyReparent((AMLDocument)oldParentElement, (AMLDocument)newParentElement, this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}
		
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public void validateIfIsInDocumentScope(AMLDocumentElement elementToReparent, AMLDocumentImpl document,
			AMLValidationResultListImpl result, Set<AMLDocumentElement> checkedElements) {
		if (checkedElements.contains(this))
			return;
		checkedElements.add(this);
		
		if (!isDescendantOf(elementToReparent) &&!getDocumentManager().getDocumentScope(document).isInDocumentScope(this))
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "instance hierarchy is not part of document scope"));
		
		
		for (AMLInternalElement internalElement : internalElements.values()) {
			((AMLInternalElementImpl)internalElement).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		for (AMLMirrorObject mirrorObject : mirrors.values()) {
			((AMLMirrorObjectImpl)mirrorObject).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		for (AMLGroup group : groups.values()) {
			((AMLGroupImpl)group).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
	}

	@Override
	public boolean isReferencedByDocumentElements(AMLDocument document, AMLDocument documentToRemove,
			AMLValidationResultListImpl result) {
		for (AMLInternalElement internalElement : getInternalElements()) {
			((AbstractAMLDocumentElement)internalElement).isReferencedByDocumentElements(document, documentToRemove, result);
		}
		
		for (AMLMirrorObject mirrorObject : mirrors.values()) {
			((AbstractAMLDocumentElement)mirrorObject).isReferencedByDocumentElements(document, documentToRemove, result);
		}
		
		for (AMLGroup group : getGroups()) {
			((AbstractAMLDocumentElement)group).isReferencedByDocumentElements(document, documentToRemove, result);
		}
		
		return false;
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkFrom)
			throws AMLValidationException {
		for (AMLInternalElement internalElement : getInternalElements()) {
			((AbstractAMLDocumentElement)internalElement)._doUnlink(unlinkFrom);
		}
		
		for (AMLMirrorObject mirrorObject : mirrors.values()) {
			((AbstractAMLDocumentElement)mirrorObject)._doUnlink(unlinkFrom);
		}
		
		for (AMLGroup group : getGroups()) {
			((AbstractAMLDocumentElement)group)._doUnlink(unlinkFrom);
		}		
	}

	@Override
	public void _doDeepDelete(AMLDocumentElement baseElement) throws AMLValidationException {
		
		AMLValidationResultListImpl validationResultList = new AMLValidationResultListImpl();
		doValidateDeepDelete(null, baseElement, validationResultList);
		if (validationResultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResultList);
		
		while (getInternalElements().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getInternalElements().iterator().next())._doDeepDelete(baseElement);
		}
		
		while (getMirrorObjects().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getMirrorObjects().iterator().next())._doDeepDelete(baseElement);
		}
		
		while (getGroups().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getGroups().iterator())._doDeepDelete(baseElement);
		}
		
		delete();
	}

	@Override
	protected void doValidateDeepDelete(
			AMLValidator validator, AMLDocumentElement baseElement, AMLValidationResultListImpl validationResultList) {
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInstanceHierarchyDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}

		for (AMLInternalElement internalElement : getInternalElements()) {
			((AbstractAMLDocumentElement)internalElement).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		
		for (AMLMirrorObject mirrorObject : mirrors.values()) {
			((AbstractAMLDocumentElement)mirrorObject).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		
		for (AMLGroup group : getGroups()) {
			((AbstractAMLDocumentElement)group).doValidateDeepDelete(validator, baseElement, validationResultList);
		}

	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement, AMLDocumentElement rootCopyElement, 
			AMLDocumentElement original, Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		
		mapping.put(original, this);
		
		for (AMLInternalElement internalElement : ((AMLInternalElementContainer) original).getInternalElements()) {
			AMLInternalElement copyInternalElement = createInternalElement();
			((AbstractAMLDocumentElement) copyInternalElement).deepCopy(rootElement, rootCopyElement, internalElement, mapping);
		}
		
		for (AMLInternalElement internalElement : ((AMLInternalElementContainer) original).getInternalElements()) {
			AMLInternalElement copyInternalElement = (AMLInternalElement) mapping.get(internalElement);
			if (copyInternalElement != null)
				((AMLInternalElementImpl) copyInternalElement).deepCopyMirrors(rootElement, rootCopyElement, internalElement, mapping);
		}		
		
		for (AMLMirrorObject mirrorObject : ((AMLMirrorContainer) original).getMirrorObjects()) {
			AMLInternalElement internalElement = mirrorObject.getInternalElement();
			if (mapping.containsKey(internalElement))
				internalElement = (AMLInternalElement) mapping.get(internalElement);
			AMLMirrorObject copy = createMirror(internalElement);
			((AbstractAMLDocumentElement) copy).deepCopy(rootElement, rootCopyElement, mirrorObject, mapping);
		}
		
		for (AMLGroup group : ((AMLGroupContainer) original).getGroups()) {
			AMLGroup copyGroup = createGroup();
			((AbstractAMLDocumentElement) copyGroup).deepCopy(rootElement, rootCopyElement, group, mapping);
		}
		
		for (AMLGroup group : ((AMLInternalGroupContainer) original).getGroups()) {
			AMLGroup copyGroup = (AMLGroup) mapping.get(group);
			if (copyGroup != null)
				((AMLInternalElementImpl) copyGroup).deepCopyMirrors(rootElement, rootCopyElement, group, mapping);
		}	

		
	}

	@Override
	public AMLInternalElement createInternalElement(
			AMLInternalElement internalElement) throws AMLValidationException {
		assertNotDeleted();
		AMLInternalElementImpl copy = AMLInternalElementContainerHelper.createInternalElement(this);
		
		if (internalElement != null) {
			Map<AMLDocumentElement, AMLDocumentElement> mapping = new HashMap<AMLDocumentElement, AMLDocumentElement>();
			copy.deepCopy(internalElement, copy, internalElement, mapping);
			copy.deepCopyMirrors(internalElement, copy, internalElement, mapping);
		}
		return copy;
	}

	@Override
	protected AMLDocumentElement _getAfter() {
		AMLDocumentImpl document = (AMLDocumentImpl) getParent();
		return document._getInstanceHierarchyAfter(this);
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
		assertNotDeleted();
		return mirrors;
	}

	@Override
	public AMLGroup createGroup(AMLGroup group) throws AMLValidationException {
		assertNotDeleted();
		AMLGroupImpl copy = (AMLGroupImpl) AMLGroupContainerHelper.createGroup(this);
		
		if (group != null) {
			Map<AMLDocumentElement, AMLDocumentElement> mapping = new HashMap<AMLDocumentElement, AMLDocumentElement>();
			copy.deepCopy(group, copy, group, mapping);
			copy.deepCopyMirrors(group, copy, group, mapping);
		}
		return copy;
	}
	
	@Override
	public AMLValidationResultList validateGroupCreate() {
		assertNotDeleted();
		return AMLGroupContainerHelper.validateCreateGroup(this); 
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
}