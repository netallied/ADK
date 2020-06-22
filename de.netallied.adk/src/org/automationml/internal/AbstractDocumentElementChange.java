/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal;

import org.automationml.aml.AMLDocument;
import org.automationml.internal.aml.AMLSessionImpl;
import org.automationml.internal.aml.AbstractAMLDocumentElement;

public abstract class AbstractDocumentElementChange<T extends AbstractAMLDocumentElement> extends Change {
	protected Identifier<T> identifier;

	public AbstractDocumentElementChange(AbstractAMLDocumentElement documentElement) {
		AMLSessionImpl session = (AMLSessionImpl) documentElement.getSession();
		IdentifierManager identifierManager = session.getIdentifierManager();
		identifier = (Identifier<T>) identifierManager.getIdentifier(documentElement);
		initializeIdentifiers(identifierManager);
	}

	protected abstract void initializeIdentifiers(IdentifierManager identifierManager);

	@Override
	public final void delete() {
		identifier.release();
		identifier = null;
		_delete();
	}

	protected abstract void _delete();

	protected T getDocumentElement() {
		return identifier.getDocumentElement();
	}

	protected IdentifierManager getIdentifierManager() {
		return getDocumentElement().getSavepointManager().getIdentifierManager();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractDocumentElementChange other = (AbstractDocumentElementChange) obj;
		if (identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!identifier.equals(other.identifier)) {
			return false;
		}
		return true;
	}

	public AMLDocument getDocument() {
		T documentElement = getDocumentElement();
		if( documentElement == null)
			return null;
		return documentElement.getDocument();
	}
}
