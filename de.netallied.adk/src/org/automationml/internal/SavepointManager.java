/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import java.util.ArrayList;
import java.util.List;

import org.automationml.SavepointException;
import org.automationml.aml.AMLDocument;
import org.automationml.internal.aml.AMLDocumentImpl;
import org.automationml.internal.aml.AMLSessionImpl;

public class SavepointManager {

	private List<SavepointImpl> savepoints = new ArrayList<SavepointImpl>();
	private List<SavepointImpl> savepointBranch  = new ArrayList<SavepointImpl>();
	private int currentSavepointIndex = -1;
	private AMLSessionImpl session;

	public SavepointManager(AMLSessionImpl session) {
		this.session = session;
	}

	public SavepointImpl createSavepoint() {
		invalidateAllSucceedingSavepoints();

		SavepointImpl currentSavepoint = getCurrentSavepoint();
		if (currentSavepoint != null && !currentSavepoint.hasChanges())
			return currentSavepoint;

		currentSavepoint = new SavepointImpl(this);
		addSavepoint(currentSavepoint);
		return currentSavepoint;
	}
	
	public SavepointImpl createTemporarySavepoint() {
		invalidateAllSucceedingSavepoints();

		SavepointImpl currentSavepoint = getCurrentSavepoint();
		if (currentSavepoint != null && !currentSavepoint.hasChanges())
			return currentSavepoint;

		currentSavepoint = new SavepointImpl(this);
		addSavepoint(currentSavepoint);
		return currentSavepoint;
	}

	private void invalidateAllSucceedingSavepoints() {
		// delete previous branch
		for (SavepointImpl savepoint : savepointBranch) {
			savepoint._delete();
		}
		savepointBranch.clear();
		if (currentSavepointIndex >= 0 && currentSavepointIndex != savepoints.size() - 1) {
			for (int i = currentSavepointIndex + 1; i < savepoints.size(); i++) {
				SavepointImpl savepoint = savepoints.get(i);
				//savepoint._delete();
				savepointBranch.add(savepoint);
			}
			savepoints = savepoints.subList(0, currentSavepointIndex + 1);
		}
	}

	private SavepointImpl getCurrentSavepoint() {
		if (currentSavepointIndex == -1)
			return null;
		return savepoints.get(currentSavepointIndex);
	}

	public void addSavepoint(SavepointImpl savepoint) {
		savepoints.add(savepoint);
		currentSavepointIndex = savepoints.size() - 1;
	}

	public void removeSavepoint(SavepointImpl savepoint) {
		int savepointIndex = savepoints.indexOf(savepoint);

		if (savepointIndex == -1)
			return;

		if (savepoints.size() <= 2) {
			boolean firstSavepointDeleted = savepoints.get(0).isDeleted();
			if (firstSavepointDeleted) {
				savepoints.get(0)._delete();
				savepoints.get(1)._delete();
				savepoints.clear();
				currentSavepointIndex = -1;
				return;
			}
		}

		if (savepointIndex == 0) {
			savepoint.deleted = true;
		} else {
			SavepointImpl previousSavepoint = savepoints.get(savepointIndex - 1);
			previousSavepoint.mergeWith(savepoint);
		}

		if (savepointIndex != 0 || savepoints.size() == 1) {
			savepoint._delete();
			savepoints.remove(savepointIndex);
		}

		if (currentSavepointIndex == savepointIndex && currentSavepointIndex > 0)
			currentSavepointIndex--;

		if (savepoints.isEmpty())
			currentSavepointIndex = -1;

	}

	public void addChange(Change change) {
		AMLDocumentImpl document = SavepointManager.getDocument(change);
		if( document != null)
			document.incrementChangesCount();

		SavepointImpl savepoint = getCurrentSavepoint();
		if (savepoint == null)
			return;

		//invalidateAllSucceedingSavepoints();
		change = savepoint.addChange(change);
	}

	static AMLDocumentImpl getDocument(Change change) {
		if(!( change instanceof AbstractDocumentElementChange))
			return null;
		AbstractDocumentElementChange<?> documentElementChange = (AbstractDocumentElementChange<?>) change;
		AMLDocument document = documentElementChange.getDocument();
		if( !(document instanceof AMLDocumentImpl))
			return null;
		return (AMLDocumentImpl) document;
	}

	public void restore(SavepointImpl targetSavepoint) throws SavepointException, Exception {
		if (targetSavepoint.isDeleted())
			throwSavepointDeletedException();

		int targetSavepointIndex = savepoints.indexOf(targetSavepoint);
		if (targetSavepointIndex == -1)
			throwSavepointDeletedException();

		if (targetSavepointIndex <= currentSavepointIndex) {
			
			int startSavepointIndex = currentSavepointIndex;
			for (int i = currentSavepointIndex; i >= targetSavepointIndex; i--) {
				SavepointImpl savepoint = savepoints.get(i);
				if (!savepoint.restored) {
					startSavepointIndex = i;
					break;
				}
			}

			// notify listeners before changes have been applied
			session.beginSessionChanges();
			for (int i = startSavepointIndex; i >= targetSavepointIndex; i--) {
				SavepointImpl savepoint = savepoints.get(i);
				if (!savepoint.isDeleted()) {
					savepoint.notifyChangeListenersBeforeUndo();
				}
			}

			for (int i = startSavepointIndex; i >= targetSavepointIndex; i--) {
				SavepointImpl savepoint = savepoints.get(i);
				if (!savepoint.isDeleted())
					savepoint.undo();
			}

			// notify listeners after changes have been applied so that the model's validity is guaranteed
			for (int i = startSavepointIndex; i >= targetSavepointIndex; i--) {
				SavepointImpl savepoint = savepoints.get(i);
				if (!savepoint.isDeleted()) {
					savepoint.notifyChangeListenersAfterUndo();
				}
			}
			session.endSessionChanges();

		} else {
			boolean removeFirstSavepoint = false;

			int startSavepointIndex = currentSavepointIndex;
			for (int i = currentSavepointIndex; i < targetSavepointIndex; i++) {
				SavepointImpl savepoint = savepoints.get(i);
				if (savepoint.restored) {
					startSavepointIndex = i;
					break;
				}
			}
			
			// notify listeners before changes have been applied
			session.beginSessionChanges();
			for (int i = startSavepointIndex; i < targetSavepointIndex; i++) {
				SavepointImpl savepoint = savepoints.get(i);
				if (!savepoint.isDeleted())
					savepoint.notifyChangeListenersBeforeRedo();
			}
//			session.endSessionChanges();

			for (int i = startSavepointIndex; i < targetSavepointIndex; i++) {
				SavepointImpl savepoint = savepoints.get(i);
				if (savepoint.isDeleted()) {
					if (targetSavepointIndex == 0) {
						throwSavepointDeletedException();
					}
					if (i == 0)
						removeFirstSavepoint = true;
				}
				savepoint.redo();
			}

			// notify listeners after all changes have been applied so that the model's validity is guaranteed
//			session.beginSessionChanges();
			for (int i = startSavepointIndex; i < targetSavepointIndex; i++) {
				SavepointImpl savepoint = savepoints.get(i);
				if (!savepoint.isDeleted())
					savepoint.notifyChangeListenersAfterRedo();
			}
			session.endSessionChanges();

			if (removeFirstSavepoint) {
				SavepointImpl firstSavepoint = savepoints.get(0);
				firstSavepoint._delete();
				savepoints.remove(0);
				targetSavepointIndex--;
			}
		}

		currentSavepointIndex = targetSavepointIndex;
	}

	private void throwSavepointDeletedException() throws SavepointException {
		throw new SavepointException("Savepoint was deleted");
	}

	public IdentifierManager getIdentifierManager() {
		return session.getIdentifierManager();
	}

	public boolean hasCurrentSavepoint() {
		return currentSavepointIndex != -1;
	}

	public Iterable<SavepointImpl> getSavepoints() {
		return savepoints;
	}

	public void cancel(SavepointImpl targetSavepoint) throws SavepointException, Exception {
		// only the last savepoint in the list can be aborted
		int targetSavepointIndex = savepoints.indexOf(targetSavepoint);
		if (targetSavepointIndex < savepoints.size() - 1)
			throw new SavepointException("Only the last savepoint can be cancelled!");

		restore(targetSavepoint);	
		if (savepointBranch.size() > 0) {

			targetSavepoint._delete();
			currentSavepointIndex--;
			savepoints = savepoints.subList(0, targetSavepointIndex);
			savepoints.addAll(savepointBranch);
			savepointBranch.clear();			
		} else {
			targetSavepoint.clearChanges();			
		}		
	
	}
}
