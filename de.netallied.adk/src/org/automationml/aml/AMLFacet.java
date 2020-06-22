/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.UUID;

public interface AMLFacet extends AMLDocumentElement, AMLElement, AMLRenamable {

	public static final String AUTOMATION_ML_ROLE_FACET_PATH = "AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Facet";

	UUID getId();

	String getName();

	void setName(String string) throws AMLValidationException;

	void addAttribute(AMLAttribute attribute) throws AMLValidationException;

	Iterable<AMLAttribute> getAttributes();

	AMLAttribute getAttribute(String name);

	AMLValidationResultList validateFacetAddAttribute(AMLAttribute attribute);
	
	AMLValidationResultList validateFacetAddExternalInterface(AMLExternalInterface externalInterface);

	void addExternalInterface(AMLExternalInterface externalInterface) throws AMLValidationException;

	Iterable<AMLExternalInterface> getExternalInterfaces();

	AMLExternalInterface getExternalInterface(UUID id);

	void removeAttribute(AMLAttribute attribute) throws AMLValidationException;

	void removeExternalInterface(AMLExternalInterface externalInterface) throws AMLValidationException;

	AMLValidationResultList validateFacetRemoveAttribute(AMLAttribute attribute);

	AMLValidationResultList validateFacetRemoveExternalInterface(AMLExternalInterface externalInterface);

	int getAttributesCount();

}
