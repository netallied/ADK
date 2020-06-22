/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import java.net.URI;

import org.automationml.aml.AMLCOLLADAInterface;
import org.automationml.aml.AMLCOLLADAInterfaceContainer;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLSession;
import org.automationml.internal.aml.AMLElementType;

public class AMLCreateCOLLADAInterfaceInstruction extends AMLCreateElementInstruction {

	private AMLDeserializeIdentifier parentIdentifier;
	public AMLCOLLADAInterface.RefType refType;
	public String name;
	public String target;
	public URI uri;

	public AMLCreateCOLLADAInterfaceInstruction(AMLSession session,
			AMLDeserializeIdentifier identifier,
			AMLDeserializeIdentifier parentIdentifier) {
		super(session, identifier, parentIdentifier, AMLElementType.ELMENET_UNKNOWN);
		this.parentIdentifier = parentIdentifier;
	}

	
	public AMLDocumentElement getParent() {
		if (parentIdentifier instanceof AMLDeserializeIdentifier)
			return parentIdentifier.getResolvedElement();
		return null;
	}

	@Override
	public void execute() throws Exception {
		AMLDocumentElement parent = getParent();
		AMLCOLLADAInterfaceContainer container = (AMLCOLLADAInterfaceContainer) parent;

		AMLCOLLADAInterface colladaInterface = container.createCOLLADAInterface(refType);
		colladaInterface.setName(name);
		colladaInterface.setTarget(target);
		colladaInterface.setRefURI(uri);

		setElementProperties(colladaInterface);

		if (selfIdentifier instanceof AMLDeserializeIdentifier) {
			selfIdentifier.setResolvedElement(colladaInterface);
		}
	}



}
