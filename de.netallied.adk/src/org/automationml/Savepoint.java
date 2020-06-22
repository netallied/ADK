/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml;

public interface Savepoint {

	void restore() throws Exception;

	void delete();

	boolean hasChanges();

	void cancel() throws Exception;
}
