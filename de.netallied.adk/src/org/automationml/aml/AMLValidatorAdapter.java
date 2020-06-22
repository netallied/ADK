/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.aml.AMLAttribute.Constraint;
import org.automationml.aml.AMLCOLLADAInterface.RefType;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.aml.AMLInternalAttributeContainer;
import org.automationml.internal.aml.AMLValidationResultListImpl;

public abstract class AMLValidatorAdapter implements AMLValidator {

	@Override
	public AMLValidationResultList validateCOLLADAInterfaceDelete(AMLCOLLADAInterface colladaInterface) {
		return new AMLValidationResultListImpl(colladaInterface, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateCOLLADAInterfaceCreate(AMLCOLLADAInterfaceContainer container, RefType refType) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateFrameAttributeDelete(AMLFrameAttribute attribute) {
		return new AMLValidationResultListImpl(attribute, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateFrameAttributeCreate(AMLFrameAttributeContainer container) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateGroupSetName(AMLGroup group, String newName) {
		return new AMLValidationResultListImpl(group, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateGroupDelete(AMLGroup group) {
		return new AMLValidationResultListImpl(group, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateGroupCreate(AMLGroupContainer container) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateFacetRemoveAttribute(AMLFacet facet, AMLAttribute attribute) {
		return new AMLValidationResultListImpl(facet, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateFacetRemoveExternalInterface(AMLFacet facet, AMLExternalInterface externalInterface) {
		return new AMLValidationResultListImpl(facet, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateFacetAddExternalInterface(AMLFacet facet, AMLExternalInterface externalInterface) {
		return new AMLValidationResultListImpl(facet, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateFacetAddAttribute(AMLFacet facet, AMLAttribute attribute) {
		return new AMLValidationResultListImpl(facet, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateFacetSetName(AMLFacet facet, String newName) {
		return new AMLValidationResultListImpl(facet, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateFacetDelete(AMLFacet facet) {
		return new AMLValidationResultListImpl(facet, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateFacetCreate(AMLInternalElement internalElement, String facetName) {
		return new AMLValidationResultListImpl(internalElement, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateMirrorObjectDelete(AMLMirrorObject mirror) {
		return new AMLValidationResultListImpl(mirror, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateMirrorObjectCreate(AMLMirrorContainer container, AMLInternalElement internalElement) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInternalLinkSetName(AMLInternalLink internalLink, String newName) {
		return new AMLValidationResultListImpl(internalLink, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInternalLinkDelete(AMLInternalLink internalLink) {
		return new AMLValidationResultListImpl(internalLink, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInternalLinkCreate(AMLInternalLinkContainer internalLinkContainer, String linkName, AMLExternalInterface refPartnerSideA,
			AMLExternalInterface refPartnerSideB) {
		return new AMLValidationResultListImpl(internalLinkContainer, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateRoleRequirementDelete(AMLRoleRequirements roleRequierments) {
		return new AMLValidationResultListImpl(roleRequierments, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateRoleRequirementCreate(AMLSupportedRoleClass supportedRoleClass) {
		return new AMLValidationResultListImpl(supportedRoleClass, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateUnmapAttribute(AMLMappingObject mappingObject, AMLAttribute roleAttribute) {
		return new AMLValidationResultListImpl(mappingObject, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateExternalInterfaceDelete(AMLExternalInterface externalInterface) {
		return new AMLValidationResultListImpl(externalInterface, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateSupportedRoleClassCreate(AMLSupportedRoleClassContainer container, AMLRoleClass roleClass) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateSupportedRoleClassDelete(AMLSupportedRoleClass supportedRole) {
		return new AMLValidationResultListImpl(supportedRole, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateMapAttribute(AMLMappingObject mappingObject, AMLAttribute roleAttribute, AMLAttribute attribute) {
		return new AMLValidationResultListImpl(mappingObject, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateExternalInterfaceCreate(AMLExternalInterfaceContainer container, AMLInterfaceClass interfaceClass) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateExternalInterfaceSetName(AMLExternalInterface externalInterface, String newName) {
		return new AMLValidationResultListImpl(externalInterface, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateSession() {
		return AMLValidationResultList.EMPTY;
	}

	@Override
	public AMLValidationResultList validateInterfaceClassLibraryCreate(AMLDocument document, String name) {
		return new AMLValidationResultListImpl(document, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInterfaceClassLibrarySetName(AMLInterfaceClassLibrary library, String newName) {
		return new AMLValidationResultListImpl(library, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInterfaceClassSetName(AMLInterfaceClass clazz, String newName) {
		return new AMLValidationResultListImpl(clazz, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateSystemUnitClassLibraryCreate(AMLDocument document, String libraryName) {
		return new AMLValidationResultListImpl(document, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateSystemUnitClassLibrarySetName(AMLSystemUnitClassLibrary library, String newName) {
		return new AMLValidationResultListImpl(library, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateSystemUnitClassSetName(AMLSystemUnitClass clazz, String newName) {
		return new AMLValidationResultListImpl(clazz, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateRoleClassLibraryCreate(AMLDocument document, String name) {
		return new AMLValidationResultListImpl(document, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateRoleClassLibrarySetName(AMLRoleClassLibrary library, String newName) {
		return new AMLValidationResultListImpl(library, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateRoleClassSetName(AMLRoleClass clazz, String newName) {
		return new AMLValidationResultListImpl(clazz, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateAttributeCreate(AMLInternalAttributeContainer container, String name) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateAttributeSetName(AMLAttribute attribute, String newName) {
		return new AMLValidationResultListImpl(attribute, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateAttributeDelete(AMLAttribute attribute) {
		return new AMLValidationResultListImpl(attribute, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInstanceHierarchyCreate(AMLDocument document, String name) {
		return new AMLValidationResultListImpl(document, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInstanceHierarchySetName(AMLInstanceHierarchy instanceHierarchy, String newName) {
		return new AMLValidationResultListImpl(instanceHierarchy, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInstanceHierarchyDelete(AMLInstanceHierarchy instanceHierarchy) {
		return new AMLValidationResultListImpl(instanceHierarchy, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInterfaceClassDelete(AMLInterfaceClass clazz) {
		return new AMLValidationResultListImpl(clazz, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateSystemUnitClassDelete(AMLSystemUnitClass clazz) {
		return new AMLValidationResultListImpl(clazz, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateRoleClassDelete(AMLRoleClass clazz) {
		return new AMLValidationResultListImpl(clazz, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInterfaceClassLibraryDelete(AMLInterfaceClassLibrary library) {
		return new AMLValidationResultListImpl(library, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateSystemUnitClassLibraryDelete(AMLSystemUnitClassLibrary library) {
		return new AMLValidationResultListImpl(library, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateRoleClassLibraryDelete(AMLRoleClassLibrary library) {
		return new AMLValidationResultListImpl(library, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInterfaceClassCreate(AMLInterfaceClassContainer container, String name) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateSystemUnitClassCreate(AMLSystemUnitClassContainer container, String name) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateRoleClassCreate(AMLRoleClassContainer container, String name) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInternalElementDelete(AMLInternalElement internalElement) {
		return new AMLValidationResultListImpl(internalElement, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInternalElementCreate(AMLInternalElementContainer container) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInternalElementSetName(AMLInternalElement internalElement, String newName) {
		return new AMLValidationResultListImpl(internalElement, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInternalElementSetBaseSystemUnitClass(AMLInternalElement internalElement, AMLSystemUnitClass systemUnitClass) {
		return new AMLValidationResultListImpl(internalElement, Severity.OK, "");
	}
	
	@Override
	public AMLValidationResultListImpl validateInstanceHierarchyReparent(
			AMLDocument oldParentElement,
			AMLDocument newParentElement,
			AMLInstanceHierarchy amlInstanceHierarchyImpl) {
		return new AMLValidationResultListImpl(amlInstanceHierarchyImpl, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInternalElementReparent(
			AMLInternalElementContainer oldParentElement,
			AMLInternalElementContainer newParentElement,
			AMLInternalElement amlInternalElementImpl) {
		return new AMLValidationResultListImpl(amlInternalElementImpl, Severity.OK, "");
	}
	
	@Override
	public AMLValidationResultListImpl validateRoleClassLibraryReparent(
			AMLDocument oldParentElement, AMLDocument newParentElement,
			AMLRoleClassLibrary amlRoleClassLibrary) {
		return new AMLValidationResultListImpl(amlRoleClassLibrary, Severity.OK, "");
	}

	@Override
	public AMLValidationResultListImpl validateInterfaceClassLibraryReparent(
			AMLDocument oldParentElement, AMLDocument newParentElement,
			AMLInterfaceClassLibrary amlInterfaceClassLibrary) {
		return new AMLValidationResultListImpl(amlInterfaceClassLibrary, Severity.OK, "");
	}

	@Override
	public AMLValidationResultListImpl validateSystemUnitClassLibraryReparent(
			AMLDocument oldParentElement, AMLDocument newParentElement,
			AMLSystemUnitClassLibrary amlSystemUnitClassLibrary) {
		return new AMLValidationResultListImpl(amlSystemUnitClassLibrary, Severity.OK, "");
	}

	@Override
	public AMLValidationResultListImpl validateInterfaceClassReparent(
			AMLInterfaceClassContainer oldParentElement,
			AMLInterfaceClassContainer newParentElement,
			AMLInterfaceClass amlInterfaceClass) {
		return new AMLValidationResultListImpl(amlInterfaceClass, Severity.OK, "");
	}

	@Override
	public AMLValidationResultListImpl validateRoleClassReparent(
			AMLRoleClassContainer oldParentElement,
			AMLRoleClassContainer newParentElement, AMLRoleClass amlRoleClass) {
		return new AMLValidationResultListImpl(amlRoleClass, Severity.OK, "");
	}

	@Override
	public AMLValidationResultListImpl validateSystemUnitClassReparent(
			AMLSystemUnitClassContainer oldParentElement,
			AMLSystemUnitClassContainer newParentElement,
			AMLSystemUnitClass amlSystemUnitClass) {
		return new AMLValidationResultListImpl(amlSystemUnitClass, Severity.OK, "");
	}

	@Override
	public AMLValidationResultListImpl validateAttributeReparent(
			AMLAttributeContainer oldParentElement,
			AMLAttributeContainer newParentElement, AMLAttribute amlAttribute) {
		return new AMLValidationResultListImpl(amlAttribute, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateAddRefSemantic(
			AMLAttribute amlAttribute, String refSemantic) {
		return new AMLValidationResultListImpl(amlAttribute, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateRemoveRefSemantic(
			AMLAttribute amlAttribute, String refSemantic) {
		return new AMLValidationResultListImpl(amlAttribute, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateCreateNominalScalesConstraint(
			AMLAttribute amlAttribute, String constraint) {
		return new AMLValidationResultListImpl(amlAttribute, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateCreateOrdinalScalesConstraint(
			AMLAttribute amlAttribute, String constraint) {
		return new AMLValidationResultListImpl(amlAttribute, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateCreateUnknownConstraint(
			AMLAttribute amlAttribute, String constraint) {
		return new AMLValidationResultListImpl(amlAttribute, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateFrameAttributeReparent(
			AMLFrameAttributeContainer oldParentElement,
			AMLFrameAttributeContainer newParentElement,
			AMLFrameAttribute amlFrameAttribute) {
		return new AMLValidationResultListImpl(amlFrameAttribute, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateCOLLADAInterfaceReparent(
			AMLFrameAttributeContainer oldParentElement,
			AMLFrameAttributeContainer newParentElement,
			AMLCOLLADAInterface amlcolladaInterface) {
		return new AMLValidationResultListImpl(amlcolladaInterface, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateAttributeCreate(
			AMLRoleRequirements amlRoleRequirements,
			AMLSupportedRoleClass supportedRoleClass, String name) {
		return new AMLValidationResultListImpl(amlRoleRequirements, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateMirrorReparent(
			AMLMirrorContainer oldParentElement,
			AMLMirrorContainer newParentElement, AMLMirrorObject amlMirrorObject) {
		return new AMLValidationResultListImpl(amlMirrorObject, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateGroupReparent(
			AMLGroupContainer oldParentElement,
			AMLGroupContainer newParentElement, AMLGroup amlGroup) {
		return new AMLValidationResultListImpl(amlGroup, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateInternalElementCreate(
			AMLInternalElementContainer container,
			AMLSystemUnitClass systemUnitClass) {
		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateConstraintSetName(
			Constraint constraint, String newName) {
		return new AMLValidationResultListImpl(constraint, Severity.OK, "");
	}

}
