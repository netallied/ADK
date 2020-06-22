/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Map;
import java.util.Set;

import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLMappingObject;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;

public class AMLSupportedRoleClassImpl extends AMLElementImpl implements AMLSupportedRoleClass {

	protected static class DeleteSupportedRoleClassChange extends AbstractDeleteDocumentElementChange<AMLSupportedRoleClassImpl> {

		private Identifier<AMLRoleClassImpl> roleClassIdentifier;
		
		public DeleteSupportedRoleClassChange(AMLSupportedRoleClassImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLRoleClassImpl ref = (AMLRoleClassImpl) getDocumentElement().getRoleClass();
			if (ref != null)
				roleClassIdentifier = (Identifier<AMLRoleClassImpl>) identifierManager.getIdentifier(ref);
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement documentElement = (AbstractAMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLInternalSupportedRoleClassContainer parent = (AMLInternalSupportedRoleClassContainer) documentElement;

			AMLRoleClass roleClass = roleClassIdentifier.getDocumentElement();
//			AMLMappingObject mappings = null;
//			AMLSupportedRoleClassImpl newCreatedSupportedRoleClass = (AMLSupportedRoleClassImpl) AMLSupportedRoleClassContainerHelper.createSupportedRoleClass(
//					parent, roleClass, mappings);
			AMLSupportedRoleClassImpl newCreatedSupportedRoleClass = (AMLSupportedRoleClassImpl) AMLSupportedRoleClassContainerHelper.createSupportedRoleClass(parent, roleClass);
			identifier.setDocumentElement(newCreatedSupportedRoleClass);
		}

		@Override
		protected void _delete() {
			if (roleClassIdentifier != null)
				roleClassIdentifier.release();
			super._delete();
		}
	}

	AMLInternalSupportedRoleClassContainer supportedRoleClassContainer;
	AMLRoleClass roleClass;
	AMLMappingObjectImpl mappingObject;

	public AMLSupportedRoleClassImpl(AMLInternalSupportedRoleClassContainer container, AMLRoleClass roleClass, AMLMappingObject mappings)
			throws AMLValidationException {
		this.supportedRoleClassContainer = container;
		this.roleClass = roleClass;
		this.mappingObject = (AMLMappingObjectImpl) mappings;
		if (this.mappingObject != null)
			this.mappingObject.setSupportedRoleClass(this);

		((AMLRoleClassImpl) this.roleClass).addReferrer(this);
	}

	@Override
	public AMLDocumentElement getParent() {
		return supportedRoleClassContainer;
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
			DeleteSupportedRoleClassChange change = new DeleteSupportedRoleClassChange(this);
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
	}

	@Override
	public AMLRoleClass getRoleClass() {
		assertNotDeleted();
		return roleClass;
	}

	@Override
	public AMLDocumentImpl getDocument() {
		assertNotDeleted();
		return (AMLDocumentImpl) supportedRoleClassContainer.getDocument();
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		AMLSupportedRoleClassContainerHelper.removeSupportedRoleClass(supportedRoleClassContainer, (AMLRoleClassImpl) roleClass, this);
	}

	@Override
	public AMLMappingObject getMappingObject() {
		assertNotDeleted();
		return mappingObject;
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		
		AMLValidationResultList validationResult = AMLSupportedRoleClassContainerHelper.validateDeleteSupportedRoleClass(this);
		if (validationResult.isAnyOperationNotPermitted())
			return validationResult;
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateSupportedRoleClassDelete(this);
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
		return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "a Suppported Role Class can't be reparented");
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
	public void validateIfIsInDocumentScope(AMLDocumentElement elementToReparent, AMLDocumentImpl document,
			AMLValidationResultListImpl result, Set<AMLDocumentElement> checkedElements) {
		if (checkedElements.contains(this))
			return;
		checkedElements.add(this);
		
		if (!isDescendantOf(elementToReparent) &&!getDocumentManager().getDocumentScope(document).isInDocumentScope(this))
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "supoorted role class is not part of document scope"));
		
		((AMLRoleClassImpl)roleClass).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);		
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkForm)
			throws AMLValidationException {
		if (getRoleClass() != null) {
			if (!((AbstractAMLDocumentElement)getRoleClass()).isDescendantOf(unlinkForm))
				delete();
		}
	}

	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateSupportedRoleClassDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		mapping.put(original, this);
		
		AMLMappingObject mappingObject = ((AMLSupportedRoleClass)original).getMappingObject();
		((AbstractAMLDocumentElement)getMappingObject()).deepCopy(rootElement, rootCopyElement, mappingObject, mapping);
	}

	@Override
	protected void _doDeepDelete(AMLDocumentElement baseElement)
			throws AMLValidationException {
		((AbstractAMLDocumentElement)getMappingObject())._doDeepDelete(baseElement);
		super._doDeepDelete(baseElement);
	}
}
