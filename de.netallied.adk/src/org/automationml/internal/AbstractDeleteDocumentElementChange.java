/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLValidationException;
import org.automationml.internal.aml.AMLDocumentImpl;
import org.automationml.internal.aml.AbstractAMLDocumentElement;

public abstract class AbstractDeleteDocumentElementChange<T extends AbstractAMLDocumentElement> extends AbstractDocumentElementChange<T> {
	
	@Override
	public AMLDocument getDocument() {
		T documentElement = parentIdentifier.getDocumentElement();
		if( documentElement == null)
			return null;
		return documentElement.getDocument();
	}

	protected Identifier<T> parentIdentifier;

	public AbstractDeleteDocumentElementChange(T documentElement) {
		super(documentElement);
	}

	@Override
	protected void initializeIdentifiers(IdentifierManager identifierManager) {
		T parent = (T) getDocumentElement().getParent();
		if (parent != null)
			parentIdentifier = (Identifier<T>) identifierManager.getIdentifier(parent);
	}

	@Override
	protected void _delete() {
		if (parentIdentifier != null)
			parentIdentifier.release();
	}

	@Override
	public void redo() throws AMLValidationException {
		T element = getDocumentElement();
		element._delete();
	}

	@Override
	public boolean mergeInto(Change change) {
		return true;
	}

	@Override
	protected void notifyChangeListenersBeforeUndo() {
		
	}

	@Override
	protected void notifyChangeListenersAfterUndo() {
		T parent = parentIdentifier.getDocumentElement();
		AMLDocumentImpl document = (AMLDocumentImpl) parent.getDocument();
		document.notifyElementCreated(getDocumentElement(), parent);
	}

	@Override
	protected void notifyChangeListenersBeforeRedo() {
		if (getDocumentElement() == null)
			return;
		T parent = parentIdentifier.getDocumentElement();
		AMLDocumentImpl document = (AMLDocumentImpl) parent.getDocument();
		document.notifyElementDeleting(getDocumentElement(), parent);
	}

	@Override
	protected void notifyChangeListenersAfterRedo() {
	}
}
