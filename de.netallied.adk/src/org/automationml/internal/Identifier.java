/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import org.automationml.internal.aml.AbstractAMLDocumentElement;

public class Identifier<T extends AbstractAMLDocumentElement> {

	private final IdentifierManager identifierManager;
	T documentElement;

	private int refCount;

	public Identifier(IdentifierManager identifierManager) {
		super();
		this.identifierManager = identifierManager;
	}

	public void release() {
		if (refCount == 0)
			return;
		refCount--;
		if (refCount > 0)
			return;
		identifierManager.removeIdentifier(this);
		documentElement = null;
	}

	void incrementRefCount() {
		refCount++;
	}

	public T getDocumentElement() {
		return (T) documentElement;
	}

	public void setDocumentElement(T documentElement) {
		identifierManager.replaceDocumentElement((Identifier<AbstractAMLDocumentElement>) this, this.documentElement, documentElement);
		this.documentElement = documentElement;
	}

	public void unsetDocumentElement() {
		setDocumentElement(null);
	}
}
