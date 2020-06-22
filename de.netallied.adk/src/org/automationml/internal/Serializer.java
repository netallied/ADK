/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import org.automationml.SerializerException;
import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentURLResolver;


public interface Serializer {

	void serialize(AMLDocument document, AMLDocumentURLResolver urlResolver) throws SerializerException;

}
