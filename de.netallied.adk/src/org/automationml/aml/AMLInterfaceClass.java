/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;


public interface AMLInterfaceClass extends AMLDocumentElement, AMLElement, AMLAttributeContainer, AMLInterfaceClassContainer, AMLRenamable {

	void setBaseInterfaceClass(AMLInterfaceClass baseclass) throws AMLValidationException;

	void unsetBaseInterfaceClass() throws AMLValidationException;

	String getClassPath();
	
	int getReferrerCount();

	Iterable<AMLDocumentElement> getReferrers();

	AMLInterfaceClass getBaseInterfaceClass();

	AMLValidationResultList validateSetBaseInterfaceClass(AMLInterfaceClass baseClass);
}
