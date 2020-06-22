/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.internal.aml.AMLRoleClassImpl;
import org.automationml.internal.aml.AMLRoleClassLibraryImpl;
import org.junit.Test;

public class AMLRoleValidatorTest extends AbstractAMLValidatorTest<AMLRoleClassLibrary, AMLRoleClass> {
	@Test
	public void valid_createTwoWeaklyConflictingLibraryReferences() throws Exception {
		_valid_createTwoWeaklyConflictingLibraryReferences();
	}

	@Test
	public void invalid_createTwoWeaklyConflictingLibraryReferences_enforcing() throws Exception {
		_invalid_createTwoWeaklyConflictingLibraryReferences_enforcing();
	}

	@Test
	public void valid_renameClass() throws Exception {
		_valid_renameClass(AMLRoleClassLibraryImpl.class, AMLRoleClassImpl.class);
	}

	@Test
	public void deleteClass() throws Exception {
		_valid_deleteClass(AMLRoleClassLibraryImpl.class, AMLRoleClassImpl.class);
	}

//	@Test
//	public void deleteClassLibrary() throws Exception {
//		_valid_deleteClassLibrary(AMLRoleClassLibraryImpl.class);
//	}


	@Override
	AMLRoleClassLibrary createClassLibrary(AMLDocument document, String name) throws AMLValidationException {
		return document.createRoleClassLibrary(name);
	}

	@Override
	void renameClassLibrary(AMLRoleClassLibrary library, String name) throws AMLValidationException {
		((AMLRoleClassLibrary) library).setName(name);
	}

	@Override
	AMLRoleClass createClass(AMLDocumentElement classContainer, String name) throws AMLValidationException {
		return ((AMLRoleClassLibrary) classContainer).createRoleClass(name);
	}

	@Override
	void renameClass(AMLRoleClass clazz, String name) throws AMLValidationException {
		((AMLRoleClass) clazz).setName(name);
	}

	@Override
	void deleteClass(AMLRoleClass clazz) throws AMLValidationException {
		((AMLRoleClass) clazz).delete();
	}

	@Override
	void deleteClassLibrary(AMLRoleClassLibrary library) throws AMLValidationException {
		library.delete();
	}

	@Override
	AMLValidationResultList validateCreateClassLibrary(AMLDocument document, String name) throws AMLValidationException {
		return document.validateCreateRoleClassLibrary(name);
	}

	@Override
	AMLValidationResultList validateCreateClass(AMLDocumentElement classContainer, String name) throws AMLValidationException {
		return ((AMLRoleClassLibrary) classContainer).validateCreateRoleClass(name);
	}
}
