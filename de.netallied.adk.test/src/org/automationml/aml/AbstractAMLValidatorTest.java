/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.aml.TestValidator.TestValidatorFactory;
import org.junit.Before;

import static org.fest.assertions.Assertions.assertThat;

import static org.fest.assertions.Fail.fail;

public abstract class AbstractAMLValidatorTest<LIB extends AMLDocumentElement, CLASS extends AMLDocumentElement> extends AbstractAMLTest {

	private AMLDocument document;

	@Before
	public void before() throws AMLValidatorException{
		document = session.createAMLDocument();
		attachDocumentChangeListener(document);

		TestValidatorFactory validatorFactory = new TestValidatorFactory();
		session.setValidator(validatorFactory);
	}
	
	void _valid_createTwoWeaklyConflictingLibraryReferences() throws Exception {
		assertThat(validateCreateClassLibrary(document, "xlib").isOk()).isTrue();

		createClassLibrary(document, "xlib");
		String changeListenerResultString = documentChangeListener.result.toString();
		assertThat(changeListenerResultString).contains("Validated AMLDocumentImpl OK");

		assertThat(validateCreateClassLibrary(document, "x").isOk()).isFalse();

		resetChangeListener();
		createClassLibrary(document, "x");
		assertThat(documentChangeListener.result.toString()).contains("Validated AMLDocumentImpl VALIDATION_WARNING Invalid library name");
		
	}

	void _invalid_createTwoWeaklyConflictingLibraryReferences_enforcing() throws Exception {
		TestValidator validator = (TestValidator) session.getValidator();

		validator.enforcing = true;
		createClassLibrary(document, "xlib");

		Exception caughtException = null;

		resetChangeListener();
		try {
			createClassLibrary(document, "x");
			assertNoChangesNotified();
			fail();
		} catch (AMLValidationException e) {
			caughtException = e;
		}
		
		assertThat( caughtException ).isNotNull();
		assertThat( caughtException ).isInstanceOf( AMLValidationException.class );
	}

	@SuppressWarnings("rawtypes")
	void _valid_renameClass(Class libraryType, Class classType) throws Exception {
		LIB lib = createClassLibrary(document, "lib");

		resetChangeListener();
		CLASS clazz =  createClass(lib, "class1");
		assertThat(documentChangeListener.result.toString()).contains("Validated " + libraryType.getSimpleName() + " OK");
		
		resetChangeListener();
		renameClass(clazz, "xxx");

		assertThat(documentChangeListener.result.toString()).contains("Validated " + classType.getSimpleName() + " VALIDATION_WARNING Invalid class name");

		resetChangeListener();
		renameClass(clazz, "xxxclass");

		assertThat(documentChangeListener.result.toString()).contains("Validated " + classType.getSimpleName() + " OK");
	}

	void _valid_deleteClass(Class libraryType, Class classType) throws Exception {
		LIB lib = createClassLibrary(document, "lib");

		resetChangeListener();
		CLASS clazz =  createClass(lib, "class1");
		String string = documentChangeListener.result.toString();
		assertThat(string).contains("Validated " + libraryType.getSimpleName() + " OK");
		
		resetChangeListener();
		deleteClass(clazz);

		string = documentChangeListener.result.toString();
		assertThat(string).contains("Deleting " + classType.getSimpleName());
		assertThat(string).contains("Validated " + classType.getSimpleName());
	}

	void _valid_deleteClassLibrary(Class libraryType) throws Exception {
		LIB lib = createClassLibrary(document, "lib");

		resetChangeListener();
		deleteClassLibrary(lib);

		String string = documentChangeListener.result.toString();
		assertThat(string).contains("Deleting " + libraryType.getSimpleName());
		assertThat(string).contains("Validated " + libraryType.getSimpleName());
	}

	
	abstract LIB createClassLibrary(AMLDocument document, String name) throws AMLValidationException;

	abstract void renameClassLibrary(LIB library, String name) throws AMLValidationException;

	abstract CLASS createClass(AMLDocumentElement classContainer, String name) throws AMLValidationException;

	abstract void renameClass(CLASS clazz, String name) throws AMLValidationException;

	abstract AMLValidationResultList validateCreateClassLibrary(AMLDocument document, String name) throws AMLValidationException;

	abstract AMLValidationResultList validateCreateClass(AMLDocumentElement classContainer, String name) throws AMLValidationException;

	abstract void deleteClass(CLASS clazz) throws AMLValidationException;

	abstract void deleteClassLibrary(LIB library) throws AMLValidationException;

	//	abstract void validateClassLibrarySetName( String name) throws AMLValidationException;
	//
	//	abstract void validateCreateClass( String name) throws AMLValidationException;

}
