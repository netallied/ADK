/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import org.automationml.aml.AMLDocumentElement;

public class AMLDeserializeReferenceIdentifier {
	private final String name;
	private AMLDeserializeIdentifier referencedIdentifier;
	private final AMLLocationInFile filePosition;

	public AMLDeserializeReferenceIdentifier(String name, AMLLocationInFile filePosition) {
		this.name = name;
		this.filePosition = filePosition;
	}

	public AMLDocumentElement getResolvedElement() {
		if (referencedIdentifier == null)
			return null;
		return referencedIdentifier.getResolvedElement();
	}

	public boolean isResolved() {
		return getResolvedElement() != null;
	}

	public String getName() {
		return name;
	}

	public void setReferencedIdentifier(AMLDeserializeIdentifier referencedIdentifier) {
		this.referencedIdentifier = referencedIdentifier;
	}

	public AMLLocationInFile getFilePosition() {
		return filePosition;
	}
}
