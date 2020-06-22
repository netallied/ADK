/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;

import org.automationml.Savepoint;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class AMLMirrorObjectTest extends AbstractAMLTest {

	private TestFileLocator testFileLocator = new TestFileLocator(getClass());

	@Test
	public void valid_createMirror() throws Exception {

		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLGroup group = instanceHierarchy.createGroup();

		Savepoint sp1 = session.createSavepoint();
		AMLMirrorObject mirror = group.createMirror(internalElement);
		UUID id = mirror.getId();
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(mirror.isDeleted());
		sp2.restore();
		mirror = group.getMirrorObjects().iterator().next();
		assertThat(mirror).isNotNull();
		assertThat(mirror.getId()).isEqualTo(id);
		assertThat(mirror.getInternalElement()).isEqualTo(internalElement);

		sp1.delete();
		sp2.delete();

	}

	@Test
	public void valid_deleteMirror() throws Exception {

		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLGroup group = instanceHierarchy.createGroup();
		AMLMirrorObject mirror = group.createMirror(internalElement);
		UUID id = mirror.getId();

		Savepoint sp1 = session.createSavepoint();
		mirror.delete();
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		mirror = group.getMirrorObjects().iterator().next();
		assertThat(mirror).isNotNull();
		assertThat(mirror.getId()).isEqualTo(id);
		assertThat(mirror.getInternalElement()).isEqualTo(internalElement);

		sp2.restore();
		assertThat(mirror.isDeleted());

		sp1.delete();
		sp2.delete();

	}

	@Test(expected = AMLValidationException.class)
	public void invalid_deleteMirroredInternalElement() throws Exception {

		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLGroup group = instanceHierarchy.createGroup();
		AMLMirrorObject mirror = group.createMirror(internalElement);

		internalElement.delete();
	}

	// @Test (expected = AMLValidationException.class)
	// public void invalid_createCircuitMirror() throws Exception {
	//
	// AMLDocument a = session.createAMLDocument();
	//
	// AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
	// AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
	// internalElement.createMirror(internalElement);
	// }

	// @Test (expected = AMLValidationException.class)
	// public void invalid_createCircuitMirror2() throws Exception {
	//
	// AMLDocument a = session.createAMLDocument();
	//
	// AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
	// AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
	// AMLInternalElement internalElement2 = internalElement.createInternalElement();
	// internalElement2.createMirror(internalElement);
	// }

	@Test
	public void valid_30_mirror() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("30_mirror.aml");
		URL url = file.toURI().toURL();

		AMLDocument document = session.loadAMLDocument(url);

		AMLInstanceHierarchy instanceHierarchy = document.getInstanceHierarchies().iterator().next();
		AMLGroup group = instanceHierarchy.getGroups().iterator().next();
		assertThat(group.getName()).isEqualTo("Group");
	}
	
	@Test
	public void valid_Mirror_DeepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);

		Savepoint sp1 = session.createSavepoint();
		
		internalElement.deepDelete();
		assertThat(mirror.isDeleted());	
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		iterator.next();
		internalElement = iterator.next();
		assertThat(internalElement.getMirrorObjectsCount()).isEqualTo(1);
		mirror = internalElement.getMirrorObjects().iterator().next();
		assertThat(mirror).isNotNull();
		
		sp2.restore();
		assertThat(mirror.isDeleted()).isTrue();

		sp1.delete();
		sp2.delete();

	}
	
	@Test
	public void invalid_Mirror_DeepDelet2() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);
		try {
			elementToMirror.deepDelete();
			Fail.fail();
		} catch (AMLValidationException ex){
			
		}	
	}
	
	@Test
	public void valid_Mirror_DeepDelete3() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);

		Savepoint sp1 = session.createSavepoint();
		
		instanceHierarchy.deepDelete();
		assertThat(mirror.isDeleted());	
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		Iterator<AMLInternalElement> iterator = a.getInstanceHierarchy("hierarchy").getInternalElements().iterator();
		iterator.next();
		internalElement = iterator.next();
		assertThat(internalElement.getMirrorObjectsCount()).isEqualTo(1);
		mirror = internalElement.getMirrorObjects().iterator().next();
		assertThat(mirror).isNotNull();
		
		sp2.restore();
		assertThat(mirror.isDeleted()).isTrue();

		sp1.delete();
		sp2.delete();

	}
	
	@Test 
	public void valid_Mirror_DeepCopy() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);

		Savepoint sp1 = session.createSavepoint();
		
		AMLInternalElement copy = instanceHierarchy.createInternalElement(internalElement);
		AMLMirrorObject mirrorCopy = copy.getMirrorObjects().iterator().next();
		assertThat(mirrorCopy).isNotNull();
		assertThat(mirrorCopy.getInternalElement()).isEqualTo(elementToMirror);		
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		assertThat(mirrorCopy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLInternalElement> iterator = a.getInstanceHierarchy("hierarchy").getInternalElements().iterator();
		iterator.next();
		iterator.next();
		copy = iterator.next();
		mirrorCopy = copy.getMirrorObjects().iterator().next();
		assertThat(mirrorCopy).isNotNull();
		assertThat(mirrorCopy.getInternalElement()).isEqualTo(elementToMirror);		

		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_Mirror_DeepCopy2() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);

		Savepoint sp1 = session.createSavepoint();
		
		AMLInstanceHierarchy copy = a.createInstanceHierarchy("h2", instanceHierarchy);
		Iterator<AMLInternalElement> iterator = copy.getInternalElements().iterator();
		AMLInternalElement copyInternalElement = iterator.next();
		AMLInternalElement copyElementToMirror = iterator.next();
		AMLMirrorObject mirrorCopy = copyInternalElement.getMirrorObjects().iterator().next();
		assertThat(mirrorCopy).isNotNull();
		assertThat(mirrorCopy.getInternalElement()).isEqualTo(copyElementToMirror);		
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(mirrorCopy.isDeleted()).isTrue();
		
		
		sp2.restore();
		iterator = a.getInstanceHierarchy("h2").getInternalElements().iterator();
		copyInternalElement = iterator.next();
		copyElementToMirror = iterator.next();
		mirrorCopy = copyInternalElement.getMirrorObjects().iterator().next();
		assertThat(mirrorCopy).isNotNull();
		assertThat(mirrorCopy.getInternalElement()).isEqualTo(copyElementToMirror);		

		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void invalid_Mirror_reparent() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		a.addExplicitExternalReference(b);
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);
		
		AMLInstanceHierarchy instanceHierarchy2 = b.createInstanceHierarchy("h2");
		AMLInternalElement internalElement2 = instanceHierarchy2.createInternalElement();
		
		assertThat(mirror.validateReparent(internalElement, internalElement2, null, null).isAnyOperationNotPermitted()).isTrue();
		try {
			mirror.reparent(internalElement, internalElement2, null, null);
			Fail.fail();
		} catch (AMLValidationException ex) {
			
		}
		
	}
	
	@Test 
	public void valid_Mirror_reparent() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);
		AMLInternalElement internalElement2 = instanceHierarchy.createInternalElement();
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(mirror.validateReparent(internalElement, internalElement2, null, null).isOk()).isTrue();
		mirror.reparent(internalElement, internalElement2, null, null);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(mirror.getParent()).isEqualTo(internalElement);
		
		sp2.restore();
		assertThat(mirror.getParent()).isEqualTo(internalElement2);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_Mirror_reparent_before() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement2 = instanceHierarchy.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);
		AMLMirrorObject mirror2 = internalElement2.createMirror(elementToMirror);

		
		Savepoint sp1 = session.createSavepoint();
		assertThat(mirror.validateReparent(internalElement, internalElement2, mirror2, null).isOk()).isTrue();
		mirror.reparent(internalElement, internalElement2, mirror2, null);
		Iterator<AMLMirrorObject> iterator = internalElement2.getMirrorObjects().iterator();
		assertThat(iterator.next()).isEqualTo(mirror);
		assertThat(iterator.next()).isEqualTo(mirror2);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		iterator = internalElement2.getMirrorObjects().iterator();
		assertThat(iterator.next()).isEqualTo(mirror2);
		assertThat(iterator.hasNext()).isFalse();
		
		sp2.restore();
		iterator = internalElement2.getMirrorObjects().iterator();
		assertThat(iterator.next()).isEqualTo(mirror);
		assertThat(iterator.next()).isEqualTo(mirror2);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_Mirror_reparent_after() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement2 = instanceHierarchy.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);
		AMLMirrorObject mirror2 = internalElement2.createMirror(elementToMirror);

		
		Savepoint sp1 = session.createSavepoint();
		assertThat(mirror.validateReparent(internalElement, internalElement2, null, mirror2).isOk()).isTrue();
		mirror.reparent(internalElement, internalElement2, null, mirror2);
		Iterator<AMLMirrorObject> iterator = internalElement2.getMirrorObjects().iterator();
		assertThat(iterator.next()).isEqualTo(mirror2);
		assertThat(iterator.next()).isEqualTo(mirror);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		iterator = internalElement2.getMirrorObjects().iterator();
		assertThat(iterator.next()).isEqualTo(mirror2);
		assertThat(iterator.hasNext()).isFalse();
		
		sp2.restore();
		iterator = internalElement2.getMirrorObjects().iterator();
		assertThat(iterator.next()).isEqualTo(mirror2);
		assertThat(iterator.next()).isEqualTo(mirror);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_Mirror_DeepDelete2() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);

		Savepoint sp1 = session.createSavepoint();
		
		elementToMirror.unlink();
		elementToMirror.deepDelete();
		assertThat(mirror.isDeleted());	
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(internalElement.getMirrorObjectsCount()).isEqualTo(1);
		mirror = internalElement.getMirrorObjects().iterator().next();
		assertThat(mirror).isNotNull();
		
		sp2.restore();
		assertThat(mirror.isDeleted()).isTrue();

		sp1.delete();
		sp2.delete();

	}
	
	@Test 
	public void valid_reparentOverDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		a.addExplicitExternalReference(b);
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInstanceHierarchy instanceHierarchy2 = b.createInstanceHierarchy("h2");
		AMLInternalElement internalElement = instanceHierarchy2.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy2.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);
				
		assertThat(internalElement.validateReparent(instanceHierarchy2, instanceHierarchy, null, null).isOk()).isTrue();
		internalElement.reparent(instanceHierarchy2, instanceHierarchy, null, null);
		
		assertThat(internalElement.getParent()).isEqualTo(instanceHierarchy);
	}
	
	@Test 
	public void invalid_reparentOverDocuments2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		a.addExplicitExternalReference(b);
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInstanceHierarchy instanceHierarchy2 = b.createInstanceHierarchy("h2");
		AMLInternalElement internalElement = instanceHierarchy2.createInternalElement();
		AMLInternalElement elementToMirror = instanceHierarchy2.createInternalElement();
		AMLMirrorObject mirror = internalElement.createMirror(elementToMirror);
				
		assertThat(elementToMirror.validateReparent(instanceHierarchy2, instanceHierarchy, null, null).isAnyOperationNotPermitted()).isTrue();
		
		try {
			elementToMirror.reparent(instanceHierarchy2, instanceHierarchy, null, null);
			Fail.fail();
		} catch (AMLValidationException ex) {
		}
		
	}

}
