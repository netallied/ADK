/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.automationml.Deserializer;
import org.automationml.DeserializerException;
import org.automationml.DocumentLocation;
import org.automationml.DocumentURLResolver;
import org.automationml.URLDocumentLocation;
import org.automationml.aml.AMLCOLLADAInterface;
import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLNameAlreadyInUseException;
import org.automationml.aml.AMLSession;
import org.automationml.aml.AMLValidationException;
import org.automationml.internal.ParserException;
import org.automationml.internal.ProgressMonitor;
import org.automationml.internal.aml.AMLDocumentManager;
import org.automationml.internal.aml.AMLElementNames;
import org.automationml.internal.aml.AMLElementType;
import org.automationml.internal.aml.AMLSessionImpl;

// Performance comparisons:
//http://piccolo.sourceforge.net/bench.html
//http://kxml.sourceforge.net/kxml2/
//http://www.jclark.com/xml/xp/
public class AMLDeserializer implements Deserializer {

	private static final String PATH_SEPARATOR = "/";
	private static final String AUTOMATIONML_FRAME_ATTRIBUTE = "Frame";
	private static final String AUTOMATION_ML_ROLE_FACET_PATH = "AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Facet";
	private static final String AUTOMATION_ML_ROLE_GROUP_PATH = "AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Group";

	private Map<String, AMLDeserializeReferenceIdentifier> unresolvedIdentifiers = new LinkedHashMap<String, AMLDeserializeReferenceIdentifier>();

	// identifiers that have an instruction that will create an aml document item
	private Map<AMLDeserializeIdentifier, AMLCreateInstruction> identifierToCreateLibraryInstruction = new LinkedHashMap<AMLDeserializeIdentifier, AMLCreateInstruction>();
	private Map<AMLDeserializeIdentifier, AMLCreateInstruction> identifierToCreateInstruction = new LinkedHashMap<AMLDeserializeIdentifier, AMLCreateInstruction>();
	private List<AMLInstruction> internalLinkRelatedInstructions = new ArrayList<AMLInstruction>();
	private List<AMLInstruction> changeInstructions = new ArrayList<AMLInstruction>();

	private Set<URL> missingFiles = new HashSet<URL>();
	private Set<URL> unresolvedFiles = new HashSet<URL>();
	private Set<URL> resolvedFiles = new HashSet<URL>();

	private Map<String, AMLDeserializeIdentifier> aMLDeserializeIdentifiers = new LinkedHashMap<String, AMLDeserializeIdentifier>();

	private AMLSession session;

	@Override
	public void deserialize(URL url, AMLSession session) throws Exception {

		this.session = session;
		ProgressMonitor progressMonitor = session.getProgressMonitor();

		try {
			createInstructions(url);

			if (progressMonitor != null)
				progressMonitor.worked(1);

			if (!missingFiles.isEmpty())
				throw new DeserializerException("The following referenced Files could not be found: " + missingFiles);

			// if (!unresolvedIdentifiers.isEmpty())
			// throw new DeserializerException("Not all references could be resolved");

			// leave Savepoint management in the hands of application!!!
			//Savepoint savepoint = session.createSavepoint();
			try {
				executeInstructions();
			} catch (Exception e) {
				//savepoint.restore();
				throw e;
			} finally {
				//savepoint.delete();
			}

			if (progressMonitor != null)
				progressMonitor.worked(1);

		} finally {
			unresolvedFiles.clear();
			resolvedFiles.clear();
			missingFiles.clear();
			aMLDeserializeIdentifiers.clear();
			unresolvedIdentifiers.clear();
			changeInstructions.clear();
			identifierToCreateInstruction.clear();
		}
	}

	private void executeInstructions() throws Exception {
		Collection<AMLInstruction> aMLInstructions = new LinkedHashSet<AMLInstruction>();
		aMLInstructions.addAll(identifierToCreateLibraryInstruction.values());
		aMLInstructions.addAll(identifierToCreateInstruction.values());
		aMLInstructions.addAll(changeInstructions);

		executeInstructions(aMLInstructions);
		aMLInstructions.clear();
		aMLInstructions.addAll(internalLinkRelatedInstructions);
		executeInstructions(aMLInstructions);
	}

	private void executeInstructions(Collection<AMLInstruction> amlInstructions) throws Exception, AMLUnresolvedDependenciesException {
		while (!amlInstructions.isEmpty()) {

			boolean anyInstructionExecuted = false;
			for (Iterator<AMLInstruction> iterator = amlInstructions.iterator(); iterator.hasNext();) {
				AMLInstruction aMLInstruction = iterator.next();
				if (aMLInstruction.hasUnresolvedDependencies())
					continue;
				aMLInstruction.execute();
				iterator.remove();
				anyInstructionExecuted = true;
			}

			if (!anyInstructionExecuted)
				throw new AMLUnresolvedDependenciesException(amlInstructions);
		}
	}

	private void createInstructions(URL url) throws ParserException, MalformedURLException, AMLNameAlreadyInUseException {

		createInstructionsFromFile(url);

		while (true) {

			int resolvedFilesCount = resolvedFiles.size();

			Iterator<URL> unresolvedFilesIterator = unresolvedFiles.iterator();
			if (unresolvedFilesIterator.hasNext()) {
				URL unresolvedFile = unresolvedFilesIterator.next();
				createInstructionsFromFile(unresolvedFile);
				resolvedFiles.add(unresolvedFile);
				unresolvedFiles.remove(unresolvedFile);
			}

			if (unresolvedIdentifiers.isEmpty() && unresolvedFiles.isEmpty())
				break;

			if (resolvedFilesCount == resolvedFiles.size())
				break;
		}
	}

	private void createInstructionsFromFile(URL url) throws ParserException, MalformedURLException, AMLNameAlreadyInUseException {
		XMLStreamReader reader = null;
		InputStream is = null;
		try {
			is = url.openStream();
			XMLInputFactory factory = XMLInputFactory.newInstance();
			reader = factory.createXMLStreamReader(is);

			reader.require(XMLStreamConstants.START_DOCUMENT, null, null);
			parseDocument(reader, url);
		} catch (XMLStreamException e) {
			throw new ParserException(e);
		} catch (IOException e) {
			throw new ParserException("File not found : " + url);
		} catch (ParserException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader == null)
				return;
			try {
				reader.close();
				is.close();
			} catch (XMLStreamException e) {
				throw new ParserException(e);
			} catch (IOException e) {
				throw new ParserException(e);
			}
		}
	}

	private void parseDocument(XMLStreamReader reader, URL url) throws XMLStreamException, ParserException, MalformedURLException, AMLNameAlreadyInUseException {

		reader.nextTag();
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_CAEX_FILE);

		if (!reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_SCHEMA_VERSION).equals(AMLElementNames.ATTRIBUTE_VALUE_SCHEMA_VERSION))
			throw new ParserException("Unsupported CAEX version");

		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(url.toString(), null, new AMLLocationInFileImpl());

		AMLCreateDocumentInstruction instruction = new AMLCreateDocumentInstruction(session, identifier);

		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			if (elementType == null) {
				// throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElements.ELEMENT_CAEX_FILE + ".");
				continue;
			}

			switch (elementType) {
			case ELEMENT_ADDITIONAL_INFORMATION:
				if (reader.getAttributeCount() == 1 && reader.getAttributeLocalName(0) == AMLElementNames.ATTRIBUTE_AUTOMATION_ML_VERSION)
					parseAMLVersion(reader);
				else if (reader.getAttributeCount() == 0)
					parseAdditionalInformation(reader, instruction);
				break;
			case ELEMENT_EXTERNAL_REFERENCE:
				parseExternalReferences(reader, identifier, url);
				break;
			case ELEMENT_INTERFACE_CLASS_LIB:
			case ELEMENT_ROLE_CLASS_LIB:
			case ELEMENT_SYSTEM_UNIT_CLASS_LIB:
				parseClassLibrary(reader, identifier, elementType);
				break;
			case ELEMENT_INSTANCE_HIERARCHY:
				parseInstanceHierarchy(reader, identifier);
				break;
			case ELEMENT_DESCRIPTION:
			case ELEMENT_VERSION:
			case ELEMENT_COPYRIGHT:
			case ELEMENT_REVISION:
				parseElement(reader, identifier, instruction);
				break;
			default:
				// throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElements.ELEMENT_CAEX_FILE + ".");
			}
		}
		addInstruction(identifier, instruction, false);

		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_CAEX_FILE);
	}

	private void parseInstanceHierarchy(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier) throws XMLStreamException, ParserException,
			AMLNameAlreadyInUseException {

		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_INSTANCE_HIERARCHY);

		String name = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_NAME);
		if (name == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_NAME + " in element " + AMLElementNames.ELEMENT_INSTANCE_HIERARCHY);

		AMLLocationInFile filePosition = getFilePosition(reader);
		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(name, parentIdentifier, filePosition);
		AMLCreateElementInstruction createElementInstruction = new AMLCreateElementInstruction(
				session,
				identifier,
				parentIdentifier,
				AMLElementType.ELEMENT_INSTANCE_HIERARCHY);
		createElementInstruction.name = name;
		addInstruction(identifier, createElementInstruction, false);

		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			if (elementType == null) {
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_INSTANCE_HIERARCHY + ".");
			}
			switch (elementType) {
			case ELEMENT_DESCRIPTION:
			case ELEMENT_VERSION:
			case ELEMENT_COPYRIGHT:
			case ELEMENT_ADDITIONAL_INFORMATION:
			case ELEMENT_REVISION:
				parseElement(reader, identifier, createElementInstruction);
				break;
			case ELEMENT_INTERNAL_ELEMENT:
				parseInternalElement(reader, identifier);
				break;
			default:
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_INSTANCE_HIERARCHY + ".");
			}
		}

		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_INSTANCE_HIERARCHY);
	}

	private void parseInternalElement(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier) throws XMLStreamException, ParserException,
			AMLNameAlreadyInUseException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_INTERNAL_ELEMENT);

		String originalUuidString = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_ID);
		String uuidString = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_ID);
		UUID id = null;
		try {
			uuidString = uuidString.replace("{", "");
			uuidString = uuidString.replace("}", "");
			id = UUID.fromString(uuidString);
		} catch (Exception e) {
			throw new ParserException(e);
		}

		if (id == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_ID + " in element " + AMLElementNames.ELEMENT_INTERNAL_ELEMENT);

		AMLLocationInFile filePosition = getFilePosition(reader);
		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(originalUuidString, parentIdentifier, filePosition);
		AMLCreateElementInstruction createElementInstruction = new AMLCreateElementInstruction(
				session,
				identifier,
				parentIdentifier,
				AMLElementType.ELEMENT_INTERNAL_ELEMENT);
		createElementInstruction.id = id;
		createElementInstruction.name = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_NAME);
		addInstruction(identifier, createElementInstruction, false);

		String refBaseClassPath = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_REF_BASE_SYSTEM_UNIT_PATH);
		if (refBaseClassPath != null && refBaseClassPath.length() != 0) {

			// test if UUID
			String copyRefBaseClassPath = refBaseClassPath;
			try {
				copyRefBaseClassPath = copyRefBaseClassPath.replace("{", "");
				copyRefBaseClassPath = copyRefBaseClassPath.replace("}", "");
				UUID refId = UUID.fromString(copyRefBaseClassPath);
				createElementInstruction.setElementType(AMLElementType.ELEMENT_MIRROR_OBJECT);
				AMLDeserializeReferenceIdentifier refBaseClassPathIdentifier = getReferencedDeserializeIdentifier(
						refBaseClassPath,
						parentIdentifier,
						filePosition);
				createElementInstruction.addReferencedIdentifier(refBaseClassPathIdentifier);

			} catch (Exception e) {
				filePosition = getFilePosition(reader);
				AMLDeserializeReferenceIdentifier refBaseClassPathIdentifier = getReferencedDeserializeIdentifier(
						refBaseClassPath,
						parentIdentifier,
						filePosition);

				AMLChangeReferenceInstruction changeReferenceInstruction = new AMLChangeReferenceInstruction(
						session,
						identifier,
						refBaseClassPathIdentifier,
						AMLElementType.ELEMENT_INTERNAL_ELEMENT);
				addInstruction(identifier, changeReferenceInstruction, false);
			}
		}

		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			if (elementType == null) {
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_INTERNAL_ELEMENT + ".");
			}
			switch (elementType) {
			case ELEMENT_ATTRIBUTE:
				parseAttribute(reader, identifier, originalUuidString, false);
				break;
			case ELEMENT_EXTERNAL_INTERFACE:
				parseExternalInterface(reader, identifier, originalUuidString);
				break;
			case ELEMENT_ROLE_REQUIREMENTS:
				parseRoleRequirements(reader, identifier, originalUuidString);
				break;
			case ELEMENT_DESCRIPTION:
			case ELEMENT_VERSION:
			case ELEMENT_COPYRIGHT:
			case ELEMENT_ADDITIONAL_INFORMATION:
			case ELEMENT_REVISION:
				parseElement(reader, identifier, createElementInstruction);
				break;
			case ELEMENT_INTERNAL_ELEMENT:
				parseInternalElement(reader, identifier);
				break;
			case ELEMENT_SUPPORTED_ROLE_CLASS:
				parseSupportedRoleClass(reader, identifier, originalUuidString);
				break;

			case ELEMENT_INTERNAL_LINK:
				parseInternalLink(reader, identifier);
				break;
			default:
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_INTERNAL_ELEMENT + ".");
			}
		}

		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_INTERNAL_ELEMENT);
	}

	private void parseInternalLink(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier) throws XMLStreamException, ParserException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_INTERNAL_LINK);

		AMLLocationInFile filePosition = getFilePosition(reader);
		String name = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_NAME);

		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(name, parentIdentifier, filePosition);

		AMLCreateElementInstruction createInstruction = new AMLCreateElementInstruction(
				session,
				identifier,
				parentIdentifier,
				AMLElementType.ELEMENT_INTERNAL_LINK);
		createInstruction.name = name;
		String refPartnerSideA = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_REF_PARTNER_SIDE_A);
		if (refPartnerSideA == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_REF_PARTNER_SIDE_A + " in element "
					+ AMLElementNames.ELEMENT_INTERNAL_LINK);
		String refPartnerSideB = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_REF_PARTNER_SIDE_B);
		if (refPartnerSideB == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_REF_PARTNER_SIDE_B + " in element "
					+ AMLElementNames.ELEMENT_INTERNAL_LINK);

		AMLDeserializeReferenceIdentifier refPartnerSideAIdentifier = getReferencedDeserializeIdentifier(refPartnerSideA, parentIdentifier, filePosition);
		AMLDeserializeReferenceIdentifier refPartnerSideBIdentifier = getReferencedDeserializeIdentifier(refPartnerSideB, parentIdentifier, filePosition);
		createInstruction.addReferencedIdentifier(refPartnerSideAIdentifier);
		createInstruction.addReferencedIdentifier(refPartnerSideBIdentifier);
		internalLinkRelatedInstructions.add(createInstruction);

		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			switch (elementType) {

			default:
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_ROLE_REQUIREMENTS + ".");
			}
		}

		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_INTERNAL_LINK);
	}

	private void parseRoleRequirements(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier, String uuidString) throws XMLStreamException,
			ParserException, AMLNameAlreadyInUseException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_ROLE_REQUIREMENTS);

		AMLLocationInFile filePosition = getFilePosition(reader);
		String refRoleClassPath = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_REF_BASE_ROLE_CLASS_PATH);
		StringBuffer supportedRoleClassName = new StringBuffer();
		supportedRoleClassName.append(uuidString).append(PATH_SEPARATOR).append(refRoleClassPath);

		AMLDeserializeIdentifier identifier = null;
		if (normalizeName(refRoleClassPath).equals(AUTOMATION_ML_ROLE_FACET_PATH)) {
			AMLCreateElementInstruction createInstruction = (AMLCreateElementInstruction) identifierToCreateInstruction.get(parentIdentifier);
			createInstruction.setElementType(AMLElementType.ELEMENT_FACET);
			AMLDeserializeReferenceIdentifier refRoleClassPathIdentifier = getReferencedDeserializeIdentifier(refRoleClassPath, parentIdentifier, filePosition);
			createInstruction.addReferencedIdentifier(refRoleClassPathIdentifier);
			identifier = parentIdentifier;
			reader.nextTag();
		} else if (normalizeName(refRoleClassPath).equals(AUTOMATION_ML_ROLE_GROUP_PATH)) {
			AMLCreateElementInstruction createInstruction = (AMLCreateElementInstruction) identifierToCreateInstruction.get(parentIdentifier);
			createInstruction.setElementType(AMLElementType.ELEMENT_GROUP);
			AMLDeserializeReferenceIdentifier refRoleClassPathIdentifier = getReferencedDeserializeIdentifier(refRoleClassPath, parentIdentifier, filePosition);
			createInstruction.addReferencedIdentifier(refRoleClassPathIdentifier);
			identifier = parentIdentifier;
			reader.nextTag();
		} else {
			identifier = createDeserializeIdentifier("", parentIdentifier, filePosition);

			AMLCreateElementInstruction createInstruction = new AMLCreateElementInstruction(
					session,
					identifier,
					parentIdentifier,
					AMLElementType.ELEMENT_ROLE_REQUIREMENTS);

			if (refRoleClassPath == null)
				throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_REF_BASE_ROLE_CLASS_PATH + " in element "
						+ AMLElementNames.ELEMENT_SUPPORTED_ROLE_CLASS);

			if (!hasDeserializeIdentifier(supportedRoleClassName.toString())) {
				AMLDeserializeIdentifier identifier1 = createDeserializeIdentifier(supportedRoleClassName.toString(), parentIdentifier, filePosition);
				AMLCreateElementInstruction createInstruction1 = new AMLCreateElementInstruction(
						session,
						identifier1,
						parentIdentifier,
						AMLElementType.ELEMENT_SUPPORTED_ROLE_CLASS);

				AMLDeserializeReferenceIdentifier refRoleClassPathIdentifier = getReferencedDeserializeIdentifier(
						refRoleClassPath,
						parentIdentifier,
						filePosition);
				createInstruction1.addReferencedIdentifier(refRoleClassPathIdentifier);
				addInstruction(identifier1, createInstruction1, false);
			}

			AMLDeserializeReferenceIdentifier refRoleClassPathIdentifier = getReferencedDeserializeIdentifier(
					supportedRoleClassName.toString(),
					parentIdentifier,
					filePosition);
			createInstruction.addReferencedIdentifier(refRoleClassPathIdentifier);
			addInstruction(identifier, createInstruction, false);
			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
				AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
				switch (elementType) {
				case ELEMENT_ATTRIBUTE:
					parseAttribute(reader, identifier, "", false);
					break;

				default:
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_SUPPORTED_ROLE_CLASS + ".");
				}
			}
		}

		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_ROLE_REQUIREMENTS);
	}

	private void parseExternalInterface(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier, String path) throws XMLStreamException,
			ParserException, AMLNameAlreadyInUseException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_EXTERNAL_INTERFACE);

		String uuidString = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_ID);
		UUID id = null;

		if (uuidString == null) {
			id = UUID.randomUUID();
		} else {
			try {
				uuidString = uuidString.replace("{", "");
				uuidString = uuidString.replace("}", "");
				id = UUID.fromString(uuidString);
			} catch (Exception e) {
				throw new ParserException(e);
			}
		}

		String name = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_NAME);

		AMLLocationInFile filePosition = getFilePosition(reader);

		String refBaseClassPath = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_REF_BASE_CLASS_PATH);
		if (refBaseClassPath == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_REF_BASE_CLASS_PATH + " in element "
					+ AMLElementNames.ELEMENT_EXTERNAL_INTERFACE);
		StringBuffer classPath = new StringBuffer();
		classPath.append(path).append(":").append(name);

		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(classPath.toString(), parentIdentifier, filePosition);
		AMLDeserializeReferenceIdentifier refBaseClassPathIdentifier = getReferencedDeserializeIdentifier(refBaseClassPath, parentIdentifier, filePosition);
		
		for (AMLDocument existing : session.getDocuments()) {
			
			try {
				AMLInterfaceClass interfaceClass = existing.getInterfaceClassByPath(refBaseClassPath);
				if (interfaceClass != null) {
					AMLDeserializeIdentifier resolved = new AMLDeserializeIdentifier(refBaseClassPath);
					resolved.setResolvedElement(interfaceClass);
					refBaseClassPathIdentifier.setReferencedIdentifier(resolved);
				}
			} catch (AMLValidationException e) {
				e.printStackTrace();
			}
		}
		
		if (refBaseClassPath.equals("AutomationMLInterfaceClassLib/AutomationMLBaseInterface/ExternalDataConnector/COLLADAInterface")) {
			AMLCreateCOLLADAInterfaceInstruction createElementInstruction = new AMLCreateCOLLADAInterfaceInstruction(session, identifier, parentIdentifier);
			createElementInstruction.name = name;
			createElementInstruction.addReferencedIdentifier(refBaseClassPathIdentifier);
			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
				AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
				if (elementType == null) {
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_EXTERNAL_INTERFACE + ".");
				}
				switch (elementType) {
				case ELEMENT_DESCRIPTION:
				case ELEMENT_VERSION:
				case ELEMENT_COPYRIGHT:
				case ELEMENT_ADDITIONAL_INFORMATION:
				case ELEMENT_REVISION:
					parseElement(reader, identifier, createElementInstruction);
					break;
				case ELEMENT_ATTRIBUTE:
					reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_ATTRIBUTE);

					String attrName = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_NAME);
					if (attrName == null)
						throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_NAME + " in element " + AMLElementNames.ELEMENT_ATTRIBUTE);

					if (!attrName.equals("refURI") && !attrName.equals("refType") && !attrName.equals("target"))
						throw new ParserException("Unexpected attribute " + AMLElementNames.ATTRIBUTE_NAME + " in element " + AMLElementNames.ELEMENT_INTERFACE_CLASS
								+ ". Should be refURI, refType or target.");

					while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
						AMLElementType elementTypeAttr = AMLElementNames.getElementType(reader.getLocalName());
						if (elementTypeAttr == null) {
							throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_ATTRIBUTE + ".");
						}
						switch (elementTypeAttr) {
						case ELEMENT_DESCRIPTION:
						case ELEMENT_DEFAULT_VALUE:
						case ELEMENT_CONSTRAINT:
							break;
						case ELEMENT_VALUE:
							if (attrName.equals("refURI"))
								createElementInstruction.uri = URI.create(reader.getElementText());
							else if (attrName.equals("target"))
								createElementInstruction.target = reader.getElementText();
							else if (attrName.equals("refType")) {
								String refType = reader.getElementText();
								createElementInstruction.refType = refType.equals("explicit") ? AMLCOLLADAInterface.RefType.EXPLICIT : AMLCOLLADAInterface.RefType.IMPLICIT;
							}								
							break;

						default:
							throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_ATTRIBUTE + ".");
						}
					}

					reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_ATTRIBUTE);
					break;
				default:
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_EXTERNAL_INTERFACE + ".");
				}
			}
			addInstruction(identifier, createElementInstruction, false);
		} else {
			AMLCreateElementInstruction createElementInstruction = new AMLCreateElementInstruction(
					session,
					identifier,
					parentIdentifier,
					AMLElementType.ELEMENT_EXTERNAL_INTERFACE);
			createElementInstruction.id = id;
			createElementInstruction.name = name;
			createElementInstruction.addReferencedIdentifier(refBaseClassPathIdentifier);
			internalLinkRelatedInstructions.add(createElementInstruction);
	
			List<AMLCreateExternalInterfaceAttributeInstruction> attributeInstructions = new ArrayList<AMLCreateExternalInterfaceAttributeInstruction>();
			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
				AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
				if (elementType == null) {
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_EXTERNAL_INTERFACE + ".");
				}
				switch (elementType) {
				case ELEMENT_DESCRIPTION:
				case ELEMENT_VERSION:
				case ELEMENT_COPYRIGHT:
				case ELEMENT_ADDITIONAL_INFORMATION:
				case ELEMENT_REVISION:
					parseElement(reader, identifier, createElementInstruction);
					break;
				case ELEMENT_ATTRIBUTE:
					parseExternalInterfaceAttribute(reader, identifier, classPath.toString(), attributeInstructions);
					break;
				default:
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_EXTERNAL_INTERFACE + ".");
				}
			}
	
			for (AMLCreateExternalInterfaceAttributeInstruction instruction : attributeInstructions) {
				instruction.setExternalInterfaceIdentifier(identifier);
			}
		}

		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_EXTERNAL_INTERFACE);
	}

	private void parseExternalInterfaceAttribute(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier, String path,
			List<AMLCreateExternalInterfaceAttributeInstruction> attributeInstructions) throws XMLStreamException, ParserException,
			AMLNameAlreadyInUseException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_ATTRIBUTE);

		String name = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_NAME);
		if (name == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_NAME + " in element " + AMLElementNames.ELEMENT_INTERFACE_CLASS);

		StringBuffer classPath = new StringBuffer();
		classPath.append(path).append(PATH_SEPARATOR).append(name);

		AMLLocationInFile filePosition = getFilePosition(reader);
		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(classPath.toString(), parentIdentifier, filePosition);

		AMLCreateExternalInterfaceAttributeInstruction createElementInstruction = new AMLCreateExternalInterfaceAttributeInstruction(
				session,
				identifier,
				parentIdentifier);
		createElementInstruction.name = name;
		createElementInstruction.unit = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_UNIT);
		createElementInstruction.dataType = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_ATTRIBUTE_DATA_TYPE);

		attributeInstructions.add(createElementInstruction);

		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			if (elementType == null) {
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_ATTRIBUTE + ".");
			}
			switch (elementType) {
			case ELEMENT_DESCRIPTION:
				createElementInstruction.description = reader.getElementText();
				break;
			case ELEMENT_VALUE:
				createElementInstruction.value = reader.getElementText();
				break;
			case ELEMENT_DEFAULT_VALUE:
				createElementInstruction.defaultValue = reader.getElementText();
				break;
			case ELEMENT_ATTRIBUTE:
				parseExternalInterfaceAttribute(reader, identifier, classPath.toString(), attributeInstructions);
				break;
			case ELEMENT_CONSTRAINT:
				parseConstraint(reader, identifier, classPath.toString());
				break;
			default:
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_INTERFACE_CLASS + ".");
			}
		}
		internalLinkRelatedInstructions.add(createElementInstruction);
		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_ATTRIBUTE);
	}

	private void parseExternalReferences(XMLStreamReader reader, AMLDeserializeIdentifier documentIdentifier, URL baseUrl) throws XMLStreamException,
			ParserException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_EXTERNAL_REFERENCE);

		String pathString = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_PATH);
		if (pathString == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_PATH + " in element " + AMLElementNames.ELEMENT_EXTERNAL_REFERENCE);

		String alias = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_ALIAS);
		if (alias == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_ALIAS + " in element " + AMLElementNames.ELEMENT_EXTERNAL_REFERENCE);

		try {
			addExternalReference(baseUrl, pathString, alias, documentIdentifier);
		} catch (Exception e) {
			throw new ParserException("Attribute " + AMLElementNames.ATTRIBUTE_PATH + " in element " + AMLElementNames.ELEMENT_EXTERNAL_REFERENCE
					+ " is not a valid URL.");
		}

		reader.nextTag();
		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_EXTERNAL_REFERENCE);
	}

	private void addExternalReference(URL baseUrl, String pathString, String alias, AMLDeserializeIdentifier parentIdentifier) throws URISyntaxException,
			MalformedURLException {
		URL originalUrl = new URL(baseUrl, pathString);
		URL resolvedUrl = null;

		URL url = originalUrl;

		AMLDocument document = getDocument(originalUrl);
		AMLDeserializeReferenceIdentifier documentIdentifier = null;
		if (document == null) {
			documentIdentifier = getDocumentIdentifier(originalUrl);
			if (documentIdentifier.getResolvedElement() == null) {
				if (fileExists(url)) {
					documentIdentifier = getReferencedDeserializeIdentifier(url.toString());
				} else {
					DocumentURLResolver documentURLResolver = session.getDocumentURLResolver();
					if (documentURLResolver != null) {
						resolvedUrl = documentURLResolver.getResolvedURL(baseUrl, pathString);
						url = resolvedUrl;
						document = getDocument(resolvedUrl);
					}
					if (document == null) {
						documentIdentifier = getDocumentIdentifier(url);
						if (documentIdentifier == null) {
							if (fileExists(url)) {
								documentIdentifier = getReferencedDeserializeIdentifier(url.toString());
							} else {
								missingFiles.add(originalUrl);
								return;
							}
						}
					}
				}
			}
		}

		if (document != null) {
			getDocumentManager().setAlias(document, alias);
			AMLDeserializeReferenceIdentifier referenceIdentifier = getReferencedDeserializeIdentifier(originalUrl.toString());
			AMLDeserializeIdentifier identifier = new AMLDeserializeIdentifier(originalUrl.toString());
			identifier.setResolvedElement(document);
			referenceIdentifier.setReferencedIdentifier(identifier);
			AMLChangeReferenceInstruction instruction = new AMLChangeReferenceInstruction(
					session,
					parentIdentifier,
					referenceIdentifier,
					AMLElementType.ELEMENT_EXTERNAL_REFERENCE);
			addChangeInstruction(instruction);
			return;
		}

		if (documentIdentifier != null) {
			AMLChangeReferenceInstruction instruction = new AMLChangeReferenceInstruction(
					session,
					parentIdentifier,
					documentIdentifier,
					AMLElementType.ELEMENT_EXTERNAL_REFERENCE);
			addChangeInstruction(instruction);
		}

		if (!resolvedFiles.contains(url))
			unresolvedFiles.add(url);

	}

	private AMLDeserializeReferenceIdentifier getDocumentIdentifier(URL url) {
		AMLDeserializeReferenceIdentifier identifier = getReferencedDeserializeIdentifier(url.toString());
		return identifier;
	}

	private AMLDocument getDocument(URL url) {
		DocumentLocation documentLocation = new URLDocumentLocation(url);

		AMLDocument document = getDocumentManager().getDocument(documentLocation);
		return document;
	}

	private AMLDocumentManager getDocumentManager() {
		return ((AMLSessionImpl) session).getDocumentManager();
	}

	private boolean fileExists(URL url) {
		try {
			InputStream stream = url.openStream();
			stream.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private void parseClassLibrary(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier, AMLElementType libraryElementType)
			throws XMLStreamException, ParserException, AMLNameAlreadyInUseException {

		String elementLibraryName = null;
		switch (libraryElementType) {
		case ELEMENT_INTERFACE_CLASS_LIB:
			elementLibraryName = AMLElementNames.ELEMENT_INTERFACE_CLASS_LIB;
			break;
		case ELEMENT_ROLE_CLASS_LIB:
			elementLibraryName = AMLElementNames.ELEMENT_ROLE_CLASS_LIB;
			break;
		case ELEMENT_SYSTEM_UNIT_CLASS_LIB:
			elementLibraryName = AMLElementNames.ELEMENT_SYSTEM_UNIT_CLASS_LIB;
			break;
		default:
			break;
		}

		if (elementLibraryName == null) {
			// throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElements.ELEMENT_CAEX_FILE + ".");
			return;
		}

		reader.require(XMLStreamConstants.START_ELEMENT, null, elementLibraryName);

		String name = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_NAME);
		if (name == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_NAME + " in element " + elementLibraryName);

		AMLLocationInFile filePosition = getFilePosition(reader);
		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(name, parentIdentifier, filePosition);
		AMLCreateElementInstruction createElementInstruction = new AMLCreateElementInstruction(session, identifier, parentIdentifier, libraryElementType);
		createElementInstruction.name = name;
		addInstruction(identifier, createElementInstruction, true);

		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			if (elementType == null) {
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + elementLibraryName + ".");
			}
			switch (elementType) {
			case ELEMENT_DESCRIPTION:
			case ELEMENT_VERSION:
			case ELEMENT_COPYRIGHT:
			case ELEMENT_ADDITIONAL_INFORMATION:
			case ELEMENT_REVISION:
				parseElement(reader, identifier, createElementInstruction);
				break;
			case ELEMENT_INTERFACE_CLASS:
				if (libraryElementType != AMLElementType.ELEMENT_INTERFACE_CLASS_LIB)
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + elementLibraryName + ".");
				parseClass(reader, identifier, name, elementType);
				break;
			case ELEMENT_ROLE_CLASS:
				if (libraryElementType != AMLElementType.ELEMENT_ROLE_CLASS_LIB)
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + elementLibraryName + ".");
				parseClass(reader, identifier, name, elementType);
				break;
			case ELEMENT_SYSTEM_UNIT_CLASS:
				if (libraryElementType != AMLElementType.ELEMENT_SYSTEM_UNIT_CLASS_LIB)
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + elementLibraryName + ".");
				parseClass(reader, identifier, name, elementType);
				break;
			default:
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + elementLibraryName + ".");
			}
		}

		reader.require(XMLStreamConstants.END_ELEMENT, null, elementLibraryName);
	}

	private void parseClass(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier, String pathFragment, AMLElementType elementClassType)
			throws XMLStreamException, ParserException, AMLNameAlreadyInUseException {

		String elementClassName = null;
		switch (elementClassType) {
		case ELEMENT_INTERFACE_CLASS:
			elementClassName = AMLElementNames.ELEMENT_INTERFACE_CLASS;
			break;
		case ELEMENT_ROLE_CLASS:
			elementClassName = AMLElementNames.ELEMENT_ROLE_CLASS;
			break;
		case ELEMENT_SYSTEM_UNIT_CLASS:
			elementClassName = AMLElementNames.ELEMENT_SYSTEM_UNIT_CLASS;
			break;
		default:
			break;
		}

		if (elementClassName == null) {
			// throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElements.ELEMENT_CAEX_FILE + ".");
			return;
		}

		reader.require(XMLStreamConstants.START_ELEMENT, null, elementClassName);

		String name = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_NAME);
		if (name == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_NAME + " in element " + elementClassName);

		StringBuffer classPath = new StringBuffer();
		classPath.append(pathFragment).append(PATH_SEPARATOR).append(name);
		AMLLocationInFile filePosition = getFilePosition(reader);
		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(classPath.toString(), parentIdentifier, filePosition);

		AMLCreateElementInstruction createElementInstruction = new AMLCreateElementInstruction(session, identifier, parentIdentifier, elementClassType);
		createElementInstruction.name = name;
		

		String refBaseClassPath = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_REF_BASE_CLASS_PATH);
		if (refBaseClassPath != null) {
			filePosition = getFilePosition(reader);
			
			if (refBaseClassPath.indexOf('/') == -1) {
				refBaseClassPath = pathFragment;
			}
			
			AMLDeserializeReferenceIdentifier refBaseClassPathIdentifier = getReferencedDeserializeIdentifier(refBaseClassPath, parentIdentifier, filePosition);
			
			createElementInstruction.addReferencedIdentifier(refBaseClassPathIdentifier);

//			AMLChangeReferenceInstruction changeReferenceInstruction = new AMLChangeReferenceInstruction(
//					session,
//					identifier,
//					refBaseClassPathIdentifier,
//					elementClassType);
//			addInstruction(identifier, changeReferenceInstruction, true);
		}
		
		addInstruction(identifier, createElementInstruction, true);

		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			if (elementType == null) {
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + elementClassName + ".");
			}
			switch (elementType) {
			case ELEMENT_DESCRIPTION:
			case ELEMENT_VERSION:
			case ELEMENT_COPYRIGHT:
			case ELEMENT_ADDITIONAL_INFORMATION:
			case ELEMENT_REVISION:
				parseElement(reader, identifier, createElementInstruction);
				break;
			case ELEMENT_ATTRIBUTE:
				parseAttribute(reader, identifier, classPath.toString(), true);
				break;
			case ELEMENT_INTERFACE_CLASS:
			case ELEMENT_ROLE_CLASS:
			case ELEMENT_SYSTEM_UNIT_CLASS:
				if (elementClassType != elementType) {
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_INTERFACE_CLASS + ".");
				}
				parseClass(reader, identifier, classPath.toString(), elementType);
				break;
			case ELEMENT_INTERNAL_ELEMENT:
				if (elementClassType != AMLElementType.ELEMENT_SYSTEM_UNIT_CLASS)
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + elementClassName + ".");
				parseInternalElement(reader, identifier);
				break;
			case ELEMENT_EXTERNAL_INTERFACE:
				parseExternalInterface(reader, identifier, classPath.toString());
				break;
			case ELEMENT_SUPPORTED_ROLE_CLASS:
				parseSupportedRoleClass(reader, identifier, classPath.toString());
				break;
			case ELEMENT_INTERNAL_LINK:
				parseInternalLink(reader, identifier);
				break;
			default:
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_INTERFACE_CLASS + ".");
			}
		}

		reader.require(XMLStreamConstants.END_ELEMENT, null, elementClassName);
	}

	private void parseSupportedRoleClass(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier, String path) throws XMLStreamException,
			ParserException, AMLNameAlreadyInUseException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_SUPPORTED_ROLE_CLASS);

		AMLLocationInFile filePosition = getFilePosition(reader);

		String refRoleClassPath = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_REF_ROLE_CLASS_PATH);
		if (refRoleClassPath == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_REF_ROLE_CLASS_PATH + " in element "
					+ AMLElementNames.ELEMENT_SUPPORTED_ROLE_CLASS);

		StringBuffer name = new StringBuffer();
		name.append(path).append(PATH_SEPARATOR).append(refRoleClassPath);
		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(name.toString(), parentIdentifier, filePosition);
		AMLCreateElementInstruction createInstruction = new AMLCreateElementInstruction(
				session,
				identifier,
				parentIdentifier,
				AMLElementType.ELEMENT_SUPPORTED_ROLE_CLASS);

		AMLDeserializeReferenceIdentifier refRoleClassPathIdentifier = getReferencedDeserializeIdentifier(refRoleClassPath, parentIdentifier, filePosition);
		createInstruction.addReferencedIdentifier(refRoleClassPathIdentifier);

		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			switch (elementType) {
			case ELEMENT_MAPPING_OBJECT:
				parseMappingObject(reader, parentIdentifier, path, filePosition, createInstruction, refRoleClassPath);
				break;
			default:
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_SUPPORTED_ROLE_CLASS + ".");
			}
		}

		addInstruction(identifier, createInstruction, false);

		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_SUPPORTED_ROLE_CLASS);
	}

	private void parseMappingObject(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier, String path, AMLLocationInFile filePosition,
			AMLCreateElementInstruction createInstruction, String refRoleClassPath) throws XMLStreamException, ParserException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_MAPPING_OBJECT);
		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType2 = AMLElementNames.getElementType(reader.getLocalName());
			if (elementType2 == null) {
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_MAPPING_OBJECT + ".");
			}
			switch (elementType2) {
			case ELEMENT_ATTRIBUTE_NAME_MAPPING:
				parseAttributeNameMapping(reader, parentIdentifier, path, filePosition, createInstruction, refRoleClassPath);
				break;
			default:
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_MAPPING_OBJECT + ".");
			}
		}

		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_MAPPING_OBJECT);
	}

	private void parseAttributeNameMapping(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier, String path, AMLLocationInFile filePosition,
			AMLCreateElementInstruction createInstruction, String refRoleClassPath) throws XMLStreamException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_ATTRIBUTE_NAME_MAPPING);

		StringBuffer systemUnitAttributeName = new StringBuffer();
		systemUnitAttributeName.append(path);
		systemUnitAttributeName.append(PATH_SEPARATOR).append(reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_SYSTEM_UNIT_ATTRIBUTE_NAME));
		StringBuffer roleAttributeName = new StringBuffer();
		roleAttributeName.append(refRoleClassPath);
		roleAttributeName.append(PATH_SEPARATOR).append(reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_ROLE_ATTRIBUTE_NAME));

		AMLDeserializeReferenceIdentifier systemUnitAttributeIdentifier = getReferencedDeserializeIdentifier(
				systemUnitAttributeName.toString(),
				parentIdentifier,
				filePosition);
		AMLDeserializeReferenceIdentifier roleAttributeNameIdentifier = getReferencedDeserializeIdentifier(
				roleAttributeName.toString(),
				parentIdentifier,
				filePosition);

		createInstruction.addReferencedIdentifier(systemUnitAttributeIdentifier);
		createInstruction.addReferencedIdentifier(roleAttributeNameIdentifier);

		reader.nextTag();
		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_ATTRIBUTE_NAME_MAPPING);
	}

	private void parseAttribute(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier, String path, boolean library) throws XMLStreamException, ParserException,
			AMLNameAlreadyInUseException {

		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_ATTRIBUTE);

		String name = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_NAME);
		if (name == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_NAME + " in element " + AMLElementNames.ELEMENT_INTERFACE_CLASS);

		StringBuffer classPath = new StringBuffer();
		classPath.append(path).append(PATH_SEPARATOR).append(name);

		AMLLocationInFile filePosition = getFilePosition(reader);
		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(classPath.toString(), parentIdentifier, filePosition);

		AMLCreateInstruction testCreateInstruction = identifierToCreateInstruction.get(parentIdentifier);
		if (testCreateInstruction == null)
			testCreateInstruction = identifierToCreateLibraryInstruction.get(parentIdentifier);
		if (name.equals(AUTOMATIONML_FRAME_ATTRIBUTE)
				&& testCreateInstruction instanceof AMLCreateElementInstruction
				&& (((AMLCreateElementInstruction) testCreateInstruction).getElementType() == AMLElementType.ELEMENT_INTERNAL_ELEMENT || ((AMLCreateElementInstruction) testCreateInstruction)
						.getElementType() == AMLElementType.ELEMENT_SYSTEM_UNIT_CLASS)) {
			AMLCreateFrameAttributeInstruction createInstruction = new AMLCreateFrameAttributeInstruction(session, identifier, parentIdentifier);

			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
				AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
				if (elementType == null) {
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_ATTRIBUTE + ".");
				}
				switch (elementType) {
				case ELEMENT_DESCRIPTION:
				case ELEMENT_VALUE:
				case ELEMENT_DEFAULT_VALUE:
				case ELEMENT_CONSTRAINT:
					break;
				case ELEMENT_ATTRIBUTE:
					parseFrameAttributeValues(reader, createInstruction);
					break;
				default:
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_INTERFACE_CLASS + ".");
				}
			}
			addInstruction(identifier, createInstruction, library);

		} else {
			AMLCreateAttributeInstruction createElementInstruction = new AMLCreateAttributeInstruction(session, identifier, parentIdentifier);
			createElementInstruction.name = name;
			createElementInstruction.unit = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_UNIT);
			createElementInstruction.dataType = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_ATTRIBUTE_DATA_TYPE);

			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
				AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
				if (elementType == null) {
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_ATTRIBUTE + ".");
				}
				switch (elementType) {
				case ELEMENT_DESCRIPTION:
					createElementInstruction.description = reader.getElementText();
					break;
				case ELEMENT_VALUE:
					createElementInstruction.value = reader.getElementText();
					break;
				case ELEMENT_DEFAULT_VALUE:
					createElementInstruction.defaultValue = reader.getElementText();
					break;
				case ELEMENT_ATTRIBUTE:
					parseAttribute(reader, identifier, classPath.toString(), library);
					break;
				case ELEMENT_REF_SEMANTIC:
					parseRefSemantic(reader, identifier, classPath.toString());
					break;
				case ELEMENT_CONSTRAINT:
					parseConstraint(reader, identifier, classPath.toString());
					break;
				default:
					throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_INTERFACE_CLASS + ".");
				}
			}
			addInstruction(identifier, createElementInstruction, library);
		}
		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_ATTRIBUTE);

	}

	private void parseRefSemantic(XMLStreamReader reader,
			AMLDeserializeIdentifier parentIdentifier, String path) throws XMLStreamException, ParserException, AMLNameAlreadyInUseException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_REF_SEMANTIC);
		
		String attributePath = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_CORRESPONDING_ATTRIBUTE_PATH);
		if (attributePath == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_CORRESPONDING_ATTRIBUTE_PATH + " in element " + AMLElementNames.ELEMENT_REF_SEMANTIC);

		StringBuffer classPath = new StringBuffer();
		classPath.append(path).append(PATH_SEPARATOR).append(attributePath);

		AMLLocationInFile filePosition = getFilePosition(reader);
		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(classPath.toString(), parentIdentifier, filePosition);
		
		AMLCreateRefSemanticInstruction createRefSemanticInstruction = new AMLCreateRefSemanticInstruction(session, identifier, parentIdentifier);
		createRefSemanticInstruction.attributePath = attributePath;
		
		addInstruction(identifier, createRefSemanticInstruction, false);
		reader.next();
		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_REF_SEMANTIC);
	}

	private void parseFrameAttributeValues(XMLStreamReader reader, AMLCreateFrameAttributeInstruction createInstruction) throws XMLStreamException,
			ParserException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_ATTRIBUTE);

		String name = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_NAME);
		if (name == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_NAME + " in element " + AMLElementNames.ELEMENT_INTERFACE_CLASS);

		if (!name.equals("x") && !name.equals("y") && !name.equals("z") && !name.equals("rx") && !name.equals("ry") && !name.equals("rz"))
			throw new ParserException("Unexpected attribute " + AMLElementNames.ATTRIBUTE_NAME + " in element " + AMLElementNames.ELEMENT_INTERFACE_CLASS
					+ ". Should be x, y, z, rx, ry or rz.");

		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			if (elementType == null) {
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_ATTRIBUTE + ".");
			}
			switch (elementType) {
			case ELEMENT_DESCRIPTION:
			case ELEMENT_DEFAULT_VALUE:
			case ELEMENT_CONSTRAINT:
				break;
			case ELEMENT_VALUE:
				if (name.equals("x"))
					createInstruction.x = Double.valueOf(reader.getElementText());
				else if (name.equals("y"))
					createInstruction.y = Double.valueOf(reader.getElementText());
				else if (name.equals("z"))
					createInstruction.z = Double.valueOf(reader.getElementText());
				else if (name.equals("rx"))
					createInstruction.rx = Double.valueOf(reader.getElementText());
				else if (name.equals("ry"))
					createInstruction.ry = Double.valueOf(reader.getElementText());
				else if (name.equals("rz"))
					createInstruction.rz = Double.valueOf(reader.getElementText());
				break;

			default:
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_INTERFACE_CLASS + ".");
			}
		}

		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_ATTRIBUTE);
	}

	private void parseConstraint(XMLStreamReader reader, AMLDeserializeIdentifier parentIdentifier, String path) throws XMLStreamException, ParserException, AMLNameAlreadyInUseException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_CONSTRAINT);

		String name = reader.getAttributeValue(null, AMLElementNames.ATTRIBUTE_NAME);
		if (name == null)
			throw new ParserException("Missing attribute " + AMLElementNames.ATTRIBUTE_NAME + " in element " + AMLElementNames.ELEMENT_CONSTRAINT);

		StringBuffer classPath = new StringBuffer();
		classPath.append(path).append(PATH_SEPARATOR).append(name);

		AMLLocationInFile filePosition = getFilePosition(reader);
		AMLDeserializeIdentifier identifier = createDeserializeIdentifier(classPath.toString(), parentIdentifier, filePosition);
		
		AMLCreateConstraintInstruction createElementInstruction = null;
		
		if (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			if (elementType == null) {
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_CONSTRAINT + ".");
			}
			switch (elementType) {
			case ELEMENT_UNKNOWN_TYPE:
				createElementInstruction = new AMLCreateConstraintInstruction(session, identifier, parentIdentifier, AMLCreateConstraintInstruction.TYPE_UNKNOWN_TYPE);
				reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_UNKNOWN_TYPE);	
				if (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
					reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_REQUIREMENTS);
					createElementInstruction.requirement = reader.getElementText();
					reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_REQUIREMENTS);
				}
				reader.nextTag();
				reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_UNKNOWN_TYPE);
				break;
			case ELEMENT_NOMINAL_SCALED_TYPE:
				createElementInstruction = new AMLCreateConstraintInstruction(session, identifier, parentIdentifier, AMLCreateConstraintInstruction.TYPE_NOMINAL_SCALED_TYPE);
				reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_NOMINAL_SCALED_TYPE);
				while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
					reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_REQUIRED_VALUE);
					createElementInstruction.requieredValues.add(reader.getElementText());
					reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_REQUIRED_VALUE);
				}
				reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_NOMINAL_SCALED_TYPE);
				break;
			case ELEMENT_ORDINAL_SCALED_TYPE:
				createElementInstruction = new AMLCreateConstraintInstruction(session, identifier, parentIdentifier, AMLCreateConstraintInstruction.TYPE_ORDINAL_SCALED_TYPE);
				reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_ORDINAL_SCALED_TYPE);
				if (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
					reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_REQUIRED_MAX_VALUE);
					createElementInstruction.maxValue = reader.getElementText();
					reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_REQUIRED_MAX_VALUE);
				}
				if (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
					reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_REQUIRED_VALUE);
					createElementInstruction.requirement = reader.getElementText();
					reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_REQUIRED_VALUE);
				}
				if (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
					reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_REQUIRED_MIN_VALUE);
					createElementInstruction.minValue = reader.getElementText();
					reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_REQUIRED_MIN_VALUE);
				}
				reader.nextTag();
				reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_ORDINAL_SCALED_TYPE);
				break;			
			default:
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_CONSTRAINT + ".");
			}
		}
		createElementInstruction.name = name;
		addInstruction(identifier, createElementInstruction, false);
		
		reader.nextTag();// characters
		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_CONSTRAINT);
	}

	private void skipElement(XMLStreamReader reader) throws XMLStreamException {
		if (!reader.isStartElement())
			return;
		// reader.next();
		String elementName = reader.getLocalName();
		while (reader.hasNext()) {
			if (reader.isEndElement()) {
				if (elementName.equals(reader.getLocalName()))
					break;
			}
			reader.next();
		}
	}

	private AMLDeserializeReferenceIdentifier getReferencedDeserializeIdentifier(String name) {
		return getReferencedDeserializeIdentifier(name, null, new AMLLocationInFileImpl());
	}

	private AMLDeserializeReferenceIdentifier getReferencedDeserializeIdentifier(String name, AMLDeserializeIdentifier parentIdentifier,
			AMLLocationInFile filePosition) {

		String normalizedName = normalizeName(name);

		AMLDeserializeReferenceIdentifier referenceIdentifier = unresolvedIdentifiers.get(normalizedName);
		if (referenceIdentifier != null)
			return referenceIdentifier;

		referenceIdentifier = new AMLDeserializeReferenceIdentifier(normalizedName, filePosition);

		AMLDeserializeIdentifier identifier = aMLDeserializeIdentifiers.get(normalizedName);
		if (identifier == null)
			unresolvedIdentifiers.put(normalizedName, referenceIdentifier);
		else
			referenceIdentifier.setReferencedIdentifier(identifier);

		return referenceIdentifier;
	}

	private String normalizeName(String name) {
		String copyName = new String(name);
		int index = copyName.indexOf('@');
		if (index >= 0) {
			int index2 = copyName.lastIndexOf(PATH_SEPARATOR, index);
			if (index2 >= 0) {
				copyName = copyName.substring(0, index2 + 1) + copyName.substring(index + 1);
			} else {
				copyName = copyName.substring(index + 1);
			}
		}
		return copyName;
	}

	private boolean hasDeserializeIdentifier(String name) {
		return aMLDeserializeIdentifiers.containsKey(name);
	}

	private AMLDeserializeIdentifier createDeserializeIdentifier(String name, AMLDeserializeIdentifier parentIdentifier, AMLLocationInFile filePosition) {
		String normalizedName = normalizeName(name);

		AMLDeserializeIdentifier identifier = new AMLDeserializeIdentifier(normalizedName, parentIdentifier, filePosition);
		aMLDeserializeIdentifiers.put(normalizedName, identifier);

		AMLDeserializeReferenceIdentifier unresolvedIdentifier = unresolvedIdentifiers.get(normalizedName);
		if (unresolvedIdentifier != null) {
			unresolvedIdentifier.setReferencedIdentifier(identifier);
			unresolvedIdentifiers.remove(normalizedName);
		}

		return identifier;
	}

	private AMLLocationInFile getFilePosition(XMLStreamReader reader) {
		Location location = reader.getLocation();
		if (location == null)
			return new AMLLocationInFileImpl();
		return new AMLLocationInFileImpl(location.getLineNumber(), location.getColumnNumber());
	}

	private void parseElement(XMLStreamReader reader, AMLDeserializeIdentifier identifier, AMLCreateElementInstruction changeElementInstruction)
			throws XMLStreamException, ParserException {
		AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
		if (elementType == null)
			return;

		switch (elementType) {
		case ELEMENT_DESCRIPTION:
			changeElementInstruction.description = reader.getElementText();
			break;
		case ELEMENT_VERSION:
			changeElementInstruction.version = reader.getElementText();
			break;
		case ELEMENT_REVISION:
			parseRevision(reader, changeElementInstruction);
			break;
		case ELEMENT_COPYRIGHT:
			changeElementInstruction.copyright = reader.getElementText();
			break;
		case ELEMENT_ADDITIONAL_INFORMATION:
			changeElementInstruction.additionalInformation = reader.getElementText();
			break;
		default:
			break;
		}
	}

	private void parseRevision(XMLStreamReader reader, AMLCreateElementInstruction instruction) throws XMLStreamException, ParserException {
		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_REVISION);

		Date revisionDate = null;
		String oldVersion = null;
		String newVersion = null;
		String authorName = null;
		String comment = null;

		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			if (elementType == null) {
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_REVISION + ".");
			}

			switch (elementType) {
			case ELEMENT_REVISION_DATE:
				String dateString = reader.getElementText();
				try {
					revisionDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(dateString);
				} catch (ParseException e) {
					try {
						revisionDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateString);
					} catch (ParseException ex) {
					}
				}
				break;
			case ELEMENT_OLD_VERSION:
				oldVersion = reader.getElementText();
				break;
			case ELEMENT_NEW_VERSION:
				newVersion = reader.getElementText();
				break;
			case ELEMENT_AUTHOR_NAME:
				authorName = reader.getElementText();
				break;
			case ELEMENT_COMMENT:
				comment = reader.getElementText();
				break;
			default:
				break;
			}
		}

		instruction.addRevision(revisionDate, oldVersion, newVersion, authorName, comment);

		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_REVISION);
	}

	private void parseAdditionalInformation(XMLStreamReader reader, AMLCreateDocumentInstruction instruction) throws XMLStreamException, ParserException,
			AMLNameAlreadyInUseException {

		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_ADDITIONAL_INFORMATION);

		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {

			AMLElementType elementType = AMLElementNames.getElementType(reader.getLocalName());
			if (elementType == null)
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_ADDITIONAL_INFORMATION + ".");

			switch (elementType) {
			case ELEMENT_WRITER_HEADER:
				while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
					AMLElementType headerElementType = AMLElementNames.getElementType(reader.getLocalName());
					switch (headerElementType) {
					case ELEMENT_WRITER_NAME:
						instruction.writerName = reader.getElementText();
						break;
					case ELEMENT_WRITER_ID:
						instruction.writerID = reader.getElementText();
						break;
					case ELEMENT_WRITER_VENDOR:
						instruction.writerVendor = reader.getElementText();
						break;
					case ELEMENT_WRITER_VENDOR_URL:
						instruction.writerVendorURL = reader.getElementText();
						break;
					case ELEMENT_WRITER_VERSION:
						instruction.writerVersion = reader.getElementText();
						break;
					case ELEMENT_WRITER_RELEASE:
						instruction.writerRelease = reader.getElementText();
						break;
					case ELEMENT_LAST_WRITING_DATE_TIME:
						String dateString = reader.getElementText();
						Date date;
						try {
							date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(dateString);
						} catch (ParseException e) {
							try {
								date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateString);
							} catch (ParseException ex) {
								date = new Date();
							}
						}
						instruction.lastWritingDate = date;
						break;
					case ELEMENT_WRITER_PROJECT_TITLE:
						instruction.writerProjectTitle = reader.getElementText();
						break;
					case ELEMENT_WRITER_PROJECT_ID:
						instruction.writerProjectID = reader.getElementText();
						break;
					default:
						break;
					}
				}
				break;

			default:
				throw new ParserException("Unexpected element " + reader.getLocalName() + " in " + AMLElementNames.ELEMENT_ADDITIONAL_INFORMATION + ".");

			}
		}

		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_ADDITIONAL_INFORMATION);
	}

	private void parseAMLVersion(XMLStreamReader reader) throws XMLStreamException, ParserException {

		reader.require(XMLStreamConstants.START_ELEMENT, null, AMLElementNames.ELEMENT_ADDITIONAL_INFORMATION);

		if (!reader.getAttributeValue(0).equals(AMLElementNames.ATTRIBUTE_VALUE_AUTOMATION_ML_VERSION))
			throw new ParserException("Unsupported version");

		reader.nextTag();
		reader.require(XMLStreamConstants.END_ELEMENT, null, AMLElementNames.ELEMENT_ADDITIONAL_INFORMATION);
	}

	private void addInstruction(AMLDeserializeIdentifier identifier, AMLInstruction aMLInstruction, boolean library) throws AMLNameAlreadyInUseException {
		if (aMLInstruction instanceof AMLCreateInstruction) {
			if (library) {
				if (identifierToCreateLibraryInstruction.containsKey(identifier)) {
					return;
				}
				identifierToCreateLibraryInstruction.put(identifier, (AMLCreateInstruction) aMLInstruction);
			} else { 
				if (identifierToCreateInstruction.containsKey(identifier)) {
					return;
				}
				identifierToCreateInstruction.put(identifier, (AMLCreateInstruction) aMLInstruction);
			}
		} else {
			changeInstructions.add(aMLInstruction);
		}
	}

	private void addChangeInstruction(AMLInstruction aMLInstruction) {
		changeInstructions.add(aMLInstruction);
	}

}
