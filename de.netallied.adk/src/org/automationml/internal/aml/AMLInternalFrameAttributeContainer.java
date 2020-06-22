/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import org.automationml.aml.AMLFrameAttributeContainer;

public interface AMLInternalFrameAttributeContainer extends AMLFrameAttributeContainer {

	void _setFrameAttribute(AMLFrameAttributeImpl frameAttribute);

	void _removeFrameAttribute();

	void _addFrameAttribute(AMLFrameAttributeImpl amlFrameAttributeImpl);

}
