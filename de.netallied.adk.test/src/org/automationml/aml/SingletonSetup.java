/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SingletonSetup {

	public static void initializeSingleton(Class<?> singletonClass, String instanceFieldName) throws Exception {
		Field documentManagerField = singletonClass.getDeclaredField(instanceFieldName);
		documentManagerField.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(documentManagerField, documentManagerField.getModifiers() & ~Modifier.FINAL);

		Constructor<?> ctor = singletonClass.getDeclaredConstructor();
		ctor.setAccessible(true);
		Object documentManager = ctor.newInstance();
		documentManagerField.set(null, documentManager);
	}

}
