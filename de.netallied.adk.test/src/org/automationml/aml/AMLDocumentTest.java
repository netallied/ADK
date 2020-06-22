/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.Iterator;

import org.automationml.Savepoint;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.automationml.aml.AMLSessionManager.amlSessionManager;
import static org.fest.assertions.Assertions.assertThat;

public class AMLDocumentTest extends AbstractAMLTest {

	@Test(expected = AMLDocumentScopeInvalidException.class)
	public void invalid_addExplicitExternalReference() throws Exception {
		AMLDocument doc1 = session.createAMLDocument();
		AMLDocument doc2 = session.createAMLDocument();

		doc1.createInterfaceClassLibrary("lib1");
		doc2.createInterfaceClassLibrary("lib1");

		doc1.addExplicitExternalReference(doc2);
	}

	@Test(expected = AMLDocumentScopeInvalidException.class)
	public void invalid_addExplicitExternalReferenceChain() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();

		a.createInterfaceClassLibrary("lib");
		c.createInterfaceClassLibrary("lib");

		a.addExplicitExternalReference(b);
		b.addExplicitExternalReference(c);
	}

	@Test(expected = AMLDocumentScopeInvalidException.class)
	public void invalid_addExplicitExternalReferenceCycle_otherSession() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSession session2 = amlSessionManager.createSession();
		AMLDocument b = session2.createAMLDocument();
		a.addExplicitExternalReference(b);
	}

	@Test(expected = AMLDocumentScopeInvalidException.class)
	public void invalid_addExplicitExternalReferenceCycle1() throws Exception {
		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(a);
	}

	@Test(expected = AMLDocumentScopeInvalidException.class)
	public void invalid_addExplicitExternalReferenceCycle2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		a.addExplicitExternalReference(b);
		b.addExplicitExternalReference(a);
	}

	@Test(expected = AMLDocumentScopeInvalidException.class)
	public void invalid_addExplicitExternalReferenceCycle3() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();
		AMLDocument d = session.createAMLDocument();

		a.addExplicitExternalReference(b);
		b.addExplicitExternalReference(c);
		c.addExplicitExternalReference(d);
		c.addExplicitExternalReference(a);
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_addExplicitExternalReferenceDiamond() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();
		AMLDocument d = session.createAMLDocument();

		a.createInterfaceClassLibrary("lib");

		a.addExplicitExternalReference(b);
		a.addExplicitExternalReference(c);
		b.addExplicitExternalReference(d);
		c.addExplicitExternalReference(d);

		d.createInterfaceClassLibrary("lib");
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_addImplicitAndRemoveExplicitExternalReference() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		a.addExplicitExternalReference(b);

		AMLInterfaceClassLibrary lib1_a = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass1 = lib1_a.createInterfaceClass("class");

		AMLInterfaceClassLibrary lib2_b = b.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass2 = lib2_b.createInterfaceClass("class");
		interfaceClass1.setBaseInterfaceClass(interfaceClass2);

		a.removeExplicitExternalReference(b);
		assertThat(a.getExplicitlyReferencedDocuments()).isEmpty();

		// this shall fail, because the implicit reference still exists
		b.createInterfaceClassLibrary("lib1");
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_addImplicitExternalReference() throws Exception {
		AMLDocument doc1 = session.createAMLDocument();
		AMLDocument doc2 = session.createAMLDocument();

		AMLInterfaceClassLibrary lib1_1 = doc1.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass1 = lib1_1.createInterfaceClass("class");
		doc1.createInterfaceClassLibrary("lib2");

		AMLInterfaceClassLibrary lib2_2 = doc2.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass2 = lib2_2.createInterfaceClass("class");

		interfaceClass1.setBaseInterfaceClass(interfaceClass2);
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_createTwoConflictingLibraryReferences() throws Exception {
		AMLDocument document = session.createAMLDocument();

		document.createInterfaceClassLibrary("LIB1");
		document.createInterfaceClassLibrary("LIB1");
	}

	@Test(expected = AMLDocumentScopeInvalidException.class)
	public void invalid_disposeDocument() throws Exception {
		AMLDocument document = session.createAMLDocument();
		document.createInterfaceClassLibrary("LIB1");
		document.delete();
	}

	@Test(expected = AMLDocumentScopeInvalidException.class)
	public void invalid_disposeDocument_explicitelyReferenced() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		b.addExplicitExternalReference(a);
		a.delete();
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_disposeDocument_implicitelyReferenced() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLInterfaceClass baseclass = a.createInterfaceClassLibrary("lib1").createInterfaceClass("baseclass");
		b.createInterfaceClassLibrary("lib2").createInterfaceClass("class").setBaseInterfaceClass(baseclass);

		a.delete();
	}

	@Test
	public void valid_addExplicitExternalReference() throws Exception {
		AMLDocument doc1 = session.createAMLDocument();
		AMLDocument doc2 = session.createAMLDocument();

		doc1.createInterfaceClassLibrary("lib1");
		doc2.createInterfaceClassLibrary("lib2");
		doc1.addExplicitExternalReference(doc2);
	}

	@Test
	public void valid_addExplicitExternalReference_complex() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();
		AMLDocument d = session.createAMLDocument();
		AMLDocument e = session.createAMLDocument();
		AMLDocument f = session.createAMLDocument();
		AMLDocument g = session.createAMLDocument();
		AMLDocument h = session.createAMLDocument();
		AMLDocument i = session.createAMLDocument();
		AMLDocument j = session.createAMLDocument();

		a.addExplicitExternalReference(b);
		c.addExplicitExternalReference(d);
		e.addExplicitExternalReference(f);
		g.addExplicitExternalReference(h);
		i.addExplicitExternalReference(j);

		String lib = "lib";
		a.createInterfaceClassLibrary(lib);
		AMLInterfaceClassLibrary lib_f = f.createInterfaceClassLibrary(lib);

		try {
			b.addExplicitExternalReference(e);
			Fail.fail();
		} catch (AMLDocumentScopeInvalidException ex) {
		}

		d.addExplicitExternalReference(e);
		try {
			c.createInterfaceClassLibrary(lib);
			Fail.fail();
		} catch (AMLValidationException ex) {
		}

		lib_f.delete();
		c.createInterfaceClassLibrary(lib);
	}

	@Test
	public void valid_addExplicitExternalReference2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();

		a.createInterfaceClassLibrary("lib");
		b.createInterfaceClassLibrary("lib");
		a.addExplicitExternalReference(c);
		b.addExplicitExternalReference(c);
	}

	@Test
	public void valid_addExplicitExternalReferenceDiamond() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();
		AMLDocument d = session.createAMLDocument();

		a.addExplicitExternalReference(b);
		a.addExplicitExternalReference(c);
		b.addExplicitExternalReference(d);
		c.addExplicitExternalReference(d);
	}

	@Test
	public void valid_disposeDocument() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		a.addExplicitExternalReference(b);
		a.delete();
	}

	@Test
	public void valid_getReferrers() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		b.addExplicitExternalReference(a);;

		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass_1_1 = lib1.createInterfaceClass("class1");
		AMLInterfaceClass interfaceClass_1_2 = lib1.createInterfaceClass("class2");

		AMLInterfaceClassLibrary lib2 = b.createInterfaceClassLibrary("lib2");
		AMLInterfaceClass interfaceClass_2_1 = lib2.createInterfaceClass("class1");
		AMLInterfaceClass interfaceClass_2_2 = lib2.createInterfaceClass("class2");

		assertThat(interfaceClass_1_1.getReferrers()).isEmpty();
		assertThat(interfaceClass_1_2.getReferrers()).isEmpty();
		assertThat(interfaceClass_2_1.getReferrers()).isEmpty();
		assertThat(interfaceClass_2_2.getReferrers()).isEmpty();

		interfaceClass_2_2.setBaseInterfaceClass(interfaceClass_2_1);
		assertThat(interfaceClass_1_1.getReferrers()).isEmpty();
		assertThat(interfaceClass_1_2.getReferrers()).isEmpty();
		assertThat(interfaceClass_2_1.getReferrers()).containsOnly(interfaceClass_2_2);
		assertThat(interfaceClass_2_2.getReferrers()).isEmpty();

		interfaceClass_2_2.setBaseInterfaceClass(interfaceClass_1_1);
		assertThat(interfaceClass_1_1.getReferrers()).containsOnly(interfaceClass_2_2);
		assertThat(interfaceClass_1_2.getReferrers()).isEmpty();
		assertThat(interfaceClass_2_1.getReferrers()).isEmpty();
		assertThat(interfaceClass_2_2.getReferrers()).isEmpty();

		interfaceClass_1_2.setBaseInterfaceClass(interfaceClass_1_1);
		interfaceClass_2_1.setBaseInterfaceClass(interfaceClass_1_1);
		assertThat(interfaceClass_1_1.getReferrers()).containsOnly(interfaceClass_1_2, interfaceClass_2_1, interfaceClass_2_2);
		assertThat(interfaceClass_1_2.getReferrers()).isEmpty();
		assertThat(interfaceClass_2_1.getReferrers()).isEmpty();
		assertThat(interfaceClass_2_2.getReferrers()).isEmpty();
	}

	@Test
	public void valid_removeExplicitExternalReference() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		a.addExplicitExternalReference(b);
		assertThat(a.getExplicitlyReferencedDocuments()).containsOnly(b);

		a.removeExplicitExternalReference(b);
		assertThat(a.getExplicitlyReferencedDocuments()).isEmpty();

		String lib = "lib";
		a.createInterfaceClassLibrary(lib);
		b.createInterfaceClassLibrary(lib);
	}


	@Test
	public void valid_deleteDocument_savepoints() throws Exception {

		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		a.delete();
		Savepoint sp2 = session.createSavepoint();
		b.delete();
		Savepoint sp3 = session.createSavepoint();

		assertThat(a.isDeleted()).isTrue();
		assertThat(b.isDeleted()).isTrue();

		assertThat(session.getDocuments()).hasSize(0);

		sp2.restore();
		assertThat(session.getDocuments()).hasSize(1);
		
		sp3.restore();
		assertThat(session.getDocuments()).hasSize(0);
		
		sp2.restore();
		assertThat(session.getDocuments()).hasSize(1);
		
		sp1.restore();
		assertThat(session.getDocuments()).hasSize(2);	

		sp1.delete();
		sp2.delete();
		sp3.delete();
	}

	@Test
	public void valid_createDocument_savepoints() throws Exception {

		Savepoint sp1 = session.createSavepoint();
		AMLDocument a = session.createAMLDocument();
		Savepoint sp2 = session.createSavepoint();
		AMLDocument b = session.createAMLDocument();
		Savepoint sp3 = session.createSavepoint();

		assertThat(session.getDocuments()).hasSize(2);

		sp1.restore();
		assertThat(a.isDeleted()).isTrue();
		assertThat(b.isDeleted()).isTrue();
		assertThat(session.getDocuments()).hasSize(0);

		sp2.restore();
		assertThat(session.getDocuments()).hasSize(1);

		sp3.restore();
		assertThat(session.getDocuments()).hasSize(2);

		sp1.delete();
		sp2.delete();
		sp3.delete();
	}
	
	@Test
	public void valid_addReferenceWithSavePoints() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		Savepoint sp1 = session.createSavepoint();
		a.addExplicitExternalReference(b);
		Savepoint sp2 = session.createSavepoint();
		
		assertThat(a.getExplicitlyReferencedDocuments().iterator().hasNext() == true);
		
		sp1.restore();
		assertThat(a.getExplicitlyReferencedDocuments().iterator().hasNext() == false);
		
		sp2.restore();
		assertThat(a.getExplicitlyReferencedDocuments().iterator().hasNext() == true);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_reparent() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		b.addExplicitExternalReference(c);
		
		assertThat(c.validateReparent(b, a, null, null).isOk() == true);
		c.reparent(b, a,  null, null);
		
		Iterator<AMLDocument> iterator = a.getExplicitlyReferencedDocuments().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.hasNext());
		assertThat(iterator.hasNext() == false);
		assertThat(b.getExplicitlyReferencedDocuments().iterator().hasNext() == false);		
	}
	
	@Test
	public void valid_reparentAfter() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();
		AMLDocument d = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		a.addExplicitExternalReference(d);
		b.addExplicitExternalReference(c);
		
		assertThat(c.validateReparent(b, a, null, b).isOk() == true);
		c.reparent(b, a,  null, null);
		
		Iterator<AMLDocument> iterator = a.getExplicitlyReferencedDocuments().iterator();
		assertThat(iterator.next().equals(b));
		assertThat(iterator.next().equals(c));
		assertThat(iterator.next().equals(d));
	}
	
	@Test
	public void valid_reparentBefore() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();
		AMLDocument d = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		a.addExplicitExternalReference(d);
		b.addExplicitExternalReference(c);
		
		assertThat(c.validateReparent(b, a, b, null).isOk() == true);
		c.reparent(b, a,  null, null);
		
		Iterator<AMLDocument> iterator = a.getExplicitlyReferencedDocuments().iterator();
		assertThat(iterator.next().equals(c));
		assertThat(iterator.next().equals(b));
		assertThat(iterator.next().equals(d));
	}

	@Test
	public void invalid_reparentCreatingCycles() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		b.addExplicitExternalReference(c);
		
		assertThat(a.validateReparent(b, c, null, null).isOk() == false);
		try {
			a.reparent(b, c,  null, null);
			Fail.fail();
		} catch (Exception e) {
		}
	}
	
	@Test
	public void valid_addReferencingDocumentWithReference() throws Exception {
		{
			AMLDocument a = session.createAMLDocument();
			AMLDocument b = session.createAMLDocument();
			AMLDocument c = session.createAMLDocument();
			c.createRoleClassLibrary("dummy");
			
			a.addExplicitExternalReference(b);
			b.addExplicitExternalReference(c);
			a.addExplicitExternalReference(c);
		}
		
		{
			AMLDocument a = session.createAMLDocument();
			AMLDocument b = session.createAMLDocument();
			AMLDocument c = session.createAMLDocument();
			c.createRoleClassLibrary("dummy");
			
			a.addExplicitExternalReference(c);
			b.addExplicitExternalReference(c);
			a.addExplicitExternalReference(b);
		}
		
		
	}
	
	@Test
	public void valid_removeExplicitReference() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		a.addExplicitExternalReference(c);
		b.addExplicitExternalReference(c);
		
		AMLInterfaceClassLibrary interfaceClassLibrary = c.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		
		AMLInterfaceClassLibrary interfaceClassLibrary2 = a.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary2.createInterfaceClass("ic2");
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
		
		assertThat(a.validateRemoveExplicitExternalReference(c).isOk()).isTrue();
		assertThat(a.validateRemoveExplicitExternalReference(b).isOk()).isTrue();	
	}
	
	@Test
	public void invalid_removeExplicitReference() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		a.addExplicitExternalReference(c);
		
		AMLInterfaceClassLibrary interfaceClassLibrary = c.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		
		AMLInterfaceClassLibrary interfaceClassLibrary2 = a.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary2.createInterfaceClass("ic2");
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
		
		assertThat(a.validateRemoveExplicitExternalReference(c).isAnyOperationNotPermitted()).isTrue();
		assertThat(a.validateRemoveExplicitExternalReference(b).isOk()).isTrue();
	}
	
	@Test
	public void invalid_removeExplicitReference2() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		AMLDocument c = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		b.addExplicitExternalReference(c);
		
		AMLInterfaceClassLibrary interfaceClassLibrary = c.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		
		AMLInterfaceClassLibrary interfaceClassLibrary2 = a.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary2.createInterfaceClass("ic2");
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
		
		assertThat(a.validateRemoveExplicitExternalReference(b).isAnyOperationNotPermitted()).isTrue();	
	}
	
	@Test
	public void valid_unlinkReference() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLInterfaceClassLibrary interfaceClassLibrary = b.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		
		AMLInterfaceClassLibrary interfaceClassLibrary2 = a.createInterfaceClassLibrary("icl2");
		AMLInterfaceClass interfaceClass2 = interfaceClassLibrary2.createInterfaceClass("ic2");
		interfaceClass2.setBaseInterfaceClass(interfaceClass);
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(b.validateUnlink().isOk()).isTrue();
		b.unlink();
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isNull();
		
		sp1.restore();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isEqualTo(interfaceClass);
		
		sp2.restore();
		assertThat(interfaceClass2.getBaseInterfaceClass()).isNull();
		assertThat(a.validateRemoveExplicitExternalReference(b).isOk()).isTrue();
		
		sp1.delete();
		sp2.delete();
	}
	
	
}
