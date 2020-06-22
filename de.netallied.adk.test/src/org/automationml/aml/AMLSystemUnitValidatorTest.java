/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.internal.aml.AMLSystemUnitClassImpl;
import org.automationml.internal.aml.AMLSystemUnitClassLibraryImpl;
import org.junit.Test;

public class AMLSystemUnitValidatorTest extends AbstractAMLValidatorTest<AMLSystemUnitClassLibrary, AMLSystemUnitClass> {
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
		_valid_renameClass(AMLSystemUnitClassLibraryImpl.class, AMLSystemUnitClassImpl.class);
	}

	@Test
	public void deleteClass() throws Exception {
		_valid_deleteClass(AMLSystemUnitClassLibraryImpl.class, AMLSystemUnitClassImpl.class);
	}

	@Override
	AMLSystemUnitClassLibrary createClassLibrary(AMLDocument document, String name) throws AMLValidationException {
		return document.createSystemUnitClassLibrary(name);
	}

	@Override
	void renameClassLibrary(AMLSystemUnitClassLibrary library, String name) throws AMLValidationException {
		((AMLSystemUnitClassLibrary) library).setName(name);
	}

	@Override
	AMLSystemUnitClass createClass(AMLDocumentElement classContainer, String name) throws AMLValidationException {
		return ((AMLSystemUnitClassLibrary) classContainer).createSystemUnitClass(name);
	}

	@Override
	void renameClass(AMLSystemUnitClass clazz, String name) throws AMLValidationException {
		((AMLSystemUnitClass) clazz).setName(name);
	}

	@Override
	void deleteClass(AMLSystemUnitClass clazz) throws AMLValidationException {
		((AMLSystemUnitClass) clazz).delete();
	}

	@Override
	void deleteClassLibrary(AMLSystemUnitClassLibrary library) throws AMLValidationException {
		library.delete();
	}

//	@Test
//	public void deleteClassLibrary() throws Exception {
//		_valid_deleteClassLibrary(AMLSystemUnitClassLibraryImpl.class);
//	}


	@Override
	AMLValidationResultList validateCreateClassLibrary(AMLDocument document, String name) throws AMLValidationException {
		return document.validateCreateSystemUnitClassLibrary(name);
	}

	@Override
	AMLValidationResultList validateCreateClass(AMLDocumentElement classContainer, String name) throws AMLValidationException {
		return ((AMLSystemUnitClassLibrary) classContainer).validateCreateSystemUnitClass(name);
	}
}
