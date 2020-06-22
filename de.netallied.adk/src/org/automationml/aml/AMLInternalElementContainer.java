/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.UUID;



public interface AMLInternalElementContainer extends AMLDocumentElement {
	
	AMLInternalElement createInternalElement(UUID id) throws AMLValidationException;
	
	AMLInternalElement createInternalElement() throws AMLValidationException;
	
	AMLInternalElement createInternalElement(AMLInternalElement internalElement) throws AMLValidationException;
	
	AMLInternalElement createInternalElement(AMLSystemUnitClass systemUnitClass) throws AMLValidationException;

	Iterable<AMLInternalElement> getInternalElements();

	int getInternalElementsCount();	

	AMLValidationResultList validateInternalElementCreate(UUID id) throws AMLValidationException;
		
	AMLValidationResultList validateInternalElementCreate();
	
	AMLValidationResultList validateInternalElementCreate(AMLSystemUnitClass systemUnitClass);
}
