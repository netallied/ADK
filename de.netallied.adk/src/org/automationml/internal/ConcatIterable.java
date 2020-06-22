/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import java.util.Iterator;

public class ConcatIterable<T> implements Iterable<T> {
	private final Iterable<T> iterables[];

	public ConcatIterable(Iterable<T>... iterables) {
		this.iterables = iterables;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		Iterator<T>[] iterators = new Iterator[iterables.length];
		int i = 0;
		for (Iterable<T> iterable : iterables) {
			iterators[i] = iterable.iterator();
		}
		return new ConcatIterator<T>(iterators);
	}
}