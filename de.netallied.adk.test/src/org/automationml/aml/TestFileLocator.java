/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.fest.assertions.Assertions.*;

public class TestFileLocator {
	private String rootFolder;
	private Class<?> resourceClass;

	public TestFileLocator(Class<?> resourceClass) {
		this(resourceClass, null);
	}

	public TestFileLocator(Class<?> resourceClass, String rootFolder) {
		super();
		this.resourceClass = resourceClass;
		this.rootFolder = rootFolder;
	}

	public File getFile(String fileName, String folderName) throws URISyntaxException {
		URL url = null;
		if( rootFolder == null || rootFolder.isEmpty())
			url = resourceClass.getResource("/" + folderName);
		else
			url = resourceClass.getResource("/" + rootFolder + "/" + folderName);
			
		if (url == null)
			return null;
		File testCasesFolder = new File(url.toURI());
		assertThat(testCasesFolder).isDirectory();
		return new File(testCasesFolder, fileName);
	}

	public File getInputFile(String fileName) throws Exception {
		return getFile(fileName, "");
	}

	public File getExpectedFile(String fileName) throws Exception {
		return getFile(fileName, "expected");
	}

	private File getTestCaseFile(String fileName, boolean failureIsExpected) throws Exception {
		String folderName = failureIsExpected ? "invalid" : "valid";
		return getFile(fileName, folderName);
	}

	public File getInvalidTestCaseFile(String fileName) throws Exception {
		return getTestCaseFile(fileName, true);
	}

	public File getValidTestCaseFile(String fileName) throws Exception {
		return getTestCaseFile(fileName, false);
	}

}
