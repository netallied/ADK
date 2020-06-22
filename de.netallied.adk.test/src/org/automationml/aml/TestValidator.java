/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.aml.AMLValidationResultImpl;
import org.automationml.internal.aml.AMLValidationResultListImpl;

class TestValidator extends AMLValidatorAdapter {

	static class TestValidatorFactory implements AMLValidatorFactory {
		@Override
		public AMLValidator createValidator(AMLSession session) throws AMLValidationException {
			return new TestValidator();
		}
	}

	boolean disposed = false;
	boolean enforcing;

	@Override
	public void dispose() {
		disposed = true;
	}

	@Override
	public AMLValidationResultList validateSession() {
		return AMLValidationResultListImpl.EMPTY;
	}

	@Override
	public AMLValidationResultList validateInterfaceClassLibrarySetName(AMLInterfaceClassLibrary library, String newName) {
		if (newName.endsWith("lib"))
			return super.validateInterfaceClassLibrarySetName(library, newName);

		AMLValidationResultImpl validationResult = new AMLValidationResultImpl(library, Severity.VALIDATION_WARNING, "Invalid library name");
		validationResult.setOperationPermitted(!enforcing);
		return new AMLValidationResultListImpl(validationResult);
	}

	@Override
	public AMLValidationResultList validateInterfaceClassLibraryCreate(AMLDocument document, String name) {
		if (name.endsWith("lib"))
			return super.validateInterfaceClassLibraryCreate(document, name);

		AMLValidationResultImpl validationResult = new AMLValidationResultImpl(document, Severity.VALIDATION_WARNING, "Invalid library name");
		validationResult.setOperationPermitted(!enforcing);
		return new AMLValidationResultListImpl(validationResult);
	}

	@Override
	public AMLValidationResultList validateSystemUnitClassLibrarySetName(AMLSystemUnitClassLibrary library, String newName) {
		if (newName.endsWith("lib"))
			return super.validateSystemUnitClassLibrarySetName(library, newName);

		AMLValidationResultImpl validationResult = new AMLValidationResultImpl(library, Severity.VALIDATION_WARNING, "Invalid library name");
		validationResult.setOperationPermitted(!enforcing);
		return new AMLValidationResultListImpl(validationResult);
	}

	@Override
	public AMLValidationResultList validateSystemUnitClassLibraryCreate(AMLDocument document, String libraryName) {
		if (libraryName.endsWith("lib"))
			return super.validateSystemUnitClassLibraryCreate(document, libraryName);

		AMLValidationResultImpl validationResult = new AMLValidationResultImpl(document, Severity.VALIDATION_WARNING, "Invalid library name");
		validationResult.setOperationPermitted(!enforcing);
		return new AMLValidationResultListImpl(validationResult);
	}

	@Override
	public AMLValidationResultList validateRoleClassLibrarySetName(AMLRoleClassLibrary library, String newName) {
		if (newName.endsWith("lib"))
			return super.validateRoleClassLibrarySetName(library, newName);

		AMLValidationResultImpl validationResult = new AMLValidationResultImpl(library, Severity.VALIDATION_WARNING, "Invalid library name");
		validationResult.setOperationPermitted(!enforcing);
		return new AMLValidationResultListImpl(validationResult);
	}

	@Override
	public AMLValidationResultList validateRoleClassLibraryCreate(AMLDocument document, String libraryName) {
		if (libraryName.endsWith("lib"))
			return super.validateRoleClassLibraryCreate(document, libraryName);

		AMLValidationResultImpl validationResult = new AMLValidationResultImpl(document, Severity.VALIDATION_WARNING, "Invalid library name");
		validationResult.setOperationPermitted(!enforcing);
		return new AMLValidationResultListImpl(validationResult);
	}

	@Override
	public AMLValidationResultList validateInterfaceClassSetName(AMLInterfaceClass clazz, String newName) {
		if (newName.endsWith("class"))
			return super.validateInterfaceClassSetName(clazz, newName);

		AMLValidationResultImpl validationResult = new AMLValidationResultImpl(clazz, Severity.VALIDATION_WARNING, "Invalid class name");
		validationResult.setOperationPermitted(!enforcing);
		return new AMLValidationResultListImpl(validationResult);
	}

	@Override
	public AMLValidationResultList validateRoleClassSetName(AMLRoleClass clazz, String newName) {
		if (newName.endsWith("class"))
			return super.validateRoleClassSetName(clazz, newName);

		AMLValidationResultImpl validationResult = new AMLValidationResultImpl(clazz, Severity.VALIDATION_WARNING, "Invalid class name");
		validationResult.setOperationPermitted(!enforcing);
		return new AMLValidationResultListImpl(validationResult);
	}

	@Override
	public AMLValidationResultList validateSystemUnitClassSetName(AMLSystemUnitClass clazz, String newName) {
		if (newName.endsWith("class"))
			return super.validateSystemUnitClassSetName(clazz, newName);

		AMLValidationResultImpl validationResult = new AMLValidationResultImpl(clazz, Severity.VALIDATION_WARNING, "Invalid class name");
		validationResult.setOperationPermitted(!enforcing);
		return new AMLValidationResultListImpl(validationResult);
	}

	@Override
	public AMLValidationResultList validateInternalElementSetName(AMLInternalElement internalElement, String newName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMLValidationResultList validateInternalElementSetBaseSystemUnitClass(AMLInternalElement internalElement, AMLSystemUnitClass systemUnitClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMLValidationResultListImpl validateInstanceHierarchyReparent(
			AMLDocument oldParentElement, AMLDocument newParentElement,
			AMLInstanceHierarchy amlInstanceHierarchyImpl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMLValidationResultListImpl validateRoleClassLibraryReparent(
			AMLDocument oldParentElement, AMLDocument newParentElement,
			AMLRoleClassLibrary amlRoleClassLibrary) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMLValidationResultListImpl validateInterfaceClassLibraryReparent(
			AMLDocument oldParentElement, AMLDocument newParentElement,
			AMLInterfaceClassLibrary amlInterfaceClassLibrary) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMLValidationResultListImpl validateSystemUnitClassLibraryReparent(
			AMLDocument oldParentElement, AMLDocument newParentElement,
			AMLSystemUnitClassLibrary amlSystemUnitClassLibrary) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMLValidationResultListImpl validateInterfaceClassReparent(
			AMLInterfaceClassContainer oldParentElement,
			AMLInterfaceClassContainer newParentElement,
			AMLInterfaceClass amlInterfaceClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMLValidationResultListImpl validateRoleClassReparent(
			AMLRoleClassContainer oldParentElement,
			AMLRoleClassContainer newParentElement, AMLRoleClass amlRoleClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMLValidationResultListImpl validateSystemUnitClassReparent(
			AMLSystemUnitClassContainer oldParentElement,
			AMLSystemUnitClassContainer newParentElement,
			AMLSystemUnitClass amlSystemUnitClass) {
		// TODO Auto-generated method stub
		return null;
	}

}