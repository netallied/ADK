/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.UUID;

public interface AMLMirrorContainer extends AMLDocumentElement  {
	
	AMLMirrorObject createMirror(UUID id, AMLInternalElement internalElement) throws AMLValidationException;

	AMLMirrorObject createMirror(AMLInternalElement internalElement) throws AMLValidationException;

	Iterable<AMLMirrorObject> getMirrorObjects();

	AMLValidationResultList validateMirrorCreate(UUID id, AMLInternalElement internalElement) throws AMLValidationException;

	AMLValidationResultList validateMirrorCreate(AMLInternalElement internalElement);

	int getMirrorObjectsCount();
	
}
