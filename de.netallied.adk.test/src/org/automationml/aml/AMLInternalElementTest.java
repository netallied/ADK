/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.UUID;

import org.automationml.Savepoint;
import org.junit.Test;

public class AMLInternalElementTest extends AbstractAMLTest {

	@Test
	public void valid_createInternalElementUnderInstanceHierarchy() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		UUID id = internalElement.getId();
		Iterator<AMLInternalElement> internalElements = instanceHierarchy.getInternalElements().iterator();

		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement).isNotNull();
		assertThat(internalElements.hasNext());

		sp1.restore();
		internalElements = instanceHierarchy.getInternalElements().iterator();
		assertThat(internalElement.isDeleted());
		assertThat(internalElements.hasNext()).isFalse();

		sp2.restore();
		internalElements = instanceHierarchy.getInternalElements().iterator();
		assertThat(internalElements.hasNext());
		internalElement = internalElements.next();
		assertThat(id).isEqualTo(internalElement.getId());

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_createInternalElementUnderInternalElement() throws Exception {

		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElementBase = instanceHierarchy.createInternalElement();
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement internalElement = internalElementBase.createInternalElement();
		UUID id = internalElement.getId();
		Iterator<AMLInternalElement> internalElements = internalElementBase.getInternalElements().iterator();

		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement).isNotNull();
		assertThat(internalElements.hasNext());

		sp1.restore();
		internalElements = internalElementBase.getInternalElements().iterator();
		assertThat(internalElement.isDeleted());
		assertThat(internalElements.hasNext()).isFalse();

		sp2.restore();
		internalElements = internalElementBase.getInternalElements().iterator();
		assertThat(internalElements.hasNext());
		internalElement = internalElements.next();
		assertThat(id).isEqualTo(internalElement.getId());

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_createInternalElementUnderSystemUnitClass() throws Exception {

		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("SUCL");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("SUC");
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement internalElement = systemUnitClass.createInternalElement();
		UUID id = internalElement.getId();
		Iterator<AMLInternalElement> internalElements = systemUnitClass.getInternalElements().iterator();

		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement).isNotNull();
		assertThat(internalElements.hasNext());

		sp1.restore();
		internalElements = systemUnitClass.getInternalElements().iterator();
		assertThat(internalElement.isDeleted());
		assertThat(internalElements.hasNext()).isFalse();

		sp2.restore();
		internalElements = systemUnitClass.getInternalElements().iterator();
		assertThat(internalElements.hasNext());
		internalElement = internalElements.next();
		assertThat(id).isEqualTo(internalElement.getId());

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_deleteInternalElementUnderInstanceHierarchy() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		UUID id = internalElement.getId();

		Savepoint sp1 = session.createSavepoint();
		internalElement.delete();
		Iterator<AMLInternalElement> internalElements = instanceHierarchy.getInternalElements().iterator();
		assertThat(internalElement.isDeleted());
		assertThat(internalElements.hasNext()).isFalse();

		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		internalElements = instanceHierarchy.getInternalElements().iterator();
		assertThat(internalElements.hasNext());
		internalElement = internalElements.next();
		assertThat(internalElement.isDeleted()).isFalse();
		assertThat(internalElement.getId().equals(id));

		sp2.restore();
		internalElements = instanceHierarchy.getInternalElements().iterator();
		assertThat(internalElement.isDeleted());
		assertThat(internalElements.hasNext()).isFalse();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_deleteInternalElementUnderInternalElement() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElementBase = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = internalElementBase.createInternalElement();
		UUID id = internalElement.getId();

		Savepoint sp1 = session.createSavepoint();
		internalElement.delete();
		Iterator<AMLInternalElement> internalElements = internalElementBase.getInternalElements().iterator();
		assertThat(internalElement.isDeleted());
		assertThat(internalElements.hasNext()).isFalse();

		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		internalElements = internalElementBase.getInternalElements().iterator();
		assertThat(internalElements.hasNext());
		internalElement = internalElements.next();
		assertThat(internalElement.isDeleted()).isFalse();
		assertThat(internalElement.getId().equals(id));

		sp2.restore();
		internalElements = internalElementBase.getInternalElements().iterator();
		assertThat(internalElement.isDeleted());
		assertThat(internalElements.hasNext()).isFalse();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_deleteInternalElementUnderSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("SUCL");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("SUC");
		AMLInternalElement internalElement = systemUnitClass.createInternalElement();
		UUID id = internalElement.getId();

		Savepoint sp1 = session.createSavepoint();
		internalElement.delete();
		Iterator<AMLInternalElement> internalElements = systemUnitClass.getInternalElements().iterator();
		assertThat(internalElement.isDeleted());
		assertThat(internalElements.hasNext()).isFalse();

		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		internalElements = systemUnitClass.getInternalElements().iterator();
		assertThat(internalElements.hasNext());
		internalElement = internalElements.next();
		assertThat(internalElement.isDeleted()).isFalse();
		assertThat(internalElement.getId().equals(id));

		sp2.restore();
		internalElements = systemUnitClass.getInternalElements().iterator();
		assertThat(internalElement.isDeleted());
		assertThat(internalElements.hasNext()).isFalse();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_changeNameInternalElement() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setName("xxx");
		Savepoint sp1 = session.createSavepoint();

		internalElement.setName("yyy");
		Savepoint sp2 = session.createSavepoint();

		sp1.restore();
		assertThat(internalElement.getName().equals("xxx"));

		sp2.restore();
		assertThat(internalElement.getName().equals("yyy"));

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_setSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		Savepoint sp1 = session.createSavepoint();
		internalElement.setBaseSystemUnitClass(baseClass);

		Savepoint sp2 = session.createSavepoint();

		assertThat(internalElement.getBaseSystemUnitClass() == baseClass);

		sp1.restore();
		assertThat(internalElement.getBaseSystemUnitClass()).isNull();

		sp2.restore();
		assertThat(internalElement.getBaseSystemUnitClass() == baseClass);

		sp1.delete();
		sp2.delete();
	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setSelfIncludingSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");
		AMLInternalElement internalElement = baseClass.createInternalElement();
		internalElement.setBaseSystemUnitClass(baseClass);

	}

	@Test(expected = AMLValidationException.class)
	public void invalid_setSelfIncludingSystemUnitClass2() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");
		AMLSystemUnitClass baseClass2 = baseClass.createSystemUnitClass("baseclass");
		AMLInternalElement internalElement = baseClass2.createInternalElement();
		AMLInternalElement internalElement2 = internalElement.createInternalElement();
		internalElement2.setBaseSystemUnitClass(baseClass2);

	}

	@Test
	public void valid_unsetSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setBaseSystemUnitClass(baseClass);

		Savepoint sp1 = session.createSavepoint();
		internalElement.unsetBaseSystemUnitClass();

		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement.getBaseSystemUnitClass()).isNull();

		sp1.restore();
		assertThat(internalElement.getBaseSystemUnitClass() == baseClass);

		sp2.restore();
		assertThat(internalElement.getBaseSystemUnitClass()).isNull();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void invalid_twoInternalElementsWithSameID() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		UUID id = UUID.randomUUID();
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement(id);

		Savepoint sp1 = session.createSavepoint();
		try {
			instanceHierarchy.createInternalElement(id);
			fail();
		} catch (AMLValidationException ex) {
			
		} catch (Exception e) {
			fail();
		}
		
		internalElement.delete();
		
		AMLInternalElement internalElement2 = instanceHierarchy.createInternalElement(id);
		
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(internalElement2).isNotNull();
		assertThat(internalElement.isDeleted());

		sp2.restore();
		assertThat(internalElement2.isDeleted());
		assertThat(internalElement).isNotNull();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void invalid_twoInternalElementsWithSameIDInDifferentDocuments() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		b.addExplicitExternalReference(a);
		AMLInstanceHierarchy instanceHierarchyA = a.createInstanceHierarchy("hierarchy");
				
		AMLInstanceHierarchy instanceHierarchyB = b.createInstanceHierarchy("hierarchy");
		AMLSystemUnitClassLibrary lib1 = a.createSystemUnitClassLibrary("lib1");
		AMLSystemUnitClass baseClass = lib1.createSystemUnitClass("baseclass");	
		
		
		UUID id = UUID.randomUUID();
		AMLInternalElement internalElement = instanceHierarchyB.createInternalElement(id);
		internalElement.setBaseSystemUnitClass(baseClass);

		Savepoint sp1 = session.createSavepoint();
		try {
			instanceHierarchyA.createInternalElement(id);
			fail();
		} catch (AMLValidationException ex) {
			
		} catch (Exception e) {
			fail();
		}
		
		internalElement.delete();
		
		AMLInternalElement internalElement2 = instanceHierarchyA.createInternalElement(id);
		
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(internalElement2).isNotNull();
		assertThat(internalElement.isDeleted());

		sp2.restore();
		assertThat(internalElement2.isDeleted());
		assertThat(internalElement).isNotNull();

		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_reparentOverDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInstanceHierarchy instanceHierarchy2 = b.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy2.createInternalElement();
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(internalElement.validateReparent(instanceHierarchy2, instanceHierarchy, null, null).isOk() == true);
		internalElement.reparent(instanceHierarchy2, instanceHierarchy,  null, null);
		
		Savepoint sp2 = session.createSavepoint();

		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext());
		assertThat(instanceHierarchy.getInternalElements().iterator().next().equals(internalElement));
		assertThat(instanceHierarchy2.getInternalElements().iterator().hasNext() == false);
		
		sp1.restore();
		assertThat(instanceHierarchy2.getInternalElements().iterator().hasNext());
		assertThat(instanceHierarchy2.getInternalElements().iterator().next().equals(internalElement));
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext() == false);
		
		sp2.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext());
		assertThat(instanceHierarchy.getInternalElements().iterator().next().equals(internalElement));
		assertThat(instanceHierarchy2.getInternalElements().iterator().hasNext() == false);
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentAfter() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement2 = instanceHierarchy.createInternalElement();
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(internalElement.validateReparent(instanceHierarchy, instanceHierarchy, null, internalElement2).isOk() == true);
		internalElement.reparent(instanceHierarchy, instanceHierarchy,  null, internalElement2);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement2));
		assertThat(iterator.next().equals(internalElement));
		
		sp1.restore();
		iterator = instanceHierarchy.getInternalElements().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement));
		assertThat(iterator.next().equals(internalElement2));
		
		sp2.restore();
		iterator = instanceHierarchy.getInternalElements().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement2));
		assertThat(iterator.next().equals(internalElement));
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void valid_reparentBefore() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement2 = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(internalElement.validateReparent(instanceHierarchy, instanceHierarchy, internalElement2, null).isOk() == true);
		internalElement.reparent(instanceHierarchy, instanceHierarchy, internalElement2,  null);
		
		Savepoint sp2 = session.createSavepoint();

		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement2));
		assertThat(iterator.next().equals(internalElement));
		
		sp1.restore();
		iterator = instanceHierarchy.getInternalElements().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement));
		assertThat(iterator.next().equals(internalElement2));
		
		sp2.restore();
		iterator = instanceHierarchy.getInternalElements().iterator();
		assertThat(iterator.hasNext());
		assertThat(iterator.next().equals(internalElement2));
		assertThat(iterator.next().equals(internalElement));
		
		sp2.delete();
		sp1.delete();	
	}
	
	@Test
	public void invalid_reparentSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLSystemUnitClassLibrary interfaceClassLibrary = b.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass interfaceClass = interfaceClassLibrary.createSystemUnitClass("ic");
		
		AMLInstanceHierarchy instanceHierarchy = b.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setBaseSystemUnitClass(interfaceClass);
		
		assertThat(instanceHierarchy.validateReparent(b, a, null, null).isOk()).isTrue();
	}
	
	@Test
	public void valid_reparentSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLSystemUnitClassLibrary interfaceClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass interfaceClass = interfaceClassLibrary.createSystemUnitClass("ic");
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setBaseSystemUnitClass(interfaceClass);
		
		assertThat(instanceHierarchy.validateReparent(a, b, null, null).isAnyOperationNotPermitted()).isTrue();
	}
	
	@Test
	public void invalid_reparentParentToChildSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();		
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("icl");
		AMLInternalElement parent = instanceHierarchy.createInternalElement();
		AMLInternalElement child = parent.createInternalElement();
		
		assertThat(parent.validateReparent(instanceHierarchy, child, null, null).isAnyOperationNotPermitted()).isTrue();
		
	}
	
	@Test
	public void valid_unlink() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setBaseSystemUnitClass(systemUnitClass);
		
		Savepoint sp1 = session.createSavepoint();
		internalElement.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement.getBaseSystemUnitClass()).isNull();
		
		sp1.restore();
		assertThat(internalElement.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp2.restore();
		assertThat(internalElement.getBaseSystemUnitClass()).isNull();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlink2() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setBaseSystemUnitClass(systemUnitClass);
		
		Savepoint sp1 = session.createSavepoint();
		a.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp1.restore();
		assertThat(internalElement.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp2.restore();
		assertThat(internalElement.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp1.delete();
		sp2.delete();		
	}

	@Test
	public void valid_unlink3() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("icl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement2 = internalElement.createInternalElement();
		internalElement2.setBaseSystemUnitClass(systemUnitClass);
		
		Savepoint sp1 = session.createSavepoint();
		internalElement.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement2.getBaseSystemUnitClass()).isNull();
		
		sp1.restore();
		assertThat(internalElement2.getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp2.restore();
		assertThat(internalElement2.getBaseSystemUnitClass()).isNull();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test 
	public void valid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement2 = internalElement.createInternalElement();
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement.isDeleted()).isTrue();
		assertThat(internalElement2.isDeleted()).isTrue();
		
		sp1.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext()).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getInternalElements().iterator().hasNext()).isTrue();

		sp2.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext()).isFalse();
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_deepDeleteNotUnlinked() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement2 = internalElement.createInternalElement();
		internalElement2.setBaseSystemUnitClass(systemUnitClass);
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement.isDeleted()).isTrue();
		assertThat(internalElement2.isDeleted()).isTrue();
		
		sp1.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext()).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getInternalElements().iterator().hasNext()).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getInternalElements().iterator().next().getBaseSystemUnitClass()).isEqualTo(systemUnitClass);

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
		AMLInternalElement internalElement2 = internalElement.createInternalElement();
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(internalElement);
		
		Savepoint sp2 = session.createSavepoint();
		assertThat(instanceHierarchy.getInternalElementsCount()).isEqualTo(2);
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
			
		sp2.restore();
		assertThat(instanceHierarchy.getInternalElementsCount()).isEqualTo(2);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_deepCopy_baseSystemUnitClassNotCopied() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement2 = internalElement.createInternalElement();
		internalElement2.setBaseSystemUnitClass(systemUnitClass);
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(internalElement);
		UUID id = copy.getId();
		Savepoint sp2 = session.createSavepoint();
		assertThat(instanceHierarchy.getInternalElementsCount()).isEqualTo(2);
		assertThat(copy.getInternalElementsCount()).isEqualTo(1);
		assertThat(copy.getInternalElements().iterator().next().getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		sp1.restore();
		assertThat(instanceHierarchy.getInternalElementsCount()).isEqualTo(1);
			
		sp2.restore();
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		iterator.next();
		AMLInternalElement next = iterator.next();
		assertThat(instanceHierarchy.getInternalElementsCount()).isEqualTo(2);
		assertThat(next.getInternalElementsCount()).isEqualTo(1);
		assertThat(next.getInternalElements().iterator().next().getBaseSystemUnitClass()).isEqualTo(systemUnitClass);
		
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_deepCopy_AvoidStackOverflow() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement2 = internalElement.createInternalElement();
		internalElement2.setBaseSystemUnitClass(systemUnitClass);
		AMLInternalElement copy = internalElement.createInternalElement(internalElement);
	}
	
	@Test
	public void valid_createInternalElementBySystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement(systemUnitClass);
		
		assertThat(internalElement.getBaseSystemUnitClass()).isSameAs(systemUnitClass);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		
		assertThat(internalElement.isDeleted()).isTrue();
		
		sp2.restore();
		
		assertThat(instanceHierarchy.getInternalElementsCount() == 1).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getBaseSystemUnitClass()).isSameAs(systemUnitClass);
		
		sp1.delete();
		sp2.delete();
		
	}
	
	@Test
	public void valid_createInternalElementBySystemUnitClassWithAttributesRolesAndInterfaces() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLInterfaceClassLibrary interfaceClassLibrary = a.createInterfaceClassLibrary("icl");
		AMLInterfaceClass interfaceClass = interfaceClassLibrary.createInterfaceClass("ic");
		
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("rcl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("rc");
		
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		AMLExternalInterface externalInterface = systemUnitClass.createExternalInterface(interfaceClass);
		AMLSupportedRoleClass supportedRoleClass = systemUnitClass.createSupportedRoleClass(roleClass);
		AMLAttribute attribute = systemUnitClass.createAttribute("attr");
	
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement(systemUnitClass);
		
		assertThat(internalElement.getBaseSystemUnitClass()).isSameAs(systemUnitClass);
		assertThat(internalElement.getExternalInterfacesCount() == 1).isTrue();
		assertThat(internalElement.getExternalInterfaces().iterator().next().getInterfaceClass()).isSameAs(interfaceClass);
		assertThat(internalElement.getSupportedRoleClassesCount() == 1).isTrue();
		assertThat(internalElement.getSupportedRoleClasses().iterator().next().getRoleClass()).isSameAs(roleClass);
		assertThat(internalElement.getAttributesCount() == 1).isTrue();
		assertThat(internalElement.getAttributes().iterator().next().getName()).isEqualTo(attribute.getName());
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		
		assertThat(internalElement.isDeleted()).isTrue();
		
		sp2.restore();
		
		assertThat(instanceHierarchy.getInternalElementsCount() == 1).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getBaseSystemUnitClass()).isSameAs(systemUnitClass);
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getExternalInterfacesCount() == 1).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getExternalInterfaces().iterator().next().getInterfaceClass()).isSameAs(interfaceClass);
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getSupportedRoleClassesCount() == 1).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getSupportedRoleClasses().iterator().next().getRoleClass()).isSameAs(roleClass);
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getAttributesCount() == 1).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getAttributes().iterator().next().getName()).isEqualTo(attribute.getName());
		
		
		sp1.delete();
		sp2.delete();
		
	}
	
	@Test
	public void valid_createInternalElementBySystemUnitClassWithInternalElements() throws Exception {
		AMLDocument a = session.createAMLDocument();
				
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		AMLInternalElement sucInternalElement = systemUnitClass.createInternalElement();
		sucInternalElement.setName("inner");
			
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement(systemUnitClass);
		
		assertThat(internalElement.getBaseSystemUnitClass()).isSameAs(systemUnitClass);
		assertThat(internalElement.getInternalElementsCount() == 1).isTrue();
		assertThat(internalElement.getInternalElements().iterator().next().getName()).isEqualTo(sucInternalElement.getName());
		
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		
		assertThat(internalElement.isDeleted()).isTrue();
		
		sp2.restore();
		
		assertThat(instanceHierarchy.getInternalElementsCount() == 1).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getBaseSystemUnitClass()).isSameAs(systemUnitClass);
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getInternalElementsCount() == 1).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getInternalElements().iterator().next().getName()).isEqualTo(sucInternalElement.getName());
		
		
		
		sp1.delete();
		sp2.delete();
		
	}
	
	@Test
	public void invalid_createInternalElementBySystemUnitClassFromUnknwonDocument() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		
		AMLDocument b = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = b.createInstanceHierarchy("hierachy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		Savepoint sp1 = session.createSavepoint();
		
		AMLInternalElement subElement = internalElement.createInternalElement();
		try {
			subElement.setBaseSystemUnitClass(systemUnitClass);
			throw new Exception();
		} catch (AMLValidationException ex) {
		}
		
		sp1.restore();
		assertThat(subElement.isDeleted()).isTrue();
		assertThat(internalElement.getInternalElementsCount()).isEqualTo(0);
		sp1.delete();

	}
	
	@Test
	public void invalid_createInternalElementBySystemUnitClassFromUnknwonDocument2() throws Exception {
		
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		
		AMLDocument b = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = b.createInstanceHierarchy("hierachy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		Savepoint sp1 = session.createSavepoint();
		
		AMLInternalElement subElement = null;
		try {
			subElement = internalElement.createInternalElement(systemUnitClass);		
			throw new Exception();
		} catch (AMLValidationException ex) {
		}
		
		sp1.restore();
		assertThat(subElement).isNull();
		assertThat(internalElement.getInternalElementsCount()).isEqualTo(0);
		sp1.delete();

	}
		
	// TODO: Mirror objects
}
