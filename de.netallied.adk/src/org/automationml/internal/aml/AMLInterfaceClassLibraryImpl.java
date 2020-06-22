/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;

import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInterfaceClassLibrary;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.Change;

public class AMLInterfaceClassLibraryImpl extends AbstractAMLClassLibraryImpl implements AMLInterfaceClassLibrary {

	private static class DeleteInterfaceClassLibraryChange extends AbstractDeleteDocumentElementChange<AbstractAMLDocumentElement> {
		private String libraryName;

		public DeleteInterfaceClassLibraryChange(AbstractAMLClassLibraryImpl library) {
			super(library);
			libraryName = library.getName();
		}

		@Override
		public void undo() throws Exception {
			AMLDocumentImpl document = (AMLDocumentImpl) parentIdentifier.getDocumentElement();
			AbstractAMLDocumentElement documentElement = document._createInterfaceClassLibrary(libraryName);
			identifier.setDocumentElement(documentElement);
		}
	}

	AMLInterfaceClassLibraryImpl(AMLDocumentImpl amlDocument, String name) {
		super(amlDocument, name);
	}

	@Override
	protected AbstractAMLClassImpl<?> _newClass() throws AMLValidationException {
		return new AMLInterfaceClassImpl(this);
	}

	@Override
	public AMLInterfaceClass getInterfaceClass(String name) {
		return (AMLInterfaceClass) super.getClass(name);
	}

	@Override
	public Iterable<AMLInterfaceClass> getInterfaceClasses() {
		return (Iterable<AMLInterfaceClass>) super.getClasses();
	}
	
	@Override
	public int getInterfaceClassesCount() {
		return super.getClassesCount();
	}

	@Override
	public AMLInterfaceClass createInterfaceClass(String name) throws AMLValidationException {
		return createInterfaceClass(name, null);
	}

	@Override
	public AMLValidationResultList validateCreateInterfaceClass(String name) {
		return _validateCreateInterfaceClass(name);
	}

	private AMLValidationResultList _validateCreateInterfaceClass(String name) {
		return validateRenameClass(name);
	}

	protected boolean isClassLibraryNameDefined(String newName) throws AMLDocumentScopeInvalidException {
		return getDocumentManager().isInterfaceClassLibraryNameDefined(this, newName);
	}

	protected AMLValidationResultListImpl validateName(AMLValidator validator, String newName) {
		AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInterfaceClassLibrarySetName(this, newName);
		return validationResult;
	}

	protected void _setName(String newName) throws AMLValidationException {
		getDocument()._renameInterfaceClassLibrary(this, newName);
		this.name = newName;
	}

	protected Change createDeleteChange() {
		Change change = new DeleteInterfaceClassLibraryChange(this);
		return change;
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		getDocument()._removeInterfaceClassLibrary(name);
		getDocumentManager().removeInterfaceClassLibraryName(getDocument(), name);
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		return validator.validateInterfaceClassLibraryDelete(this);
	}

	@Override
	protected AMLValidationResultList _validateCreateClass(AMLValidator validator, String name) {
		return validator.validateInterfaceClassCreate(this, name);
	}
	
	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
	
		if (!(newParentElement instanceof AMLDocument))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "new parent is not a document");
		
		AMLDocumentImpl newParent = (AMLDocumentImpl)newParentElement;
		if (!oldParentElement.equals(newParentElement) && newParent.getInstanceHierarchy(getName()) != null)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "a interface class library with name " + getName() + " already exists");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateInterfaceClassLibraryReparent((AMLDocument)oldParentElement, (AMLDocument)newParentElement, this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}
		
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}
	
	@Override
	protected void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		AMLDocumentImpl oldParent = (AMLDocumentImpl)oldParentElement;
		AMLDocumentImpl newParent = (AMLDocumentImpl)newParentElement;
		oldParent._removeInterfaceClassLibrary(getName());
		_setDocument(newParent);
		newParent._addInterfaceClassLibrary(this, (AMLInterfaceClassLibraryImpl)beforeElement, (AMLInterfaceClassLibraryImpl)afterElement);		
	}
	
	@Override
	protected AMLDocumentElement _getBefore() {
		AMLDocumentImpl document = (AMLDocumentImpl) getParent();
		return document._getInterfaceClassLibraryBefore(this);
	}
	
	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInterfaceClassLibraryDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
		super.doValidateDeepDelete(validator, baseElement, validationResultList);
	}

	@Override
	public AMLInterfaceClass createInterfaceClass(String name,
			AMLInterfaceClass interfaceClass) throws AMLValidationException {
		AMLValidationResultList validationResult = _validateCreateInterfaceClass(name);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		AMLInterfaceClassImpl interfaceClass2 =  (AMLInterfaceClassImpl) super.createClass(name);
		if (interfaceClass != null)
			interfaceClass2.deepCopy(interfaceClass, interfaceClass2, interfaceClass, new HashMap<AMLDocumentElement, AMLDocumentElement>());
		
		return interfaceClass2;
	}

	@Override
	protected AMLDocumentElement _getAfter() {
		AMLDocumentImpl document = (AMLDocumentImpl) getParent();
		return document._getInterfaceClassLibraryAfter(this);
	}
}
