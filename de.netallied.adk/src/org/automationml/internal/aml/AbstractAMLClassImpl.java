/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLForbiddenReferenceException;
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

public abstract class AbstractAMLClassImpl<T extends AbstractAMLClassImpl<?>> extends AbstractAMLClassContainer<T> implements AMLInternalAttributeContainer {

	protected static class DeleteClassChange<T extends AbstractAMLClassImpl<?>> extends AbstractDeleteDocumentElementChange<T> {
		private String name;

		public DeleteClassChange(T element) {
			super(element);
			name = element.getName();
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement documentElement = (AbstractAMLDocumentElement) parentIdentifier.getDocumentElement();
			AbstractAMLClassContainer<T> parent = (AbstractAMLClassContainer<T>) documentElement;

			T parentDocumentElement = (T) parent._createClass(name);
			identifier.setDocumentElement(parentDocumentElement);
		}
	}

	private static class ModifyClassNameChange<T extends AbstractAMLClassImpl<?>> extends AbstractDocumentElementChange<T> {
		private String oldName;
		private String newName;

		public ModifyClassNameChange(T element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			T clazz = (T) getDocumentElement();
			this.oldName = clazz.getName();
		}

		public void setNewName(String newName) {
			this.newName = newName;
		}

		@Override
		public void undo() throws Exception {
			T clazz = (T) getDocumentElement();
			if (newName == null)
				newName = clazz.getName();
			clazz._setName(oldName);
		}

		@Override
		public void redo() throws Exception {
			T clazz = (T) getDocumentElement();
			clazz._setName(newName);
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyClassNameChange))
				return false;
			ModifyClassNameChange<T> change = (ModifyClassNameChange) _change;
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
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
			document.notifyElementModified(getDocumentElement());
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

	private static class ModifyBaseClassChange<T extends AbstractAMLClassImpl<?>> extends AbstractDocumentElementChange<T> {
		private Identifier<T> oldBaseClassIdentifier;
		private Identifier<T> newBaseClassIdentifier;

		public ModifyBaseClassChange(T element, T baseClass) {
			super(element);
			setBaseClass(baseClass);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			T clazz = getDocumentElement();
			T baseClass = (T) clazz.getBaseClass();
			if (baseClass != null)
				this.oldBaseClassIdentifier = (Identifier<T>) identifierManager.getIdentifier(baseClass);
		}

		private void setBaseClass(T newBaseClass) {
			if (newBaseClass == null) {
				if (newBaseClassIdentifier != null)
					newBaseClassIdentifier.release();
				newBaseClassIdentifier = null;
				return;
			}
			if (newBaseClassIdentifier == null) {
				newBaseClassIdentifier = (Identifier<T>) getIdentifierManager().getIdentifier((AbstractAMLClassImpl) newBaseClass);
			} else {
				newBaseClassIdentifier.setDocumentElement(newBaseClass);
			}
		}

		protected void setBaseClassIdentifier(Identifier<?> newBaseClassIdentifier) {
			if (this.newBaseClassIdentifier != null)
				this.newBaseClassIdentifier.release();
			this.newBaseClassIdentifier = (Identifier<T>) newBaseClassIdentifier;
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLClassImpl<T> clazz = (AbstractAMLClassImpl<T>) getDocumentElement();
			if (oldBaseClassIdentifier == null) {
				clazz._setBaseClass(null);
				return;
			}
			T baseClass = oldBaseClassIdentifier.getDocumentElement();
			clazz._setBaseClass(baseClass);
		}

		@Override
		public void redo() throws Exception {
			AbstractAMLClassImpl<T> clazz = (AbstractAMLClassImpl<T>) getDocumentElement();

			if (newBaseClassIdentifier == null) {
				clazz._setBaseClass(null);
				return;
			}
			T baseClass = newBaseClassIdentifier.getDocumentElement();
			clazz._setBaseClass(baseClass);
		}

		@Override
		protected void _delete() {
			if (oldBaseClassIdentifier != null)
				oldBaseClassIdentifier.release();
			if (newBaseClassIdentifier != null)
				newBaseClassIdentifier.release();
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyBaseClassChange))
				return false;
			ModifyBaseClassChange<T> change = (ModifyBaseClassChange<T>) _change;
			change.setBaseClassIdentifier((Identifier<T>) newBaseClassIdentifier);
			return true;
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

	}

	private AbstractAMLClassContainer<T> classContainer;
	private String name;
	private Map<String, AMLAttribute> attributes = new LinkedHashMap<String, AMLAttribute>();
	private T baseClass;
	private Set<AMLDocumentElement> cachedReferrers;

	AbstractAMLClassImpl(AbstractAMLClassContainer<T> classContainer) {
		super();
		this.classContainer = classContainer;
	}

	public String getName() {
		assertNotDeleted();
		return name;
	}

	public T getBaseClass() {
		assertNotDeleted();
		return baseClass;
	}

	public void setBaseClass(T clazz) throws AMLValidationException {
		if (clazz == null)
			throw new AMLForbiddenReferenceException(this, "BaseClass must not be null");
		
		assertNotDeleted();
		assertSameSession(clazz);
		
		if (this.baseClass == clazz)
			return;

		AMLValidationResultList validationResult = validateSetBaseClass(clazz);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyBaseClassChange<AbstractAMLClassImpl<?>> change = new ModifyBaseClassChange<AbstractAMLClassImpl<?>>(this, clazz);
			getSavepointManager().addChange(change);
		}

		_setBaseClass(clazz);
		getDocument().notifyElementModified(this);
	}

	public void unsetBaseClass() throws AMLValidationException {
		assertNotDeleted();

		if (this.baseClass == null)
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyBaseClassChange<AbstractAMLClassImpl<?>> change = new ModifyBaseClassChange<AbstractAMLClassImpl<?>>(this, null);
			getSavepointManager().addChange(change);
		}

		_setBaseClass(null);
	}

	void _setBaseClass(T clazz) throws AMLValidationException {
		if (this.baseClass != null)
			((AbstractAMLClassImpl) baseClass).removeReferrer(this);

		if (clazz != null) {
			((AbstractAMLClassImpl) clazz).addReferrer(this);
		}

		this.baseClass = clazz;
	}
	
	public AMLValidationResultList validateSetBaseClass(
			T clazz) {
		
		if (clazz != null && !isClassDocumentReferenced(clazz))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Document of the base class must be referenced");
		
		if (isInBaseClassHierarchy(clazz))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "BaseClass must not be contained in Base Class Hierarchy (circular reference)");
				
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}


	private boolean isClassDocumentReferenced(T clazz) {
		AMLDocument documentA = this.getDocument();
		AMLDocument documentB = clazz.getDocument();
		
		if (documentA.equals(documentB))
			return true;
		
		AMLSessionImpl sessionA = (AMLSessionImpl)documentA.getSession();		
		AMLSessionImpl sessionB = (AMLSessionImpl)documentB.getSession();
		if (!sessionA.equals(sessionB))
			return false;
		
		AMLDocumentManager docManager = sessionA.getDocumentManager();
		
		return docManager.isDocumentReferencedBy(documentA, documentB, null);
	}

	protected void addReferrer(AMLDocumentElement referrer) throws AMLValidationException {
		getDocumentManager().addImplicitReference(referrer, this);
		if (cachedReferrers == null)
			cachedReferrers = new HashSet<AMLDocumentElement>();
		cachedReferrers.add(referrer);
	}

	protected void removeReferrer(AMLDocumentElement referrer) throws AMLValidationException {
		getDocumentManager().removeImplicitReference(referrer, this);
		if (cachedReferrers == null)
			return;
		cachedReferrers.remove(referrer);
	}

	@Override
	public AMLDocumentImpl getDocument() {
		return classContainer.getDocument();
	}

	public AMLValidationResultList validateNameChange(String newName) {
		return _validateNameChange(newName);
	}

	protected AMLValidationResultList _validateNameChange(String newName) {
		if (newName != null && newName.equals(this.name))
			return new AMLValidationResultListImpl(this, Severity.OK, "");
		if (newName.isEmpty())
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name must not be empty");

		AMLValidationResultList resultList = classContainer.validateRenameClass(newName);
		if (!resultList.isOk())
			return resultList;
		AMLValidator validator = getSession().getValidator();
		if (validator == null)
			return resultList;
		return _validateNameChange(validator, newName);
	}

	protected abstract AMLValidationResultList _validateNameChange(AMLValidator validator, String newName);

	public void setName(String newName) throws AMLValidationException {
		assertNotDeleted();
		if (newName != null && newName.equals(this.name))
			return;

		AMLValidationResultList validationResult = _validateNameChange(newName);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyClassNameChange<AbstractAMLClassImpl<?>> change = new ModifyClassNameChange<AbstractAMLClassImpl<?>>(this);
			change.setNewName(newName);
			getSavepointManager().addChange(change);
		}
		_setName(newName);
		getDocument().notifyElementModified(this);
	}

	public void _setName(String name) throws AMLValidationException {
		classContainer._renameClass(this, name);
		this.name = name;
	}

	@Override
	public String getClassPath() {
		assertNotDeleted();
		StringBuffer buffer = new StringBuffer();
		buffer.append(classContainer.getClassPath());
		buffer.append("/");
		buffer.append(name);
		return buffer.toString();
	}

	public boolean isInBaseClassHierarchy(T clazz) {
		if (clazz == null)
			return false;
		T nextClass = clazz;
		while (nextClass != null) {
			if (nextClass == this)
				return true;
			nextClass = (T) nextClass.getBaseClass();
		}

		return false;
	}

	public AMLValidationResultList validateDelete() {
		return _validateDelete();
	}

	private AMLValidationResultList _validateDelete() {
		AMLValidationResultList result = classContainer.validateDeleteClass(this);
		if (!result.isOk())
			return result;

		if (getClassesCount() != 0)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Element has child elements");

		if (getAttributes().iterator().hasNext())
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Element has attributes");

		AMLValidator validator = getSession().getValidator();

		return doValidateDelete(validator);
	}

	protected abstract AMLValidationResultList doValidateDelete(AMLValidator validator);

	@Override
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

	public boolean isReferenced() {
		return cachedReferrers != null && !cachedReferrers.isEmpty();
	}
	
	public int getReferrerCount() {
		assertNotDeleted();
		if (cachedReferrers == null)
			return 0;
		return cachedReferrers.size();
	}

	public Iterable<AMLDocumentElement> getReferrers() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLDocumentElement>(cachedReferrers);
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		classContainer.removeClass(this);
		if (getBaseClass() != null) {
			getBaseClass().removeReferrer(this);
			getDocumentManager().removeImplicitReference(this, getBaseClass());
		}
	}

	@Override
	public AMLDocumentElement getParent() {
		assertNotDeleted();
		return classContainer;
	}

	// private void setAttributes(AMLAttributeIterator attributes) {
	// while (attributes.hasNext()) {
	// AMLAttribute attribute = attributes.next();
	// AMLAttribute newAttribute = new AMLAttributeImpl(this, attribute);
	// this.attributes.put(attribute.getName(), newAttribute);
	// }
	// }

	// public AMLAttribute addAttribute(AMLAttribute attribute) throws AMLNameAlreadyInUseException {
	// return AMLAttributeContainerHelper.addAttribute(this, attributes, attribute);
	// }
	//
	// AMLAttribute _addAttribute(AMLAttribute attribute) throws AMLNameAlreadyInUseException {
	// return AMLAttributeContainerHelper._addAttribute(this, attributes, attribute);
	// }

	public AMLAttribute getAttribute(String name) {
		return AMLAttributeContainerHelper.getAttribute(this, name);
	}

	public void removeAttribute(AMLAttribute attribute) {
		_removeAttribute(attribute);
	}

	protected void _removeAttribute(AMLAttribute attribute) {
		AMLAttributeContainerHelper._removeAttribute(this, attribute);
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

	protected AMLAttribute _createAttribute(String name) throws AMLValidationException  {
		return AMLAttributeContainerHelper._createAttribute(this, name);
	}

	@Override
	public Iterable<AMLAttribute> getAttributes() {
		return AMLAttributeContainerHelper.getAttributes(attributes);
	}

	@Override
	public int getAttributesCount() {
		assertNotDeleted();
		return attributes.size();
	}
	
	@Override
	public Map<String, AMLAttribute> _getAttributes() {
		return attributes;
	}

	@Override
	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}

	@Override
	protected void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		AbstractAMLClassContainer<T> oldParent = (AbstractAMLClassContainer<T>)oldParentElement;
		AbstractAMLClassContainer<T> newParent = (AbstractAMLClassContainer<T>)newParentElement;
		oldParent._removeClass(getName());
		this.classContainer = newParent;
		newParent._addClass((T)this, (T)beforeElement, (T)afterElement);
	}
	
	@Override
	protected AMLDocumentElement _getBefore() {
		AbstractAMLClassContainer<T> parent = (AbstractAMLClassContainer<T>) getParent();
		return parent._getClassBefore((T)this);
	}
	
	@Override
	public void validateIfIsInDocumentScope(AMLDocumentElement elementToReparent, AMLDocumentImpl document,
			AMLValidationResultListImpl result, Set<AMLDocumentElement> checkedElements) {
		if (checkedElements.contains(this))
			return;
		checkedElements.add(this);
		
		if (!isDescendantOf(elementToReparent) &&!getDocumentManager().getDocumentScope(document).isInDocumentScope(this))
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "class is not part of document scope"));
		
		if (isDescendantOf(elementToReparent)) {
			for (AMLDocumentElement documentElement : getReferrers()) {
				// falscher test!!
				//((AbstractAMLDocumentElement)documentElement).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
				// richtig ist
				if (checkedElements.contains(documentElement))
					continue;
				AMLDocumentImpl referrerDocumentScope = (AMLDocumentImpl)documentElement.getDocument();
				if (!getDocumentManager().getDocumentScope(referrerDocumentScope).isInDocumentScope(document)) 
					result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "base class is not part of referrer's document scope"));
			}
			if (baseClass != null) {
				baseClass.validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
			}
			for (AMLAttribute attribute : attributes.values()) {
				((AMLAttributeImpl)attribute).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
			}
		}		
	}

	@Override
	public boolean isReferencedByDocumentElements(AMLDocument document, AMLDocument documentToRemove,
			AMLValidationResultListImpl result) {
		for (AMLDocumentElement documentElement : getReferrers()) {
			if (documentElement.getDocument().equals(document))
				result.addDocumentElementValidationResult(new AMLValidationResultImpl(documentElement, Severity.AML_ERROR, "class is referred by an element of the referrer document"));
		}
		return super.isReferencedByDocumentElements(document, documentToRemove, result);
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkForm)
			throws AMLValidationException {
		if (getBaseClass() != null) {
			if (!getBaseClass().isDescendantOf(unlinkForm)) {
				unsetBaseClass();
			}
		}
		
		super._doUnlink(unlinkForm);
	}
	
	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		for (AMLDocumentElement clazz : getReferrers()) {
			if (!((AbstractAMLDocumentElement)clazz).isDescendantOf(baseElement)) {
				validationResultList.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "Base class must be unlinked"));
			}
		}	
		for (AMLAttribute attribute	: getAttributes()) {
			((AbstractAMLDocumentElement)attribute).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		
		super.doValidateDeepDelete(validator, baseElement, validationResultList);
	}

	@Override
	protected void _doDeepDelete(AMLDocumentElement baseElement)
			throws AMLValidationException {
	
		while (getAttributes().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getAttributes().iterator().next())._doDeepDelete(baseElement);
		}
		
		super._doDeepDelete(baseElement);
	}
	
	@Override
	public void deepCopy(AMLDocumentElement rootElement, AMLDocumentElement rootCopyElement, 
			AMLDocumentElement original, Map<AMLDocumentElement, AMLDocumentElement> mapping) throws AMLValidationException {
		
		super.deepCopy(rootElement, rootCopyElement, original, mapping);
		
		T baseClass = (T) ((T) original).getBaseClass();
		if (baseClass != null) {
			if (baseClass.isDescendantOf(rootElement)) {
				if (mapping.containsKey(baseClass))
					setBaseClass((T) mapping.get(baseClass));
				else
					throw new AMLValidationException(new AMLValidationResultListImpl(this, Severity.AML_ERROR, "missing copy of base class"));
			} else if (((AbstractAMLDocumentElement) baseClass).isDescendantOf(rootElement.getDocument())) {
				if (getSession().equals(rootElement.getSession())) {
					setBaseClass((T) ((T) original).getBaseClass());
				} else {
					// find base class in document 
					String path = ((T) original).getBaseClass().getClassPath();
					T baseClass1 = ((AMLDocumentImpl)getDocument()).getClassByPath(path, baseClass);
					setBaseClass(baseClass1);
				}
			}
		}
	}

	@Override
	protected AMLDocumentElement _getAfter() {
		AbstractAMLClassContainer<T> parent = (AbstractAMLClassContainer<T>) getParent();
		return parent._getClassBefore((T)this);
	}
}
