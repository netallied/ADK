/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;

import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLSystemUnitClass;
import org.automationml.aml.AMLSystemUnitClassLibrary;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.Change;

public class AMLSystemUnitClassLibraryImpl extends AbstractAMLClassLibraryImpl implements AMLSystemUnitClassLibrary {

	private static class DeleteSystemUnitClassLibraryChange extends AbstractDeleteDocumentElementChange<AbstractAMLDocumentElement> {
		private String libraryName;

		public DeleteSystemUnitClassLibraryChange(AbstractAMLClassLibraryImpl library) {
			super(library);
			libraryName = library.getName();
		}

		@Override
		public void undo() throws Exception {
			AMLDocumentImpl document = (AMLDocumentImpl) parentIdentifier.getDocumentElement();
			AbstractAMLDocumentElement documentElement = document._createSystemUnitClassLibrary(libraryName);
			identifier.setDocumentElement(documentElement);
		}
	}

	AMLSystemUnitClassLibraryImpl(AMLDocumentImpl amlDocument, String name) {
		super(amlDocument, name);
	}

	@Override
	protected AbstractAMLClassImpl<?> _newClass() throws AMLValidationException {
		return new AMLSystemUnitClassImpl(this);
	}

	@Override
	public AMLSystemUnitClass getSystemUnitClass(String name) {
		return (AMLSystemUnitClass) super.getClass(name);
	}

	@Override
	public Iterable<AMLSystemUnitClass> getSystemUnitClasses() {
		return (Iterable<AMLSystemUnitClass>) super.getClasses();
	}

	@Override
	public int getSystemUnitClassesCount() {
		return super.getClassesCount();
	}

	@Override
	public AMLSystemUnitClass createSystemUnitClass(String name) throws AMLValidationException {
		return createSystemUnitClass(name, null);
	}

	protected boolean isClassLibraryNameDefined(String newName) throws AMLDocumentScopeInvalidException {
		return getDocumentManager().isSystemUnitClassLibraryNameDefined(this, newName);
	}

	protected AMLValidationResultListImpl validateName(AMLValidator validator, String newName) {
		AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateSystemUnitClassLibrarySetName(this, newName);
		return validationResult;
	}

	protected void _setName(String newName) throws AMLValidationException {

		getDocument()._renameSystemUnitClassLibrary(this, newName);
		this.name = newName;
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		getDocument()._removeSystemUnitClassLibrary(name);
		getDocumentManager().removeSystemUnitClassLibraryName(getDocument(), name);
	}

	protected Change createDeleteChange() {
		Change change = new DeleteSystemUnitClassLibraryChange(this);
		return change;
	}

	@Override
	public AMLValidationResultList validateCreateSystemUnitClass(String name) {
		return validateRenameClass(name);
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		return validator.validateSystemUnitClassLibraryDelete(this);
	}

	@Override
	protected AMLValidationResultList _validateCreateClass(AMLValidator validator, String name) {
		return validator.validateSystemUnitClassCreate(this, name);
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
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "a system unit class library with name " + getName() + " already exists");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateSystemUnitClassLibraryReparent((AMLDocument)oldParentElement, (AMLDocument)newParentElement, this);
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
		oldParent._removeSystemUnitClassLibrary(getName());
		_setDocument(newParent);
		newParent._addSystemUnitClassLibrary(this, (AMLSystemUnitClassLibraryImpl)beforeElement, (AMLSystemUnitClassLibraryImpl)afterElement);		
	}
	
	@Override
	protected AMLDocumentElement _getBefore() {
		AMLDocumentImpl document = (AMLDocumentImpl) getParent();
		return document._getSystemUnitClassLibraryBefore(this);
	}
	
	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateSystemUnitClassLibraryDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
		super.doValidateDeepDelete(validator, baseElement, validationResultList);
	}

	@Override
	public AMLSystemUnitClass createSystemUnitClass(String name,
			AMLSystemUnitClass systemUnitClass) throws AMLValidationException {
		AMLSystemUnitClassImpl sysetmUnitClassImpl = (AMLSystemUnitClassImpl) super.createClass(name);
		
		if (systemUnitClass != null)
			sysetmUnitClassImpl.deepCopy(systemUnitClass, sysetmUnitClassImpl, systemUnitClass, new HashMap<AMLDocumentElement, AMLDocumentElement>());
		
		return sysetmUnitClassImpl;
	}

	@Override
	protected AMLDocumentElement _getAfter() {
		AMLDocumentImpl document = (AMLDocumentImpl) getParent();
		return document._getSystemUnitClassLibraryAfter(this);
	}
}
