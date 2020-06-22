/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.UUID;

public interface AMLGroup extends AMLDocumentElement, AMLElement, AMLMirrorContainer, AMLGroupContainer, 
	AMLAttributeContainer, AMLExternalInterfaceContainer, AMLRenamable {

	public static final String AUTOMATION_ML_ROLE_GROUP_PATH = "AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Group";

	UUID getId();
}
