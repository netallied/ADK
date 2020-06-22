/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLInternalLink extends AMLDocumentElement, AMLElement, AMLRenamable{
	AMLExternalInterface getRefPartnerSideA();
	
	AMLExternalInterface getRefPartnerSideB();
}
