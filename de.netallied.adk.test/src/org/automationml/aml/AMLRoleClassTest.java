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

public class AMLRoleClassTest extends AbstractAMLTest {
	
	private TestFileLocator testFileLocator = new TestFileLocator(getClass());


	@Test
	public void invalid_deleteRoleClass_referenced() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLRoleClassLibrary lib1 = a.createRoleClassLibrary("lib1");
		AMLRoleClass baseclass = lib1.createRoleClass("baseclass");
		AMLRoleClass class1 = lib1.createRoleClass("class1");
		class1.setBaseRoleClass(baseclass);
	
		try {
			baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		assertThat(lib1.getRoleClass("baseclass")).isSameAs(baseclass);
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseRoleClass_cycle_oneDocument() throws Exception {
		AMLDocument doc = session.createAMLDocument();
	
		AMLRoleClassLibrary lib = doc.createRoleClassLibrary("lib1");
		AMLRoleClass class1 = lib.createRoleClass("class1");
		AMLRoleClass class2 = lib.createRoleClass("class2");
		AMLRoleClass class3 = lib.createRoleClass("class3");
	
		class1.setBaseRoleClass(class2);
		class2.setBaseRoleClass(class3);
		class3.setBaseRoleClass(class1);
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseRoleClass_cycle_twoDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		AMLRoleClassLibrary lib = a.createRoleClassLibrary("lib1");
		AMLRoleClass class1 = lib.createRoleClass("class1");
		AMLRoleClass class2 = lib.createRoleClass("class2");
	
		AMLRoleClassLibrary lib2 = b.createRoleClassLibrary("lib2");
		AMLRoleClass class3 = lib2.createRoleClass("class3");
		AMLRoleClass class4 = lib2.createRoleClass("class4");
	
		class1.setBaseRoleClass(class2);
		class2.setBaseRoleClass(class3);
		class3.setBaseRoleClass(class4);
		class4.setBaseRoleClass(class1);
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseRoleClass_onlyOneDocument() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLRoleClassLibrary lib1 = a.createRoleClassLibrary("lib1");
		AMLRoleClass baseClass = lib1.createRoleClass("baseclass");
		AMLRoleClass class1 = lib1.createRoleClass("class");
		AMLRoleClass class2 = lib1.createRoleClass("class2");
	
		AMLRoleClassLibrary lib2 = a.createRoleClassLibrary("lib2");
		lib2.createRoleClass("class");
		lib2.createRoleClass("class2");
	
		class1.setBaseRoleClass(baseClass);
		class2.setBaseRoleClass(baseClass);
	
		baseClass.delete();
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseRoleClass_otherSession() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSession session2 = amlSessionManager.createSession();
		AMLDocument b = session2.createAMLDocument();
	
		AMLRoleClassLibrary lib = a.createRoleClassLibrary("lib1");
		AMLRoleClass class1 = lib.createRoleClass("class1");
	
		AMLRoleClassLibrary lib2 = b.createRoleClassLibrary("lib2");
		AMLRoleClass class2 = lib2.createRoleClass("class3");
	
		class1.setBaseRoleClass(class2);
	}
	
	@Test(expected = AMLValidationException.class)
	public void invalid_setBaseRoleClass2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		a.addExplicitExternalReference(b);
	
		AMLRoleClassLibrary lib1_a = a.createRoleClassLibrary("lib1");
		AMLRoleClass class1_a = lib1_a.createRoleClass("class");
		AMLRoleClass class2_a = lib1_a.createRoleClass("class2");
	
		AMLRoleClassLibrary lib2_b = b.createRoleClassLibrary("lib2");
		AMLRoleClass class2_b = lib2_b.createRoleClass("class");
	
		class1_a.setBaseRoleClass(class2_b);
		class2_a.setBaseRoleClass(class2_b); // <---- class2_a != class1_a
		class1_a.unsetBaseRoleClass();
	
		a.removeExplicitExternalReference(b);
		b.createRoleClassLibrary("lib1");
	}


	@Test(expected = AMLValidationException.class)
	public void invalid_setName() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLRoleClassLibrary lib = a.createRoleClassLibrary("lib1");
		AMLRoleClass roleClass = lib.createRoleClass("class");
		lib.createRoleClass("class1");
	
		roleClass.setName("class1");
	}

	@Test
	public void valid_addImplicitExternalReference() throws Exception {
		AMLDocument doc1 = session.createAMLDocument();
		AMLDocument doc2 = session.createAMLDocument();
		
		doc1.addExplicitExternalReference(doc2);
	
		AMLRoleClassLibrary lib1_1 = doc1.createRoleClassLibrary("lib1");
		AMLRoleClass class1 = lib1_1.createRoleClass("class");
	
		AMLRoleClassLibrary lib2_2 = doc2.createRoleClassLibrary("lib2");
		AMLRoleClass class2 = lib2_2.createRoleClass("class");
	
		class1.setBaseRoleClass(class2);
	}

	@Test
	public void valid_deleteRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLRoleClassLibrary lib1 = a.createRoleClassLibrary("lib1");
			
		AMLRoleClass baseclass = lib1.createRoleClass("baseclass");
		assertThat(lib1.getRoleClass("baseclass")).isNotNull();
		
		Savepoint sp1 = session.createSavepoint();
		baseclass.delete();
		assertThat(lib1.getRoleClass("baseclass")).isNull();
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(lib1.getRoleClass("baseclass")).isNotNull();
		
		sp2.restore();
		assertThat(lib1.getRoleClass("baseclass")).isNull();
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_deleteRoleClassWithinARoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLRoleClassLibrary lib1 = a.createRoleClassLibrary("lib1");
			
		AMLRoleClass baseclass = lib1.createRoleClass("baseclass");
		assertThat(lib1.getRoleClass("baseclass")).isNotNull();
		AMLRoleClass childClass = baseclass.createRoleClass("childClass");
		assertThat(baseclass.getRoleClass("childClass")).isNotNull();
		
		Savepoint sp1 = session.createSavepoint();
		childClass.delete();
		assertThat(baseclass.getRoleClass("childClass")).isNull();
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(baseclass.getRoleClass("childClass")).isNotNull();
		
		sp2.restore();
		assertThat(baseclass.getRoleClass("childClass")).isNull();
		
		sp1.delete();
		sp2.delete();
	}


	@Test
	public void valid_deleteRoleClass_complex() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLRoleClassLibrary lib1 = a.createRoleClassLibrary("lib1");
		AMLRoleClass baseclass1 = lib1.createRoleClass("baseclass1");
		AMLRoleClass baseclass2 = lib1.createRoleClass("baseclass2");
		AMLRoleClass class1 = lib1.createRoleClass("class1");
		AMLRoleClass class2 = lib1.createRoleClass("class2");
		class1.setBaseRoleClass(baseclass1);
		class2.setBaseRoleClass(baseclass2);
	
		class1.setBaseRoleClass(baseclass2);
		class2.setBaseRoleClass(baseclass1);
	
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
	
		class2.unsetBaseRoleClass();
	
		baseclass1.delete();
	
		try {
			baseclass2.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		class1.unsetBaseRoleClass();
	
		baseclass2.delete();
	
		assertThat(lib1.getRoleClass("baseclass")).isNull();
	}

	@Test
	public void valid_deleteRoleClass_notReferencedAnyMore() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLRoleClassLibrary lib1 = a.createRoleClassLibrary("lib1");
		AMLRoleClass baseclass = lib1.createRoleClass("baseclass");
		AMLRoleClass class1 = lib1.createRoleClass("class1");
		AMLRoleClass class2 = lib1.createRoleClass("class2");
		class1.setBaseRoleClass(baseclass);
		class2.setBaseRoleClass(baseclass);
	
		try {
			baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		class1.unsetBaseRoleClass();
	
		try {
			baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		class2.unsetBaseRoleClass();
	
		baseclass.delete();
	
		assertThat(lib1.getRoleClass("baseclass")).isNull();
	}

	@Test
	public void valid_setBaseClass_savepoints() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLRoleClassLibrary lib = a.createRoleClassLibrary("lib1");
		AMLRoleClass roleClass = lib.createRoleClass("class");
		AMLRoleClass baseClass1 = lib.createRoleClass("baseClass1");
		AMLRoleClass baseClass2 = lib.createRoleClass("baseClass2");
	
		Savepoint sp1 = session.createSavepoint();
		roleClass.setBaseRoleClass(baseClass1);
		Savepoint sp2 = session.createSavepoint();
		roleClass.setBaseRoleClass(baseClass2);
		Savepoint sp3 = session.createSavepoint();
	
		sp1.restore();
		assertThat(roleClass.getBaseRoleClass()).isNull();
		
		

		sp2.restore();
		assertThat(roleClass.getBaseRoleClass()).isSameAs(baseClass1);
	
		sp3.restore();
		assertThat(roleClass.getBaseRoleClass()).isSameAs(baseClass2);
	
		sp2.restore();
		assertThat(roleClass.getBaseRoleClass()).isSameAs(baseClass1);
	
		sp1.delete();
		sp2.delete();
		sp3.delete();
	}

	@Test
	public void valid_setBaseClass_savepoints_removeImplicitReference() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLRoleClassLibrary lib1= a.createRoleClassLibrary("lib1");
		AMLRoleClassLibrary lib2 = a.createRoleClassLibrary("lib2");
		AMLRoleClass roleClass = lib1.createRoleClass("class");
		AMLRoleClass baseClass = lib2.createRoleClass("baseClass1");
	
		Savepoint sp1 = session.createSavepoint();
		roleClass.setBaseRoleClass(baseClass);
		Savepoint sp2 = session.createSavepoint();
	
		sp1.restore();
		assertThat(roleClass.getBaseRoleClass()).isNull();
		
		baseClass.delete();
		
		sp1.delete();
		sp2.delete();
		
	}
	@Test
	public void valid_setBaseRoleClass_calledTwice() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		a.addExplicitExternalReference(b);
	
		AMLRoleClassLibrary lib1_a = a.createRoleClassLibrary("lib1");
		AMLRoleClass class1 = lib1_a.createRoleClass("class");
	
		AMLRoleClassLibrary lib2_b = b.createRoleClassLibrary("lib2");
		AMLRoleClass class2 = lib2_b.createRoleClass("class");
	
		class1.setBaseRoleClass(class2);
		class1.setBaseRoleClass(class2);
		class1.unsetBaseRoleClass();
	
		a.removeExplicitExternalReference(b);
		b.createRoleClassLibrary("lib1");
	}

	@Test
	public void valid_setBaseRoleClass_onlyOneDocument() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLRoleClassLibrary lib1 = a.createRoleClassLibrary("lib1");
		AMLRoleClass baseClass = lib1.createRoleClass("baseclass");
		AMLRoleClass class1 = lib1.createRoleClass("class");
		AMLRoleClass class2 = lib1.createRoleClass("class2");
	
		class1.setBaseRoleClass(baseClass);
		class2.setBaseRoleClass(baseClass);
		class1.unsetBaseRoleClass();
	}
	
	@Test (expected = AMLValidationException.class)
	public void invalid_setBaseRoleClass_twoDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
	
		AMLRoleClassLibrary lib1 = a.createRoleClassLibrary("lib1");
		AMLRoleClass baseClass = lib1.createRoleClass("baseclass");
	
		AMLRoleClassLibrary lib2 = b.createRoleClassLibrary("lib2");
		AMLRoleClass class1 = lib2.createRoleClass("class");
	
		class1.setBaseRoleClass(baseClass);
	
	}

	@Test
	public void valid_setBaseRoleClass_twoDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		b.addExplicitExternalReference(a);
	
		AMLRoleClassLibrary lib1 = a.createRoleClassLibrary("lib1");
		AMLRoleClass baseClass = lib1.createRoleClass("baseclass");
	
		AMLRoleClassLibrary lib2 = b.createRoleClassLibrary("lib2");
		AMLRoleClass class1 = lib2.createRoleClass("class");
		AMLRoleClass class2 = lib2.createRoleClass("class2");
	
		class1.setBaseRoleClass(baseClass);
		class2.setBaseRoleClass(baseClass);
	
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
	
		class1.unsetBaseRoleClass();
		try {
			baseClass.delete();
			fail();
		} catch (AMLValidationException e) {
		}
	
		class2.unsetBaseRoleClass();
	
		baseClass.delete();
	}

	@Test
	public void valid_setName_savepoints() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLRoleClassLibrary lib = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = lib.createRoleClass("class");
	
		Savepoint sp1 = session.createSavepoint();

		attachDocumentChangeListener(a);
		roleClass.setName("xxx");
		assertThat(documentChangeListener.result.toString()).contains("begin\nModified AMLRoleClassImpl\nend\n");
		
		resetChangeListener();
		roleClass.setName("yyy");
		Savepoint sp2 = session.createSavepoint();
		assertThat(documentChangeListener.result.toString()).contains("begin\nModified AMLRoleClassImpl\nend\n");

		sp1.restore();
		assertThat(roleClass.getName()).isEqualTo("class");
	
		sp2.restore();
		assertThat(roleClass.getName()).isEqualTo("yyy");
	
		sp1.delete();
		sp2.delete();
		
//		System.out.println(changeListener);

	}
	
	@Test
	public void valid_reparentOverDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		AMLRoleClassLibrary interfaceClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClassLibrary interfaceClassLibrary2 = (AMLRoleClassLibrary) b.createRoleClassLibrary("icl2");
		AMLRoleClass interfaceClass = interfaceClassLibrary2.createRoleClass("ic");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(interfaceClass.validateReparent(interfaceClassLibrary2, interfaceClassLibrary, null, null).isOk());
		interfaceClass.reparent(interfaceClassLibrary2, interfaceClassLibrary,  null, null);
		
		Savepoint sp2 = session.createSavepoint();

		assertThat(interfaceClassLibrary.getRoleClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getRoleClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getRoleClasses().iterator().hasNext() == false);
		
		sp1.restore();
		assertThat(interfaceClassLibrary2.getRoleClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary2.getRoleClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary.getRoleClasses().iterator().hasNext() == false);
		
		sp2.restore();
		assertThat(interfaceClassLibrary.getRoleClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getRoleClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getRoleClasses().iterator().hasNext() == false);
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentOverDocuments2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		b.addExplicitExternalReference(a);
		AMLRoleClassLibrary interfaceClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClassLibrary interfaceClassLibrary2 = (AMLRoleClassLibrary) b.createRoleClassLibrary("icl2");
		AMLRoleClass interfaceClass = interfaceClassLibrary2.createRoleClass("ic");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(interfaceClass.validateReparent(interfaceClassLibrary2, interfaceClassLibrary, null, null).isOk() == true);
		interfaceClass.reparent(interfaceClassLibrary2, interfaceClassLibrary,  null, null);
		
		Savepoint sp2 = session.createSavepoint();

		assertThat(interfaceClassLibrary.getRoleClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getRoleClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getRoleClasses().iterator().hasNext() == false);
		
		sp1.restore();
		assertThat(interfaceClassLibrary2.getRoleClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary2.getRoleClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary.getRoleClasses().iterator().hasNext() == false);
		
		sp2.restore();
		assertThat(interfaceClassLibrary.getRoleClasses().iterator().hasNext());
		assertThat(interfaceClassLibrary.getRoleClasses().iterator().next().equals(interfaceClass));
		assertThat(interfaceClassLibrary2.getRoleClasses().iterator().hasNext() == false);
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentAfter() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLRoleClassLibrary interfaceClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass interfaceClass = interfaceClassLibrary.createRoleClass("ic1");
		AMLRoleClass interfaceClass2 = interfaceClassLibrary.createRoleClass("ic2");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(interfaceClass.validateReparent(interfaceClassLibrary, interfaceClassLibrary, null, interfaceClass2).isOk() == true);
		interfaceClass.reparent(interfaceClassLibrary, interfaceClassLibrary,  null, interfaceClass2);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLRoleClass> iterator = interfaceClassLibrary.getRoleClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClass2));
		assertThat(iterator.next().equals(interfaceClass));
		
		sp1.restore();
		iterator = interfaceClassLibrary.getRoleClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClass));
		assertThat(iterator.next().equals(interfaceClass2));
		
		sp2.restore();
		iterator = interfaceClassLibrary.getRoleClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(interfaceClass2));
		assertThat(iterator.next().equals(interfaceClass));
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentBefore() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLRoleClassLibrary instanceHierarchy = a.createRoleClassLibrary("icl");
		AMLRoleClass internalElement2 = instanceHierarchy.createRoleClass("ic1");
		AMLRoleClass internalElement = instanceHierarchy.createRoleClass("ic2");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(internalElement.validateReparent(instanceHierarchy, instanceHierarchy, internalElement2, null).isOk() == true);
		internalElement.reparent(instanceHierarchy, instanceHierarchy, internalElement2,  null);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLRoleClass> iterator = instanceHierarchy.getRoleClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement2));
		assertThat(iterator.next().equals(internalElement));
		
		sp1.restore();
		iterator = instanceHierarchy.getRoleClasses().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement));
		assertThat(iterator.next().equals(internalElement2));
		
		sp2.restore();
		iterator = instanceHierarchy.getRoleClasses().iterator();
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
		AMLRoleClassLibrary interfaceClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass interfaceClass = interfaceClassLibrary.createRoleClass("ic");
		AMLRoleClass interfaceClass2 = interfaceClassLibrary.createRoleClass("ic2");
		interfaceClass2.setBaseRoleClass(interfaceClass);
		AMLRoleClassLibrary interfaceClassLibrary2 = (AMLRoleClassLibrary) b.createRoleClassLibrary("icl2");
		
		assertThat(interfaceClass2.validateReparent(interfaceClass, interfaceClassLibrary2, null, null).isAnyOperationNotPermitted());
		try {
			interfaceClass2.reparent(interfaceClass, interfaceClassLibrary2, null, null);
			Fail.fail();
		} catch (Exception ex) {
		}
		
	}
	
	@Test
	public void invalid_reparentRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLRoleClassLibrary interfaceClassLibrary = b.createRoleClassLibrary("icl");
		AMLRoleClass interfaceClass = interfaceClassLibrary.createRoleClass("ic");
		
		AMLRoleClassLibrary interfaceClassLibrary2 = b.createRoleClassLibrary("icl2");
		AMLRoleClass interfaceClass2 = interfaceClassLibrary2.createRoleClass("ic2");
		interfaceClass.setBaseRoleClass(interfaceClass2);
		
		assertThat(interfaceClassLibrary2.validateReparent(b, a, null, null).isAnyOperationNotPermitted()).isTrue();
	}
	
	@Test
	public void invalid_reparentParentToChildSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();		
		
		AMLRoleClassLibrary interfaceClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass parent = interfaceClassLibrary.createRoleClass("ic");
		AMLRoleClass child = parent.createRoleClass("ic2");
		
		assertThat(parent.validateReparent(interfaceClassLibrary, child, null, null).isAnyOperationNotPermitted()).isTrue();
		
	}
	
	@Test
	public void valid_reparentRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLRoleClassLibrary interfaceClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass interfaceClass = interfaceClassLibrary.createRoleClass("ic");
		
		AMLRoleClassLibrary interfaceClassLibrary2 = a.createRoleClassLibrary("icl2");
		AMLRoleClass interfaceClass2 = interfaceClassLibrary2.createRoleClass("ic2");
		interfaceClass.setBaseRoleClass(interfaceClass2);
		
		assertThat(interfaceClassLibrary2.validateReparent(a, b, null, null).isOk()).isTrue();
	}
	
	@Test
	public void valid_unlink() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");		
		AMLRoleClassLibrary roleClassLibrary2 = a.createRoleClassLibrary("icl2");
		AMLRoleClass roleClass2 = roleClassLibrary2.createRoleClass("ic2");	
		roleClass2.setBaseRoleClass(roleClass);
		
		Savepoint sp1 = session.createSavepoint();
		roleClass2.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(roleClass2.getBaseRoleClass()).isNull();
				
		sp1.restore();
		assertThat(roleClass2.getBaseRoleClass()).isEqualTo(roleClass);
		
		sp2.restore();
		assertThat(roleClass2.getBaseRoleClass()).isNull();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlink2() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");		
		AMLRoleClassLibrary roleClassLibrary2 = a.createRoleClassLibrary("icl2");
		AMLRoleClass roleClass2 = roleClassLibrary2.createRoleClass("ic2");	
		roleClass2.setBaseRoleClass(roleClass);
		
		Savepoint sp1 = session.createSavepoint();
		roleClass.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(roleClass2.getBaseRoleClass()).isNull();
				
		sp1.restore();
		assertThat(roleClass2.getBaseRoleClass()).isEqualTo(roleClass);
		
		sp2.restore();
		assertThat(roleClass2.getBaseRoleClass()).isNull();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlinkConcurrent() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLRoleClass roleClass3 = roleClass.createRoleClass("ic3");
		AMLRoleClassLibrary roleClassLibrary2 = a.createRoleClassLibrary("icl2");
		AMLRoleClass roleClass2 = roleClassLibrary2.createRoleClass("ic2");	
		roleClass2.setBaseRoleClass(roleClass);
		roleClass3.setBaseRoleClass(roleClass);
		
		Savepoint sp1 = session.createSavepoint();
		roleClass.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(roleClass2.getBaseRoleClass()).isNull();
				
		sp1.restore();
		assertThat(roleClass2.getBaseRoleClass()).isEqualTo(roleClass);
		
		sp2.restore();
		assertThat(roleClass2.getBaseRoleClass()).isNull();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlink3() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");		
		AMLRoleClassLibrary roleClassLibrary2 = a.createRoleClassLibrary("icl2");
		AMLRoleClass roleClass2 = roleClassLibrary2.createRoleClass("ic2");	
		roleClass2.setBaseRoleClass(roleClass);
		
		Savepoint sp1 = session.createSavepoint();
		a.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(roleClass2.getBaseRoleClass()).isEqualTo(roleClass);
		
		sp1.restore();
		assertThat(roleClass2.getBaseRoleClass()).isEqualTo(roleClass);
		
		sp2.restore();
		assertThat(roleClass2.getBaseRoleClass()).isEqualTo(roleClass);
		
		sp1.delete();
		sp2.delete();			
	}
	
	@Test 
	public void invalid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("rcl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("rc");
		AMLRoleClass roleClass2 = roleClass.createRoleClass("rc2");
		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass2);
		
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
		AMLRoleClass roleClass2 = roleClass.createRoleClass("rc2");
		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass2);
		
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
	public void valid_deepDelete3() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("rcl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("rc");
		AMLRoleClass roleClass2 = roleClass.createRoleClass("rc2");
		roleClass2.setBaseRoleClass(roleClass);
		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass2);
		
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
		AMLRoleClass roleClass2 = roleClass.createRoleClass("ic2");
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(roleClassLibrary.validateCreateRoleClass("ic2").isOk()).isTrue();
		AMLRoleClass copy = roleClassLibrary.createRoleClass("ic2", roleClass);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(roleClassLibrary.getRoleClassesCount()).isEqualTo(2);
		
		sp1.restore();
		assertThat(roleClassLibrary.getRoleClassesCount()).isEqualTo(1);
		
		sp2.restore();
		assertThat(roleClassLibrary.getRoleClassesCount()).isEqualTo(2);
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void valid_deepCopy2() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLRoleClass roleClass2 = roleClass.createRoleClass("ic2");
		roleClass2.setBaseRoleClass(roleClass);
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(roleClassLibrary.validateCreateRoleClass("ic2").isOk()).isTrue();
		AMLRoleClass copy = roleClassLibrary.createRoleClass("ic2", roleClass);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(copy.getRoleClasses().iterator().next().getBaseRoleClass()).isEqualTo(copy);
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLRoleClass> iterator = roleClassLibrary.getRoleClasses().iterator();
		iterator.next();
		copy = iterator.next(); 
		assertThat(copy.getRoleClasses().iterator().next().getBaseRoleClass()).isEqualTo(copy);
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void valid_deepCopy3() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLRoleClass roleClass2 = roleClass.createRoleClass("ic2");
		roleClass2.setBaseRoleClass(roleClass);
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(roleClass.validateCreateRoleClass("ic3").isOk()).isTrue();
		AMLRoleClass copy = roleClass.createRoleClass("ic3", roleClass2);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(copy.getBaseRoleClass()).isEqualTo(roleClass);
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLRoleClass> iterator = roleClass.getRoleClasses().iterator();
		iterator.next();
		assertThat(iterator.next().getBaseRoleClass()).isEqualTo(roleClass);
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void valid_deepCopy_AvoidStackOverflow() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLRoleClass roleClass2 = roleClass.createRoleClass("ic2");
		roleClass2.setBaseRoleClass(roleClass);
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(roleClass.validateCreateRoleClass("ic3").isOk()).isTrue();
		AMLRoleClass copy = roleClass.createRoleClass("ic3", roleClass);
		
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
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLRoleClass roleClass2 = roleClass.createRoleClass("ic2");
	
		assertThat(roleClassLibrary.validateCreateRoleClass("ic").isAnyOperationNotPermitted()).isTrue();
		try {
			roleClassLibrary.createRoleClass("ic", roleClass);
			Fail.fail();
		} catch (AMLValidationException ex){
			
		}		
	}
	
	@Test
	public void valid_LoadLibrary() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLRoleClass roleClass = roleClassLibDoc.getRoleClassByPath("AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Structure/ResourceStructure/Cell");
		assertThat(roleClass).isNotNull();
		
		AMLRoleClass baseRole = roleClass.getBaseRoleClass();
		assertThat(baseRole).isNotNull();
		assertThat(baseRole.getName()).isEqualTo("ResourceStructure");
		
	}
	
	@Test
	public void valid_LoadRoleRequirment() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("33_rolerequirment.aml");
		URL url = file.toURI().toURL();
		AMLDocument doc = session.loadAMLDocument(url);

		
	}
	
	@Test
	public void valid_LoadRoleRequirment2() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("34_rolerequirment.aml");
		URL url = file.toURI().toURL();
		AMLDocument doc = session.loadAMLDocument(url);

		
	}
}
