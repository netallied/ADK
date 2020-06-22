/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import org.automationml.Savepoint;
import org.automationml.SavepointException;
import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInterfaceClassLibrary;
import org.automationml.aml.AMLSession;
import org.automationml.aml.AbstractAMLTest;
import org.automationml.internal.aml.AMLSessionImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.automationml.aml.AMLSessionManager.amlSessionManager;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class SavepointManagerTest {
	private AMLSession session;

	@Before
	public void createSession() throws Exception {
		session = amlSessionManager.createSession();
	}

	@After
	public void assertSessionClean() {
		assertThat(((AMLSessionImpl) session).getIdentifierManager().isEmpty()).describedAs("IdentifierManager is not cleaned up").isTrue();
	}

	@Test
	public void createSavepoint_sameSavepoints() throws Exception {
		Savepoint sp1 = session.createSavepoint();
		Savepoint sp2 = session.createSavepoint();
		assertThat(sp1).isSameAs(sp2);
	}

	@Test
	public void createSavepoint_differentSavepoints() throws Exception {
		AMLDocument a = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		a.createInterfaceClassLibrary("1");
		Savepoint sp2 = session.createSavepoint();
		assertThat(sp1).isNotSameAs(sp2);
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	@Ignore
	public void createSavepoint_afterRestore_invalidatingSucceedingSavepoints() throws Exception {
		AMLDocument a = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		a.createInterfaceClassLibrary("1");
		Savepoint sp2 = session.createSavepoint();
		a.createInterfaceClassLibrary("2");
		Savepoint sp3 = session.createSavepoint();

		sp2.restore();

		Savepoint sp4 = session.createSavepoint();

		try {
			sp3.restore();
			fail();
		} catch (SavepointException e) {
		}
		
		// nothing has changed since sp2 to sp4  should work but does not!!! 
		sp2.restore();
		sp1.restore();

		assertThat(a.getInterfaceClassLibraries()).isEmpty();

		sp1.delete();
		sp2.delete();
		sp3.delete();
		sp4.delete();
	}

	@Test
	public void restoreOneSavepoint() throws Exception {
		Savepoint sp1 = session.createSavepoint();
		AMLDocument a = session.createAMLDocument();
		sp1.restore();

		assertThat(a.isDeleted()).isTrue();
		assertSessionClean();
	}

	@Test
	public void restoreSavepoint() throws Exception {
		AMLDocument a = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		a.createInterfaceClassLibrary("1");
		a.createInterfaceClassLibrary("2");

		Savepoint sp2 = session.createSavepoint();
		a.createInterfaceClassLibrary("3");
		a.createInterfaceClassLibrary("4");

		Savepoint sp3 = session.createSavepoint();

		assertThat(AbstractAMLTest.getNamesOfInterfaceClassLibraries(a)).containsOnly("1", "2", "3", "4");

		sp2.restore();

		assertThat(AbstractAMLTest.getNamesOfInterfaceClassLibraries(a)).containsOnly("1", "2");

		sp3.restore();

		assertThat(AbstractAMLTest.getNamesOfInterfaceClassLibraries(a)).containsOnly("1", "2", "3", "4");

		sp1.restore();

		assertThat(a.getInterfaceClassLibraries()).isEmpty();

		sp1.delete();
		sp3.delete();
		sp2.delete();
	}

	@Test
	public void restoreSavepoint_resultingInDifferentObjects() throws Exception {
		AMLDocument a = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("1");
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();

		sp2.restore();

		assertThat(AbstractAMLTest.getNamesOfInterfaceClassLibraries(a)).containsOnly("1");

		assertThat(lib1.isDeleted()).isTrue();

		AMLInterfaceClassLibrary newLib = a.getInterfaceClassLibrary("1");

		assertThat(newLib).isNotNull();

		assertThat(lib1).isNotSameAs(a.getInterfaceClassLibrary("1"));

		sp2.delete();
		sp1.delete();
	}

	@Test
	public void deleteCurrentSavepoint() throws Exception {
		Savepoint sp1 = session.createSavepoint();
		sp1.delete();
		try {
			sp1.restore();
			fail();
		} catch (SavepointException e) {
		}
	}

	@Test
	public void deleteSavepoint_withMerge1() throws Exception {
		AMLDocument a = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		a.createInterfaceClassLibrary("1");
		Savepoint sp2 = session.createSavepoint();
		a.createInterfaceClassLibrary("2");
		Savepoint sp3 = session.createSavepoint();

		//		sp1.restore();
		//		sp2.restore();
		sp1.delete();
		sp2.delete();
		sp3.delete();
	}

	@Test
	public void deleteSavepoint_withMerge2() throws Exception {
		AMLDocument a = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		a.createInterfaceClassLibrary("1");
		Savepoint sp2 = session.createSavepoint();
		a.createInterfaceClassLibrary("2");
		Savepoint sp3 = session.createSavepoint();
		a.createInterfaceClassLibrary("3");
		Savepoint sp4 = session.createSavepoint();

		sp1.restore();
		sp2.delete();
		sp3.restore();

		assertThat(AbstractAMLTest.getNamesOfInterfaceClassLibraries(a)).containsOnly("1", "2");

		sp1.restore();
		sp3.delete();
		sp4.restore();

		assertThat(AbstractAMLTest.getNamesOfInterfaceClassLibraries(a)).containsOnly("1", "2", "3");

		sp4.delete();
		sp1.delete();
	}

	@Test
	public void deleteFirstSavepoint() throws Exception {
		AMLDocument a = session.createAMLDocument();

		Savepoint sp1 = session.createSavepoint();
		a.createInterfaceClassLibrary("1");
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		sp1.delete();
		sp2.restore();

		assertThat(AbstractAMLTest.getNamesOfInterfaceClassLibraries(a)).containsOnly("1");

		sp2.delete();
	}

	@Test
	public void merge_of_changes_in_savepoint() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass = lib.createInterfaceClass("class");

		Savepoint sp1 = session.createSavepoint();
		interfaceClass.setName("yyy");
		interfaceClass.setName("xxx");
		assertThat(((SavepointImpl) sp1).changes).hasSize(1);

		sp1.delete();
	}
	
	@Test 
	public void valid_abortSavepoint() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		
		Savepoint sp1 = session.createSavepoint();
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib1");
		
		Savepoint sp2 = session.createSavepoint();
		lib.setName("lib2");
		
		Savepoint sp3 = session.createSavepoint();
		lib.setName("lib3");
		assertThat(lib.getName()).isEqualTo("lib3");
		
		sp3.cancel();
		assertThat(lib.getName()).isEqualTo("lib2");
		
		sp3.delete();
		sp2.delete();
		sp1.delete();		
	}
	
	@Test 
	public void valid_abortSavepoint2() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		
		Savepoint sp1 = session.createSavepoint();
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib1");
		
		Savepoint sp2 = session.createSavepoint();
		lib.setName("lib2");
		
		Savepoint sp3 = session.createSavepoint();
		lib.setName("lib3");
		assertThat(lib.getName()).isEqualTo("lib3");
		
		Savepoint sp4 = session.createSavepoint();
		
		sp3.restore();
		assertThat(lib.getName()).isEqualTo("lib2");
		
		sp2.restore();
		assertThat(lib.getName()).isEqualTo("lib1");
		
		Savepoint sp3a = session.createSavepoint();
		lib.setName("lib3a");
		
		sp3a.cancel();		
		assertThat(lib.getName()).isEqualTo("lib1");
		
		sp3.restore();
		assertThat(lib.getName()).isEqualTo("lib2");
		
		sp4.restore();
		assertThat(lib.getName()).isEqualTo("lib3");
		
		sp4.delete();
		sp3.delete();
		sp2.delete();
		sp1.delete();		
	}
}
