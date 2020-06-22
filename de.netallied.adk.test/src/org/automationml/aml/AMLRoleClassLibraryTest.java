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
import org.junit.Test;

import static org.automationml.aml.AMLSessionManager.amlSessionManager;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class AMLRoleClassLibraryTest extends AbstractAMLTest {

	private TestFileLocator testFileLocator = new TestFileLocator(getClass());

	@Test
	public void invalid_deleteRoleClassLibrary_containingRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary lib1 = a.createRoleClassLibrary("lib1");
		lib1.createRoleClass("baseclass");

		try {
			lib1.delete();
			fail();
		} catch (AMLValidationException e) {
		}

		assertThat(a.getRoleClassLibrary("lib1")).isSameAs(lib1);
	}

	@Test
	public void invalid_getRoleClassByPath() throws Exception {
		AMLDocument a = session.createAMLDocument();
		assertThat(a.getRoleClassByPath("yxcy")).isNull();
	}

	@Test
	public void invalid_setName() throws Exception {
		AMLDocument a = session.createAMLDocument();

		a.createRoleClassLibrary("lib1");
		AMLRoleClassLibrary lib = a.createRoleClassLibrary("lib2");
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

		AMLRoleClassLibrary lib1 = a.createRoleClassLibrary("lib1");
		AMLRoleClass class1 = lib1.createRoleClass("class1");

		AMLRoleClassLibrary lib_b = b.createRoleClassLibrary("lib2");
		AMLRoleClass class_b = lib_b.createRoleClass("class1");
		class1.setBaseRoleClass(class_b);

		AMLRoleClassLibrary lib_c = c.createRoleClassLibrary("lib");

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
	public void valid_addRoleClassLibrary_twoScopes() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		a.createRoleClassLibrary("lib1");
		Savepoint sp2 = session.createSavepoint();
		b.createRoleClassLibrary("lib1");
		Savepoint sp3 = session.createSavepoint();

		sp1.restore();
		assertThat(a.getRoleClassLibrary("lib1")).isNull();
		assertThat(b.getRoleClassLibrary("lib1")).isNull();

		sp2.restore();
		assertThat(a.getRoleClassLibrary("lib1")).isNotNull();
		assertThat(b.getRoleClassLibrary("lib1")).isNull();

		sp3.restore();
		assertThat(a.getRoleClassLibrary("lib1")).isNotNull();
		assertThat(b.getRoleClassLibrary("lib1")).isNotNull();

		sp1.restore();
		assertThat(a.getRoleClassLibrary("lib1")).isNull();
		assertThat(b.getRoleClassLibrary("lib1")).isNull();

		sp1.delete();
		sp2.delete();
		sp3.delete();

	}

	@Test
	public void valid_deleteRoleClassLibrary() throws Exception {
		AMLDocument a = session.createAMLDocument();

		attachDocumentChangeListener(a);

		AMLRoleClassLibrary lib = a.createRoleClassLibrary("lib1");
		assertThat(documentChangeListener.result.toString()).contains("begin\nCreated AMLRoleClassLibraryImpl\nend\n");

		resetChangeListener();

		lib.delete();

		assertThat(a.getRoleClassLibrary("lib1")).isNull();

		String string = documentChangeListener.result.toString();
		assertThat(string).contains("begin\nDeleting AMLRoleClassLibraryImpl\nend\n");
		assertThat(string).contains("Validated AMLRoleClassLibraryImpl");
	}

	@Test
	public void valid_deleteRoleClassLibrary_twoScopes() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		AMLRoleClassLibrary lib_a = a.createRoleClassLibrary("lib1");
		AMLRoleClassLibrary lib_b = b.createRoleClassLibrary("lib1");
		Savepoint sp1 = session.createSavepoint();
		lib_a.delete();
		Savepoint sp2 = session.createSavepoint();
		lib_b.delete();
		Savepoint sp3 = session.createSavepoint();

		assertThat(a.getRoleClassLibrary("lib1")).isNull();
		assertThat(b.getRoleClassLibrary("lib1")).isNull();

		sp1.restore();
		assertThat(a.getRoleClassLibrary("lib1")).isNotNull();
		assertThat(b.getRoleClassLibrary("lib1")).isNotNull();

		sp2.restore();
		assertThat(a.getRoleClassLibrary("lib1")).isNull();
		assertThat(b.getRoleClassLibrary("lib1")).isNotNull();

		sp3.restore();
		assertThat(a.getRoleClassLibrary("lib1")).isNull();
		assertThat(b.getRoleClassLibrary("lib1")).isNull();

		sp1.delete();
		sp2.delete();
		sp3.delete();
	}

	@Test
	public void valid_deleteRoleClassLibrary_undo() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary lib_a = a.createRoleClassLibrary("lib1");

		Savepoint sp1 = session.createSavepoint();
		lib_a.delete();
		Savepoint sp2 = session.createSavepoint();

		assertThat(a.getRoleClassLibrary("lib1")).isNull();

		sp1.restore();

		assertThat(a.getRoleClassLibrary("lib1")).isNotNull();

		sp2.restore();
		assertThat(a.getRoleClassLibrary("lib1")).isNull();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_getRoleClassByPath() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		AMLRoleClassLibrary lib_a_1 = a.createRoleClassLibrary("lib1");
		AMLRoleClass class_a_1_1 = lib_a_1.createRoleClass("class1");
		AMLRoleClass class_a_1_2 = lib_a_1.createRoleClass("class2");

		AMLRoleClassLibrary lib_a_2 = a.createRoleClassLibrary("lib2");
		AMLRoleClass class_a_2_1 = lib_a_2.createRoleClass("class1");
		AMLRoleClass class_a_2_2 = lib_a_2.createRoleClass("class2");

		AMLRoleClassLibrary lib_b_1 = b.createRoleClassLibrary("lib3");
		AMLRoleClass class_b_1_1 = lib_b_1.createRoleClass("class1");
		AMLRoleClass class_b_1_2 = lib_b_1.createRoleClass("class2");

		AMLRoleClassLibrary lib_b_2 = b.createRoleClassLibrary("lib4");
		AMLRoleClass class_b_2_1 = lib_b_2.createRoleClass("class1");
		AMLRoleClass class_b_2_2 = lib_b_2.createRoleClass("class2");

		assertThat(a.getRoleClassByPath("lib1/class1")).isSameAs(class_a_1_1);
		assertThat(a.getRoleClassByPath("lib1/class2")).isSameAs(class_a_1_2);
		assertThat(a.getRoleClassByPath("lib2/class1")).isSameAs(class_a_2_1);
		assertThat(a.getRoleClassByPath("lib2/class2")).isSameAs(class_a_2_2);

		assertThat(b.getRoleClassByPath("lib3/class1")).isSameAs(class_b_1_1);
		assertThat(b.getRoleClassByPath("lib3/class2")).isSameAs(class_b_1_2);
		assertThat(b.getRoleClassByPath("lib4/class1")).isSameAs(class_b_2_1);
		assertThat(b.getRoleClassByPath("lib4/class2")).isSameAs(class_b_2_2);

		assertThat(a.getRoleClassByPath("lib1 / class")).isNull();
		assertThat(a.getRoleClassByPath("x/class")).isNull();
		assertThat(a.getRoleClassByPath("lib1/x")).isNull();

		assertThat(b.getRoleClassByPath("lib1 / class")).isNull();
		assertThat(b.getRoleClassByPath("x/class")).isNull();
		assertThat(b.getRoleClassByPath("lib1/x")).isNull();
	}

	@Test
	public void valid_setName() throws Exception {
		AMLDocument a = session.createAMLDocument();

		a.createRoleClassLibrary("lib1");
		AMLRoleClassLibrary lib = a.createRoleClassLibrary("lib2");

		attachDocumentChangeListener(a);
		lib.setName("lib3");

		String currentLibName = lib.getName();
		assertThat(currentLibName).isEqualTo("lib3");

		assertThat(documentChangeListener.result.toString()).contains("begin\nModified AMLRoleClassLibraryImpl\nend\n");
	}

	@Test
	public void valid_reparentOverDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		a.addExplicitExternalReference(b);
		AMLRoleClassLibrary roleClassLibrary = b.createRoleClassLibrary("rcl");

		Savepoint sp1 = session.createSavepoint();

		assertThat(roleClassLibrary.validateReparent(b, a, null, null).isOk() == true);
		roleClassLibrary.reparent(b, a, null, null);

		Savepoint sp2 = session.createSavepoint();

		assertThat(a.getRoleClassLibraries().iterator().hasNext());
		assertThat(a.getRoleClassLibraries().iterator().next().equals(roleClassLibrary));
		assertThat(b.getRoleClassLibraries().iterator().hasNext() == false);

		sp1.restore();
		assertThat(b.getRoleClassLibraries().iterator().hasNext());
		assertThat(b.getRoleClassLibraries().iterator().next().equals(roleClassLibrary));
		assertThat(a.getRoleClassLibraries().iterator().hasNext() == false);

		sp2.restore();
		assertThat(a.getRoleClassLibraries().iterator().hasNext());
		assertThat(a.getRoleClassLibraries().iterator().next().equals(roleClassLibrary));
		assertThat(b.getRoleClassLibraries().iterator().hasNext() == false);

		sp2.delete();
		sp1.delete();
	}

	@Test
	public void valid_reparentAfter() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("rcl");
		AMLRoleClassLibrary roleClassLibrary2 = a.createRoleClassLibrary("rcl2");

		Savepoint sp1 = session.createSavepoint();

		assertThat(roleClassLibrary.validateReparent(a, a, null, roleClassLibrary2).isOk() == true);
		roleClassLibrary.reparent(a, a, null, roleClassLibrary2);

		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLRoleClassLibrary> iterator = a.getRoleClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(roleClassLibrary2));
		assertThat(iterator.next().equals(roleClassLibrary));

		sp1.restore();
		iterator = a.getRoleClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(roleClassLibrary));
		assertThat(iterator.next().equals(roleClassLibrary2));

		sp2.restore();
		iterator = a.getRoleClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(roleClassLibrary2));
		assertThat(iterator.next().equals(roleClassLibrary));

		sp2.delete();
		sp1.delete();
	}

	@Test
	public void valid_reparentBefore() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("rcl");
		AMLRoleClassLibrary roleClassLibrary2 = a.createRoleClassLibrary("rcl2");

		Savepoint sp1 = session.createSavepoint();

		assertThat(roleClassLibrary2.validateReparent(a, a, roleClassLibrary, null).isOk() == true);
		roleClassLibrary2.reparent(a, a, roleClassLibrary, null);

		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLRoleClassLibrary> iterator = a.getRoleClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(roleClassLibrary2));
		assertThat(iterator.next().equals(roleClassLibrary));

		sp1.restore();
		iterator = a.getRoleClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(roleClassLibrary));
		assertThat(iterator.next().equals(roleClassLibrary2));

		sp2.restore();
		iterator = a.getRoleClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(roleClassLibrary2));
		assertThat(iterator.next().equals(roleClassLibrary));

		sp2.delete();
		sp1.delete();
	}

	@Test
	public void valid_reparentWithRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		a.addExplicitExternalReference(b);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("rcl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("rc");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.createSupportedRoleClass(roleClass);

		assertThat(roleClassLibrary.validateReparent(a, b, null, null).isOk()).isTrue();
		roleClassLibrary.reparent(a, b, null, null);
	}

	@Test
	public void invalid_reparentWithRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		a.addExplicitExternalReference(b);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("rcl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("rc");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.createSupportedRoleClass(roleClass);

		assertThat(instanceHierarchy.validateReparent(a, b, null, null).isAnyOperationNotPermitted() == true);
		try {
			instanceHierarchy.reparent(a, b, null, null);
			Fail.fail();
		} catch (AMLValidationException ex) {

		}
	}

	@Test
	public void invalid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("rcl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("rc");

		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);

		assertThat(roleClassLibrary.validateDeepDelete().isAnyOperationNotPermitted()).isTrue();
		try {
			roleClassLibrary.deepDelete();
			Fail.fail();
		} catch (AMLValidationException ex) {

		}
	}

	@Test
	public void valid_deepDelete2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("rcl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("rc");

		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);

		Savepoint sp1 = session.createSavepoint();
		assertThat(a.validateDeepDelete().isOk()).isTrue();
		a.deepDelete();

		assertThat(a.isDeleted()).isTrue();
		assertThat(supportedRoleClass.isDeleted()).isTrue();
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
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");

		Savepoint sp1 = session.createSavepoint();
		assertThat(a.validateCreateInterfaceClassLibrary("icl2").isOk()).isTrue();
		AMLRoleClassLibrary copy = a.createRoleClassLibrary("icl2", roleClassLibrary);

		Savepoint sp2 = session.createSavepoint();
		assertThat(a.getRoleClassLibrariesCount()).isEqualTo(2);

		sp1.restore();
		assertThat(a.getRoleClassLibrariesCount()).isEqualTo(1);

		sp2.restore();
		assertThat(a.getRoleClassLibrariesCount()).isEqualTo(2);

		sp2.delete();
		sp1.delete();
	}

	@Test
	public void invalid_deepCopy() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");

		assertThat(a.validateCreateRoleClassLibrary("icl").isAnyOperationNotPermitted()).isTrue();
		try {
			a.createRoleClassLibrary("icl", roleClassLibrary);
			Fail.fail();
		} catch (AMLValidationException ex) {

		}
	}

	@Test
	public void valid_copyLibraryDifferentSession() throws Exception {
		AMLSession session2 = amlSessionManager.createSession();

		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);
		AMLDocument sub = a.getExplicitlyReferencedDocuments().iterator().next();
		AMLRoleClassLibrary lib1 = a.getRoleClassLibrary("AutomationMLBaseRoleClassLib");
		AMLInterfaceClassLibrary lib2 = sub.getInterfaceClassLibrary("AutomationMLInterfaceClassLib");

		AMLDocument b = session2.createAMLDocument();
		b.createInterfaceClassLibrary("AutomationMLInterfaceClassLib", lib2);
		b.createRoleClassLibrary("AutomationMLBaseRoleClassLib", lib1);

		assertThat(b.getInterfaceClassLibrariesCount()).isEqualTo(1);
	}

	@Test
	public void invalid_copyLibraryDifferentSession() throws Exception {
		AMLSession session2 = amlSessionManager.createSession();

		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);
		AMLDocument sub = a.getExplicitlyReferencedDocuments().iterator().next();
		AMLRoleClassLibrary lib1 = a.getRoleClassLibrary("AutomationMLBaseRoleClassLib");
		AMLInterfaceClassLibrary lib2 = sub.getInterfaceClassLibrary("AutomationMLInterfaceClassLib");

		try {
			AMLDocument b = session2.createAMLDocument();
			b.createRoleClassLibrary("AutomationMLBaseRoleClassLib", lib1);
			Fail.fail();
			assertThat(b.getInterfaceClassLibrariesCount()).isEqualTo(1);
		} catch (AMLValidationException ex) {

		}
	}
	
	@Test
	public void valid_copyLibraryDifferentSession_Undo() throws Exception {
		AMLSession session2 = amlSessionManager.createSession();

		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);
		AMLDocument sub = a.getExplicitlyReferencedDocuments().iterator().next();
		AMLRoleClassLibrary lib1 = a.getRoleClassLibrary("AutomationMLBaseRoleClassLib");
		AMLInterfaceClassLibrary lib2 = sub.getInterfaceClassLibrary("AutomationMLInterfaceClassLib");

		AMLDocument b = session2.createAMLDocument();
		Savepoint sp1 = session2.createSavepoint();
		b.createInterfaceClassLibrary("AutomationMLInterfaceClassLib", lib2);
		
		{
			Savepoint sp2 = session2.createSavepoint();
			b.createRoleClassLibrary("AutomationMLBaseRoleClassLib", lib1);
			assertThat(b.getRoleClassLibrariesCount()).isEqualTo(1);
			assertThat(b.getInterfaceClassLibrariesCount()).isEqualTo(1);
			sp2.cancel();
			assertThat(b.getRoleClassLibrariesCount()).isEqualTo(0);
		}
		
		Savepoint sp2 = session2.createSavepoint();
		b.createRoleClassLibrary("AutomationMLBaseRoleClassLib", lib1);
		assertThat(b.getRoleClassLibrariesCount()).isEqualTo(1);
		assertThat(b.getInterfaceClassLibrariesCount()).isEqualTo(1);
		
		sp2.restore();
		assertThat(b.getRoleClassLibrariesCount()).isEqualTo(0);
		
		sp1.restore();
		assertThat(b.getInterfaceClassLibrariesCount()).isEqualTo(0);

		sp2.delete();
		sp1.delete();
	}
}
