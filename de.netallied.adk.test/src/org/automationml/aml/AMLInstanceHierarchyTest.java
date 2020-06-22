/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.automationml.Savepoint;
import org.fest.assertions.Fail;
import org.junit.Test;

public class AMLInstanceHierarchyTest extends AbstractAMLTest {

	@Test 
	public void valid_createInstanceHierarchy() throws Exception {
		
		AMLDocument a = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInstanceHierarchy ref = a.getInstanceHierarchy("hierarchy");
		
		Savepoint sp2 = session.createSavepoint();
		
		assertThat(instanceHierarchy).isNotNull();
		assertThat(ref).isNotNull();
		
		sp1.restore();
		ref = a.getInstanceHierarchy("hierarchy");
		assertThat(ref).isNull();
		assertThat(instanceHierarchy.isDeleted());
		
		sp2.restore();
		ref = a.getInstanceHierarchy("hierarchy");
		assertThat(ref).isNotNull();
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_deleteInstanceHierarchy() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		
		Savepoint sp = session.createSavepoint();
		instanceHierarchy.delete();		
		instanceHierarchy = a.getInstanceHierarchy("hierarchy");
		assertThat(instanceHierarchy).isNull();
		
		Savepoint sp2 = session.createSavepoint();
		
		sp.restore();
		instanceHierarchy = a.getInstanceHierarchy("hierarchy");
		assertThat(instanceHierarchy).isNotNull();
		
		sp2.restore();
		instanceHierarchy = a.getInstanceHierarchy("hierarchy");
		assertThat(instanceHierarchy).isNull();
		
		sp.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_changeName() throws Exception {
		
		AMLDocument a = session.createAMLDocument();

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierachy");
		
		Savepoint sp = session.createSavepoint();
		instanceHierarchy.setName("xxx");
		assertThat(instanceHierarchy.getName().equals("xxx"));
	
		sp.restore();
		assertThat(instanceHierarchy.getName().equals("hierachy"));
		
		sp.delete();
	}
	
	@Test(expected = AMLValidationException.class)
	public void invalid_createTwoHierachiesWithSameNameInOneDocument() throws Exception {
		AMLDocument a = session.createAMLDocument();
		a.createInstanceHierarchy("hierachy");
		a.createInstanceHierarchy("hierachy");
	}
	
	@Test
	public void valid_createTwoHierachiesWithSameNameInTwoDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		a.createInstanceHierarchy("hierachy");
		b.createInstanceHierarchy("hierachy");		
	}
	
	@Test(expected = AMLValidationException.class)
	public void invalid_changeNameInOneDocument() throws Exception {
		AMLDocument a = session.createAMLDocument();
		a.createInstanceHierarchy("hierarchy");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy2");
		instanceHierarchy.setName("hierarchy");		
	}
	
	@Test
	public void valid_changeNameInTwoDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		a.createInstanceHierarchy("hierachy");
		AMLInstanceHierarchy instanceHierarchy = b.createInstanceHierarchy("hierachy2");		
		instanceHierarchy.setName("hierachy");
	}
	
	@Test
	public void valid_reparentOverDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		AMLInstanceHierarchy instanceHierarchy = b.createInstanceHierarchy("hierarchy");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(instanceHierarchy.validateReparent(b, a, null, null).isOk() == true);
		instanceHierarchy.reparent(b, a,  null, null);
		
		Savepoint sp2 = session.createSavepoint();

		assertThat(a.getInstanceHierarchies().iterator().hasNext());
		assertThat(a.getInstanceHierarchies().iterator().next().equals(instanceHierarchy));
		assertThat(b.getInstanceHierarchies().iterator().hasNext() == false);
		
		sp1.restore();
		assertThat(b.getInstanceHierarchies().iterator().hasNext());
		assertThat(b.getInstanceHierarchies().iterator().next().equals(instanceHierarchy));
		assertThat(a.getInstanceHierarchies().iterator().hasNext() == false);
		
		sp2.restore();
		assertThat(a.getInstanceHierarchies().iterator().hasNext());
		assertThat(a.getInstanceHierarchies().iterator().next().equals(instanceHierarchy));
		assertThat(b.getInstanceHierarchies().iterator().hasNext() == false);
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void invalid_reparentOverDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		AMLInstanceHierarchy instanceHierarchy = b.createInstanceHierarchy("hierarchy");
		a.createInstanceHierarchy("hierarchy");
		
		assertThat(instanceHierarchy.validateReparent(b, a, null, null).isOk() == true);
		try {
			instanceHierarchy.reparent(b, a,  null, null);
			Fail.fail();
		} catch (AMLValidationException ex) {
			
		}

		
	}
	
	@Test
	public void valid_reparentAfter() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInstanceHierarchy instanceHierarchy2 = a.createInstanceHierarchy("hierarchy2");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(instanceHierarchy.validateReparent(a, a, null, instanceHierarchy2).isOk() == true);
		instanceHierarchy.reparent(a, a,  null, instanceHierarchy2);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLInstanceHierarchy> iterator = a.getInstanceHierarchies().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(instanceHierarchy2));
		assertThat(iterator.next().equals(instanceHierarchy));
		
		sp1.restore();
		iterator = a.getInstanceHierarchies().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(instanceHierarchy));
		assertThat(iterator.next().equals(instanceHierarchy2));
		
		sp2.restore();
		iterator = a.getInstanceHierarchies().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(instanceHierarchy2));
		assertThat(iterator.next().equals(instanceHierarchy));
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentBefore() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInstanceHierarchy instanceHierarchy2 = a.createInstanceHierarchy("hierarchy2");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(instanceHierarchy2.validateReparent(a, a, instanceHierarchy, null).isOk() == true);
		instanceHierarchy2.reparent(a, a,  instanceHierarchy, null);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLInstanceHierarchy> iterator = a.getInstanceHierarchies().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(instanceHierarchy2));
		assertThat(iterator.next().equals(instanceHierarchy));
		
		sp1.restore();
		iterator = a.getInstanceHierarchies().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(instanceHierarchy));
		assertThat(iterator.next().equals(instanceHierarchy2));
		
		sp2.restore();
		iterator = a.getInstanceHierarchies().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(instanceHierarchy2));
		assertThat(iterator.next().equals(instanceHierarchy));
		
		sp2.delete();
		sp1.delete();	
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
			instanceHierarchy.reparent(a, b,  null, null);
			Fail.fail();
		} catch (AMLValidationException ex) {
			
		}
	}
	
	@Test 
	public void valid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(instanceHierarchy.validateDeepDelete().isOk()).isTrue();
		instanceHierarchy.deepDelete();
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement.isDeleted()).isTrue();
		assertThat(instanceHierarchy.isDeleted()).isTrue();
		
		sp1.restore();
		assertThat(a.getInstanceHierarchies().iterator().hasNext()).isTrue();
		assertThat(a.getInstanceHierarchies().iterator().next().getInternalElements().iterator().hasNext()).isTrue();

		sp2.restore();
		assertThat(a.getInstanceHierarchies().iterator().hasNext()).isFalse();
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_deepCopy() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(a.validateCreateInstanceHierarchy("hierarchy2").isOk()).isTrue();
		AMLInstanceHierarchy copy = a.createInstanceHierarchy("hierarchy2", instanceHierarchy);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(a.getInstanceHierarchiesCount()).isEqualTo(2);
		
		sp1.restore();
		assertThat(a.getInstanceHierarchiesCount()).isEqualTo(1);
		
		sp2.restore();
		assertThat(a.getInstanceHierarchiesCount()).isEqualTo(2);
		
		sp2.delete();
		sp1.delete();		
	}
	
	@Test
	public void invalid_deepCopy() throws Exception {		
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
	
		assertThat(a.validateCreateInstanceHierarchy("hierarchy").isAnyOperationNotPermitted()).isTrue();
		try {
			a.createInstanceHierarchy("hierarchy", instanceHierarchy);
			Fail.fail();
		} catch (AMLValidationException ex){
			
		}		
	}
	
	@Test
	public void valid_simpleWizard() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		Savepoint sp = session.createSavepoint();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		instanceHierarchy.delete();
		
		sp.restore();
		
		assertThat(a.getInstanceHierarchiesCount()).isEqualTo(0);
		
		sp.delete();
	}
	
	@Test
	public void valid_deepDelete_tooLong() throws Exception {
		TestFileLocator testFileLocator = new TestFileLocator(getClass());
		
		File file = testFileLocator.getValidTestCaseFile("deepDelete_long.aml");
		URL url = file.toURI().toURL();
		AMLDocument document = session.loadAMLDocument(url);
		
		AMLInstanceHierarchy hierarchy = document.getInstanceHierarchies().iterator().next();
		
		AMLValidationResultList validateDelete = hierarchy.validateUnlink();
		if (validateDelete.isOk()) {
			hierarchy.unlink();
			hierarchy.deepDelete();
		}
	}
		
}
