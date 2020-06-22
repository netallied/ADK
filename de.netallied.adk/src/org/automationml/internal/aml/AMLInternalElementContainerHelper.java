/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalElementContainer;
import org.automationml.aml.AMLInternalLink;
import org.automationml.aml.AMLMappingObject;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLSystemUnitClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractCreateDocumentElementChange;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;

public class AMLInternalElementContainerHelper {

	private static class CreateInternalElementChange extends AbstractCreateDocumentElementChange {
		private UUID id;

		public CreateInternalElementChange(AMLInternalElementImpl documentElement) {
			super(documentElement);
			this.id = documentElement.getId();
		}

		@Override
		public void redo() throws Exception {
			AMLInternalInternalElementContainer container = (AMLInternalInternalElementContainer) parentIdentifier
					.getDocumentElement();
			AMLInternalElementImpl clazz = (AMLInternalElementImpl) AMLInternalElementContainerHelper
					._createInternalElement(container, id);
			identifier.setDocumentElement(clazz);
		}
	}

	private static class CreateMappingObjectChange extends AbstractCreateDocumentElementChange {

		Identifier<AMLRoleClassImpl> roleClass;

		public CreateMappingObjectChange(AMLMappingObjectImpl documentElement) {
			super(documentElement);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLRoleClassImpl roleClass = (AMLRoleClassImpl) ((AMLMappingObjectImpl) getDocumentElement())
					.getRoleClass();
			this.roleClass = (Identifier<AMLRoleClassImpl>) identifierManager.getIdentifier(roleClass);
		}

		@Override
		public void redo() throws Exception {
			AMLInternalSupportedRoleClassContainer container = (AMLInternalSupportedRoleClassContainer) parentIdentifier
					.getDocumentElement();

			AMLRoleClass roleClass = this.roleClass.getDocumentElement();

			AMLMappingObjectImpl clazz = (AMLMappingObjectImpl) AMLInternalElementContainerHelper
					._createMappingObject(container, roleClass);
			identifier.setDocumentElement(clazz);
		}

		@Override
		protected void _delete() {
			super._delete();
			if (roleClass != null)
				roleClass.release();
			roleClass = null;
		}

	}

	static void _removeInternalElement(AMLInternalInternalElementContainer internalElementContainer,
			AMLInternalElement amlInternalElement) {
		internalElementContainer._getInternalElements().remove(amlInternalElement.getId());
	}

	static AMLInternalElementImpl _createInternalElement(AMLInternalInternalElementContainer container, UUID id)
			throws AMLDocumentScopeInvalidException {

		AMLInternalElementImpl internalElement = new AMLInternalElementImpl(container, id);
		container._getInternalElements().put(id, internalElement);

		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();
		document.getDocumentManager().addUniqueId(internalElement, id);
		return internalElement;
	}

	static AMLInternalElementImpl createInternalElement(AMLInternalInternalElementContainer container)
			throws AMLValidationException {

		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		UUID newId = UUID.randomUUID();
		while (document.getDocumentManager().isUniqueIdDefined(container, newId))
			newId = UUID.randomUUID();

		return createInternalElement(container, newId);
	}

	static AMLInternalElementImpl createInternalElement(AMLInternalInternalElementContainer container, UUID id)
			throws AMLValidationException {

		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		AMLValidationResultList resultList = validateCreateInternalElement(container, id);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		document.notifyElementValidated(resultList);

		AMLInternalElementImpl internalElement = _createInternalElement(container, id);

		if (((AMLSessionImpl) container.getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateInternalElementChange change = new CreateInternalElementChange(internalElement);
			((AMLSessionImpl) container.getSession()).getSavepointManager().addChange(change);
		}

		document.notifyElementCreated(internalElement, container);

		return internalElement;
	}

	static AMLValidationResultList validateCreateInternalElement(AMLInternalInternalElementContainer container, UUID id)
			throws AMLValidationException {

		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();
		if (document.getDocumentManager().isUniqueIdDefined(container, id))
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Id already exists");

		return validateCreateInternalElement(container);
	}

	static AMLValidationResultList validateCreateInternalElement(AMLInternalInternalElementContainer container) {

		AMLValidator validator = container.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator
					.validateInternalElementCreate(container);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	static AMLValidationResultList validateDeleteInternalElement(AMLInternalElementImpl internalElement) {
		if (internalElement.getInternalElements().iterator().hasNext())
			return new AMLValidationResultListImpl(internalElement, Severity.AML_ERROR,
					"InternalElement has InternalElements.");

		if (internalElement.getAttributes().iterator().hasNext())
			return new AMLValidationResultListImpl(internalElement, Severity.AML_ERROR,
					"InternalElement has Attributes.");

		if (internalElement.getSupportedRoleClassesCount() > 0)
			return new AMLValidationResultListImpl(internalElement, Severity.AML_ERROR,
					"InternalElement has SupportedRoleClass elements.");

		if (internalElement.getRoleRequirements() != null)
			return new AMLValidationResultListImpl(internalElement, Severity.AML_ERROR,
					"InternalElement has RoleRequirements.");

		if (internalElement.getInternalLinkCount() > 0)
			return new AMLValidationResultListImpl(internalElement, Severity.AML_ERROR,
					"InternalElement has InternalLink elements.");

		if (internalElement.getMirrorObjectsCount() > 0)
			return new AMLValidationResultListImpl(internalElement, Severity.AML_ERROR,
					"InternalElement has mirror elements.");

		if (internalElement.getMirrorCount() > 0)
			return new AMLValidationResultListImpl(internalElement, Severity.AML_ERROR,
					"InternalElement has mirrored referrers.");

		if (internalElement.getFacetsCount() > 0)
			return new AMLValidationResultListImpl(internalElement, Severity.AML_ERROR, "InternalElement has facets.");

		AMLValidator validator = internalElement.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator
					.validateInternalElementDelete(internalElement);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(internalElement, Severity.OK, "");
	}

	static AMLMappingObject createMappingObject(AMLInternalSupportedRoleClassContainer container,
			AMLRoleClass roleClass) {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		AMLMappingObjectImpl mappingObject = _createMappingObject(container, roleClass);

		if (((AMLSessionImpl) container.getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateMappingObjectChange change = new CreateMappingObjectChange(mappingObject);
			((AMLSessionImpl) container.getSession()).getSavepointManager().addChange(change);
		}

		document.notifyElementCreated(mappingObject, container);

		return mappingObject;
	}

	static AMLMappingObjectImpl _createMappingObject(AMLInternalSupportedRoleClassContainer container,
			AMLRoleClass roleClass) {
		AMLMappingObjectImpl mappingObject = new AMLMappingObjectImpl(container, roleClass);

		return mappingObject;
	}

	public static void _addInternalElement(AMLInternalInternalElementContainer parent,
			AMLInternalElementImpl internalElement, AMLInternalElement beforeElement, AMLInternalElement afterElement) {

		if (beforeElement == null && afterElement == null) {
			parent._getInternalElements().put(internalElement.getId(), internalElement);
			return;
		}
		Map<UUID, AMLInternalElement> internalElements = new LinkedHashMap<UUID, AMLInternalElement>();
		for (UUID id : parent._getInternalElements().keySet()) {
			if (beforeElement != null && beforeElement.getId().equals(id))
				internalElements.put(internalElement.getId(), internalElement);
			internalElements.put(id, parent._getInternalElements().get(id));
			if (afterElement != null && afterElement.getId().equals(id))
				internalElements.put(internalElement.getId(), internalElement);
		}
		parent._getInternalElements().clear();
		parent._getInternalElements().putAll(internalElements);
	}

	public static AMLDocumentElement _getInternalElementBefore(
			AMLInternalInternalElementContainer internalElementContainer, AMLInternalElement internalElement) {
		ListIterator<UUID> iterator = new ArrayList<UUID>(internalElementContainer._getInternalElements().keySet())
				.listIterator();
		while (iterator.hasNext()) {
			if (internalElement.getId().equals(iterator.next())) {
				AMLInternalElement previous = (AMLInternalElement) internalElementContainer._getInternalElements()
						.get(iterator.previous());
				if (previous.equals(internalElement))
					return null;
				else
					return previous;
			}
		}
		return null;
	}

	public static AMLDocumentElement _getInternalElementAfter(
			AMLInternalInternalElementContainer internalElementContainer, AMLInternalElementImpl internalElement) {
		ListIterator<UUID> iterator = new ArrayList<UUID>(internalElementContainer._getInternalElements().keySet())
				.listIterator();
		while (iterator.hasNext()) {
			if (internalElement.getId().equals(iterator.next())) {
				if (iterator.hasNext()) {
					AMLInternalElement after = (AMLInternalElement) internalElementContainer._getInternalElements()
							.get(iterator.next());
					if (after.equals(internalElement))
						return null;
					else
						return after;
				}
				return null;
			}
		}
		return null;
	}

	static AMLValidationResultList validateCreateInternalElement(AMLInternalElementContainer container,
			AMLSystemUnitClass systemUnitClass) {

		AMLValidator validator = container.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator
					.validateInternalElementCreate(container, systemUnitClass);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	static AMLInternalElement createInternalElement(AMLInternalElementContainer container,
			AMLSystemUnitClass systemUnitClass) throws AMLValidationException {

		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		UUID newId = UUID.randomUUID();
		while (document.getDocumentManager().isUniqueIdDefined(container, newId))
			newId = UUID.randomUUID();

		AMLInternalElement internalElement = createInternalElement((AMLInternalInternalElementContainer) container,
				newId);

		internalElement.setBaseSystemUnitClass(systemUnitClass);

		Map<AMLDocumentElement, AMLDocumentElement> mapping = new HashMap<AMLDocumentElement, AMLDocumentElement>();

		AMLSystemUnitClass currentSystemUnitClass = systemUnitClass;
		while (currentSystemUnitClass != null) {
			for (AMLAttribute attribute : currentSystemUnitClass.getAttributes()) {
				if (internalElement.getAttribute(attribute.getName()) == null) {
					AMLAttributeImpl copy = (AMLAttributeImpl) internalElement.createAttribute(attribute.getName(),
							attribute);
					copy.deepCopy(attribute, copy, attribute, mapping);
				}
			}

			for (AMLExternalInterface externalInterface : currentSystemUnitClass.getExternalInterfaces()) {
				// AMLExternalInterfaceImpl copy = (AMLExternalInterfaceImpl)
				// internalElement.createExternalInterface(externalInterface.getInterfaceClass());
				AMLExternalInterfaceImpl copy = (AMLExternalInterfaceImpl) AMLExternalInterfaceContainerHelper
						.createExternalInterface((AMLInternalExternalInterfaceContainer) internalElement,
								externalInterface.getInterfaceClass(), false);
				copy.deepCopy(externalInterface, copy, externalInterface, mapping);
			}

			for (AMLSupportedRoleClass supportedRoleClass : currentSystemUnitClass.getSupportedRoleClasses()) {
				if (internalElement.getSupportedRoleClass(supportedRoleClass.getRoleClass()) == null) {
					AMLSupportedRoleClassImpl copy = (AMLSupportedRoleClassImpl) internalElement
							.createSupportedRoleClass(supportedRoleClass.getRoleClass());
					copy.deepCopy(supportedRoleClass, copy, supportedRoleClass, mapping);
				}
			}

			for (AMLInternalElement innerInternalElement : currentSystemUnitClass.getInternalElements()) {
				AMLInternalElementImpl copy = (AMLInternalElementImpl) ((AMLInternalElementImpl) internalElement)
						._createInternalElement(innerInternalElement, mapping);
				// copy.deepCopy(innerInternalElement, copy,
				// innerInternalElement, mapping);
			}

			for (AMLInternalLink internalLink : currentSystemUnitClass.getInternalLinks()) {
				AMLExternalInterface a = internalLink.getRefPartnerSideA();
				AMLExternalInterface b = internalLink.getRefPartnerSideB();
				if (mapping.containsKey(a))
					a = (AMLExternalInterface) mapping.get(a);
				else if (!((AbstractAMLDocumentElement) a).isDescendantOf(currentSystemUnitClass.getDocument()))
					a = null;
				if (mapping.containsKey(b))
					b = (AMLExternalInterface) mapping.get(b);
				else if (!((AbstractAMLDocumentElement) b).isDescendantOf(currentSystemUnitClass.getDocument()))
					b = null;
				if (a != null && b != null) {
					AMLInternalLink copyInternalLink = internalElement.createInternalLink(internalLink.getName(), a, b);
					((AbstractAMLDocumentElement) copyInternalLink).deepCopy(internalLink, copyInternalLink,
							internalLink, mapping);
				}
			}

			currentSystemUnitClass = currentSystemUnitClass.getBaseSystemUnitClass();
		}

		return internalElement;
	}

}
