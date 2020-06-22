/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLSupportedRoleClassContainer extends AMLDocumentElement {
	
	AMLMappingObject createMappingObject(AMLRoleClass roleClass);

	AMLValidationResultList validateCreateSupportedRoleClass(AMLRoleClass roleClass);

	//AMLSupportedRoleClass createSupportedRoleClass(AMLRoleClass roleClass, AMLMappingObject mappings) throws AMLValidationException;
	
	AMLSupportedRoleClass createSupportedRoleClass(AMLRoleClass roleClass) throws AMLValidationException;
	
	AMLSupportedRoleClass getSupportedRoleClass(AMLRoleClass roleClass);

	Iterable<AMLSupportedRoleClass> getSupportedRoleClasses();
	
	int getSupportedRoleClassesCount();
}
