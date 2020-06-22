/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml;

@SuppressWarnings("serial")
public class SerializerException extends Exception {

	public SerializerException(String message) {
		super(message);
	}

	public SerializerException(Throwable cause) {
		super(cause);
	}
}
