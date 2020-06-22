/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttributeContainer;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLFacet;
import org.automationml.aml.AMLFrameAttribute;
import org.automationml.aml.AMLFrameAttributeContainer;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLNameAlreadyInUseException;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLRoleRequirements;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractCreateDocumentElementChange;
import org.automationml.internal.ReadOnlyIterable;

class AMLAttributeContainerHelper {

	static class CreateAttributeChange extends AbstractCreateDocumentElementChange {
		private String name;

		public CreateAttributeChange(AMLAttributeImpl documentElement) {
			super(documentElement);
			this.name = documentElement.getName();
		}

		@Override
		public void redo() throws Exception {
			AMLInternalAttributeContainer container = (AMLInternalAttributeContainer) parentIdentifier.getDocumentElement();
			AMLAttributeImpl clazz = (AMLAttributeImpl) AMLAttributeContainerHelper._createAttribute(container, name);
			identifier.setDocumentElement(clazz);
		}
	}

	private static class CreateFrameAttributeChange extends AbstractCreateDocumentElementChange {

		public CreateFrameAttributeChange(AMLFrameAttributeImpl documentElement) {
			super(documentElement);
		}

		@Override
		public void redo() throws Exception {
			AMLInternalFrameAttributeContainer container = (AMLInternalFrameAttributeContainer) parentIdentifier.getDocumentElement();
			AMLFrameAttributeImpl clazz = (AMLFrameAttributeImpl) AMLAttributeContainerHelper._createFrameAttribute(container);
			identifier.setDocumentElement(clazz);
		}
	}

	static AMLAttribute getAttribute(AMLInternalAttributeContainer container, String name) {
		AMLAttribute attribute = container._getAttributes().get(name);
		return attribute;
	}

	static void removeAttribute(AMLInternalAttributeContainer container, AMLAttribute attribute) {
		// TODO: remove mappings
		// finde RoleClass
		// get SupportedRoleClass
		// get Mappings
		// remove Mapping
		_removeAttribute(container, attribute);
	}

	static void _removeAttribute(AMLInternalAttributeContainer container, AMLAttribute attribute) {
		if (container instanceof AMLRoleRequirementsImpl)
			((AMLRoleRequirementsImpl)container)._removeAttribute(attribute);
		else
			container._getAttributes().remove(attribute.getName());
	}

	static AMLAttributeImpl createAttribute(AMLInternalAttributeContainer container, String name) throws AMLValidationException {

		AMLValidationResultList resultList = validateCreateAttribute(container, name);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		((AMLDocumentImpl) container.getDocument()).notifyElementValidated(resultList);

		AMLAttributeImpl attribute = _createAttribute(container, name);

		if (((AMLSessionImpl) container.getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateAttributeChange change = new CreateAttributeChange(attribute);
			((AMLSessionImpl) container.getSession()).getSavepointManager().addChange(change);
		}

		((AMLDocumentImpl) container.getDocument()).notifyElementCreated(attribute, container);

		return attribute;
	}

	static AMLValidationResultList validateCreateAttribute(AMLInternalAttributeContainer container, String name) {
		return container.validateCreateAttribute(name);
	}

	static AMLValidationResultList _validateCreateAttribute(AMLInternalAttributeContainer container, String name) {
		if (container._getAttributes().containsKey(name))
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Name already exists");
		
		if (container instanceof AMLInternalFrameAttributeContainer && name.equals("Frame"))
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Name \"Frame\" not allowed.");
		
//		if (container instanceof AMLExternalInterface) {
//			AMLExternalInterface externalInterface = (AMLExternalInterface)container;
//			if (externalInterface.getInterfaceClass().getAttribute(name) == null)
//				return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Attribute does not exist in Interface class");
//		}

		AMLValidator validator = container.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateAttributeCreate(container, name);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	static AMLAttributeImpl _createAttribute(AMLInternalAttributeContainer container, String name) throws AMLValidationException {
		if (container instanceof AMLRoleRequirements) {
			throw new AMLValidationException(new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Not allowed"));
		} else {
			AMLAttributeImpl attribute = new AMLAttributeImpl(container);
			attribute._setName(name);
			container._getAttributes().put(name, attribute);
			return attribute;
		}		
	}

	static Iterable<AMLAttribute> getAttributes(Map<String, AMLAttribute> attributes) {
		return new ReadOnlyIterable<AMLAttribute>(attributes.values());
	}

	static AMLValidationResultList validateRenameAttribute(AMLInternalAttributeContainer container, AMLAttributeImpl attribute, String newName) {

		if (newName != null && newName.equals(attribute.getName()))
			return AMLValidationResultList.EMPTY;
		if (newName.isEmpty())
			return new AMLValidationResultListImpl(attribute, Severity.AML_ERROR, "Name must not be empty");

		if (container._getAttributes().containsKey(newName))
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Name already exists");

		AMLValidator validator = container.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateAttributeSetName(attribute, newName);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(attribute, Severity.OK, "");
	}

	static void renameAttribute(AMLInternalAttributeContainer attributeContainer, AMLAttributeImpl amlAttributeImpl, String name) {
		_renameAttribute(attributeContainer, amlAttributeImpl, name);
	}

	static void _renameAttribute(AMLInternalAttributeContainer attributeContainer, AMLAttribute amlAttribute, String name) {
		if (attributeContainer instanceof AMLRoleRequirementsImpl) {
			((AMLRoleRequirementsImpl)attributeContainer)._renameAttribute(amlAttribute, name);
		} else {
			AMLAttribute att = attributeContainer._getAttributes().get(amlAttribute.getName());
			if (att == amlAttribute) {
				attributeContainer._getAttributes().remove(att.getName());
				attributeContainer._getAttributes().put(name, att);
			}
		}
	}

	static AMLValidationResultList validateDeleteAttribute(AMLAttributeImpl attribute) {

		if (attribute.getAttributesCount() != 0)
			return new AMLValidationResultListImpl(attribute, Severity.AML_ERROR, "Attribute has child attributes.");

		AMLAttributeContainer container = attribute;
		List<String> attributePath = new ArrayList<String>();
		while (container instanceof AMLAttribute) {
			attributePath.add(0, ((AMLAttribute) container).getName());
			container = (AMLAttributeContainer) container.getParent();
		}
//		if (container instanceof AMLSupportedRoleClassContainer) {
//			AMLSupportedRoleClassContainer supportedRoleClassContainer = (AMLSupportedRoleClassContainer) container;
//			for (AMLSupportedRoleClass supportedRoleClass : supportedRoleClassContainer.getSupportedRoleClasses()) {
//				AMLRoleClass roleClass = supportedRoleClass.getRoleClass();
//				AMLAttributeContainer attributeContainer = (AMLAttributeContainer) roleClass;
//				AMLAttribute checkRoleAttribute = null;
//				for (int i = 0; i < attributePath.size(); i++) {
//					AMLAttribute attr = attributeContainer.getAttribute(attributePath.get(i));
//					if (attr != null && i == attributePath.size() - 1)
//						checkRoleAttribute = attr;
//					attributeContainer = (AMLAttributeContainer) attr;
//				}
//
//				AMLMappingObject mappingObject = supportedRoleClass.getMappingObject();
//				if (mappingObject.getMappedAttribute(checkRoleAttribute) == null && checkRoleAttribute != null)
//					return new AMLValidationResultListImpl(attribute, Severity.AML_ERROR, "Deleting unallowed because of RoleClass");
//
//				if (mappingObject.getMappedAttribute(checkRoleAttribute) == attribute)
//					return new AMLValidationResultListImpl(attribute, Severity.AML_ERROR, "Deleting unallowed because of RoleClass");
//
//				if (mappingObject.isAttributeMapped(attribute))
//					return new AMLValidationResultListImpl(attribute, Severity.AML_ERROR, "eleting unallowed because attribute is mapped");
//			}
//		}

		if (container instanceof AMLInternalElement) {
			Iterable<AMLFacet> facets = ((AMLInternalElement) container).getFacets();
			for (AMLFacet facet : facets) {
				if (facet.getAttribute(attribute.getName()) == attribute)
					return new AMLValidationResultListImpl(attribute, Severity.AML_ERROR, "Deleting unallowed because of Facet");
			}
		}

		AMLValidator validator = attribute.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateAttributeDelete(attribute);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(attribute, Severity.OK, "");
	}

	static AMLAttributeImpl createAttribute(AMLInternalAttributeContainer container, AMLAttributeImpl attribute) throws AMLValidationException {
		AMLAttributeImpl attributeCopy = createAttribute(container, attribute.getName());
		// attribute.addReferrer(attributeCopy);
		// attributeCopy.setReferenceAttribute(attribute);

		copyAttributes(attributeCopy, attribute.getAttributes());

		return attributeCopy;
	}

	static void copyAttributes(AMLAttributeContainer container, Iterable<AMLAttribute> attributes) throws AMLValidationException {
		for (AMLAttribute attribute : attributes) {
			AMLAttribute attributeCopy = container.createAttribute(attribute.getName());
			attributeCopy.setDataType(attribute.getDataType());
			attributeCopy.setDefaultValue(attribute.getDefaultValue());
			attributeCopy.setDescription(attribute.getDescription());
			attributeCopy.setUnit(attribute.getUnit());
			attributeCopy.setValue(attribute.getValue());
			copyAttributes(attributeCopy, attribute.getAttributes());
		}
	}

	static AMLFrameAttribute createFrameAttribute(AMLInternalFrameAttributeContainer container) throws AMLValidationException {
		AMLValidationResultList resultList = validateCreateFrameAttribute(container);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		((AMLDocumentImpl) container.getDocument()).notifyElementValidated(resultList);

		AMLFrameAttributeImpl attribute = _createFrameAttribute(container);

		if (((AMLSessionImpl) container.getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateFrameAttributeChange change = new CreateFrameAttributeChange(attribute);
			((AMLSessionImpl) container.getSession()).getSavepointManager().addChange(change);
		}

		((AMLDocumentImpl) container.getDocument()).notifyElementCreated(attribute, container);

		return attribute;
	}

	static AMLFrameAttributeImpl _createFrameAttribute(AMLInternalFrameAttributeContainer container) {
		AMLFrameAttributeImpl frameAttribute = new AMLFrameAttributeImpl(container);
		container._setFrameAttribute(frameAttribute);
		return frameAttribute;
	}

	static AMLValidationResultList validateCreateFrameAttribute(AMLFrameAttributeContainer container) {
		return container.validateCreateFrameAttribute();
	}

	static AMLValidationResultList _validateCreateFrameAttribute(AMLFrameAttributeContainer container) {

		AMLValidator validator = container.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateFrameAttributeCreate(container);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	static AMLValidationResultList validateDeleteFrameAttribute(AMLFrameAttributeImpl attribute) {

		AMLValidator validator = attribute.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateFrameAttributeDelete(attribute);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(attribute, Severity.OK, "");
	}

	static void _removeFrameAttribute(AMLInternalFrameAttributeContainer container, AMLFrameAttributeImpl frameAttribute) {
		container._setFrameAttribute(null);

	}

	public static void _addAttribute(AMLInternalAttributeContainer parent,
			AMLAttributeImpl attribute, AMLAttribute beforeElement,
			AMLAttribute afterElement) {
		
		if (beforeElement == null && afterElement == null) {
			parent._getAttributes().put(attribute.getName(), attribute);
			return;
		}
		Map<String, AMLAttribute> internalElements = new LinkedHashMap<String, AMLAttribute>();
		for (String name : parent._getAttributes().keySet()) {
			if (beforeElement != null && beforeElement.getName().equals(name))
				internalElements.put(attribute.getName(), attribute);
			internalElements.put(name, parent._getAttributes().get(name));
			if (afterElement != null && afterElement.getName().equals(name))
				internalElements.put(attribute.getName(), attribute);
		}
		parent._getAttributes().clear();
		parent._getAttributes().putAll(internalElements);		
		
	}

	public static AMLDocumentElement _getAttributeBefore(
			AMLInternalAttributeContainer attributeContainer,
			AMLAttributeImpl attribute) {
		ListIterator<String> iterator = new ArrayList<String>(attributeContainer._getAttributes().keySet()).listIterator();
		while (iterator.hasNext()) {
			if (attribute.getName().equals(iterator.next())) {
				AMLAttributeImpl previous = (AMLAttributeImpl) attributeContainer._getAttributes().get(iterator.previous());
				if (previous.equals(attribute))
					return null;
				else
					return previous;
			}
		}
		return null;		
	}

	public static AMLDocumentElement _getAttributeAfter(
			AMLInternalAttributeContainer attributeContainer,
			AMLAttributeImpl attribute) {
		ListIterator<String> iterator = new ArrayList<String>(attributeContainer._getAttributes().keySet()).listIterator();
		while (iterator.hasNext()) {
			if (attribute.getName().equals(iterator.next())) {
				if (iterator.hasNext()) {
					AMLAttributeImpl after = (AMLAttributeImpl) attributeContainer._getAttributes().get(iterator.next());
					if (after.equals(attribute))
						return null;
					else
						return after;
				}
				return null;
			}
		}
		return null;		
	}

	static void _renameAttribute(
			AMLRoleRequirementsImpl amlRoleRequirementsImpl,
			AMLAttributeImpl newAttribute, String name,
			AMLSupportedRoleClass supportedRoleClass) {
		Map<String, AMLAttribute> attributes = amlRoleRequirementsImpl._getAttributes(supportedRoleClass);
		if (attributes == null)
			return;
		AMLAttribute att = attributes.get(newAttribute.getName());
		if (att == newAttribute) {
			attributes.remove(att.getName());
			attributes.put(name, att);
		}
	}

}
