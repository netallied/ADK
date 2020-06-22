/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;

import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLRoleClassLibrary;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.Change;

public class AMLRoleClassLibraryImpl extends AbstractAMLClassLibraryImpl implements AMLRoleClassLibrary {

	private static class DeleteRoleClassLibraryChange extends AbstractDeleteDocumentElementChange<AbstractAMLDocumentElement> {
		private String libraryName;

		public DeleteRoleClassLibraryChange(AbstractAMLClassLibraryImpl library) {
			super(library);
			libraryName = library.getName();
		}

		@Override
		public void undo() throws Exception {
			AMLDocumentImpl document = (AMLDocumentImpl) parentIdentifier.getDocumentElement();
			AbstractAMLDocumentElement documentElement = document._createRoleClassLibrary(libraryName);
			identifier.setDocumentElement(documentElement);
		}
	}

	AMLRoleClassLibraryImpl(AMLDocumentImpl amlDocument, String name) {
		super(amlDocument, name);
	}

	@Override
	protected AbstractAMLClassImpl<?> _newClass() throws AMLValidationException {
		return new AMLRoleClassImpl(this);
	}

	@Override
	public AMLRoleClass getRoleClass(String name) {
		return (AMLRoleClass) super.getClass(name);
	}

	@Override
	public Iterable<AMLRoleClass> getRoleClasses() {
		return (Iterable<AMLRoleClass>) super.getClasses();
	}
	
	@Override
	public int getRoleClassesCount() {
		return super.getClassesCount();
	}

	@Override
	public AMLRoleClass createRoleClass(String name) throws AMLValidationException {
		return createRoleClass(name, null);
	}

	protected boolean isClassLibraryNameDefined(String newName) throws AMLDocumentScopeInvalidException {
		return getDocumentManager().isRoleClassLibraryNameDefined(this, newName);
	}

	protected AMLValidationResultListImpl validateName(AMLValidator validator, String newName) {
		AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateRoleClassLibrarySetName(this, newName);
		return validationResult;
	}

	protected void _setName(String newName) throws AMLValidationException {
		AMLDocumentImpl document = getDocument();
		document._renameRoleClassLibrary(this, newName);
		this.name = newName;
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		getDocument()._removeRoleClassLibrary(name);
		getDocumentManager().removeRoleClassLibraryName(getDocument(), name);
	}

	protected Change createDeleteChange() {
		Change change = new DeleteRoleClassLibraryChange(this);
		return change;
	}


	@Override
	public AMLValidationResultList validateCreateRoleClass(String name) {
		return validateRenameClass(name);
	}


	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		return validator.validateRoleClassLibraryDelete(this);
	}

	@Override
	protected AMLValidationResultList _validateCreateClass(AMLValidator validator, String name) {
		return validator.validateRoleClassCreate(this, name);
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
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "a role class library with name " + getName() + " already exists");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateRoleClassLibraryReparent((AMLDocument)oldParentElement, (AMLDocument)newParentElement, this);
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
		oldParent._removeRoleClassLibrary(getName());
		_setDocument(newParent);
		newParent._addRoleClassLibrary(this, (AMLRoleClassLibraryImpl)beforeElement, (AMLRoleClassLibraryImpl)afterElement);		
	}
	
	@Override
	protected AMLDocumentElement _getBefore() {
		AMLDocumentImpl document = (AMLDocumentImpl) getParent();
		return document._getRoleClassLibraryBefore(this);
	}
	
	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateRoleClassLibraryDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
		super.doValidateDeepDelete(validator, baseElement, validationResultList);
	}

	@Override
	public AMLRoleClass createRoleClass(String name, AMLRoleClass roleClass)
			throws AMLValidationException {
		AMLRoleClassImpl roleClassImpl = (AMLRoleClassImpl) super.createClass(name);
		
		if (roleClass != null)
			roleClassImpl.deepCopy(roleClass, roleClassImpl, roleClass, new HashMap<AMLDocumentElement, AMLDocumentElement>());
		
		return roleClassImpl;
	}

	@Override
	protected AMLDocumentElement _getAfter() {
		AMLDocumentImpl document = (AMLDocumentImpl) getParent();
		return document._getRoleClassLibraryAfter(this);
	}
}
