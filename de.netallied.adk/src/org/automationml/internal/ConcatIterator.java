/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import java.util.Iterator;

public class ConcatIterator<T> implements Iterator<T> {
	private final Iterator<T> is[];
	private int current;

	public ConcatIterator(Iterator<T>... iterators) {
		is = iterators;
		current = 0;
	}

	@Override
	public boolean hasNext() {
		while (current < is.length && !is[current].hasNext())
			current++;

		return current < is.length;
	}

	@Override
	public T next() {
		while (current < is.length && !is[current].hasNext())
			current++;

		return is[current].next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove operation is not supported");
	}
}