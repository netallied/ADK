/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import java.util.Iterator;

public class ReadOnlyIterator<T> implements Iterator<T> {
	private Iterator<T> delegate;

	public ReadOnlyIterator(Iterator<T> delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public boolean hasNext() {
		if (delegate == null)
			return false;
		return delegate.hasNext();
	}

	@Override
	public T next() {
		if (delegate == null)
			return null;
		return delegate.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove operation is not supported");
	}

}