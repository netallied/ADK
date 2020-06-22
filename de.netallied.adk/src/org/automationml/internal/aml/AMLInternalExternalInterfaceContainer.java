/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Map;
import java.util.UUID;

import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLExternalInterfaceContainer;

public interface AMLInternalExternalInterfaceContainer extends AMLExternalInterfaceContainer{

	Map<UUID, AMLExternalInterface> _getExternalInterfaces();

}
