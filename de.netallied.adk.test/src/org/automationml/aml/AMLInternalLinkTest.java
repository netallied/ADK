/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Iterator;

import org.automationml.Savepoint;
import org.junit.Test;

public class AMLInternalLinkTest extends AbstractAMLTest {

	@Test
	public void valid_createInternalLink() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);

		Savepoint sp1 = session.createSavepoint();
		AMLInternalLink internalLink = parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);
		assertThat(internalLink).isNotNull();
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(internalLink.isDeleted()).isTrue();

		sp2.restore();
		Iterable<AMLInternalLink> internalLinks = parentInternalElement.getInternalLinks();
		assertThat(internalLinks.iterator().hasNext()).isTrue();
		sp1.delete();
		sp2.delete();
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_createInternalLink() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement parentInternalElement2 = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement2.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);

		parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);
	}

	@Test
	public void invalid_deleteInternalLink() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);
		AMLInternalLink internalLink = parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);

		Savepoint sp1 = session.createSavepoint();
		internalLink.delete();
		assertThat(internalLink.isDeleted()).isTrue();
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		Iterable<AMLInternalLink> internalLinks = parentInternalElement.getInternalLinks();
		assertThat(internalLink.isDeleted()).isTrue();
		internalLink = internalLinks.iterator().next();
		assertThat(internalLink).isNotNull();

		sp2.restore();
		internalLinks = parentInternalElement.getInternalLinks();
		assertThat(internalLinks).isEmpty();
		sp1.delete();
		sp2.delete();
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_deleteExternalInterface() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);
		parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);

		externalInterface.delete();

	}

	@Test
	public void valid_deleteExternalInterface() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);
		AMLInternalLink internalLink = parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);

		internalLink.delete();
		externalInterface.delete();
	}

	@Test
	public void valid_renameLink() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("class");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);
		AMLInternalLink internalLink = parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);

		Savepoint sp1 = session.createSavepoint();
		internalLink.setName("myLink");

		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(internalLink.getName()).isEqualTo("link");

		sp2.restore();
		assertThat(internalLink.getName()).isEqualTo("myLink");

		sp1.delete();
		sp2.delete();

	}
	
	@Test
	public void valid_unlink() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);
		AMLInternalLink internalLink = parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);
		
		Savepoint sp1 = session.createSavepoint();
		internalElement.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(parentInternalElement.getInternalLinks().iterator().hasNext()).isFalse();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isFalse();
		assertThat(internalElement2.getExternalInterfaces().iterator().hasNext()).isTrue();
		
		sp1.restore();
		assertThat(parentInternalElement.getInternalLinks().iterator().hasNext()).isTrue();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(internalElement2.getExternalInterfaces().iterator().hasNext()).isTrue();
		
		sp2.restore();
		assertThat(parentInternalElement.getInternalLinks().iterator().hasNext()).isFalse();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isFalse();
		assertThat(internalElement2.getExternalInterfaces().iterator().hasNext()).isTrue();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlink2() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);
		AMLInternalLink internalLink = parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);
		
		Savepoint sp1 = session.createSavepoint();
		a.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(parentInternalElement.getInternalLinks().iterator().hasNext()).isTrue();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(internalElement2.getExternalInterfaces().iterator().hasNext()).isTrue();
		
		sp1.restore();
		assertThat(parentInternalElement.getInternalLinks().iterator().hasNext()).isTrue();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(internalElement2.getExternalInterfaces().iterator().hasNext()).isTrue();
		
		sp2.restore();
		assertThat(parentInternalElement.getInternalLinks().iterator().hasNext()).isTrue();
		assertThat(internalElement.getExternalInterfaces().iterator().hasNext()).isTrue();
		assertThat(internalElement2.getExternalInterfaces().iterator().hasNext()).isTrue();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);
		AMLInternalLink internalLink = parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(parentInternalElement.validateDeepDelete().isOk()).isTrue();
		parentInternalElement.deepDelete();
		Savepoint sp2 = session.createSavepoint();
		assertThat(parentInternalElement.isDeleted()).isTrue();
		assertThat(internalLink.isDeleted()).isTrue();
		
		sp1.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getInternalLinks().iterator().hasNext()).isTrue();
	
		sp2.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext()).isFalse();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void invalid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);
		AMLInternalLink internalLink = parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(parentInternalElement.validateDeepDelete().isOk()).isTrue();
		parentInternalElement.deepDelete();
		Savepoint sp2 = session.createSavepoint();
		assertThat(parentInternalElement.isDeleted()).isTrue();
		assertThat(internalLink.isDeleted()).isTrue();
		
		sp1.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getInternalLinks().iterator().hasNext()).isTrue();
	
		sp2.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext()).isFalse();
		
		sp1.delete();
		sp2.delete();		
				
	}
	
	@Test
	public void invalid_deepCopy() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);
		AMLInternalLink internalLink = parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(parentInternalElement);
		Savepoint sp2 = session.createSavepoint();
		
		Iterator<AMLInternalElement> iterator = copy.getInternalElements().iterator();
		AMLInternalElement copyChild = iterator.next();
		AMLInternalElement copyChild2 = iterator.next();
		assertThat(copy.getInternalLinks().iterator().hasNext()).isTrue();
		assertThat(copy.getInternalLinks().iterator().next().getRefPartnerSideA()).isEqualTo(copyChild.getExternalInterfaces().iterator().next());
		assertThat(copy.getInternalLinks().iterator().next().getRefPartnerSideB()).isEqualTo(copyChild2.getExternalInterfaces().iterator().next());
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
	
		sp2.restore();
		Iterator<AMLInternalElement> iterator2 = instanceHierarchy.getInternalElements().iterator();
		iterator2.next();
		copy = iterator2.next(); 
		iterator = copy.getInternalElements().iterator();
		copyChild = iterator.next();
		copyChild2 = iterator.next();
		assertThat(copy.getInternalLinks().iterator().hasNext()).isTrue();
		assertThat(copy.getInternalLinks().iterator().next().getRefPartnerSideA()).isEqualTo(copyChild.getExternalInterfaces().iterator().next());
		assertThat(copy.getInternalLinks().iterator().next().getRefPartnerSideB()).isEqualTo(copyChild2.getExternalInterfaces().iterator().next());
		
		sp1.delete();
		sp2.delete();		
				
	}
	
	@Test
	public void invalid_deepCopy2() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement parentInternalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface = internalElement.createExternalInterface(interfaceClass);
		AMLInternalElement internalElement2 = parentInternalElement.createInternalElement();
		AMLExternalInterface externalInterface2 = internalElement2.createExternalInterface(interfaceClass);
		AMLInternalLink internalLink = parentInternalElement.createInternalLink("link", externalInterface, externalInterface2);
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = parentInternalElement.createInternalElement(internalElement);
		Savepoint sp2 = session.createSavepoint();
		
		assertThat(copy.getExternalInterfaces().iterator().next().getInternalLinks().iterator().hasNext()).isFalse();
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
	
		sp2.restore();
		Iterator<AMLInternalElement> iterator2 = parentInternalElement.getInternalElements().iterator();
		iterator2.next();
		iterator2.next();
		copy = iterator2.next(); 
		assertThat(copy.getExternalInterfaces().iterator().next().getInternalLinks().iterator().hasNext()).isFalse();
		
		sp1.delete();
		sp2.delete();		
				
	}
}
