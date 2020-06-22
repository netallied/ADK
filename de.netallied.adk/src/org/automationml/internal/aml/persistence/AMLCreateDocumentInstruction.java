/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import java.net.URL;
import java.util.Date;

import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLSession;
import org.automationml.aml.AMLValidationException;
import org.automationml.internal.aml.AMLElementNames;
import org.automationml.internal.aml.AMLElementType;

public class AMLCreateDocumentInstruction extends AMLCreateElementInstruction {

	public String writerName;
	public String writerID;
	public String writerVendor;
	public String writerVendorURL;
	public String writerVersion;
	public String writerRelease;
	public String writerProjectTitle;
	public String writerProjectID;
	public Date lastWritingDate;
	
	protected AMLCreateDocumentInstruction(AMLSession session, AMLDeserializeIdentifier selfIdentifier) {
		super(session, selfIdentifier, null, AMLElementType.ELEMENT_CAEX_FILE);
	}

	@Override
	public void execute() throws Exception {
		AMLDocument document;
		if (selfIdentifier == null) {
			document = session.createAMLDocument();
		} else {
			URL url = new URL(selfIdentifier.getName());
			document = session.createAMLDocument(url);
			selfIdentifier.setResolvedElement(document);
		}
		document.setWriterName(writerName);
		document.setWriterID(writerID);
		document.setWriterVendor(writerVendor);
		document.setWriterVendorURL(writerVendorURL);
		document.setWriterVersion(writerVersion);
		document.setWriterRelease(writerRelease);
		document.setLastWritingDate(lastWritingDate);
		document.setWriterProjectTitle(writerProjectTitle);
		document.setWriterProjectID(writerProjectID);
		
		setElementProperties(document);
	}

	@Override
	public AMLDeserializeIdentifier getParentIdentifier() {
		return null;
	}
	
	@Override
	public boolean hasUnresolvedDependencies() {
		return false;
	}
}
