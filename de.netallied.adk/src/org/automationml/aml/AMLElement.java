/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.Date;

public interface AMLElement {

	public interface Revision {
		Date getRevisionDate();

		String getOldVersion();

		String getNewVersion();

		String getAuthorName();

		String getComment();
	}

	String getAdditionalInformation();

	String getCopyright();

	String getVersion();

	String getDescription();

	void setDescription(String description);

	void setCopyright(String copyright);

	void setVersion(String version);

	void setAdditionalInformation(String additionalInformation);

	void addRevision(Date date, String oldVersion, String newVersion, String authorName, String comment);

	int getRevisionsCount();

	Iterable<Revision> getRevisions();
}
