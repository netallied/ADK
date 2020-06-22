/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

@SuppressWarnings("serial")
public class AMLValidationException extends Exception {

	private AMLValidationResultList validationResultList;

	public AMLValidationException() {
	}
	
	public AMLValidationException(AMLValidationResultList validationResultList) {
		this.validationResultList = validationResultList;
	}

	public AMLValidationResultList getValidationResultList() {
		return validationResultList;
	}

	@Override
	public String getMessage() {
		String message = "";
		if (validationResultList == null)
			return message;

		Iterable<AMLValidationResult> validationResults = validationResultList.getValidationResults();
		if (validationResults == null)
			return message;

		for (AMLValidationResult result : validationResults) {
			String msg = result.getMessage();
			if (msg != null && !msg.isEmpty())
				message += msg + "\n";
		}

		return message;
	}

}
