/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.net.URI;
import java.util.UUID;

public interface AMLCOLLADAInterface extends AMLDocumentElement, AMLElement, AMLRenamable {
	enum RefType {
		EXPLICIT, IMPLICIT
	}

	void setRefURI(URI uri);

	URI getRefURI();

	void setRefType(RefType implicit);

	RefType getRefType();

	void setTarget(String target);

	String getTarget();

	UUID getId();

	AMLInterfaceClass getInterfaceClass()  throws AMLValidationException;

}
