/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.automationml.aml.AMLCOLLADAInterface;
import org.automationml.aml.AMLCOLLADAInterfaceContainer;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLFrameAttributeContainer;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.AbstractDocumentElementChange;
import org.automationml.internal.Change;
import org.automationml.internal.IdentifierManager;

public class AMLCOLLADAInterfaceImpl extends AMLElementImpl implements AMLCOLLADAInterface {

	private static class ModifyCOLLADAInterfaceChange extends AbstractDocumentElementChange<AMLCOLLADAInterfaceImpl> {

		private RefType oldRefType;
		private String oldTarget;
		private String oldName;
		private URI oldRefURI;
		private RefType newRefType;
		private String newTarget;
		private String newName;
		private URI newRefURI;

		public ModifyCOLLADAInterfaceChange(AMLCOLLADAInterfaceImpl element) {
			super(element);
			oldRefType = element.getRefType();
			oldTarget = element.getTarget();
			oldName = element.getName();
			oldRefURI = element.getRefURI();
		}

		public void setNewRefType(RefType refType) {
			this.newRefType = refType;
		}

		public void setNewTarget(String target) {
			this.newTarget = target;
		}

		public void setNewName(String name) {
			this.newName = name;
		}

		public void setNewRefURI(URI refURI) {
			this.newRefURI = refURI;
		}

		@Override
		public void undo() throws Exception {
			AMLCOLLADAInterfaceImpl colladaInterface = (AMLCOLLADAInterfaceImpl) getDocumentElement();
			newRefType = colladaInterface.getRefType();
			colladaInterface._setRefType(oldRefType);

			newTarget = colladaInterface.getTarget();
			colladaInterface._setTarget(oldTarget);

			newName = colladaInterface.getName();
			colladaInterface._setName(oldName);

			newRefURI = colladaInterface.getRefURI();
			colladaInterface._setRefURI(oldRefURI);

		}

		@Override
		public void redo() throws Exception {
			AMLCOLLADAInterfaceImpl colladaInterface = (AMLCOLLADAInterfaceImpl) getDocumentElement();

			colladaInterface._setRefType(newRefType);
			colladaInterface._setName(newName);
			colladaInterface._setTarget(newTarget);
			colladaInterface._setRefURI(newRefURI);
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyCOLLADAInterfaceChange))
				return false;
			ModifyCOLLADAInterfaceChange change = (ModifyCOLLADAInterfaceChange) _change;
			change.setNewRefType(newRefType);
			change.setNewName(newName);
			change.setNewTarget(newTarget);
			change.setNewRefURI(newRefURI);
			return true;
		}

		@Override
		protected void _delete() {
			// this is not really required, but helpful for debugging
		}

		@Override
		protected void notifyChangeListenersBeforeUndo() {
		}

		@Override
		protected void notifyChangeListenersAfterUndo() {
			if (getDocumentElement() != null) {
				AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
				document.notifyElementModified(getDocumentElement());
			}
		}

		@Override
		protected void notifyChangeListenersBeforeRedo() {
		}

		@Override
		protected void notifyChangeListenersAfterRedo() {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
			document.notifyElementModified(getDocumentElement());
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			// TODO Auto-generated method stub

		}
	}

	protected static class DeleteCOLLADAInterfaceChange extends AbstractDeleteDocumentElementChange<AMLCOLLADAInterfaceImpl> {
		private UUID id;
		private RefType refType;
		private String target;
		private String name;
		private URI refURI;

		public DeleteCOLLADAInterfaceChange(AMLCOLLADAInterfaceImpl element) {
			super(element);
			id = element.getId();
			refType = element.getRefType();
			target = element.getTarget();
			name = element.getName();
			refURI = element.getRefURI();
		}

		@Override
		public void undo() throws Exception {
			AMLInternalCOLLADAInterfaceContainer container = (AMLInternalCOLLADAInterfaceContainer) parentIdentifier.getDocumentElement();
			AMLCOLLADAInterfaceImpl clazz = (AMLCOLLADAInterfaceImpl) AMLExternalInterfaceContainerHelper._createCOLLADAInterface(container, refType, id);
			identifier.setDocumentElement(clazz);
			clazz.setTarget(target);
			clazz.setName(name);
			clazz.setRefURI(refURI);
		}
	}

	private AMLInternalCOLLADAInterfaceContainer colladaInterfaceContainer;
	private RefType refType;
	private UUID id;
	private String target;
	private String name;
	private URI refURI;

	public AMLCOLLADAInterfaceImpl(AMLInternalCOLLADAInterfaceContainer container, RefType refType, UUID newId) {
		this.colladaInterfaceContainer = container;
		this.refType = refType;
		this.id = newId;
		refURI = URI.create("");
		target = "";
		name = "";
	}

	@Override
	public AMLDocumentElement getParent() {
		return colladaInterfaceContainer;
	}

	@Override
	public void delete() throws AMLValidationException {
		if (isDeleted())
			return;

		AMLValidationResultList validationResult = AMLExternalInterfaceContainerHelper.validateDeleteCOLLADAInterface(this);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			DeleteCOLLADAInterfaceChange change = new DeleteCOLLADAInterfaceChange(this);
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
	}

	@Override
	public void setRefURI(URI uri) {
		assertNotDeleted();
		if (uri == null || uri.equals(this.refURI))
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyCOLLADAInterfaceChange change = new ModifyCOLLADAInterfaceChange(this);
			change.setNewRefURI(uri);
			getSavepointManager().addChange(change);
		}
		_setRefURI(uri);
		getDocument().notifyElementModified(this);

	}

	private void _setRefURI(URI uri) {
		this.refURI = uri;
	}

	@Override
	public URI getRefURI() {
		assertNotDeleted();
		return refURI;
	}

	@Override
	public void setRefType(RefType type) {
		assertNotDeleted();
		if (type.equals(this.refType))
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyCOLLADAInterfaceChange change = new ModifyCOLLADAInterfaceChange(this);
			change.setNewRefType(type);
			getSavepointManager().addChange(change);
		}
		_setRefType(type);
		getDocument().notifyElementModified(this);
	}

	private void _setRefType(RefType type) {
		this.refType = type;
	}

	@Override
	public RefType getRefType() {
		assertNotDeleted();
		return refType;
	}

	@Override
	public void setTarget(String target) {
		assertNotDeleted();
		if (target == null || target.equals(this.target))
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyCOLLADAInterfaceChange change = new ModifyCOLLADAInterfaceChange(this);
			change.setNewTarget(target);
			getSavepointManager().addChange(change);
		}
		_setTarget(target);
		getDocument().notifyElementModified(this);

	}

	private void _setTarget(String target) {
		this.target = target;
	}

	@Override
	public String getTarget() {
		assertNotDeleted();
		return target;
	}

	@Override
	public void setName(String name) {
		assertNotDeleted();
		if (name.equals(this.name))
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyCOLLADAInterfaceChange change = new ModifyCOLLADAInterfaceChange(this);
			change.setNewName(name);
			getSavepointManager().addChange(change);
		}
		_setName(name);
		getDocument().notifyElementModified(this);

	}

	private void _setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		assertNotDeleted();
		return name;
	}

	@Override
	public AMLValidationResultList validateNameChange(String name) {
		return _validateNameChange(name);
	}

	protected AMLValidationResultList _validateNameChange(String name) {
		//TODO implement me
		return AMLValidationResultList.EMPTY;
	}

	@Override
	public AMLDocumentImpl getDocument() {
		return (AMLDocumentImpl) colladaInterfaceContainer.getDocument();
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		AMLExternalInterfaceContainerHelper._removeCOLLADAInterface(colladaInterfaceContainer, this);
	}

	@Override
	public UUID getId() {
		assertNotDeleted();
		return id;
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		AMLValidationResultList validationResult = AMLExternalInterfaceContainerHelper.validateDeleteCOLLADAInterface(this);
		if (validationResult.isAnyOperationNotPermitted())
			return validationResult;

		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateCOLLADAInterfaceDelete(this);
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
		if (!(newParentElement instanceof AMLCOLLADAInterfaceContainer))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "new parent is not a COLLADA interface container");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateCOLLADAInterfaceReparent((AMLFrameAttributeContainer)oldParentElement, (AMLFrameAttributeContainer)newParentElement, this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}
		
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		AMLInternalCOLLADAInterfaceContainer oldParent = (AMLInternalCOLLADAInterfaceContainer)oldParentElement;
		AMLInternalCOLLADAInterfaceContainer newParent = (AMLInternalCOLLADAInterfaceContainer)newParentElement;
		try {
			AMLExternalInterfaceContainerHelper._removeCOLLADAInterface(oldParent, this);
		} catch (AMLValidationException e) {
		}
		this.colladaInterfaceContainer = newParent;
		newParent._setCOLLADAInterface(this);
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
	public void validateIfIsInDocumentScope(
			AMLDocumentElement elementToReparent, AMLDocumentImpl document,
			AMLValidationResultListImpl result,
			Set<AMLDocumentElement> checkedElements) {
		if (checkedElements.contains(this))
			return;
		checkedElements.add(this);
		
		if (!isDescendantOf(elementToReparent) &&!getDocumentManager().getDocumentScope(document).isInDocumentScope(this))
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "COLLADA interface is not part of document scope"));
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkFrom)
			throws AMLValidationException {
		return;
	}

	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateCOLLADAInterfaceDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		setName(((AMLCOLLADAInterface)original).getName());		
		setRefType(((AMLCOLLADAInterface)original).getRefType());
		setRefURI(((AMLCOLLADAInterface)original).getRefURI());
		setTarget(((AMLCOLLADAInterface)original).getTarget());
	}

	@Override
	public AMLInterfaceClass getInterfaceClass() throws AMLValidationException {
		return getDocument().getInterfaceClassByPath(AMLExternalInterfaceContainerHelper.AUTOMATION_ML_COLLADA_INTERFACE);
	}
}
