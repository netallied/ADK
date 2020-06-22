/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLGroup;
import org.automationml.aml.AMLMirrorObject;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractCreateDocumentElementChange;

public class AMLGroupContainerHelper {

	private static class CreateGroupChange extends AbstractCreateDocumentElementChange {
		private UUID id;

		public CreateGroupChange(AMLGroupImpl documentElement) {
			super(documentElement);
			this.id = documentElement.getId();
		}

		@Override
		public void redo() throws Exception {
			AMLInternalGroupContainer container = (AMLInternalGroupContainer) parentIdentifier.getDocumentElement();
			AMLGroupImpl clazz = (AMLGroupImpl) AMLGroupContainerHelper._createGroup(container, id);
			identifier.setDocumentElement(clazz);
		}
	}

	static AMLGroup createGroup(AMLInternalGroupContainer container) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		UUID newId = UUID.randomUUID();
		while (document.getDocumentManager().isUniqueIdDefined(container, newId))
			newId = UUID.randomUUID();

		return createGroup(container, newId);
	}

	static AMLGroup createGroup(AMLInternalGroupContainer container, UUID newId) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		AMLValidationResultList resultList = validateCreateGroup(container, newId);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		document.notifyElementValidated(resultList);

		AMLGroupImpl group = _createGroup(container, newId);

		if (((AMLSessionImpl) container.getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateGroupChange change = new CreateGroupChange(group);
			((AMLSessionImpl) container.getSession()).getSavepointManager().addChange(change);
		}

		document.notifyElementCreated(group, container);

		return group;
	}

	static AMLGroupImpl _createGroup(AMLInternalGroupContainer container, UUID id) throws AMLValidationException {
		AMLGroupImpl group = new AMLGroupImpl(container, id);
		container._getGroups().put(id, group);

		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();
		document.getDocumentManager().addUniqueId(group, id);
		return group;
	}

	static AMLValidationResultList validateCreateGroup(AMLInternalGroupContainer container, UUID newId) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();
		if (document.getDocumentManager().isUniqueIdDefined(container, newId))
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Id already exists");

		return validateCreateGroup(container);
	}

	static AMLValidationResultList validateCreateGroup(AMLInternalGroupContainer container)  {
		
		AMLRoleClass groupRole = container.getDocument().getRoleClassByPath(AMLGroup.AUTOMATION_ML_ROLE_GROUP_PATH);
		if (groupRole == null)
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "RoleClass \"Group\" is not available");

		AMLValidator validator = container.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateGroupCreate(container);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	static AMLValidationResultList validateDeleteGroup(AMLGroupImpl group) {
		if (group.getGroups().iterator().hasNext())
			return new AMLValidationResultListImpl(group, Severity.AML_ERROR, "Group has Groups.");

		if (group.getAttributes().iterator().hasNext())
			return new AMLValidationResultListImpl(group, Severity.AML_ERROR, "Group has Attributes.");

		if (group.getMirrorObjectsCount() > 0)
			return new AMLValidationResultListImpl(group, Severity.AML_ERROR, "Group has MirrorElement.");

		AMLValidator validator = group.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateGroupDelete(group);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(group, Severity.OK, "");
	}

	public static void _removeGroup(AMLInternalGroupContainer internalGroupContainer, AMLGroupImpl group) {
		internalGroupContainer._getGroups().remove(group.getId());		
	}
	
	public static void _addGroup(AMLInternalGroupContainer parent,
			AMLGroupImpl amlGroup,
			AMLGroup beforeElement, AMLGroup afterElement) {
	
		if (beforeElement == null && afterElement == null) {
			parent._getGroups().put(amlGroup.getId(), amlGroup);
			return;
		}
		Map<UUID, AMLGroup> groups = new LinkedHashMap<UUID, AMLGroup>();
		for (UUID id : parent._getGroups().keySet()) {
			if (beforeElement != null && beforeElement.getId().equals(id))
				groups.put(amlGroup.getId(), amlGroup);
			groups.put(id, parent._getGroups().get(id));
			if (afterElement != null && afterElement.getId().equals(id))
				groups.put(amlGroup.getId(), amlGroup);
		}
		parent._getGroups().clear();
		parent._getGroups().putAll(groups);		
	}

	static AMLDocumentElement _getGroupBefore(
			AMLInternalGroupContainer internalGroupContainer,
			AMLGroupImpl amlGroupImpl) {
		ListIterator<UUID> iterator = new ArrayList<UUID>(internalGroupContainer._getGroups().keySet()).listIterator();
		while (iterator.hasNext()) {
			if (amlGroupImpl.getId().equals(iterator.next())) {
				AMLGroup previous = (AMLGroup) internalGroupContainer._getGroups().get(iterator.previous());
				if (previous.equals(amlGroupImpl))
					return null;
				else
					return previous;
			}
		}
		return null;		
	}

	public static AMLDocumentElement _getGroupAfter(
			AMLInternalGroupContainer internalGroupContainer,
			AMLGroupImpl amlGroupImpl) {
		ListIterator<UUID> iterator = new ArrayList<UUID>(internalGroupContainer._getGroups().keySet()).listIterator();
		while (iterator.hasNext()) {
			if (amlGroupImpl.getId().equals(iterator.next())) {
				if (iterator.hasNext()) {
					AMLGroup after = (AMLGroup) internalGroupContainer._getGroups().get(iterator.next());
					if (after.equals(amlGroupImpl))
						return null;
					else
						return after;
				}
				return null;
			}
		}
		return null;		
	}

}
