/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;

import org.automationml.Savepoint;
import org.junit.Test;

public class AMLCOLLADAInterfaceTest extends AbstractAMLTest {

	private TestFileLocator testFileLocator = new TestFileLocator(getClass());

	@Test(expected = AMLValidationException.class)
	public void invalid_createColladaInterface() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		internalElement.createCOLLADAInterface(AMLCOLLADAInterface.RefType.EXPLICIT);
	}

	@Test
	public void valid_createColladaInterface() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("InterfaceClassLibraries/AutomationMLInterfaceClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument interfaceClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(interfaceClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		Savepoint sp1 = session.createSavepoint();
		AMLCOLLADAInterface colladaInterface = internalElement.createCOLLADAInterface(AMLCOLLADAInterface.RefType.EXPLICIT);

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(colladaInterface.isDeleted()).isTrue();
		assertThat(internalElement.getCOLLADAInterface()).isNull();
		sp2.restore();
		colladaInterface = internalElement.getCOLLADAInterface();
		assertThat(colladaInterface.isDeleted()).isFalse();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_deleteColladaInterface() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("InterfaceClassLibraries/AutomationMLInterfaceClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument interfaceClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(interfaceClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		AMLCOLLADAInterface colladaInterface = internalElement.createCOLLADAInterface(AMLCOLLADAInterface.RefType.EXPLICIT);
		Savepoint sp1 = session.createSavepoint();
		colladaInterface.delete();
		assertThat(colladaInterface.isDeleted()).isTrue();
		assertThat(internalElement.getCOLLADAInterface()).isNull();
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		colladaInterface = internalElement.getCOLLADAInterface();
		assertThat(colladaInterface.isDeleted()).isFalse();
		sp2.restore();
		assertThat(colladaInterface.isDeleted()).isTrue();

		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_changeName() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("InterfaceClassLibraries/AutomationMLInterfaceClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument interfaceClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(interfaceClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLCOLLADAInterface colladaInterface = internalElement.createCOLLADAInterface(AMLCOLLADAInterface.RefType.EXPLICIT);
		Savepoint sp1 = session.createSavepoint();
		colladaInterface.setName("Representation");
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(colladaInterface.getName()).isNullOrEmpty();
		sp2.restore();
		assertThat(colladaInterface.getName()).isEqualTo("Representation");

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_setURL() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("InterfaceClassLibraries/AutomationMLInterfaceClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument interfaceClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(interfaceClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLCOLLADAInterface colladaInterface = internalElement.createCOLLADAInterface(AMLCOLLADAInterface.RefType.EXPLICIT);
		URI uri = new URI("file://./collada.dae#node");
		Savepoint sp1 = session.createSavepoint();
		colladaInterface.setRefURI(uri);
		assertThat(colladaInterface.getRefURI()).isEqualTo(uri);
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		URI refURI = colladaInterface.getRefURI();
		assertThat(refURI.toString()).isNullOrEmpty();
		sp2.restore();
		assertThat(colladaInterface.getRefURI()).isEqualTo(uri);

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_changeReference() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("InterfaceClassLibraries/AutomationMLInterfaceClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument interfaceClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(interfaceClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLCOLLADAInterface colladaInterface = internalElement.createCOLLADAInterface(AMLCOLLADAInterface.RefType.EXPLICIT);
		Savepoint sp1 = session.createSavepoint();
		colladaInterface.setRefType(AMLCOLLADAInterface.RefType.IMPLICIT);
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(colladaInterface.getRefType()).isEqualTo(AMLCOLLADAInterface.RefType.EXPLICIT);
		sp2.restore();
		assertThat(colladaInterface.getRefType()).isEqualTo(AMLCOLLADAInterface.RefType.IMPLICIT);

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_changeTarget() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("InterfaceClassLibraries/AutomationMLInterfaceClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument interfaceClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(interfaceClassLibDoc);

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLCOLLADAInterface colladaInterface = internalElement.createCOLLADAInterface(AMLCOLLADAInterface.RefType.EXPLICIT);
		Savepoint sp1 = session.createSavepoint();
		colladaInterface.setTarget("./bla");
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(colladaInterface.getTarget()).isNullOrEmpty();
		sp2.restore();
		assertThat(colladaInterface.getTarget()).isEqualTo("./bla");
		

		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_deepDelete() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("InterfaceClassLibraries/AutomationMLInterfaceClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument interfaceClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(interfaceClassLibDoc);
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLCOLLADAInterface interface1 = internalElement.createCOLLADAInterface(AMLCOLLADAInterface.RefType.EXPLICIT);
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		assertThat(internalElement.isDeleted());
		assertThat(interface1.isDeleted());
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		internalElement = instanceHierarchy.getInternalElements().iterator().next();
		assertThat(internalElement.getCOLLADAInterface().getRefType()).isEqualTo(AMLCOLLADAInterface.RefType.EXPLICIT);
		sp2.restore();
		assertThat(internalElement.isDeleted());
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_deepCopy() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("InterfaceClassLibraries/AutomationMLInterfaceClassLib.aml");
		URL url = file.toURI().toURL();
		AMLDocument interfaceClassLibDoc = session.loadAMLDocument(url);

		AMLDocument a = session.createAMLDocument();
		a.addExplicitExternalReference(interfaceClassLibDoc);
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLCOLLADAInterface cOLLADAInterface = internalElement.createCOLLADAInterface(AMLCOLLADAInterface.RefType.EXPLICIT);
		cOLLADAInterface.setName("Test");		
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(instanceHierarchy.validateInternalElementCreate().isOk()).isTrue();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(internalElement);
		assertThat(copy.getCOLLADAInterface().getRefType()).isEqualTo(AMLCOLLADAInterface.RefType.EXPLICIT);
		assertThat(copy.getCOLLADAInterface().getName()).isEqualTo("Test");
			
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		iterator.next();
		copy = iterator.next();
		assertThat(copy.getCOLLADAInterface().getRefType()).isEqualTo(AMLCOLLADAInterface.RefType.EXPLICIT);
		assertThat(copy.getCOLLADAInterface().getName()).isEqualTo("Test");
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_deserialize() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("colladaInterface.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		assertThat(a.getInstanceHierarchies().iterator().next().getInternalElements().iterator().next().getCOLLADAInterface().getName()).isEqualTo("Robot");
		assertThat(a.getInstanceHierarchies().iterator().next().getInternalElements().iterator().next().getCOLLADAInterface().getTarget()).isEqualTo("./baseframe");
	}
}
