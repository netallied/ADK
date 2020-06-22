/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;

import org.automationml.Savepoint;
import org.junit.Test;

public class AMLGroupTest  extends AbstractAMLTest {

	private TestFileLocator testFileLocator = new TestFileLocator(getClass());

	@Test(expected = AMLValidationException.class)
	public void invalid_createGroup() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");

		instanceHierarchy.createGroup();
	}
	
	@Test
	public void valid_createGroup() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		
		Savepoint sp1 = session.createSavepoint();
		AMLGroup group = instanceHierarchy.createGroup();
		UUID id = group.getId();
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(group.isDeleted());
		sp2.restore();
		assertThat(instanceHierarchy.getGroups()).isNotEmpty();
		group = instanceHierarchy.getGroups().iterator().next();
		assertThat(group).isNotNull();
		assertThat(group.getId()).isEqualTo(id);

		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_nestedGroup() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLGroup group = instanceHierarchy.createGroup();
				
		Savepoint sp1 = session.createSavepoint();
		AMLGroup nestedGroup = group.createGroup();
		UUID id = nestedGroup.getId();
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(nestedGroup.isDeleted());
		sp2.restore();
		assertThat(group.getGroups()).isNotEmpty();
		group = group.getGroups().iterator().next();
		assertThat(group).isNotNull();
		assertThat(group.getId()).isEqualTo(id);

		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_groupChangeName() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLGroup group = instanceHierarchy.createGroup();
				
		Savepoint sp1 = session.createSavepoint();
		group.setName("Gruppe");
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(group.getName()).isNullOrEmpty();
		sp2.restore();
		assertThat(group.getName()).isEqualTo("Gruppe");

		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_Group_DeepDelete() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("h");
		AMLGroup group = instanceHierarchy.createGroup();
		AMLGroup group2 = group.createGroup();
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(group.validateDeepDelete().isOk()).isTrue();
		group.deepDelete();
		
		assertThat(group.isDeleted()).isTrue();
		assertThat(group2.isDeleted()).isTrue();
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		group = instanceHierarchy.getGroups().iterator().next();
		assertThat(group).isNotNull();
		group2 = group.getGroups().iterator().next();
		assertThat(group2).isNotNull();
		
		sp2.restore();
		assertThat(group.isDeleted()).isTrue();
		assertThat(group2.isDeleted()).isTrue();
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_Group_DeepCopy() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("h");
		AMLInstanceHierarchy instanceHierarchy2 = a.createInstanceHierarchy("h2");
		AMLGroup group = instanceHierarchy.createGroup();
		AMLGroup group2 = group.createGroup();
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(instanceHierarchy2.validateGroupCreate().isOk()).isTrue();
		AMLGroup copy = instanceHierarchy2.createGroup(group);
		assertThat(copy.getGroupsCount()).isEqualTo(1);
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		copy = instanceHierarchy2.getGroups().iterator().next();
		assertThat(copy.getGroupsCount()).isEqualTo(1);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void invalid_Group_reparent() throws Exception {
		
	}
	
	@Test 
	public void valid_Group_reparent() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("h");
		AMLInstanceHierarchy instanceHierarchy2 = a.createInstanceHierarchy("h2");
		AMLGroup group = instanceHierarchy.createGroup();
		AMLGroup group2 = group.createGroup();
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(group.validateReparent(instanceHierarchy, instanceHierarchy2, null, null).isOk()).isTrue();
		group.reparent(instanceHierarchy, instanceHierarchy2, null, null);
		assertThat(instanceHierarchy2.getGroupsCount()).isEqualTo(1);
		assertThat(instanceHierarchy.getGroupsCount()).isEqualTo(0);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(instanceHierarchy2.getGroupsCount()).isEqualTo(0);
		assertThat(instanceHierarchy.getGroupsCount()).isEqualTo(1);
		
		sp2.restore();
		assertThat(instanceHierarchy2.getGroupsCount()).isEqualTo(1);
		assertThat(instanceHierarchy.getGroupsCount()).isEqualTo(0);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_Group_reparentBefore() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("h");
		AMLInstanceHierarchy instanceHierarchy2 = a.createInstanceHierarchy("h2");
		AMLGroup group = instanceHierarchy.createGroup();
		AMLGroup group2 = instanceHierarchy2.createGroup();
	
		assertThat(group.validateReparent(instanceHierarchy, instanceHierarchy2, group2, null).isOk()).isTrue();
		group.reparent(instanceHierarchy, instanceHierarchy2, group2, null);
		
		Iterator<AMLGroup> iterator = instanceHierarchy2.getGroups().iterator();
		assertThat(iterator.next()).isEqualTo(group);
		assertThat(iterator.next()).isEqualTo(group2);
		
	}
	
	@Test 
	public void valid_Group_reparentAfter() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("h");
		AMLInstanceHierarchy instanceHierarchy2 = a.createInstanceHierarchy("h2");
		AMLGroup group = instanceHierarchy.createGroup();
		AMLGroup group2 = instanceHierarchy2.createGroup();
	
		assertThat(group.validateReparent(instanceHierarchy, instanceHierarchy2, null, group2).isOk()).isTrue();
		group.reparent(instanceHierarchy, instanceHierarchy2, null, group2);
		
		Iterator<AMLGroup> iterator = instanceHierarchy2.getGroups().iterator();
		assertThat(iterator.next()).isEqualTo(group2);
		assertThat(iterator.next()).isEqualTo(group);
	}
	
	
	
}
