/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLInternalLinkContainer extends AMLDocumentElement {
	
	AMLInternalLink createInternalLink(String linkName, AMLExternalInterface refPartnerSideA, AMLExternalInterface refPartnerSideB)
			throws AMLValidationException;

	AMLValidationResultList validateCreateInternalLink(String linkName, AMLExternalInterface refPartnerSideA, AMLExternalInterface refPartnerSideB);

	Iterable<AMLInternalLink> getInternalLinks();

	int getInternalLinksCount();
}
