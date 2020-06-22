/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.automationml.DocumentLocation;
import org.automationml.DocumentURLResolver;
import org.automationml.Savepoint;
import org.automationml.URLDocumentLocation;
import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLSession;
import org.automationml.aml.AMLSessionChangeListener;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.aml.AMLValidatorException;
import org.automationml.aml.AMLValidatorFactory;
import org.automationml.internal.AbstractDocumentElementChange;
import org.automationml.internal.Change;
import org.automationml.internal.IdentifierManager;
import org.automationml.internal.ProgressMonitor;
import org.automationml.internal.ReadOnlyIterable;
import org.automationml.internal.SavepointManager;

public class AMLSessionImpl extends AbstractAMLObject implements AMLSession {

	private class CreateDocumentChange extends AbstractDocumentElementChange {

		private URL url;

		public CreateDocumentChange(AMLDocumentImpl documentElement, URL url) {
			super(documentElement);
			this.url = url;
		}

		@Override
		public void redo() throws Exception {
			AMLDocumentImpl document;
			if (url == null)
				document = AMLSessionImpl.this._createAMLDocument();
			else
				document = AMLSessionImpl.this._createAMLDocument(url);
			identifier.setDocumentElement(document);
		}

		@Override
		protected void notifyChangeListenersBeforeUndo() {
			AMLDocument document = (AMLDocument) getDocumentElement();
			documentRemoving(document);
		}

		@Override
		protected void notifyChangeListenersAfterUndo() {
		}

		@Override
		protected void notifyChangeListenersBeforeRedo() {
		}

		@Override
		protected void notifyChangeListenersAfterRedo() {
			AMLDocument document = (AMLDocument) getDocumentElement();
			documentAdded(document);
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement element = getDocumentElement();
			if (element == null)
				return;
			element._delete();
			identifier.unsetDocumentElement();
		}

		@Override
		public boolean mergeInto(Change change) {
			return false;
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
		}

		@Override
		protected void _delete() {
		}
	}

	private final SavepointManager savepointManager = new SavepointManager(this);
	private final AMLDocumentManager documentManager = new AMLDocumentManager(this);
	private IdentifierManager identifierManager = new IdentifierManager();
	private Collection<AMLSessionChangeListener> sessionChangeListeners = new HashSet<AMLSessionChangeListener>();
	private int sessionChangeTransactionsCount;
	private DocumentURLResolver documentURLResolver;
	private ProgressMonitor progressMonitor;
	private AMLValidator validator;
	private boolean notifyEnabled = true;

	@Override
	public AMLDocumentImpl createAMLDocument() {
		AMLDocumentImpl document = _createAMLDocument();

		if (getSavepointManager().hasCurrentSavepoint()) {
			CreateDocumentChange change = new CreateDocumentChange(document, null);
			getSavepointManager().addChange(change);
		}
		documentAdded(document);

		return document;
	}

	private AMLDocumentImpl _createAMLDocument() {
		AMLDocumentImpl document = new AMLDocumentImpl(this);
		getDocumentManager().registerDocument(document);
		return document;
	}

	@Override
	public AMLDocument getAMLDocumentByDocumentLocation(DocumentLocation documentLocation) throws Exception {
		AMLDocument document = getDocumentManager().getDocument(documentLocation);
		return document;
	}

	// TODO im Ggs. zu loadAMLDocument wird das document *nicht* eingeladen (diese Methode wird nur für den Deserializer benötigt)
	@Override
	public AMLDocument createAMLDocument(URL url) {
		AMLDocumentImpl document = (AMLDocumentImpl) getDocumentManager().getDocument(new URLDocumentLocation(url));
		if (document != null)
			return document;

		document = _createAMLDocument(url);

		if (getSavepointManager().hasCurrentSavepoint()) {
			CreateDocumentChange change = new CreateDocumentChange(document, url);
			getSavepointManager().addChange(change);
		}
		documentAdded(document);
		return document;
	}

	private AMLDocumentImpl _createAMLDocument(URL url) {
		AMLDocumentImpl document = new AMLDocumentImpl(this);
		DocumentLocation location = new URLDocumentLocation(url);
		getDocumentManager().registerDocument(document, location);
		
		document.incrementChangesCount();
		
		return document;
	}

	@Override
	public AMLDocument loadAMLDocument(URL url) throws Exception {
		AMLDocument document = getDocumentManager().getDocument(new URLDocumentLocation(url));
		if (document != null)
			return document;

		try {
			beginSessionChanges();
			document = getDocumentManager().openDocument(url);

			for (AMLDocument amlDocument : getDocuments()) {
				amlDocument.unsetDirty();
			}
		} finally {
			endSessionChanges();
		}
		return document;
	}
	
	public SavepointManager getSavepointManager() {
		return savepointManager;
	}

	@Override
	public Savepoint createSavepoint() {
		return savepointManager.createSavepoint();
	}

	@Override
	public boolean hasCurrentSavepoint() {
		return savepointManager.hasCurrentSavepoint();
	}

	public AMLDocumentManager getDocumentManager() {
		return documentManager;
	}

	public IdentifierManager getIdentifierManager() {
		return identifierManager;
	}

	@Override
	public void beginSessionChanges() {
		sessionChangeTransactionsCount++;
		if (sessionChangeTransactionsCount > 1)
			return;
		for (AMLSessionChangeListener changeListener : sessionChangeListeners) {
			changeListener.sessionChangeTransactionBegin();
		}
	}

	@Override
	public void endSessionChanges() {
		sessionChangeTransactionsCount--;
		if (sessionChangeTransactionsCount > 0)
			return;
		for (AMLSessionChangeListener changeListener : sessionChangeListeners) {
			changeListener.sessionChangeTransactionEnd();
		}
	}

	public void documentAdded(AMLDocument document) {
		for (AMLSessionChangeListener changeListener : sessionChangeListeners) {
			changeListener.sessionChangeDocumentAdded(document);
		}
	}

	public void documentRemoving(AMLDocument document) {
		for (AMLSessionChangeListener changeListener : sessionChangeListeners) {
			changeListener.sessionChangeDocumentRemoving(document);
		}
	}

	@Override
	public void addChangeListener(AMLSessionChangeListener changeListener) {
		sessionChangeListeners.add(changeListener);
	}

	@Override
	public void removeChangeListener(AMLSessionChangeListener changeListener) {
		sessionChangeListeners.remove(changeListener);
	}


	@Override
	public int getDocumentsCount() {
		return documentManager.getDocumentsCount();
	}

	@Override
	public Iterable<AMLDocument> getDocuments() {
		return new ReadOnlyIterable<AMLDocument>(documentManager.getDocuments());
	}

	@Override
	public void setDocumentURLResolver(DocumentURLResolver documentURLResolver) {
		this.documentURLResolver = documentURLResolver;
	}

	@Override
	public void setProgressMonitor(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	@Override
	public DocumentURLResolver getDocumentURLResolver() {
		return documentURLResolver;
	}

	@Override
	public ProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public void unsetValidator() {
		if (this.validator == null)
			return;
		this.validator.dispose();
		this.validator = null;
	}

	public void setValidator(AMLValidatorFactory validatorFactory) throws AMLValidatorException {
		if (this.validator != null)
			throw new AMLValidatorException("Validator already set. Unset Validator first before setting another.");

		try {
			this.validator = validatorFactory.createValidator(this);
		} catch (AMLValidationException e) {
			throw new AMLValidatorException("");
		}

		AMLValidationResultList validationResultList = validator.validateSession();

		if (validationResultList.isAnyOperationNotPermitted()) {
			validator.dispose();
			throw new AMLValidatorException("Validator can not be attached in strict mode because it reports errors.", validationResultList);
		}

		if (validationResultList.isOk())
			return;

		//		for (AMLDocumentChangeListener listener : validationListeners) {
		//			for (AMLValidationResult result : validationResultList.getValidationResults()) {
		//
		//				listener.documentChangeElementValidated(result);
		//			}
		//		}
	}

	public AMLValidator getValidator() {
		return validator;
	}

	@Override
	public AMLDocument getRootDocument(AMLDocument documentInScope) {
		return documentManager.getRootDocument(documentInScope);
	}

	@Override
	public void enableNotify(boolean enable) {
		this.notifyEnabled = enable;		
	}

	public boolean getNotifyEnable() {
		return notifyEnabled;
	}
}
