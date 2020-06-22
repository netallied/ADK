/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

@SuppressWarnings("serial")
public class ParserException extends Exception {

	public ParserException(String string) {
		super(string);
	}

	public ParserException(Throwable cause) {
		super(cause);
	}

}
