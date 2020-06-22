/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml;

import java.io.PrintStream;

import org.automationml.aml.AMLAttributeTest;
import org.automationml.aml.AMLClassLibraryTest;
import org.automationml.aml.AMLDocumentDeserializeTest;
import org.automationml.aml.AMLDocumentTest;
import org.automationml.aml.AMLGroupTest;
import org.automationml.aml.AMLInstanceHierarchyTest;
import org.automationml.aml.AMLInterfaceClassLibraryTest;
import org.automationml.aml.AMLInterfaceClassTest;
import org.automationml.aml.AMLInterfaceValidatorTest;
import org.automationml.aml.AMLInternalElementTest;
import org.automationml.aml.AMLRoleClassLibraryTest;
import org.automationml.aml.AMLRoleClassTest;
import org.automationml.aml.AMLRoleValidatorTest;
import org.automationml.aml.AMLSystemUnitClassLibraryTest;
import org.automationml.aml.AMLSystemUnitClassTest;
import org.automationml.aml.AMLSystemUnitValidatorTest;
import org.automationml.internal.SavepointManagerTest;
import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;

public class AMLTestRunner {

	public static class MyTextListener extends TextListener {

		public MyTextListener(PrintStream writer) {
			super(writer);
			// TODO Auto-generated constructor stub
		}

		public void testStarted(Description description) {
			System.out.println(description.toString());
		}

	}

	public static void main(String[] args) {
		JUnitCore junit = new JUnitCore();
		MyTextListener listener = new MyTextListener(System.out);
		junit.addListener(listener);
		//		junit.run(AMLTestSuite.class);
		//		junit.run(AMLDocumentTest.class);

		junit.run(
				AMLDocumentTest.class,
				AMLInterfaceClassLibraryTest.class,
				AMLInterfaceClassTest.class,
				AMLRoleClassLibraryTest.class,
				AMLRoleClassTest.class,
				AMLSystemUnitClassLibraryTest.class,
				AMLSystemUnitClassTest.class,
				SavepointManagerTest.class,
				AMLClassLibraryTest.class,
				AMLInterfaceValidatorTest.class,
				AMLRoleValidatorTest.class,
				AMLSystemUnitValidatorTest.class,
				AMLAttributeTest.class,
				AMLInstanceHierarchyTest.class,
				AMLInternalElementTest.class,
				AMLDocumentDeserializeTest.class,
				AMLGroupTest.class);

	}
}
