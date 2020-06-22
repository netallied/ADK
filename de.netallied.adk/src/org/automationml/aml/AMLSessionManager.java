/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.internal.aml.AMLSessionImpl;

public class AMLSessionManager {

	public static final AMLSessionManager amlSessionManager = new AMLSessionManager();

	public AMLSession createSession() {
		return new AMLSessionImpl();
	}
}
