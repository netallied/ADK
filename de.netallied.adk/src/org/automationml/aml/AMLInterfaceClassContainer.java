/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLInterfaceClassContainer extends AMLDocumentElement {

	AMLValidationResultList validateCreateInterfaceClass(String name);

	AMLInterfaceClass createInterfaceClass(String name) throws AMLValidationException;
	
	AMLInterfaceClass createInterfaceClass(String name,	AMLInterfaceClass interfaceClass) throws AMLValidationException;

	AMLInterfaceClass getInterfaceClass(String name);

	Iterable<AMLInterfaceClass> getInterfaceClasses();

	int getInterfaceClassesCount();

	boolean hasClasses();
}
