/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Iterator;

import org.automationml.aml.AMLAttribute;
import org.automationml.internal.ReadOnlyIterator;

public class AMLAttributeIteratorImpl extends ReadOnlyIterator<AMLAttribute> implements AMLAttributeIterator {
	public AMLAttributeIteratorImpl(Iterator<AMLAttribute> delegate) {
		super(delegate);
	}
}
