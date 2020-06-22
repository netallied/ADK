/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

public interface AMLFrameAttribute extends AMLDocumentElement {

	void setX(double x); 
	
	void setY(double y);
	
	void setZ(double z);
	
	void setRX(double x); 
	
	void setRY(double y);
	
	void setRZ(double z);
	
	double getX();
	
	double getY();
	
	double getZ();
	
	double getRX();
	
	double getRY();
	
	double getRZ();
}
