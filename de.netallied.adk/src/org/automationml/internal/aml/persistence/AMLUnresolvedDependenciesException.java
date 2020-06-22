/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import java.util.Collection;

import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.internal.aml.AMLValidationResultImpl;
import org.automationml.internal.aml.AMLValidationResultListImpl;

@SuppressWarnings("serial")
public class AMLUnresolvedDependenciesException extends AMLValidationException {
	private Collection<AMLInstruction> unresolvedInstructions;

	public AMLUnresolvedDependenciesException(Collection<AMLInstruction> unresolvedInstructions) {
		this.unresolvedInstructions = unresolvedInstructions;
	}

	public Collection<AMLInstruction> getUnresolvedInstructions() {
		return unresolvedInstructions;
	}

	@Override
	public AMLValidationResultList getValidationResultList() {
		AMLValidationResultListImpl resultList = new AMLValidationResultListImpl();
		for (AMLInstruction instruction : unresolvedInstructions) {
			AMLDeserializeIdentifier selfIdentifier = instruction.getSelfIdentifier();
			String message = selfIdentifier.getName() + " is undefined";
			//			if (selfIdentifier instanceof AMLDeserializeIdentifier) {
			//				message += instruction.getLocationAsString(
			//						instruction.getDocumentElement(),
			//						(AMLDeserializeReferenceIdentifier) selfIdentifier);
			//
			//			}
			AMLValidationResultImpl result = new AMLValidationResultImpl(instruction.getDocumentElement(), Severity.AML_ERROR, message);
			resultList.addDocumentElementValidationResult(result);
		}
		return resultList;
	}

}
