/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.UUID;

public interface AMLExternalInterfaceContainer extends AMLDocumentElement  {
	
	AMLValidationResultList validateCreateExternalInterface(AMLInterfaceClass interfaceClass);
	
	Iterable<AMLExternalInterface> getExternalInterfaces();

	int getExternalInterfacesCount();

	AMLExternalInterface getExternalInterface(UUID id);

	AMLExternalInterface createExternalInterface(AMLInterfaceClass interfaceClass) throws AMLValidationException;
	
	AMLExternalInterface createExternalInterface(UUID id, AMLInterfaceClass interfaceClass) throws AMLValidationException;
}
