/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;


@SuppressWarnings("serial")
public class AMLValidatorException extends Exception {

	public AMLValidatorException(String message) {
		super(message);
	}

	private AMLValidationResultList sessionValidationResult;

	public AMLValidatorException(String message, AMLValidationResultList sessionValidationResult) {
		super(message);
		this.sessionValidationResult = sessionValidationResult;
	}

	public AMLValidationResultList getSessionValidationResult() {
		return sessionValidationResult;
	}
}
