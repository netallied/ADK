/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml;

@SuppressWarnings("serial")
public class DeserializerException extends Exception {

	public DeserializerException(String message) {
		super(message);
	}

	public DeserializerException(Throwable cause) {
		super(cause);
	}

}