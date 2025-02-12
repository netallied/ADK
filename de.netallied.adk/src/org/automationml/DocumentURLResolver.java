/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml;

import java.net.URL;

public interface DocumentURLResolver {

	URL getResolvedURL(URL baseUrl, String pathString);

}
