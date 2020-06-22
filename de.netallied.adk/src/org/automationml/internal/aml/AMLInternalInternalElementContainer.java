/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.LinkedHashMap;
import java.util.UUID;

import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalElementContainer;

public interface AMLInternalInternalElementContainer extends AMLInternalElementContainer {

	LinkedHashMap<UUID, AMLInternalElement> _getInternalElements();

}
