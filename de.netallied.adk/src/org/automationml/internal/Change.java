/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

public abstract class Change {

	public abstract void undo() throws Exception;

	public abstract void redo() throws Exception;

	public abstract void delete();

	/**
	 * @param change
	 * @return TODO
	 * @return true if this change was actually merged into the given change and hence can be deleted
	 */
	public abstract boolean mergeInto(Change change);

	protected abstract void notifyChangeListenersBeforeUndo();

	protected abstract void notifyChangeListenersAfterUndo();

	protected abstract void notifyChangeListenersBeforeRedo();

	protected abstract void notifyChangeListenersAfterRedo();
}
