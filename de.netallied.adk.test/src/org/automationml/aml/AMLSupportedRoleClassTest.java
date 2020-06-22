/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Iterator;

import org.automationml.Savepoint;
import org.fest.assertions.Fail;
import org.junit.Test;

public class AMLSupportedRoleClassTest extends AbstractAMLTest {

	@Test
	public void valid_createSupportedRoleClass() throws Exception {

		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");

		Savepoint sp1 = session.createSavepoint();
		AMLSupportedRoleClass supportedRoleClass = systemUnitClass.createSupportedRoleClass(roleClass);
		assertThat(supportedRoleClass.getRoleClass()).isNotNull();
		AMLRoleClass rc = supportedRoleClass.getRoleClass();
		assertThat(rc).isSameAs(roleClass);

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(supportedRoleClass.isDeleted()).isTrue();
		supportedRoleClass = systemUnitClass.getSupportedRoleClass(roleClass);
		assertThat(supportedRoleClass).isNull();

		sp2.restore();
		supportedRoleClass = systemUnitClass.getSupportedRoleClass(roleClass);
		assertThat(supportedRoleClass).isNotNull();
		assertThat(supportedRoleClass.getRoleClass()).isSameAs(roleClass);

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_createSupportedRoleClassWithAttributesWithoutMapping() throws Exception {

		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
		roleAttribute.createAttribute("Sub");

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute");
		sucAttribute.createAttribute("Sub");

		AMLSupportedRoleClass supportedRoleClass = systemUnitClass.createSupportedRoleClass(roleClass);
		assertThat(supportedRoleClass).isNotNull();
	}

//	@Ignore
//	@Test(expected = AMLValidationException.class)
//	public void invalid_createSupportedRoleClassWithAttributesWithoutMapping() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		roleClass.createAttribute("Attribute");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		systemUnitClass.createAttribute("Attribute2");
//
//		systemUnitClass.createSupportedRoleClass(roleClass, null);
//	}
//
//	@Ignore
//	@Test(expected = AMLValidationException.class)
//	public void invalid_createSupportedRoleClassWithAttributeHierarchyWithoutMapping() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//		roleAttribute.createAttribute("Sub");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		systemUnitClass.createAttribute("Attribute");
//
//		systemUnitClass.createSupportedRoleClass(roleClass, null);
//	}
//
//	@Ignore
//	@Test
//	public void valid_createSupportedRoleClassWithAttributesWithMapping() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute2");
//
//		Savepoint sp1 = session.createSavepoint();
//		AMLMappingObject mappings = systemUnitClass.createMappingObject(roleClass);
//		mappings.mapAttribute(roleAttribute, sucAttribute);
//
//		AMLSupportedRoleClass supportedRoleClass = systemUnitClass.createSupportedRoleClass(roleClass, mappings);
//
//		Savepoint sp2 = session.createSavepoint();
//		sp1.restore();
//		assertThat(supportedRoleClass.isDeleted()).isTrue();
//
//		sp2.restore();
//		supportedRoleClass = systemUnitClass.getSupportedRoleClass(roleClass);
//		mappings = supportedRoleClass.getMappingObject();
//		assertThat(mappings).isNotNull();
//
//		sp1.delete();
//		sp2.delete();
//	}
//
//	@Ignore
//	@Test
//	public void valid_createSupportedRoleClassWithAttributesMixed() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//		roleClass.createAttribute("Attribute2");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		systemUnitClass.createAttribute("Attribute2");
//		AMLAttribute sucAttribute2 = systemUnitClass.createAttribute("Attribute3");
//
//		AMLMappingObject mappings = systemUnitClass.createMappingObject(roleClass);
//		mappings.mapAttribute(roleAttribute, sucAttribute2);
//
//		systemUnitClass.createSupportedRoleClass(roleClass, mappings);
//	}

	@Test
	public void valid_supportedRoleClassAddAttribute() throws Exception {

		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");

		systemUnitClass.createSupportedRoleClass(roleClass);

		systemUnitClass.createAttribute("Attribute");
		roleClass.createAttribute("Attribute");
	}

//	@Ignore
//	@Test(expected = AMLValidationException.class)
//	public void invalid_supportedRoleClassAddAttribute() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//
//		systemUnitClass.createSupportedRoleClass(roleClass, null);
//
//		roleClass.createAttribute("Attribute");
//	}

//	@Test(expected = AMLValidationException.class)
//	public void invalid_supportedRoleClassAddHierarchyAttribute() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		systemUnitClass.createAttribute("Attribute");
//
//		systemUnitClass.createSupportedRoleClass(roleClass);
//
//		roleAttribute.createAttribute("Attribute2");
//	}

	@Test
	public void valid_supportedRoleClassAddHierarchyAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute");

		systemUnitClass.createSupportedRoleClass(roleClass);

		sucAttribute.createAttribute("Attribute2");
		roleAttribute.createAttribute("Attribute2");
	}

//	@Test(expected = AMLValidationException.class)
//	public void invalid_supportedRoleClassRemoveAttribute() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//
//		systemUnitClass.createSupportedRoleClass(roleClass);
//
//		AMLAttribute attribute = systemUnitClass.createAttribute("Attribute");
//		roleClass.createAttribute("Attribute");
//		attribute.delete();
//	}

	@Test
	public void valid_supportedRoleClassRemoveAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");

		systemUnitClass.createSupportedRoleClass(roleClass);

		AMLAttribute attribute = systemUnitClass.createAttribute("Attribute");
		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");

		roleAttribute.delete();
		attribute.delete();
	}

//	@Test(expected = AMLValidationException.class)
//	public void invalid_supportedRoleClassRemoveHierarchyAttribute() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//
//		systemUnitClass.createSupportedRoleClass(roleClass);
//
//		AMLAttribute attribute = systemUnitClass.createAttribute("Attribute");
//		attribute = attribute.createAttribute("Attribute2");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//		roleAttribute = roleAttribute.createAttribute("Attribute2");
//		attribute.delete();
//	}

	@Test
	public void valid_supportedRoleClassRemoveHierarchyAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");

		systemUnitClass.createSupportedRoleClass(roleClass);

		AMLAttribute attribute = systemUnitClass.createAttribute("Attribute");
		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");

		roleAttribute.delete();
		attribute.delete();
	}

//	@Ignore
//	@Test(expected = AMLValidationException.class)
//	public void invalid_supportedRoleClassUnmapAttribute() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute2");
//
//		AMLMappingObject mappings = systemUnitClass.createMappingObject(roleClass);
//		mappings.mapAttribute(roleAttribute, sucAttribute);
//
//		systemUnitClass.createSupportedRoleClass(roleClass, mappings);
//		mappings.unmapAttribute(roleAttribute);
//	}
//
//	@Ignore
//	@Test
//	public void valid_supportedRoleClassUnmapAttribute() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute2");
//
//		AMLMappingObject mappings = systemUnitClass.createMappingObject(roleClass);
//		mappings.mapAttribute(roleAttribute, sucAttribute);
//
//		systemUnitClass.createSupportedRoleClass(roleClass, mappings);
//		systemUnitClass.createAttribute("Attribute");
//		mappings.unmapAttribute(roleAttribute);
//	}
//
//	@Ignore
//	@Test(expected = AMLValidationException.class)
//	public void invalid_supportedRoleClassRemoveMappedAttribute() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute2");
//
//		AMLMappingObject mappings = systemUnitClass.createMappingObject(roleClass);
//		mappings.mapAttribute(roleAttribute, sucAttribute);
//
//		systemUnitClass.createSupportedRoleClass(roleClass, mappings);
//		sucAttribute.delete();
//	}
//
//	@Ignore
//	@Test
//	public void valid_supportedRoleClassRemoveMappedAttribute() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute2");
//
//		AMLMappingObject mappings = systemUnitClass.createMappingObject(roleClass);
//		mappings.mapAttribute(roleAttribute, sucAttribute);
//
//		systemUnitClass.createSupportedRoleClass(roleClass, mappings);
//		systemUnitClass.createAttribute("Attribute");
//		mappings.unmapAttribute(roleAttribute);
//		sucAttribute.delete();
//	}

//	@Test(expected = AMLValidationException.class)
//	public void invalid_supportedRoleClassRenameAttribute() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute");
//
//		systemUnitClass.createSupportedRoleClass(roleClass);
//
//		sucAttribute.setName("Attribut2");
//	}

	@Test
	public void valid_supportedRoleClassRenameAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute");

		systemUnitClass.createSupportedRoleClass(roleClass);
		roleAttribute.delete();
		sucAttribute.setName("Attribut2");
	}

//	@Ignore
//	@Test
//	public void valid_supportedRoleClassRenameMappedAttribute() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute2");
//
//		AMLMappingObject mappings = systemUnitClass.createMappingObject(roleClass);
//		mappings.mapAttribute(roleAttribute, sucAttribute);
//		systemUnitClass.createSupportedRoleClass(roleClass, mappings);
//
//		sucAttribute.setName("Attribut3");
//	}

//	@Test(expected = AMLValidationException.class)
//	public void invalid_supportedRoleClassRenameRoleAttribute() throws Exception {
//		AMLDocument a = session.createAMLDocument();
//
//		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
//		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
//		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");
//
//		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
//		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
//		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute");
//
//		systemUnitClass.createSupportedRoleClass(roleClass);
//
//		roleAttribute.setName("Attribut2");
//	}

	@Test
	public void valid_supportedRoleClassRenameRoleAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");
		AMLAttribute roleAttribute = roleClass.createAttribute("Attribute");

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.createSystemUnitClassLibrary("sucl");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.createSystemUnitClass("suc");
		AMLAttribute sucAttribute = systemUnitClass.createAttribute("Attribute");

		AMLSupportedRoleClass supportedRoleClass = systemUnitClass.createSupportedRoleClass(roleClass);
		AMLMappingObject mappingObject = supportedRoleClass.getMappingObject();
		mappingObject.mapAttribute(roleAttribute, sucAttribute);

		roleAttribute.setName("Attribute2");
		sucAttribute.setName("Attribute2");
		mappingObject.unmapAttribute(roleAttribute);
	}

	@Test
	public void valid_setSupportedRoleClassAsRoleRequierement() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setName("Element");

		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		Savepoint sp1 = session.createSavepoint();
		AMLRoleRequirements roleRequirements = internalElement.createRoleRequirements(supportedRoleClass);

		assertThat(roleRequirements.getRoleClass()).isNotNull();
		AMLRoleClass rc = roleRequirements.getRoleClass();
		assertThat(rc).isSameAs(roleClass);

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(roleRequirements.isDeleted()).isTrue();
		roleRequirements = internalElement.getRoleRequirements();
		assertThat(roleRequirements).isNull();

		sp2.restore();
		roleRequirements = internalElement.getRoleRequirements();
		assertThat(roleRequirements).isNotNull();
		assertThat(roleRequirements.getRoleClass()).isSameAs(roleClass);

		sp1.delete();
		sp2.delete();
	}

	@Test
	public void valid_deleteRoleRequierement() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("class");

		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("Hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		internalElement.setName("Element");

		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		AMLRoleRequirements roleRequirements = internalElement.createRoleRequirements(supportedRoleClass);
		Savepoint sp1 = session.createSavepoint();
		roleRequirements.delete();

		assertThat(roleRequirements.isDeleted()).isTrue();
		roleRequirements = internalElement.getRoleRequirements();
		assertThat(roleRequirements).isNull();

		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		roleRequirements = internalElement.getRoleRequirements();
		assertThat(roleRequirements).isNotNull();
		assertThat(roleRequirements.getRoleClass()).isSameAs(roleClass);

		sp2.restore();
		assertThat(roleRequirements.isDeleted()).isTrue();
		roleRequirements = internalElement.getRoleRequirements();
		assertThat(roleRequirements).isNull();

		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void invalid_reparentRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLRoleClassLibrary roleClassLibrary = b.createRoleClassLibrary("rcl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("rc");
		
		AMLInstanceHierarchy instanceHierarchy = b.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		
		assertThat(roleClassLibrary.validateReparent(b, a, null, null).isAnyOperationNotPermitted()).isTrue();
	}
	
	@Test
	public void valid_reparentRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLDocument b = session.createAMLDocument();
		
		a.addExplicitExternalReference(b);
		
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("rcl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("rc");
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		
		assertThat(roleClassLibrary.validateReparent(a, b, null, null).isOk()).isTrue();
	}
	
	@Test
	public void valid_unlink() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		
		Savepoint sp1 = session.createSavepoint();
		internalElement.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement.getSupportedRoleClasses().iterator().hasNext()).isFalse();
		
		sp1.restore();
		assertThat(internalElement.getSupportedRoleClasses().iterator().hasNext()).isTrue();
		assertThat(internalElement.getSupportedRoleClasses().iterator().next().getRoleClass()).isEqualTo(roleClass);
		
		sp2.restore();
		assertThat(internalElement.getSupportedRoleClasses().iterator().hasNext()).isFalse();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_unlink2() throws Exception {
		AMLDocument a = session.createAMLDocument();

		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		
		Savepoint sp1 = session.createSavepoint();
		a.unlink();
		Savepoint sp2 = session.createSavepoint();
		assertThat(internalElement.getSupportedRoleClasses().iterator().hasNext()).isTrue();
		assertThat(internalElement.getSupportedRoleClasses().iterator().next()).isEqualTo(supportedRoleClass);
		
		sp1.restore();
		assertThat(internalElement.getSupportedRoleClasses().iterator().hasNext()).isTrue();
		assertThat(internalElement.getSupportedRoleClasses().iterator().next()).isEqualTo(supportedRoleClass);
		
		sp2.restore();
		assertThat(internalElement.getSupportedRoleClasses().iterator().hasNext()).isTrue();
		assertThat(internalElement.getSupportedRoleClasses().iterator().next()).isEqualTo(supportedRoleClass);
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test 
	public void valid_deepDelete() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("rcl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("rc");
		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		Savepoint sp1 = session.createSavepoint();
		
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		assertThat(internalElement.isDeleted()).isTrue();
		assertThat(supportedRoleClass.isDeleted()).isTrue();

		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(instanceHierarchy.getInternalElements().iterator().hasNext()).isTrue();
		assertThat(instanceHierarchy.getInternalElements().iterator().next().getSupportedRoleClasses().iterator().hasNext()).isTrue();
		
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
		
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(internalElement);
		assertThat(copy.getSupportedRoleClasses().iterator().hasNext()).isTrue();
		assertThat(copy.getSupportedRoleClasses().iterator().next().getRoleClass()).isEqualTo(roleClass);
		
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		iterator.next();
		copy = iterator.next();
		assertThat(copy.getSupportedRoleClasses().iterator().hasNext()).isTrue();
		assertThat(copy.getSupportedRoleClasses().iterator().next().getRoleClass()).isEqualTo(roleClass);
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_addAttributeToMapping() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute ieAttr = internalElement.createAttribute("ieAttr");
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLAttribute roleAttr = roleClass.createAttribute("roleAttr");		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		
		Savepoint sp1 = session.createSavepoint();
		AMLMappingObject mapping = supportedRoleClass.getMappingObject();
		mapping.mapAttribute(roleAttr, ieAttr);
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(mapping.getMappedAttribute(roleAttr)).isNull();
		
		sp2.restore();
		assertThat(mapping.getMappedAttribute(roleAttr)).isNotNull();
		assertThat(mapping.getMappedAttribute(roleAttr)).isEqualTo(ieAttr);
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_roleRequirementsWithAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute ieAttr = internalElement.createAttribute("ieAttr");
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLAttribute roleAttr = roleClass.createAttribute("roleAttr");		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		AMLMappingObject mapping = supportedRoleClass.getMappingObject();
		mapping.mapAttribute(roleAttr, ieAttr);
		
		Savepoint sp1 = session.createSavepoint();
		AMLRoleRequirements rr = internalElement.createRoleRequirements(supportedRoleClass);
		AMLAttribute rRoleAttr = rr.createAttribute(supportedRoleClass, "roleAttr");
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(rr.isDeleted()).isTrue();
		assertThat(rRoleAttr.isDeleted()).isTrue();
		
		sp2.restore();
		assertThat(internalElement.getRoleRequirements()).isNotNull();
		assertThat(internalElement.getRoleRequirements().getSupportedRoleClass()).isEqualTo(supportedRoleClass);
		assertThat(internalElement.getRoleRequirements().getAttribute(supportedRoleClass, "roleAttr")).isNotNull();
		
		sp1.delete();
		sp2.delete();
	}

	
	@Test
	public void valid_roleRequirementschangeAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute ieAttr = internalElement.createAttribute("ieAttr");
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLAttribute roleAttr = roleClass.createAttribute("roleAttr");		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		AMLMappingObject mapping = supportedRoleClass.getMappingObject();
		mapping.mapAttribute(roleAttr, ieAttr);
		AMLRoleRequirements rr = internalElement.createRoleRequirements(supportedRoleClass);
		AMLAttribute rRoleAttr = rr.createAttribute(supportedRoleClass, "roleAttr");
		
		Savepoint sp1 = session.createSavepoint();
		roleAttr.setName("other");
		assertThat(rRoleAttr.getName()).isEqualTo(roleAttr.getName());
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		assertThat(rRoleAttr.getName()).isEqualTo(roleAttr.getName());
		
		sp2.restore();
		assertThat(rRoleAttr.getName()).isEqualTo(roleAttr.getName());
		
		sp1.delete();
		sp2.delete();
	}
	
	@Test
	public void invalid_roleRequirementschangeAttribute() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute ieAttr = internalElement.createAttribute("ieAttr");
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLAttribute roleAttr = roleClass.createAttribute("roleAttr");		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		AMLMappingObject mapping = supportedRoleClass.getMappingObject();
		mapping.mapAttribute(roleAttr, ieAttr);
		AMLRoleRequirements rr = internalElement.createRoleRequirements(supportedRoleClass);
		AMLAttribute rRoleAttr = rr.createAttribute(supportedRoleClass, "roleAttr");
		
		assertThat(rRoleAttr.validateNameChange("test").isAnyOperationNotPermitted()).isTrue();
		try {
			rRoleAttr.setName("Test");
			Fail.fail();
		} catch (AMLValidationException ex) {
		}
	}
	
	@Test
	public void invalid_roleRequirementschangeAttribute2() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute ieAttr = internalElement.createAttribute("ieAttr");
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLAttribute roleAttr = roleClass.createAttribute("roleAttr");		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		AMLMappingObject mapping = supportedRoleClass.getMappingObject();
		mapping.mapAttribute(roleAttr, ieAttr);
		AMLRoleRequirements rr = internalElement.createRoleRequirements(supportedRoleClass);
		AMLAttribute rRoleAttr = rr.createAttribute(supportedRoleClass, "roleAttr");
		
		assertThat(rRoleAttr.validateCreateAttribute("test2").isAnyOperationNotPermitted()).isTrue();
		try {
			rRoleAttr.createAttribute("test2");
			Fail.fail();
		} catch (AMLValidationException ex) {
		}
	}
	
	@Test
	public void valid_deepCopy_SupportedRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute ieAttr = internalElement.createAttribute("ieAttr");
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLAttribute roleAttr = roleClass.createAttribute("roleAttr");		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		AMLMappingObject mapping = supportedRoleClass.getMappingObject();
		mapping.mapAttribute(roleAttr, ieAttr);
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(internalElement);
		AMLAttribute copyAttr = copy.getAttribute("ieAttr");
		assertThat(copy.getSupportedRoleClasses().iterator().next().getMappingObject().getMappedAttribute(roleAttr)).isEqualTo(copyAttr);
		
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		iterator.next();
		copy = iterator.next();
		copyAttr = copy.getAttribute("ieAttr");
		assertThat(copy.getSupportedRoleClasses().iterator().next().getMappingObject().getMappedAttribute(roleAttr)).isEqualTo(copyAttr);
		
		sp1.delete();
		sp2.delete();		
		
	}
	
	@Test
	public void valid_deepDelete_SupportedRoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute ieAttr = internalElement.createAttribute("ieAttr");
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLAttribute roleAttr = roleClass.createAttribute("roleAttr");		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		AMLMappingObject mapping = supportedRoleClass.getMappingObject();
		mapping.mapAttribute(roleAttr, ieAttr);
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		assertThat(internalElement.isDeleted()).isTrue();
		assertThat(supportedRoleClass.isDeleted()).isTrue();
		assertThat(mapping.isDeleted()).isTrue();

		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		AMLInternalElement copy = instanceHierarchy.getInternalElements().iterator().next();
		AMLAttribute copyAttr = copy.getAttribute("ieAttr");
		assertThat(copy.getSupportedRoleClasses().iterator().next().getMappingObject().getMappedAttribute(roleAttr)).isEqualTo(copyAttr);
		
		sp2.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_deepCopy_RoleRequierements() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute ieAttr = internalElement.createAttribute("ieAttr");
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLAttribute roleAttr = roleClass.createAttribute("roleAttr");		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		AMLMappingObject mapping = supportedRoleClass.getMappingObject();
		mapping.mapAttribute(roleAttr, ieAttr);
		AMLRoleRequirements rr = internalElement.createRoleRequirements(supportedRoleClass);
		AMLAttribute rRoleAttr = rr.createAttribute(supportedRoleClass, "roleAttr");
		
		Savepoint sp1 = session.createSavepoint();
		AMLInternalElement copy = instanceHierarchy.createInternalElement(internalElement);
		assertThat(copy.getRoleRequirements()).isNotNull();
		assertThat(copy.getRoleRequirements().getAttributesCount()).isEqualTo(1);
		assertThat(copy.getRoleRequirements().getAttributes().iterator().next().getName()).isEqualTo(roleAttr.getName());
		
		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp2.restore();
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		iterator.next();
		copy = iterator.next();
		assertThat(copy.getRoleRequirements()).isNotNull();
		assertThat(copy.getRoleRequirements().getAttributesCount()).isEqualTo(1);
		assertThat(copy.getRoleRequirements().getAttributes().iterator().next().getName()).isEqualTo(roleAttr.getName());
		
		sp1.delete();
		sp2.delete();		
		
	}
	
	@Test
	public void valid_deepDelete_RoleRequierements() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLAttribute ieAttr = internalElement.createAttribute("ieAttr");
		AMLRoleClassLibrary roleClassLibrary = a.createRoleClassLibrary("icl");
		AMLRoleClass roleClass = roleClassLibrary.createRoleClass("ic");
		AMLAttribute roleAttr = roleClass.createAttribute("roleAttr");		
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		AMLMappingObject mapping = supportedRoleClass.getMappingObject();
		mapping.mapAttribute(roleAttr, ieAttr);
		AMLRoleRequirements rr = internalElement.createRoleRequirements(supportedRoleClass);
		AMLAttribute rRoleAttr = rr.createAttribute(supportedRoleClass, "roleAttr");
		
		Savepoint sp1 = session.createSavepoint();
		assertThat(internalElement.validateDeepDelete().isOk()).isTrue();
		internalElement.deepDelete();
		assertThat(internalElement.isDeleted()).isTrue();
		assertThat(rr.isDeleted()).isTrue();
		assertThat(rRoleAttr.isDeleted()).isTrue();

		Savepoint sp2 = session.createSavepoint();
		
		sp1.restore();
		AMLInternalElement copy = instanceHierarchy.getInternalElements().iterator().next();
		assertThat(copy.getRoleRequirements()).isNotNull();
		assertThat(copy.getRoleRequirements().getAttributesCount()).isEqualTo(1);
		assertThat(copy.getRoleRequirements().getAttributes().iterator().next().getName()).isEqualTo(roleAttr.getName());
		
		sp2.restore();
		assertThat(copy.isDeleted()).isTrue();
		
		sp1.delete();
		sp2.delete();		
	}
	
	@Test
	public void valid_deepDelete_RoleClass() throws Exception {
		AMLDocument a = session.createAMLDocument();
		AMLRoleClassLibrary library = a.createRoleClassLibrary("lib");
		AMLRoleClass roleClass = library.createRoleClass("role");
		
		AMLInstanceHierarchy instanceHierarchy = a.createInstanceHierarchy("hierarchy");
		AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
		AMLSupportedRoleClass supportedRoleClass = internalElement.createSupportedRoleClass(roleClass);
		
		
		assertThat(supportedRoleClass.getMappingObject()).isNotNull();
		
		Savepoint sp1 = session.createSavepoint();
		roleClass.unlink();
		roleClass.deepDelete();
		assertThat(library.getRoleClassesCount() == 0).isTrue();
		assertThat(internalElement.getSupportedRoleClassesCount() == 0).isTrue();
		assertThat(roleClass.isDeleted()).isTrue();
		assertThat(supportedRoleClass.isDeleted()).isTrue();
		
		
		Savepoint sp2 = session.createSavepoint();
		sp1.restore();
		
		assertThat(library.getRoleClass("role")).isNotNull();
		assertThat(internalElement.getSupportedRoleClassesCount() == 1).isTrue();
		assertThat(internalElement.getSupportedRoleClass(library.getRoleClass("role"))).isNotNull();
		assertThat(internalElement.getSupportedRoleClass(library.getRoleClass("role")).getMappingObject()).isNotNull();
		
		sp2.restore();
		assertThat(library.getRoleClassesCount() == 0).isTrue();
		assertThat(internalElement.getSupportedRoleClassesCount() == 0).isTrue();
		
		sp1.delete();
		sp2.delete();			
	}
}
