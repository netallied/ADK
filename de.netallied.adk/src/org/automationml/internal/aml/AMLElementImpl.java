/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.automationml.aml.AMLElement;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.ReadOnlyIterable;

public abstract class AMLElementImpl extends AbstractAMLDocumentElement implements AMLElement {

	public class Revision implements AMLElement.Revision {
		public Date date;
		public String oldVersion;
		public String newVersion;
		public String authorName;
		public String comment;

		protected Revision() {
		}

		@Override
		public Date getRevisionDate() {
			return date;
		}

		@Override
		public String getOldVersion() {
			return oldVersion;
		}

		@Override
		public String getNewVersion() {
			return newVersion;
		}

		@Override
		public String getAuthorName() {
			return authorName;
		}

		@Override
		public String getComment() {
			return comment;
		}
	}

	private String description;
	private String version;
	private String copyright;
	private String additionalInformation;
	private Collection<AMLElement.Revision> revisions = new ArrayList<AMLElement.Revision>();

	AMLElementImpl(AMLElement element) {
		this.setAdditionalInformation(element.getAdditionalInformation());
		this.setCopyright(element.getCopyright());
		this.setDescription(element.getDescription());
		this.setVersion(element.getVersion());
	}

	AMLElementImpl() {
	}

	public void setDescription(String description) {
		this.description = description;

	}

	public void setVersion(String version) {
		this.version = version;

	}
	
	@Override
	public AMLValidationResultList validateDelete() {
		AMLValidator validator = getSession().getValidator();
		return doValidateDelete(validator);
	}

	@Override
	public void addRevision(Date date, String oldVersion, String newVersion, String authorName, String comment) {
		Revision revision = new Revision();
		revision.date = date;
		revision.oldVersion = oldVersion;
		revision.newVersion = newVersion;
		revision.authorName = authorName;
		revision.comment = comment;

		revisions.add(revision);
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;

	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;

	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getCopyright() {
		return copyright;
	}

	@Override
	public String getAdditionalInformation() {
		return additionalInformation;
	}

	@Override
	public abstract AMLDocumentImpl getDocument();

	@Override
	public AMLSessionImpl getSession() {
		return getDocument().getSession();
	}

	@Override
	public int getRevisionsCount() {
		assertNotDeleted();
		return revisions.size();
	}

	@Override
	public Iterable<AMLElement.Revision> getRevisions() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLElement.Revision>(revisions);
	}
}
