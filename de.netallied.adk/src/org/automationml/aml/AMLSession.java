/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.net.URL;

import org.automationml.DocumentLocation;
import org.automationml.DocumentURLResolver;
import org.automationml.Savepoint;
import org.automationml.internal.ProgressMonitor;

public interface AMLSession extends AMLObject {

	AMLDocument createAMLDocument();

	AMLDocument getAMLDocumentByDocumentLocation(DocumentLocation documentLocation) throws Exception;

	AMLDocument createAMLDocument(URL url);

	AMLDocument loadAMLDocument(URL url) throws Exception;
	
	AMLDocument getRootDocument(AMLDocument documentInScope);
	
	int getDocumentsCount();

	Iterable<AMLDocument> getDocuments();

	Savepoint createSavepoint();

	boolean hasCurrentSavepoint();

	void addChangeListener(AMLSessionChangeListener changeListener);

	void removeChangeListener(AMLSessionChangeListener changeListener);

	void beginSessionChanges();

	void endSessionChanges();

	void setDocumentURLResolver(DocumentURLResolver documentURLResolver);

	void setProgressMonitor(ProgressMonitor progressMonitor);

	DocumentURLResolver getDocumentURLResolver();

	ProgressMonitor getProgressMonitor();

	AMLValidator getValidator();

	void setValidator(AMLValidatorFactory validatorFactory) throws AMLValidatorException;

	void unsetValidator();

	void enableNotify(boolean enable);
}
