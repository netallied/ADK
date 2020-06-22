/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;



public interface AMLDocumentChangeListener {
	void documentChangeTransactionBegin();
	void documentChangeTransactionEnd();
	
	void documentChangeElementDeleting(AMLDocumentElement amlDocumentElement, AMLDocumentElement amlParent);
	void documentChangeElementModified(AMLDocumentElement amlDocumentElement);
	void documentChangeElementCreated(AMLDocumentElement amlDocumentElement, AMLDocumentElement amlParent);
	void documentChangeElementReparented(AMLDocumentElement amlDocumentElement, AMLDocumentElement oldParent, AMLDocumentElement newParent);

	void documentChangeElementValidated(AMLValidationResult validationResult);
	
	void documentChangeDirtyStateChanged(AMLDocument document);
}
