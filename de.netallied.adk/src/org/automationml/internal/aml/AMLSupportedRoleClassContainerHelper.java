/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Set;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttributeContainer;
import org.automationml.aml.AMLMappingObject;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractCreateDocumentElementChange;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;

public class AMLSupportedRoleClassContainerHelper {

	private static final String AUTOMATION_ML_ROLE_FACET_PATH = "AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Facet";

	private static class CreateSupportedRoleClass extends AbstractCreateDocumentElementChange {

		private Identifier<AMLRoleClassImpl> roleClass;
		private Identifier<AMLMappingObjectImpl> mappingObject;

		public CreateSupportedRoleClass(AMLSupportedRoleClassImpl documentElement) {
			super(documentElement);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLRoleClassImpl roleClass = (AMLRoleClassImpl) ((AMLSupportedRoleClass) getDocumentElement()).getRoleClass();
			AMLMappingObjectImpl mappingObject = (AMLMappingObjectImpl) ((AMLSupportedRoleClass) getDocumentElement()).getMappingObject();
			this.roleClass = (Identifier<AMLRoleClassImpl>) identifierManager.getIdentifier(roleClass);
			this.mappingObject = (Identifier<AMLMappingObjectImpl>) identifierManager.getIdentifier(mappingObject);
		}

		@Override
		public void redo() throws Exception {
			AMLInternalSupportedRoleClassContainer container = (AMLInternalSupportedRoleClassContainer) parentIdentifier.getDocumentElement();
			AMLRoleClass roleClass = this.roleClass.getDocumentElement();
			AMLMappingObject mappingObject = null;
			if (this.mappingObject != null)
				mappingObject = this.mappingObject.getDocumentElement();
			AMLSupportedRoleClassImpl clazz = (AMLSupportedRoleClassImpl) AMLSupportedRoleClassContainerHelper._createSupportedRoleClass(container, roleClass,
					mappingObject);
			identifier.setDocumentElement(clazz);
		}

		@Override
		protected void _delete() {
			super._delete();
			if (roleClass != null)
				roleClass.release();
			roleClass = null;

			if (mappingObject != null)
				mappingObject.release();
			mappingObject = null;
		}
	}

	static AMLSupportedRoleClass createSupportedRoleClass(AMLInternalSupportedRoleClassContainer container, AMLRoleClass roleClass, AMLMappingObject mappings)
			throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		AMLValidationResultList resultList = validateCreateSupportedRoleClass(container, roleClass);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		document.notifyElementValidated(resultList);

		AMLSupportedRoleClassImpl supportedRoleClass = _createSupportedRoleClass(container, roleClass, mappings);

		if (((AMLSessionImpl) container.getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateSupportedRoleClass change = new CreateSupportedRoleClass(supportedRoleClass);
			((AMLSessionImpl) container.getSession()).getSavepointManager().addChange(change);
		}
		document.notifyElementCreated(supportedRoleClass, container);

		return supportedRoleClass;
	}

	static AMLValidationResultList validateCreateSupportedRoleClass(AMLInternalSupportedRoleClassContainer container, AMLRoleClass roleClass) {

		if (roleClass == null)
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "RoleClass not specified");

		if (roleClass == container.getDocument().getRoleClassByPath(AUTOMATION_ML_ROLE_FACET_PATH))
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Use createFacet() to create a Facet object.");

//		if (mappings == null)
//			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Mapping not specified");

//		Set<AMLAttribute> unreferredAttributes = new HashSet<AMLAttribute>();
//		if (!(container instanceof AMLAttributeContainer))
//			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "parent element can't have attributes");
//		AMLAttributeContainer attributeContainer = (AMLAttributeContainer) container;
//		Iterable<AMLAttribute> attributes = roleClass.getAttributes();
//		checkAttributeExistance(unreferredAttributes, attributeContainer, attributes, mappings);
//
//		if (unreferredAttributes.size() > 0)
//			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "SupportedRoleClass has not mapped attributes");

		AMLValidator validator = container.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateSupportedRoleClassCreate(container, roleClass);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	private static void checkAttributeExistance(Set<AMLAttribute> unreferredAttributes, AMLAttributeContainer attributeContainer,
			Iterable<AMLAttribute> attributes, AMLMappingObject mappings) {
		for (AMLAttribute attribute : attributes) {
			AMLAttribute containerAttribute = attributeContainer.getAttribute(attribute.getName());
			if (containerAttribute == null) {
				// find mapping
				if (mappings == null || mappings.getMappedAttribute(attribute) == null)
					unreferredAttributes.add(attribute);
			}
			checkAttributeExistance(unreferredAttributes, containerAttribute, attribute.getAttributes(), mappings);
		}
	}

	private static AMLSupportedRoleClassImpl _createSupportedRoleClass(AMLInternalSupportedRoleClassContainer container, AMLRoleClass roleClass,
			AMLMappingObject mappings) throws AMLValidationException {
		AMLSupportedRoleClassImpl supportedRoleClass = new AMLSupportedRoleClassImpl(container, roleClass, mappings);

		container._getSupportedRoleClasses().put(roleClass, supportedRoleClass);
		return supportedRoleClass;
	}

	static AMLValidationResultList validateDeleteSupportedRoleClass(AMLSupportedRoleClassImpl supportedRoleClass) {
		AMLValidator validator = supportedRoleClass.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateSupportedRoleClassDelete(supportedRoleClass);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(supportedRoleClass, Severity.OK, "");
	}

	static void removeSupportedRoleClass(AMLInternalSupportedRoleClassContainer supportedRoleClassContainer, AMLRoleClassImpl roleClass,
			AMLSupportedRoleClassImpl amlSupportedRoleClass) throws AMLValidationException {
		supportedRoleClassContainer._getSupportedRoleClasses().remove(amlSupportedRoleClass.getRoleClass());
		((AMLDocumentImpl)supportedRoleClassContainer.getDocument()).notifyElementModified(supportedRoleClassContainer);
		roleClass.removeReferrer(amlSupportedRoleClass);
	}

	static AMLSupportedRoleClass createSupportedRoleClass(AMLInternalSupportedRoleClassContainer container, AMLRoleClass roleClass)
			throws AMLValidationException {
		AMLMappingObject mappings = container.createMappingObject(roleClass);
		return createSupportedRoleClass(container, roleClass, mappings);
	}

}
