/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLSession;
import org.automationml.aml.AMLDocumentElement;

public abstract class AMLInstruction {

	protected final AMLDeserializeIdentifier selfIdentifier;
	protected final AMLSession session;

	protected AMLInstruction(AMLSession session, AMLDeserializeIdentifier selfIdentifier) {
		this.selfIdentifier = selfIdentifier;
		this.session = session;
	}

	public AMLDeserializeIdentifier getSelfIdentifier() {
		return selfIdentifier;
	}

	public AMLDocumentElement getDocumentElement() {
		if (selfIdentifier instanceof AMLDeserializeIdentifier) {
			return selfIdentifier.getResolvedElement();
		}
		return null;
	}

	public abstract void execute() throws Exception;

	public abstract boolean hasUnresolvedDependencies();

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [selfIdentifier=" + selfIdentifier + "]";
	}

	protected String getLocationAsString(AMLDocumentElement documentElement, AMLDeserializeReferenceIdentifier referencedIdentifier){
		AMLLocationInFile location = referencedIdentifier.getFilePosition();
		String locationAsString = String.format(" in %s at line %d column %d", documentElement.getDocument().getDocumentLocation(), location.getLineNumber(), location.getColumnNumber());
		return locationAsString;
	}
}
