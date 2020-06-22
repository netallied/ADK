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
import org.automationml.aml.AMLCOLLADAInterface.RefType;
import org.junit.Ignore;
import org.junit.Test;

public class AMLExternalInterfaceTest extends AbstractAMLTest {

	private AMLInterfaceClassLibrary createInterfaceClassLibrary;
	private TestFileLocator testFileLocator = new TestFileLocator(getClass());

	@Test
	public void valid_createExternalInterface() throws Exception {

		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		Savepoint sp1 = session.createSavepoint();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		UUID id = externalInterface.getId();

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		Iterator<AMLExternalInterface> externalInterfaces = internalElement.getExternalInterfaces().iterator();
		assertThat(externalInterfaces.hasNext()).isFalse();

		sp2.restore();
		externalInterfaces = internalElement.getExternalInterfaces().iterator();
		assertThat(externalInterfaces.hasNext()).isTrue();
		externalInterface = externalInterfaces.next();
		assertThat(externalInterface.getId().equals(id));

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_deleteExternalInterface() throws Exception {

		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		UUID id = externalInterface.getId();

		Savepoint sp1 = session.createSavepoint();
		externalInterface.delete();

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		Iterator<AMLExternalInterface> externalInterfaces = internalElement.getExternalInterfaces().iterator();
		assertThat(externalInterfaces.hasNext()).isTrue();
		externalInterface = externalInterfaces.next();
		assertThat(externalInterface.getId().equals(id));

		sp2.restore();
		externalInterfaces = internalElement.getExternalInterfaces().iterator();
		assertThat(externalInterfaces.hasNext()).isFalse();

		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_deleteExternalInterfaceWithAttributes() throws Exception {

		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLAttribute baseAttribute = interfaceClass.createAttribute("attribute");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();		

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		UUID id = externalInterface.getId();

		Savepoint sp1 = session.createSavepoint();
		externalInterface.delete();

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		Iterator<AMLExternalInterface> externalInterfaces = internalElement.getExternalInterfaces().iterator();
		assertThat(externalInterfaces.hasNext()).isTrue();
		externalInterface = externalInterfaces.next();
		assertThat(externalInterface.getId().equals(id));
		assertThat(externalInterface.getAttribute("attribute")).isNotNull();

		sp2.restore();
		externalInterfaces = internalElement.getExternalInterfaces().iterator();
		assertThat(externalInterfaces.hasNext()).isFalse();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_externalInterfaceSetName() throws Exception {

		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);

		Savepoint sp1 = session.createSavepoint();
		externalInterface.setName("interface");

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(externalInterface.getName()).isNullOrEmpty();

		sp2.restore();
		assertThat(externalInterface.getName().equals("interface"));

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_createExternalInterfaceWithAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLAttribute baseAttribute = interfaceClass.createAttribute("attribute");
		baseAttribute.createAttribute("sub");

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		Savepoint sp1 = session.createSavepoint();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		Iterator<AMLAttribute> attributes = externalInterface.getAttributes().iterator();
		assertThat(attributes.hasNext()).isTrue();
		AMLAttribute attribute = attributes.next();
		assertThat(attribute.getName().equals("attribute")).isTrue();
		AMLAttribute subAttribute = attribute.getAttribute("sub");
		assertThat(subAttribute).isNotNull();

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(externalInterface.isDeleted()).isTrue();

		sp2.restore();
		externalInterface = internalElement.getExternalInterfaces().iterator().next();
		assertThat(externalInterface).isNotNull();
		attributes = externalInterface.getAttributes().iterator();
		assertThat(attributes.hasNext());
		attribute = attributes.next();
		assertThat(attribute.getName().equals("attribute")).isTrue();
		subAttribute = attribute.getAttribute("sub");
		assertThat(subAttribute).isNotNull();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_createExternalInterfaceAddAttributeLate() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);

		externalInterface.createAttribute("Attribute");
		interfaceClass.createAttribute("Attribute");
	}

	@Ignore
	@Test(expected = AMLValidationException.class)
	public void invalid_createExternalInterfaceAddAttributeLate() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		internalElement.createExternalInterface(interfaceClass);

		interfaceClass.createAttribute("Attribute");
	}

	@Ignore
	@Test(expected = AMLValidationException.class)
	public void invalid_changeAttributeNameInInternalElement() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		interfaceClass.createAttribute("attribute");

		AMLAttribute attribute = externalInterface.getAttribute("attribute");

		attribute.setName("bla");
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_changeAttributeName() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLAttribute baseAttribute = interfaceClass.createAttribute("attribute");

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		baseAttribute.setName("Test");
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_changeExternalInterfaceAttributeName() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		interfaceClass.createAttribute("attribute");

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLAttribute attribute = externalInterface.getAttribute("attribute");
		attribute.setName("Test");
	}

	@Test
	public void valid_createExternalInterfaceDeleteAttributeLate() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLAttribute baseAttribute = interfaceClass.createAttribute("attribute");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		externalInterface.setName("interface");

		Savepoint sp1 = session.createSavepoint();
		baseAttribute.delete();

		AMLAttribute attribute = externalInterface.getAttribute("attribute");
		assertThat(attribute).isNotNull();

		sp1.restore();
		baseAttribute = interfaceClass.getAttribute("attribute");
		assertThat(baseAttribute).isNotNull();
		attribute = externalInterface.getAttribute("attribute");
		assertThat(attribute).isNotNull();

		sp1.delete();
	}

	@Test
	public void valid_createAttributeInInterfaceClassAlreadyExistsInExternalInterface() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		externalInterface.setName("interface");
		AMLAttribute attribute = externalInterface.createAttribute("attribute");

		Savepoint sp1 = session.createSavepoint();
		AMLAttribute baseAttribute = interfaceClass.createAttribute("attribute");
		assertThat(externalInterface.getAttribute("attribute")).isEqualTo(attribute);

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();

		assertThat(externalInterface.getAttribute("attribute")).isEqualTo(attribute);

		sp2.restore();
		assertThat(externalInterface.getAttribute("attribute")).isEqualTo(attribute);

		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void invalid_reparentInterfaceClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLInterfaceClassLibrary interfaceClassLibrary = b.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		
		AMLInstanceHierarchy instanceHierarchy = b.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		
		assertThat(interfaceClassLibrary.validateReparent(b, a, null, null).isAnyOperationNotPermitted()).isTrue();
	}
	
	@Test
	public void valid_reparentInterfaceClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		
		assertThat(interfaceClassLibrary.validateReparent(a, b, null, null).isOk()).isTrue();
	}
	
	@Test
	public void valid_unlink() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		
		Savepoint sp1 = session.createSavepoint();
		internalElement.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isFalse();
		
		sp1.restore();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(internalElement.getExternalInterfaces().iterator().next().getInterfaceClass()).isEqualTo(interfaceClass);
		
		sp2.restore();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isFalse();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlink2() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		
		Savepoint sp1 = session.createSavepoint();
		a.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(internalElement.getExternalInterfaces().iterator().next()).isEqualTo(externalInterface);
		sp1.restore();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(internalElement.getExternalInterfaces().iterator().next()).isEqualTo(externalInterface);
		
		sp2.restore();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(internalElement.getExternalInterfaces().iterator().next()).isEqualTo(externalInterface);
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test 
	public void valid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
			
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLAttribute attribute = externalInterface.createAttribute("attr");
		attribute.setValue("6");
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		assertThat(internalElement.isDeleted()).isTrue();
		assertThat(externalInterface.isDeleted()).isTrue();

		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext()).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getExternalInterfaces().iterator().hasNext()).isTrue();
		
		sp2.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext()).isFalse();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test 
	public void valid_unlinkAndDeepDelete() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("InterfaceClassLibraries/AutomationMLInterfaceClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument roleClassLibDoc = session.loadAMLDocument(url);
		
		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(roleClassLibDoc);
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
			
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLAttribute attribute = externalInterface.createAttribute("attr");
		attribute.setValue("6");
		AMLCOLLADAInterface colladaInterface = internalElement.createCOLLADAInterface(RefType.EXPLICIT);
		colladaInterface.setName("Hallo");
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.unlink();
		internalElement.deepDelete();
		assertThat(internalElement.isDeleted()).isTrue();
		assertThat(externalInterface.isDeleted()).isTrue();
		assertThat(colladaInterface.isDeleted()).isTrue();

		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext()).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getExternalInterfaces().iterator().next().getAttribute("attr").getValue()).isEqualTo("6");
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getCOLLADAInterface().getName()).isEqualTo("Hallo");
		sp2.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext()).isFalse();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_deepCopy() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(internalElement);
		assertThat(copy.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(copy.getExternalInterfaces().iterator().next().getInterfaceClass()).isEqualTo(interfaceClass);
		
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		iterator.next();
		copy = iterator.next();
		assertThat(copy.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(copy.getExternalInterfaces().iterator().next().getInterfaceClass()).isEqualTo(interfaceClass);
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_addAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		interfaceClass.createAttribute("Attribute");
		assertThat(externalInterface.validateCreateAttribute("Attribute").isOk()).isTrue();
	}
	
	@Ignore
	@Test
	public void invalid_addAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		interfaceClass.createAttribute("Attribute");
		assertThat(externalInterface.validateCreateAttribute("Attribute2").isAnyOperationNotPermitted()).isTrue();
	}
	
	@Test
	public void valid_deepCopyExternalInterfaceWithAddionalAttributes () throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		AMLAttribute baseAttribute = interfaceClass.createAttribute("base");
		baseAttribute.setValue("alt");
		AMLAttribute baseAttribute2 = interfaceClass.createAttribute("base2");
		baseAttribute2.setValue("alt");
		
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		externalInterface.getAttribute("base2").setValue("neu");
		AMLAttribute attribute = externalInterface.createAttribute("instance");
		attribute.setValue("val");
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(internalElement);
		assertThat(copy.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(copy.getExternalInterfaces().iterator().next().getInterfaceClass()).isEqualTo(interfaceClass);
		assertThat(copy.getExternalInterfaces().iterator().next().getAttribute("base").getValue()).isEqualTo("alt");
		assertThat(copy.getExternalInterfaces().iterator().next().getAttribute("base2").getValue()).isEqualTo("neu");
		assertThat(copy.getExternalInterfaces().iterator().next().getAttribute("instance").getValue()).isEqualTo("val");
		
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		iterator.next();
		copy = iterator.next();
		assertThat(copy.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(copy.getExternalInterfaces().iterator().next().getInterfaceClass()).isEqualTo(interfaceClass);
		assertThat(copy.getExternalInterfaces().iterator().next().getAttribute("base").getValue()).isEqualTo("alt");
		assertThat(copy.getExternalInterfaces().iterator().next().getAttribute("base2").getValue()).isEqualTo("neu");
		assertThat(copy.getExternalInterfaces().iterator().next().getAttribute("instance").getValue()).isEqualTo("val");
		
		sp1.delete();
		sp2.delete();		
	}

}
