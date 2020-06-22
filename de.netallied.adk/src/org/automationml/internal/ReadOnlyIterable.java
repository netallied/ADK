/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import java.util.Collections;
import java.util.Iterator;

public class ReadOnlyIterable<T> implements Iterable<T> {
	private Iterable<T> delegate;

	public ReadOnlyIterable(Iterable<T> delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public Iterator<T> iterator() {
		if (delegate == null)
			return Collections.emptyIterator();
		return new ReadOnlyIterator<T>(delegate.iterator());
	}

}