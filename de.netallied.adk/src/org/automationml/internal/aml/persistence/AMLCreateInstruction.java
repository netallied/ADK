/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import org.automationml.aml.AMLSession;


public abstract class AMLCreateInstruction extends AMLInstruction {

	protected AMLCreateInstruction(AMLSession session, AMLDeserializeIdentifier selfIdentifier) {
		super(session, selfIdentifier);
	}

	abstract AMLDeserializeIdentifier getParentIdentifier();

	@Override
	public boolean hasUnresolvedDependencies() {
		return !getParentIdentifier().isResolved();
	}

}
