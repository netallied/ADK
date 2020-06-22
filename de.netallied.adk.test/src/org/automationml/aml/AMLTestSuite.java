/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.internal.SavepointManagerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AMLDocumentTest.class, AMLInterfaceClassLibraryTest.class, AMLInterfaceClassTest.class, AMLRoleClassLibraryTest.class,
		AMLRoleClassTest.class, AMLSystemUnitClassLibraryTest.class, AMLSystemUnitClassTest.class, AMLDocumentDeserializeTest.class,
		SavepointManagerTest.class, AMLClassLibraryTest.class, AMLInterfaceValidatorTest.class, AMLRoleValidatorTest.class, AMLSystemUnitValidatorTest.class,
		AMLAttributeTest.class, AMLInstanceHierarchyTest.class, AMLInternalElementTest.class, AMLExternalInterfaceTest.class, AMLSupportedRoleClassTest.class,
		AMLInternalLinkTest.class, AMLMirrorObjectTest.class, AMLFacetTest.class, AMLCOLLADAInterfaceTest.class, AMLDocumentSerializeTest.class,
		AMLGroupTest.class})
public class AMLTestSuite {
}