/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLMirrorContainer;
import org.automationml.aml.AMLMirrorObject;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.AbstractCreateDocumentElementChange;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;

public class AMLMirrorContainerHelper {
	
	private static class CreateMirrorChange extends AbstractCreateDocumentElementChange {
		private UUID id;
		private Identifier<AMLInternalElementImpl> internalElement;

		public CreateMirrorChange(AMLMirrorObjectImpl documentElement) {
			super(documentElement);
			this.id = documentElement.getId();
		}

		@Override
		public void redo() throws Exception {
			AMLInternalMirrorContainer container = (AMLInternalMirrorContainer) parentIdentifier.getDocumentElement();
			AMLInternalElement internalElement = this.internalElement.getDocumentElement();
			AMLMirrorObjectImpl clazz = (AMLMirrorObjectImpl) AMLMirrorContainerHelper._createMirror(container, id, internalElement);
			identifier.setDocumentElement(clazz);
		}

		@Override
		protected void _delete() {
			super._delete();
			if (internalElement != null)
				internalElement.release();
			internalElement = null;
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLInternalElementImpl roleClass = (AMLInternalElementImpl) ((AMLMirrorObject) getDocumentElement()).getInternalElement();
			this.internalElement = (Identifier<AMLInternalElementImpl>) identifierManager.getIdentifier(roleClass);
		}
	}

	static AMLMirrorObjectImpl createMirror(AMLMirrorContainer mirrorContainer,
			UUID id, AMLInternalElement internalElement) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) mirrorContainer.getDocument();

		AMLValidationResultList resultList = validateMirrorCreate(mirrorContainer, id, internalElement);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		document.notifyElementValidated(resultList);

		AMLMirrorObjectImpl mirror = _createMirror(mirrorContainer, id, internalElement);

		if (((AMLSessionImpl) document.getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateMirrorChange change = new CreateMirrorChange(mirror);
			((AMLSessionImpl) document.getSession()).getSavepointManager().addChange(change);
		}

		document.notifyElementCreated(mirror, mirrorContainer);

		return mirror;
	}

	static AMLMirrorObjectImpl _createMirror(
			AMLMirrorContainer mirrorContainer, UUID id,
			AMLInternalElement internalElement) throws AMLDocumentScopeInvalidException {
		AMLMirrorObjectImpl mirror = new AMLMirrorObjectImpl(mirrorContainer, id, internalElement);
		((AMLInternalMirrorContainer) mirrorContainer)._getMirrors().put(id, mirror);

		((AMLInternalElementImpl) internalElement).addMirrorObject(mirror);

		AMLDocumentImpl document = (AMLDocumentImpl) mirrorContainer.getDocument();
		document.getDocumentManager().addUniqueId(mirror, id);
		return mirror;
	}

	static AMLValidationResultList validateMirrorCreate(
			AMLMirrorContainer mirrorContainer, UUID id,
			AMLInternalElement internalElement) {
		
		AMLDocumentImpl document = (AMLDocumentImpl) mirrorContainer.getDocument();
		try {
			if (document.getDocumentManager().isUniqueIdDefined(mirrorContainer, id))
				return new AMLValidationResultListImpl(mirrorContainer, Severity.AML_ERROR, "Id already exists");
		} catch (AMLDocumentScopeInvalidException e) {
		}

		return validateMirrorCreate(mirrorContainer, internalElement);
	}

	static AMLValidationResultList validateMirrorCreate(
			AMLMirrorContainer mirrorContainer,
			AMLInternalElement internalElement) {
		if (internalElement == null)
			return new AMLValidationResultListImpl(mirrorContainer, Severity.AML_ERROR, "InternalElement not specified");

		AMLValidator validator = mirrorContainer.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateMirrorObjectCreate(mirrorContainer, internalElement);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(mirrorContainer, Severity.OK, "");
	}

	static void _removeMirror(AMLInternalMirrorContainer mirrorContainer,
			AMLMirrorObjectImpl amlMirrorObjectImpl) {
		mirrorContainer._getMirrors().remove(amlMirrorObjectImpl.getId());		
	}

	static AMLMirrorObject createMirror(
			AMLInternalMirrorContainer container,
			AMLInternalElement internalElement) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		UUID newId = UUID.randomUUID();
		while (document.getDocumentManager().isUniqueIdDefined(container, newId))
			newId = UUID.randomUUID();

		return createMirror(container, newId, internalElement);
	}

	public static void _addMirror(AMLInternalMirrorContainer parent,
			AMLMirrorObjectImpl amlMirrorObjectImpl,
			AMLMirrorObject beforeElement, AMLMirrorObject afterElement) {
	
		if (beforeElement == null && afterElement == null) {
			parent._getMirrors().put(amlMirrorObjectImpl.getId(), amlMirrorObjectImpl);
			return;
		}
		Map<UUID, AMLMirrorObject> mirrorObjects = new LinkedHashMap<UUID, AMLMirrorObject>();
		for (UUID id : parent._getMirrors().keySet()) {
			if (beforeElement != null && beforeElement.getId().equals(id))
				mirrorObjects.put(amlMirrorObjectImpl.getId(), amlMirrorObjectImpl);
			mirrorObjects.put(id, parent._getMirrors().get(id));
			if (afterElement != null && afterElement.getId().equals(id))
				mirrorObjects.put(amlMirrorObjectImpl.getId(), amlMirrorObjectImpl);
		}
		parent._getMirrors().clear();
		parent._getMirrors().putAll(mirrorObjects);		
	}

	static AMLDocumentElement _getMirrorBefore(
			AMLInternalMirrorContainer internalMirrorContainer,
			AMLMirrorObjectImpl amlMirrorObjectImpl) {
		ListIterator<UUID> iterator = new ArrayList<UUID>(internalMirrorContainer._getMirrors().keySet()).listIterator();
		while (iterator.hasNext()) {
			if (amlMirrorObjectImpl.getId().equals(iterator.next())) {
				AMLMirrorObject previous = (AMLMirrorObject) internalMirrorContainer._getMirrors().get(iterator.previous());
				if (previous.equals(amlMirrorObjectImpl))
					return null;
				else
					return previous;
			}
		}
		return null;		
	}

	static AMLDocumentElement _getMirrorAfter(
			AMLInternalMirrorContainer internalMirrorContainer,
			AMLMirrorObjectImpl amlMirrorObjectImpl) {
		ListIterator<UUID> iterator = new ArrayList<UUID>(internalMirrorContainer._getMirrors().keySet()).listIterator();
		while (iterator.hasNext()) {
			if (amlMirrorObjectImpl.getId().equals(iterator.next())) {
				if (iterator.hasNext()) {
					AMLMirrorObject after = (AMLMirrorObject) internalMirrorContainer._getMirrors().get(iterator.next());
					if (after.equals(amlMirrorObjectImpl))
						return null;
					else
						return after;
				}
				return null;
			}
		}
		return null;		
	}

}
