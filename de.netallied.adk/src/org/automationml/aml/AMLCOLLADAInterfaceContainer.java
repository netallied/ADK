/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.aml.AMLCOLLADAInterface.RefType;

public interface AMLCOLLADAInterfaceContainer extends AMLDocumentElement {

	AMLValidationResultList validateCreateCOLLADAInterface(RefType explicit);
	
	AMLCOLLADAInterface createCOLLADAInterface(RefType explicit) throws AMLValidationException;
	
	AMLCOLLADAInterface getCOLLADAInterface();
}
