/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.Iterator;
import java.util.UUID;

import org.automationml.Savepoint;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.automationml.aml.AMLSessionManager.amlSessionManager;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class AMLSystemUnitClassTest extends AbstractAMLTest {


	@Test
	public void invalid_deleteSystemUnitClass_referenced() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseclass = lib1.createSystemUnitClass("baseclass");
		AMLSystemUnitClass class1 = lib1.createSystemUnitClass("class1");
		class1.setBaseSystemUnitClass(baseclass);
	
		try {
			baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		assertThat(lib1.getSystemUnitClass("baseclass")).isSameAs(baseclass);
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseSystemUnitClass_cycle_oneDocument() throws Exception {
		AMLDocument doc = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib = doc.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass systemUnitClass1 = lib.createSystemUnitClass("class1");
		AMLSystemUnitClass systemUnitClass2 = lib.createSystemUnitClass("class2");
		AMLSystemUnitClass systemUnitClass3 = lib.createSystemUnitClass("class3");
	
		systemUnitClass1.setBaseSystemUnitClass(systemUnitClass2);
		systemUnitClass2.setBaseSystemUnitClass(systemUnitClass3);
		systemUnitClass3.setBaseSystemUnitClass(systemUnitClass1);
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseSystemUnitClass_cycle_twoDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass systemUnitClass1 = (AMLSystemUnitClass) lib.createSystemUnitClass("class1");
		AMLSystemUnitClass systemUnitClass2 = (AMLSystemUnitClass) lib.createSystemUnitClass("class2");
	
		AMLSystemUnitClassLibrary lib2 = b.createSystemUnitClassLibrary("lib2");
		AMLSystemUnitClass systemUnitClass3 = (AMLSystemUnitClass) lib2.createSystemUnitClass("class3");
		AMLSystemUnitClass systemUnitClass4 = (AMLSystemUnitClass) lib2.createSystemUnitClass("class4");
	
		systemUnitClass1.setBaseSystemUnitClass(systemUnitClass2);
		systemUnitClass2.setBaseSystemUnitClass(systemUnitClass3);
		systemUnitClass3.setBaseSystemUnitClass(systemUnitClass4);
		systemUnitClass4.setBaseSystemUnitClass(systemUnitClass1);
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseSystemUnitClass_onlyOneDocument() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");
		AMLSystemUnitClass systemUnitClass1 = lib1.createSystemUnitClass("class");
		AMLSystemUnitClass systemUnitClass2 = lib1.createSystemUnitClass("class2");
	
		AMLSystemUnitClassLibrary lib2 = a.createSystemUnitClassLibrary("lib2");
		lib2.createSystemUnitClass("class");
		lib2.createSystemUnitClass("class2");
	
		systemUnitClass1.setBaseSystemUnitClass(baseClass);
		systemUnitClass2.setBaseSystemUnitClass(baseClass);
	
		baseClass.delete();
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseSystemUnitClass_otherSession() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSession session2 = amlSessionManager.createSession();
		AMLDocument b = session2.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass systemUnitClass1 = lib.createSystemUnitClass("class1");
	
		AMLSystemUnitClassLibrary lib2 = b.createSystemUnitClassLibrary("lib2");
		AMLSystemUnitClass systemUnitClass2 = lib2.createSystemUnitClass("class3");
	
		systemUnitClass1.setBaseSystemUnitClass(systemUnitClass2);
	}
	
	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseSystemUnitClass2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		a.addExplicitExternalReference(b);
	
		AMLSystemUnitClassLibrary lib1_a = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass systemUnitClass1_a = lib1_a.createSystemUnitClass("class");
		AMLSystemUnitClass systemUnitClass2_a = lib1_a.createSystemUnitClass("class2");
	
		AMLSystemUnitClassLibrary lib2_b = b.createSystemUnitClassLibrary("lib2");
		AMLSystemUnitClass systemUnitClass2_b = lib2_b.createSystemUnitClass("class");
	
		systemUnitClass1_a.setBaseSystemUnitClass(systemUnitClass2_b);
		systemUnitClass2_a.setBaseSystemUnitClass(systemUnitClass2_b); // <---- systemUnitClass2_a != systemUnitClass1_a
		systemUnitClass1_a.unsetBaseSystemUnitClass();
	
		a.removeExplicitExternalReference(b);
		b.createSystemUnitClassLibrary("lib1");
	}


	@Test(expected = AMLValidationException.class)
	public void invalid_setName() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass systemUnitClass = lib.createSystemUnitClass("class");
		lib.createSystemUnitClass("class1");
	
		systemUnitClass.setName("class1");
	}

	@Test
	public void valid_addImplicitExternalReference() throws Exception {
		AMLDocument doc1 = session.createAMLDocument();
		AMLDocument doc2 = session.createAMLDocument();
		
		doc1.addExplicitExternalReference(doc2);
	
		AMLSystemUnitClassLibrary lib1_1 = doc1.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass systemUnitClass1 = lib1_1.createSystemUnitClass("class");
	
		AMLSystemUnitClassLibrary lib2_2 = doc2.createSystemUnitClassLibrary("lib2");
		AMLSystemUnitClass systemUnitClass2 = lib2_2.createSystemUnitClass("class");
	
		systemUnitClass1.setBaseSystemUnitClass(systemUnitClass2);
	}

	@Test
	public void valid_deleteSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseclass = lib1.createSystemUnitClass("baseclass");
	
		baseclass.delete();
	
		assertThat(lib1.getSystemUnitClass("baseclass")).isNull();
	}

	@Test
	public void valid_deleteSystemUnitClass_complex() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseclass1 = lib1.createSystemUnitClass("baseclass1");
		AMLSystemUnitClass baseclass2 = lib1.createSystemUnitClass("baseclass2");
		AMLSystemUnitClass class1 = lib1.createSystemUnitClass("class1");
		AMLSystemUnitClass class2 = lib1.createSystemUnitClass("class2");
		class1.setBaseSystemUnitClass(baseclass1);
		class2.setBaseSystemUnitClass(baseclass2);
	
		class1.setBaseSystemUnitClass(baseclass2);
		class2.setBaseSystemUnitClass(baseclass1);
	
		try {
			baseclass1.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		try {
			baseclass2.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		class2.unsetBaseSystemUnitClass();
	
		baseclass1.delete();
	
		try {
			baseclass2.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		class1.unsetBaseSystemUnitClass();
	
		baseclass2.delete();
	
		assertThat(lib1.getSystemUnitClass("baseclass")).isNull();
	}

	@Test
	public void valid_deleteSystemUnitClass_notReferencedAnyMore() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseclass = lib1.createSystemUnitClass("baseclass");
		AMLSystemUnitClass class1 = lib1.createSystemUnitClass("class1");
		AMLSystemUnitClass class2 = lib1.createSystemUnitClass("class2");
		class1.setBaseSystemUnitClass(baseclass);
		class2.setBaseSystemUnitClass(baseclass);
	
		try {
			baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		class1.unsetBaseSystemUnitClass();
	
		try {
			baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		class2.unsetBaseSystemUnitClass();
	
		baseclass.delete();
	
		assertThat(lib1.getSystemUnitClass("baseclass")).isNull();
	}

	@Test
	public void valid_setBaseClass_savepoints() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass systemUnitClass = lib.createSystemUnitClass("class");
		AMLSystemUnitClass baseClass1 = lib.createSystemUnitClass("baseClass1");
		AMLSystemUnitClass baseClass2 = lib.createSystemUnitClass("baseClass2");
	
		Savepoint sp1 = session.createSavepoint();
		systemUnitClass.setBaseSystemUnitClass(baseClass1);
		Savepoint sp2 = session.createSavepoint();
		systemUnitClass.setBaseSystemUnitClass(baseClass2);
		Savepoint sp3 = session.createSavepoint();
	
		sp1.restore();
		assertThat(systemUnitClass.getBaseSystemUnitClass()).isNull();
		
		

		sp2.restore();
		assertThat(systemUnitClass.getBaseSystemUnitClass()).isSameAs(baseClass1);
	
		sp3.restore();
		assertThat(systemUnitClass.getBaseSystemUnitClass()).isSameAs(baseClass2);
	
		sp2.restore();
		assertThat(systemUnitClass.getBaseSystemUnitClass()).isSameAs(baseClass1);
	
		sp1.delete();
		sp2.delete();
		sp3.delete();
	}

	@Test
	public void valid_setBaseClass_savepoints_removeImplicitReference() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib1= a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClassLibrary lib2 = a.createSystemUnitClassLibrary("lib2");
		AMLSystemUnitClass systemUnitClass = lib1.createSystemUnitClass("class");
		AMLSystemUnitClass baseClass = lib2.createSystemUnitClass("baseClass1");
	
		Savepoint sp1 = session.createSavepoint();
		systemUnitClass.setBaseSystemUnitClass(baseClass);
		Savepoint sp2 = session.createSavepoint();
	
		sp1.restore();
		assertThat(systemUnitClass.getBaseSystemUnitClass()).isNull();
		
		baseClass.delete();
		
		sp1.delete();
		sp2.delete();
		
	}
	@Test
	public void valid_setBaseSystemUnitClass_calledTwice() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		a.addExplicitExternalReference(b);
	
		AMLSystemUnitClassLibrary lib1_a = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass systemUnitClass1 = lib1_a.createSystemUnitClass("class");
	
		AMLSystemUnitClassLibrary lib2_b = b.createSystemUnitClassLibrary("lib2");
		AMLSystemUnitClass systemUnitClass2 = lib2_b.createSystemUnitClass("class");
	
		systemUnitClass1.setBaseSystemUnitClass(systemUnitClass2);
		systemUnitClass1.setBaseSystemUnitClass(systemUnitClass2);
		systemUnitClass1.unsetBaseSystemUnitClass();
	
		a.removeExplicitExternalReference(b);
		b.createSystemUnitClassLibrary("lib1");
	}

	@Test
	public void valid_setBaseSystemUnitClass_onlyOneDocument() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");
		AMLSystemUnitClass systemUnitClass1 = lib1.createSystemUnitClass("class");
		AMLSystemUnitClass systemUnitClass2 = lib1.createSystemUnitClass("class2");
	
		systemUnitClass1.setBaseSystemUnitClass(baseClass);
		systemUnitClass2.setBaseSystemUnitClass(baseClass);
		systemUnitClass1.unsetBaseSystemUnitClass();
	}
	
	@Test (expected = AMLValidationException.class)
	public void invalid_setBaseSystemUnitClass_twoDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");
	
		AMLSystemUnitClassLibrary lib2 = b.createSystemUnitClassLibrary("lib2");
		AMLSystemUnitClass systemUnitClass1 = lib2.createSystemUnitClass("class");
	
		systemUnitClass1.setBaseSystemUnitClass(baseClass);
	}


	@Test
	public void valid_setBaseSystemUnitClass_twoDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		b.addExplicitExternalReference(a);
	
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");
	
		AMLSystemUnitClassLibrary lib2 = b.createSystemUnitClassLibrary("lib2");
		AMLSystemUnitClass systemUnitClass1 = lib2.createSystemUnitClass("class");
		AMLSystemUnitClass systemUnitClass2 = lib2.createSystemUnitClass("class2");
	
		systemUnitClass1.setBaseSystemUnitClass(baseClass);
		systemUnitClass2.setBaseSystemUnitClass(baseClass);
	
		try {
			lib1.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		try {
			baseClass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		systemUnitClass1.unsetBaseSystemUnitClass();
		try {
			baseClass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		systemUnitClass2.unsetBaseSystemUnitClass();
	
		baseClass.delete();
	}

	@Test
	public void valid_setName_savepoints() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLSystemUnitClassLibrary lib = a.createSystemUnitClassLibrary("lib");
		AMLSystemUnitClass systemUnitClass = lib.createSystemUnitClass("class");
	
		Savepoint sp1 = session.createSavepoint();

		attachDocumentChangeListener(a);
		systemUnitClass.setName("xxx");
		assertThat(documentChangeListener.result.toString()).contains("begin\nModified AMLSystemUnitClassImpl\nend\n");
		
		resetChangeListener();
		systemUnitClass.setName("yyy");
		Savepoint sp2 = session.createSavepoint();
		assertThat(documentChangeListener.result.toString()).contains("begin\nModified AMLSystemUnitClassImpl\nend\n");

		sp1.restore();
		assertThat(systemUnitClass.getName()).isEqualTo("class");
	
		sp2.restore();
		assertThat(systemUnitClass.getName()).isEqualTo("yyy");
	
		sp1.delete();
		sp2.delete();
		
//		System.out.println(changeListener);

	}
	
	@Test (expected = AMLValidationException.class)
	public void invalid_deleteSystemUnitClassWithReferencingInternalElement() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");	
		
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setBaseSystemUnitClass(baseClass);
		baseClass.delete();	
	}
	
	@Test 
	public void valid_deleteSystemUnitClassWithReferencingInternalElement() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");	
		
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setBaseSystemUnitClass(baseClass);
		internalElement.unsetBaseSystemUnitClass();

		baseClass.delete();		
		assertThat(internalElement.getBaseSystemUnitClass()).isNull();
		assertThat(baseClass.isDeleted());
		
	}
	
	@Test (expected = AMLValidationException.class)
	public void invalid_deleteSystemUnitClassWithChildInternalElement() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");
		
		baseClass.createInternalElement();
		baseClass.delete();	
	}
	
	@Test
	public void valid_reparentOverDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		AMLSystemUnitClassLibrary interfaceClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClassLibrary interfaceClassLibrary2 = (AMLSystemUnitClassLibrary) b.createSystemUnitClassLibrary("icl2");
		AMLSystemUnitClass interfaceClass = interfaceClassLibrary2.createSystemUnitClass("ic");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(interfaceClass.validateReparent(interfaceClassLibrary2, interfaceClassLibrary, null, null).isOk());
		interfaceClass.reparent(interfaceClassLibrary2, interfaceClassLibrary,  null, null);
		
		Savepoint sp2 = session.createSavepoint();

		assertThat(interfaceClassLibrary.getSystemUnitClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getSystemUnitClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getSystemUnitClasses().iterator().hasNext() == false);
		
		sp1.restore();
		assertThat(interfaceClassLibrary2.getSystemUnitClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary2.getSystemUnitClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary.getSystemUnitClasses().iterator().hasNext() == false);
		
		sp2.restore();
		assertThat(interfaceClassLibrary.getSystemUnitClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getSystemUnitClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getSystemUnitClasses().iterator().hasNext() == false);
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentOverDocuments2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		b.addExplicitExternalReference(a);
		AMLSystemUnitClassLibrary interfaceClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClassLibrary interfaceClassLibrary2 = (AMLSystemUnitClassLibrary) b.createSystemUnitClassLibrary("icl2");
		AMLSystemUnitClass interfaceClass = interfaceClassLibrary2.createSystemUnitClass("ic");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(interfaceClass.validateReparent(interfaceClassLibrary2, interfaceClassLibrary, null, null).isOk() == true);
		interfaceClass.reparent(interfaceClassLibrary2, interfaceClassLibrary,  null, null);
		
		Savepoint sp2 = session.createSavepoint();

		assertThat(interfaceClassLibrary.getSystemUnitClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getSystemUnitClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getSystemUnitClasses().iterator().hasNext() == false);
		
		sp1.restore();
		assertThat(interfaceClassLibrary2.getSystemUnitClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary2.getSystemUnitClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary.getSystemUnitClasses().iterator().hasNext() == false);
		
		sp2.restore();
		assertThat(interfaceClassLibrary.getSystemUnitClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getSystemUnitClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getSystemUnitClasses().iterator().hasNext() == false);
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentAfter() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary interfaceClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass interfaceClass = interfaceClassLibrary.createSystemUnitClass("ic1");
		AMLSystemUnitClass interfaceClass2 = interfaceClassLibrary.createSystemUnitClass("ic2");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(interfaceClass.validateReparent(interfaceClassLibrary, interfaceClassLibrary, null, interfaceClass2).isOk() == true);
		interfaceClass.reparent(interfaceClassLibrary, interfaceClassLibrary,  null, interfaceClass2);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLSystemUnitClass> iterator = interfaceClassLibrary.getSystemUnitClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClass2));
		assertThat(iterator.next().equals(interfaceClass));
		
		sp1.restore();
		iterator = interfaceClassLibrary.getSystemUnitClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClass));
		assertThat(iterator.next().equals(interfaceClass2));
		
		sp2.restore();
		iterator = interfaceClassLibrary.getSystemUnitClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClass2));
		assertThat(iterator.next().equals(interfaceClass));
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentBefore() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary instanceHierarchy = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass internalElement2 = instanceHierarchy.createSystemUnitClass("ic1");
		AMLSystemUnitClass internalElement = instanceHierarchy.createSystemUnitClass("ic2");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(internalElement.validateReparent(instanceHierarchy, instanceHierarchy, internalElement2, null).isOk() == true);
		internalElement.reparent(instanceHierarchy, instanceHierarchy, internalElement2,  null);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLSystemUnitClass> iterator = instanceHierarchy.getSystemUnitClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement2));
		assertThat(iterator.next().equals(internalElement));
		
		sp1.restore();
		iterator = instanceHierarchy.getSystemUnitClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement));
		assertThat(iterator.next().equals(internalElement2));
		
		sp2.restore();
		iterator = instanceHierarchy.getSystemUnitClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement2));
		assertThat(iterator.next().equals(internalElement));
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test 
	public void invalid_reparentBaseClassNotInScope() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		AMLSystemUnitClassLibrary interfaceClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass interfaceClass = interfaceClassLibrary.createSystemUnitClass("ic");
		AMLSystemUnitClass interfaceClass2 = interfaceClassLibrary.createSystemUnitClass("ic2");
		interfaceClass2.setBaseSystemUnitClass(interfaceClass);
		AMLSystemUnitClassLibrary interfaceClassLibrary2 = b.createSystemUnitClassLibrary("icl2");
		
		assertThat(interfaceClass2.validateReparent(interfaceClass, interfaceClassLibrary2, null, null).isAnyOperationNotPermitted());
		try {
			interfaceClass2.reparent(interfaceClass, interfaceClassLibrary2, null, null);
			Fail.fail();
		} catch (Exception ex) {
		}
		
	}
	
	@Test
	public void invalid_reparentSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLSystemUnitClassLibrary interfaceClassLibrary = b.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass interfaceClass = interfaceClassLibrary.createSystemUnitClass("ic");
		
		AMLSystemUnitClassLibrary interfaceClassLibrary2 = b.createSystemUnitClassLibrary("icl2");
		AMLSystemUnitClass interfaceClass2 = interfaceClassLibrary2.createSystemUnitClass("ic2");
		interfaceClass.setBaseSystemUnitClass(interfaceClass2);
		
		assertThat(interfaceClassLibrary2.validateReparent(b, a, null, null).isAnyOperationNotPermitted()).isTrue();
	}
	
	@Test
	public void invalid_reparentParentToChildSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		
		AMLSystemUnitClassLibrary interfaceClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass parent = interfaceClassLibrary.createSystemUnitClass("ic");
		AMLSystemUnitClass child = parent.createSystemUnitClass("ic2");
		
		assertThat(parent.validateReparent(interfaceClassLibrary, child, null, null).isAnyOperationNotPermitted()).isTrue();
		
	}
	
	@Test
	public void valid_reparentSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLSystemUnitClassLibrary interfaceClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass interfaceClass = interfaceClassLibrary.createSystemUnitClass("ic");
		
		AMLSystemUnitClassLibrary interfaceClassLibrary2 = a.createSystemUnitClassLibrary("icl2");
		AMLSystemUnitClass interfaceClass2 = interfaceClassLibrary2.createSystemUnitClass("ic2");
		interfaceClass.setBaseSystemUnitClass(interfaceClass2);
		
		assertThat(interfaceClassLibrary2.validateReparent(a, b, null, null).isOk()).isTrue();
	}
	
	@Test
	public void valid_unlink() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");		
		AMLSystemUnitClassLibrary systemUnitClassLibrary2 = a.createSystemUnitClassLibrary("icl2");
		AMLSystemUnitClass systemUnitClass2 = systemUnitClassLibrary2.createSystemUnitClass("ic2");	
		systemUnitClass2.setBaseSystemUnitClass(systemUnitClass);
		
		Savepoint sp1 = session.createSavepoint();
		systemUnitClass2.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isNull();		
		
		sp1.restore();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp2.restore();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isNull();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlink2() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");		
		AMLSystemUnitClassLibrary systemUnitClassLibrary2 = a.createSystemUnitClassLibrary("icl2");
		AMLSystemUnitClass systemUnitClass2 = systemUnitClassLibrary2.createSystemUnitClass("ic2");	
		systemUnitClass2.setBaseSystemUnitClass(systemUnitClass);
		
		Savepoint sp1 = session.createSavepoint();
		systemUnitClass.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isNull();		
		
		sp1.restore();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp2.restore();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isNull();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlinkConcurrent() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLSystemUnitClass systemUnitClass3 = systemUnitClass.createSystemUnitClass("ic3");	
		AMLSystemUnitClassLibrary systemUnitClassLibrary2 = a.createSystemUnitClassLibrary("icl2");
		AMLSystemUnitClass systemUnitClass2 = systemUnitClassLibrary2.createSystemUnitClass("ic2");	
		systemUnitClass2.setBaseSystemUnitClass(systemUnitClass);
		systemUnitClass3.setBaseSystemUnitClass(systemUnitClass);
		
		Savepoint sp1 = session.createSavepoint();
		systemUnitClass.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isNull();		
		
		sp1.restore();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp2.restore();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isNull();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlink3() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");		
		AMLSystemUnitClassLibrary systemUnitClassLibrary2 = a.createSystemUnitClassLibrary("icl2");
		AMLSystemUnitClass systemUnitClass2 = systemUnitClassLibrary2.createSystemUnitClass("ic2");	
		systemUnitClass2.setBaseSystemUnitClass(systemUnitClass);
		
		Savepoint sp1 = session.createSavepoint();
		a.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp1.restore();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp2.restore();
		assertThat(systemUnitClass2.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp1.delete();
		sp2.delete();			
	}
	
	@Test 
	public void invalid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		AMLSystemUnitClass systemUnitClass2 = systemUnitClass.createSystemUnitClass("suc2");
		
		internalElement.setBaseSystemUnitClass(systemUnitClass2);
		
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
		AMLSystemUnitClass systemUnitClass2 = systemUnitClass.createSystemUnitClass("suc2");
		
		internalElement.setBaseSystemUnitClass(systemUnitClass2);
		
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
	public void valid_deepDelete3() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		AMLSystemUnitClass systemUnitClass2 = systemUnitClass.createSystemUnitClass("suc2");
		systemUnitClass2.setBaseSystemUnitClass(systemUnitClass);
		
		internalElement.setBaseSystemUnitClass(systemUnitClass2);
		
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
	public void valid_deepDelete4() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		AMLAttribute attribute = systemUnitClass.createAttribute("attr");
		attribute.setValue("5");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(systemUnitClass.validateDeepDelete().isOk()).isTrue();
		a.deepDelete();
		
		assertThat(systemUnitClass.isDeleted()).isTrue();
		assertThat(attribute.isDeleted()).isTrue();
		assertThat(session.getDocuments().iterator().hasNext()).isFalse();
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		
		sp2.restore();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_copy() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLSystemUnitClass systemUnitClass2 = systemUnitClass.createSystemUnitClass("ic2");
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(systemUnitClassLibrary.validateCreateSystemUnitClass("ic2").isOk()).isTrue();
		AMLSystemUnitClass copy = systemUnitClassLibrary.createSystemUnitClass("ic2", systemUnitClass);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(systemUnitClassLibrary.getSystemUnitClassesCount()).isEqualTo(2);
		
		sp1.restore();
		assertThat(systemUnitClassLibrary.getSystemUnitClassesCount()).isEqualTo(1);
		
		sp2.restore();
		assertThat(systemUnitClassLibrary.getSystemUnitClassesCount()).isEqualTo(2);
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void valid_deepCopy2() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLSystemUnitClass systemUnitClass2 = systemUnitClass.createSystemUnitClass("ic2");
		systemUnitClass2.setBaseSystemUnitClass(systemUnitClass);
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(systemUnitClassLibrary.validateCreateSystemUnitClass("ic2").isOk()).isTrue();
		AMLSystemUnitClass copy = systemUnitClassLibrary.createSystemUnitClass("ic2", systemUnitClass);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(copy.getSystemUnitClasses().iterator().next().getBaseSystemUnitClass()).isEqualTo(copy);
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLSystemUnitClass> iterator = systemUnitClassLibrary.getSystemUnitClasses().iterator();
		iterator.next();
		copy = iterator.next(); 
		assertThat(copy.getSystemUnitClasses().iterator().next().getBaseSystemUnitClass()).isEqualTo(copy);
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void valid_deepCopy3() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLSystemUnitClass systemUnitClass2 = systemUnitClass.createSystemUnitClass("ic2");
		systemUnitClass2.setBaseSystemUnitClass(systemUnitClass);
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(systemUnitClass.validateCreateSystemUnitClass("ic3").isOk()).isTrue();
		AMLSystemUnitClass copy = systemUnitClass.createSystemUnitClass("ic3", systemUnitClass2);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(copy.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLSystemUnitClass> iterator = systemUnitClass.getSystemUnitClasses().iterator();
		iterator.next();
		assertThat(iterator.next().getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void valid_deepCopy_AvoidStackOverflow() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLSystemUnitClass systemUnitClass2 = systemUnitClass.createSystemUnitClass("ic2");
		systemUnitClass2.setBaseSystemUnitClass(systemUnitClass);
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(systemUnitClass.validateCreateSystemUnitClass("ic3").isOk()).isTrue();
		AMLSystemUnitClass copy = systemUnitClass.createSystemUnitClass("ic3", systemUnitClass);
		
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void invalid_deepCopy() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLSystemUnitClass systemUnitClass2 = systemUnitClass.createSystemUnitClass("ic2");
	
		assertThat(systemUnitClassLibrary.validateCreateSystemUnitClass("ic").isAnyOperationNotPermitted()).isTrue();
		try {
			systemUnitClassLibrary.createSystemUnitClass("ic", systemUnitClass);
			Fail.fail();
		} catch (AMLValidationException ex){
			
		}		
	}
	
	@Test
	public void valid_deepCopy_IEWithAttributes() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLInternalElement internalElement = systemUnitClass.createInternalElement();
		internalElement.createAttribute("att");
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(systemUnitClass);
		assertThat(copy.getInternalElementsCount()).isEqualTo(1);
		assertThat(copy.getInternalElements().iterator().next().getAttributesCount()).isEqualTo(1);
		assertThat(copy.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		Savepoint sp2 = session.createSavepoint();		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		assertThat(instanceHierarchy.getInternalElementsCount()).isEqualTo(1);
		copy = instanceHierarchy.getInternalElements().iterator().next();
		assertThat(copy.getInternalElementsCount()).isEqualTo(1);
		assertThat(copy.getInternalElements().iterator().next().getAttributesCount()).isEqualTo(1);
		
		sp2.delete();
		sp1.delete();
	}
	
	@Test
	public void valid_deepCopy_WithInternalLinks() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary icl = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass ic = icl.createInterfaceClass("ic");
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLInternalElement parent = systemUnitClass.createInternalElement();
		AMLInternalElement internalElement1 = parent.createInternalElement();
		AMLExternalInterface externalInterface1 = internalElement1.createExternalInterface(ic);
		AMLInternalElement internalElement2 = parent.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement1.createExternalInterface(ic);
		AMLInternalLink link = parent.createInternalLink("link", externalInterface1, externalInterface2);
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(systemUnitClass);
		assertThat(copy.getInternalElementsCount()).isEqualTo(1);
		assertThat(copy.getInternalElements().iterator().next().getInternalLinks().iterator().hasNext()).isTrue();
		
		Savepoint sp2 = session.createSavepoint();		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		assertThat(instanceHierarchy.getInternalElementsCount()).isEqualTo(1);
		copy = instanceHierarchy.getInternalElements().iterator().next();
		assertThat(copy.getInternalElementsCount()).isEqualTo(1);
		assertThat(copy.getInternalElements().iterator().next().getInternalLinks().iterator().hasNext()).isTrue();
		
		sp2.delete();
		sp1.delete();
	}
	
	@Test
	public void valid_deepCopy_WithInternalLinks2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary icl = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass ic = icl.createInterfaceClass("ic");
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");
		AMLInternalElement internalElement1 = systemUnitClass.createInternalElement();
		AMLExternalInterface externalInterface1 = internalElement1.createExternalInterface(ic);
		AMLInternalElement internalElement2 = systemUnitClass.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement1.createExternalInterface(ic);
		AMLInternalLink link = systemUnitClass.createInternalLink("link", externalInterface1, externalInterface2);
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(systemUnitClass);
		assertThat(copy.getInternalElementsCount()).isEqualTo(2);
		assertThat(copy.getInternalLinks().iterator().hasNext()).isTrue();
		
		Savepoint sp2 = session.createSavepoint();		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		assertThat(instanceHierarchy.getInternalElementsCount()).isEqualTo(1);
		copy = instanceHierarchy.getInternalElements().iterator().next();
		assertThat(copy.getInternalElementsCount()).isEqualTo(2);
		assertThat(copy.getInternalLinks().iterator().hasNext()).isTrue();
		
		sp2.delete();
		sp1.delete();
	}
	
	@Test 
	public void valid_DeleteSucWithexternalInterface() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		
		AMLInterfaceClassLibrary icl = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass ic = icl.createInterfaceClass("ic");
		
		AMLExternalInterface externalInterface = systemUnitClass.createExternalInterface(ic);
		UUID id = externalInterface.getId();
		Savepoint sp1 = session.createSavepoint();
		systemUnitClass.deepDelete();
		assertThat(systemUnitClass.isDeleted()).isTrue();
		assertThat(externalInterface.isDeleted()).isTrue();
		
		Savepoint sp2 = session.createSavepoint();		
		sp1.restore();
		systemUnitClass = systemUnitClassLibrary.getSystemUnitClass("suc");
		assertThat(systemUnitClass.isDeleted()).isFalse();
		externalInterface = systemUnitClass.getExternalInterface(id);
		assertThat(externalInterface.isDeleted()).isFalse();	
		
		sp2.restore();
		assertThat(systemUnitClass.isDeleted());
		assertThat(externalInterface.isDeleted());
		
		sp2.delete();
		sp1.delete();
		
	}
	
	@Test
	public void valid_copyWithAttributes() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		AMLAttribute attribute = systemUnitClass.createAttribute("att");
		attribute.setValue("val");
		
		Savepoint sp1 = session.createSavepoint();
		AMLSystemUnitClass copy = systemUnitClassLibrary.createSystemUnitClass("copy", systemUnitClass); 
		assertThat(copy.getAttributesCount()).isEqualTo(1);
		assertThat(copy.getAttribute("att")).isNotNull();
		assertThat(copy.getAttribute("att").getValue()).isEqualTo("val");
		
		Savepoint sp2 = session.createSavepoint();		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		copy = systemUnitClassLibrary.getSystemUnitClass("copy"); 
		assertThat(copy.getAttributesCount()).isEqualTo(1);
		assertThat(copy.getAttribute("att")).isNotNull();
		assertThat(copy.getAttribute("att").getValue()).isEqualTo("val");
		
		sp2.delete();
		sp1.delete();
	}
	
	@Test
	public void valid_canceledSavepoint() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary sucl = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass suc1 = sucl.createSystemUnitClass("suc1");
		AMLSystemUnitClass suc2 = suc1.createSystemUnitClass("suc2");
		
		Savepoint sp1 = session.createSavepoint();
		suc2.setBaseSystemUnitClass(suc1);
		
		{
			Savepoint sp2 = session.createSavepoint();
			suc2.createSystemUnitClass("suc3", suc1);
			sp2.cancel();		
		}
		
		Savepoint sp2 = session.createSavepoint();
		suc2.createSystemUnitClass("suc3", suc1);
		sp2.restore();
		
		sp1.restore();		
		
		
		sp2.delete();
		sp1.delete();
		
	}
	
	@Test
	public void valid_deepInstance() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary sucl = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass suc1 = sucl.createSystemUnitClass("suc1");
		AMLAttribute attr1 = suc1.createAttribute("attr1");
		AMLSystemUnitClass suc2 = suc1.createSystemUnitClass("suc2");
		suc2.setBaseSystemUnitClass(suc1);
		AMLAttribute attr2 = suc2.createAttribute("attr2");
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierachy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement(suc2);
		
		assertThat(internalElement.getAttributesCount()).isEqualTo(2);
		Iterator<AMLAttribute> iter = internalElement.getAttributes().iterator();
		assertThat(iter.next().getName()).isEqualTo("attr2");
		assertThat(iter.next().getName()).isEqualTo("attr1");
		
	}
//	@Test 
//	public void valid_DeleteSucPosition() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass1 = systemUnitClassLibrary.createSystemUnitClass("suc1");
//		AMLSystemUnitClass systemUnitClass2 = systemUnitClassLibrary.createSystemUnitClass("suc2");
//		
//		Savepoint sp1 = session.createSavepoint();
//		systemUnitClass1.deepDelete();
//		
//		Savepoint sp2 = session.createSavepoint();		
//		sp1.restore();
//		Iterator<AMLSystemUnitClass> it = systemUnitClassLibrary.getSystemUnitClasses().iterator();
//		assertThat(it.next().getName()).isEqualTo("suc1");
//		assertThat(it.next().getName()).isEqualTo("suc2");
//		
//		sp2.restore();
//		
//		sp2.delete();
//		sp1.delete();
//		
//	}
}
