/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;


public interface AMLRoleClass extends AMLDocumentElement, AMLElement, AMLAttributeContainer, AMLRoleClassContainer, AMLExternalInterfaceContainer, AMLRenamable {

	void setBaseRoleClass(AMLRoleClass baseclass) throws AMLValidationException;

	void unsetBaseRoleClass() throws AMLValidationException;

	String getClassPath();
	
	int getReferrerCount();

	Iterable<AMLDocumentElement> getReferrers();

	AMLRoleClass getBaseRoleClass();

	AMLValidationResultList validateSetBaseRoleClass(AMLRoleClass baseClass);
}
