/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml;

import java.io.OutputStream;

public interface DocumentLocation {

	OutputStream createOutputStream() throws Exception;

	boolean isPersistent();
}