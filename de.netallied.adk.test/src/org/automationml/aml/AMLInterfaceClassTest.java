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
import static org.fest.assertions.Fail.fail;

public class AMLInterfaceClassTest extends AbstractAMLTest {

	@Test
	public void invalid_deleteInterfaceClass_referenced() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass baseclass = lib1.createInterfaceClass("baseclass");
		AMLInterfaceClass class1 = lib1.createInterfaceClass("class1");
		class1.setBaseInterfaceClass(baseclass);
	
		try {
			baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		assertThat(lib1.getInterfaceClass("baseclass")).isSameAs(baseclass);
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseInterfaceClass_cycle_oneDocument() throws Exception {
		AMLDocument doc = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib = doc.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass1 = lib.createInterfaceClass("class1");
		AMLInterfaceClass interfaceClass2 = lib.createInterfaceClass("class2");
		AMLInterfaceClass interfaceClass3 = lib.createInterfaceClass("class3");
	
		interfaceClass1.setBaseInterfaceClass(interfaceClass2);
		interfaceClass2.setBaseInterfaceClass(interfaceClass3);
		interfaceClass3.setBaseInterfaceClass(interfaceClass1);
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseInterfaceClass_cycle_twoDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass1 = (AMLInterfaceClass) lib.createInterfaceClass("class1");
		AMLInterfaceClass interfaceClass2 = (AMLInterfaceClass) lib.createInterfaceClass("class2");
	
		AMLInterfaceClassLibrary lib2 = b.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass3 = (AMLInterfaceClass) lib2.createInterfaceClass("class3");
		AMLInterfaceClass interfaceClass4 = (AMLInterfaceClass) lib2.createInterfaceClass("class4");
	
		interfaceClass1.setBaseInterfaceClass(interfaceClass2);
		interfaceClass2.setBaseInterfaceClass(interfaceClass3);
		interfaceClass3.setBaseInterfaceClass(interfaceClass4);
		interfaceClass4.setBaseInterfaceClass(interfaceClass1);
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseInterfaceClass_onlyOneDocument() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass baseClass = lib1.createInterfaceClass("baseclass");
		AMLInterfaceClass interfaceClass1 = lib1.createInterfaceClass("class");
		AMLInterfaceClass interfaceClass2 = lib1.createInterfaceClass("class2");
	
		AMLInterfaceClassLibrary lib2 = a.createInterfaceClassLibrary("lib2");
		lib2.createInterfaceClass("class");
		lib2.createInterfaceClass("class2");
	
		interfaceClass1.setBaseInterfaceClass(baseClass);
		interfaceClass2.setBaseInterfaceClass(baseClass);
	
		baseClass.delete();
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseInterfaceClass_otherSession() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSession session2 = amlSessionManager.createSession();
		AMLDocument b = session2.createAMLDocument();
	
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass1 = lib.createInterfaceClass("class1");
	
		AMLInterfaceClassLibrary lib2 = b.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass2 = lib2.createInterfaceClass("class3");
	
		interfaceClass1.setBaseInterfaceClass(interfaceClass2);
	}
	
	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseInterfaceClass2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		a.addExplicitExternalReference(b);
	
		AMLInterfaceClassLibrary lib1_a = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass1_a = lib1_a.createInterfaceClass("class");
		AMLInterfaceClass interfaceClass2_a = lib1_a.createInterfaceClass("class2");
	
		AMLInterfaceClassLibrary lib2_b = b.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass2_b = lib2_b.createInterfaceClass("class");
	
		interfaceClass1_a.setBaseInterfaceClass(interfaceClass2_b);
		interfaceClass2_a.setBaseInterfaceClass(interfaceClass2_b); // <---- interfaceClass2_a != interfaceClass1_a
		interfaceClass1_a.unsetBaseInterfaceClass();
	
		a.removeExplicitExternalReference(b);
		b.createInterfaceClassLibrary("lib1");
	}


	@Test(expected = AMLValidationException.class)
	public void invalid_setName() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass = lib.createInterfaceClass("class");
		lib.createInterfaceClass("class1");
	
		interfaceClass.setName("class1");
	}

	@Test
	public void valid_addImplicitExternalReference() throws Exception {
		AMLDocument doc1 = session.createAMLDocument();
		AMLDocument doc2 = session.createAMLDocument();
		doc1.addExplicitExternalReference(doc2);
	
		AMLInterfaceClassLibrary lib1_1 = doc1.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass1 = lib1_1.createInterfaceClass("class");
	
		AMLInterfaceClassLibrary lib2_2 = doc2.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass2 = lib2_2.createInterfaceClass("class");
	
		interfaceClass1.setBaseInterfaceClass(interfaceClass2);
	}

	@Test
	public void valid_deleteInterfaceClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass baseclass = lib1.createInterfaceClass("baseclass");
	
		baseclass.delete();
	
		assertThat(lib1.getInterfaceClass("baseclass")).isNull();
	}

	@Test
	public void valid_deleteInterfaceClass_complex() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass baseclass1 = lib1.createInterfaceClass("baseclass1");
		AMLInterfaceClass baseclass2 = lib1.createInterfaceClass("baseclass2");
		AMLInterfaceClass class1 = lib1.createInterfaceClass("class1");
		AMLInterfaceClass class2 = lib1.createInterfaceClass("class2");
		class1.setBaseInterfaceClass(baseclass1);
		class2.setBaseInterfaceClass(baseclass2);
	
		class1.setBaseInterfaceClass(baseclass2);
		class2.setBaseInterfaceClass(baseclass1);
	
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
	
		class2.unsetBaseInterfaceClass();
	
		baseclass1.delete();
	
		try {
			baseclass2.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		class1.unsetBaseInterfaceClass();
	
		baseclass2.delete();
	
		assertThat(lib1.getInterfaceClass("baseclass")).isNull();
	}

	@Test
	public void valid_deleteInterfaceClass_notReferencedAnyMore() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass baseclass = lib1.createInterfaceClass("baseclass");
		AMLInterfaceClass class1 = lib1.createInterfaceClass("class1");
		AMLInterfaceClass class2 = lib1.createInterfaceClass("class2");
		class1.setBaseInterfaceClass(baseclass);
		class2.setBaseInterfaceClass(baseclass);
	
		try {
			baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		class1.unsetBaseInterfaceClass();
	
		try {
			baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		class2.unsetBaseInterfaceClass();
	
		baseclass.delete();
	
		assertThat(lib1.getInterfaceClass("baseclass")).isNull();
	}

	@Test
	public void valid_setBaseClass_savepoints() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass = lib.createInterfaceClass("class");
		AMLInterfaceClass baseClass1 = lib.createInterfaceClass("baseClass1");
		AMLInterfaceClass baseClass2 = lib.createInterfaceClass("baseClass2");
	
		Savepoint sp1 = session.createSavepoint();
		interfaceClass.setBaseInterfaceClass(baseClass1);
		Savepoint sp2 = session.createSavepoint();
		interfaceClass.setBaseInterfaceClass(baseClass2);
		Savepoint sp3 = session.createSavepoint();
	
		sp1.restore();
		assertThat(interfaceClass.getBaseInterfaceClass()).isNull();
		
		

		sp2.restore();
		assertThat(interfaceClass.getBaseInterfaceClass()).isSameAs(baseClass1);
	
		sp3.restore();
		assertThat(interfaceClass.getBaseInterfaceClass()).isSameAs(baseClass2);
	
		sp2.restore();
		assertThat(interfaceClass.getBaseInterfaceClass()).isSameAs(baseClass1);
	
		sp1.delete();
		sp2.delete();
		sp3.delete();
	}

	@Test
	public void valid_setBaseClass_savepoints_removeImplicitReference() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib1= a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClassLibrary lib2 = a.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass = lib1.createInterfaceClass("class");
		AMLInterfaceClass baseClass = lib2.createInterfaceClass("baseClass1");
	
		Savepoint sp1 = session.createSavepoint();
		interfaceClass.setBaseInterfaceClass(baseClass);
		Savepoint sp2 = session.createSavepoint();
	
		sp1.restore();
		assertThat(interfaceClass.getBaseInterfaceClass()).isNull();
		
		baseClass.delete();
		
		sp1.delete();
		sp2.delete();
		
	}
	@Test
	public void valid_setBaseInterfaceClass_calledTwice() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		a.addExplicitExternalReference(b);
	
		AMLInterfaceClassLibrary lib1_a = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass1 = lib1_a.createInterfaceClass("class");
	
		AMLInterfaceClassLibrary lib2_b = b.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass2 = lib2_b.createInterfaceClass("class");
	
		interfaceClass1.setBaseInterfaceClass(interfaceClass2);
		interfaceClass1.setBaseInterfaceClass(interfaceClass2);
		interfaceClass1.unsetBaseInterfaceClass();
	
		a.removeExplicitExternalReference(b);
		b.createInterfaceClassLibrary("lib1");
	}

	@Test
	public void valid_setBaseInterfaceClass_onlyOneDocument() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass baseClass = lib1.createInterfaceClass("baseclass");
		AMLInterfaceClass interfaceClass1 = lib1.createInterfaceClass("class");
		AMLInterfaceClass interfaceClass2 = lib1.createInterfaceClass("class2");
	
		interfaceClass1.setBaseInterfaceClass(baseClass);
		interfaceClass2.setBaseInterfaceClass(baseClass);
		interfaceClass1.unsetBaseInterfaceClass();
	}
	
	@Test (expected = AMLValidationException.class)
	public void invalid_setBaseInterfaceClass_twoDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass baseClass = lib1.createInterfaceClass("baseclass");
	
		AMLInterfaceClassLibrary lib2 = b.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass1 = lib2.createInterfaceClass("class");
	
		interfaceClass1.setBaseInterfaceClass(baseClass);
	}

	@Test
	public void valid_setBaseInterfaceClass_twoDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		b.addExplicitExternalReference(a);
	
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass baseClass = lib1.createInterfaceClass("baseclass");
	
		AMLInterfaceClassLibrary lib2 = b.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass1 = lib2.createInterfaceClass("class");
		AMLInterfaceClass interfaceClass2 = lib2.createInterfaceClass("class2");
	
		interfaceClass1.setBaseInterfaceClass(baseClass);
		interfaceClass2.setBaseInterfaceClass(baseClass);
	
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
	
		interfaceClass1.unsetBaseInterfaceClass();
		try {
			baseClass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		interfaceClass2.unsetBaseInterfaceClass();
	
		baseClass.delete();
	}

	@Test
	public void valid_setName_savepoints() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = lib.createInterfaceClass("class");
	
		Savepoint sp1 = session.createSavepoint();

		attachDocumentChangeListener(a);
		interfaceClass.setName("xxx");
		assertThat(documentChangeListener.result.toString()).contains("begin\nModified AMLInterfaceClassImpl\nend\n");
		
		resetChangeListener();
		interfaceClass.setName("yyy");
		Savepoint sp2 = session.createSavepoint();
		assertThat(documentChangeListener.result.toString()).contains("begin\nModified AMLInterfaceClassImpl\nend\n");

		sp1.restore();
		assertThat(interfaceClass.getName()).isEqualTo("class");
	
		sp2.restore();
		assertThat(interfaceClass.getName()).isEqualTo("yyy");
	
		sp1.delete();
		sp2.delete();
		
//		System.out.println(changeListener);

	}
	
	@Test
	public void valid_reparentOverDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClassLibrary interfaceClassLibrary2 = (AMLInterfaceClassLibrary) b.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary2.createInterfaceClass("ic");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(interfaceClass.validateReparent(interfaceClassLibrary2, interfaceClassLibrary, null, null).isOk() == true);
		interfaceClass.reparent(interfaceClassLibrary2, interfaceClassLibrary,  null, null);
		
		Savepoint sp2 = session.createSavepoint();

		assertThat(interfaceClassLibrary.getInterfaceClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getInterfaceClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getInterfaceClasses().iterator().hasNext() == false);
		
		sp1.restore();
		assertThat(interfaceClassLibrary2.getInterfaceClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary2.getInterfaceClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary.getInterfaceClasses().iterator().hasNext() == false);
		
		sp2.restore();
		assertThat(interfaceClassLibrary.getInterfaceClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getInterfaceClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getInterfaceClasses().iterator().hasNext() == false);
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentOverDocuments2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		b.addExplicitExternalReference(a);
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClassLibrary interfaceClassLibrary2 = (AMLInterfaceClassLibrary) b.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary2.createInterfaceClass("ic");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(interfaceClass.validateReparent(interfaceClassLibrary2, interfaceClassLibrary, null, null).isOk() == true);
		interfaceClass.reparent(interfaceClassLibrary2, interfaceClassLibrary,  null, null);
		
		Savepoint sp2 = session.createSavepoint();

		assertThat(interfaceClassLibrary.getInterfaceClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getInterfaceClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getInterfaceClasses().iterator().hasNext() == false);
		
		sp1.restore();
		assertThat(interfaceClassLibrary2.getInterfaceClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary2.getInterfaceClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary.getInterfaceClasses().iterator().hasNext() == false);
		
		sp2.restore();
		assertThat(interfaceClassLibrary.getInterfaceClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getInterfaceClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getInterfaceClasses().iterator().hasNext() == false);
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentAfter() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic1");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary.createInterfaceClass("ic2");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(interfaceClass.validateReparent(interfaceClassLibrary, interfaceClassLibrary, null, interfaceClass2).isOk() == true);
		interfaceClass.reparent(interfaceClassLibrary, interfaceClassLibrary,  null, interfaceClass2);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLInterfaceClass> iterator = interfaceClassLibrary.getInterfaceClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClass2));
		assertThat(iterator.next().equals(interfaceClass));
		
		sp1.restore();
		iterator = interfaceClassLibrary.getInterfaceClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClass));
		assertThat(iterator.next().equals(interfaceClass2));
		
		sp2.restore();
		iterator = interfaceClassLibrary.getInterfaceClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClass2));
		assertThat(iterator.next().equals(interfaceClass));
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentBefore() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary instanceHierarchy = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass internalElement2 = instanceHierarchy.createInterfaceClass("ic1");
		AMLInterfaceClass internalElement = instanceHierarchy.createInterfaceClass("ic2");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(internalElement.validateReparent(instanceHierarchy, instanceHierarchy, internalElement2, null).isOk() == true);
		internalElement.reparent(instanceHierarchy, instanceHierarchy, internalElement2,  null);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLInterfaceClass> iterator = instanceHierarchy.getInterfaceClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement2));
		assertThat(iterator.next().equals(internalElement));
		
		sp1.restore();
		iterator = instanceHierarchy.getInterfaceClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement));
		assertThat(iterator.next().equals(internalElement2));
		
		sp2.restore();
		iterator = instanceHierarchy.getInterfaceClasses().iterator();
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
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary.createInterfaceClass("ic2");
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
		AMLInterfaceClassLibrary interfaceClassLibrary2 = (AMLInterfaceClassLibrary) b.createInterfaceClassLibrary("icl2");
		
		assertThat(interfaceClass2.validateReparent(interfaceClass, interfaceClassLibrary2, null, null).isAnyOperationNotPermitted());
		try {
			interfaceClass2.reparent(interfaceClass, interfaceClassLibrary2, null, null);
			Fail.fail();
		} catch (Exception ex) {
		}
		
	}
	
	@Test
	public void invalid_reparentInterfaceClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLInterfaceClassLibrary interfaceClassLibrary = b.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		
		AMLInterfaceClassLibrary interfaceClassLibrary2 = b.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary2.createInterfaceClass("ic2");
		interfaceClass.setBaseInterfaceClass(interfaceClass2);
		
		assertThat(interfaceClassLibrary2.validateReparent(b, a, null, null).isAnyOperationNotPermitted()).isTrue();
	}
	
	@Test
	public void invalid_reparentParentToChildSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();		
		
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass parent = interfaceClassLibrary.createInterfaceClass("ic");
		AMLInterfaceClass child = parent.createInterfaceClass("ic2");
		
		assertThat(parent.validateReparent(interfaceClassLibrary, child, null, null).isAnyOperationNotPermitted()).isTrue();
		
	}
	
	
	@Test
	public void valid_reparentInterfaceClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		
		AMLInterfaceClassLibrary interfaceClassLibrary2 = a.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary2.createInterfaceClass("ic2");
		interfaceClass.setBaseInterfaceClass(interfaceClass2);
		
		assertThat(interfaceClassLibrary2.validateReparent(a, b, null, null).isOk()).isTrue();
	}
	
	@Test
	public void valid_unlink() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInterfaceClassLibrary interfaceClassLibrary2 = a.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary2.createInterfaceClass("ic2");	
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
		
		Savepoint sp1 = session.createSavepoint();
		interfaceClass2.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isNull();
		
		sp1.restore();		
		assertThat(interfaceClass2.getBaseInterfaceClass()).isEqualTo(interfaceClass);
		
		sp2.restore();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isNull();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlink2() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInterfaceClassLibrary interfaceClassLibrary2 = a.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary2.createInterfaceClass("ic2");	
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
		
		Savepoint sp1 = session.createSavepoint();
		interfaceClass.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isNull();
		
		sp1.restore();		
		assertThat(interfaceClass2.getBaseInterfaceClass()).isEqualTo(interfaceClass);
		
		sp2.restore();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isNull();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlinkConcurrent() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		AMLInterfaceClass interfaceClass3 = interfaceClass.createInterfaceClass("ic3");		
		AMLInterfaceClassLibrary interfaceClassLibrary2 = a.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary2.createInterfaceClass("ic2");	
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
		interfaceClass3.setBaseInterfaceClass(interfaceClass);
		
		Savepoint sp1 = session.createSavepoint();
		interfaceClass.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isNull();
		
		sp1.restore();		
		assertThat(interfaceClass2.getBaseInterfaceClass()).isEqualTo(interfaceClass);
		
		sp2.restore();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isNull();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlink3() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInterfaceClassLibrary interfaceClassLibrary2 = a.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary2.createInterfaceClass("ic2");	
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
		
		Savepoint sp1 = session.createSavepoint();
		a.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isEqualTo(interfaceClass);
		
		sp1.restore();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isEqualTo(interfaceClass);
		
		sp2.restore();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isEqualTo(interfaceClass);
		
		sp1.delete();
		sp2.delete();			
	}
	
	@Test
	public void valid_unlink4() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);
		AMLInternalLink link = parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);
				
		Savepoint sp1 = session.createSavepoint();
		interfaceClass.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(link.isDeleted()).isTrue();
		
		sp1.restore();
		assertThat(parentInternalElement.getInternalLinks().iterator().hasNext()).isTrue();
		
		sp2.restore();
		assertThat(parentInternalElement.getInternalLinks().iterator().hasNext()).isFalse();
		
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
		AMLInterfaceClass interfaceClass2 = interfaceClass.createInterfaceClass("ic2");
		
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass2);
		
		assertThat(interfaceClass.validateDeepDelete().isAnyOperationNotPermitted()).isTrue();
		try {
			interfaceClass.deepDelete();
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
		AMLInterfaceClass interfaceClass2 = interfaceClass.createInterfaceClass("ic2");
		
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass2);
		
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
	public void valid_deepDelete3() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		AMLInterfaceClass interfaceClass2 = interfaceClass.createInterfaceClass("ic2");
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
		
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass2);
		
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
		AMLInterfaceClass interfaceClass2 = interfaceClass.createInterfaceClass("ic2");
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(interfaceClassLibrary.validateCreateInterfaceClass("ic2").isOk()).isTrue();
		AMLInterfaceClass copy = interfaceClassLibrary.createInterfaceClass("ic2", interfaceClass);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(interfaceClassLibrary.getInterfaceClassesCount()).isEqualTo(2);
		
		sp1.restore();
		assertThat(interfaceClassLibrary.getInterfaceClassesCount()).isEqualTo(1);
		
		sp2.restore();
		assertThat(interfaceClassLibrary.getInterfaceClassesCount()).isEqualTo(2);
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void valid_deepCopy2() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		AMLInterfaceClass interfaceClass2 = interfaceClass.createInterfaceClass("ic2");
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(interfaceClassLibrary.validateCreateInterfaceClass("ic2").isOk()).isTrue();
		AMLInterfaceClass copy = interfaceClassLibrary.createInterfaceClass("ic2", interfaceClass);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(copy.getInterfaceClasses().iterator().next().getBaseInterfaceClass()).isEqualTo(copy);
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLInterfaceClass> iterator = interfaceClassLibrary.getInterfaceClasses().iterator();
		iterator.next();
		copy = iterator.next(); 
		assertThat(copy.getInterfaceClasses().iterator().next().getBaseInterfaceClass()).isEqualTo(copy);
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void valid_deepCopy3() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		AMLInterfaceClass interfaceClass2 = interfaceClass.createInterfaceClass("ic2");
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(interfaceClass.validateCreateInterfaceClass("ic3").isOk()).isTrue();
		AMLInterfaceClass copy = interfaceClass.createInterfaceClass("ic3", interfaceClass2);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(copy.getBaseInterfaceClass()).isEqualTo(interfaceClass);
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLInterfaceClass> iterator = interfaceClass.getInterfaceClasses().iterator();
		iterator.next();
		assertThat(iterator.next().getBaseInterfaceClass()).isEqualTo(interfaceClass);
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void valid_deepCopy_AvoidStackOverflow() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		AMLInterfaceClass interfaceClass2 = interfaceClass.createInterfaceClass("ic2");
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(interfaceClass.validateCreateInterfaceClass("ic3").isOk()).isTrue();
		AMLInterfaceClass copy = interfaceClass.createInterfaceClass("ic3", interfaceClass);
		
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
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		AMLInterfaceClass interfaceClass2 = interfaceClass.createInterfaceClass("ic2");
	
		assertThat(interfaceClassLibrary.validateCreateInterfaceClass("ic").isAnyOperationNotPermitted()).isTrue();
		try {
			interfaceClassLibrary.createInterfaceClass("ic", interfaceClass);
			Fail.fail();
		} catch (AMLValidationException ex){
			
		}		
	}
	
	@Test
	public void valid_copyInterfaceWithAttributeInSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		AMLAttribute attribute = interfaceClass.createAttribute("A");
		attribute.setValue("B");
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		
		Savepoint sp1 = session.createSavepoint();
		
		AMLExternalInterface externalInterface = systemUnitClass.createExternalInterface(interfaceClass);
		UUID uuid = externalInterface.getId();
		assertThat(externalInterface.getAttribute("A")).isNotNull();
		assertThat(externalInterface.getAttribute("A").getValue()).isEqualTo("B");
		
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(externalInterface.isDeleted());
		
		sp2.restore();
		externalInterface = systemUnitClass.getExternalInterface(uuid);
		assertThat(externalInterface).isNotNull();
		assertThat(externalInterface.getAttribute("A")).isNotNull();
		assertThat(externalInterface.getAttribute("A").getValue()).isEqualTo("B");
		
		sp2.restore();
		
		sp2.delete();
		sp1.delete();
	}
}
