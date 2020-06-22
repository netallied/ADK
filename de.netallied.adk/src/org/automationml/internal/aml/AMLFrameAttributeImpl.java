/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Map;
import java.util.Set;

import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLFrameAttribute;
import org.automationml.aml.AMLFrameAttributeContainer;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.AbstractDocumentElementChange;
import org.automationml.internal.Change;
import org.automationml.internal.IdentifierManager;

public class AMLFrameAttributeImpl extends AbstractAMLDocumentElement implements AMLFrameAttribute {

	private static class ModifyFrameAttributeChange extends AbstractDocumentElementChange<AMLFrameAttributeImpl> {

		public final static int CHANGE_X = 1;
		public final static int CHANGE_Y = 2;
		public final static int CHANGE_Z = 3;
		public final static int CHANGE_RX = 4;
		public final static int CHANGE_RY = 5;
		public final static int CHANGE_RZ = 6;
		private double oldValue;
		private double newValue;
		private int changedItem;

		public ModifyFrameAttributeChange(int changedItem, AMLFrameAttributeImpl element) {
			super(element);
			this.changedItem = changedItem;
			AMLSessionImpl session = (AMLSessionImpl) element.getSession();
			IdentifierManager identifierManager = session.getIdentifierManager();
			_initializeIdentifiers(identifierManager);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
		}

		protected void _initializeIdentifiers(IdentifierManager identifierManager) {
			AMLFrameAttributeImpl attribute = (AMLFrameAttributeImpl) getDocumentElement();
			switch (changedItem) {
			case CHANGE_X:
				this.oldValue = attribute.getX();
				break;
			case CHANGE_Y:
				this.oldValue = attribute.getY();
				break;
			case CHANGE_Z:
				this.oldValue = attribute.getZ();
				break;
			case CHANGE_RX:
				this.oldValue = attribute.getRX();
				break;
			case CHANGE_RY:
				this.oldValue = attribute.getRY();
				break;
			case CHANGE_RZ:
				this.oldValue = attribute.getRZ();
				break;
			default:
				break;
			}

		}

		public void setNewValue(double newValue) {
			this.newValue = newValue;
		}

		@Override
		public void undo() throws Exception {
			AMLFrameAttributeImpl attribute = (AMLFrameAttributeImpl) getDocumentElement();
			switch (changedItem) {
			case CHANGE_X:
				newValue = attribute.getX();
				attribute._setX(oldValue);
				break;
			case CHANGE_Y:
				newValue = attribute.getY();
				attribute._setY(oldValue);
				break;
			case CHANGE_Z:
				newValue = attribute.getZ();
				attribute._setZ(oldValue);
				break;
			case CHANGE_RX:
				newValue = attribute.getRX();
				attribute._setRX(oldValue);
				break;
			case CHANGE_RY:
				newValue = attribute.getRY();
				attribute._setRY(oldValue);
				break;
			case CHANGE_RZ:
				newValue = attribute.getRZ();
				attribute._setRZ(oldValue);
				break;
			default:
				break;
			}

		}

		@Override
		public void redo() throws Exception {
			AMLFrameAttributeImpl attribute = (AMLFrameAttributeImpl) getDocumentElement();

			switch (changedItem) {
			case CHANGE_X:
				attribute._setX(newValue);
				break;
			case CHANGE_Y:
				attribute._setY(newValue);
				break;
			case CHANGE_Z:
				attribute._setZ(newValue);
				break;
			case CHANGE_RX:
				attribute._setRX(newValue);
				break;
			case CHANGE_RY:
				attribute._setRY(newValue);
				break;
			case CHANGE_RZ:
				attribute._setRZ(newValue);
				break;
			default:
				break;
			}
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyFrameAttributeChange))
				return false;
			ModifyFrameAttributeChange change = (ModifyFrameAttributeChange) _change;
			change.setNewValue(newValue);
			return true;
		}

		@Override
		protected void _delete() {
			// this is not really required, but helpful for debugging
			oldValue = 0;
			newValue = 0;
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
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + changedItem;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ModifyFrameAttributeChange other = (ModifyFrameAttributeChange) obj;
			if (changedItem != other.changedItem)
				return false;
			return true;
		}
	}

	private static class DeleteFrameAttributeChange extends AbstractDeleteDocumentElementChange<AMLFrameAttributeImpl> {
		private double x;
		private double y;
		private double z;
		private double rx;
		private double ry;
		private double rz;

		public DeleteFrameAttributeChange(AMLFrameAttributeImpl element) {
			super(element);
			x = element.getX();
			y = element.getY();
			z = element.getZ();
			rx = element.getRX();
			ry = element.getRY();
			rz = element.getRZ();
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement documentElement = (AbstractAMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLInternalFrameAttributeContainer parent = (AMLInternalFrameAttributeContainer) documentElement;

			AMLFrameAttributeImpl newCreatedAttribute = (AMLFrameAttributeImpl) AMLAttributeContainerHelper._createFrameAttribute(parent);
			identifier.setDocumentElement(newCreatedAttribute);
			newCreatedAttribute.setX(x);
			newCreatedAttribute.setY(y);
			newCreatedAttribute.setZ(z);
			newCreatedAttribute.setRX(rx);
			newCreatedAttribute.setRY(ry);
			newCreatedAttribute.setRZ(rz);
		}
	}

	private AMLInternalFrameAttributeContainer frameAttributeContainer;
	double x = 0;
	double y = 0;
	double z = 0;
	double rx = 0;
	double ry = 0;
	double rz = 0;

	public AMLFrameAttributeImpl(AMLInternalFrameAttributeContainer container) {
		this.frameAttributeContainer = container;
	}

	@Override
	public AMLSessionImpl getSession() {
		return (AMLSessionImpl) frameAttributeContainer.getSession();
	}

	@Override
	public AMLDocumentImpl getDocument() {
		return (AMLDocumentImpl) frameAttributeContainer.getDocument();
	}

	@Override
	public AMLDocumentElement getParent() {
		return frameAttributeContainer;
	}

	@Override
	public void delete() throws AMLValidationException {
		if (isDeleted())
			return;

		AMLValidationResultList validationResult = validateDelete();
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			DeleteFrameAttributeChange change = new DeleteFrameAttributeChange(this);
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
	}

	@Override
	public void setX(double x) {
		assertNotDeleted();
		if (x == this.x)
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyFrameAttributeChange change = new ModifyFrameAttributeChange(ModifyFrameAttributeChange.CHANGE_X, this);
			change.setNewValue(x);
			getSavepointManager().addChange(change);
		}
		_setX(x);
		getDocument().notifyElementModified(this);
	}

	private void _setX(double x) {
		this.x = x;
	}

	private void _setY(double y) {
		this.y = y;
	}

	private void _setZ(double z) {
		this.z = z;
	}

	private void _setRX(double rx) {
		this.rx = rx;
	}

	private void _setRY(double ry) {
		this.ry = ry;
	}

	private void _setRZ(double rz) {
		this.rz = rz;
	}

	@Override
	public void setY(double y) {
		assertNotDeleted();
		if (y == this.y)
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyFrameAttributeChange change = new ModifyFrameAttributeChange(ModifyFrameAttributeChange.CHANGE_Y, this);
			change.setNewValue(y);
			getSavepointManager().addChange(change);
		}
		_setY(y);
		getDocument().notifyElementModified(this);
	}

	@Override
	public void setZ(double z) {
		assertNotDeleted();
		if (z == this.z)
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyFrameAttributeChange change = new ModifyFrameAttributeChange(ModifyFrameAttributeChange.CHANGE_Z, this);
			change.setNewValue(z);
			getSavepointManager().addChange(change);
		}
		_setZ(z);
		getDocument().notifyElementModified(this);
	}

	@Override
	public void setRX(double rx) {
		assertNotDeleted();
		if (rx == this.rx)
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyFrameAttributeChange change = new ModifyFrameAttributeChange(ModifyFrameAttributeChange.CHANGE_RX, this);
			change.setNewValue(rx);
			getSavepointManager().addChange(change);
		}
		_setRX(rx);
		getDocument().notifyElementModified(this);
	}

	@Override
	public void setRY(double ry) {
		assertNotDeleted();
		if (ry == this.ry)
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyFrameAttributeChange change = new ModifyFrameAttributeChange(ModifyFrameAttributeChange.CHANGE_RY, this);
			change.setNewValue(ry);
			getSavepointManager().addChange(change);
		}
		_setRY(ry);
		getDocument().notifyElementModified(this);

	}

	@Override
	public void setRZ(double rz) {
		assertNotDeleted();
		if (rz == this.rz)
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyFrameAttributeChange change = new ModifyFrameAttributeChange(ModifyFrameAttributeChange.CHANGE_RZ, this);
			change.setNewValue(rz);
			getSavepointManager().addChange(change);
		}
		_setRZ(rz);
		getDocument().notifyElementModified(this);
	}

	@Override
	public double getX() {
		assertNotDeleted();
		return x;
	}

	@Override
	public double getY() {
		assertNotDeleted();
		return y;
	}

	@Override
	public double getZ() {
		assertNotDeleted();
		return z;
	}

	@Override
	public double getRX() {
		assertNotDeleted();
		return rx;
	}

	@Override
	public double getRY() {
		assertNotDeleted();
		return ry;
	}

	@Override
	public double getRZ() {
		assertNotDeleted();
		return rz;
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		AMLAttributeContainerHelper._removeFrameAttribute(frameAttributeContainer, this);
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		AMLValidationResultList validationResult = AMLAttributeContainerHelper.validateDeleteFrameAttribute(this);
		if (validationResult.isAnyOperationNotPermitted())
			return validationResult;

		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateFrameAttributeDelete(this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateDelete() {
		AMLValidator validator = getSession().getValidator();
		return doValidateDelete(validator);
	}

	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		
		if (!(newParentElement instanceof AMLFrameAttributeContainer))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "new parent is not a frame attribute container");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateFrameAttributeReparent((AMLFrameAttributeContainer)oldParentElement, (AMLFrameAttributeContainer)newParentElement, this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}
		
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		AMLInternalFrameAttributeContainer oldParent = (AMLInternalFrameAttributeContainer)oldParentElement;
		AMLInternalFrameAttributeContainer newParent = (AMLInternalFrameAttributeContainer)newParentElement;
		oldParent._removeFrameAttribute();
		this.frameAttributeContainer = newParent;
		newParent._addFrameAttribute(this);
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
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "frame attribute is not part of document scope"));
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
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateFrameAttributeDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		
		setX(((AMLFrameAttribute) original).getX());
		setY(((AMLFrameAttribute) original).getY());
		setZ(((AMLFrameAttribute) original).getZ());
		setRX(((AMLFrameAttribute) original).getRX());
		setRY(((AMLFrameAttribute) original).getRY());
		setRZ(((AMLFrameAttribute) original).getRZ());
	}

}
