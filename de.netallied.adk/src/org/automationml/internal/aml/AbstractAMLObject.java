/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Map;
import java.util.WeakHashMap;

import org.automationml.aml.AMLObject;

public abstract class AbstractAMLObject implements AMLObject {
	private Map<Object, Object> clientProperties;

	public void putClientProperty(Object key, Object value) {
		if (value == null && clientProperties == null) {
			return;
		}
		Map<Object, Object> clientProperties = getClientProperties();
		Object oldValue;
		oldValue = clientProperties.get(key);
		if (value != null) {
			clientProperties.put(key, value);
		} else if (oldValue != null) {
			clientProperties.remove(key);
		}
	}

	public final Object getClientProperty(Object key) {
		if (clientProperties == null)
			return null;
		return clientProperties.get(key);
	}

	public final void removeClientProperty(Object key) {
		if (clientProperties == null)
			return;
		clientProperties.remove(key);
	}

	public Iterable<Object> getClientPropertyKeys() {
		return getClientProperties().keySet();
	}

	private Map<Object, Object> getClientProperties() {
		if (clientProperties == null)
			clientProperties = new WeakHashMap<Object, Object>(1);
		return clientProperties;
	}

}
