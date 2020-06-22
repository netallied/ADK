/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.Iterator;

import org.automationml.Savepoint;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class AMLSystemUnitClassLibraryTest extends AbstractAMLTest {

	@Test
	public void invalid_deleteSystemUnitClassLibrary_containingSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		lib1.createSystemUnitClass("baseclass");

		try {
			lib1.delete();
			fail();
		} catch (AMLValidationException e) {
		}

		assertThat(a.getSystemUnitClassLibrary("lib1")).isSameAs(lib1);
	}

	@Test
	public void invalid_setName() throws Exception {
		AMLDocument a = session.createAMLDocument();

		a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClassLibrary lib = a.createSystemUnitClassLibrary("lib2");
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

		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass class1 = lib1.createSystemUnitClass("class1");

		AMLSystemUnitClassLibrary lib_b = b.createSystemUnitClassLibrary("lib2");
		AMLSystemUnitClass class_b = lib_b.createSystemUnitClass("class1");
		class1.setBaseSystemUnitClass(class_b);

		AMLSystemUnitClassLibrary lib_c = c.createSystemUnitClassLibrary("lib");

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
	public void valid_addSystemUnitClassLibrary_twoScopes() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		a.createSystemUnitClassLibrary("lib1");
		Savepoint sp2 = session.createSavepoint();
		b.createSystemUnitClassLibrary("lib1");
		Savepoint sp3 = session.createSavepoint();

		sp1.restore();
		assertThat(a.getSystemUnitClassLibrary("lib1")).isNull();
		assertThat(b.getSystemUnitClassLibrary("lib1")).isNull();

		sp2.restore();
		assertThat(a.getSystemUnitClassLibrary("lib1")).isNotNull();
		assertThat(b.getSystemUnitClassLibrary("lib1")).isNull();

		sp3.restore();
		assertThat(a.getSystemUnitClassLibrary("lib1")).isNotNull();
		assertThat(b.getSystemUnitClassLibrary("lib1")).isNotNull();

		sp1.restore();
		assertThat(a.getSystemUnitClassLibrary("lib1")).isNull();
		assertThat(b.getSystemUnitClassLibrary("lib1")).isNull();

		sp1.delete();
		sp2.delete();
		sp3.delete();

	}

	@Test
	public void valid_deleteSystemUnitClassLibrary() throws Exception {
		AMLDocument a = session.createAMLDocument();

		attachDocumentChangeListener(a);

		AMLSystemUnitClassLibrary lib = a.createSystemUnitClassLibrary("lib1");
		assertThat(documentChangeListener.result.toString()).contains("begin\nCreated AMLSystemUnitClassLibraryImpl\nend\n");

		resetChangeListener();

		lib.delete();

		assertThat(a.getSystemUnitClassLibrary("lib1")).isNull();

		String string = documentChangeListener.result.toString();
		assertThat(string).contains("begin\nDeleting AMLSystemUnitClassLibraryImpl\nend\n");
		assertThat(string).contains("Validated AMLSystemUnitClassLibraryImpl");
	}

	@Test
	public void valid_deleteSystemUnitClassLibrary_twoScopes() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		AMLSystemUnitClassLibrary lib_a = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClassLibrary lib_b = b.createSystemUnitClassLibrary("lib1");
		Savepoint sp1 = session.createSavepoint();
		lib_a.delete();
		Savepoint sp2 = session.createSavepoint();
		lib_b.delete();
		Savepoint sp3 = session.createSavepoint();

		assertThat(a.getSystemUnitClassLibrary("lib1")).isNull();
		assertThat(b.getSystemUnitClassLibrary("lib1")).isNull();

		sp1.restore();
		assertThat(a.getSystemUnitClassLibrary("lib1")).isNotNull();
		assertThat(b.getSystemUnitClassLibrary("lib1")).isNotNull();

		sp2.restore();
		assertThat(a.getSystemUnitClassLibrary("lib1")).isNull();
		assertThat(b.getSystemUnitClassLibrary("lib1")).isNotNull();

		sp3.restore();
		assertThat(a.getSystemUnitClassLibrary("lib1")).isNull();
		assertThat(b.getSystemUnitClassLibrary("lib1")).isNull();

		sp1.delete();
		sp2.delete();
		sp3.delete();
	}

	@Test
	public void valid_deleteSystemUnitClassLibrary_undo() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary lib_a = a.createSystemUnitClassLibrary("lib1");

		Savepoint sp1 = session.createSavepoint();
		lib_a.delete();
		Savepoint sp2 = session.createSavepoint();

		assertThat(a.getSystemUnitClassLibrary("lib1")).isNull();

		sp1.restore();

		assertThat(a.getSystemUnitClassLibrary("lib1")).isNotNull();

		sp2.restore();
		assertThat(a.getSystemUnitClassLibrary("lib1")).isNull();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_getSystemUnitClassByPath() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		AMLSystemUnitClassLibrary lib_a_1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass systemUnitClass_a_1_1 = lib_a_1.createSystemUnitClass("class1");
		AMLSystemUnitClass systemUnitClass_a_1_2 = lib_a_1.createSystemUnitClass("class2");

		AMLSystemUnitClassLibrary lib_a_2 = a.createSystemUnitClassLibrary("lib2");
		AMLSystemUnitClass systemUnitClass_a_2_1 = lib_a_2.createSystemUnitClass("class1");
		AMLSystemUnitClass systemUnitClass_a_2_2 = lib_a_2.createSystemUnitClass("class2");

		AMLSystemUnitClassLibrary lib_b_1 = b.createSystemUnitClassLibrary("lib3");
		AMLSystemUnitClass systemUnitClass_b_1_1 = lib_b_1.createSystemUnitClass("class1");
		AMLSystemUnitClass systemUnitClass_b_1_2 = lib_b_1.createSystemUnitClass("class2");

		AMLSystemUnitClassLibrary lib_b_2 = b.createSystemUnitClassLibrary("lib4");
		AMLSystemUnitClass systemUnitClass_b_2_1 = lib_b_2.createSystemUnitClass("class1");
		AMLSystemUnitClass systemUnitClass_b_2_2 = lib_b_2.createSystemUnitClass("class2");

		assertThat(a.getSystemUnitClassByPath("lib1/class1")).isSameAs(systemUnitClass_a_1_1);
		assertThat(a.getSystemUnitClassByPath("lib1/class2")).isSameAs(systemUnitClass_a_1_2);
		assertThat(a.getSystemUnitClassByPath("lib2/class1")).isSameAs(systemUnitClass_a_2_1);
		assertThat(a.getSystemUnitClassByPath("lib2/class2")).isSameAs(systemUnitClass_a_2_2);

		assertThat(b.getSystemUnitClassByPath("lib3/class1")).isSameAs(systemUnitClass_b_1_1);
		assertThat(b.getSystemUnitClassByPath("lib3/class2")).isSameAs(systemUnitClass_b_1_2);
		assertThat(b.getSystemUnitClassByPath("lib4/class1")).isSameAs(systemUnitClass_b_2_1);
		assertThat(b.getSystemUnitClassByPath("lib4/class2")).isSameAs(systemUnitClass_b_2_2);

		assertThat(a.getSystemUnitClassByPath("lib1 / class")).isNull();
		assertThat(a.getSystemUnitClassByPath("x/class")).isNull();
		assertThat(a.getSystemUnitClassByPath("lib1/x")).isNull();

		assertThat(b.getSystemUnitClassByPath("lib1 / class")).isNull();
		assertThat(b.getSystemUnitClassByPath("x/class")).isNull();
		assertThat(b.getSystemUnitClassByPath("lib1/x")).isNull();
	}

	@Test
	public void valid_setName() throws Exception {
		AMLDocument a = session.createAMLDocument();

		a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClassLibrary lib = a.createSystemUnitClassLibrary("lib2");

		attachDocumentChangeListener(a);
		lib.setName("lib3");

		assertThat(lib.getName()).isEqualTo("lib3");

		assertThat(documentChangeListener.result.toString()).contains("begin\nModified AMLSystemUnitClassLibraryImpl\nend\n");
	}

	@Test
	public void valid_reparentOverDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		AMLSystemUnitClassLibrary systemUnitClassLibrary = b.createSystemUnitClassLibrary("sucl");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(systemUnitClassLibrary.validateReparent(b, a, null, null).isOk() == true);
		systemUnitClassLibrary.reparent(b, a,  null, null);
		
		Savepoint sp2 = session.createSavepoint();

		assertThat(a.getSystemUnitClassLibraries().iterator().hasNext());
		assertThat(a.getSystemUnitClassLibraries().iterator().next().equals(systemUnitClassLibrary));
		assertThat(b.getSystemUnitClassLibraries().iterator().hasNext() == false);
		
		sp1.restore();
		assertThat(b.getSystemUnitClassLibraries().iterator().hasNext());
		assertThat(b.getSystemUnitClassLibraries().iterator().next().equals(systemUnitClassLibrary));
		assertThat(a.getSystemUnitClassLibraries().iterator().hasNext() == false);
		
		sp2.restore();
		assertThat(a.getSystemUnitClassLibraries().iterator().hasNext());
		assertThat(a.getSystemUnitClassLibraries().iterator().next().equals(systemUnitClassLibrary));
		assertThat(b.getSystemUnitClassLibraries().iterator().hasNext() == false);
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentAfter() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary interfaceClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClassLibrary interfaceClassLibrary2 = a.createSystemUnitClassLibrary("sucl2");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(interfaceClassLibrary.validateReparent(a, a, null, interfaceClassLibrary2).isOk() == true);
		interfaceClassLibrary.reparent(a, a,  null, interfaceClassLibrary2);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLSystemUnitClassLibrary> iterator = a.getSystemUnitClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClassLibrary2));
		assertThat(iterator.next().equals(interfaceClassLibrary));
		
		sp1.restore();
		iterator = a.getSystemUnitClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClassLibrary));
		assertThat(iterator.next().equals(interfaceClassLibrary2));
		
		sp2.restore();
		iterator = a.getSystemUnitClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClassLibrary2));
		assertThat(iterator.next().equals(interfaceClassLibrary));
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentBefore() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary interfaceClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClassLibrary interfaceClassLibrary2 = a.createSystemUnitClassLibrary("sucl2");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(interfaceClassLibrary2.validateReparent(a, a, interfaceClassLibrary, null).isOk() == true);
		interfaceClassLibrary2.reparent(a, a,  interfaceClassLibrary, null);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLSystemUnitClassLibrary> iterator = a.getSystemUnitClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClassLibrary2));
		assertThat(iterator.next().equals(interfaceClassLibrary));
		
		sp1.restore();
		iterator = a.getSystemUnitClassLibraries().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClassLibrary));
		assertThat(iterator.next().equals(interfaceClassLibrary2));
		
		sp2.restore();
		iterator = a.getSystemUnitClassLibraries().iterator();
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
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setBaseSystemUnitClass(systemUnitClass);
		
		assertThat(systemUnitClassLibrary.validateReparent(a, b, null, null).isOk()).isTrue();
		systemUnitClassLibrary.reparent(a, b,  null, null);
	}
	
	@Test
	public void invalid_reparentWithSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setBaseSystemUnitClass(systemUnitClass);
		
		assertThat(instanceHierarchy.validateReparent(a, b, null, null).isAnyOperationNotPermitted()).isTrue();
		try {
			instanceHierarchy.reparent(a, b,  null, null);
			Fail.fail();
		} catch (AMLValidationException ex) {
			
		}
	}
	
	@Test
	public void invalid_reparentWithSystemUnitClass2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLInstanceHierarchy instanceHierarchy = b.createInstanceHierarchy("hierarchy");
		AMLSystemUnitClassLibrary systemUnitClassLibrary = b.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setBaseSystemUnitClass(systemUnitClass);
		
		assertThat(systemUnitClass.validateReparent(b, a, null, null).isAnyOperationNotPermitted()).isTrue();
		try {
			systemUnitClass.reparent(b, a,  null, null);
			Fail.fail();
		} catch (AMLValidationException ex) {
			
		}
	}
	
	@Test 
	public void invalid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		
		internalElement.setBaseSystemUnitClass(systemUnitClass);
		
		assertThat(systemUnitClassLibrary.validateDeepDelete().isAnyOperationNotPermitted()).isTrue();
		try {
			systemUnitClassLibrary.deepDelete();
			Fail.fail();
		} catch (AMLValidationException ex) {
			
		}
	}
	
	@Test 
	public void valid_deepDelete2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		
		internalElement.setBaseSystemUnitClass(systemUnitClass);
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(a.validateDeepDelete().isOk()).isTrue();
		a.deepDelete();
		
		assertThat(a.isDeleted()).isTrue();
		assertThat(systemUnitClass.isDeleted()).isTrue();
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
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(a.validateCreateSystemUnitClassLibrary("icl2").isOk()).isTrue();
		AMLSystemUnitClassLibrary copy = a.createSystemUnitClassLibrary("icl2", systemUnitClassLibrary);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(a.getSystemUnitClassLibrariesCount()).isEqualTo(2);
		
		sp1.restore();
		assertThat(a.getSystemUnitClassLibrariesCount()).isEqualTo(1);
		
		sp2.restore();
		assertThat(a.getSystemUnitClassLibrariesCount()).isEqualTo(2);
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void invalid_deepCopy() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
	
		assertThat(a.validateCreateSystemUnitClassLibrary("icl").isAnyOperationNotPermitted()).isTrue();
		try {
			a.createSystemUnitClassLibrary("icl", systemUnitClassLibrary);
			Fail.fail();
		} catch (AMLValidationException ex){
			
		}		
	}
	
}
