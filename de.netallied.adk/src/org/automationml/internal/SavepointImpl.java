/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.automationml.Savepoint;
import org.automationml.internal.aml.AMLDocumentImpl;

public class SavepointImpl implements Savepoint {

	Map<Change, Change> changes = new LinkedHashMap<Change, Change>();

	private SavepointManager savepointManager;
	boolean deleted;
	private boolean consolidated;
	boolean restored;

	SavepointImpl(SavepointManager savepointManager) {
		this.savepointManager = savepointManager;
	}

	@Override
	public void restore() throws Exception {
		savepointManager.restore(this);
	}

	@Override
	public void delete() {
		savepointManager.removeSavepoint(this);
	}

	public void _delete() {
		clearChanges();
		deleted = true;
	}

	Change addChange(Change change) {
		Change existingChange = changes.get(change);
		if (existingChange != null) {
			if (change.mergeInto(existingChange))
				change.delete();
			return existingChange;
		}
		changes.put(change, change);
		return change;
	}

	public boolean hasChanges() {
		return !changes.isEmpty();
	}

	void undo() throws Exception {
		restored = true;
		if (!consolidated)
			consolidateChanges();

		List<Change> changesList = new ArrayList<Change>(changes.values());

		for (ListIterator<Change> iterator = changesList.listIterator(changesList.size()); iterator.hasPrevious();) {
			Change change = iterator.previous();
			change.undo();
		}
	}

	void redo() throws Exception {
		restored = false;
		for (Change change : changes.values()) {
			change.redo();
		}
	}

	public boolean isDeleted() {
		return deleted;
	}

	private void consolidateChanges() {
		// TODO
		// Die Liste der Changes kann reduziert werden, wenn Paare von CreateChanges und DeleteChanges für das selbe Element vorkommen.
		// Dann lassen sich nämlich alle Changes entfernen, die das Objekt betreffen und die beiden "umklammernden" CreateChange und DeleteChange.
		consolidated = true;
	}

	public void mergeWith(SavepointImpl savepoint) {
		for (Change change : savepoint.changes.values()) {
			addChange(change);
		}
		savepoint.changes.clear();
	}

	public void notifyChangeListenersAfterUndo() {
		List<Change> changesList = new ArrayList<Change>(changes.values());
		for (ListIterator<Change> iterator = changesList.listIterator(changesList.size()); iterator.hasPrevious();) {
			Change change = iterator.previous();
			change.notifyChangeListenersAfterUndo();
			
			AMLDocumentImpl document = SavepointManager.getDocument(change);
			if( document == null)
				continue;
			document.decrementChangesCount();
		}
	}

	public void notifyChangeListenersAfterRedo() {
		for (Change change : changes.values()) {
			change.notifyChangeListenersAfterRedo();
			
			
		}
	}

	public void notifyChangeListenersAfterExecute() {
		notifyChangeListenersAfterRedo();
	}

	public void notifyChangeListenersBeforeUndo() {
		List<Change> changesList = new ArrayList<Change>(changes.values());
		for (ListIterator<Change> iterator = changesList.listIterator(changesList.size()); iterator.hasPrevious();) {
			Change change = iterator.previous();
			change.notifyChangeListenersBeforeUndo();
			
//			AMLDocumentImpl document = SavepointManager.getDocument(change);
//			if( document == null)
//				continue;
//			document.decrementChangesCount();
		}
	}

	public void notifyChangeListenersBeforeRedo() {
		for (Change change : changes.values()) {
			change.notifyChangeListenersBeforeRedo();
			
			AMLDocumentImpl document = SavepointManager.getDocument(change);
			if( document == null)
				continue;
			document.incrementChangesCount();
		}
	}

	@Override
	public void cancel() throws Exception {
		savepointManager.cancel(this);
	}

	public void clearChanges() {
		for (Change change : changes.values()) {
			change.delete();
		}
		changes.clear();
	}
}
