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

import org.automationml.Savepoint;
import org.automationml.aml.AMLAttribute.Constraint;
import org.automationml.aml.AMLAttribute.NominalScaledConstraint;
import org.automationml.aml.AMLAttribute.OrdinalScaledConstraint;
import org.automationml.aml.AMLAttribute.UnknownConstraint;
import org.fest.assertions.Fail;
import org.junit.Test;

public class AMLAttributeTest extends AbstractAMLTest {

	private TestFileLocator testFileLocator = new TestFileLocator(getClass());
	
	@Test
	public void valid_createAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass = lib1.createInterfaceClass("baseclass");
		
		String attributeName = "attribute";
		String attributeDescription = "description";
		String attributeDatatype = "xs:string";
		String attributeUnit = "mm";
		String defaultValue = "default";
		String attributeValue = "value";
		AMLAttribute attribute = interfaceClass.createAttribute(attributeName);
		attribute.setDescription(attributeDescription);
		attribute.setDataType(attributeDatatype);
		attribute.setUnit(attributeUnit);
		attribute.setDefaultValue(defaultValue);
		attribute.setValue(attributeValue);
		
		assertThat(attribute.getName().equals(attributeName));
		assertThat(attribute.getDescription().equals(attributeDescription));
		assertThat(attribute.getDataType().equals(attributeDatatype));
		assertThat(attribute.getUnit().equals(attributeUnit));
		assertThat(attribute.getDefaultValue().equals(defaultValue));
		assertThat(attribute.getValue().equals(attributeValue));
	}
	
	@Test(expected = AMLValidationException.class)
	public void invalid_createTwoAttributesWithSameName() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass = lib1.createInterfaceClass("baseclass");
		
		String attributeName = "attribute";
		interfaceClass.createAttribute(attributeName);
		interfaceClass.createAttribute(attributeName);		
	}
	
	@Test
	public void valid_createTwoAttributesHierarchical() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass = lib1.createInterfaceClass("baseclass");
		
		String attributeName = "attribute";
		AMLAttribute attribute = interfaceClass.createAttribute(attributeName);
		attribute = attribute.createAttribute(attributeName);
		assertThat(attribute.getName().equals(attributeName));		
	}
	
	@Test
	public void valid_deleteAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass = lib1.createInterfaceClass("baseclass");
		
		String attributeName = "attribute";
		AMLAttribute attribute = interfaceClass.createAttribute(attributeName);
		attribute.delete();
		
		assertThat(interfaceClass.getAttribute(attributeName)).isNull();
	}
	
	@Test
	public void valid_deleteAttributeFromAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass = lib1.createInterfaceClass("baseclass");
		
		String attributeName = "attribute";
		AMLAttribute attribute1 = interfaceClass.createAttribute(attributeName);
		AMLAttribute attribute2 = attribute1.createAttribute(attributeName);
		attribute2.delete();
		
		assertThat(interfaceClass.getAttribute(attributeName)).isNotNull();
		assertThat(attribute1.getAttribute(attributeName)).isNull();
	}
	
	@Test(expected = AMLValidationException.class)
	public void invalid_deleteAttributeWithChildren() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass = lib1.createInterfaceClass("baseclass");
		
		String attributeName = "attribute";
		AMLAttribute attribute1 = interfaceClass.createAttribute(attributeName);
		attribute1.createAttribute(attributeName);
		attribute1.delete();
	}
	
	@Test
	public void valid_setName_savepoints() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = lib.createInterfaceClass("class");
		AMLAttribute attribute = interfaceClass.createAttribute( "attribute");
		
		Savepoint sp1 = session.createSavepoint();

		attachDocumentChangeListener(a);
		attribute.setName("xxx");
		
		resetChangeListener();
		attribute.setName("yyy");
		attribute.setValue("val");
		attribute.setDescription("description");
		attribute.setDefaultValue("defaultValue");
		attribute.setDataType("dataType");
		attribute.setUnit("unit");
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(attribute.getName()).isEqualTo("attribute");
		assertThat(attribute.getValue()).isNullOrEmpty();
		assertThat(attribute.getDefaultValue()).isNullOrEmpty();
		assertThat(attribute.getUnit()).isNullOrEmpty();
		assertThat(attribute.getDataType()).isNullOrEmpty();
		assertThat(attribute.getDescription()).isNullOrEmpty();
		
	
		sp2.restore();
		assertThat(attribute.getName()).isEqualTo("yyy");
		assertThat(attribute.getValue()).isEqualTo("val");
		assertThat(attribute.getDefaultValue()).isEqualTo("defaultValue");
		assertThat(attribute.getUnit()).isEqualTo("unit");
		assertThat(attribute.getDataType()).isEqualTo("dataType");
		assertThat(attribute.getDescription()).isEqualTo("description");
	
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_delete_savepoints() throws Exception {
		AMLDocument a = session.createAMLDocument();
	
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = lib.createInterfaceClass("class");
		AMLAttribute attribute = interfaceClass.createAttribute( "attribute");
		attribute.setDescription("description");
		attribute.setDataType("xs:string");
		attribute.setUnit("unit");
		attribute.setDefaultValue("defaultValue");
		attribute.setValue("attributeValue");
		
		Savepoint sp = session.createSavepoint();

		attachDocumentChangeListener(a);
		attribute.delete();
		assertThat(documentChangeListener.result.toString()).contains("begin\nDeleting AMLAttributeImpl\nend\n");
		assertThat(interfaceClass.getAttribute("attribute")).isNull();
		sp.restore();

		AMLAttribute restoredAttribute = interfaceClass.getAttribute("attribute");
		assertThat(restoredAttribute).isNotNull();
		assertThat(restoredAttribute.getName()).isEqualTo("attribute");
		assertThat(restoredAttribute.getDescription()).isEqualTo("description");
		assertThat(restoredAttribute.getDataType()).isEqualTo("xs:string");
		assertThat(restoredAttribute.getUnit()).isEqualTo("unit");
		assertThat(restoredAttribute.getDefaultValue()).isEqualTo("defaultValue");
		assertThat(restoredAttribute.getValue()).isEqualTo("attributeValue");
		assertThat(restoredAttribute == attribute);
	
		sp.delete();
	}
	
	@Test (expected = AMLValidationException.class)
	public void invalid_renameAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLInterfaceClassLibrary lib = a.createInterfaceClassLibrary("lib");
		AMLInterfaceClass interfaceClass = lib.createInterfaceClass("class");
		interfaceClass.createAttribute( "attribute1");
		AMLAttribute attribute2 = interfaceClass.createAttribute( "attribute2");
		attribute2.setName("attribute1");
	}
	
	@Test (expected = AMLValidationException.class)
	public void invalid_deleteInterfaceClassWithAttributes() throws Exception {
		AMLDocument a = session.createAMLDocument();
		
		AMLInterfaceClassLibrary lib1 = a.createInterfaceClassLibrary("lib1");
		AMLInterfaceClass interfaceClass = lib1.createInterfaceClass("baseclass");
		interfaceClass.createAttribute("attribute");
		
		interfaceClass.delete();
	}
	
	@Test
	public void valid_addFrameAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();

		Savepoint sp1 = session.createSavepoint();
		AMLFrameAttribute frame = internalElement.getFrameAttribute();

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(frame.isDeleted()).isTrue();
		sp2.restore();
		frame = internalElement.getFrameAttribute();
		assertThat(frame.isDeleted()).isFalse();
		assertThat(frame.getX()).isEqualTo(0);

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_removeFrameAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLFrameAttribute frame = internalElement.getFrameAttribute();
		frame.setX(0.5);
		Savepoint sp1 = session.createSavepoint();
		frame.delete();
		assertThat(frame.isDeleted()).isTrue();
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		frame = internalElement.getFrameAttribute();
		assertThat(frame.isDeleted()).isFalse();
		assertThat(frame.getX()).isEqualTo(0.5);
		sp2.restore();
		assertThat(frame.isDeleted()).isTrue();

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_changeFrameAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLFrameAttribute frame = internalElement.getFrameAttribute();
		Savepoint sp1 = session.createSavepoint();
		frame.setX(0.5);
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(frame.getX()).isEqualTo(0);
		sp2.restore();
		assertThat(frame.getX()).isEqualTo(0.5);
		sp1.delete();
		sp2.delete();
	}
	
	@Test (expected = AMLValidationException.class)
	public void invalid_createAttributeOfNameFrame() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.createAttribute("Frame");
	}
	
	@Test (expected = AMLValidationException.class)
	public void invalid_createAttributeOfNameFrameSystemUnitClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLSystemUnitClassLibrary sucl = a.createSystemUnitClassLibrary("Lib");
		AMLSystemUnitClass suc = sucl.createSystemUnitClass("class");
		suc.createAttribute("Frame");
	}
	
	@Test
	public void valid_29_frameAttribute() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("29_frameAttribute.aml");
		URL url = file.toURI().toURL();

		try {
			AMLDocument document = session.loadAMLDocument(url);
			AMLInstanceHierarchy instanceHierarchy = document.getInstanceHierarchies().iterator().next();
			AMLInternalElement internalElement = instanceHierarchy.getInternalElements().iterator().next();
			AMLFrameAttribute frameAttribute = internalElement.getFrameAttribute();
			assertThat(frameAttribute.getX()).isEqualTo(1);
			assertThat(frameAttribute.getY()).isEqualTo(2);
			assertThat(frameAttribute.getZ()).isEqualTo(3);
			assertThat(frameAttribute.getRX()).isEqualTo(4);
			assertThat(frameAttribute.getRY()).isEqualTo(5);
			assertThat(frameAttribute.getRZ()).isEqualTo(6);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
		}
	}


	@Test
	public void valid_createAttributeThencreateAttributeAndRenameThenUndoThenUndo() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		Savepoint undo1 = session.createSavepoint();
		internalElement.createAttribute("attr");
		Savepoint redo1 = session.createSavepoint();

		Savepoint undo2 = session.createSavepoint();
		AMLAttribute attribute2 = internalElement.createAttribute("attr2");
		attribute2.setValue("value2");
		Savepoint redo2 = session.createSavepoint();

		undo2.restore();
		
		undo1.restore();
		
		undo2.delete();
		redo2.delete();
		undo1.delete();
		redo1.delete();
	}

	@Test
	public void valid_reparent() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLInternalElement internalElement2 = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(attribute.validateReparent(internalElement, internalElement2, null, null).isOk()).isTrue();
		attribute.reparent(internalElement, internalElement2, null, null);
		assertThat(internalElement.getAttributesCount()).isEqualTo(0);
		assertThat(internalElement2.getAttributesCount()).isEqualTo(1);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(internalElement.getAttributesCount()).isEqualTo(1);
		assertThat(internalElement2.getAttributesCount()).isEqualTo(0);
		
		sp2.restore();
		assertThat(internalElement.getAttributesCount()).isEqualTo(0);
		assertThat(internalElement2.getAttributesCount()).isEqualTo(1);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_reparentOverDocuments() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();		
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLInstanceHierarchy instanceHierarchy2 = b.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement2 = instanceHierarchy2.createInternalElement();
	
		Savepoint sp1 = session.createSavepoint();
		assertThat(attribute.validateReparent(internalElement, internalElement2, null, null).isOk()).isTrue();
		attribute.reparent(internalElement, internalElement2, null, null);
		assertThat(internalElement.getAttributesCount()).isEqualTo(0);
		assertThat(internalElement2.getAttributesCount()).isEqualTo(1);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(internalElement.getAttributesCount()).isEqualTo(1);
		assertThat(internalElement2.getAttributesCount()).isEqualTo(0);
		
		sp2.restore();
		assertThat(internalElement.getAttributesCount()).isEqualTo(0);
		assertThat(internalElement2.getAttributesCount()).isEqualTo(1);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_reparentAfter() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute attribute2 = internalElement.createAttribute("attr2");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(attribute.validateReparent(internalElement, internalElement, null, attribute2).isOk()).isTrue();
		attribute.reparent(internalElement, internalElement, null, attribute2);
		assertThat(internalElement.getAttributes().iterator().next()).isEqualTo(attribute2);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(internalElement.getAttributes().iterator().next()).isEqualTo(attribute);
		
		sp2.restore();
		assertThat(internalElement.getAttributes().iterator().next()).isEqualTo(attribute2);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_reparentBefore() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute attribute2 = internalElement.createAttribute("attr2");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(attribute2.validateReparent(internalElement, internalElement, attribute, null).isOk()).isTrue();
		attribute2.reparent(internalElement, internalElement, attribute, null);
		assertThat(internalElement.getAttributes().iterator().next()).isEqualTo(attribute2);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(internalElement.getAttributes().iterator().next()).isEqualTo(attribute);
		
		sp2.restore();
		assertThat(internalElement.getAttributes().iterator().next()).isEqualTo(attribute2);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute attribute2 = attribute.createAttribute("attr2");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		assertThat(internalElement.isDeleted());
		assertThat(attribute.isDeleted());
		assertThat(attribute2.isDeleted());
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		internalElement = instanceHierarchy.getInternalElements().iterator().next();
		assertThat(internalElement.getAttribute("attr")).isNotNull();
		assertThat(internalElement.getAttribute("attr").getAttribute("attr2")).isNotNull();
		
		sp2.restore();
		assertThat(internalElement.isDeleted());
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_deepCopy() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute attribute2 = attribute.createAttribute("attr2");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateCreateAttribute("attr3").isOk()).isTrue();
		AMLAttribute copy = internalElement.createAttribute("attr3", attribute);
		assertThat(internalElement.getAttributesCount()).isEqualTo(2);		
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLAttribute> iterator = internalElement.getAttributes().iterator();
		iterator.next();
		copy = iterator.next();
		assertThat(copy.getName()).isEqualTo("attr3");
		assertThat(copy.getAttributesCount()).isEqualTo(1);
		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_addRefSemantic() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		
		Savepoint sp1 = session.createSavepoint();
		String refSemantic = "blabla123";
		assertThat(attribute.validateAddRefSemantic(refSemantic).isOk()).isTrue();
		attribute.addRefSemantic(refSemantic);
		assertThat(attribute.getRefSemantics().iterator().next()).isEqualTo(refSemantic);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(attribute.getRefSemantics().iterator().hasNext()).isFalse();
		
		sp2.restore();
		assertThat(attribute.getRefSemantics().iterator().next()).isEqualTo(refSemantic);
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void invalid_addRefSemantic() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		String refSemantic = "blabla123";
		attribute.addRefSemantic(refSemantic);
			
		assertThat(attribute.validateAddRefSemantic(refSemantic).isAnyOperationNotPermitted()).isTrue();
		try {
			attribute.addRefSemantic(refSemantic);
			Fail.fail();
		} catch(AMLValidationException ex){
		}		
	}
	
	@Test
	public void valid_removeRefSemantic() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		String refSemantic = "blabla123";
		attribute.addRefSemantic(refSemantic);
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(attribute.validateRemoveRefSemantic(refSemantic).isOk()).isTrue();
		attribute.removeRefSemantic(refSemantic);
		assertThat(attribute.getRefSemantics().iterator().hasNext()).isFalse();
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(attribute.getRefSemantics().iterator().next()).isEqualTo(refSemantic);
				
		sp2.restore();
		assertThat(attribute.getRefSemantics().iterator().hasNext()).isFalse();
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_addNominalScaledConstraint() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		
		Savepoint sp1 = session.createSavepoint();
		AMLAttribute.NominalScaledConstraint constraint = attribute.createNominalScaledConstraint("constraint");
		constraint.addRequiredValue("5");
		assertThat(attribute.getConstraints().iterator().next()).isEqualTo(constraint);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(attribute.getConstraints().iterator().hasNext()).isFalse();
		
		sp2.restore();
		assertThat(attribute.getConstraints().iterator().next().getName()).isEqualTo("constraint");
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_addOrdinalScaledConstraint() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		
		Savepoint sp1 = session.createSavepoint();
		AMLAttribute.OrdinalScaledConstraint constraint = attribute.createOrdinalScaledConstraint("constraint");
		constraint.setRequiredValue("5");
		constraint.setRequiredMaxValue("10");
//		constraint.setRequiredMinValue("1");
		
		assertThat(attribute.getConstraints().iterator().next()).isEqualTo(constraint);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(attribute.getConstraints().iterator().hasNext()).isFalse();
		
		sp2.restore();
		assertThat(attribute.getConstraints().iterator().next().getName()).isEqualTo("constraint");
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_addUnknownConstraint() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		
		Savepoint sp1 = session.createSavepoint();
		AMLAttribute.UnknownConstraint constraint = attribute.createUnknownConstraint("constraint");
		constraint.setRequirement("5");
		
		assertThat(attribute.getConstraints().iterator().hasNext()).isTrue();
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(attribute.getConstraints().iterator().hasNext()).isFalse();
		
		sp2.restore();
		assertThat(attribute.getConstraints().iterator().hasNext()).isTrue();
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_removeConstraint() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute.UnknownConstraint constraint = attribute.createUnknownConstraint("constraint");
		constraint.setRequirement("5");
		
		Savepoint sp1 = session.createSavepoint();
		constraint.delete();
		assertThat(attribute.getConstraints().iterator().hasNext()).isFalse();
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(attribute.getConstraints().iterator().next().getName()).isEqualTo("constraint");
		
		sp2.restore();
		assertThat(attribute.getConstraints().iterator().hasNext()).isFalse();
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valis_changeConstraint() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute.UnknownConstraint constraint = attribute.createUnknownConstraint("constraint");
		constraint.setRequirement("5");
		
		Savepoint sp1 = session.createSavepoint();
		constraint.setRequirement("6");
		assertThat(constraint.getRequirement()).isEqualTo("6");
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(constraint.getRequirement()).isEqualTo("5");
		
		sp2.restore();
		assertThat(constraint.getRequirement()).isEqualTo("6");
		
		sp1.delete();
		sp2.delete();
		
	}
	
	@Test
	public void invalid_addConstraint() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute.UnknownConstraint constraint = attribute.createUnknownConstraint("constraint");
		constraint.setRequirement("5");
		
		assertThat(attribute.validateCreateUnknownConstraint("constraint").isAnyOperationNotPermitted()).isTrue();
		try {
			attribute.createUnknownConstraint("constraint");
			Fail.fail();
		} catch (AMLValidationException ex) {
		}
	}
	
	@Test
	public void valid_deserializeRefSemantic() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("refsemantic.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		assertThat(a.getInstanceHierarchies().iterator().next().getInternalElements().iterator().next().getAttributes().iterator().next().getRefSemantics().iterator().next()).isEqualTo("dfghdfgh");
	}
	
	@Test
	public void valid_deserializeUnknownConstraint() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("unknownconstraint.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		Constraint constraint = a.getInstanceHierarchies().iterator().next().getInternalElements().iterator().next().getAttributes().iterator().next().getConstraints().iterator().next();
		assertThat(constraint).isInstanceOf(UnknownConstraint.class);
		assertThat(constraint.getName()).isEqualTo("a");
		assertThat(((UnknownConstraint)constraint).getRequirement()).isEqualTo("b");
	}
	
	@Test
	public void valid_deserializeNominalScaledConstraint() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("nominalscaledconstraint.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		Constraint constraint = a.getInstanceHierarchies().iterator().next().getInternalElements().iterator().next().getAttributes().iterator().next().getConstraints().iterator().next();
		assertThat(constraint).isInstanceOf(NominalScaledConstraint.class);
		assertThat(constraint.getName()).isEqualTo("a");
		assertThat(((NominalScaledConstraint)constraint).getRequiredValues()).contains("b");
	}
	
	@Test
	public void valid_deserializeOrdinalScaledConstraint() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("ordinalscaledconstraint.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		Constraint constraint = a.getInstanceHierarchies().iterator().next().getInternalElements().iterator().next().getAttributes().iterator().next().getConstraints().iterator().next();
		assertThat(constraint).isInstanceOf(OrdinalScaledConstraint.class);
		assertThat(constraint.getName()).isEqualTo("a");
		assertThat(((OrdinalScaledConstraint)constraint).getRequiredMaxValue()).isEqualTo("b");
		assertThat(((OrdinalScaledConstraint)constraint).getRequiredValue()).isEqualTo("c");
		assertThat(((OrdinalScaledConstraint)constraint).getRequiredMinValue()).isEqualTo("d");
	}
	
	@Test
	public void valid_deepDeleteWithConstraints() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute.UnknownConstraint constraint = attribute.createUnknownConstraint("constraint");
		constraint.setRequirement("5");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		assertThat(internalElement.isDeleted());
		assertThat(attribute.isDeleted());
		assertThat(constraint.isDeleted());
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		internalElement = instanceHierarchy.getInternalElements().iterator().next();
		assertThat(internalElement.getAttribute("attr")).isNotNull();
		assertThat(internalElement.getAttribute("attr")).isNotNull();
		assertThat(internalElement.getAttribute("attr").getConstraint("constraint")).isNotNull();
		
		sp2.restore();
		assertThat(internalElement.isDeleted());
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_deepDeleteWithRefSemantic() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		attribute.addRefSemantic("Test");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		assertThat(internalElement.isDeleted());
		assertThat(attribute.isDeleted());
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		internalElement = instanceHierarchy.getInternalElements().iterator().next();
		assertThat(internalElement.getAttribute("attr")).isNotNull();
		assertThat(internalElement.getAttribute("attr")).isNotNull();
		assertThat(internalElement.getAttribute("attr").getRefSemantics().iterator().hasNext()).isTrue();
		assertThat(internalElement.getAttribute("attr").getRefSemantics().iterator().next()).isEqualTo("Test");
		
		sp2.restore();
		assertThat(internalElement.isDeleted());
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_deepCopyWithConstraints() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute.UnknownConstraint constraint = attribute.createUnknownConstraint("constraint");
		constraint.setRequirement("5");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateCreateAttribute("attr2").isOk()).isTrue();
		AMLAttribute copy = internalElement.createAttribute("attr2", attribute);
		assertThat(internalElement.getAttributesCount()).isEqualTo(2);		
		assertThat(copy.getConstraints().iterator().hasNext()).isTrue();
		assertThat(copy.getConstraint("constraint")).isNotNull();
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLAttribute> iterator = internalElement.getAttributes().iterator();
		iterator.next();
		copy = iterator.next();
		assertThat(copy.getName()).isEqualTo("attr2");
		assertThat(copy.getConstraints().iterator().hasNext()).isTrue();
		assertThat(copy.getConstraint("constraint")).isNotNull();
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_deepCopyWithRefSemantic() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		attribute.addRefSemantic("Test");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateCreateAttribute("attr2").isOk()).isTrue();
		AMLAttribute copy = internalElement.createAttribute("attr2", attribute);
		assertThat(internalElement.getAttributesCount()).isEqualTo(2);		
		assertThat(copy.getRefSemantics().iterator().hasNext()).isTrue();
		assertThat(copy.getRefSemantics().iterator().next()).isEqualTo("Test");
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLAttribute> iterator = internalElement.getAttributes().iterator();
		iterator.next();
		copy = iterator.next();
		assertThat(copy.getName()).isEqualTo("attr2");
		assertThat(copy.getRefSemantics().iterator().hasNext()).isTrue();
		assertThat(copy.getRefSemantics().iterator().next()).isEqualTo("Test");
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_deepDelete_FrameAttruibute() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLFrameAttribute attribute = internalElement.getFrameAttribute();
		attribute.setX(100);
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		assertThat(internalElement.isDeleted());
		assertThat(attribute.isDeleted());
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		internalElement = instanceHierarchy.getInternalElements().iterator().next();
		assertThat(internalElement.getFrameAttribute().getX()).isEqualTo(100);
		sp2.restore();
		assertThat(internalElement.isDeleted());
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test 
	public void valid_deepCopy_FrameAttruibute() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLFrameAttribute attribute = internalElement.getFrameAttribute();
		attribute.setX(100);

		
		Savepoint sp1 = session.createSavepoint();
		assertThat(instanceHierarchy.validateInternalElementCreate().isOk()).isTrue();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(internalElement);
		assertThat(copy.getFrameAttribute().getX()).isEqualTo(100);
			
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		iterator.next();
		copy = iterator.next();
		assertThat(copy.getFrameAttribute().getX()).isEqualTo(100);
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void valid_changeNameOfConstraint() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute.UnknownConstraint constraint = attribute.createUnknownConstraint("constraint");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(constraint.validateNameChange("constraint2").isOk()).isTrue();
		constraint.setName("constraint2");
		assertThat(constraint.getName()).isEqualTo("constraint2");
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(constraint.getName()).isEqualTo("constraint");
		
		sp2.restore();
		assertThat(constraint.getName()).isEqualTo("constraint2");
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void invalid_changeNameOfConstraint() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		attribute.createUnknownConstraint("constraint");
		AMLAttribute.UnknownConstraint constraint = attribute.createUnknownConstraint("constraint2");
		
		assertThat(constraint.validateNameChange("constraint").isAnyOperationNotPermitted()).isTrue();
		try {
			constraint.setName("constraint");
			Fail.fail();
		} catch (AMLValidationException ex) {
		}				
	}
	
	@Test 
	public void valid_deleteConstraint() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute.UnknownConstraint constraint = attribute.createUnknownConstraint("constraint");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(constraint.validateDelete().isOk()).isTrue();
		constraint.delete();
		assertThat(attribute.getConstraint("constraint")).isNull();
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(attribute.getConstraint("constraint").getName()).isEqualTo("constraint");
		
		sp2.restore();
		assertThat(attribute.getConstraint("constraint")).isNull();
		
		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_changeNominalScaledConstraint() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute attribute = internalElement.createAttribute("attr");
		AMLAttribute.NominalScaledConstraint constraint = attribute.createNominalScaledConstraint("constraint");
		constraint.addRequiredValue("val1");
		constraint.addRequiredValue("val2");
		
		Savepoint sp1 = session.createSavepoint();
		constraint.removeRequiredValue("val1");

		Iterator<String> iterator = constraint.getRequiredValues().iterator();
		assertThat(iterator.next()).isEqualTo("val2");
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		iterator = constraint.getRequiredValues().iterator();
		assertThat(iterator.next()).isEqualTo("val1");
		assertThat(iterator.next()).isEqualTo("val2");
		
		sp2.restore();
		iterator = constraint.getRequiredValues().iterator();
		assertThat(iterator.next()).isEqualTo("val2");
		
		sp1.delete();
		sp2.delete();
	}
}
