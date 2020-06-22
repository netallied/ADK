/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import java.net.URL;
import org.automationml.URLDocumentLocation;
import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLForbiddenReferenceException;
import org.automationml.aml.AMLGroup;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLSession;
import org.automationml.aml.AMLSystemUnitClass;
import org.automationml.internal.aml.AMLElementType;

public class AMLChangeReferenceInstruction extends AMLInstruction {

	private AMLDeserializeReferenceIdentifier referencedIdentifier;
	private AMLElementType elementType;

	protected AMLChangeReferenceInstruction(AMLSession session, AMLDeserializeIdentifier selfIdentifier, AMLDeserializeReferenceIdentifier referencedIdentifier,
			AMLElementType elementType) {
		super(session, selfIdentifier);
		this.referencedIdentifier = referencedIdentifier;
		this.elementType = elementType;
	}

	@Override
	public void execute() throws Exception {

		switch (elementType) {

		case ELEMENT_INTERFACE_CLASS:
			AMLInterfaceClass interfaceClass = (AMLInterfaceClass) getDocumentElement();
			if (!(referencedIdentifier.getResolvedElement() instanceof AMLInterfaceClass)){
				String message = "Reference to AMLInterfaceClass expected but not found ";
				message += getLocationAsString(interfaceClass, referencedIdentifier);
				throw new AMLForbiddenReferenceException(interfaceClass, message);
			}
			AMLInterfaceClass baseInterfaceClass = (AMLInterfaceClass) referencedIdentifier.getResolvedElement();
			if (baseInterfaceClass == null) {
				String path = referencedIdentifier.getName();
				baseInterfaceClass = interfaceClass.getDocument().getInterfaceClassByPath(path);
			}
			interfaceClass.setBaseInterfaceClass(baseInterfaceClass);
			break;

		case ELEMENT_ROLE_CLASS:
			AMLRoleClass roleClass = (AMLRoleClass) getDocumentElement();
			if (!(referencedIdentifier.getResolvedElement() instanceof AMLRoleClass)) {
				String message = "Reference to AMLRoleClass expected but not found ";
				message += getLocationAsString(roleClass, referencedIdentifier);
				throw new AMLForbiddenReferenceException(roleClass, message);
			}
			AMLRoleClass baseRoleClass = (AMLRoleClass) referencedIdentifier.getResolvedElement();
			if (baseRoleClass == null) {
				String path = referencedIdentifier.getName();
				baseRoleClass = roleClass.getDocument().getRoleClassByPath(path);
			}
			roleClass.setBaseRoleClass(baseRoleClass);
			break;

		case ELEMENT_SYSTEM_UNIT_CLASS:
			AMLSystemUnitClass systemUnitClass = (AMLSystemUnitClass) getDocumentElement();
			if (!(referencedIdentifier.getResolvedElement() instanceof AMLSystemUnitClass)) {
				String message = "Reference to AMLSystemUnitClass expected but not found ";
				message += getLocationAsString(systemUnitClass, referencedIdentifier);
				throw new AMLForbiddenReferenceException(systemUnitClass, message);
			}
			AMLSystemUnitClass baseSystemUnitClass = (AMLSystemUnitClass) referencedIdentifier.getResolvedElement();
			if (baseSystemUnitClass == null) {
				String path = referencedIdentifier.getName();
				baseSystemUnitClass = systemUnitClass.getDocument().getSystemUnitClassByPath(path);
			}
			systemUnitClass.setBaseSystemUnitClass(baseSystemUnitClass);
			break;

		case ELEMENT_EXTERNAL_REFERENCE:
			AMLDocument document = (AMLDocument) getDocumentElement();
			if (!(referencedIdentifier.getResolvedElement() instanceof AMLDocument)) {
				String message = "Reference to AMLDocument expected but not found ";
				message += getLocationAsString(document, referencedIdentifier);
				throw new AMLForbiddenReferenceException(document, message);
			}
			AMLDocument referencedDocument = (AMLDocument) referencedIdentifier.getResolvedElement();
			if (referencedDocument == null) {
				URL url = new URL(referencedIdentifier.getName());
				referencedDocument = session.getAMLDocumentByDocumentLocation(new URLDocumentLocation(url));
			}
			document.addExplicitExternalReference(referencedDocument);
			break;

		case ELEMENT_INTERNAL_ELEMENT:
			AMLDocumentElement docEl = getDocumentElement();
			AMLInternalElement internalElement = (AMLInternalElement) getDocumentElement();
			if (!(referencedIdentifier.getResolvedElement() instanceof AMLSystemUnitClass)
					&& !(referencedIdentifier.getResolvedElement() instanceof AMLInternalElement)) {
				String message = "Reference to AMLSystemUnitClass expected but not found ";
				message += getLocationAsString(internalElement, referencedIdentifier);
				throw new AMLForbiddenReferenceException(internalElement, message);
			}
			if (referencedIdentifier.getResolvedElement() instanceof AMLSystemUnitClass) {
				AMLSystemUnitClass baseSystemUnitClass1 = (AMLSystemUnitClass) referencedIdentifier.getResolvedElement();
				if (baseSystemUnitClass1 == null) {
					String path = referencedIdentifier.getName();
					baseSystemUnitClass1 = internalElement.getDocument().getSystemUnitClassByPath(path);
				}
				internalElement.setBaseSystemUnitClass(baseSystemUnitClass1);
			} else if (referencedIdentifier.getResolvedElement() instanceof AMLGroup) {
				AMLGroup group = (AMLGroup) referencedIdentifier.getResolvedElement();
//
//				AMLInternalElementContainer internalElementContainer = (AMLInternalElementContainer) internalElement.getParent();
//				UUID id = internalElement.getId();
//				internalElement.delete();
//				AMLMirrorObject mirrorObject = group.createMirror(id, internalElement2);
//				getSelfIdentifier().setResolvedElement(mirrorObject);
			}

			break;

		default:
			break;
		}

	}

	@Override
	public boolean hasUnresolvedDependencies() {
		return (!referencedIdentifier.isResolved()
				|| !selfIdentifier.isResolved());
	}
}
