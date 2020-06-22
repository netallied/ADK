/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.net.URL;
import java.util.Date;

import org.automationml.DocumentLocation;

public interface AMLDocument extends AMLDocumentElement, AMLElement {

	AMLInstanceHierarchy createInstanceHierarchy(String name) throws AMLValidationException;
	
	AMLValidationResultList validateCreateInstanceHierarchy(String name) throws AMLValidationException;
	
	AMLValidationResultList validateCreateInterfaceClassLibrary(String name) throws AMLValidationException;

	AMLInterfaceClassLibrary createInterfaceClassLibrary(String name) throws AMLValidationException;

	AMLRoleClassLibrary createRoleClassLibrary(String name) throws AMLValidationException;

	AMLSystemUnitClassLibrary createSystemUnitClassLibrary(String name) throws AMLValidationException;

	AMLInstanceHierarchy getInstanceHierarchy(String name);
	
	AMLInterfaceClassLibrary getInterfaceClassLibrary(String libraryName);

	AMLRoleClassLibrary getRoleClassLibrary(String libraryName);

	AMLSystemUnitClassLibrary getSystemUnitClassLibrary(String libraryName);

	AMLInterfaceClass getInterfaceClassByPath(String path) throws AMLValidationException;

	AMLRoleClass getRoleClassByPath(String path);

	AMLSystemUnitClass getSystemUnitClassByPath(String path) throws AMLValidationException;

	void addExplicitExternalReference(AMLDocument referencedDocument) throws AMLValidationException;

	AMLValidationResultList validateRemoveExplicitExternalReference(AMLDocument referencedDocument);
	
	void removeExplicitExternalReference(AMLDocument referencedDocument) throws AMLValidationException;

	Iterable<AMLDocument> getExplicitlyReferencedDocuments();
	
	Iterable<AMLInstanceHierarchy> getInstanceHierarchies();

	int getInstanceHierarchiesCount();

	Iterable<AMLInterfaceClassLibrary> getInterfaceClassLibraries();

	int getInterfaceClassLibrariesCount();

	Iterable<AMLRoleClassLibrary> getRoleClassLibraries();

	int getRoleClassLibrariesCount();

	Iterable<AMLSystemUnitClassLibrary> getSystemUnitClassLibraries();

	int getSystemUnitClassLibrariesCount();

	boolean isDirty();
	
	void setDirty();

	void unsetDirty();

	void addDocumentChangeListener(AMLDocumentChangeListener changeListener);

	void removeDocumentChangeListener(AMLDocumentChangeListener changeListener);
	
	DocumentLocation getDocumentLocation();

	Iterable<AMLDocument> getReferencedDocuments();

	int getReferencedDocumentsCount();

	void setWriterProjectID(String string);

	void setWriterProjectTitle(String string);

	void setLastWritingDate(Date date);

	void setWriterRelease(String string);

	void setWriterVersion(String string);

	void setWriterVendorURL(String string);

	void setWriterID(String string);

	void setWriterVendor(String writerVendor);

	void setWriterName(String writerName);
	
	public String getWriterName();

	public String getWriterID();

	public String getWriterVendor();

	public String getWriterVendorURL();

	public String getWriterVersion();

	public String getWriterRelease();

	public Date getLastWritingDate();

	public String getWriterProjectTitle();

	public String getWriterProjectID();

	AMLValidationResultList validateCreateRoleClassLibrary(String name) throws AMLValidationException;

	AMLValidationResultList validateCreateSystemUnitClassLibrary(String name) throws AMLValidationException;

	void saveAs(URL newUrl, AMLDocumentURLResolver urlResolver) throws Exception;

	void save(AMLDocumentURLResolver urlResolver) throws Exception;

	AMLInstanceHierarchy createInstanceHierarchy(String name, AMLInstanceHierarchy instanceHierarchy) throws AMLValidationException;

	AMLInterfaceClassLibrary createInterfaceClassLibrary(String name,	AMLInterfaceClassLibrary interfaceClassLibrary) throws AMLValidationException;

	AMLRoleClassLibrary createRoleClassLibrary(String name,	AMLRoleClassLibrary roleClassLibrary) throws AMLValidationException;

	AMLSystemUnitClassLibrary createSystemUnitClassLibrary(String name, AMLSystemUnitClassLibrary systemUnitClassLibrary) throws AMLValidationException;
}
