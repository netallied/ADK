/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Map;
import java.util.UUID;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLCOLLADAInterface;
import org.automationml.aml.AMLCOLLADAInterface.RefType;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractCreateDocumentElementChange;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;
import org.automationml.internal.ReadOnlyIterable;

public class AMLExternalInterfaceContainerHelper {

	public static final String AUTOMATION_ML_COLLADA_INTERFACE = "AutomationMLInterfaceClassLib/AutomationMLBaseInterface/ExternalDataConnector/COLLADAInterface";

	private static class CreateExternalInterfaceChange extends AbstractCreateDocumentElementChange {
		private UUID id;
		private Identifier<AMLInterfaceClassImpl> interfaceClass;

		public CreateExternalInterfaceChange(AMLExternalInterfaceImpl documentElement) {
			super(documentElement);
			this.id = documentElement.getId();
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLInterfaceClassImpl interfaceClass = (AMLInterfaceClassImpl) ((AMLExternalInterface) getDocumentElement())
					.getInterfaceClass();
			this.interfaceClass = (Identifier<AMLInterfaceClassImpl>) identifierManager.getIdentifier(interfaceClass);
		}

		@Override
		public void redo() throws Exception {
			AMLInternalExternalInterfaceContainer container = (AMLInternalExternalInterfaceContainer) parentIdentifier
					.getDocumentElement();
			AMLInterfaceClass interfaceClass = this.interfaceClass.getDocumentElement();
			AMLExternalInterfaceImpl clazz = (AMLExternalInterfaceImpl) AMLExternalInterfaceContainerHelper
					._createExternalInterface(container, interfaceClass, id);
			identifier.setDocumentElement(clazz);
		}

		@Override
		protected void _delete() {
			super._delete();
			if (interfaceClass != null)
				interfaceClass.release();
			interfaceClass = null;
		}
	}

	private static class CreateCOLLADAInterfaceChange extends AbstractCreateDocumentElementChange {
		private UUID id;
		private AMLCOLLADAInterface.RefType refType;

		public CreateCOLLADAInterfaceChange(AMLCOLLADAInterfaceImpl documentElement) {
			super(documentElement);
			this.id = documentElement.getId();
			this.refType = documentElement.getRefType();
		}

		@Override
		public void redo() throws Exception {
			AMLInternalCOLLADAInterfaceContainer container = (AMLInternalCOLLADAInterfaceContainer) parentIdentifier
					.getDocumentElement();
			AMLCOLLADAInterfaceImpl clazz = (AMLCOLLADAInterfaceImpl) AMLExternalInterfaceContainerHelper
					._createCOLLADAInterface(container, refType, id);
			identifier.setDocumentElement(clazz);
		}

	}

	static AMLExternalInterface createExternalInterface(AMLInternalExternalInterfaceContainer container,
			AMLInterfaceClass interfaceClass, boolean createAttributes) throws AMLValidationException {

		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		UUID newId = UUID.randomUUID();
		while (document.getDocumentManager().isUniqueIdDefined(container, newId))
			newId = UUID.randomUUID();

		return createExternalInterface(container, interfaceClass, newId, createAttributes);
	}

	static AMLExternalInterface createExternalInterface(AMLInternalExternalInterfaceContainer container,
			AMLInterfaceClass interfaceClass, UUID newId, boolean createAttributes) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		AMLValidationResultList resultList = validateCreateExternalInterface(container, interfaceClass, newId);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		document.notifyElementValidated(resultList);

		AMLExternalInterfaceImpl externalInterface = _createExternalInterface(container, interfaceClass, newId);

		if (((AMLSessionImpl) container.getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateExternalInterfaceChange change = new CreateExternalInterfaceChange(externalInterface);
			((AMLSessionImpl) container.getSession()).getSavepointManager().addChange(change);
		}
		document.notifyElementCreated(externalInterface, container);

		// add attributes
		if (createAttributes) {
			Iterable<AMLAttribute> attributes = interfaceClass.getAttributes();
			AMLAttributeContainerHelper.copyAttributes(externalInterface, attributes);
		}
		return externalInterface;
	}

	static AMLExternalInterfaceImpl _createExternalInterface(AMLInternalExternalInterfaceContainer container,
			AMLInterfaceClass interfaceClass, UUID id) throws AMLValidationException {
		AMLExternalInterfaceImpl externalInterfaceImpl = new AMLExternalInterfaceImpl(container, interfaceClass, id);

		((AMLInterfaceClassImpl) interfaceClass).addReferrer(externalInterfaceImpl);

		container._getExternalInterfaces().put(id, externalInterfaceImpl);
		return externalInterfaceImpl;
	}

	static Iterable<AMLExternalInterface> getExternalInterfaces(Map<UUID, AMLExternalInterface> externalInterfaces) {
		return new ReadOnlyIterable<AMLExternalInterface>(externalInterfaces.values());
	}

	static AMLValidationResultList validateCreateExternalInterface(AMLInternalExternalInterfaceContainer container,
			AMLInterfaceClass interfaceClass, UUID id) throws AMLValidationException {

		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();
		if (document.getDocumentManager().isUniqueIdDefined(container, id))
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Id already exists");

		return validateCreateExternalInterface(container, interfaceClass);
	}

	static AMLValidationResultList validateCreateExternalInterface(AMLInternalExternalInterfaceContainer container,
			AMLInterfaceClass interfaceClass) {

		AMLValidator validator = container.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator
					.validateExternalInterfaceCreate(container, interfaceClass);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	static AMLExternalInterface getExternalInterface(AMLInternalExternalInterfaceContainer container, UUID id) {
		AMLExternalInterface externalInterface = container._getExternalInterfaces().get(id);
		return externalInterface;
	}

	static void _removeExternalInterface(AMLInternalExternalInterfaceContainer container,
			AMLExternalInterfaceImpl externalInterface) throws AMLValidationException {
		container._getExternalInterfaces().remove(externalInterface.getId());
		AMLInterfaceClass interfaceClass = externalInterface.getInterfaceClass();
		((AMLInterfaceClassImpl) interfaceClass).removeReferrer(externalInterface);
	}

	static AMLCOLLADAInterface createCOLLADAInterface(AMLInternalCOLLADAInterfaceContainer container, RefType refType)
			throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		UUID newId = UUID.randomUUID();
		while (document.getDocumentManager().isUniqueIdDefined(container, newId))
			newId = UUID.randomUUID();

		return createCOLLADAInterface(container, refType, newId);
	}

	static AMLCOLLADAInterface createCOLLADAInterface(AMLInternalCOLLADAInterfaceContainer container, RefType refType,
			UUID newId) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		AMLValidationResultList resultList = validateCreateCOLLADAInterface(container, refType, newId);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		document.notifyElementValidated(resultList);

		AMLCOLLADAInterfaceImpl colladaInterface = _createCOLLADAInterface(container, refType, newId);

		if (((AMLSessionImpl) container.getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateCOLLADAInterfaceChange change = new CreateCOLLADAInterfaceChange(colladaInterface);
			((AMLSessionImpl) container.getSession()).getSavepointManager().addChange(change);
		}
		document.notifyElementCreated(colladaInterface, container);

		return colladaInterface;
	}

	static AMLCOLLADAInterfaceImpl _createCOLLADAInterface(AMLInternalCOLLADAInterfaceContainer container,
			RefType refType, UUID newId) {
		AMLCOLLADAInterfaceImpl colladaInterface = new AMLCOLLADAInterfaceImpl(container, refType, newId);
		container._setCOLLADAInterface(colladaInterface);
		return colladaInterface;
	}

	static AMLValidationResultList validateCreateCOLLADAInterface(AMLInternalCOLLADAInterfaceContainer container,
			RefType refType, UUID id) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();
		if (document.getDocumentManager().isUniqueIdDefined(container, id))
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Id already exists");

		return validateCreateCOLLADAInterface(container, refType);
	}

	static AMLValidationResultList validateCreateCOLLADAInterface(AMLInternalCOLLADAInterfaceContainer container,
			RefType refType) {

		AMLInterfaceClass interfaceClass = null;
		try {
			interfaceClass = container.getDocument().getInterfaceClassByPath(AUTOMATION_ML_COLLADA_INTERFACE);
		} catch (AMLValidationException ex) {

		}
		if (interfaceClass == null)
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR,
					"RoleClass \"COLLADAInterface\" is not available");

		AMLValidator validator = container.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator
					.validateCOLLADAInterfaceCreate(container, refType);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	static AMLValidationResultList validateDeleteCOLLADAInterface(AMLCOLLADAInterfaceImpl colladaInterface) {

		AMLValidator validator = colladaInterface.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator
					.validateCOLLADAInterfaceDelete(colladaInterface);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(colladaInterface, Severity.OK, "");
	}

	static AMLValidationResultList validateDeleteExternalInterface(AMLExternalInterface externalInterface) {
		if (externalInterface.hasInternalLinks())
			return new AMLValidationResultListImpl(externalInterface, Severity.AML_ERROR,
					"ExternalInterface has linked ExternalInterface elements.");

		AMLValidator validator = externalInterface.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator
					.validateExternalInterfaceDelete(externalInterface);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(externalInterface, Severity.OK, "");
	}

	static void _removeCOLLADAInterface(AMLInternalCOLLADAInterfaceContainer container,
			AMLCOLLADAInterfaceImpl colladaInterface) throws AMLValidationException {
		container._setCOLLADAInterface(null);
		colladaInterface.getDocumentManager().removeUniqueId((AMLDocumentImpl) container.getDocument(),
				colladaInterface.getId());
	}

}
