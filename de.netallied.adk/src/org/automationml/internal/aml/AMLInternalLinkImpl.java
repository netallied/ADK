/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Map;
import java.util.Set;

import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalLink;
import org.automationml.aml.AMLInternalLinkContainer;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.AbstractDocumentElementChange;
import org.automationml.internal.Change;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;

public class AMLInternalLinkImpl extends AMLElementImpl implements AMLInternalLink {

	private static class ModifyInternalLinkNameChange extends AbstractDocumentElementChange<AMLInternalLinkImpl> {
		private String oldName;
		private String newName;

		public ModifyInternalLinkNameChange(AMLInternalLinkImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			AMLInternalLinkImpl internalLink = (AMLInternalLinkImpl) getDocumentElement();
			this.oldName = internalLink.getName();
		}

		public void setNewName(String newName) {
			this.newName = newName;
		}

		@Override
		public void undo() throws Exception {
			AMLInternalLinkImpl internalLink = (AMLInternalLinkImpl) getDocumentElement();
			if (newName == null)
				newName = internalLink.getName();
			internalLink._setName(oldName);
		}

		@Override
		public void redo() throws Exception {
			AMLInternalLinkImpl internalLink = (AMLInternalLinkImpl) getDocumentElement();
			internalLink._setName(newName);
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyInternalLinkNameChange))
				return false;
			ModifyInternalLinkNameChange change = (ModifyInternalLinkNameChange) _change;
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

	protected static class DeleteInternalLinkChange extends AbstractDeleteDocumentElementChange<AMLInternalLinkImpl> {
		private Identifier<AMLExternalInterfaceImpl> refPartnerSideA;
		private Identifier<AMLExternalInterfaceImpl> refPartnerSideB;
		private String name;

		public DeleteInternalLinkChange(AMLInternalLinkImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			this.name = getDocumentElement().getName();
			AMLExternalInterfaceImpl refPartnerSideA = (AMLExternalInterfaceImpl) ((AMLInternalLinkImpl) getDocumentElement()).getRefPartnerSideA();
			this.refPartnerSideA = (Identifier<AMLExternalInterfaceImpl>) identifierManager.getIdentifier(refPartnerSideA);
			AMLExternalInterfaceImpl refPartnerSideB = (AMLExternalInterfaceImpl) ((AMLInternalLinkImpl) getDocumentElement()).getRefPartnerSideB();
			this.refPartnerSideB = (Identifier<AMLExternalInterfaceImpl>) identifierManager.getIdentifier(refPartnerSideB);
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement documentElement = (AbstractAMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLInternalInternalLinkContainer container = (AMLInternalInternalLinkContainer) documentElement;
			AMLExternalInterfaceImpl refPartnerSideA = this.refPartnerSideA.getDocumentElement();
			AMLExternalInterfaceImpl refPartnerSideB = this.refPartnerSideB.getDocumentElement();
			AMLInternalLinkImpl internalLink = container._createInternalLink(name, refPartnerSideA, refPartnerSideB);
			identifier.setDocumentElement(internalLink);
		}

		@Override
		protected void _delete() {
			super._delete();
			if (refPartnerSideA != null)
				refPartnerSideA.release();
			refPartnerSideA = null;
			if (refPartnerSideB != null)
				refPartnerSideB.release();
			refPartnerSideB = null;
		}
	}

	AMLInternalLinkContainer internalLinkContainer;
	AMLExternalInterface refPartnerSideA;
	AMLExternalInterface refPartnerSideB;
	String name;

	public AMLInternalLinkImpl(AMLInternalLinkContainer internalLinkContainer, String linkName, AMLExternalInterface refPartnerSideA, AMLExternalInterface refPartnerSideB) {
		this.internalLinkContainer = internalLinkContainer;
		this.refPartnerSideA = refPartnerSideA;
		this.refPartnerSideB = refPartnerSideB;
		this.name = linkName;
	}

	@Override
	public AMLDocumentElement getParent() {
		return internalLinkContainer;
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
			DeleteInternalLinkChange change = new DeleteInternalLinkChange(this);
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
	}

	@Override
	public AMLDocumentImpl getDocument() {
		return (AMLDocumentImpl) internalLinkContainer.getDocument();
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		((AMLInternalInternalLinkContainer) internalLinkContainer).removeInternalLink(this);
		((AMLExternalInterfaceImpl) refPartnerSideA)._removeInternalLink(this);
		((AMLExternalInterfaceImpl) refPartnerSideB)._removeInternalLink(this);
	}

	@Override
	public AMLExternalInterface getRefPartnerSideA() {
		assertNotDeleted();
		return refPartnerSideA;
	}

	@Override
	public AMLExternalInterface getRefPartnerSideB() {
		assertNotDeleted();
		return refPartnerSideB;
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

		AMLValidationResultList resultList = validateNameChange(newName);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyInternalLinkNameChange change = new ModifyInternalLinkNameChange(this);
			change.setNewName(newName);
			getSavepointManager().addChange(change);
		}
		_setName(newName);
		getDocument().notifyElementModified(this);
	}

	private void _setName(String newName) {
		this.name = newName;
	}

	@Override
	public AMLValidationResultList validateNameChange(String newName) {
		assertNotDeleted();

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInternalLinkSetName(this, newName);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInternalLinkDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkForm)
			throws AMLValidationException {
		
	}

	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		return null;
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
	}	
}
