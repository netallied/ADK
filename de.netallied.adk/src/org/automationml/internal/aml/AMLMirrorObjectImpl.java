/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalElementContainer;
import org.automationml.aml.AMLMirrorContainer;
import org.automationml.aml.AMLMirrorObject;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;

public class AMLMirrorObjectImpl extends AMLElementImpl implements AMLMirrorObject {

	protected static class DeleteMirrorChange extends AbstractDeleteDocumentElementChange<AMLMirrorObjectImpl> {
		private UUID id;
		private Identifier<AMLInternalElementImpl> internalElement;

		public DeleteMirrorChange(AMLMirrorObjectImpl element) {
			super(element);
			id = element.getId();
		}

		@Override
		public void undo() throws Exception {
			AMLDocumentElement element = (AMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLInternalMirrorContainer container = (AMLInternalMirrorContainer) element;
			AMLInternalElement internalElement = this.internalElement.getDocumentElement();
			AMLMirrorObjectImpl clazz = (AMLMirrorObjectImpl) AMLMirrorContainerHelper._createMirror(container, id, internalElement);
			identifier.setDocumentElement(clazz);
		}

		@Override
		protected void _delete() {
			super._delete();
			if (internalElement != null)
				internalElement.release();
			internalElement = null;
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLInternalElementImpl roleClass = (AMLInternalElementImpl) ((AMLMirrorObject) getDocumentElement()).getInternalElement();
			this.internalElement = (Identifier<AMLInternalElementImpl>) identifierManager.getIdentifier(roleClass);
		}
	}

	private UUID id;
	private AMLInternalElement internalElement;
	private AMLInternalMirrorContainer mirrorContainer;

	public AMLMirrorObjectImpl(AMLMirrorContainer mirrorContainer, UUID id, AMLInternalElement internalElement) {
		this.mirrorContainer = (AMLInternalMirrorContainer) mirrorContainer;
		this.id = id;
		this.internalElement = internalElement;
	}

	@Override
	public AMLDocumentElement getParent() {
		assertNotDeleted();
		return mirrorContainer;
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
			DeleteMirrorChange change = new DeleteMirrorChange(this);
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
		return (AMLDocumentImpl) mirrorContainer.getDocument();
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		AMLMirrorContainerHelper._removeMirror(mirrorContainer, this);
		((AMLInternalElementImpl)internalElement).removeMirrorObject(this);
		getDocumentManager().removeUniqueId(getDocument(), id);
	}

	@Override
	public AMLInternalElement getInternalElement() {
		assertNotDeleted();
		return internalElement;
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateMirrorObjectDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		
		if (!(newParentElement instanceof AMLMirrorContainer))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "new parent is not a container for MirrorObjects");
		
		if (beforeElement != null && !(beforeElement instanceof AMLMirrorObject))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "mirror objects can only placed before MirrorObjects");
		
		if (afterElement != null && !(afterElement instanceof AMLMirrorObject))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "mirror objects can only placed after MirrorObjects");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateMirrorReparent((AMLMirrorContainer)oldParentElement, (AMLMirrorContainer)newParentElement, this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}
		
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		AMLInternalMirrorContainer oldParent = (AMLInternalMirrorContainer)oldParentElement;
		AMLInternalMirrorContainer newParent = (AMLInternalMirrorContainer)newParentElement;
		AMLMirrorContainerHelper._removeMirror(oldParent, this);
		this.mirrorContainer = newParent;
		AMLMirrorContainerHelper._addMirror(newParent, this, (AMLMirrorObject)beforeElement, (AMLMirrorObject)afterElement);
	}

	@Override
	protected AMLDocumentElement _getBefore() {
		AMLInternalMirrorContainer internalMirrorContainer = (AMLInternalMirrorContainer) getParent();
		return AMLMirrorContainerHelper._getMirrorBefore(internalMirrorContainer, this);
	}

	@Override
	protected AMLDocumentElement _getAfter() {
		AMLInternalMirrorContainer internalMirrorContainer = (AMLInternalMirrorContainer) getParent();
		return AMLMirrorContainerHelper._getMirrorAfter(internalMirrorContainer, this);
	}

	@Override
	public void validateIfIsInDocumentScope(
			AMLDocumentElement elementToReparent, AMLDocumentImpl document,
			AMLValidationResultListImpl result,
			Set<AMLDocumentElement> checkedElements) {
		if (checkedElements.contains(this))
			return;
		checkedElements.add(this);
		
		if (!isDescendantOf(elementToReparent) &&!getDocumentManager().getDocumentScope(document).isInDocumentScope(this))
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "mirror object is not part of document scope"));
		
		AMLDocumentImpl internalElementDocumentScope = (AMLDocumentImpl)internalElement.getDocument();
		if (!getDocumentManager().getDocumentScope(document).isInDocumentScope(internalElementDocumentScope)) 
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "base class is not part of referrer's document scope"));
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkFrom)
			throws AMLValidationException {
	}

	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateMirrorObjectDelete(this);
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
	}

	@Override
	public AMLValidationResultList validateCreateAttribute(String name) {
		return internalElement.validateCreateAttribute(name);
	}

	@Override
	public Iterable<AMLAttribute> getAttributes() {
		return internalElement.getAttributes();
	}

	@Override
	public int getAttributesCount() {
		return internalElement.getAttributesCount();
	}

	@Override
	public boolean hasAttributes() {
		return internalElement.hasAttributes();
	}

	@Override
	public AMLAttribute getAttribute(String name) {
		return internalElement.getAttribute(name);
	}

	@Override
	public AMLAttribute createAttribute(String name)
			throws AMLValidationException {
		return internalElement.createAttribute(name);
	}

	@Override
	public AMLAttribute createAttribute(String name, AMLAttribute attribute)
			throws AMLValidationException {
		return internalElement.createAttribute(name, attribute);
	}

}
