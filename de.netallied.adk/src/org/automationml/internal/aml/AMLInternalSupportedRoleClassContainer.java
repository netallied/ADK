/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Map;

import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLSupportedRoleClassContainer;

public interface AMLInternalSupportedRoleClassContainer extends AMLSupportedRoleClassContainer {

	Map<AMLRoleClass, AMLSupportedRoleClass> _getSupportedRoleClasses();

}
