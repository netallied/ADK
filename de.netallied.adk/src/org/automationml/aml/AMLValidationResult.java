/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.internal.aml.AMLValidationResultImpl;

public interface AMLValidationResult {
	public static AMLValidationResult OK = new AMLValidationResultImpl(null, Severity.OK, "OK");

	public enum Severity {
		AML_ERROR /* harte regel */,
		VALIDATION_ERROR /* harte validation regel */,
		VALIDATION_WARNING /* softe validation regel */,
		OK
	}

	String getMessage();

	AMLDocumentElement getDocumentElement();

	boolean isOperationPermitted();

	Severity getSeverity();

}