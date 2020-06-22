/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.aml.AMLAttribute.Constraint;
import org.automationml.aml.AMLCOLLADAInterface.RefType;
import org.automationml.internal.aml.AMLInternalAttributeContainer;

public interface AMLValidator {
	void dispose();

	AMLValidationResultList validateSession();

	// --
	AMLValidationResultList validateInterfaceClassLibraryCreate(AMLDocument document, String name);

	AMLValidationResultList validateInterfaceClassLibrarySetName(AMLInterfaceClassLibrary library, String newName);

	AMLValidationResultList validateInterfaceClassLibraryDelete(AMLInterfaceClassLibrary library);

	AMLValidationResultList validateInterfaceClassCreate(AMLInterfaceClassContainer container, String name);

	AMLValidationResultList validateInterfaceClassSetName(AMLInterfaceClass interfaceClass, String newName);

	AMLValidationResultList validateInterfaceClassDelete(AMLInterfaceClass clazz);

	// --
	AMLValidationResultList validateSystemUnitClassLibraryCreate(AMLDocument document, String libraryName);

	AMLValidationResultList validateSystemUnitClassLibrarySetName(AMLSystemUnitClassLibrary library, String newName);

	AMLValidationResultList validateSystemUnitClassLibraryDelete(AMLSystemUnitClassLibrary library);

	AMLValidationResultList validateSystemUnitClassCreate(AMLSystemUnitClassContainer container, String name);

	AMLValidationResultList validateSystemUnitClassSetName(AMLSystemUnitClass clazz, String newName);

	AMLValidationResultList validateSystemUnitClassDelete(AMLSystemUnitClass clazz);

	// --
	AMLValidationResultList validateRoleClassLibraryCreate(AMLDocument document, String name);

	AMLValidationResultList validateRoleClassLibrarySetName(AMLRoleClassLibrary library, String newName);

	AMLValidationResultList validateRoleClassLibraryDelete(AMLRoleClassLibrary library);

	AMLValidationResultList validateRoleClassCreate(AMLRoleClassContainer container, String name);

	AMLValidationResultList validateRoleClassSetName(AMLRoleClass clazz, String newName);

	AMLValidationResultList validateRoleClassDelete(AMLRoleClass clazz);

	// --
	AMLValidationResultList validateAttributeCreate(AMLInternalAttributeContainer container, String name);

	AMLValidationResultList validateAttributeSetName(AMLAttribute attribute, String newName);

	AMLValidationResultList validateAttributeDelete(AMLAttribute attribute);

	AMLValidationResultList validateFrameAttributeCreate(AMLFrameAttributeContainer container);

	AMLValidationResultList validateFrameAttributeDelete(AMLFrameAttribute attribute);

	// --
	AMLValidationResultList validateInstanceHierarchyCreate(AMLDocument document, String name);

	AMLValidationResultList validateInstanceHierarchySetName(AMLInstanceHierarchy instanceHierarchy, String newName);

	AMLValidationResultList validateInstanceHierarchyDelete(AMLInstanceHierarchy instanceHierarchy);

	// --
	AMLValidationResultList validateInternalElementCreate(AMLInternalElementContainer container);

	AMLValidationResultList validateInternalElementDelete(AMLInternalElement internalElement);

	AMLValidationResultList validateInternalElementSetName(AMLInternalElement internalElement, String newName);

	AMLValidationResultList validateInternalElementSetBaseSystemUnitClass(AMLInternalElement internalElement, AMLSystemUnitClass systemUnitClass);

	// --
	AMLValidationResultList validateExternalInterfaceCreate(AMLExternalInterfaceContainer container, AMLInterfaceClass interfaceClass);

	AMLValidationResultList validateExternalInterfaceSetName(AMLExternalInterface externalInterface, String newName);

	AMLValidationResultList validateExternalInterfaceDelete(AMLExternalInterface externalInterface);

	// --
	AMLValidationResultList validateSupportedRoleClassCreate(AMLSupportedRoleClassContainer container, AMLRoleClass roleClass);

	AMLValidationResultList validateSupportedRoleClassDelete(AMLSupportedRoleClass supportedRole);

	// --
	AMLValidationResultList validateMapAttribute(AMLMappingObject mappingObject, AMLAttribute roleAttribute, AMLAttribute attribute);

	AMLValidationResultList validateUnmapAttribute(AMLMappingObject mappingObject, AMLAttribute roleAttribute);

	// --
	AMLValidationResultList validateRoleRequirementCreate(AMLSupportedRoleClass supportedRoleClass);

	AMLValidationResultList validateRoleRequirementDelete(AMLRoleRequirements roleRequierments);

	// --
	AMLValidationResultList validateInternalLinkCreate(AMLInternalLinkContainer internalLinkContainer, String linkName, AMLExternalInterface refPartnerSideA,
			AMLExternalInterface refPartnerSideB);

	AMLValidationResultList validateInternalLinkDelete(AMLInternalLink internalLink);

	AMLValidationResultList validateInternalLinkSetName(AMLInternalLink internalLink, String newName);

	// --
	AMLValidationResultList validateMirrorObjectCreate(AMLMirrorContainer container, AMLInternalElement internalElement);

	AMLValidationResultList validateMirrorObjectDelete(AMLMirrorObject mirror);

	// --
	AMLValidationResultList validateFacetCreate(AMLInternalElement internalElement, String facetName);

	AMLValidationResultList validateFacetDelete(AMLFacet facet);

	AMLValidationResultList validateFacetSetName(AMLFacet facet, String newName);

	AMLValidationResultList validateFacetAddAttribute(AMLFacet facet, AMLAttribute attribute);

	AMLValidationResultList validateFacetAddExternalInterface(AMLFacet facet, AMLExternalInterface externalInterface);

	AMLValidationResultList validateFacetRemoveAttribute(AMLFacet facet, AMLAttribute attribute);

	AMLValidationResultList validateFacetRemoveExternalInterface(AMLFacet facet, AMLExternalInterface externalInterface);

	// --
	AMLValidationResultList validateGroupCreate(AMLGroupContainer container);

	AMLValidationResultList validateGroupDelete(AMLGroup group);

	AMLValidationResultList validateGroupSetName(AMLGroup group, String newName);

	// --
	AMLValidationResultList validateCOLLADAInterfaceCreate(AMLCOLLADAInterfaceContainer container, RefType refType);

	AMLValidationResultList validateCOLLADAInterfaceDelete(AMLCOLLADAInterface colladaInterface);

	AMLValidationResultList validateInstanceHierarchyReparent(
			AMLDocument oldParentElement,
			AMLDocument newParentElement,
			AMLInstanceHierarchy amlInstanceHierarchyImpl);

	AMLValidationResultList validateInternalElementReparent(
			AMLInternalElementContainer oldParentElement, AMLInternalElementContainer newParentElement,
			AMLInternalElement amlInternalElementImpl);

	AMLValidationResultList validateRoleClassLibraryReparent(
			AMLDocument oldParentElement, AMLDocument newParentElement,
			AMLRoleClassLibrary amlRoleClassLibrary);

	AMLValidationResultList validateInterfaceClassLibraryReparent(
			AMLDocument oldParentElement, AMLDocument newParentElement,
			AMLInterfaceClassLibrary amlInterfaceClassLibrary);
	AMLValidationResultList validateSystemUnitClassLibraryReparent(
			AMLDocument oldParentElement, AMLDocument newParentElement,
			AMLSystemUnitClassLibrary amlSystemUnitClassLibrary);

	AMLValidationResultList validateInterfaceClassReparent(
			AMLInterfaceClassContainer oldParentElement,
			AMLInterfaceClassContainer newParentElement,
			AMLInterfaceClass amlInterfaceClass);

	AMLValidationResultList validateRoleClassReparent(
			AMLRoleClassContainer oldParentElement,
			AMLRoleClassContainer newParentElement,
			AMLRoleClass amlRoleClass);

	AMLValidationResultList validateSystemUnitClassReparent(
			AMLSystemUnitClassContainer oldParentElement,
			AMLSystemUnitClassContainer newParentElement,
			AMLSystemUnitClass amlSystemUnitClass);

	AMLValidationResultList validateAttributeReparent(
			AMLAttributeContainer oldParentElement, AMLAttributeContainer newParentElement,
			AMLAttribute amlAttribute);

	AMLValidationResultList validateAddRefSemantic(
			AMLAttribute amlAttribute, String refSemantic);

	AMLValidationResultList validateRemoveRefSemantic(
			AMLAttribute amlAttribute, String refSemantic);

	AMLValidationResultList validateCreateNominalScalesConstraint(
			AMLAttribute amlAttribute, String constraint);

	AMLValidationResultList validateCreateOrdinalScalesConstraint(
			AMLAttribute amlAttribute, String constraint);

	AMLValidationResultList validateCreateUnknownConstraint(
			AMLAttribute amlAttribute, String constraint);

	AMLValidationResultList validateFrameAttributeReparent(
			AMLFrameAttributeContainer oldParentElement,
			AMLFrameAttributeContainer newParentElement,
			AMLFrameAttribute amlFrameAttribute);

	AMLValidationResultList validateCOLLADAInterfaceReparent(
			AMLFrameAttributeContainer oldParentElement,
			AMLFrameAttributeContainer newParentElement,
			AMLCOLLADAInterface amlcolladaInterface);

	AMLValidationResultList validateAttributeCreate(
			AMLRoleRequirements amlRoleRequirements,
			AMLSupportedRoleClass supportedRoleClass, String name);

	AMLValidationResultList validateMirrorReparent(
			AMLMirrorContainer oldParentElement,
			AMLMirrorContainer newParentElement,
			AMLMirrorObject amlMirrorObject);

	AMLValidationResultList validateGroupReparent(
			AMLGroupContainer oldParentElement,
			AMLGroupContainer newParentElement, AMLGroup amlGroup);

	AMLValidationResultList validateInternalElementCreate(
			AMLInternalElementContainer container,
			AMLSystemUnitClass systemUnitClass);

	AMLValidationResultList validateConstraintSetName(
			Constraint constraint, String newName);

	// TODO ...

}