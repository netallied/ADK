/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.internal.aml.AMLInterfaceClassImpl;
import org.automationml.internal.aml.AMLInterfaceClassLibraryImpl;
import org.junit.Test;

public class AMLInterfaceValidatorTest extends AbstractAMLValidatorTest<AMLInterfaceClassLibrary, AMLInterfaceClass> {
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
		_valid_renameClass(AMLInterfaceClassLibraryImpl.class, AMLInterfaceClassImpl.class);
	}

	@Test
	public void deleteClass() throws Exception {
		_valid_deleteClass(AMLInterfaceClassLibraryImpl.class, AMLInterfaceClassImpl.class);
	}

//	@Test
//	public void deleteClassLibrary() throws Exception {
//		_valid_deleteClassLibrary(AMLInterfaceClassLibraryImpl.class);
//	}


	@Override
	AMLInterfaceClassLibrary createClassLibrary(AMLDocument document, String name) throws AMLValidationException {
		return document.createInterfaceClassLibrary(name);
	}

	@Override
	void renameClassLibrary(AMLInterfaceClassLibrary library, String name) throws AMLValidationException {
		((AMLInterfaceClassLibrary) library).setName(name);
	}

	@Override
	AMLInterfaceClass createClass(AMLDocumentElement classContainer, String name) throws AMLValidationException {
		return ((AMLInterfaceClassLibrary) classContainer).createInterfaceClass(name);
	}

	@Override
	void renameClass(AMLInterfaceClass clazz, String name) throws AMLValidationException {
		((AMLInterfaceClass) clazz).setName(name);
	}

	@Override
	void deleteClass(AMLInterfaceClass clazz) throws AMLValidationException {
		((AMLInterfaceClass) clazz).delete();
	}

	@Override
	void deleteClassLibrary(AMLInterfaceClassLibrary library) throws AMLValidationException {
		library.delete();
	}

	@Override
	AMLValidationResultList validateCreateClassLibrary(AMLDocument document, String name) throws AMLValidationException {
		return document.validateCreateInterfaceClassLibrary(name);
	}

	@Override
	AMLValidationResultList validateCreateClass(AMLDocumentElement classContainer, String name) throws AMLValidationException {
		return ((AMLInterfaceClassLibrary) classContainer).validateCreateInterfaceClass(name);
	}

}
