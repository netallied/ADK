/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLValidationResult;

public class AMLValidationResultImpl implements AMLValidationResult {

	private AMLDocumentElement documentElement;
	private Severity severity;
	private String message;
	private boolean operationPermitted;

	public AMLValidationResultImpl(AMLDocumentElement documentElement, Severity severity, String message) {
		super();
		this.documentElement = documentElement;
		this.severity = severity;
		this.message = message;
		this.operationPermitted = severity != Severity.AML_ERROR;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public AMLDocumentElement getDocumentElement() {
		return documentElement;
	}

	@Override
	public boolean isOperationPermitted() {
		return operationPermitted;
	}

	public void setOperationPermitted(boolean operationPermitted) {
		this.operationPermitted = operationPermitted;
	}

	@Override
	public Severity getSeverity() {
		return severity;
	}

}
