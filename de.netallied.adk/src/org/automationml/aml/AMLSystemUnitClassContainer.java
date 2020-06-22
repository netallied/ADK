/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLSystemUnitClassContainer extends AMLDocumentElement {

	AMLValidationResultList validateCreateSystemUnitClass(String name);

	AMLSystemUnitClass createSystemUnitClass(String name) throws AMLValidationException;
	
	AMLSystemUnitClass createSystemUnitClass(String name, AMLSystemUnitClass systemUnitClass) throws AMLValidationException;

	AMLSystemUnitClass getSystemUnitClass(String name);

	Iterable<AMLSystemUnitClass> getSystemUnitClasses();

	int getSystemUnitClassesCount();

	boolean hasClasses();
}
