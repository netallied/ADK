/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

import org.automationml.aml.AMLDocumentElement;

public class AMLDeserializeIdentifier {
	private final String name;
	private final AMLDeserializeIdentifier parentIdentifier;
	private final AMLLocationInFile filePosition;
	private AMLDocumentElement resolvedElement;

	public AMLDeserializeIdentifier(String name, AMLDeserializeIdentifier parentIdentifier, AMLLocationInFile filePosition) {
		this.name = name;
		this.parentIdentifier = parentIdentifier;
		this.filePosition = filePosition;
	}

	public AMLDeserializeIdentifier(String name) {
		this(name, null, new AMLLocationInFileImpl() );
	}

	public AMLDocumentElement getResolvedElement() {
		return resolvedElement;
	}

	public boolean isResolved() {
		return resolvedElement != null;
	}

	public void setResolvedElement(AMLDocumentElement resolvedItem) {
		this.resolvedElement = resolvedItem;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filePosition == null) ? 0 : filePosition.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parentIdentifier == null) ? 0 : parentIdentifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AMLDeserializeIdentifier other = (AMLDeserializeIdentifier) obj;
		if (filePosition != other.filePosition)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parentIdentifier == null) {
			if (other.parentIdentifier != null)
				return false;
		} else if (!parentIdentifier.equals(other.parentIdentifier))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DeserializeIdentifier [name=" + name + ", parentIdentifier=" + parentIdentifier + ", filePosition=" + filePosition + ", resolvedElement="
				+ resolvedElement + "]";
	}
}
