/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.UUID;

public interface AMLGroupContainer extends AMLDocumentElement {

	AMLGroup createGroup() throws AMLValidationException;

	AMLGroup createGroup(UUID id) throws AMLValidationException;
	
	AMLGroup createGroup(AMLGroup group) throws AMLValidationException;

	Iterable<AMLGroup> getGroups();

	int getGroupsCount();
	
	AMLValidationResultList validateGroupCreate();
}
