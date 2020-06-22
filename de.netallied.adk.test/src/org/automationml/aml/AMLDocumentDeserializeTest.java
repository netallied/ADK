/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.automationml.DocumentURLResolver;
import org.automationml.URLDocumentLocation;
import org.automationml.aml.AMLElement.Revision;
import org.automationml.internal.aml.persistence.AMLUnresolvedDependenciesException;
import org.junit.Ignore;
import org.junit.Test;

public class AMLDocumentDeserializeTest extends AbstractAMLTest {

	private TestFileLocator testFileLocator = new TestFileLocator(getClass());

	@Override
	public void createSession() throws Exception {
		super.createSession();
	}

	@Ignore
	@Test
	public void invalid_01_missingExternalReference() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("01_missingExternalReference.aml");
		URL url = file.toURI().toURL();
		try {
			session.loadAMLDocument(url);
			fail();
		} catch (Exception e) {
		}
		assertSessionHasNoDocuments();
		assertNoChangesNotified();
	}

	@Ignore
	@Test
	public void invalid_02_missingExternalReference() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("02_missingExternalReference.aml");
		URL url = file.toURI().toURL();
		try {
			session.loadAMLDocument(url);
			fail();
		} catch (Exception e) {
		}
		assertSessionHasNoDocuments();
		assertNoChangesNotified();
	}

	@Ignore
	@Test
	public void invalid_03_unresolvedInterfaceClassLib() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("03_unresolvedInterfaceClassLib.aml");
		URL url = file.toURI().toURL();
		try {
			session.loadAMLDocument(url);
			fail();
			// } catch (AMLForbiddenReferenceException e) {
		} catch (AMLUnresolvedDependenciesException e) {
		}
		assertSessionHasNoDocuments();
		assertNoChangesNotified();
	}

	@Ignore
	@Test
	public void invalid_04_duplicateInterfaceClassLibNameInDocument() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("04_duplicateInterfaceClassLibNameInDocument.aml");
		URL url = file.toURI().toURL();
		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLValidationException e) {
		}
		assertSessionHasNoDocuments();
		assertNoChangesNotified();
	}

	@Ignore
	@Test
	public void invalid_05_duplicateInterfaceClassLibNameInReferencedDocument() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("05_duplicateInterfaceClassLibNameInReferencedDocument.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLValidationException e) {
		}
		assertSessionHasNoDocuments();
		assertNoChangesNotified();
	}

	@Ignore
	@Test
	public void invalid_duplicateInterfaceClassLibName_incremental_loading() throws Exception {
		File file1 = testFileLocator.getInvalidTestCaseFile("b.aml");
		URL url1 = file1.toURI().toURL();
		session.loadAMLDocument(url1);

		assertChangesNotified();
		resetChangeListener();

		File file2 = testFileLocator.getInvalidTestCaseFile("05_duplicateInterfaceClassLibNameInReferencedDocument.aml");
		URL url2 = file2.toURI().toURL();
		try {
			session.loadAMLDocument(url2);
			fail();
		} catch (AMLValidationException e) {
		}
		assertThat(session.getDocuments()).hasSize(1);
		assertNoChangesNotified();
	}

	@Test(expected = FileNotFoundException.class)
	public void loadAMLDocument_URLNotResolved() throws Exception {
		URL url = new URL("file:///xxx.aml");
		session.loadAMLDocument(url);
		assertSessionHasNoDocuments();
		assertNoChangesNotified();
	}

	@Ignore
	@Test
	public void invalid_07_cycleInExternalReferences() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("07_cycleInExternalReferences.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLDocumentScopeInvalidException e) {
		}
		assertSessionHasNoDocuments();
		assertNoChangesNotified();
	}

	@Test
	public void valid_02_missingExternalReference_resolvedByURLResolver() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("02_missingExternalReference.aml");
		URL url = file.toURI().toURL();

		File validFile = testFileLocator.getValidTestCaseFile("b.aml");
		final URL replacementUrl = validFile.toURI().toURL();

		DocumentURLResolver documentURLResolver = new DocumentURLResolver() {
			@Override
			public URL getResolvedURL(URL baseUrl, String pathString) {
				URL url;
				try {
					url = new URL(baseUrl, pathString);
					if (url.getPath().endsWith("Lib/b.aml"))
						return replacementUrl;
					return url;
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		};

		session.setDocumentURLResolver(documentURLResolver);
		session.loadAMLDocument(url);

		assertSessionHasDocuments(3);
		assertChangesNotified();
	}

	@Test
	public void valid_01_explicitExternalReference() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("01_explicitExternalReference.aml");
		URL url = file.toURI().toURL();

		AMLDocument document = session.loadAMLDocument(url);
		assertThat(session.getAMLDocumentByDocumentLocation(new URLDocumentLocation(url))).isSameAs(document);

		assertSessionHasDocuments(2);

		assertThat(document.getExplicitlyReferencedDocuments()).hasSize(1);

		File file2 = testFileLocator.getValidTestCaseFile("b.aml");
		URL url2 = file2.toURI().toURL();
		assertThat(session.getAMLDocumentByDocumentLocation(new URLDocumentLocation(url2))).isNotNull();

		assertChangesNotified();
	}

	@Test
	public void valid_02_InterfaceClassLib() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("02_InterfaceClassLib.aml");
		URL url = file.toURI().toURL();

		AMLDocument document = session.loadAMLDocument(url);
		AMLInterfaceClassLibrary interfaceClassLibrary = document.getInterfaceClassLibrary("LIB1");
		assertThat(interfaceClassLibrary).isNotNull();

		assertSessionHasDocuments(2);
		assertChangesNotified();
	}

	@Test
	public void valid_loadAMLDocumentTwoTimes() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("b.aml");
		URL url = file.toURI().toURL();
		AMLDocument doc1 = session.loadAMLDocument(url);
		AMLDocument doc2 = session.loadAMLDocument(url);

		assertThat(doc1).isSameAs(doc2);

		assertSessionHasDocuments(1);
		assertChangesNotified();
	}

	@Test
	public void valid_03_explicitExternalReference_diamond() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("03_explicitExternalReference_diamond.aml");
		URL url = file.toURI().toURL();
		session.loadAMLDocument(url);

		assertSessionHasDocuments(4);
		assertChangesNotified();
	}

	@Test
	public void valid_04_InterfaceClassesContainingInterfaceClasses() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("04_InterfaceClassesContainingInterfaceClasses.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		assertThat(a.getInterfaceClassByPath("LIB1/Class1/Class2/Class3")).isNotNull();
		assertThat(a.getInterfaceClassByPath("LIB1/Class1/Class2/Class4")).isNotNull();
		assertThat(a.getInterfaceClassByPath("LIB1/Class1/Class3/Class4")).isNotNull();
		assertThat(a.getInterfaceClassByPath("LIB1/Class1/Class3/Class5")).isNotNull();

		assertSessionHasDocuments(2);
		assertChangesNotified();
	}

	@Test
	public void valid_interfaceClassesOrder() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("b.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		AMLInterfaceClassLibrary lib = a.getInterfaceClassLibrary("LIB2");

		assertThat(getNamesOfInterfaceClasses(lib)).isEqualTo(toList("Class1", "Class2", "Class3", "Class4", "Class5", "Class6"));
	}

	@Ignore
	@Test
	public void invalid_08_unresolvedRoleClassLib() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("08_unresolvedRoleClassLib.aml");
		URL url = file.toURI().toURL();

		Exception caughtException = null;

		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLForbiddenReferenceException e) {
			caughtException = e;
		} catch (Exception e) {
			caughtException = e;
		}

		assertSessionHasNoDocuments();
		assertNoChangesNotified();

		assertThat(caughtException).isNotNull();
		assertThat(caughtException).isInstanceOf(AMLValidationException.class);
	}

	@Ignore
	@Test
	public void invalid_09_unresolvedSystemUnitClassLib() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("09_unresolvedSystemUnitClassLib.aml");
		URL url = file.toURI().toURL();

		Exception caughtException = null;

		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLForbiddenReferenceException e) {
			caughtException = e;
		} catch (Exception e) {
			caughtException = e;
		}

		assertSessionHasNoDocuments();
		assertNoChangesNotified();

		assertThat(caughtException).isNotNull();
		assertThat(caughtException).isInstanceOf(AMLUnresolvedDependenciesException.class);
	}

	@Ignore
	@Test
	public void invalid_10_duplicateRoleClassLibNameInDocument() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("10_duplicateRoleClassLibNameInDocument.aml");
		URL url = file.toURI().toURL();

		Exception caughtException = null;

		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLValidationException e) {
			caughtException = e;
		} catch (Exception e) {
			caughtException = e;
		}

		assertSessionHasNoDocuments();
		assertNoChangesNotified();

		assertThat(caughtException).isNotNull();
		assertThat(caughtException).isInstanceOf(AMLValidationException.class);
	}

	@Ignore
	@Test
	public void invalid_11_duplicateSystemUnitClassLibNameInDocument() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("11_duplicateSystemUnitClassLibNameInDocument.aml");
		URL url = file.toURI().toURL();

		Exception caughtException = null;

		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLValidationException e) {
			caughtException = e;
		} catch (Exception e) {
			caughtException = e;
		}

		assertSessionHasNoDocuments();
		assertNoChangesNotified();

		assertThat(caughtException).isNotNull();
		assertThat(caughtException).isInstanceOf(AMLValidationException.class);
	}

	@Ignore
	@Test
	public void invalid_12_duplicateRoleClassLibNameInReferencedDocument() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("12_duplicateRoleClassLibNameInReferencedDocument.aml");
		URL url = file.toURI().toURL();

		Exception caughtException = null;

		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLDocumentScopeInvalidException e) {
			caughtException = e;
		} catch (Exception e) {
			caughtException = e;
		}

		assertSessionHasNoDocuments();
		assertNoChangesNotified();

		assertThat(caughtException).isNotNull();
		assertThat(caughtException).isInstanceOf(AMLValidationException.class);
	}

	@Ignore
	@Test
	public void invalid_13_duplicateSystemUnitClassLibNameInReferencedDocument() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("13_duplicateSystemUnitClassLibNameInReferencedDocument.aml");
		URL url = file.toURI().toURL();

		Exception caughtException = null;

		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLUnresolvedDependenciesException e) {
			caughtException = e;
		} catch (AMLDocumentScopeInvalidException e) {
			caughtException = e;
		} catch (Exception e) {
			caughtException = e;
		}

		assertSessionHasNoDocuments();
		assertNoChangesNotified();

		assertThat(caughtException).isNotNull();
		assertThat(caughtException).isInstanceOf(AMLValidationException.class);
	}

	@Ignore
	@Test
	public void invalid_14_RoleClass_referencing_external_interface_library() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("14_RoleClass_referencing_external_interface_library.aml");
		URL url = file.toURI().toURL();

		Exception caughtException = null;

		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLForbiddenReferenceException e) {
			caughtException = e;
		}

		assertSessionHasNoDocuments();
		assertNoChangesNotified();

		assertThat(caughtException).isNotNull();
		assertThat(caughtException).isInstanceOf(AMLForbiddenReferenceException.class);
	}

	@Test
	public void valid_05_RoleClassLib() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("05_RoleClassLib.aml");
		URL url = file.toURI().toURL();

		AMLDocument document = session.loadAMLDocument(url);
		AMLRoleClassLibrary roleClassLibrary = document.getRoleClassLibrary("LIB1");
		assertThat(roleClassLibrary).isNotNull();

		assertSessionHasDocuments(2);
		assertChangesNotified();
	}

	@Test
	public void valid_06_SystemUnitClassLib() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("06_SystemUnitClassLib.aml");
		URL url = file.toURI().toURL();

		AMLDocument document = session.loadAMLDocument(url);
		AMLSystemUnitClassLibrary systemUnitClassLibrary = document.getSystemUnitClassLibrary("LIB1");
		assertThat(systemUnitClassLibrary).isNotNull();

		assertSessionHasDocuments(2);
		assertChangesNotified();
	}

	@Test
	public void valid_07_RoleClassesContainingRoleClasses() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("07_RoleClassesContainingRoleClasses.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		assertThat(a.getRoleClassByPath("LIB1/Class1/Class2/Class3")).isNotNull();
		assertThat(a.getRoleClassByPath("LIB1/Class1/Class2/Class4")).isNotNull();
		assertThat(a.getRoleClassByPath("LIB1/Class1/Class3/Class4")).isNotNull();
		assertThat(a.getRoleClassByPath("LIB1/Class1/Class3/Class5")).isNotNull();

		assertSessionHasDocuments(2);
		assertChangesNotified();
	}

	@Test
	public void valid_08_SystemUnitClassesContainingSystemUnitClasses() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("08_SystemUnitClassesContainingSystemUnitClasses.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		assertThat(a.getSystemUnitClassByPath("LIB1/Class1/Class2/Class3")).isNotNull();
		assertThat(a.getSystemUnitClassByPath("LIB1/Class1/Class2/Class4")).isNotNull();
		assertThat(a.getSystemUnitClassByPath("LIB1/Class1/Class3/Class4")).isNotNull();
		assertThat(a.getSystemUnitClassByPath("LIB1/Class1/Class3/Class5")).isNotNull();

		assertSessionHasDocuments(2);
		assertChangesNotified();
	}

	@Test
	public void valid_05_interfaceClassWithAttributes() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("05_interfaceClassWithAttributes.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		AMLInterfaceClassLibrary lib = a.getInterfaceClassLibrary("Lib");
		AMLInterfaceClass interfaceClass = lib.getInterfaceClass("Class");
		AMLAttribute attribute = interfaceClass.getAttribute("attribute");
		assertThat(attribute.getValue().equals("value"));
		assertThat(attribute.getDefaultValue().equals("default value"));
		assertThat(attribute.getDataType().equals("dataType"));
		assertThat(attribute.getDescription().equals("description"));
		assertThat(attribute.getUnit().equals("unit"));
	}

	@Test
	public void valid_05_interfaceClassWithAttributeHierarchy() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("05_interfaceClassWithAttributeHierarchy.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		AMLInterfaceClassLibrary lib = a.getInterfaceClassLibrary("Lib");
		AMLInterfaceClass interfaceClass = lib.getInterfaceClass("Class");
		AMLAttribute attribute = interfaceClass.getAttribute("attribute");
		AMLAttribute attribute2 = attribute.getAttribute("attribute");
		assertThat(attribute2).isNotNull();
	}

	@Test
	public void invalid_08_interfaceClassWithTwoSameAttributes() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("08_interfaceClassWithTwoSameAttributes.aml");
		URL url = file.toURI().toURL();
		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLValidationException e) {
		} catch (Exception ex) {
			// fail();
		}

	}

	@Test
	public void valid_09_instanceHierarchyWithInternalElements() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("09_instanceHierarchyWithInternalElements.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		AMLInstanceHierarchy instanceHierarchy = a.getInstanceHierarchy("InstanceHierarchy");
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();
		assertThat(iterator.hasNext());
		AMLInternalElement internalElement = iterator.next();
		assertThat(internalElement).isNotNull();
		assertThat(internalElement.getName().equals("InternalElement"));
		assertThat(internalElement.getId().toString().equals("534f4b37-b973-43da-b85c-b7fef52895fa"));
		assertThat(iterator.hasNext()).isFalse();

		Iterator<AMLInternalElement> iterator2 = internalElement.getInternalElements().iterator();
		assertThat(iterator2.hasNext());
		AMLInternalElement internalElement2 = iterator2.next();
		assertThat(internalElement2).isNotNull();
		assertThat(internalElement2.getName().equals("InternalElement2"));
		assertThat(internalElement2.getId().toString().equals("44d76aac-0fce-46fa-a813-90ffdc27a5f5"));
		assertThat(iterator2.hasNext()).isFalse();
	}

	@Test
	public void valid_09_systemUnitClassWithInternalElements() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("09_systemUnitClassWithInternalElements.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.getSystemUnitClassLibrary("LIB1");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.getSystemUnitClass("Class1");
		Iterator<AMLInternalElement> iterator = systemUnitClass.getInternalElements().iterator();
		assertThat(iterator.hasNext());
		AMLInternalElement internalElement = iterator.next();
		assertThat(internalElement).isNotNull();
		assertThat(internalElement.getName().equals("InternalElement"));
		assertThat(internalElement.getId().toString().equals("534f4b37-b973-43da-b85c-b7fef52895fa"));
		assertThat(iterator.hasNext()).isFalse();

		Iterator<AMLInternalElement> iterator2 = internalElement.getInternalElements().iterator();
		assertThat(iterator2.hasNext());
		AMLInternalElement internalElement2 = iterator2.next();
		assertThat(internalElement2).isNotNull();
		assertThat(internalElement2.getName().equals("InternalElement2"));
		assertThat(internalElement2.getId().toString().equals("44d76aac-0fce-46fa-a813-90ffdc27a5f5"));
		assertThat(iterator2.hasNext()).isFalse();
	}

	@Test
	public void valid_09_internalElementWithBaseClass() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("09_internalElementWithBaseClass.aml");
		URL url = file.toURI().toURL();
		AMLDocument a = session.loadAMLDocument(url);

		AMLSystemUnitClassLibrary systemUnitClassLibrary = a.getSystemUnitClassLibrary("LIB1");
		AMLSystemUnitClass systemUnitClass = systemUnitClassLibrary.getSystemUnitClass("Class1");

		AMLInstanceHierarchy instanceHierarchy = a.getInstanceHierarchy("InstanceHierarchy");
		Iterator<AMLInternalElement> iterator = instanceHierarchy.getInternalElements().iterator();

		assertThat(iterator.hasNext());
		AMLInternalElement internalElement = iterator.next();
		assertThat(internalElement).isNotNull();
		assertThat(internalElement.getName().equals("InternalElement"));
		assertThat(internalElement.getId().toString().equals("534f4b37-b973-43da-b85c-b7fef52895fa"));
		assertThat(internalElement.getBaseSystemUnitClass().equals(systemUnitClass));
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void invalid_15_instanceHierarchyWithInternalElementsWithSameId() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("15_instanceHierarchyWithInternalElementsWithSameId.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLValidationException e) {
		}
	}

	@Test
	public void invalid_16_internalElementWithCircularReference() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("16_internalElementWithCircularReference.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
			fail();
		} catch (AMLValidationException e) {
		}
	}

	@Test
	public void valid_10_internalElementWithInterface() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("10_internalElementWithInterface.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void valid_20_internalElementSupportedRoleClassWithAttributeMapping() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("20_internalElementSupportedRoleClassWithAttributeMapping.aml");
		URL url = file.toURI().toURL();

		session.loadAMLDocument(url);
	}
	
	@Test
	public void valid_20_internalElementRoleRequirement() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("20_internalElementRoleRequirement.aml");
		URL url = file.toURI().toURL();

		session.loadAMLDocument(url);
	}

	@Test
	public void valid_21_internalElementWithInterfaceWithAttributes() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("21_internalElementWithInterfaceWithAttributes.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
			fail();
		}
	}

	@Ignore
	@Test
	public void valid_11_systemUnitClassWithSupportedRoleClass() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("11_systemUnitClassWithSupportedRoleClass.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void valid_18_internalElementSupportedRoleClass() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("18_internalElementSupportedRoleClass.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void valid_11_internalElementRoleRequirement() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("11_internalElementRoleRequirement.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void valid_25_internalElementWithInterfaceAndAttributes() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("25_internalElementWithInterfaceAndAttributes.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void valid_26_internalElementWithInterfaceAndAttributes() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("26_internalElementWithInterfaceAndAttributes.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void valid_27_internalElementWithInterfaceAndAttributes() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("27_internalElementWithInterfaceAndAttributes.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
			fail();
		}
	}
	
	@Ignore
	@Test (expected = AMLValidationException.class)
	public void invalid_17_internalElementWithInterfaceAndAttributes() throws Exception {
		File file = testFileLocator.getInvalidTestCaseFile("17_internalElementWithInterfaceAndAttributes.aml");
		URL url = file.toURI().toURL();

		session.loadAMLDocument(url);
	}
	
	@Test
	public void valid_24_internalElementWithLink() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("24_internalElementWithLink.aml");
		URL url = file.toURI().toURL();

		try {
			session.loadAMLDocument(url);
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void valid_28_revision() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("28_revision.aml");
		URL url = file.toURI().toURL();

		try {
			AMLDocument document = session.loadAMLDocument(url);
			Iterable<Revision> revisions = document.getRevisions();
			assertThat(revisions).isNotEmpty();
		} catch (AMLValidationException e) {
			fail();
		} catch (Exception e) {
			fail();
		}
	}

	@Ignore // siehe TODO in AMLDeserializer#parseClass
	@Test
	public void valid_32_revision() throws Exception {
		File file = testFileLocator.getValidTestCaseFile("32_InterfaceClass_RefBaseClassPath.aml");
		URL url = file.toURI().toURL();

		AMLDocument document = session.loadAMLDocument(url);
		
		AMLInterfaceClass class1 = document.getInterfaceClassByPath("LIB1/Class1");
		AMLInterfaceClass class1_1 = document.getInterfaceClassByPath("LIB1/Class1/Class1");
		AMLInterfaceClass class1_2 = document.getInterfaceClassByPath("LIB1/Class1/Class2");
		AMLInterfaceClass class1_3 = document.getInterfaceClassByPath("LIB1/Class1/Class3");
		AMLInterfaceClass class1_4 = document.getInterfaceClassByPath("LIB1/Class1/Class4");
		
		assertThat(class1.getBaseInterfaceClass()).isNull();
		assertThat(class1_1.getBaseInterfaceClass()).isNull();
		assertThat(class1_2.getBaseInterfaceClass()).isSameAs(class1_1);
		assertThat(class1_3.getBaseInterfaceClass()).isSameAs(class1);
		assertThat(class1_4.getBaseInterfaceClass()).isSameAs(class1_1);
	}
	
	@Test
	public void valid_references() throws Exception {
		
//		AMLDocument document = session.createAMLDocument();
//		
		File fileInterfaces = testFileLocator.getValidTestCaseFile("InterfaceClassLibraries/AutomationMLInterfaceClassLib.aml");
		URL url = fileInterfaces.toURI().toURL();
		AMLDocument documentIF = session.loadAMLDocument(url);
		
		File fileRoles = testFileLocator.getValidTestCaseFile("RoleClassLibraries/AutomationMLBaseRoleClassLib.aml");
		url = fileRoles.toURI().toURL();
		AMLDocument documentRole = session.loadAMLDocument(url);
//		
//		document.addExplicitExternalReference(documentIF);
//		document.addExplicitExternalReference(documentRole);
//		
		
	}
	

//	@Test
//	public void load() throws Exception {
//		URL url = new URL("file:///C:/Temp/aml/Bsp_AML_Export_E5_v06.aml");
//
//		AMLDocument document = session.loadAMLDocument(url);
//		assertThat(document).isNotNull();
//	}
}
