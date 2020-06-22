/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.automationml.Savepoint;
import org.fest.assertions.Fail;
import org.junit.Ignore;
import org.junit.Test;

import static org.automationml.aml.AMLSessionManager.amlSessionManager;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class AMLInterfaceClassLibraryTest extends AbstractAMLTest {

	private TestFileLocator testFileLocator = new TestFileLocator(getClass());

	@Test
	public void invalid_deleteInterfaceClassLibrary_containingInterfaceClass() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		lib1.createInterfaceClass("baseclass");

		try {
			lib1.delete();
			fail();
		} catch (AMLValidationException e) {
		}

		assertThat(a.getInterfaceClassLibrary("lib1")).isSameAs(lib1);
	}

	@Test(expected = AMLInvalidReferenceException.class)
	public void invalid_getInterfaceClassByPath() throws Exception {
		AMLDocument a = session.createAMLDocument();
		a.getInterfaceClassByPath("yxcy");
	}

	@Test
	public void invalid_setName() throws Exception {
		AMLDocument a = session.createAMLDocument();

		a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib2");
		try {
			lib.setName("lib1");
			fail();
		} catch (AMLValidationException e) {
		}

		assertThat(lib.getName()).isEqualTo("lib2");
	}

	@Test
	public void invalid_setName_secondDocument() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();

		a.addExplicitExternalReference(b);

		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass class1 = lib1.createInterfaceClass("class1");

		AMLInterfaceClassLibrary lib_b = b.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass class_b = lib_b.createInterfaceClass("class1");
		class1.setBaseInterfaceClass(class_b);

		AMLInterfaceClassLibrary lib_c = c.createInterfaceClassLibrary("lib");

		try {
			lib1.setName("lib2");
			fail();
		} catch (AMLValidationException e) {
		}

		assertThat(lib1.getName()).isEqualTo("lib1");

		try {
			lib_b.setName("lib1");
			fail();
		} catch (AMLValidationException e) {
		}

		assertThat(lib_b.getName()).isEqualTo("lib2");

		lib_c.setName("lib1");
		lib_c.setName("lib2");
	}

	@Test
	public void valid_addInterfaceClassLibrary_twoScopes() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		a.createInterfaceClassLibrary("lib1");
		Savepoint sp2 = session.createSavepoint();
		b.createInterfaceClassLibrary("lib1");
		Savepoint sp3 = session.createSavepoint();

		sp1.restore();
		assertThat(a.getInterfaceClassLibrary("lib1")).isNull();
		assertThat(b.getInterfaceClassLibrary("lib1")).isNull();

		sp2.restore();
		assertThat(a.getInterfaceClassLibrary("lib1")).isNotNull();
		assertThat(b.getInterfaceClassLibrary("lib1")).isNull();

		sp3.restore();
		assertThat(a.getInterfaceClassLibrary("lib1")).isNotNull();
		assertThat(b.getInterfaceClassLibrary("lib1")).isNotNull();

		sp1.restore();
		assertThat(a.getInterfaceClassLibrary("lib1")).isNull();
		assertThat(b.getInterfaceClassLibrary("lib1")).isNull();

		sp1.delete();
		sp2.delete();
		sp3.delete();

	}

	@Test
	public void valid_deleteInterfaceClassLibrary() throws Exception {
		AMLDocument a = session.createAMLDocument();

		attachDocumentChangeListener(a);

		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib1");
		assertThat(documentChangeListener.result.toString())
				.contains("begin\nCreated AMLInterfaceClassLibraryImpl\nend\n");

		resetChangeListener();

		lib.delete();

		assertThat(a.getInterfaceClassLibrary("lib1")).isNull();

		String string = documentChangeListener.result.toString();
		assertThat(string).contains("begin\nDeleting AMLInterfaceClassLibraryImpl\nend\n");
		assertThat(string).contains("Validated AMLInterfaceClassLibraryImpl");
	}

	@Test
	public void valid_deleteInterfaceClassLibrary_twoScopes() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		AMLInterfaceClassLibrary lib_a = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClassLibrary lib_b = b.createInterfaceClassLibrary("lib1");
		Savepoint sp1 = session.createSavepoint();
		lib_a.delete();
		Savepoint sp2 = session.createSavepoint();
		lib_b.delete();
		Savepoint sp3 = session.createSavepoint();

		assertThat(a.getInterfaceClassLibrary("lib1")).isNull();
		assertThat(b.getInterfaceClassLibrary("lib1")).isNull();

		sp1.restore();
		assertThat(a.getInterfaceClassLibrary("lib1")).isNotNull();
		assertThat(b.getInterfaceClassLibrary("lib1")).isNotNull();

		sp2.restore();
		assertThat(a.getInterfaceClassLibrary("lib1")).isNull();
		assertThat(b.getInterfaceClassLibrary("lib1")).isNotNull();

		sp3.restore();
		assertThat(a.getInterfaceClassLibrary("lib1")).isNull();
		assertThat(b.getInterfaceClassLibrary("lib1")).isNull();

		sp1.delete();
		sp2.delete();
		sp3.delete();
	}

	@Test
	public void valid_deleteInterfaceClassLibrary_undo() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary lib_a = a.createInterfaceClassLibrary("lib1");

		Savepoint sp1 = session.createSavepoint();
		lib_a.delete();
		Savepoint sp2 = session.createSavepoint();

		assertThat(a.getInterfaceClassLibrary("lib1")).isNull();

		sp1.restore();

		assertThat(a.getInterfaceClassLibrary("lib1")).isNotNull();

		sp2.restore();
		assertThat(a.getInterfaceClassLibrary("lib1")).isNull();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_getInterfaceClassByPath() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		AMLInterfaceClassLibrary lib_a_1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass_a_1_1 = lib_a_1.createInterfaceClass("class1");
		AMLInterfaceClass interfaceClass_a_1_2 = lib_a_1.createInterfaceClass("class2");

		AMLInterfaceClassLibrary lib_a_2 = a.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass_a_2_1 = lib_a_2.createInterfaceClass("class1");
		AMLInterfaceClass interfaceClass_a_2_2 = lib_a_2.createInterfaceClass("class2");

		AMLInterfaceClassLibrary lib_b_1 = b.createInterfaceClassLibrary("lib3");
		AMLInterfaceClass interfaceClass_b_1_1 = lib_b_1.createInterfaceClass("class1");
		AMLInterfaceClass interfaceClass_b_1_2 = lib_b_1.createInterfaceClass("class2");

		AMLInterfaceClassLibrary lib_b_2 = b.createInterfaceClassLibrary("lib4");
		AMLInterfaceClass interfaceClass_b_2_1 = lib_b_2.createInterfaceClass("class1");
		AMLInterfaceClass interfaceClass_b_2_2 = lib_b_2.createInterfaceClass("class2");

		assertThat(a.getInterfaceClassByPath("lib1/class1")).isSameAs(interfaceClass_a_1_1);
		assertThat(a.getInterfaceClassByPath("lib1/class2")).isSameAs(interfaceClass_a_1_2);
		assertThat(a.getInterfaceClassByPath("lib2/class1")).isSameAs(interfaceClass_a_2_1);
		assertThat(a.getInterfaceClassByPath("lib2/class2")).isSameAs(interfaceClass_a_2_2);

		assertThat(b.getInterfaceClassByPath("lib3/class1")).isSameAs(interfaceClass_b_1_1);
		assertThat(b.getInterfaceClassByPath("lib3/class2")).isSameAs(interfaceClass_b_1_2);
		assertThat(b.getInterfaceClassByPath("lib4/class1")).isSameAs(interfaceClass_b_2_1);
		assertThat(b.getInterfaceClassByPath("lib4/class2")).isSameAs(interfaceClass_b_2_2);

		assertThat(a.getInterfaceClassByPath("lib1 / class")).isNull();
		assertThat(a.getInterfaceClassByPath("x/class")).isNull();
		assertThat(a.getInterfaceClassByPath("lib1/x")).isNull();

		assertThat(b.getInterfaceClassByPath("lib1 / class")).isNull();
		assertThat(b.getInterfaceClassByPath("x/class")).isNull();
		assertThat(b.getInterfaceClassByPath("lib1/x")).isNull();
	}

	@Test
	public void valid_setName() throws Exception {
		AMLDocument a = session.createAMLDocument();

		a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib2");

		attachDocumentChangeListener(a);
		lib.setName("lib3");

		assertThat(lib.getName()).isEqualTo("lib3");

		assertThat(documentChangeListener.result.toString())
				.contains("begin\nModified AMLInterfaceClassLibraryImpl\nend\n");
	}

	@Test
	public void valid_createRenameRestore() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib");

		Savepoint sp1 = session.createSavepoint();
		lib.setName("lib2");

		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(lib.getName().equals("lib"));

		sp2.restore();
		assertThat(lib.getName().equals("lib2"));
		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_reparentOverDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		a.addExplicitExternalReference(b);
		AMLInterfaceClassLibrary interfaceClassLibrary = b.createInterfaceClassLibrary("icl");

		Savepoint sp1 = session.createSavepoint();

		assertThat(interfaceClassLibrary.validateReparent(b, a, null, null).isOk() == true);
		interfaceClassLibrary.reparent(b, a, null, null);

		Savepoint sp2 = session.createSavepoint();

		assertThat(a.getInterfaceClassLibraries().iterator().hasNext());
		assertThat(a.getInterfaceClassLibraries().iterator().next().equals(interfaceClassLibrary));
		assertThat(b.getInterfaceClassLibraries().iterator().hasNext() == false);

		sp1.restore();
		assertThat(b.getInterfaceClassLibraries().iterator().hasNext());
		assertThat(b.getInterfaceClassLibraries().iterator().next().equals(interfaceClassLibrary));
		assertThat(a.getInterfaceClassLibraries().iterator().hasNext() == false);

		sp2.restore();
		assertThat(a.getInterfaceClassLibraries().iterator().hasNext());
		assertThat(a.getInterfaceClassLibraries().iterator().next().equals(interfaceClassLibrary));
		assertThat(b.getInterfaceClassLibraries().iterator().hasNext() == false);

		sp2.delete();
		sp1.delete();
	}

	@Test
	public void valid_reparentOverDocuments_Dirty() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		a.addExplicitExternalReference(b);
		AMLInterfaceClassLibrary interfaceClassLibrary = b.createInterfaceClassLibrary("icl");
		a.unsetDirty();
		b.unsetDirty();
		Savepoint sp1 = session.createSavepoint();
		assertThat(a.isDirty()).isFalse();
		assertThat(b.isDirty()).isFalse();

		assertThat(interfaceClassLibrary.validateReparent(b, a, null, null).isOk() == true);
		interfaceClassLibrary.reparent(b, a, null, null);

		Savepoint sp2 = session.createSavepoint();
		assertThat(a.isDirty()).isTrue();
		assertThat(b.isDirty()).isTrue();

		sp1.restore();
		assertThat(a.isDirty()).isFalse();
		assertThat(b.isDirty()).isFalse();

		sp2.restore();
		assertThat(a.isDirty()).isTrue();
		assertThat(b.isDirty()).isTrue();

		sp2.delete();
		sp1.delete();
	}

	@Test
	public void valid_reparentAfter() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClassLibrary interfaceClassLibrary2 = a.createInterfaceClassLibrary("icl2");

		Savepoint sp1 = session.createSavepoint();

		assertThat(interfaceClassLibrary.validateReparent(a, a, null, interfaceClassLibrary2).isOk() == true);
		interfaceClassLibrary.reparent(a, a, null, interfaceClassLibrary2);

		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLInterfaceClassLibrary> iterator = a.getInterfaceClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClassLibrary2));
		assertThat(iterator.next().equals(interfaceClassLibrary));

		sp1.restore();
		iterator = a.getInterfaceClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClassLibrary));
		assertThat(iterator.next().equals(interfaceClassLibrary2));

		sp2.restore();
		iterator = a.getInterfaceClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClassLibrary2));
		assertThat(iterator.next().equals(interfaceClassLibrary));

		sp2.delete();
		sp1.delete();
	}

	@Test
	public void valid_reparentBefore() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClassLibrary interfaceClassLibrary2 = a.createInterfaceClassLibrary("icl2");

		Savepoint sp1 = session.createSavepoint();

		assertThat(interfaceClassLibrary2.validateReparent(a, a, interfaceClassLibrary, null).isOk() == true);
		interfaceClassLibrary2.reparent(a, a, interfaceClassLibrary, null);

		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLInterfaceClassLibrary> iterator = a.getInterfaceClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClassLibrary2));
		assertThat(iterator.next().equals(interfaceClassLibrary));

		sp1.restore();
		iterator = a.getInterfaceClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClassLibrary));
		assertThat(iterator.next().equals(interfaceClassLibrary2));

		sp2.restore();
		iterator = a.getInterfaceClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClassLibrary2));
		assertThat(iterator.next().equals(interfaceClassLibrary));

		sp2.delete();
		sp1.delete();
	}

	@Test
	public void valid_reparentWithRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		a.addExplicitExternalReference(b);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.createExternalInterface(interfaceClass);

		assertThat(interfaceClassLibrary.validateReparent(a, b, null, null).isOk()).isTrue();
		interfaceClassLibrary.reparent(a, b, null, null);
	}

	@Test
	public void invalid_reparentWithRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		a.addExplicitExternalReference(b);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.createExternalInterface(interfaceClass);

		assertThat(instanceHierarchy.validateReparent(a, b, null, null).isAnyOperationNotPermitted()).isTrue();
		try {
			instanceHierarchy.reparent(a, b, null, null);
			Fail.fail();
		} catch (AMLValidationException ex) {

		}
	}

	@Test
	public void valid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");

		Savepoint sp1 = session.createSavepoint();
		assertThat(interfaceClassLibrary.validateDeepDelete().isOk()).isTrue();
		interfaceClassLibrary.deepDelete();

		Savepoint sp2 = session.createSavepoint();
		assertThat(interfaceClassLibrary.isDeleted()).isTrue();
		assertThat(interfaceClass.isDeleted()).isTrue();

		sp1.restore();
		assertThat(a.getInterfaceClassLibraries().iterator().hasNext()).isTrue();
		assertThat(a.getInterfaceClassLibraries().iterator().next().getInterfaceClasses().iterator().hasNext())
				.isTrue();

		sp2.restore();
		assertThat(a.getInterfaceClassLibraries().iterator().hasNext()).isFalse();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void invalid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);

		assertThat(interfaceClassLibrary.validateDeepDelete().isAnyOperationNotPermitted()).isTrue();
		try {
			interfaceClassLibrary.deepDelete();
			Fail.fail();
		} catch (AMLValidationException ex) {

		}
	}

	@Test
	public void valid_deepDelete2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);

		Savepoint sp1 = session.createSavepoint();
		assertThat(a.validateDeepDelete().isOk()).isTrue();
		a.deepDelete();

		assertThat(a.isDeleted()).isTrue();
		assertThat(externalInterface.isDeleted()).isTrue();
		assertThat(session.getDocuments().iterator().hasNext()).isFalse();
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(session.getDocuments().iterator().hasNext()).isTrue();
		assertThat(session.getDocuments().iterator().next().getInstanceHierarchies().iterator().hasNext()).isTrue();

		sp2.restore();
		assertThat(session.getDocuments().iterator().hasNext()).isFalse();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_deepCopy() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");

		Savepoint sp1 = session.createSavepoint();
		assertThat(a.validateCreateInterfaceClassLibrary("icl2").isOk()).isTrue();
		AMLInterfaceClassLibrary copy = a.createInterfaceClassLibrary("icl2", interfaceClassLibrary);

		Savepoint sp2 = session.createSavepoint();
		assertThat(a.getInterfaceClassLibrariesCount()).isEqualTo(2);

		sp1.restore();
		assertThat(a.getInterfaceClassLibrariesCount()).isEqualTo(1);

		sp2.restore();
		assertThat(a.getInterfaceClassLibrariesCount()).isEqualTo(2);

		sp2.delete();
		sp1.delete();
	}

	@Test
	public void invalid_deepCopy() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");

		assertThat(a.validateCreateInterfaceClassLibrary("icl").isAnyOperationNotPermitted()).isTrue();
		try {
			a.createInterfaceClassLibrary("icl", interfaceClassLibrary);
			Fail.fail();
		} catch (AMLValidationException ex) {

		}
	}

	@Test
	public void invalid_copyLibrary() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("copyLibrary.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);
		AMLInterfaceClassLibrary lib1 = a.getInterfaceClassLibrary("AutomationMLInterfaceClassLib");
		AMLInterfaceClassLibrary lib2 = a.getInterfaceClassLibrary("ADKInterfaceLib");

		try {
			AMLDocument b = session.createAMLDocument();
			b.createInterfaceClassLibrary("AutomationMLInterfaceClassLib", lib1);
			b.createInterfaceClassLibrary("ADKInterfaceLib", lib2);

			assertThat(b.getInterfaceClassLibrariesCount()).isEqualTo(2);
			Fail.fail();
		} catch (AMLValidationException ex) {

		}
	}

	@Test
	public void valid_copyLibraryDifferentSession() throws Exception {
		AMLSession session2 = amlSessionManager.createSession();

		File file = testFileLocator.getValidTestCaseFile("copyLibrary.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);
		AMLInterfaceClassLibrary lib1 = a.getInterfaceClassLibrary("AutomationMLInterfaceClassLib");
		AMLInterfaceClassLibrary lib2 = a.getInterfaceClassLibrary("ADKInterfaceLib");

		AMLDocument b = session2.createAMLDocument();
		b.createInterfaceClassLibrary("AutomationMLInterfaceClassLib", lib1);
		b.createInterfaceClassLibrary("ADKInterfaceLib", lib2);

		assertThat(b.getInterfaceClassLibrariesCount()).isEqualTo(2);
	}
	
	@Test
	public void valid_copyLibraryDifferentSessionWithAttributes() throws Exception {
		AMLSession session2 = amlSessionManager.createSession();

		File file = testFileLocator.getValidTestCaseFile("copyLibrary.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);
		AMLInterfaceClassLibrary lib1 = a.getInterfaceClassLibrary("AutomationMLInterfaceClassLib");

		AMLDocument b = session2.createAMLDocument();
		b.createInterfaceClassLibrary("AutomationMLInterfaceClassLib", lib1);

		AMLInterfaceClass externalDataConnector = b.getInterfaceClassByPath("AutomationMLInterfaceClassLib/AutomationMLBaseInterface/ExternalDataConnector");
		assertThat(externalDataConnector).isNotNull();
		assertThat(externalDataConnector.getAttribute("refURI")).isNotNull();
	}


}
