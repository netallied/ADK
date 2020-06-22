/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import org.automationml.aml.AMLCOLLADAInterfaceContainer;

public interface AMLInternalCOLLADAInterfaceContainer extends AMLCOLLADAInterfaceContainer {

	void _setCOLLADAInterface(AMLCOLLADAInterfaceImpl colladaInterface);

}
