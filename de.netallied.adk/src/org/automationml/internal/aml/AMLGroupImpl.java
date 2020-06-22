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

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLGroup;
import org.automationml.aml.AMLGroupContainer;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLMirrorContainer;
import org.automationml.aml.AMLMirrorObject;
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

public class AMLGroupImpl extends AMLElementImpl implements AMLGroup, AMLInternalGroupContainer, AMLInternalAttributeContainer,
		AMLInternalExternalInterfaceContainer, AMLInternalMirrorContainer {

	private static class ModifyGroupNameChange extends AbstractDocumentElementChange<AMLGroupImpl> {
		private String oldName;
		private String newName;

		public ModifyGroupNameChange(AMLGroupImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			AMLGroupImpl group = (AMLGroupImpl) getDocumentElement();
			this.oldName = group.getName();
		}

		public void setNewName(String newName) {
			this.newName = newName;
		}

		@Override
		public void undo() throws Exception {
			AMLGroupImpl group = (AMLGroupImpl) getDocumentElement();
			if (newName == null)
				newName = group.getName();
			group._setName(oldName);
		}

		@Override
		public void redo() throws Exception {
			AMLGroupImpl group = (AMLGroupImpl) getDocumentElement();
			group._setName(newName);
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyGroupNameChange))
				return false;
			ModifyGroupNameChange change = (ModifyGroupNameChange) _change;
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

	protected static class DeleteGroupChange extends AbstractDeleteDocumentElementChange<AMLGroupImpl> {
		private UUID id;

		public DeleteGroupChange(AMLGroupImpl element) {
			super(element);
			id = element.getId();
		}

		@Override
		public void undo() throws Exception {
			AMLInternalGroupContainer container = (AMLInternalGroupContainer) parentIdentifier.getDocumentElement();
			AMLGroupImpl clazz = (AMLGroupImpl) AMLGroupContainerHelper._createGroup(container, id);
			identifier.setDocumentElement(clazz);
		}
	}

	private AMLInternalGroupContainer internalGroupContainer;
	private UUID id;
	private String name;
	private LinkedHashMap<UUID, AMLGroup> groups = new LinkedHashMap<UUID, AMLGroup>();
	private LinkedHashMap<UUID, AMLMirrorObject> mirrors = new LinkedHashMap<UUID, AMLMirrorObject>();
	private Map<String, AMLAttribute> attributes = new LinkedHashMap<String, AMLAttribute>();
	private Map<UUID, AMLExternalInterface> externalInterfaces = new LinkedHashMap<UUID, AMLExternalInterface>();

	public AMLGroupImpl(AMLInternalGroupContainer container, UUID id) {
		this.internalGroupContainer = container;
		this.id = id;
	}

	@Override
	public AMLDocumentElement getParent() {
		return internalGroupContainer;
	}

	@Override
	public void delete() throws AMLValidationException {
		if (isDeleted())
			return;

		AMLValidationResultList validationResult = AMLGroupContainerHelper.validateDeleteGroup(this);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			DeleteGroupChange change = new DeleteGroupChange(this);
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
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
	public UUID getId() {
		assertNotDeleted();
		return id;
	}

	@Override
	public AMLDocumentImpl getDocument() {
		return (AMLDocumentImpl) internalGroupContainer.getDocument();
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		AMLGroupContainerHelper._removeGroup(internalGroupContainer, this);
		getDocumentManager().removeUniqueId(getDocument(), id);
	}

	@Override
	public LinkedHashMap<UUID, AMLGroup> _getGroups() {
		return groups;
	}

	@Override
	public AMLValidationResultList validateCreateAttribute(String name) {
		return AMLAttributeContainerHelper._validateCreateAttribute(this, name);
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
	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}

	@Override
	public AMLAttribute getAttribute(String name) {
		assertNotDeleted();
		return AMLAttributeContainerHelper.getAttribute(this, name);
	}

	@Override
	public AMLAttribute createAttribute(String name) throws AMLValidationException {
		assertNotDeleted();
		return AMLAttributeContainerHelper.createAttribute(this, name);
	}

	@Override
	public Map<String, AMLAttribute> _getAttributes() {
		return attributes;
	}

	// @Override
	// public AMLAttributeImpl createAttribute(AMLAttributeImpl attribute) throws AMLValidationException {
	// return null;
	// }

	@Override
	public AMLMirrorObject createMirror(UUID id, AMLInternalElement internalElement) throws AMLValidationException {
		assertNotDeleted();
		return AMLMirrorContainerHelper.createMirror(this, id, internalElement);
		
	}

	@Override
	public AMLMirrorObject createMirror(AMLInternalElement internalElement) throws AMLValidationException {
		assertNotDeleted();
		return AMLMirrorContainerHelper.createMirror(this, internalElement);
	}

	@Override
	public Iterable<AMLMirrorObject> getMirrorObjects() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLMirrorObject>(mirrors.values());
	}

	@Override
	public AMLValidationResultList validateMirrorCreate(UUID id, AMLInternalElement internalElement) throws AMLValidationException {
		assertNotDeleted();
		return AMLMirrorContainerHelper.validateMirrorCreate(this, id, internalElement);
		
	}

	@Override
	public AMLValidationResultList validateMirrorCreate(AMLInternalElement internalElement) {
		assertNotDeleted();
		return AMLMirrorContainerHelper.validateMirrorCreate(this, internalElement);		
	}

	@Override
	public int getMirrorObjectsCount() {
		assertNotDeleted();
		return mirrors.size();
	}

	void _removeMirror(AMLMirrorObjectImpl mirror) {
		mirrors.remove(mirror.getId());
	}

	@Override
	public AMLGroup createGroup(UUID id) throws AMLValidationException {
		assertNotDeleted();
		return AMLGroupContainerHelper.createGroup(this, id);
	}

	@Override
	public AMLValidationResultList validateCreateExternalInterface(AMLInterfaceClass interfaceClass) {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.validateCreateExternalInterface(this, interfaceClass);
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
	public AMLExternalInterface getExternalInterface(UUID id) {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.getExternalInterface(this, id);
	}

	@Override
	public AMLExternalInterface createExternalInterface(AMLInterfaceClass interfaceClass) throws AMLValidationException {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.createExternalInterface(this, interfaceClass, true);
	}

	@Override
	public AMLExternalInterface createExternalInterface(UUID id, AMLInterfaceClass interfaceClass) throws AMLValidationException {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.createExternalInterface(this, interfaceClass, id, true);
	}

	@Override
	public Map<UUID, AMLExternalInterface> _getExternalInterfaces() {
		return externalInterfaces;
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
			ModifyGroupNameChange change = new ModifyGroupNameChange(this);
			change.setNewName(newName);
			getSavepointManager().addChange(change);
		}
		_setName(newName);
		getDocument().notifyElementModified(this);
	}

	@Override
	public AMLValidationResultList validateNameChange(String newName) {
		assertNotDeleted();

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateGroupSetName(this, newName);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	private void _setName(String newName) {
		this.name = newName;
	}

	@Override
	public String getName() {
		assertNotDeleted();
		return name;
	}

	public LinkedHashMap<UUID, AMLMirrorObject> _getMirrors() {
		return mirrors;
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
//		AMLValidationResultList validationResult = internalGroupContainer.validateDeleteGroup(this);
//		if (validationResult.isAnyOperationNotPermitted())
//			return validationResult;

		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateGroupDelete(this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
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
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		if (!(newParentElement instanceof AMLInternalGroupContainer))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "new parent is not a container for groups");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateGroupReparent((AMLGroupContainer)oldParentElement, (AMLGroupContainer)newParentElement, this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}
		
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		AMLInternalGroupContainer oldParent = (AMLInternalGroupContainer)oldParentElement;
		AMLInternalGroupContainer newParent = (AMLInternalGroupContainer)newParentElement;
		AMLGroupContainerHelper._removeGroup(oldParent, this);
		this.internalGroupContainer = newParent;
		AMLGroupContainerHelper._addGroup(newParent, this, (AMLGroup)beforeElement, (AMLGroup)afterElement);
	}

	@Override
	protected AMLDocumentElement _getBefore() {
		AMLInternalGroupContainer internalGroupContainer = (AMLInternalGroupContainer) getParent();
		return AMLGroupContainerHelper._getGroupBefore(internalGroupContainer, this);
	}

	@Override
	protected AMLDocumentElement _getAfter() {
		AMLInternalGroupContainer internalGroupContainer = (AMLInternalGroupContainer) getParent();
		return AMLGroupContainerHelper._getGroupAfter(internalGroupContainer, this);
	}

	@Override
	public void validateIfIsInDocumentScope(
			AMLDocumentElement elementToReparent, AMLDocumentImpl document,
			AMLValidationResultListImpl result,
			Set<AMLDocumentElement> checkedElements) {
		
		if (checkedElements.contains(this))
			return;
		checkedElements.add(this);
		
		AMLRoleClass groupRole = document.getRoleClassByPath(AUTOMATION_ML_ROLE_GROUP_PATH);
		if (groupRole == null)
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "RoleClass \"Group\" is not available"));
		
		if (!isDescendantOf(elementToReparent) &&!getDocumentManager().getDocumentScope(document).isInDocumentScope(this))
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "mirror object is not part of document scope"));
		
		for (AMLGroup group : groups.values()) {
			((AMLGroupImpl)group).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		
		for (AMLMirrorObject mirrorObject : mirrors.values()) {
			((AMLMirrorObjectImpl)mirrorObject).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
		
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkFrom)
			throws AMLValidationException {
		
		for (AMLGroup group : groups.values()) {
			((AbstractAMLDocumentElement)group)._doUnlink(unlinkFrom);
		}
		
		for (AMLMirrorObject mirrorObject : mirrors.values()) {
			((AbstractAMLDocumentElement) mirrorObject)._doUnlink(unlinkFrom);
		}
		
	}

	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateGroupDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
		
		for (AMLGroup group : getGroups()) {
			((AbstractAMLDocumentElement)group).doValidateDeepDelete(validator, baseElement, validationResultList);
		}	
		
		for (AMLMirrorObject mirrorObject : getMirrorObjects()) {
			((AbstractAMLDocumentElement)mirrorObject).doValidateDeepDelete(validator, baseElement, validationResultList);
		}		
		
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		
		mapping.put(original, this);
		
		setName(((AMLGroupImpl) original).getName());
		
		
		for (AMLGroup group : ((AMLGroupContainer) original).getGroups()) {
			if (group.equals(rootCopyElement))
				continue;
			AMLGroup copyGroup = createGroup();
			((AbstractAMLDocumentElement)  copyGroup).deepCopy(rootElement, rootCopyElement, group, mapping);
		}
			
	}
	
	public void deepCopyMirrors(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		for (AMLMirrorObject mirrorObject : ((AMLMirrorContainer) original).getMirrorObjects()) {
			AMLInternalElement internalElement = mirrorObject.getInternalElement();
			if (mapping.containsKey(internalElement))
				internalElement = (AMLInternalElement) mapping.get(internalElement);
			AMLMirrorObject copyMirror = createMirror(internalElement);
			((AbstractAMLDocumentElement)mirrorObject).deepCopy(rootElement, rootCopyElement, copyMirror, mapping);
		}
		
		for (AMLGroup group : getGroups()) {
			((AMLGroupImpl) group).deepCopyMirrors(rootElement, rootCopyElement, group, mapping);
		}
	}

	@Override
	protected void _doDeepDelete(AMLDocumentElement baseElement)
			throws AMLValidationException {
		
		AMLValidationResultListImpl validationResultList = new AMLValidationResultListImpl();
		doValidateDeepDelete(null, baseElement, validationResultList);
		if (validationResultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResultList);
		
		while (getGroups().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getGroups().iterator().next())._doDeepDelete(baseElement);
		}	
		
		while (getMirrorObjects().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getMirrorObjects().iterator().next())._doDeepDelete(baseElement);
		}	
		
		
		delete();
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

}
