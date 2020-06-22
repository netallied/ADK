/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.automationml.SerializerException;
import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttribute.Constraint;
import org.automationml.aml.AMLAttribute.NominalScaledConstraint;
import org.automationml.aml.AMLAttribute.OrdinalScaledConstraint;
import org.automationml.aml.AMLAttribute.UnknownConstraint;
import org.automationml.aml.AMLCOLLADAInterface;
import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentURLResolver;
import org.automationml.aml.AMLElement;
import org.automationml.aml.AMLElement.Revision;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLFacet;
import org.automationml.aml.AMLFrameAttribute;
import org.automationml.aml.AMLGroup;
import org.automationml.aml.AMLInstanceHierarchy;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInterfaceClassLibrary;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalLink;
import org.automationml.aml.AMLMappingObject;
import org.automationml.aml.AMLMirrorObject;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLRoleClassLibrary;
import org.automationml.aml.AMLRoleRequirements;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLSystemUnitClass;
import org.automationml.aml.AMLSystemUnitClassLibrary;
import org.automationml.aml.AMLValidationException;
import org.automationml.internal.aml.AMLElementNames;

public class AMLSerializer {

	private static final String AUTOMATION_ML_FRAME_ATTRIBUTE = "Frame";
	private static final String AUTOMATION_ML_ROLE_FACET_PATH = "AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Facet";
	private static final String AUTOMATION_ML_ROLE_GROUP_PATH = "AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Group";

	private AMLDocument baseDocument;
	private AMLDocumentURLResolver urlResolver;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private int aliasCounter = 0;

	private AMLSerializer(AMLDocument baseDocument, AMLDocumentURLResolver urlResolver) {
		this.baseDocument = baseDocument;
		this.urlResolver = urlResolver;
	}

	public static void serialize(AMLDocument document, AMLDocumentURLResolver urlResolver, OutputStream outputStream) throws SerializerException {
		AMLSerializer amlSerializer = new AMLSerializer(document, urlResolver);
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		try {
			URL url = urlResolver.getUrl(document);

			XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(outputStream, "UTF-8");
			xmlWriter.writeStartDocument("UTF-8", "1.0");
			if (url != null) {
				String fileName;
				try {
					fileName = new File(url.toURI()).getName();
				} catch (Exception e) {
					fileName = url.toString();
				}
				amlSerializer.serializeDocument(xmlWriter, document, fileName);
			}
			xmlWriter.writeEndDocument();
			xmlWriter.close();
		} catch (Exception e) {
			throw new SerializerException(e);
		}
	}

	private void serializeDocument(XMLStreamWriter writer, AMLDocument document, String fileName) throws XMLStreamException, AMLValidationException,
			SerializerException {

		writer.writeStartElement(AMLElementNames.ELEMENT_CAEX_FILE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_FILE_NAME, fileName);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_SCHEMA_VERSION, AMLElementNames.ATTRIBUTE_VALUE_SCHEMA_VERSION);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_XSI_NO_NAMESPACE_SCHEMA_LOCATION, AMLElementNames.ATTRIBUTE_CAEX_CLASS_MODEL);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_XMLNS_XSI, AMLElementNames.ATTRIBUTE_VALUE_XML_SCHEMA_INSTANCE);

		serializeRevisions(writer, document);

		writer.writeStartElement(AMLElementNames.ELEMENT_ADDITIONAL_INFORMATION);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_AUTOMATION_ML_VERSION, AMLElementNames.ATTRIBUTE_VALUE_AUTOMATION_ML_VERSION);
		writer.writeEndElement();

		writer.writeStartElement(AMLElementNames.ELEMENT_ADDITIONAL_INFORMATION);
		writer.writeStartElement(AMLElementNames.ELEMENT_WRITER_HEADER);
		writer.writeStartElement(AMLElementNames.ELEMENT_WRITER_NAME);
		writer.writeCharacters(document.getWriterName());
		writer.writeEndElement();
		writer.writeStartElement(AMLElementNames.ELEMENT_WRITER_ID);
		writer.writeCharacters(document.getWriterID());
		writer.writeEndElement();
		writer.writeStartElement(AMLElementNames.ELEMENT_WRITER_VENDOR);
		writer.writeCharacters(document.getWriterVendor());
		writer.writeEndElement();
		writer.writeStartElement(AMLElementNames.ELEMENT_WRITER_VENDOR_URL);
		writer.writeCharacters(document.getWriterVendorURL());
		writer.writeEndElement();
		writer.writeStartElement(AMLElementNames.ELEMENT_WRITER_VERSION);
		writer.writeCharacters(document.getWriterVersion());
		writer.writeEndElement();
		writer.writeStartElement(AMLElementNames.ELEMENT_WRITER_RELEASE);
		writer.writeCharacters(document.getWriterRelease());
		writer.writeEndElement();
		writer.writeStartElement(AMLElementNames.ELEMENT_LAST_WRITING_DATE_TIME);
		Date currentDate = new Date();
		document.setLastWritingDate(currentDate);
		writer.writeCharacters(formatDate(currentDate));
		writer.writeEndElement();
		writer.writeStartElement(AMLElementNames.ELEMENT_WRITER_PROJECT_TITLE);
		writer.writeCharacters(document.getWriterProjectTitle());
		writer.writeEndElement();
		writer.writeStartElement(AMLElementNames.ELEMENT_WRITER_PROJECT_ID);
		writer.writeCharacters(document.getWriterProjectID());
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();

		for (AMLDocument referencedDocument : document.getExplicitlyReferencedDocuments()) {
			serializeExternalReference(writer, referencedDocument);
		}

		Iterator<AMLInstanceHierarchy> instanceHierarchyIterator = document.getInstanceHierarchies().iterator();
		while (instanceHierarchyIterator.hasNext()) {
			AMLInstanceHierarchy instanceHierarchy = instanceHierarchyIterator.next();
			serializeInstanceHierarchy(writer, instanceHierarchy);
		}

		Iterator<AMLInterfaceClassLibrary> interfaceClassLibraryIterator = document.getInterfaceClassLibraries().iterator();
		while (interfaceClassLibraryIterator.hasNext()) {
			AMLInterfaceClassLibrary interfaceClassLibrary = interfaceClassLibraryIterator.next();
			serializeInterfaceClassLibrary(writer, interfaceClassLibrary);
		}

		Iterator<AMLRoleClassLibrary> roleClassLibraryIterator = document.getRoleClassLibraries().iterator();
		while (roleClassLibraryIterator.hasNext()) {
			AMLRoleClassLibrary roleClassLibrary = roleClassLibraryIterator.next();
			serializeRoleClassLibrary(writer, roleClassLibrary);
		}

		Iterator<AMLSystemUnitClassLibrary> systemUnitClassLibraryIterator = document.getSystemUnitClassLibraries().iterator();
		while (systemUnitClassLibraryIterator.hasNext()) {
			AMLSystemUnitClassLibrary systemUnitClassLibrary = systemUnitClassLibraryIterator.next();
			serializeSystemUnitClassLibrary(writer, systemUnitClassLibrary);
		}

		writer.writeEndElement();
	}

	private static String formatDate(Date currentDate) {
		return dateFormat.format(currentDate);
	}
	
	private void serializeRevisions(XMLStreamWriter writer, AMLElement amlElement) throws XMLStreamException {
		for (Revision revision : amlElement.getRevisions()) {
			writer.writeStartElement(AMLElementNames.ELEMENT_REVISION);
			
			writer.writeStartElement(AMLElementNames.ELEMENT_REVISION_DATE);
			writer.writeCharacters(formatDate(revision.getRevisionDate()));
			writer.writeEndElement();

			writer.writeStartElement(AMLElementNames.ELEMENT_OLD_VERSION);
			writer.writeCharacters(revision.getOldVersion());
			writer.writeEndElement();

			writer.writeStartElement(AMLElementNames.ELEMENT_NEW_VERSION);
			writer.writeCharacters(revision.getNewVersion());
			writer.writeEndElement();

			writer.writeStartElement(AMLElementNames.ELEMENT_AUTHOR_NAME);
			writer.writeCharacters(revision.getAuthorName());
			writer.writeEndElement();

			writer.writeStartElement(AMLElementNames.ELEMENT_COMMENT);
			writer.writeCharacters(revision.getComment());
			writer.writeEndElement();

			writer.writeEndElement();
		}
	}

	private void serializeExternalReference(XMLStreamWriter writer, AMLDocument referencedDocument) throws XMLStreamException, SerializerException {
		writer.writeStartElement(AMLElementNames.ELEMENT_EXTERNAL_REFERENCE);

		URL baseUrl = urlResolver.getUrl(baseDocument);
		URL referencedUrl = urlResolver.getUrl(referencedDocument);

		String path = null;
		if (urlResolver.isRelative(baseDocument, referencedDocument)) {
			try {
				URI baseURI = new URI(baseUrl.toURI().toString() + "/..");
				//				URI relativeURI = baseUrl.toURI().relativize(referencedUrl.toURI());
				URI relativeURI = baseURI.relativize(referencedUrl.toURI());
				path = relativeURI.toString();
			} catch (URISyntaxException e) {
				throw new SerializerException(e);
			}
		} else {
			path = referencedUrl.toString();
		}

		writer.writeAttribute(AMLElementNames.ATTRIBUTE_PATH, path);

		String alias = "Other" + aliasCounter++;
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_ALIAS, alias);

		writer.writeEndElement();
	}

	private void serializeInstanceHierarchy(XMLStreamWriter writer, AMLInstanceHierarchy instanceHierarchy) throws XMLStreamException, AMLValidationException {
		writer.writeStartElement(AMLElementNames.ELEMENT_INSTANCE_HIERARCHY);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, instanceHierarchy.getName());
		serializeElement(writer, instanceHierarchy);

		Iterable<AMLInternalElement> internalElements = instanceHierarchy.getInternalElements();
		for (AMLInternalElement internalElement : internalElements) {
			serializeInternalElement(writer, internalElement);
		}

		Iterable<AMLGroup> groups = instanceHierarchy.getGroups();
		for (AMLGroup group : groups) {
			serializeGroup(writer, group);
		}

		writer.writeEndElement();

	}

	private void serializeGroup(XMLStreamWriter writer, AMLGroup group) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_INTERNAL_ELEMENT);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, group.getName());
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_ID, getUUIDAsAttributeValue(group.getId()));
		serializeElement(writer, group);

		Iterable<AMLAttribute> attributeIterator = group.getAttributes();
		for (AMLAttribute attribute : attributeIterator) {
			serializeAttribute(writer, attribute, "");
		}

		Iterable<AMLExternalInterface> externalInterfaces = group.getExternalInterfaces();
		for (AMLExternalInterface externalInterface : externalInterfaces) {
			serializeExternalInterface(writer, externalInterface);
		}

		Iterable<AMLGroup> groups = group.getGroups();
		for (AMLGroup nextGroup : groups) {
			serializeGroup(writer, nextGroup);
		}

		Iterable<AMLMirrorObject> mirrorObjects = group.getMirrorObjects();
		for (AMLMirrorObject mirrorObject : mirrorObjects) {
			serializeMirrorObject(writer, mirrorObject);
		}

		writer.writeStartElement(AMLElementNames.ELEMENT_ROLE_REQUIREMENTS);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_BASE_ROLE_CLASS_PATH, AUTOMATION_ML_ROLE_GROUP_PATH);
		writer.writeEndElement();

		writer.writeEndElement();

	}

	private void serializeMirrorObject(XMLStreamWriter writer, AMLMirrorObject mirrorObject) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_INTERNAL_ELEMENT);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_ID, getUUIDAsAttributeValue(mirrorObject.getId()));
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_BASE_SYSTEM_UNIT_PATH, getUUIDAsAttributeValue(mirrorObject.getInternalElement().getId()));
		writer.writeEndElement();
	}

	private String getUUIDAsAttributeValue(UUID id) {
		return "{" + id.toString() + "}";
	}

	private void serializeInternalElement(XMLStreamWriter writer, AMLInternalElement internalElement) throws XMLStreamException, AMLValidationException {
		writer.writeStartElement(AMLElementNames.ELEMENT_INTERNAL_ELEMENT);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_ID, getUUIDAsAttributeValue(internalElement.getId()));
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, internalElement.getName());
		if (internalElement.getBaseSystemUnitClass() != null)
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_BASE_SYSTEM_UNIT_PATH, internalElement.getBaseSystemUnitClass().getClassPath());
		serializeElement(writer, internalElement);

		if (internalElement.hasFrameAttribute()) {
			serializeFrameAttribute(writer, internalElement.getFrameAttribute());
		}

		Iterable<AMLAttribute> attributeIterator = internalElement.getAttributes();
		for (AMLAttribute attribute : attributeIterator) {
			serializeAttribute(writer, attribute, "");
		}

		if (internalElement.getCOLLADAInterface() != null) {
			serializeCOLLADAInterface(writer, internalElement.getCOLLADAInterface());
		}
		
		Iterable<AMLExternalInterface> externalInterfaces = internalElement.getExternalInterfaces();
		for (AMLExternalInterface externalInterface : externalInterfaces) {
			serializeExternalInterface(writer, externalInterface);
		}

		Iterable<AMLFacet> facets = internalElement.getFacets();
		for (AMLFacet facet : facets) {
			serializeFacet(writer, facet);
		}
		
		Iterable<AMLMirrorObject> mirrorObjects = internalElement.getMirrorObjects();
		for (AMLMirrorObject mirrorObject : mirrorObjects) {
			serializeMirrorObject(writer, mirrorObject);
		}

		Iterable<AMLInternalElement> internalElements = internalElement.getInternalElements();
		for (AMLInternalElement nextInternalElement : internalElements) {
			serializeInternalElement(writer, nextInternalElement);
		}

		Iterable<AMLSupportedRoleClass> supportedRoleClasses = internalElement.getSupportedRoleClasses();
		for (AMLSupportedRoleClass supportedRoleClass : supportedRoleClasses) {
			serializeSupportedRoleClass(writer, supportedRoleClass);
		}

		AMLRoleRequirements roleRequirements = internalElement.getRoleRequirements();
		if (roleRequirements != null) {
			serializeRoleRequirements(writer, roleRequirements);
		}

		Iterable<AMLInternalLink> internalLinks = internalElement.getInternalLinks();
		for (AMLInternalLink internalLink : internalLinks) {
			serializeInternalLink(writer, internalLink);
		}

		writer.writeEndElement();
	}

	private void serializeCOLLADAInterface(XMLStreamWriter writer,
			AMLCOLLADAInterface colladaInterface) throws XMLStreamException, AMLValidationException {
		writer.writeStartElement(AMLElementNames.ELEMENT_EXTERNAL_INTERFACE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, colladaInterface.getName());
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_ID, getUUIDAsAttributeValue(colladaInterface.getId()));
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_BASE_CLASS_PATH, colladaInterface.getInterfaceClass().getClassPath());
		serializeElement(writer, colladaInterface);

		if (colladaInterface.getRefURI() != null) {
			writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, "refURI");
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_ATTRIBUTE_DATA_TYPE, "xs:anyURI");
			writer.writeStartElement(AMLElementNames.ELEMENT_VALUE);
			writer.writeCharacters(colladaInterface.getRefURI().toString());
			writer.writeEndElement();
			writer.writeEndElement();
		}
		
		writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, "refType");
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_ATTRIBUTE_DATA_TYPE, "xs:string");
		writer.writeStartElement(AMLElementNames.ELEMENT_VALUE);
		writer.writeCharacters(colladaInterface.getRefType() == AMLCOLLADAInterface.RefType.EXPLICIT ? "explicit" : "implicit");
		writer.writeEndElement();
		writer.writeEndElement();
		
		if (colladaInterface.getTarget() != null) {
			writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, "target");	
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_ATTRIBUTE_DATA_TYPE, "xs:string");
			writer.writeStartElement(AMLElementNames.ELEMENT_VALUE);
			writer.writeCharacters(colladaInterface.getTarget());
			writer.writeEndElement();
			writer.writeEndElement();
		}

		writer.writeEndElement();
	}

	private void serializeFrameAttribute(XMLStreamWriter writer, AMLFrameAttribute frameAttribute) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, AUTOMATION_ML_FRAME_ATTRIBUTE);

		writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, "x");
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_UNIT, "m");
		writer.writeStartElement(AMLElementNames.ELEMENT_VALUE);
		writer.writeCharacters(Double.toString(frameAttribute.getX()));
		writer.writeEndElement();
		writer.writeEndElement();

		writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, "y");
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_UNIT, "m");
		writer.writeStartElement(AMLElementNames.ELEMENT_VALUE);
		writer.writeCharacters(Double.toString(frameAttribute.getY()));
		writer.writeEndElement();
		writer.writeEndElement();

		writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, "z");
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_UNIT, "m");
		writer.writeStartElement(AMLElementNames.ELEMENT_VALUE);
		writer.writeCharacters(Double.toString(frameAttribute.getZ()));
		writer.writeEndElement();
		writer.writeEndElement();

		writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, "rx");
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_UNIT, "°");
		writer.writeStartElement(AMLElementNames.ELEMENT_VALUE);
		writer.writeCharacters(Double.toString(frameAttribute.getRX()));
		writer.writeEndElement();
		writer.writeEndElement();

		writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, "ry");
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_UNIT, "°");
		writer.writeStartElement(AMLElementNames.ELEMENT_VALUE);
		writer.writeCharacters(Double.toString(frameAttribute.getRY()));
		writer.writeEndElement();
		writer.writeEndElement();

		writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, "rz");
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_UNIT, "°");
		writer.writeStartElement(AMLElementNames.ELEMENT_VALUE);
		writer.writeCharacters(Double.toString(frameAttribute.getRZ()));
		writer.writeEndElement();
		writer.writeEndElement();

		writer.writeEndElement();

	}

	private void serializeFacet(XMLStreamWriter writer, AMLFacet facet) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_INTERNAL_ELEMENT);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, facet.getName());
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_ID, getUUIDAsAttributeValue(facet.getId()));

		Iterable<AMLAttribute> attributes = facet.getAttributes();
		for (AMLAttribute attribute : attributes) {
			writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, attribute.getName());
			writer.writeEndElement();
		}

		Iterable<AMLExternalInterface> externalInterfaces = facet.getExternalInterfaces();
		for (AMLExternalInterface externalInterface : externalInterfaces) {
			writer.writeStartElement(AMLElementNames.ELEMENT_EXTERNAL_INTERFACE);
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, externalInterface.getName());
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_ID, getUUIDAsAttributeValue(externalInterface.getId()));
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_BASE_CLASS_PATH, externalInterface.getInterfaceClass().getClassPath());
			writer.writeEndElement();
		}

		writer.writeStartElement(AMLElementNames.ELEMENT_ROLE_REQUIREMENTS);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_BASE_ROLE_CLASS_PATH, AUTOMATION_ML_ROLE_FACET_PATH);
		writer.writeEndElement();

		writer.writeEndElement();
	}

	private void serializeInternalLink(XMLStreamWriter writer, AMLInternalLink internalLink) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_INTERNAL_LINK);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, internalLink.getName());
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_PARTNER_SIDE_A, internalLink.getRefPartnerSideA().getSymbolName());
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_PARTNER_SIDE_B, internalLink.getRefPartnerSideB().getSymbolName());

		serializeElement(writer, internalLink);
		writer.writeEndElement();
	}

	private void serializeRoleRequirements(XMLStreamWriter writer, AMLRoleRequirements roleRequirements) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_ROLE_REQUIREMENTS);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_BASE_ROLE_CLASS_PATH, roleRequirements.getRoleClass().getClassPath());

		serializeElement(writer, roleRequirements);

		Iterable<AMLAttribute> attributeIterator = roleRequirements.getAttributes();
		for (AMLAttribute attribute : attributeIterator) {
			AMLSupportedRoleClass supportedRoleClass = roleRequirements.getSupportedRoleClassOfAttribute(attribute);
			AMLRoleClass roleClass = supportedRoleClass.getRoleClass();
			serializeAttribute(writer, attribute, roleClass.getName() + ".");
		}

		writer.writeEndElement();
	}

	private void serializeSystemUnitClassLibrary(XMLStreamWriter writer, AMLSystemUnitClassLibrary systemUnitClassLibrary) throws XMLStreamException,
			AMLValidationException {

		writer.writeStartElement(AMLElementNames.ELEMENT_SYSTEM_UNIT_CLASS_LIB);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, systemUnitClassLibrary.getName());
		serializeElement(writer, systemUnitClassLibrary);

		Iterator<AMLSystemUnitClass> systemUnitClassIterator = systemUnitClassLibrary.getSystemUnitClasses().iterator();
		while (systemUnitClassIterator.hasNext()) {
			AMLSystemUnitClass systemUnitClass = systemUnitClassIterator.next();
			serializeSystemUnitClass(writer, systemUnitClass);
		}

		writer.writeEndElement();

	}

	private void serializeSystemUnitClass(XMLStreamWriter writer, AMLSystemUnitClass systemUnitClass) throws XMLStreamException, AMLValidationException {

		writer.writeStartElement(AMLElementNames.ELEMENT_SYSTEM_UNIT_CLASS);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, systemUnitClass.getName());
		if (systemUnitClass.getBaseSystemUnitClass() != null)
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_BASE_CLASS_PATH, systemUnitClass.getBaseSystemUnitClass().getClassPath());

		serializeElement(writer, systemUnitClass);

		if (systemUnitClass.hasFrameAttribute()) {
			serializeFrameAttribute(writer, systemUnitClass.getFrameAttribute());
		}

		Iterable<AMLAttribute> attributes = systemUnitClass.getAttributes();
		for (AMLAttribute attribute : attributes) {
			serializeAttribute(writer, attribute, "");
		}

		Iterable<AMLExternalInterface> externalInterfaces = systemUnitClass.getExternalInterfaces();
		for (AMLExternalInterface externalInterface : externalInterfaces) {
			serializeExternalInterface(writer, externalInterface);
		}

		Iterable<AMLInternalElement> internalElements = systemUnitClass.getInternalElements();
		for (AMLInternalElement internalElement : internalElements) {
			serializeInternalElement(writer, internalElement);
		}

		Iterable<AMLGroup> groups = systemUnitClass.getGroups();
		for (AMLGroup group : groups) {
			serializeGroup(writer, group);
		}

		Iterable<AMLSupportedRoleClass> supportedRoleClasses = systemUnitClass.getSupportedRoleClasses();
		for (AMLSupportedRoleClass supportedRoleClass : supportedRoleClasses) {
			serializeSupportedRoleClass(writer, supportedRoleClass);
		}

		Iterable<AMLSystemUnitClass> systemUnitClasses = systemUnitClass.getSystemUnitClasses();
		for (AMLSystemUnitClass nextSystemUnitClass : systemUnitClasses) {
			serializeSystemUnitClass(writer, nextSystemUnitClass);
		}
		
		Iterable<AMLInternalLink> internalLinks = systemUnitClass.getInternalLinks();
		for (AMLInternalLink internalLink : internalLinks) {
			serializeInternalLink(writer, internalLink);
		}

		writer.writeEndElement();

	}

	private void serializeExternalInterface(XMLStreamWriter writer, AMLExternalInterface externalInterface) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_EXTERNAL_INTERFACE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, externalInterface.getName());
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_ID, getUUIDAsAttributeValue(externalInterface.getId()));
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_BASE_CLASS_PATH, externalInterface.getInterfaceClass().getClassPath());
		serializeElement(writer, externalInterface);

		Iterable<AMLAttribute> attributeIterator = externalInterface.getAttributes();
		for (AMLAttribute attribute : attributeIterator) {
			serializeAttribute(writer, attribute, "");
		}

		writer.writeEndElement();
	}

	private void serializeSupportedRoleClass(XMLStreamWriter writer, AMLSupportedRoleClass supportedRoleClass) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_SUPPORTED_ROLE_CLASS);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_ROLE_CLASS_PATH, supportedRoleClass.getRoleClass().getClassPath());

		serializeElement(writer, supportedRoleClass);

		AMLMappingObject mappingObject = supportedRoleClass.getMappingObject();
		if (mappingObject.hasAttributeMappings()) {
			writer.writeStartElement(AMLElementNames.ELEMENT_MAPPING_OBJECT);
			Iterable<AMLAttribute> roleAttributes = mappingObject.getMappedRoleAttributes();
			for (AMLAttribute roleAttribute : roleAttributes) {
				AMLAttribute mappedAttribute = mappingObject.getMappedAttribute(roleAttribute);
				writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE_NAME_MAPPING);
				writer.writeAttribute(AMLElementNames.ATTRIBUTE_ROLE_ATTRIBUTE_NAME, roleAttribute.getName());
				writer.writeAttribute(AMLElementNames.ATTRIBUTE_SYSTEM_UNIT_ATTRIBUTE_NAME, mappedAttribute.getName());
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}

		writer.writeEndElement();
	}

	private void serializeAttribute(XMLStreamWriter writer, AMLAttribute attribute, String prefix) throws XMLStreamException {

		writer.writeStartElement(AMLElementNames.ELEMENT_ATTRIBUTE);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, prefix + attribute.getName());
		if (attribute.getUnit() != null)
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_UNIT, attribute.getUnit());
		if (attribute.getDataType() != null)
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_ATTRIBUTE_DATA_TYPE, attribute.getDataType());

		if (attribute.getDescription() != null) {
			writer.writeStartElement(AMLElementNames.ELEMENT_DESCRIPTION);
			writer.writeCharacters(attribute.getDescription());
			writer.writeEndElement();
		}

		if (attribute.getDefaultValue() != null) {
			writer.writeStartElement(AMLElementNames.ELEMENT_DEFAULT_VALUE);
			writer.writeCharacters(attribute.getDefaultValue());
			writer.writeEndElement();
		}

		if (attribute.getValue() != null) {
			writer.writeStartElement(AMLElementNames.ELEMENT_VALUE);
			writer.writeCharacters(attribute.getValue());
			writer.writeEndElement();
		}

		Iterator<AMLAttribute> attributeIterator = attribute.getAttributes().iterator();
		while (attributeIterator.hasNext()) {
			AMLAttribute nextAttribute = attributeIterator.next();
			serializeAttribute(writer, nextAttribute, "");
		}
		
		for (String refSemantic : attribute.getRefSemantics()) {
			serializeRefSemantic(writer, refSemantic);
		}
		
		for (Constraint constraint : attribute.getConstraints()) {
			serializeConstraint(writer, constraint);
		}

		writer.writeEndElement();

	}

	private void serializeConstraint(XMLStreamWriter writer,
			Constraint constraint) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_CONSTRAINT);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, constraint.getName());
		if (constraint instanceof OrdinalScaledConstraint) 
			serialiazeOrdinalScaledConstraint(writer, (OrdinalScaledConstraint)constraint);
		else if (constraint instanceof NominalScaledConstraint) 
			serialiazeNominalScaledConstraint(writer, (NominalScaledConstraint)constraint);
		else if (constraint instanceof UnknownConstraint)
			serialiazeUnknownConstraint(writer, (UnknownConstraint)constraint);
		writer.writeEndElement();		
	}

	private void serialiazeUnknownConstraint(XMLStreamWriter writer,
			UnknownConstraint constraint) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_UNKNOWN_TYPE);
		writer.writeStartElement(AMLElementNames.ELEMENT_REQUIREMENTS);
		writer.writeCharacters(constraint.getRequirement());
		writer.writeEndElement();	
		writer.writeEndElement();
	}

	private void serialiazeNominalScaledConstraint(XMLStreamWriter writer,
			NominalScaledConstraint constraint) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_NOMINAL_SCALED_TYPE);
		for (String requiredValue : constraint.getRequiredValues()) {
			writer.writeStartElement(AMLElementNames.ELEMENT_REQUIRED_VALUE);
			writer.writeCharacters(requiredValue);
			writer.writeEndElement();	
		}
		writer.writeEndElement();
	}

	private void serialiazeOrdinalScaledConstraint(XMLStreamWriter writer,
			OrdinalScaledConstraint constraint) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_ORDINAL_SCALED_TYPE);
		writer.writeStartElement(AMLElementNames.ELEMENT_REQUIRED_MAX_VALUE);
		writer.writeCharacters(constraint.getRequiredMaxValue());
		writer.writeEndElement();
		writer.writeStartElement(AMLElementNames.ELEMENT_REQUIRED_VALUE);
		writer.writeCharacters(constraint.getRequiredValue());
		writer.writeEndElement();		
		writer.writeStartElement(AMLElementNames.ELEMENT_REQUIRED_MIN_VALUE);
		writer.writeCharacters(constraint.getRequiredMinValue());
		writer.writeEndElement();		
		writer.writeEndElement();
	}

	private void serializeRefSemantic(XMLStreamWriter writer, String refSemantic) throws XMLStreamException {
		writer.writeStartElement(AMLElementNames.ELEMENT_REF_SEMANTIC);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_CORRESPONDING_ATTRIBUTE_PATH, refSemantic);
		writer.writeEndElement();		
	}

	private void serializeRoleClassLibrary(XMLStreamWriter writer, AMLRoleClassLibrary roleClassLibrary) throws XMLStreamException {

		writer.writeStartElement(AMLElementNames.ELEMENT_ROLE_CLASS_LIB);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, roleClassLibrary.getName());
		serializeElement(writer, roleClassLibrary);

		Iterator<AMLRoleClass> roleClassIterator = roleClassLibrary.getRoleClasses().iterator();
		while (roleClassIterator.hasNext()) {
			AMLRoleClass roleClass = roleClassIterator.next();
			serializeRoleClass(writer, roleClass);
		}

		writer.writeEndElement();

	}

	private void serializeRoleClass(XMLStreamWriter writer, AMLRoleClass roleClass) throws XMLStreamException {

		writer.writeStartElement(AMLElementNames.ELEMENT_ROLE_CLASS);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, roleClass.getName());
		if (roleClass.getBaseRoleClass() != null)
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_BASE_CLASS_PATH, roleClass.getBaseRoleClass().getClassPath());

		serializeElement(writer, roleClass);

		Iterator<AMLAttribute> attributeIterator = roleClass.getAttributes().iterator();
		while (attributeIterator.hasNext()) {
			AMLAttribute attribute = attributeIterator.next();
			serializeAttribute(writer, attribute, "");
		}

		Iterator<AMLRoleClass> roleClassIterator = roleClass.getRoleClasses().iterator();
		while (roleClassIterator.hasNext()) {
			AMLRoleClass nextRoleClass = roleClassIterator.next();
			serializeRoleClass(writer, nextRoleClass);
		}

		for (AMLExternalInterface externalInterface : roleClass.getExternalInterfaces()) {
			serializeExternalInterface(writer, externalInterface);
		}

		writer.writeEndElement();

	}

	private void serializeInterfaceClassLibrary(XMLStreamWriter writer, AMLInterfaceClassLibrary interfaceClassLibrary) throws XMLStreamException {

		writer.writeStartElement(AMLElementNames.ELEMENT_INTERFACE_CLASS_LIB);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, interfaceClassLibrary.getName());
		serializeElement(writer, interfaceClassLibrary);

		Iterator<AMLInterfaceClass> interfacelassIterator = interfaceClassLibrary.getInterfaceClasses().iterator();
		while (interfacelassIterator.hasNext()) {
			AMLInterfaceClass interfaceClass = interfacelassIterator.next();
			serializeInterfaceClass(writer, interfaceClass);
		}

		writer.writeEndElement();

	}

	private void serializeInterfaceClass(XMLStreamWriter writer, AMLInterfaceClass interfaceClass) throws XMLStreamException {

		writer.writeStartElement(AMLElementNames.ELEMENT_INTERFACE_CLASS);
		writer.writeAttribute(AMLElementNames.ATTRIBUTE_NAME, interfaceClass.getName());
		if (interfaceClass.getBaseInterfaceClass() != null)
			writer.writeAttribute(AMLElementNames.ATTRIBUTE_REF_BASE_CLASS_PATH, interfaceClass.getBaseInterfaceClass().getClassPath());

		serializeElement(writer, interfaceClass);

		Iterator<AMLAttribute> attributeIterator = interfaceClass.getAttributes().iterator();
		while (attributeIterator.hasNext()) {
			AMLAttribute attribute = attributeIterator.next();
			serializeAttribute(writer, attribute, "");
		}

		Iterator<AMLInterfaceClass> interfaceClassIterator = interfaceClass.getInterfaceClasses().iterator();
		while (interfaceClassIterator.hasNext()) {
			AMLInterfaceClass nextInterfaceClass = interfaceClassIterator.next();
			serializeInterfaceClass(writer, nextInterfaceClass);
		}

		writer.writeEndElement();
	}

	private void serializeElement(XMLStreamWriter writer, AMLElement element) throws XMLStreamException {
		serializeRevisions(writer, element);

		if (element.getDescription() != null) {
			writer.writeStartElement(AMLElementNames.ELEMENT_DESCRIPTION);
			writer.writeCharacters(element.getDescription());
			writer.writeEndElement();
		}
		if (element.getVersion() != null) {
			writer.writeStartElement(AMLElementNames.ELEMENT_VERSION);
			writer.writeCharacters(element.getVersion());
			writer.writeEndElement();
		}
		if (element.getCopyright() != null) {
			writer.writeStartElement(AMLElementNames.ELEMENT_COPYRIGHT);
			writer.writeCharacters(element.getCopyright());
			writer.writeEndElement();
		}
		if (element.getAdditionalInformation() != null) {
			writer.writeStartElement(AMLElementNames.ELEMENT_ADDITIONAL_INFORMATION);
			writer.writeCharacters(element.getAdditionalInformation());
			writer.writeEndElement();
		}
	}

}
