/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.aml.AMLValidationResultListImpl;

@SuppressWarnings("serial")
public class AMLNameAlreadyInUseException extends AMLValidationException {

	public AMLNameAlreadyInUseException(AMLDocumentElement documentElement) {
		super(new AMLValidationResultListImpl(documentElement, Severity.AML_ERROR, "Name already in use"));
	}

	public AMLNameAlreadyInUseException(AMLValidationResultList validationResultList) {
		super(validationResultList);
	}

}
