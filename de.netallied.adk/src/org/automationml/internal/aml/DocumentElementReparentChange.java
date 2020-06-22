/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import org.automationml.aml.AMLDocumentElement;
import org.automationml.internal.AbstractDocumentElementChange;
import org.automationml.internal.Change;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;

public class DocumentElementReparentChange <T extends AbstractAMLDocumentElement, P extends AbstractAMLDocumentElement> extends AbstractDocumentElementChange<T> {

	protected Identifier<P> oldParent;
	protected Identifier<P> newParent;
	protected Identifier<T> newBefore;
	protected Identifier<T> newAfter;
	protected Identifier<T> oldBefore;
	protected Identifier<T> oldAfter;

	public DocumentElementReparentChange(T instanceHierarchy) {
		super(instanceHierarchy);
	}
		
	public void setNewParent(AMLDocumentElement newParent, AMLDocumentElement before, AMLDocumentElement after) {
		AMLSessionImpl session = (AMLSessionImpl) newParent.getSession();
		IdentifierManager identifierManager = session.getIdentifierManager();
		AMLDocumentImpl newDocument = (AMLDocumentImpl) newParent.getDocument();
		this.newParent = (Identifier<P>) identifierManager.getIdentifier((P)newParent);
		this.newBefore = (Identifier<T>) identifierManager.getIdentifier((T)before);
		this.newAfter = (Identifier<T>) identifierManager.getIdentifier((T)after);
		newDocument.incrementChangesCount();
	}

		@Override
		public void undo() throws Exception {
			T elementToReparent =  getDocumentElement();
			T elementBefore = oldBefore == null ? null : oldBefore.getDocumentElement();
			T elementAfter = oldAfter == null ? null : oldAfter.getDocumentElement();
			P oldParentDocument = oldParent.getDocumentElement();
			P newParentDocument = newParent.getDocumentElement();			
			elementToReparent._reparent(newParentDocument, oldParentDocument, elementAfter, elementBefore);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			T elementToReparent =  getDocumentElement();
			oldParent = (Identifier<P>) identifierManager.getIdentifier((P)elementToReparent.getParent());
			T previous = (T)elementToReparent._getBefore();
			oldBefore = (Identifier<T>) identifierManager.getIdentifier(previous);
			T after = (T)elementToReparent._getAfter();
			oldAfter = (Identifier<T>) identifierManager.getIdentifier(after);
		}

		@Override
		protected void _delete() {
			// this is not really required, but helpful for debugging
			oldParent.release();
			newParent.release();
			if (oldBefore != null)
				oldBefore.release();
			if (oldAfter != null)
				oldAfter.release();
			if (newBefore != null)
				newBefore.release();
			if (newAfter != null)
				newAfter.release();
		}

		@Override
		public void redo() throws Exception {
			T elementToReparent = (T) getDocumentElement();
			elementToReparent._reparent(oldParent.getDocumentElement(), newParent.getDocumentElement(), 
					newBefore == null ? null : newBefore.getDocumentElement(), 
					newAfter == null ? null : newAfter.getDocumentElement());		
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof DocumentElementReparentChange))
				return false;
			DocumentElementReparentChange change = (DocumentElementReparentChange) _change;
			change.setNewParent(newParent.getDocumentElement(), newBefore.getDocumentElement(), newAfter.getDocumentElement());
			return true;
		}

		@Override
		protected void notifyChangeListenersBeforeUndo() {
			// 
		}

		@Override
		protected void notifyChangeListenersAfterUndo() {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocument();
			AMLDocumentImpl document1 = (AMLDocumentImpl) newParent.getDocumentElement().getDocument();
			AMLDocumentImpl document2 = (AMLDocumentImpl) oldParent.getDocumentElement().getDocument();
			document1.notifyElementModified(newParent.getDocumentElement());
			document2.notifyElementModified(oldParent.getDocumentElement());
			if (document.equals(document2))
				document1.decrementChangesCount();
			else
				document2.decrementChangesCount();
		}

		@Override
		protected void notifyChangeListenersBeforeRedo() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void notifyChangeListenersAfterRedo() {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocument();
			AMLDocumentImpl document1 = (AMLDocumentImpl) newParent.getDocumentElement().getDocument();
			AMLDocumentImpl document2 = (AMLDocumentImpl) oldParent.getDocumentElement().getDocument();
			document1.notifyElementModified(newParent.getDocumentElement());
			document2.notifyElementModified(oldParent.getDocumentElement());		

			if (document.equals(document2))
				document2.incrementChangesCount();
			else
				document1.incrementChangesCount();
		}

}
