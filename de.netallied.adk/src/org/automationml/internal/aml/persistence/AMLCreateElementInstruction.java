/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLElement;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLExternalInterfaceContainer;
import org.automationml.aml.AMLFacet;
import org.automationml.aml.AMLGroup;
import org.automationml.aml.AMLGroupContainer;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInterfaceClassLibrary;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalElementContainer;
import org.automationml.aml.AMLInternalLink;
import org.automationml.aml.AMLInternalLinkContainer;
import org.automationml.aml.AMLMappingObject;
import org.automationml.aml.AMLMirrorContainer;
import org.automationml.aml.AMLMirrorObject;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLRoleClassLibrary;
import org.automationml.aml.AMLRoleRequirements;
import org.automationml.aml.AMLSession;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLSupportedRoleClassContainer;
import org.automationml.aml.AMLSystemUnitClass;
import org.automationml.aml.AMLSystemUnitClassLibrary;
import org.automationml.internal.aml.AMLElementType;

public class AMLCreateElementInstruction extends AMLCreateInstruction {

	protected class Revision {
		public Date date;
		public String oldVersion;
		public String newVersion;
		public String authorName;
		public String comment;

		protected Revision() {
		}
	}

	private AMLDeserializeIdentifier parentIdentifier;
	public String name;
	public String description;
	public String version;
	public String copyright;
	public String additionalInformation;
	public List<Revision> revisions = new ArrayList<Revision>();
	private List<AMLDeserializeReferenceIdentifier> references = new ArrayList<AMLDeserializeReferenceIdentifier>();
	public AMLElementType elementType;
	public UUID id;

	public AMLCreateElementInstruction(AMLSession session, AMLDeserializeIdentifier selfIdentifier,
			AMLDeserializeIdentifier parentIdentifier, AMLElementType elementType) {
		super(session, selfIdentifier);
		this.parentIdentifier = parentIdentifier;
		this.elementType = elementType;
	}

	public void addReferencedIdentifier(AMLDeserializeReferenceIdentifier identifier) {
		references.add(identifier);
	}

	public AMLDocumentElement getParent() {
		if (parentIdentifier instanceof AMLDeserializeIdentifier)
			return parentIdentifier.getResolvedElement();
		return null;
	}

	public void addRevision(Date date, String oldVersion, String newVersion, String authorName, String comment) {
		Revision revision = new Revision();
		revision.date = date;
		revision.oldVersion = oldVersion;
		revision.newVersion = newVersion;
		revision.authorName = authorName;
		revision.comment = comment;

		revisions.add(revision);
	}

	@Override
	public AMLDeserializeIdentifier getParentIdentifier() {
		return parentIdentifier;
	}

	@Override
	public void execute() throws Exception {
		AMLDocument document = null;
		AMLDocumentElement documentItem = null;

		AMLDocumentElement parent = getParent();

		switch (elementType) {
		case ELEMENT_INSTANCE_HIERARCHY:
			document = (AMLDocument) parent;
			documentItem = document.createInstanceHierarchy(name);
			break;

		case ELEMENT_INTERNAL_ELEMENT:
			AMLInternalElementContainer internalElementContainer = (AMLInternalElementContainer) parent;
			AMLInternalElement internalElement = internalElementContainer.createInternalElement(id);
			internalElement.setName(name);
			documentItem = internalElement;
			break;

		case ELEMENT_INTERFACE_CLASS_LIB:
			document = (AMLDocument) parent;
			documentItem = document.createInterfaceClassLibrary(name);
			break;

		case ELEMENT_INTERFACE_CLASS:
			if (parent instanceof AMLInterfaceClassLibrary) {
				documentItem = ((AMLInterfaceClassLibrary) parent).createInterfaceClass(name);
			} else if (parent instanceof AMLInterfaceClass) {
				documentItem = ((AMLInterfaceClass) parent).createInterfaceClass(name);
			}
			if (references.size() > 0) {
				AMLDeserializeReferenceIdentifier referencedIdentifier = references.get(0);
				AMLInterfaceClass baseInterfaceClass = (AMLInterfaceClass) referencedIdentifier.getResolvedElement();
				((AMLInterfaceClass) documentItem).setBaseInterfaceClass(baseInterfaceClass);
			}
			break;

		case ELEMENT_ROLE_CLASS:

			if (parent instanceof AMLRoleClassLibrary) {
				documentItem = ((AMLRoleClassLibrary) parent).createRoleClass(name);
			} else if (parent instanceof AMLRoleClass) {
				documentItem = ((AMLRoleClass) parent).createRoleClass(name);
			}

			if (references.size() > 0) {
				AMLDeserializeReferenceIdentifier referencedIdentifier = references.get(0);
				AMLRoleClass baseRoleClass = (AMLRoleClass) referencedIdentifier.getResolvedElement();
				((AMLRoleClass) documentItem).setBaseRoleClass(baseRoleClass);
			}
			break;

		case ELEMENT_SYSTEM_UNIT_CLASS:

			if (parent instanceof AMLSystemUnitClassLibrary) {
				documentItem = ((AMLSystemUnitClassLibrary) parent).createSystemUnitClass(name);
			} else if (parent instanceof AMLSystemUnitClass) {
				documentItem = ((AMLSystemUnitClass) parent).createSystemUnitClass(name);
			}
			if (references.size() > 0) {
				AMLDeserializeReferenceIdentifier referencedIdentifier = references.get(0);
				AMLSystemUnitClass baseSystemUnitClass = (AMLSystemUnitClass) referencedIdentifier.getResolvedElement();
				((AMLSystemUnitClass) documentItem).setBaseSystemUnitClass(baseSystemUnitClass);
			}
			break;

		case ELEMENT_SYSTEM_UNIT_CLASS_LIB:

			document = (AMLDocument) parent;
			documentItem = document.createSystemUnitClassLibrary(name);
			break;

		case ELEMENT_ROLE_CLASS_LIB:

			document = (AMLDocument) parent;
			documentItem = document.createRoleClassLibrary(name);
			break;

		case ELEMENT_FACET:
			AMLInternalElement internalElement1 = (AMLInternalElement) parent;
			AMLFacet facet = internalElement1.createFacet(name, id);
			documentItem = facet;
			break;

		case ELEMENT_GROUP:
			AMLGroupContainer groupContainer = (AMLGroupContainer) parent;
			AMLGroup group = groupContainer.createGroup(id);
			group.setName(name);
			documentItem = group;
			break;

		case ELEMENT_MIRROR_OBJECT:
			AMLMirrorContainer container = (AMLMirrorContainer) parent;
			if (references.size() == 1) {
				AMLDeserializeReferenceIdentifier referencedIdentifier = references.get(0);
				internalElement1 = (AMLInternalElement) referencedIdentifier.getResolvedElement();
				AMLMirrorObject mirrorObject = container.createMirror(id, internalElement1);
				documentItem = mirrorObject;
			}
			break;

		case ELEMENT_EXTERNAL_INTERFACE:
			if (parent instanceof AMLExternalInterfaceContainer) {
				AMLExternalInterfaceContainer externalInterfaceContainer = (AMLExternalInterfaceContainer) parent;

				AMLInterfaceClass interfaceClass = null;
				if (references.size() == 1) {
					AMLDeserializeReferenceIdentifier referencedIdentifier = references.get(0);
					interfaceClass = (AMLInterfaceClass) referencedIdentifier.getResolvedElement();
				}

				AMLExternalInterface externalInterface = externalInterfaceContainer.createExternalInterface(id,
						interfaceClass);
				externalInterface.setName(name);
				documentItem = externalInterface;
			} else if (parent instanceof AMLFacet) {
				facet = (AMLFacet) parent;
				internalElement1 = (AMLInternalElement) parent.getParent();
				AMLExternalInterface externalInterface = internalElement1.getExternalInterface(id);
				facet.addExternalInterface(externalInterface);
				documentItem = externalInterface;
			}

			break;

		case ELEMENT_SUPPORTED_ROLE_CLASS:
			AMLSupportedRoleClassContainer supportedRoleClassContainer = (AMLSupportedRoleClassContainer) parent;

			AMLRoleClass roleClass = null;
			AMLMappingObject mappingObject = null;
			if (references.size() > 0) {
				AMLDeserializeReferenceIdentifier referencedIdentifier = references.get(0);
				roleClass = (AMLRoleClass) referencedIdentifier.getResolvedElement();
			}
			AMLSupportedRoleClass supportedRoleClass = null;
			supportedRoleClass = supportedRoleClassContainer.createSupportedRoleClass(roleClass);
			if (references.size() > 1 && references.size() % 2 == 1) {
				mappingObject = supportedRoleClass.getMappingObject();
				for (int i = 1; i < references.size(); i = i + 2) {
					AMLDeserializeReferenceIdentifier systemUnitAttributeIdentifier = references.get(i);
					AMLDeserializeReferenceIdentifier roleAttributeIdentifier = references.get(i + 1);
					AMLAttribute systemUnitAttribute = (AMLAttribute) systemUnitAttributeIdentifier
							.getResolvedElement();
					AMLAttribute roleUnitAttribute = (AMLAttribute) roleAttributeIdentifier.getResolvedElement();
					mappingObject.mapAttribute(roleUnitAttribute, systemUnitAttribute);
				}
			}

			documentItem = supportedRoleClass;
			break;

		case ELEMENT_ROLE_REQUIREMENTS:
			AMLInternalElement internalElement2 = (AMLInternalElement) parent;
			AMLSupportedRoleClass supportedRoleClass1 = null;
			if (references.size() > 0) {
				AMLDeserializeReferenceIdentifier referencedIdentifier = references.get(0);
				supportedRoleClass1 = (AMLSupportedRoleClass) referencedIdentifier.getResolvedElement();
			}

			AMLRoleRequirements roleRequirements = internalElement2.createRoleRequirements(supportedRoleClass1);
			documentItem = roleRequirements;
			break;

		case ELEMENT_INTERNAL_LINK:
			AMLInternalLinkContainer internalLinkContainer = (AMLInternalLinkContainer) parent;
			AMLExternalInterface refPartnerSideA = null;
			AMLExternalInterface refPartnerSideB = null;
			if (references.size() == 2) {
				AMLDeserializeReferenceIdentifier refPartnerSide = references.get(0);
				refPartnerSideA = (AMLExternalInterface) refPartnerSide.getResolvedElement();
				refPartnerSide = references.get(1);
				refPartnerSideB = (AMLExternalInterface) refPartnerSide.getResolvedElement();
			}

			AMLInternalLink internalLink = internalLinkContainer.createInternalLink(name, refPartnerSideA, refPartnerSideB);
			documentItem = internalLink;
			break;

		default:
			break;
		}

		setElementProperties(documentItem);

		if (selfIdentifier instanceof AMLDeserializeIdentifier) {
			selfIdentifier.setResolvedElement(documentItem);
		}

	}

	protected void setElementProperties(AMLDocumentElement documentItem) {
		AMLElement element = (AMLElement) documentItem;
		element.setAdditionalInformation(additionalInformation);
		element.setCopyright(copyright);
		element.setDescription(description);
		element.setVersion(version);
		for (Revision revision : revisions) {
			element.addRevision(revision.date, revision.oldVersion, revision.newVersion, revision.authorName,
					revision.comment);
		}
	}

	public AMLElementType getElementType() {
		return elementType;
	}

	public void setElementType(AMLElementType elementType) {
		this.elementType = elementType;
	}

	@Override
	public boolean hasUnresolvedDependencies() {
		if (super.hasUnresolvedDependencies())
			return true;
		for (AMLDeserializeReferenceIdentifier identifier : references) {
			if (!identifier.isResolved())
				return true;
		}
		return false;
	}

}
