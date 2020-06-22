/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import java.util.ArrayList;
import java.util.List;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttributeContainer;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLSession;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.aml.AMLValidationResultListImpl;

public class AMLCreateExternalInterfaceAttributeInstruction extends AMLCreateAttributeInstruction {

	AMLDeserializeIdentifier externalInterface;

	protected AMLCreateExternalInterfaceAttributeInstruction(AMLSession session, AMLDeserializeIdentifier selfIdentifier, AMLDeserializeIdentifier parentIdentifier) {
		super(session, selfIdentifier, parentIdentifier);
	}
	
	public void setExternalInterfaceIdentifier(AMLDeserializeIdentifier externalInterface) {
		this.externalInterface = externalInterface;
	}

	@Override
	public void execute() throws Exception {
		AMLDocumentElement parent = getParent();
		AMLAttributeContainer attributeContainer = (AMLAttributeContainer) parent;
		AMLAttributeContainer parentContainer = attributeContainer;
		AMLExternalInterface externalInterface = null;
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add(name);
		while (!(parentContainer instanceof AMLExternalInterface)) {
			if (parentContainer instanceof AMLAttribute)
				attributeNames.add(0, ((AMLAttribute)parentContainer).getName());
			parentContainer = (AMLAttributeContainer) parentContainer.getParent();			
		}
		externalInterface = (AMLExternalInterface) parentContainer;
		AMLInterfaceClass interfaceClass = externalInterface.getInterfaceClass();
		
		// check if attribute is already created
		AMLAttribute attribute = attributeContainer.getAttribute(name);
		if (attribute != null) {
			AMLAttributeContainer interfaceAttributes = interfaceClass;			
			for (String string : attributeNames) {
				AMLAttributeContainer lastInterfaceAttributes = interfaceAttributes;
				interfaceAttributes = interfaceAttributes.getAttribute(string);
				if (interfaceAttributes == null)
					throw new AMLValidationException(new AMLValidationResultListImpl(lastInterfaceAttributes, Severity.AML_ERROR, "Name already exists"));
			}			
		}
		if (attribute == null)
			attribute = attributeContainer.createAttribute(name);
		
		attribute.setDataType(dataType);
		attribute.setDefaultValue(defaultValue);
		attribute.setDescription(description);
		attribute.setUnit(unit);
		attribute.setValue(value);

		if (selfIdentifier instanceof AMLDeserializeIdentifier) {
			selfIdentifier.setResolvedElement(attribute);
		}
	}
}
