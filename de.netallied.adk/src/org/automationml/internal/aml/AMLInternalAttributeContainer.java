/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Map;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttributeContainer;

public interface AMLInternalAttributeContainer extends AMLAttributeContainer {

	Map<String, AMLAttribute> _getAttributes();
}
