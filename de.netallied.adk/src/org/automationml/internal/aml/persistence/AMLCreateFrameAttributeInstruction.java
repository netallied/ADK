/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLFrameAttribute;
import org.automationml.aml.AMLFrameAttributeContainer;
import org.automationml.aml.AMLSession;

public class AMLCreateFrameAttributeInstruction extends AMLCreateInstruction {

	private AMLDeserializeIdentifier parentIdentifier;
	public double x;
	public double y;
	public double z;
	public double rx;
	public double ry;
	public double rz;

	protected AMLCreateFrameAttributeInstruction(AMLSession session, AMLDeserializeIdentifier selfIdentifier, AMLDeserializeIdentifier parentIdentifier) {
		super(session, selfIdentifier);
		this.parentIdentifier = parentIdentifier;
	}

	@Override
	AMLDeserializeIdentifier getParentIdentifier() {
		return parentIdentifier;
	}

	public AMLDocumentElement getParent() {
		if (parentIdentifier instanceof AMLDeserializeIdentifier)
			return parentIdentifier.getResolvedElement();
		return null;
	}

	@Override
	public void execute() throws Exception {
		AMLDocumentElement parent = getParent();
		AMLFrameAttributeContainer container = (AMLFrameAttributeContainer) parent;

		AMLFrameAttribute attribute = container.getFrameAttribute();
		attribute.setX(x);
		attribute.setY(y);
		attribute.setZ(z);
		attribute.setRX(rx);
		attribute.setRY(ry);
		attribute.setRZ(rz);

		if (selfIdentifier instanceof AMLDeserializeIdentifier) {
			selfIdentifier.setResolvedElement(attribute);
		}

	}

}
