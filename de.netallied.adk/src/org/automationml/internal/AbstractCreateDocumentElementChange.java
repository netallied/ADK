/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import org.automationml.internal.aml.AMLDocumentImpl;
import org.automationml.internal.aml.AbstractAMLDocumentElement;

public abstract class AbstractCreateDocumentElementChange extends AbstractDocumentElementChange {
	protected Identifier parentIdentifier;

	public AbstractCreateDocumentElementChange(AbstractAMLDocumentElement documentElement) {
		super(documentElement);
	}

	@Override
	protected void initializeIdentifiers(IdentifierManager identifierManager) {
		AbstractAMLDocumentElement parent = (AbstractAMLDocumentElement) getDocumentElement().getParent();
		this.parentIdentifier = identifierManager.getIdentifier(parent);
	}

	@Override
	public void undo() throws Exception {
		AbstractAMLDocumentElement element = getDocumentElement();
		if (element == null)
			return;
		element._delete();
		identifier.setDocumentElement(null);
	}

	@Override
	protected void _delete() {
		if (parentIdentifier != null)
			parentIdentifier.release();
		parentIdentifier = null;
	}

	@Override
	public boolean mergeInto(Change change) {
		return false;
	}
	
	@Override
	protected void notifyChangeListenersBeforeUndo() {
		AbstractAMLDocumentElement parent = parentIdentifier.getDocumentElement();
		if( parent == null || parent.isDeleted())
			return;
		AMLDocumentImpl document = (AMLDocumentImpl) parent.getDocument();
		// the object may already be deleted
		if (getDocumentElement() != null && !getDocumentElement().isDeleted())
			document.notifyElementDeleting(getDocumentElement(), parent);
	}

	@Override
	protected void notifyChangeListenersAfterUndo() {
	}
	
	@Override
	protected void notifyChangeListenersBeforeRedo() {
	}

	@Override
	protected void notifyChangeListenersAfterRedo() {
		AbstractAMLDocumentElement parent = parentIdentifier.getDocumentElement();
		AMLDocumentImpl document = (AMLDocumentImpl) parent.getDocument();
		document.notifyElementCreated(getDocumentElement(), parent);
	}
}
