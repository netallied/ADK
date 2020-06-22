/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.net.URL;

public interface AMLDocumentURLResolver {

	URL getUrl(AMLDocument document);

	boolean isRelative(AMLDocument baseDocument, AMLDocument referencedDocument);
}
