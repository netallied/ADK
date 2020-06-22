/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;


public interface AMLAttributeContainer extends AMLDocumentElement {

	AMLValidationResultList validateCreateAttribute(String name);
	
	Iterable<AMLAttribute> getAttributes();

	int getAttributesCount();

	boolean hasAttributes();

	AMLAttribute getAttribute(String name);

	AMLAttribute createAttribute(String name) throws AMLValidationException;
	
	AMLAttribute createAttribute(String name, AMLAttribute attribute) throws AMLValidationException;


}
