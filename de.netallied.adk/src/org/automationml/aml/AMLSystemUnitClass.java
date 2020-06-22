/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLSystemUnitClass extends AMLDocumentElement, AMLElement, AMLAttributeContainer, AMLSystemUnitClassContainer, AMLInternalElementContainer,
		AMLSupportedRoleClassContainer, AMLExternalInterfaceContainer, AMLGroupContainer, AMLFrameAttributeContainer, AMLInternalLinkContainer,
		AMLRenamable, AMLCOLLADAInterfaceContainer {

	void setBaseSystemUnitClass(AMLSystemUnitClass baseclass) throws AMLValidationException;

	void unsetBaseSystemUnitClass() throws AMLValidationException;

	String getClassPath();
	
	int getReferrerCount();

	Iterable<AMLDocumentElement> getReferrers();

	AMLSystemUnitClass getBaseSystemUnitClass();
	
	AMLValidationResultList validateSetBaseSystemUnitClass(AMLSystemUnitClass baseclass);
}
