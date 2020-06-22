/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLDocumentElement extends AMLObject {
	AMLSession getSession();

	AMLDocument getDocument();

	AMLDocumentElement getParent();
	
	AMLValidationResultList validateUnlink();
	
	void unlink() throws AMLValidationException;
	
	AMLValidationResultList validateDeepDelete();
	
	AMLValidationResultList validateDelete();
	
	void deepDelete() throws AMLValidationException;
	
	void delete() throws AMLValidationException;

	boolean isDeleted();

	AMLValidationResultList validateReparent(AMLDocumentElement oldParentElement, AMLDocumentElement newParentElement, 
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement);

	void reparent(AMLDocumentElement oldParentElement, AMLDocumentElement newParentElement, 
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) throws AMLValidationException;
}
