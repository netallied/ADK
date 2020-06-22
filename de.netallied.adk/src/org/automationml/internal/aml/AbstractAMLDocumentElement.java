/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.SavepointManager;

public abstract class AbstractAMLDocumentElement extends AbstractAMLObject implements AMLDocumentElement {
	

	private boolean deleted;

	public SavepointManager getSavepointManager() {
		return ((AMLSessionImpl) getSession()).getSavepointManager();
	}

	public AMLDocumentManager getDocumentManager() {
		return ((AMLSessionImpl) getSession()).getDocumentManager();
	}

	protected void assertSameSession(AMLDocumentElement aMLDocumentElement) throws AMLValidationException {
		if (aMLDocumentElement != null && aMLDocumentElement.getSession() != getSession())
			throw new AMLDocumentScopeInvalidException(this, "Referenced Element does not belong to this Session");
	}

	public final void _delete() throws AMLValidationException {
		_doDelete();
		deleted = true;
	}

	protected abstract void _doDelete() throws AMLValidationException;
	
	protected abstract AMLValidationResultList doValidateDelete(AMLValidator validator);
	
	@Override
	public boolean isDeleted() {
		return deleted;
	}

	protected void assertNotDeleted() {
		if (isDeleted())
			throw new RuntimeException("AML Element is already deleted");
	}
	
	@Override
	public AMLValidationResultList validateReparent(
			AMLDocumentElement oldParentElement, AMLDocumentElement newParentElement, 
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		// every element must check if its new parent is not a descendant.
		AMLDocumentElement _oldParent = oldParentElement;
		while (_oldParent != null && !_oldParent.equals(this)) {
			_oldParent = _oldParent.getParent();
		}
		if (_oldParent != null && _oldParent.equals(this))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Creating cycles is not allowed");
		
		// check if parent is not reparent as child
		AMLDocumentElement _parentParent = newParentElement.getParent();
		while (_parentParent != null && !_parentParent.equals(this)) {
			_parentParent = _parentParent.getParent();
		}
		if (_parentParent != null && _parentParent.equals(this))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Object can not be child of one of its children!");
		
		
		AMLValidationResultListImpl result = new AMLValidationResultListImpl();
		Set<AMLDocumentElement> checkedElements = new HashSet<AMLDocumentElement>();
		validateIfIsInDocumentScope(this, (AMLDocumentImpl) newParentElement.getDocument(), result, checkedElements);
		if (result.isAnyOperationNotPermitted())
			return result;
		
		// after this do specific checks
		AMLValidator validator = getSession().getValidator();
		return doValidateReparent(validator, oldParentElement, newParentElement, beforeElement, afterElement);
	}
	
	protected abstract AMLValidationResultList doValidateReparent(AMLValidator validator, 
			AMLDocumentElement oldParentElement, AMLDocumentElement newParentElement, 
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement);
	
	protected abstract void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement);
	
	protected abstract AMLDocumentElement _getBefore();
	protected abstract AMLDocumentElement _getAfter();
	
	@Override
	public void reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement)
			throws AMLValidationException {
		
		if (isDeleted())
			return;

		AMLValidationResultList validationResult = validateReparent(oldParentElement, newParentElement, beforeElement, afterElement);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		((AMLDocumentImpl)getDocument()).notifyElementValidated(validationResult);

		if (((AMLSessionImpl)getSession()).getSavepointManager().hasCurrentSavepoint()) {
			DocumentElementReparentChange change = new DocumentElementReparentChange(this);
			change.setNewParent(newParentElement, beforeElement, afterElement);
			getSavepointManager().addChange(change);
		}

		_reparent(oldParentElement, newParentElement, beforeElement, afterElement);

		
		((AMLDocumentImpl)getDocument()).notifyElementReparented(this, oldParentElement, newParentElement);		
	}
	
	public abstract void validateIfIsInDocumentScope(AMLDocumentElement elementToReparent, AMLDocumentImpl document, AMLValidationResultListImpl result, Set<AMLDocumentElement> checkedElements);
	
	boolean isDescendantOf(AMLDocumentElement documentElement) {
		if (this.equals(documentElement))
			return true;
		if (getParent() == null)
			return false;
		return ((AbstractAMLDocumentElement)getParent()).isDescendantOf(documentElement);
	}

	@Override
	public AMLValidationResultList validateUnlink() {
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}
	
	public boolean isReferencedByDocumentElements(AMLDocument document, AMLDocument documentToIgnore, AMLValidationResultListImpl result) {
		return false;
	}

	@Override
	public void unlink() throws AMLValidationException {
		
		if (isDeleted())
			return;

		AMLValidationResultList validationResult = validateUnlink();
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		((AMLDocumentImpl)getDocument()).notifyElementValidated(validationResult);

		_doUnlink(this);
		
		((AMLDocumentImpl)getDocument()).notifyElementModified(this);		
	}
	
	protected abstract void _doUnlink(AMLDocumentElement unlinkFrom) throws AMLValidationException;

	@Override
	public AMLValidationResultList validateDeepDelete() {
		// after this do specific checks
		AMLValidator validator = getSession().getValidator();
		AMLValidationResultListImpl result = new AMLValidationResultListImpl();
		doValidateDeepDelete(validator, this, result);
		return result;
	}
	
	protected abstract void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList);
	
	@Override
	public void deepDelete() throws AMLValidationException {
		
		if (isDeleted())
			return;

		AMLValidationResultList validationResult = validateDeepDelete();
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		_doDeepDelete(this);
	}
	
	protected void _doDeepDelete(AMLDocumentElement baseElement) throws AMLValidationException {
		delete();
	}
	
	public abstract void deepCopy(AMLDocumentElement rootElement, AMLDocumentElement rootCopyElement, 
			AMLDocumentElement original, Map<AMLDocumentElement, AMLDocumentElement> mapping) throws AMLValidationException;
	
}
