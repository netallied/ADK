/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalLink;
import org.automationml.aml.AMLMirrorObject;
import org.automationml.aml.AMLRenamable;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.AbstractDocumentElementChange;
import org.automationml.internal.Change;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;
import org.automationml.internal.ReadOnlyIterable;

public class AMLExternalInterfaceImpl extends AMLElementImpl implements AMLExternalInterface, AMLInternalAttributeContainer {

	protected static class DeleteExternalInterfaceChange extends AbstractDeleteDocumentElementChange<AMLExternalInterfaceImpl> {
		private UUID id;
		private String name;
		private Identifier<AMLInterfaceClassImpl> referenceIdentifier;
		private Set<Identifier<AMLAttributeImpl>> attributes;

		public DeleteExternalInterfaceChange(AMLExternalInterfaceImpl element) {
			super(element);
			id = element.getId();
			name = element.getName();
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLInterfaceClassImpl ref = (AMLInterfaceClassImpl) getDocumentElement().getInterfaceClass();
			referenceIdentifier = (Identifier<AMLInterfaceClassImpl>) identifierManager.getIdentifier(ref);

			for (AMLAttribute attribute : getDocumentElement().getAttributes()) {
				if (attributes == null)
					attributes = new HashSet<Identifier<AMLAttributeImpl>>();
				attributes.add((Identifier<AMLAttributeImpl>) identifierManager.getIdentifier((AMLAttributeImpl) attribute));
			}
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement documentElement = (AbstractAMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLInternalExternalInterfaceContainer parent = (AMLInternalExternalInterfaceContainer) documentElement;
			AMLInterfaceClassImpl interfaceClass = referenceIdentifier.getDocumentElement();

			AMLExternalInterfaceImpl newCreatedExternalInterface = (AMLExternalInterfaceImpl) AMLExternalInterfaceContainerHelper._createExternalInterface(
					parent, interfaceClass, id);
			newCreatedExternalInterface.setName(name);
			if (attributes != null) {
				for (Identifier<AMLAttributeImpl> attribute : attributes) {
					AMLAttribute attr = attribute.getDocumentElement();
					newCreatedExternalInterface.attributes.put(attr.getName(), attr);
				}
			}
			identifier.setDocumentElement(newCreatedExternalInterface);
		}

		@Override
		protected void _delete() {
			super._delete();
			if (referenceIdentifier != null)
				referenceIdentifier.release();
			referenceIdentifier = null;

			if (attributes != null) {
				for (Identifier<AMLAttributeImpl> attribute : attributes) {
					attribute.release();
				}
				attributes.clear();
			}
		}
	}

	private static class ModifyExternalInterfaceNameChange extends AbstractDocumentElementChange<AMLExternalInterfaceImpl> {
		private String oldName;
		private String newName;

		public ModifyExternalInterfaceNameChange(AMLExternalInterfaceImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			AMLExternalInterfaceImpl externalInterface = (AMLExternalInterfaceImpl) getDocumentElement();
			this.oldName = externalInterface.getName();
		}

		public void setNewName(String newName) {
			this.newName = newName;
		}

		@Override
		public void undo() throws Exception {
			AMLExternalInterfaceImpl externalInterface = (AMLExternalInterfaceImpl) getDocumentElement();
			if (newName == null)
				newName = externalInterface.getName();
			externalInterface._setName(oldName);
		}

		@Override
		public void redo() throws Exception {
			AMLExternalInterfaceImpl internalElement = (AMLExternalInterfaceImpl) getDocumentElement();
			internalElement._setName(newName);
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyExternalInterfaceNameChange))
				return false;
			ModifyExternalInterfaceNameChange change = (ModifyExternalInterfaceNameChange) _change;
			change.setNewName(newName);
			return true;
		}

		@Override
		protected void _delete() {
			// this is not really required, but helpful for debugging
			oldName = null;
			newName = null;
		}

		@Override
		protected void notifyChangeListenersBeforeUndo() {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
			document.notifyElementModified(getDocumentElement());
		}

		@Override
		protected void notifyChangeListenersAfterUndo() {
			
		}

		@Override
		protected void notifyChangeListenersBeforeRedo() {
		}

		@Override
		protected void notifyChangeListenersAfterRedo() {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
			document.notifyElementModified(getDocumentElement());
		}

	}

	private UUID id;
	private AMLInternalExternalInterfaceContainer externalInterfaceContainer = null;
	private AMLInterfaceClass interfaceClass;
	private Map<String, AMLAttribute> attributes = new LinkedHashMap<String, AMLAttribute>();
	private Set<AMLInternalLink> internalLinks = new HashSet<AMLInternalLink>();
	private String name;

	public AMLExternalInterfaceImpl(AMLInternalExternalInterfaceContainer container, AMLInterfaceClass interfaceClass, UUID id) {
		this.externalInterfaceContainer = container;
		this.id = id;
		this.interfaceClass = interfaceClass;
	}

	@Override
	public AMLDocumentElement getParent() {
		assertNotDeleted();
		return externalInterfaceContainer;
	}

	@Override
	public void delete() throws AMLValidationException {
		if (isDeleted())
			return;

		AMLValidationResultList validationResult = AMLExternalInterfaceContainerHelper.validateDeleteExternalInterface(this);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			DeleteExternalInterfaceChange change = new DeleteExternalInterfaceChange(this);
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		
		// deleting attributes
//		for (AMLAttribute attribute : getAttributes()) {
//			deleteAttribute(attribute);
//		}
//
		_delete();
	}

	private void deleteAttribute(AMLAttribute attribute) throws AMLValidationException {
		if (attribute.hasAttributes()) {
			for (AMLAttribute attribute2 : getAttributes()) {
				deleteAttribute(attribute2);
			}
		}

		attribute.delete();
	}

	@Override
	public AMLValidationResultList validateCreateAttribute(String name) {
		return AMLAttributeContainerHelper._validateCreateAttribute(this, name);
	}

	@Override
	public Iterable<AMLAttribute> getAttributes() {
		assertNotDeleted();
		Iterable<AMLAttribute> attributes = AMLAttributeContainerHelper.getAttributes(this.attributes);
		return attributes;
	}

	@Override
	public int getAttributesCount() {
		assertNotDeleted();
		return attributes.size();
	}
	
	@Override
	public AMLAttribute getAttribute(String name) {
		assertNotDeleted();
		AMLAttribute attribute = AMLAttributeContainerHelper.getAttribute(this, name);

		return attribute;
	}

	@Override
	public AMLAttribute createAttribute(String name) throws AMLValidationException {
		return createAttribute(name, null);
	}
	
	@Override
	public AMLAttribute createAttribute(String name, AMLAttribute attribute)
			throws AMLValidationException {
		assertNotDeleted();
		AMLAttributeImpl copy = AMLAttributeContainerHelper.createAttribute(this, name);
		
		if (attribute != null) 
			copy.deepCopy(attribute, copy, attribute, new HashMap<AMLDocumentElement, AMLDocumentElement>());
		
		return copy;
	}

	@Override
	public Map<String, AMLAttribute> _getAttributes() {
		return attributes;
	}

	@Override
	public void setName(String newName) throws AMLValidationException {
		assertNotDeleted();
		if (newName != null && newName.equals(this.name))
			return;

		AMLValidationResultList resultList = validateNameChange(newName);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyExternalInterfaceNameChange change = new ModifyExternalInterfaceNameChange(this);
			change.setNewName(newName);
			getSavepointManager().addChange(change);
		}
		_setName(newName);
		getDocument().notifyElementModified(this);
	}

	private void _setName(String newName) {
		this.name = newName;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public AMLDocumentImpl getDocument() {
		return (AMLDocumentImpl) externalInterfaceContainer.getDocument();
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		AMLExternalInterfaceContainerHelper._removeExternalInterface(externalInterfaceContainer, this);
		getDocumentManager().removeUniqueId(getDocument(), id);
	}

	@Override
	public AMLInterfaceClass getInterfaceClass() {
		assertNotDeleted();
		return interfaceClass;
	}

	@Override
	public String getName() {
		assertNotDeleted();
		return name == null ? "" : name;
	}

	@Override
	public AMLValidationResultList validateNameChange(String newName) {
		assertNotDeleted();

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateExternalInterfaceSetName(this, newName);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}

	public AMLAttributeImpl createAttribute(AMLAttributeImpl attribute) throws AMLValidationException {
		assertNotDeleted();
		AMLAttributeImpl attributeImpl = AMLAttributeContainerHelper.createAttribute(this, attribute);
		return attributeImpl;
	}

	@Override
	public String getSymbolName() {
		StringBuffer buffer = new StringBuffer();
		//buffer.append(id.toString()).append(":").append(name);
		buffer.append("{").append(((AMLInternalElement)getParent()).getId().toString()).append("}:").append(name);
		return buffer.toString();
	}

	@Override
	public boolean hasInternalLinks() {
		assertNotDeleted();
		return !internalLinks.isEmpty();
	}

	@Override
	public int getInternalLinksCount() {
		assertNotDeleted();
		return internalLinks.size();
	}

	@Override
	public Iterable<AMLInternalLink> getInternalLinks() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLInternalLink>(internalLinks);
	}

	public void _addInternalLink(AMLInternalLink internalLink) {
		internalLinks.add(internalLink);
	}

	public void _removeInternalLink(AMLInternalLink internalLink) {
		internalLinks.remove(internalLink);
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
//		AMLValidationResultList validationResult = externalInterfaceContainer.validateExternalInterface(this);
//		if (validationResult.isAnyOperationNotPermitted())
//			return validationResult;

		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateExternalInterfaceDelete(this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}
	
	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "an ExternalInterface can't be reparented");
	}

	@Override
	protected void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
	}

	@Override
	protected AMLDocumentElement _getBefore() {
		return null;
	}
	@Override
	protected AMLDocumentElement _getAfter() {
		return null;
	}

	@Override
	public void validateIfIsInDocumentScope(AMLDocumentElement elementToReparent, AMLDocumentImpl document,
			AMLValidationResultListImpl result, Set<AMLDocumentElement> checkedElements) {
		if (checkedElements.contains(this))
			return;
		checkedElements.add(this);
		
		if (!isDescendantOf(elementToReparent) && !getDocumentManager().getDocumentScope(document).isInDocumentScope(this))
		{	
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "external interface class is not part of document scope"));
			getDocumentManager().getDocumentScope(document).isInDocumentScope(this);
		}
		
		((AMLInterfaceClassImpl)interfaceClass).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);		
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkForm)
			throws AMLValidationException {
		if (getInterfaceClass() != null) {
			if (!isDescendantOf(unlinkForm) || !((AbstractAMLDocumentElement) getInterfaceClass()).isDescendantOf(unlinkForm)) {
				// delete link
//				for (AMLInternalLink internalLink : internalLinks) {
//					internalLink.delete();
//				}
				Iterator<AMLInternalLink> iter = internalLinks.iterator();
				while (iter.hasNext()) {
					AMLInternalLink internalLink = iter.next();
					internalLink.delete();
					iter = internalLinks.iterator();
				}
				deepDelete();
			}
		}
	}

	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateExternalInterfaceDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
		
		// check links
		for (AMLInternalLink internalLink : internalLinks) {
			((AbstractAMLDocumentElement)internalLink).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		
		for (AMLAttribute attribute	: getAttributes()) {
			((AbstractAMLDocumentElement)attribute).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		
	}

	@Override
	protected void _doDeepDelete(AMLDocumentElement baseElement)
			throws AMLValidationException {
		while (getInternalLinks().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getInternalLinks().iterator().next())._doDeepDelete(baseElement);
		}
		
		while (getAttributes().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getAttributes().iterator().next())._doDeepDelete(baseElement);
		}
		
		delete();		
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		mapping.put(original, this);
		
		setName(((AMLRenamable) original).getName());
		
		for (AMLAttribute attribute	: ((AMLExternalInterface) original).getAttributes()) {
			AMLAttribute copyAttribute = createAttribute(attribute.getName());
			((AbstractAMLDocumentElement)copyAttribute).deepCopy(rootElement, rootCopyElement, attribute, mapping);
		}
		
		// links will be created by the containing internal element		
	}
}
