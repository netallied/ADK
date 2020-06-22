/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml;

import java.net.URL;

import org.automationml.aml.AMLSession;

public interface Deserializer {

	public void deserialize(URL url, AMLSession session) throws Exception;

}
