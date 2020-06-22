/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttributeContainer;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLFacet;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLRoleRequirements;
import org.automationml.aml.AMLSession;
import org.automationml.aml.AMLSupportedRoleClass;

public class AMLCreateAttributeInstruction extends AMLCreateInstruction {

	private AMLDeserializeIdentifier parentIdentifier;
	public String name = "";
	public String dataType = "";
	public String unit = "";
	public String description = "";
	public String defaultValue = "";
	public String value = "";

	protected AMLCreateAttributeInstruction(AMLSession session, AMLDeserializeIdentifier selfIdentifier, AMLDeserializeIdentifier parentIdentifier) {
		super(session, selfIdentifier);
		this.parentIdentifier = parentIdentifier;
	}

	@Override
	public AMLDeserializeIdentifier getParentIdentifier() {
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
		if (parent instanceof AMLRoleRequirements) {
			AMLRoleRequirements roleRequirements = (AMLRoleRequirements)parent;
			AMLSupportedRoleClass supportedRoleClass = roleRequirements.getSupportedRoleClass();
			String unescapedName = name;
			if (name.contains(".")) {
				AMLInternalElement internalElement = (AMLInternalElement) roleRequirements.getParent();
				for (AMLSupportedRoleClass supportedRoleClass2 : internalElement.getSupportedRoleClasses()) {
					if (name.contains(supportedRoleClass2.getRoleClass().getName() + "."))  {
						supportedRoleClass = supportedRoleClass2;
						break;
					}
				}
				unescapedName = name.replace(supportedRoleClass.getRoleClass().getName() + ".", "");
			}	
			AMLAttribute attribute = roleRequirements.createAttribute(supportedRoleClass, unescapedName);
			attribute.setDataType(dataType);
			attribute.setDefaultValue(defaultValue);
			attribute.setDescription(description);
			attribute.setUnit(unit);
			attribute.setValue(value);

			if (selfIdentifier instanceof AMLDeserializeIdentifier) {
				selfIdentifier.setResolvedElement(attribute);
			}
			
		} else if (parent instanceof AMLFacet) {
			AMLFacet facet = (AMLFacet) parent;
			AMLInternalElement internalElement = (AMLInternalElement) parent.getParent();
			AMLAttribute attribute = internalElement.getAttribute(name);
			facet.addAttribute(attribute);
			if (selfIdentifier instanceof AMLDeserializeIdentifier) {
				selfIdentifier.setResolvedElement(attribute);
			}
		} else if (parent instanceof AMLAttributeContainer) {
			AMLAttributeContainer attributeContainer = (AMLAttributeContainer) parent;

			AMLAttribute attribute = attributeContainer.createAttribute(name);
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
}
