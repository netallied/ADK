/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;

import org.automationml.Savepoint;
import org.automationml.aml.AMLAttribute.NominalScaledConstraint;
import org.junit.Test;

public class AMLFacetTest extends AbstractAMLTest {

	private TestFileLocator testFileLocator = new TestFileLocator(getClass());

	@Test(expected = AMLValidationException.class)
	public void invalid_createFacet() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		internalElement.createFacet("HMI");
	}

	@Test
	public void valid_createFacet() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		Savepoint sp1 = session.createSavepoint();
		AMLFacet facet = internalElement.createFacet("HMI");
		UUID id = facet.getId();
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(facet.isDeleted());
		sp2.restore();
		assertThat(internalElement.getFacets()).isNotEmpty();
		facet = internalElement.getFacet("HMI");
		assertThat(facet).isNotNull();
		assertThat(facet.getId()).isEqualTo(id);

		sp1.delete();
		sp2.delete();
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_createInternalElementAsFacet() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement facetElement = internalElement.createInternalElement();
		facetElement.setName("HMI");
		AMLRoleClass facetRole = a.getRoleClassByPath("AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Facet");
		facetElement.createSupportedRoleClass(facetRole);

	}

	@Test(expected = AMLValidationException.class)
	public void invalid_createFacetDuplicateName() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		internalElement.createFacet("HMI");
		internalElement.createFacet("HMI");
	}

	@Test
	public void valid_deleteFacet() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLFacet facet = internalElement.createFacet("HMI");
		UUID id = facet.getId();

		Savepoint sp1 = session.createSavepoint();
		facet.delete();
		assertThat(facet.isDeleted());
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(internalElement.getFacets()).isNotEmpty();
		facet = internalElement.getFacet("HMI");
		assertThat(facet).isNotNull();
		assertThat(facet.getId()).isEqualTo(id);

		sp2.restore();
		assertThat(facet.isDeleted());

		sp1.delete();
		sp2.delete();

	}

	@Test(expected = AMLValidationException.class)
	public void invalid_deleteInternalElement() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		internalElement.createFacet("HMI");
		internalElement.delete();
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_renameFacet() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.createFacet("HMI");
		AMLFacet facet = internalElement.createFacet("Test");
		facet.setName("HMI");
	}

	@Test
	public void valid_renameFacet() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLFacet facet = internalElement.createFacet("HMI");

		Savepoint sp1 = session.createSavepoint();
		facet.setName("Test");
		assertThat(internalElement.getFacet("Test")).isNotNull();
		assertThat(internalElement.getFacet("HMI")).isNull();
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(internalElement.getFacet("Test")).isNull();
		assertThat(internalElement.getFacet("HMI")).isNotNull();
		sp2.restore();
		assertThat(internalElement.getFacet("Test")).isNotNull();
		assertThat(internalElement.getFacet("HMI")).isNull();

		sp1.delete();
		sp2.delete();

	}

	@Test(expected = AMLValidationException.class)
	public void invalid_facetLinkAttribute() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement2 = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement2.createAttribute("Attribute");

		AMLFacet facet = internalElement.createFacet("HMI");
		facet.addAttribute(attribute);
	}

	@Test
	public void valid_facetLinkAttribute() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("Attribute");

		AMLFacet facet = internalElement.createFacet("HMI");
		Savepoint sp1 = session.createSavepoint();
		facet.addAttribute(attribute);
		assertThat(facet.getAttributes()).isNotEmpty();
		assertThat(facet.getAttribute("Attribute")).isSameAs(attribute);
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(facet.getAttributes()).isEmpty();

		sp2.restore();
		assertThat(facet.getAttributes()).isNotEmpty();
		assertThat(facet.getAttribute("Attribute")).isSameAs(attribute);

		sp1.delete();
		sp2.delete();
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_facetRenameAttribute() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("Attribute");

		AMLFacet facet = internalElement.createFacet("HMI");
		facet.addAttribute(attribute);

		attribute.setName("Test");
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_facetLinkInterface() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement2 = instanceHierarchy.createInternalElement();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("Lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");

		AMLExternalInterface externalInterface = internalElement2.createExternalInterface(interfaceClass);
		AMLFacet facet = internalElement.createFacet("HMI");
		facet.addExternalInterface(externalInterface);
	}

	@Test
	public void valid_facetLinkInterface() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("Lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLFacet facet = internalElement.createFacet("HMI");
		Savepoint sp1 = session.createSavepoint();
		facet.addExternalInterface(externalInterface);

		assertThat(facet.getExternalInterfaces()).isNotEmpty();
		assertThat(facet.getExternalInterface(externalInterface.getId())).isSameAs(externalInterface);
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(facet.getExternalInterfaces()).isEmpty();

		sp2.restore();
		assertThat(facet.getExternalInterfaces()).isNotEmpty();
		assertThat(facet.getExternalInterface(externalInterface.getId())).isSameAs(externalInterface);

		sp1.delete();
		sp2.delete();
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_deleteAttributeOfFacet() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("Attribute");

		AMLFacet facet = internalElement.createFacet("HMI");
		facet.addAttribute(attribute);

		attribute.delete();
	}

	@Test
	public void valid_deleteAttributeOfFacet() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("Attribute");

		AMLFacet facet = internalElement.createFacet("HMI");
		facet.addAttribute(attribute);

		Savepoint sp1 = session.createSavepoint();
		facet.removeAttribute(attribute);
		assertThat(facet.getAttributes()).isEmpty();

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();

		assertThat(facet.getAttributes()).isNotEmpty();
		assertThat(facet.getAttribute("Attribute")).isSameAs(attribute);

		sp2.restore();
		assertThat(facet.getAttributes()).isEmpty();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_deleteExternalInterfaceOfFacet() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("Lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLFacet facet = internalElement.createFacet("HMI");
		facet.addExternalInterface(externalInterface);

		Savepoint sp1 = session.createSavepoint();
		facet.removeExternalInterface(externalInterface);
		assertThat(facet.getExternalInterfaces()).isEmpty();
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(facet.getExternalInterfaces()).isNotEmpty();
		assertThat(facet.getExternalInterface(externalInterface.getId())).isSameAs(externalInterface);

		sp2.restore();
		assertThat(facet.getExternalInterfaces()).isEmpty();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_deserialize_31_facet() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("31_facet.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void invalid_deserialize_18_facet() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("18_facet_with_children.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
		}
	}

	@Test
	public void invalid_deserialize_19_facet() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("19_facet_with_wrong_attribute.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
		}
	}

	@Test 
	public void valid_deepDelete() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("att");
		NominalScaledConstraint constraint = attribute.createNominalScaledConstraint("Test");
		constraint.addRequiredValue("5");
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);

		AMLFacet facet = internalElement.createFacet("HMI");
		facet.addAttribute(attribute);
		facet.addExternalInterface(externalInterface);
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(facet.isDeleted()).isTrue();
		
		sp1.restore();
		internalElement = instanceHierarchy.getInternalElements().iterator().next();
		attribute = internalElement.getAttribute("att");
		externalInterface = internalElement.getExternalInterfaces().iterator().next();
		facet = internalElement.getFacet("HMI");
		assertThat(facet).isNotNull();
		assertThat(facet.getAttribute("att")).isEqualTo(attribute);
		assertThat(facet.getExternalInterface(externalInterface.getId())).isEqualTo(externalInterface);
		
		sp2.restore();
		assertThat(facet.isDeleted()).isTrue();
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_deepCopy() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("att");
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);

		AMLFacet facet = internalElement.createFacet("HMI");
		facet.addAttribute(attribute);
		facet.addExternalInterface(externalInterface);
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(instanceHierarchy.validateInternalElementCreate().isOk()).isTrue();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(internalElement);
		
		AMLFacet copyFacet = copy.getFacet("HMI");
		assertThat(copyFacet).isNotNull();
		assertThat(copyFacet.getAttribute("att")).isEqualTo(copy.getAttribute("att"));
		assertThat(copyFacet.getExternalInterfaces().iterator().next()).isEqualTo(copy.getExternalInterfaces().iterator().next());
		
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(copyFacet.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		iterator.next();
		copy = iterator.next();
		
		copyFacet = copy.getFacet("HMI");
		assertThat(copyFacet).isNotNull();
		assertThat(copyFacet.getAttribute("att")).isEqualTo(copy.getAttribute("att"));
		assertThat(copyFacet.getExternalInterfaces().iterator().next()).isEqualTo(copy.getExternalInterfaces().iterator().next());
		
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_createAndRemoveFacet() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		Savepoint sp1 = session.createSavepoint();
		AMLFacet facet = internalElement.createFacet("HMI");
		UUID id = facet.getId();
		facet.delete();
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		sp2.restore();

		sp1.delete();
		sp2.delete();
	}
}

