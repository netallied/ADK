/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.internal.aml.AMLValidationResultListImpl;

public interface AMLValidationResultList {

	public static final AMLValidationResultListImpl EMPTY = new AMLValidationResultListImpl(){
		public void addDocumentElementValidationResult(AMLValidationResult result) {
			throw new RuntimeException("Adding to the EMPTY Result List is not permitted");
		}
	};

	void addDocumentElementValidationResult(AMLValidationResult result);
	
	void addDocumentElementValidationResultList(AMLValidationResultList result);

	Iterable<AMLValidationResult> getValidationResults();

	boolean isAnyOperationNotPermitted();

	boolean isOk();

}