/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import java.util.ArrayList;
import java.util.List;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttribute.Constraint;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLSession;
import org.automationml.aml.AMLAttribute.NominalScaledConstraint;
import org.automationml.aml.AMLAttribute.UnknownConstraint;
import org.automationml.aml.AMLAttribute.OrdinalScaledConstraint;

public class AMLCreateConstraintInstruction extends AMLCreateInstruction {

	private AMLDeserializeIdentifier parentIdentifier;
	public static final int TYPE_UNKNOWN_TYPE = 0;
	public static final int TYPE_NOMINAL_SCALED_TYPE = 1;
	public static final int TYPE_ORDINAL_SCALED_TYPE = 2;
	
	public int type;
	public String name;
	public String requirement;
	public List<String> requieredValues = new ArrayList<String>();
	public String maxValue;
	public String minValue;
	
	protected AMLCreateConstraintInstruction(AMLSession session,
			AMLDeserializeIdentifier selfIdentifier, AMLDeserializeIdentifier parentIdentifier, int type) {
		super(session, selfIdentifier);
		this.parentIdentifier = parentIdentifier;
		this.type = type;
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
		Constraint constraint = null;
		switch (type) {
		case TYPE_UNKNOWN_TYPE:	
			constraint = attribute.createUnknownConstraint(name);
			((UnknownConstraint)constraint).setRequirement(requirement);
			break;
		case TYPE_NOMINAL_SCALED_TYPE:	
			constraint = attribute.createNominalScaledConstraint(name);
			for (String value : requieredValues) {
				((NominalScaledConstraint)constraint).addRequiredValue(value);
			}
			break;
		case TYPE_ORDINAL_SCALED_TYPE:	
			constraint = attribute.createOrdinalScaledConstraint(name);
			((OrdinalScaledConstraint)constraint).setRequiredValue(requirement);
			((OrdinalScaledConstraint)constraint).setRequiredMinValue(minValue);
			((OrdinalScaledConstraint)constraint).setRequiredMaxValue(maxValue);
			break;

		default:
			break;
		}
	}
	
	@Override
	public boolean hasUnresolvedDependencies() {
		return super.hasUnresolvedDependencies();
	}
}
