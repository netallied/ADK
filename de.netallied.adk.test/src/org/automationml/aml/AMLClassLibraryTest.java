/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

import static org.fest.assertions.Fail.fail;

public class AMLClassLibraryTest extends AbstractAMLTest {

	@Test
	public void invalid_deleteClassLibrary_containingClass() throws Exception {
		AMLDocument a = session.createAMLDocument();

		String uniqueLibraryName = "lib1";
		String uniqueClassName = "baseclass";
		
		AMLInterfaceClassLibrary ic_lib1 = a.createInterfaceClassLibrary(uniqueLibraryName);
		ic_lib1.createInterfaceClass(uniqueClassName);
		try {
			ic_lib1.delete();
			fail();
		} catch (AMLValidationException e) {
		}

		AMLRoleClassLibrary rc_lib1 = a.createRoleClassLibrary(uniqueLibraryName);
		rc_lib1.createRoleClass(uniqueClassName);
		try {
			rc_lib1.delete();
			fail();
		} catch (AMLValidationException e) {
		}

		AMLSystemUnitClassLibrary suc_lib1 = a.createSystemUnitClassLibrary(uniqueLibraryName);
		suc_lib1.createSystemUnitClass(uniqueClassName);
		try {
			suc_lib1.delete();
			fail();
		} catch (AMLValidationException e) {
		}

		assertThat(a.getInterfaceClassLibrary(uniqueLibraryName)).isSameAs(ic_lib1);
		assertThat(a.getRoleClassLibrary(uniqueLibraryName)).isSameAs(rc_lib1);
		assertThat(a.getSystemUnitClassLibrary(uniqueLibraryName)).isSameAs(suc_lib1);
	}

	@Test
	public void invalid_deleteClass_containingClass() throws Exception {
		AMLDocument a = session.createAMLDocument();

		String uniqueLibraryName = "lib1";
		String uniqueBaseClassName = "baseclass";
		String uniqueClassName = "class";
		
		String uniqueBaseClassPath = uniqueLibraryName + "/" + uniqueBaseClassName;
		String uniqueClassPath = uniqueLibraryName + "/" + uniqueClassName;
		
		AMLInterfaceClassLibrary ic_lib1 = a.createInterfaceClassLibrary(uniqueLibraryName);
		AMLInterfaceClass ic_baseclass = ic_lib1.createInterfaceClass(uniqueBaseClassName);
		AMLInterfaceClass ic_class = ic_lib1.createInterfaceClass(uniqueClassName);
		ic_class.setBaseInterfaceClass(ic_baseclass);
		try {
			ic_baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}

		AMLRoleClassLibrary rc_lib1 = a.createRoleClassLibrary(uniqueLibraryName);
		AMLRoleClass rc_baseclass = rc_lib1.createRoleClass(uniqueBaseClassName);
		AMLRoleClass rc_class = rc_lib1.createRoleClass(uniqueClassName);
		rc_class.setBaseRoleClass(rc_baseclass);
		try {
			rc_baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}

		AMLSystemUnitClassLibrary suc_lib1 = a.createSystemUnitClassLibrary(uniqueLibraryName);
		AMLSystemUnitClass suc_baseclass = suc_lib1.createSystemUnitClass(uniqueBaseClassName);
		AMLSystemUnitClass suc_class = suc_lib1.createSystemUnitClass(uniqueClassName);
		suc_class.setBaseSystemUnitClass(suc_baseclass);
		try {
			suc_baseclass.delete();
			fail();
		} catch (AMLValidationException e) {
		}

		assertThat(a.getInterfaceClassLibrary(uniqueLibraryName)).isSameAs(ic_lib1);
		assertThat(a.getRoleClassLibrary(uniqueLibraryName)).isSameAs(rc_lib1);
		assertThat(a.getSystemUnitClassLibrary(uniqueLibraryName)).isSameAs(suc_lib1);

		assertThat(a.getInterfaceClassByPath(uniqueBaseClassPath)).isSameAs(ic_baseclass);
		assertThat(a.getInterfaceClassByPath(uniqueClassPath)).isSameAs(ic_class);

		assertThat(a.getRoleClassByPath(uniqueBaseClassPath)).isSameAs(rc_baseclass);
		assertThat(a.getRoleClassByPath(uniqueClassPath)).isSameAs(rc_class);

		assertThat(a.getSystemUnitClassByPath(uniqueBaseClassPath)).isSameAs(suc_baseclass);
		assertThat(a.getSystemUnitClassByPath(uniqueClassPath)).isSameAs(suc_class);
}

	@Test
	public void valid_classLibraryLookup_by_name() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		lib1.createInterfaceClass("baseclass");

		AMLRoleClassLibrary lib2 = a.createRoleClassLibrary("lib2");
		lib2.createRoleClass("baseclass");

		AMLSystemUnitClassLibrary lib3 = a.createSystemUnitClassLibrary("lib3");
		lib3.createSystemUnitClass("baseclass");

		assertThat(a.getInterfaceClassLibrary("lib1")).isSameAs(lib1);
		assertThat(a.getRoleClassLibrary("lib2")).isSameAs(lib2);
		assertThat(a.getSystemUnitClassLibrary("lib3")).isSameAs(lib3);
		
		//--- invalid library names
		AMLInterfaceClassLibrary interfaceClassLibrary = a.getInterfaceClassLibrary("lib3");
		AMLRoleClassLibrary roleClassLibrary = a.getRoleClassLibrary("lib1");
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.getSystemUnitClassLibrary("lib2");
		
		assertThat( interfaceClassLibrary ).isNull();
		assertThat( roleClassLibrary ).isNull();
		assertThat( systemUnitClassLibrary ).isNull();
	}

	@Test
	public void invalid_classLookup_by_path() throws Exception {
		AMLDocument a = session.createAMLDocument();

		String uniqueLibraryName = "lib1";
		
		a.createInterfaceClassLibrary(uniqueLibraryName);
		AMLInterfaceClass ic_class = a.getInterfaceClassLibrary(uniqueLibraryName).createInterfaceClass("baseClass1");

		a.createRoleClassLibrary(uniqueLibraryName);
		AMLRoleClass rc_class = a.getRoleClassLibrary(uniqueLibraryName).createRoleClass("baseClass2");

		a.createSystemUnitClassLibrary(uniqueLibraryName);
		AMLSystemUnitClass suc_class = a.getSystemUnitClassLibrary(uniqueLibraryName).createSystemUnitClass("baseClass3");

		String correctInterfaceClassPath = uniqueLibraryName + "/" + "baseClass1";
		String correctRoleClassPath = uniqueLibraryName + "/" + "baseClass2";
		String correctSystemUnitClassPath = uniqueLibraryName + "/" + "baseClass3";
		
		assertThat(a.getInterfaceClassByPath( correctInterfaceClassPath )).isNotNull();
		assertThat(a.getRoleClassByPath( correctRoleClassPath )).isNotNull();
		assertThat(a.getSystemUnitClassByPath( correctSystemUnitClassPath )).isNotNull();
		
		assertThat(a.getInterfaceClassByPath(correctInterfaceClassPath)).isSameAs(ic_class);
		assertThat(a.getRoleClassByPath(correctRoleClassPath)).isSameAs(rc_class);
		assertThat(a.getSystemUnitClassByPath(correctSystemUnitClassPath)).isSameAs(suc_class);
		
		//--- invalid class pathes ---
		assertThat(a.getInterfaceClassByPath(uniqueLibraryName + "/" + "baseClass3")).isNull();
		assertThat(a.getRoleClassByPath(uniqueLibraryName + "/" + "baseClass1")).isNull();
		assertThat(a.getSystemUnitClassByPath(uniqueLibraryName + "/" + "baseClass2")).isNull();

		assertThat(a.getInterfaceClassByPath(uniqueLibraryName + "/" + "baseClass2")).isNull();
		assertThat(a.getRoleClassByPath(uniqueLibraryName + "/" + "baseClass3")).isNull();
		assertThat(a.getSystemUnitClassByPath(uniqueLibraryName + "/" + "baseClass1")).isNull();
	}



}
