/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.ArrayList;
import java.util.List;

import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLValidationResult;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;

public class AMLValidationResultListImpl implements AMLValidationResultList {
	private List<AMLValidationResult> results;

	public AMLValidationResultListImpl() {
	}

	/**
	 * Convenience method to create a list with one single AMLValidationResult element.
	 * 
	 * @param documentElement
	 * @param severity
	 * @param message
	 */
	public AMLValidationResultListImpl(AMLDocumentElement documentElement, Severity severity, String message) {
		this(new AMLValidationResultImpl(documentElement, severity, message));
	}

	public AMLValidationResultListImpl(AMLValidationResultImpl validationResult) {
		addDocumentElementValidationResult(validationResult);
	}

	@Override
	public void addDocumentElementValidationResult(AMLValidationResult result) {
		if (results == null)
			results = new ArrayList<AMLValidationResult>();
		results.add(result);
	}

	@Override
	public Iterable<AMLValidationResult> getValidationResults() {
		return results;
	}

	@Override
	public boolean isAnyOperationNotPermitted() {
		if (results == null)
			return false;
		for (AMLValidationResult result : results) {
			if (!result.isOperationPermitted())
				return true;
		}
		return false;
	}

	// TODO cache ok result
	@Override
	public boolean isOk() {
		if (results == null)
			return true;
		for (AMLValidationResult result : results) {
			if (result.getSeverity() != Severity.OK)
				return false;
		}
		return true;
	}

	public void assertNotInternalValidationSeverity() {
		if (results == null)
			return;

		for (AMLValidationResult result : results) {
			if (result.getSeverity() == Severity.AML_ERROR)
				throw new RuntimeException("AML_ERROR is reserverd for internal API!");
		}
	}

	@Override
	public void addDocumentElementValidationResultList(
			AMLValidationResultList result) {
		if (results == null)
			results = new ArrayList<AMLValidationResult>();
		results.addAll(((AMLValidationResultListImpl)result).results);
	}

}
