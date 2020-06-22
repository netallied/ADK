/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.UUID;

public interface AMLInternalElement extends AMLElement, AMLAttributeContainer, AMLInternalElementContainer, AMLExternalInterfaceContainer,
		AMLSupportedRoleClassContainer, AMLFrameAttributeContainer, AMLCOLLADAInterfaceContainer, AMLInternalLinkContainer, 
		AMLMirrorContainer, AMLRenamable {

	void setBaseSystemUnitClass(AMLSystemUnitClass baseclass) throws AMLValidationException;

	void unsetBaseSystemUnitClass() throws AMLValidationException;

	UUID getId();

	AMLSystemUnitClass getBaseSystemUnitClass();

	AMLValidationResultList validateInternalElementSetBaseSystemUnitClass(AMLSystemUnitClass systemUnitClass);

	AMLValidationResultList validateInternalElementUnsetBaseSystemUnitClass(AMLSystemUnitClass systemUnitClass) throws AMLValidationException;

	AMLRoleRequirements createRoleRequirements(AMLSupportedRoleClass supportedRoleClass) throws AMLValidationException;

	AMLRoleRequirements getRoleRequirements();

	AMLValidationResultList validateCreateRoleRequirements(AMLSupportedRoleClass supportedRoleClass);	

	AMLFacet createFacet(String facetName) throws AMLValidationException;

	AMLFacet createFacet(String facetName, UUID id) throws AMLValidationException;

	AMLValidationResultList validateCreateFacet(String facetName);

	Iterable<AMLFacet> getFacets();
	
	int getFacetsCount();

	AMLFacet getFacet(String facetName);

	AMLValidationResultList validateCreateFacet(String facetName, UUID newId) throws AMLValidationException;
	
	int getReferrerCount();
	
	Iterable<AMLMirrorObject> getReferrers();

}
