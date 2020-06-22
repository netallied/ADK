/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLSession;

public class AMLCreateRefSemanticInstruction extends AMLCreateInstruction {
	
	private AMLDeserializeIdentifier parentIdentifier;
	public String attributePath;
	
	protected AMLCreateRefSemanticInstruction(AMLSession session,
			AMLDeserializeIdentifier selfIdentifier, AMLDeserializeIdentifier parentIdentifier) {
		super(session, selfIdentifier);
		this.parentIdentifier = parentIdentifier;
	}

	@Override
	AMLDeserializeIdentifier getParentIdentifier() {
		return parentIdentifier;
	}
	
	public AMLDocumentElement getParent() {
		return parentIdentifier.getResolvedElement();
	}

	@Override
	public void execute() throws Exception {
		AMLAttribute attribute = (AMLAttribute) getParent();
		attribute.addRefSemantic(attributePath);
	}
	
	@Override
	public boolean hasUnresolvedDependencies() {
		return super.hasUnresolvedDependencies();
	}
}
