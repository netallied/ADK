/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLRoleClassContainer extends AMLDocumentElement {

	AMLValidationResultList validateCreateRoleClass(String name);

	AMLRoleClass createRoleClass(String name) throws AMLValidationException;
	
	AMLRoleClass createRoleClass(String name, AMLRoleClass roleClass) throws AMLValidationException;

	AMLRoleClass getRoleClass(String name);

	Iterable<AMLRoleClass> getRoleClasses();

	int getRoleClassesCount();

	boolean hasClasses();
}
