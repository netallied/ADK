/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLRenamable;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractCreateDocumentElementChange;
import org.automationml.internal.ReadOnlyIterable;

public abstract class AbstractAMLClassContainer<T extends AbstractAMLClassImpl<?>> extends AMLElementImpl implements AMLDocumentElement {
	private Map<String, T> classes = new LinkedHashMap<String, T>();

	public T createClass(String name) throws AMLValidationException {

		AMLValidationResultList resultList = _validateCreateClass(name);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		getDocument().notifyElementValidated(resultList);

		T clazz = _createClass(name);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			registerCreateChange(clazz);
		}

		getDocument().notifyElementCreated(clazz, this);

		return clazz;
	}

	private AMLValidationResultList _validateCreateClass(String name) {
		if (classes.containsKey(name))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "");

		AMLValidator validator = getSession().getValidator();
		if (validator == null)
			return new AMLValidationResultListImpl(this, Severity.OK, "");
		return _validateCreateClass(validator, name);
	}

	protected abstract AMLValidationResultList _validateCreateClass(AMLValidator validator, String name);

	private static class CreateClassChange<T extends AbstractAMLClassImpl<?>> extends AbstractCreateDocumentElementChange {
		private String name;

		public CreateClassChange(T documentElement) {
			super(documentElement);
			this.name = documentElement.getName();
		}

		@Override
		public void redo() throws Exception {
			AbstractAMLClassContainer<T> container = (AbstractAMLClassContainer<T>) parentIdentifier.getDocumentElement();
			T clazz = (T) container._createClass(name);
			identifier.setDocumentElement(clazz);
		}
	}

	protected void registerCreateChange(T clazz) {
		CreateClassChange<T> change = new CreateClassChange<T>(clazz);
		getSavepointManager().addChange(change);
	}

	public T getClass(String name) {
		return classes.get(name);
	}

	T getClassByPath(String path)  {
		return _getClassByPath(path);
	}

	private T _getClassByPath(String path){
		assertNotDeleted();

		int i = path.indexOf(AMLDocumentImpl.LIBRARY_PATH_SEPARATOR);
		if (i == -1)
			return getClass(path);

		final String containerName = path.substring(0, i);
		final String classPathName = path.substring(i + 1, path.length());

		T container = (T) getClass(containerName);

		if (container == null)
			return null;

		return (T) container.getClassByPath(classPathName);
	}

	protected abstract T _newClass() throws AMLValidationException;

	protected T _createClass(String name) throws AMLValidationException {
		T clazz = _newClass();
		clazz._setName(name);
		classes.put(name, clazz);
		return clazz;
	}

	protected void removeClass(AbstractAMLClassImpl<T> clazz) throws AMLValidationException {
		if (clazz.isReferenced())
			throw new AMLDocumentScopeInvalidException(this, "Document Item is still referenced");
		_removeClass(clazz.getName());
	}
	
	protected void _removeClass(String name) {
		classes.remove(name);
	}

	protected AMLValidationResultList validateRenameClass(String newName) {
		if (classes.containsKey(newName))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name already exists");
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	protected void _renameClass(AbstractAMLClassImpl<T> clazz, String newName) {
		T ic = classes.get(clazz.getName());
		if( ic == null){
			classes.put(newName, ic);
			return;
		}
		
		// preserve order
		LinkedHashMap<String, T> newClasses = new LinkedHashMap<String, T>();
		for (T oldClass : classes.values()) {
			if( oldClass == ic)
				newClasses.put(newName, oldClass);
			else
				newClasses.put(oldClass.getName(), oldClass);
		}
		classes = newClasses;
	}

	protected AMLValidationResultList validateDeleteClass(AbstractAMLClassImpl<T> clazz) {
		if (clazz.isReferenced())
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Document Item is still referenced");
		
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	public Iterable<? extends T> getClasses() {
		return new ReadOnlyIterable<T>(classes.values());
	}

	protected void _removeAllClasses() {
		classes.clear();
	}

	protected void _addClass(T clazz) {
		classes.put(clazz.getName(), clazz);
	}
	
	void _addClass(T clazz, T beforeElement, T afterElement) {
		if (beforeElement == null && afterElement == null) {
			this.classes.put(clazz.getName(), clazz);
			return;
		}
		Map<String, T> classes = new LinkedHashMap<String, T>();
		for (String name : this.classes.keySet()) {
			if (beforeElement != null && beforeElement.getName().equals(name))
				classes.put(clazz.getName(), clazz);
			classes.put(name, this.classes.get(name));
			if (afterElement != null && afterElement.getName().equals(name))
				classes.put(clazz.getName(), clazz);
		}
		this.classes.clear();
		this.classes.putAll(classes);
	}

	abstract String getClassPath();

	int getClassesCount() {
		return classes.size();
	}
	
	public boolean hasClasses(){
		return !classes.isEmpty();
	}

	@Override
	public void validateIfIsInDocumentScope(AMLDocumentElement elementToReparent, AMLDocumentImpl document,
			AMLValidationResultListImpl result,
			Set<AMLDocumentElement> checkedElements) {
		if (checkedElements.contains(this))
			return;
		checkedElements.add(this);
		
		if (!isDescendantOf(elementToReparent) &&!getDocumentManager().getDocumentScope(document).isInDocumentScope(this))
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "class library is not part of document scope"));
		
			
		for (T clazz : classes.values()) {
			clazz.validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}		
	}
	
	T _getClassBefore(T clazz) {
		ListIterator<String> iterator = new ArrayList<String>(classes.keySet()).listIterator();
		while (iterator.hasNext()) {
			if (clazz.getName().equals(iterator.next())) {
				T previous = (T) classes.get(iterator.previous());
				if (previous.equals(clazz))
					return null;
				else
					return previous;
			}
		}
		return null;		
	}
	
	T _getClassAfter(T clazz) {
		ListIterator<String> iterator = new ArrayList<String>(classes.keySet()).listIterator();
		while (iterator.hasNext()) {
			if (clazz.getName().equals(iterator.next())) {
				if (iterator.hasNext()) {
					T after = (T) classes.get(iterator.next());
					if (after.equals(clazz))
						return null;
					else
						return after;
				}
				return null;
			}
		}
		return null;		
	}

	@Override
	public boolean isReferencedByDocumentElements(AMLDocument document, AMLDocument documentToRemove,
			AMLValidationResultListImpl result) {
		for (T clazz : getClasses()) {
			clazz.isReferencedByDocumentElements(document, documentToRemove, result);
		}
		return result.isAnyOperationNotPermitted();
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkForm)
			throws AMLValidationException {
		for (T clazz : getClasses()) {
			clazz._doUnlink(unlinkForm);
		}		
	}
	
	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		for (T clazz : getClasses()) {
			clazz.doValidateDeepDelete(validator, baseElement, validationResultList);
		}		
	}

	@Override
	protected void _doDeepDelete(AMLDocumentElement baseElement)
			throws AMLValidationException {
		
		AMLValidationResultListImpl validationResultList = new AMLValidationResultListImpl();
		doValidateDeepDelete(null, baseElement, validationResultList);
		if (validationResultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResultList);
		
		while (getClasses().iterator().hasNext()) {
			getClasses().iterator().next()._doDeepDelete(baseElement);
		}	
		delete();
	}
	
	@Override
	public void deepCopy(AMLDocumentElement rootElement, AMLDocumentElement rootCopyElement, 
			AMLDocumentElement original, Map<AMLDocumentElement, AMLDocumentElement> mapping) throws AMLValidationException {
		
		mapping.put(original, this);
		
		for (T clazz : ((AbstractAMLClassContainer<T>) original).getClasses()) {
			if (clazz.equals(rootCopyElement))
				continue;
			T copyClass = createClass(((AMLRenamable) clazz).getName());
			copyClass.deepCopy(rootElement, rootCopyElement, clazz, mapping);
		}	
	}
}
