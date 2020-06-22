/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLRenamable;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractDocumentElementChange;
import org.automationml.internal.Change;
import org.automationml.internal.IdentifierManager;

public abstract class AbstractAMLClassLibraryImpl extends AbstractAMLClassContainer<AbstractAMLClassImpl<?>> implements AMLRenamable {

	private static class ModifyClassLibraryNameChange extends AbstractDocumentElementChange {
		private String oldName;
		private String newName;

		public ModifyClassLibraryNameChange(AbstractAMLClassLibraryImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			AbstractAMLClassLibraryImpl library = (AbstractAMLClassLibraryImpl) getDocumentElement();
			this.oldName = library.getName();
		}

		public void setNewName(String newName) {
			this.newName = newName;
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLClassLibraryImpl library = (AbstractAMLClassLibraryImpl) getDocumentElement();
			if (newName == null)
				newName = library.getName();
			library._setName(oldName);
		}

		@Override
		public void redo() throws Exception {
			AbstractAMLClassLibraryImpl library = (AbstractAMLClassLibraryImpl) getDocumentElement();
			library._setName(newName);
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyClassLibraryNameChange))
				return false;
			ModifyClassLibraryNameChange change = (ModifyClassLibraryNameChange) _change;
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
		}

		@Override
		protected void notifyChangeListenersAfterUndo() {
			if (getDocumentElement() != null)
			{
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
	}

	private AMLDocumentImpl document;
	protected String name;

	AbstractAMLClassLibraryImpl(AMLDocumentImpl amlDocument, String name) {
		this.document = amlDocument;
		this.name = name;
	}

	@Override
	public AMLDocumentImpl getDocument() {
		assertNotDeleted();
		return document;
	}
	
	void _setDocument(AMLDocumentImpl document) {
		this.document = document; 
	}

	@Override
	public AMLValidationResultList validateNameChange(String newName) {
		return _validateNameChange(newName);
	}

	protected AMLValidationResultList _validateNameChange(String newName) {
		if (newName != null && newName.equals(getName()))
			return AMLValidationResultList.EMPTY;

		if (newName.isEmpty())
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name must not be empty");

		try {
			if (isClassLibraryNameDefined(newName))
				return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name already exists");
		} catch (AMLDocumentScopeInvalidException e) {
			return e.getValidationResultList();
		}

		AMLValidator validator = getSession().getValidator();
		if (validator == null)
			return new AMLValidationResultListImpl(this, Severity.OK, "" );

		AMLValidationResultListImpl validationResult = validateName(validator, newName);
		validationResult.assertNotInternalValidationSeverity();

		return validationResult;
	}

	protected abstract boolean isClassLibraryNameDefined(String newName) throws AMLDocumentScopeInvalidException;
	
	protected abstract AMLValidationResultListImpl validateName(AMLValidator validator, String newName);

	public void setName(String newName) throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList validationResult = _validateNameChange(newName);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyClassLibraryNameChange change = new ModifyClassLibraryNameChange(this);
			change.setNewName(newName);
			getSavepointManager().addChange(change);
		}

		// Modell modifizieren
		_setName(newName);

		getDocument().notifyElementModified(this);
	}

	protected abstract void _setName(String newName) throws AMLValidationException;

	public String getName() {
		assertNotDeleted();
		return name;
	}

	@Override
	public String getClassPath() {
		assertNotDeleted();
		return name;
	}

	public void delete() throws AMLValidationException {
		if (isDeleted())
			return;
		
		AMLValidationResultList validationResult = _validateDelete();
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);
		
		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			Change change = createDeleteChange();
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
	}
	
	protected abstract Change createDeleteChange();

	public AMLValidationResultList validateDelete() {
		return _validateDelete();
	}

	private AMLValidationResultList _validateDelete() {
		if (getClassesCount() != 0) {
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Library has child elements.");
		}
		
		AMLValidator validator = getSession().getValidator();
		if (validator == null)
			return new AMLValidationResultListImpl(this, Severity.OK, "" );
		
		return doValidateDelete( validator );
	}
	
	@Override
	public AMLDocumentElement getParent() {
		assertNotDeleted();
		return getDocument();
	}
}
