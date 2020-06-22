/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import java.util.HashMap;
import java.util.Map;

import org.automationml.aml.AMLDocumentElement;
import org.automationml.internal.aml.AbstractAMLDocumentElement;

public class IdentifierManager {

	private Map<AbstractAMLDocumentElement, Identifier<AbstractAMLDocumentElement>> elementToIdentifier = new HashMap<AbstractAMLDocumentElement, Identifier<AbstractAMLDocumentElement>>();

	void removeIdentifier(Identifier<? extends AbstractAMLDocumentElement> identifier) {
		elementToIdentifier.remove(identifier.getDocumentElement());
	}

	public Identifier<? extends AMLDocumentElement> getIdentifier(AbstractAMLDocumentElement documentElement) {
		if (documentElement == null)
			return null;
		Identifier<AbstractAMLDocumentElement> identifier = elementToIdentifier.get(documentElement);
		if (identifier == null) {
			identifier = new Identifier<AbstractAMLDocumentElement>(this);
			identifier.documentElement = documentElement;
			elementToIdentifier.put(documentElement, identifier);
		}
		identifier.incrementRefCount();
		return identifier;
	}

	public boolean isEmpty() {
		return elementToIdentifier.isEmpty();
	}

	void releaseIdentifier(AbstractAMLDocumentElement documentElement) {
		Identifier<? extends AbstractAMLDocumentElement> identifier = elementToIdentifier.get(documentElement);
		if (identifier == null)
			return;
		identifier.release();
	}

	void replaceDocumentElement(Identifier<AbstractAMLDocumentElement> identifier, AbstractAMLDocumentElement oldDocumentElement, AbstractAMLDocumentElement newDocumentElement) {
		elementToIdentifier.remove(oldDocumentElement);
		if (newDocumentElement != null)
			elementToIdentifier.put(newDocumentElement, identifier);
	}

}
