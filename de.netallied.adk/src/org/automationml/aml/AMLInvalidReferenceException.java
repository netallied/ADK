/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.aml.AMLValidationResultListImpl;

@SuppressWarnings("serial")
public class AMLInvalidReferenceException extends AMLValidationException {

	public AMLInvalidReferenceException(AMLDocumentElement documentElement, String message) {
		super(new AMLValidationResultListImpl(documentElement, Severity.AML_ERROR, message));
	}

}
