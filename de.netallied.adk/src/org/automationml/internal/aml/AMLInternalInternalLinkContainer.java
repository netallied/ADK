/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Set;

import org.automationml.aml.AMLInternalLink;
import org.automationml.aml.AMLInternalLinkContainer;

public interface AMLInternalInternalLinkContainer extends AMLInternalLinkContainer {
	Set<AMLInternalLink> _getInternalLinks();

	void removeInternalLink(AMLInternalLink internalLink);

	AMLInternalLinkImpl _createInternalLink(String name, AMLExternalInterfaceImpl refPartnerSideA,
			AMLExternalInterfaceImpl refPartnerSideB);
}
