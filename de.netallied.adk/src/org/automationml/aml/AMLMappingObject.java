/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLMappingObject extends AMLDocumentElement {

	void mapAttribute(AMLAttribute roleAttribute, AMLAttribute attribute) throws AMLValidationException;

	AMLAttribute getMappedAttribute(AMLAttribute attribute);

	Iterable<AMLAttribute> getMappedRoleAttributes();
	
	int getMappedRoleAttributeCount();

	void unmapAttribute(AMLAttribute roleAttribute) throws AMLValidationException;

	boolean isAttributeMapped(AMLAttribute attribute);

	boolean hasAttributeMappings();
	
	public AMLValidationResultList validateUnmapAttribute(AMLAttribute roleAttribute);
	
	public AMLValidationResultList validateMapAttribute(AMLAttribute roleAttribute, AMLAttribute attribute);

}
