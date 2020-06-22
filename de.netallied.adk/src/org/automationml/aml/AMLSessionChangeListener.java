/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLSessionChangeListener {
	void sessionChangeTransactionBegin();

	void sessionChangeTransactionEnd();

	void sessionChangeDocumentAdded(AMLDocument document);

	void sessionChangeDocumentRemoving(AMLDocument document);

	// TODO
	//	void deleteSavepoint(Savepoint savepoint);
}
