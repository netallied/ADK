/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLRoleRequirements extends AMLDocumentElement, AMLElement {

	AMLRoleClass getRoleClass();

	AMLSupportedRoleClass getSupportedRoleClass();
	
	AMLValidationResultList validateCreateAttribute(AMLSupportedRoleClass supportedRoleClass, String name);
	
	Iterable<AMLAttribute> getAttributes();

	int getAttributesCount();

	boolean hasAttributes();

	AMLAttribute getAttribute(AMLSupportedRoleClass supportedRoleClass, String name);

	AMLAttribute createAttribute(AMLSupportedRoleClass supportedRoleClass, String name) throws AMLValidationException;

	AMLSupportedRoleClass getSupportedRoleClassOfAttribute(AMLAttribute attribute);

	AMLAttribute getAttributeOfSupportedRoleClassAttribute(AMLSupportedRoleClass supportedRoleClass, AMLAttribute attribute);

}
