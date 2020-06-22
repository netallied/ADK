/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.automationml.DocumentLocation;
import org.automationml.URLDocumentLocation;
import org.automationml.internal.aml.AMLDocumentManager.MemoryDocumentLocation;
import org.automationml.internal.aml.persistence.AMLSerializer;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;

import static org.fest.assertions.Assertions.assertThat;

@Ignore
public class AMLDocumentSerializeTest extends AbstractAMLTest {

	static {
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setIgnoreAttributeOrder(true);
	}

	private static class IgnoreVariableAttributesDifferenceListener implements DifferenceListener {

		@Override
		public int differenceFound(Difference difference) {
			switch (difference.getId()) {
			case DifferenceConstants.TEXT_VALUE_ID:
				if (difference.getControlNodeDetail().getXpathLocation().contains("LastWritingDateTime"))
					return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
				break;
			case DifferenceConstants.ATTR_VALUE_ID:
				if ("FileName".equals(difference.getControlNodeDetail().getNode().getNodeName()))
					return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
				if ("Alias".equals(difference.getControlNodeDetail().getNode().getNodeName()))
					return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
				//					return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
				break;
			}
			return RETURN_ACCEPT_DIFFERENCE;
		}

		@Override
		public void skippedComparison(Node control, Node test) {
			// nothing to do
		}
	}

	private static DifferenceListener differenceListener = new IgnoreVariableAttributesDifferenceListener();

	private class AMLTestUrlResolver implements AMLDocumentURLResolver {
		@Override
		public boolean isRelative(AMLDocument baseDocument, AMLDocument referencedDocument) {
			return true;
		}

		@Override
		public URL getUrl(AMLDocument document) {
			DocumentLocation documentLocation = document.getDocumentLocation();
			if (documentLocation instanceof URLDocumentLocation) {
				URL url = ((URLDocumentLocation) documentLocation).getUrl();
				return url;
			}
			if (documentLocation instanceof MemoryDocumentLocation) {
				//				((MemoryDocumentLocation)documentLocation).getUrl();
				//				new StringWriter().getBuffer().append(

			}
			return null;
		}
	}

	private TestFileLocator testFileLocator = new TestFileLocator(getClass());
	private XMLTestCase xmlTestCase = new XMLTestCase() {
	};

	private AMLTestUrlResolver urlResolver = new AMLTestUrlResolver();

	private static Collection<String> ignoredFileNames = new HashSet<String>();
	static {
		// these are expected to fail:
		ignoredFileNames.add("11_internalElementRoleRequirement.aml");
		ignoredFileNames.add("16_internalElementRoleRequirementWithAttribute.aml");
		ignoredFileNames.add("17_internalElementRoleRequirementWithAttributeMapping.aml");
		ignoredFileNames.add("29_frameAttribute.aml"); // int -> double

		// TODO make these running ok:
		ignoredFileNames.add("20_internalElementSupportedRoleClassWithAttributeMapping.aml"); // http://intranet:8000/adk/ticket/9#ticket
		ignoredFileNames.add("23_externalHTTPUrl.aml"); // file://b.aml nicht gefunden 
		ignoredFileNames.add("24_internalElementWithLink.aml"); // andere IDs in InternalLink RefPartnerSideA/B
		ignoredFileNames.add("27_internalElementWithInterfaceAndAttributes.aml"); // alias
		ignoredFileNames.add("30_mirror.aml"); // alias
		ignoredFileNames.add("31_facet.aml"); // alias
	}

	@Test
	public void testAll() throws Exception {
		File sourceDirectory = testFileLocator.getValidTestCaseFile(".").getCanonicalFile();

		File[] files = sourceDirectory.listFiles();
		for (File inputFile : files) {
			if (inputFile.isDirectory())
				continue;
			if( !inputFile.getName().toLowerCase().endsWith("aml"))
				continue;
			if (ignoredFileNames.contains(inputFile.getName())) {
				debugPrint("skipped " + inputFile);
				continue;
			}
			validate(inputFile.getCanonicalFile());
		}
	}

//	@Test
//	public void valid_01_explicitExternalReference() throws Exception {
//		File source = testFileLocator.getValidTestCaseFile("01_explicitExternalReference.aml");
//		validate(source);
//	}
//
//	@Test
//	public void valid_02_InterfaceClassLib() throws Exception {
//		File source = testFileLocator.getValidTestCaseFile("02_InterfaceClassLib.aml");
//		validate(source);
//	}
//
//	@Test
//	public void valid_05_interfaceClassWithAttributeHierarchy() throws Exception {
//		File source = testFileLocator.getValidTestCaseFile("05_interfaceClassWithAttributeHierarchy.aml");
//		validate(source);
//	}
//
//	@Test
//	public void valid_11_internalElementRoleRequirement() throws Exception {
//		File source = testFileLocator.getValidTestCaseFile("11_internalElementRoleRequirement.aml");
//		validate(source);
//	}
//	
//	@Test
//	public void valid_20_internalElementSupportedRoleClassWithAttributeMapping() throws Exception {
//		File source = testFileLocator.getValidTestCaseFile("20_internalElementSupportedRoleClassWithAttributeMapping.aml");
//		validate(source);
//	}

//	@Test
//	public void valid_23_externalHTTPUrl() throws Exception {
//		File source = testFileLocator.getValidTestCaseFile("23_externalHTTPUrl.aml");
//		validate(source);
//	}
	
	@Ignore
	@Test
	public void valid_24_internalElementWithLink() throws Exception {
		File source = testFileLocator.getValidTestCaseFile("24_internalElementWithLink.aml");
		validate(source);
	}

//	@Test
//	public void valid_27_internalElementWithInterfaceAndAttributes() throws Exception {
//		File source = testFileLocator.getValidTestCaseFile("27_internalElementWithInterfaceAndAttributes.aml");
//		validate(source);
//	}
//	
//	@Test
//	public void valid_28_Revision() throws Exception {
//		File source = testFileLocator.getValidTestCaseFile("28_Revision.aml");
//		validate(source);
//	}
//
//	@Test
//	public void valid_30_mirror() throws Exception {
//		File source = testFileLocator.getValidTestCaseFile("30_mirror.aml");
//		validate(source);
//	}
	
	private void validate(File source) throws Exception {
		createSession();

		debugPrint(source.toString());

		URL sourceUrl = source.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(sourceUrl);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		AMLSerializer.serialize(a, urlResolver, outputStream);
		
		String actualString = outputStream.toString("UTF-8");

		Diff diff = xmlTestCase.compareXML(new FileReader(source), new StringReader(actualString));
		diff.overrideDifferenceListener(differenceListener);
		if (!diff.similar()) {
			actualString = prettyPrintXML(new StringReader(actualString));
			String expectedString = prettyPrintXML(new FileReader(source));
			assertThat(actualString).describedAs(source.toString()).isEqualTo(expectedString);
		}

		assertSessionClean();
	}

	private static void debugPrint(String text) {
//		System.out.println(text);
	}

	static String prettyPrintXML(Reader inputReader) throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer(); // An identity transformer
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		Source xmlInput = new StreamSource(inputReader);
		StringWriter stringWriter = new StringWriter();
		StreamResult xmlOutput = new StreamResult(stringWriter);
		transformer.transform(xmlInput, xmlOutput);
		return stringWriter.toString();
	}

}
