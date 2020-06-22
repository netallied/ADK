/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml;

@SuppressWarnings("serial")
public class SavepointException extends Exception {

	public SavepointException() {
		super();
	}

	public SavepointException(String message) {
		super(message);
	}

}
