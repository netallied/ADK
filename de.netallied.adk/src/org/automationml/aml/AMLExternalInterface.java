/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.UUID;

public interface AMLExternalInterface extends AMLDocumentElement, AMLElement, AMLAttributeContainer, AMLRenamable {

	UUID getId();

	AMLInterfaceClass getInterfaceClass();

	String getName();

	String getSymbolName();

	boolean hasInternalLinks();

	Iterable<AMLInternalLink> getInternalLinks();

	int getInternalLinksCount();
}
