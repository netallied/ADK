/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.automationml.DocumentLocation;
import org.automationml.URLDocumentLocation;
import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentChangeListener;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLDocumentTraverser;
import org.automationml.aml.AMLDocumentURLResolver;
import org.automationml.aml.AMLInstanceHierarchy;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInterfaceClassLibrary;
import org.automationml.aml.AMLInvalidReferenceException;
import org.automationml.aml.AMLNameAlreadyInUseException;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLRoleClassLibrary;
import org.automationml.aml.AMLSystemUnitClass;
import org.automationml.aml.AMLSystemUnitClassLibrary;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractCreateDocumentElementChange;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.AbstractDocumentElementChange;
import org.automationml.internal.Change;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;
import org.automationml.internal.ReadOnlyIterable;
import org.automationml.internal.aml.persistence.AMLSerializer;

public class AMLDocumentImpl extends AMLElementImpl implements AMLDocument {

	private class DeleteDocumentChange extends AbstractDeleteDocumentElementChange<AMLDocumentImpl> {

		@Override
		public AMLDocument getDocument() {
			return null;
		}

		public DeleteDocumentChange(AMLDocumentImpl element) {
			super(element);
		}

		@Override
		public void undo() throws Exception {
			AMLDocumentImpl documentElement = session.createAMLDocument();
			identifier.setDocumentElement(documentElement);
		}

		@Override
		public void notifyChangeListenersBeforeRedo() {
			AMLDocument document = (AMLDocument) getDocumentElement();
			session.documentRemoving(document);
		}

		@Override
		public void notifyChangeListenersAfterUndo() {
			AMLDocument document = (AMLDocument) getDocumentElement();
			session.documentAdded(document);
		}
	}

	private static class CreateInstanceHierarchyChange extends AbstractCreateDocumentElementChange {
		private String name;

		public CreateInstanceHierarchyChange(AMLInstanceHierarchyImpl documentElement) {
			super(documentElement);
			this.name = documentElement.getName();
		}

		@Override
		public void redo() throws Exception {
			AMLDocumentImpl document = (AMLDocumentImpl) parentIdentifier.getDocumentElement();
			AMLInstanceHierarchyImpl lib = document._createInstanceHierarchy(name);
			identifier.setDocumentElement(lib);
		}
	}

	private static class CreateInterfaceClassLibraryChange extends AbstractCreateDocumentElementChange {
		private String name;

		public CreateInterfaceClassLibraryChange(AbstractAMLClassLibraryImpl documentElement) {
			super(documentElement);
			this.name = documentElement.getName();
		}

		@Override
		public void redo() throws Exception {
			AMLDocumentImpl document = (AMLDocumentImpl) parentIdentifier.getDocumentElement();
			AMLInterfaceClassLibraryImpl lib = document._createInterfaceClassLibrary(name);
			identifier.setDocumentElement(lib);
		}
	}

	private static class CreateRoleClassLibraryChange extends AbstractCreateDocumentElementChange {
		private String name;

		public CreateRoleClassLibraryChange(AbstractAMLClassLibraryImpl documentElement) {
			super(documentElement);
			this.name = documentElement.getName();
		}

		@Override
		public void redo() throws Exception {
			AMLDocumentImpl document = (AMLDocumentImpl) parentIdentifier.getDocumentElement();
			AMLRoleClassLibraryImpl lib = document._createRoleClassLibrary(name);
			identifier.setDocumentElement(lib);
		}
	}

	private static class CreateSystemUnitClassLibraryChange extends AbstractCreateDocumentElementChange {
		private String name;

		public CreateSystemUnitClassLibraryChange(AbstractAMLClassLibraryImpl documentElement) {
			super(documentElement);
			this.name = documentElement.getName();
		}

		@Override
		public void redo() throws Exception {
			AMLDocumentImpl document = (AMLDocumentImpl) parentIdentifier.getDocumentElement();
			AMLSystemUnitClassLibraryImpl lib = document._createSystemUnitClassLibrary(name);
			identifier.setDocumentElement(lib);
		}
	}

	private abstract static class AbstractExternalReferenceChange extends AbstractDocumentElementChange<AbstractAMLDocumentElement> {
		protected Identifier referencedIdentifier;

		public AbstractExternalReferenceChange(AbstractAMLDocumentElement element, AbstractAMLDocumentElement referencedElement) {
			super(element);
			this.referencedIdentifier = getIdentifierManager().getIdentifier(referencedElement);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
		}

		@Override
		protected void _delete() {
			referencedIdentifier.release();
		}

		protected void removeExternalReference() throws Exception {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement();
			AMLDocument explicitExternalReference = (AMLDocument) referencedIdentifier.getDocumentElement();
			document._removeExplicitExternalReference(explicitExternalReference);
		}

		protected void addExternalReference() throws Exception {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement();
			AMLDocument explicitExternalReference = (AMLDocument) referencedIdentifier.getDocumentElement();
			document._addExplicitExternalReference(explicitExternalReference);
		}

		@Override
		public boolean mergeInto(Change change) {
			return true;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((referencedIdentifier == null) ? 0 : referencedIdentifier.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			AbstractExternalReferenceChange other = (AbstractExternalReferenceChange) obj;
			if (referencedIdentifier == null) {
				if (other.referencedIdentifier != null) {
					return false;
				}
			} else if (!referencedIdentifier.equals(other.referencedIdentifier)) {
				return false;
			}
			return true;
		}
	}

	private static class AddExternalReferenceChange extends AbstractExternalReferenceChange {

		public AddExternalReferenceChange(AbstractAMLDocumentElement element, AbstractAMLDocumentElement referencedElement) {
			super(element, referencedElement);
		}

		@Override
		public void undo() throws Exception {
			removeExternalReference();
		}

		@Override
		public void redo() throws Exception {
			addExternalReference();
		}
	}

	private static class RemoveExternalReferenceChange extends AbstractExternalReferenceChange {
		public RemoveExternalReferenceChange(AbstractAMLDocumentElement element, AbstractAMLDocumentElement referencedElement) {
			super(element, referencedElement);
		}

		@Override
		public void undo() throws Exception {
			addExternalReference();
		}

		@Override
		public void redo() throws Exception {
			removeExternalReference();
		}
	}

	static final String LIBRARY_PATH_SEPARATOR = "/";
	// TODO setze default-values für ...
	private String writerName;
	private String writerID;
	private String writerVendor;
	private String writerVendorURL;
	private String writerVersion;
	private String writerRelease;
	private Date lastWritingDate;
	private String writerProjectTitle;
	private String writerProjectID;

	private AMLSessionImpl session;

	AMLDocumentImpl(AMLSessionImpl session) {
		this.session = session;
	}

	private Map<String, AMLInterfaceClassLibrary> interfaceClassLibraries = new LinkedHashMap<String, AMLInterfaceClassLibrary>();
	private Map<String, AMLSystemUnitClassLibrary> systemUnitClassLibraries = new LinkedHashMap<String, AMLSystemUnitClassLibrary>();
	private Map<String, AMLRoleClassLibrary> roleClassLibraries = new LinkedHashMap<String, AMLRoleClassLibrary>();
	private Map<String, AMLInstanceHierarchy> instanceHierarchies = new LinkedHashMap<String, AMLInstanceHierarchy>();
	private boolean dirty;
	private int changesCount;

	public interface DocumentItemTraverser {
		void traverse(AMLDocumentElement element) throws Exception;
	}

	public AMLValidationResultList validateCreateInterfaceClassLibrary(String name) throws AMLValidationException {
		return _validateCreateInterfaceClassLibrary(name);
	}

	private AMLValidationResultList _validateCreateInterfaceClassLibrary(String name) throws AMLValidationException {
		AMLDocumentScope scope = getDocumentManager().getDocumentScope(this);
		if (scope.isInterfaceClassLibraryNameDefined(name))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name already exists");

		AMLValidator validator = getSession().getValidator();
		if (validator != null)
			return validator.validateInterfaceClassLibraryCreate(this, name);

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	private AMLValidationResultList _validateCreateRoleClassLibrary(String name) throws AMLValidationException {
		AMLDocumentScope scope = getDocumentManager().getDocumentScope(this);
		if (scope.isRoleClassLibraryNameDefined(name))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name already exists");

		AMLValidator validator = getSession().getValidator();
		if (validator != null)
			return validator.validateRoleClassLibraryCreate(this, name);

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	private AMLValidationResultList _validateCreateSystemUnitClassLibrary(String name) throws AMLValidationException {
		AMLDocumentScope scope = getDocumentManager().getDocumentScope(this);
		if (scope.isSystemUnitClassLibraryNameDefined(name))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name already exists");

		AMLValidator validator = getSession().getValidator();
		if (validator != null)
			return validator.validateSystemUnitClassLibraryCreate(this, name);

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public AMLInterfaceClassLibrary createInterfaceClassLibrary(final String name) throws AMLValidationException {
		return createInterfaceClassLibrary(name, null);
	}

	AMLInterfaceClassLibraryImpl _createInterfaceClassLibrary(final String libraryName) throws AMLDocumentScopeInvalidException {
		getDocumentManager().addInterfaceClassLibraryName(this, libraryName);

		// Modell modifizieren
		AMLInterfaceClassLibraryImpl library = new AMLInterfaceClassLibraryImpl(this, libraryName);
		interfaceClassLibraries.put(libraryName, library);

		return library;
	}

	@Override
	public AMLRoleClassLibrary createRoleClassLibrary(String libraryName) throws AMLValidationException {
		return createRoleClassLibrary(libraryName, null);
	}

	AMLRoleClassLibraryImpl _createRoleClassLibrary(final String libraryName) throws AMLNameAlreadyInUseException, AMLDocumentScopeInvalidException {
		getDocumentManager().addRoleClassLibraryName(this, libraryName);

		// Modell modifizieren
		AMLRoleClassLibraryImpl library = new AMLRoleClassLibraryImpl(this, libraryName);
		roleClassLibraries.put(libraryName, library);

		// Changelistener notifizieren
		// TODO

		return library;
	}

	@Override
	public AMLSystemUnitClassLibrary createSystemUnitClassLibrary(String libraryName) throws AMLValidationException {
		return createSystemUnitClassLibrary(libraryName, null);
	}

	AMLSystemUnitClassLibraryImpl _createSystemUnitClassLibrary(final String libraryName) throws AMLValidationException {
		getDocumentManager().addSystemUnitClassLibraryName(this, libraryName);

		// Modell modifizieren
		AMLSystemUnitClassLibraryImpl library = new AMLSystemUnitClassLibraryImpl(this, libraryName);
		systemUnitClassLibraries.put(libraryName, library);

		// Changelistener notifizieren
		// TODO

		return library;
	}

	void _renameInterfaceClassLibrary(AbstractAMLClassLibraryImpl library, final String newName) throws AMLDocumentScopeInvalidException {

		boolean notInstanceOfInterfaceClassLibrary = !(library instanceof AMLInterfaceClassLibrary);
		if (notInstanceOfInterfaceClassLibrary) {
			// FIXME exception
			throw new RuntimeException();
		}

		getDocumentManager().renameInterfaceClassLibrary(library, newName);
		String oldName = library.getName();
		interfaceClassLibraries.remove(oldName);
		interfaceClassLibraries.put(newName, (AMLInterfaceClassLibrary) library);
	}

	void _renameRoleClassLibrary(AbstractAMLClassLibraryImpl library, final String newName) throws AMLDocumentScopeInvalidException {

		boolean notInstanceOfRoleClassLibrary = !(library instanceof AMLRoleClassLibrary);
		if (notInstanceOfRoleClassLibrary) {
			// FIXME exception
			throw new RuntimeException();
		}

		getDocumentManager().renameRoleClassLibrary(library, newName);
		String oldName = library.getName();
		roleClassLibraries.remove(oldName);
		roleClassLibraries.put(newName, (AMLRoleClassLibrary) library);
	}

	void _renameSystemUnitClassLibrary(AbstractAMLClassLibraryImpl library, final String newName) throws AMLDocumentScopeInvalidException {

		boolean notInstanceOfSystemUnitClassLibrary = !(library instanceof AMLSystemUnitClassLibrary);
		if (notInstanceOfSystemUnitClassLibrary) {
			// FIXME exception
			throw new RuntimeException();
		}

		getDocumentManager().renameSystemUnitClassLibrary(library, newName);
		String oldName = library.getName();
		systemUnitClassLibraries.remove(oldName);
		systemUnitClassLibraries.put(newName, (AMLSystemUnitClassLibrary) library);
	}

	@Override
	public AMLInterfaceClassLibrary getInterfaceClassLibrary(String libraryName) {
		assertNotDeleted();
		AMLInterfaceClassLibrary interfaceClassLibrary = interfaceClassLibraries.get(libraryName);
		return interfaceClassLibrary;
	}

	@Override
	public AMLRoleClassLibrary getRoleClassLibrary(String libraryName) {
		assertNotDeleted();
		AMLRoleClassLibrary roleClassLibrary = roleClassLibraries.get(libraryName);
		return roleClassLibrary;
	}

	@Override
	public AMLSystemUnitClassLibrary getSystemUnitClassLibrary(String libraryName) {
		assertNotDeleted();
		AMLSystemUnitClassLibrary systemUnitClassLibrary = systemUnitClassLibraries.get(libraryName);
		return systemUnitClassLibrary;
	}

	@Override
	public void save(AMLDocumentURLResolver urlResolver) throws Exception {
		assertNotDeleted();
		OutputStream outputStream = getDocumentLocation().createOutputStream();
		try {
			AMLSerializer.serialize(this, urlResolver, outputStream);
		} finally {
			if (outputStream != null)
				outputStream.close();
		}
		unsetDirty();
	}

	@Override
	public void saveAs(URL newUrl, AMLDocumentURLResolver urlResolver) throws Exception {
		assertNotDeleted();
		getDocumentManager().relocate(this, newUrl);
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(newUrl.toURI())));
		try {
			AMLSerializer.serialize(this, urlResolver, outputStream);
		} finally {
			if (outputStream != null)
				outputStream.close();
		}
		beginDocumentChanges();
		unsetDirty();

		endDocumentChanges();
	}

	public void iterate(AMLDocumentTraverser traverser, int depthfirst) {
		assertNotDeleted();
		// TODO implement
	}

	@Override
	public void delete() throws AMLValidationException {
		if (isDeleted())
			return;

		if (!interfaceClassLibraries.isEmpty() || !systemUnitClassLibraries.isEmpty() || !roleClassLibraries.isEmpty())
			throw new AMLDocumentScopeInvalidException(this, "Document is not empty");

		getDocumentManager().assertDocumentNotReferenced(this);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			DeleteDocumentChange change = new DeleteDocumentChange(this);
			getSavepointManager().addChange(change);
		}

		_delete();
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		getDocumentManager().unregisterDocument(this);
	}

	@Override
	public void setWriterName(String writerName) {
		// if (writerName == null) {
		// if (validatorCallback != null)
		// validatorCallback.conflictdetected(this, "writer name null", 999);
		// throw new Exception();
		// }
		this.writerName = writerName;
		// validator.validateDocument(this, null);
	}

	// oder
	@Override
	public void setWriterVendor(String writerVendor) {
		// if (writerVendor == null) {
		// throw new Exception("writer name null");
		// }
		this.writerVendor = writerVendor;
		// validator.validateDocument(this);
	}

	@Override
	public void setWriterID(String string) {
		writerID = string;
		incrementChangesCount();
	}

	@Override
	public void setWriterVendorURL(String string) {
		writerVendorURL = string;
		incrementChangesCount();
	}

	@Override
	public void setWriterVersion(String string) {
		writerVersion = string;
		incrementChangesCount();
	}

	@Override
	public void setWriterRelease(String string) {
		writerRelease = string;
		incrementChangesCount();
	}

	@Override
	public void setLastWritingDate(Date date) {
		lastWritingDate = date;
	}

	@Override
	public void setWriterProjectTitle(String string) {
		writerProjectTitle = string;
		incrementChangesCount();
	}

	@Override
	public void setWriterProjectID(String string) {
		writerProjectID = string;
		incrementChangesCount();
	}

	@Override
	public String getWriterName() {
		return writerName;
	}

	@Override
	public String getWriterID() {
		return writerID;
	}

	@Override
	public String getWriterVendor() {
		return writerVendor;
	}

	@Override
	public String getWriterVendorURL() {
		return writerVendorURL;
	}

	@Override
	public String getWriterVersion() {
		return writerVersion;
	}

	@Override
	public String getWriterRelease() {
		return writerRelease;
	}

	@Override
	public Date getLastWritingDate() {
		return lastWritingDate;
	}

	@Override
	public String getWriterProjectTitle() {
		return writerProjectTitle;
	}

	@Override
	public String getWriterProjectID() {
		return writerProjectID;
	}

	@Override
	public AMLInterfaceClass getInterfaceClassByPath(String path) throws AMLValidationException {
		assertNotDeleted();

		int i = path.indexOf(LIBRARY_PATH_SEPARATOR);

		if (i == -1)
			throw new AMLInvalidReferenceException(this, "Invalid Reference: " + path);

		final String libraryName = path.substring(0, i);
		final String className = path.substring(i + 1, path.length());

		AMLDocumentScope scope = getDocumentManager().getDocumentScope(this);
		AMLInterfaceClassLibraryImpl interfaceClassLibrary = (AMLInterfaceClassLibraryImpl) scope.getInterfaceClassLibrary(libraryName);
		if (interfaceClassLibrary == null)
			return null;

		// AMLInterfaceClass interfaceClass = interfaceClassLibrary.getInterfaceClass(className);
		AMLInterfaceClass interfaceClass = (AMLInterfaceClass) interfaceClassLibrary.getClassByPath(className);
		if (interfaceClass != null)
			return interfaceClass;

		return null;
	}

	@Override
	public AMLRoleClass getRoleClassByPath(String path) {
		assertNotDeleted();

		int i = path.indexOf(LIBRARY_PATH_SEPARATOR);

		if (i == -1)
			return null;
			//throw new AMLInvalidReferenceException(this, "Invalid Reference: " + path);

		final String libraryName = path.substring(0, i);
		final String className = path.substring(i + 1, path.length());

		AMLDocumentScope scope = getDocumentManager().getDocumentScope(this);
		AMLRoleClassLibraryImpl roleClassLibrary = (AMLRoleClassLibraryImpl) scope.getRoleClassLibrary(libraryName);
		if (roleClassLibrary == null)
			return null;

		AMLRoleClass roleClass = (AMLRoleClass) roleClassLibrary.getClassByPath(className);
		if (roleClass != null)
			return roleClass;

		return null;
	}

	@Override
	public AMLSystemUnitClass getSystemUnitClassByPath(String path) throws AMLValidationException {
		assertNotDeleted();

		int i = path.indexOf(LIBRARY_PATH_SEPARATOR);

		if (i == -1)
			return null;
		// throw new AMLInvalidReferenceException(this, "Invalid Reference: " + path);

		final String libraryName = path.substring(0, i);
		final String className = path.substring(i + 1, path.length());

		AMLDocumentScope scope = getDocumentManager().getDocumentScope(this);
		AMLSystemUnitClassLibraryImpl systemUnitClassLibrary = (AMLSystemUnitClassLibraryImpl) scope.getSystemUnitClassLibrary(libraryName);
		if (systemUnitClassLibrary == null)
			return null;

		AMLSystemUnitClass systemUnitClass = (AMLSystemUnitClass) systemUnitClassLibrary.getClassByPath(className);
		if (systemUnitClass != null)
			return systemUnitClass;

		return null;
	}

	@Override
	public void addExplicitExternalReference(AMLDocument explicitExternalReference) throws AMLValidationException {
		assertNotDeleted();
		assertSameSession(explicitExternalReference);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			AddExternalReferenceChange change = new AddExternalReferenceChange(this, (AMLDocumentImpl) explicitExternalReference);
			getSavepointManager().addChange(change);
		}

		_addExplicitExternalReference(explicitExternalReference);
		notifyElementCreated(explicitExternalReference, this);
	}

	void _addExplicitExternalReference(AMLDocument explicitExternalReference) throws AMLValidationException {
		getDocumentManager().addExplicitDocumentReference(this, explicitExternalReference);
	}

	void _addInstanceHierarchy(AMLInstanceHierarchyImpl instanceHierarchy, AMLInstanceHierarchyImpl beforeElement, AMLInstanceHierarchyImpl afterElement){
		if (beforeElement == null && afterElement == null) {
			this.instanceHierarchies.put(instanceHierarchy.getName(), instanceHierarchy);
			return;
		}
		Map<String, AMLInstanceHierarchy> instanceHierarchies = new LinkedHashMap<String, AMLInstanceHierarchy>();
		for (String name : this.instanceHierarchies.keySet()) {
			if (beforeElement != null && beforeElement.getName().equals(name))
				instanceHierarchies.put(instanceHierarchy.getName(), instanceHierarchy);
			instanceHierarchies.put(name, this.instanceHierarchies.get(name));
			if (afterElement != null && afterElement.getName().equals(name))
				instanceHierarchies.put(instanceHierarchy.getName(), instanceHierarchy);
		}
		this.instanceHierarchies.clear();
		this.instanceHierarchies.putAll(instanceHierarchies);
	}

	@Override
	public void removeExplicitExternalReference(AMLDocument explicitExternalReference) throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList validationResult = validateRemoveExplicitExternalReference(explicitExternalReference);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			RemoveExternalReferenceChange change = new RemoveExternalReferenceChange(this, (AMLDocumentImpl) explicitExternalReference);
			getSavepointManager().addChange(change);
		}

		_removeExplicitExternalReference(explicitExternalReference);
		notifyElementDeleting(explicitExternalReference, this);
	}

	void _removeExplicitExternalReference(AMLDocument explicitExternalReference) throws AMLValidationException {
		getDocumentManager().removeExplicitDocumentReference(this, explicitExternalReference);
	}

	@Override
	public Iterable<AMLDocument> getExplicitlyReferencedDocuments() {
		assertNotDeleted();
		return getDocumentManager().getExplicitlyReferencedDocuments(this);
	}

	public void setOriginalURL(URL url) {
		DocumentLocation location = new URLDocumentLocation(url);
		// TODO
		getDocumentManager().registerDocument(this, location);
	}

	public void setResolvedURL(URL url) {
		DocumentLocation location = new URLDocumentLocation(url);
		// TODO
		getDocumentManager().registerDocument(this, location);
	}

	@Override
	public Iterable<AMLInterfaceClassLibrary> getInterfaceClassLibraries() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLInterfaceClassLibrary>(interfaceClassLibraries.values());
	}

	@Override
	public Iterable<AMLRoleClassLibrary> getRoleClassLibraries() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLRoleClassLibrary>(roleClassLibraries.values());
	}

	@Override
	public Iterable<AMLSystemUnitClassLibrary> getSystemUnitClassLibraries() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLSystemUnitClassLibrary>(systemUnitClassLibraries.values());
	}

	@Override
	public AMLDocumentImpl getDocument() {
		assertNotDeleted();
		return this;
	}

	void _removeInterfaceClassLibrary(String libraryName) {
		interfaceClassLibraries.remove(libraryName);
	}

	void _removeRoleClassLibrary(String libraryName) {
		roleClassLibraries.remove(libraryName);
	}

	void _removeSystemUnitClassLibrary(String libraryName) {
		systemUnitClassLibraries.remove(libraryName);
	}

	@Override
	public AMLSessionImpl getSession() {
		assertNotDeleted();
		return session;
	}

	@Override
	public AMLDocumentElement getParent() {
		assertNotDeleted();
		return null;
	}

	@Override
	public void addDocumentChangeListener(AMLDocumentChangeListener changeListener) {
		session.getDocumentManager().addDocumentChangeListener(this, changeListener);
	}

	@Override
	public void removeDocumentChangeListener(AMLDocumentChangeListener changeListener) {
		session.getDocumentManager().removeDocumentChangeListener(this, changeListener);
	}

	public void notifyElementCreated(AMLDocumentElement documentElement, AMLDocumentElement parent) {
		session.getDocumentManager().notifyElementCreated(this, documentElement, parent);
	}

	public void notifyElementModified(AMLDocumentElement documentElement) {
		session.getDocumentManager().notifyElementModified(this, documentElement);
	}

	public void notifyElementDeleting(AMLDocumentElement documentElement, AMLDocumentElement oldParent) {
		session.getDocumentManager().notifyElementDeleting(documentElement, oldParent);
	}

	public void notifyElementReparented(AMLDocumentElement documentElement, AMLDocumentElement oldParent, AMLDocumentElement newParent) {
		session.getDocumentManager().notifyElementReparented(documentElement, oldParent, newParent);
	}

	public void notifyDocumentDirtyStateChanged() {
		session.getDocumentManager().notifyDocumentChangeDirtyStateChanged(this);
	}

	public void beginDocumentChanges() {
		session.getDocumentManager().notifyDocumentChangeTransactionBegin(this);
	}

	public void endDocumentChanges() {
		session.getDocumentManager().notifyDocumentChangeTransactionEnd(this);
	}

	@Override
	public DocumentLocation getDocumentLocation() {
		return session.getDocumentManager().getDocumentLocation(this);
	}

	@Override
	public Iterable<AMLDocument> getReferencedDocuments() {
		return session.getDocumentManager().getReferencedDocuments(this);
	}

	@Override
	public int getReferencedDocumentsCount() {
		return session.getDocumentManager().getReferencedDocumentsCount(this);
	}

	@Override
	public AMLValidationResultList validateCreateRoleClassLibrary(String name) throws AMLValidationException {
		return _validateCreateRoleClassLibrary(name);
	}

	@Override
	public AMLValidationResultList validateCreateSystemUnitClassLibrary(String name) throws AMLValidationException {
		return _validateCreateSystemUnitClassLibrary(name);
	}

	public void notifyElementValidated(AMLValidationResultList validationResult) {
		session.getDocumentManager().notifyElementValidated(validationResult);
	}

	@Override
	public AMLInstanceHierarchy getInstanceHierarchy(String name) {
		assertNotDeleted();
		AMLInstanceHierarchy instanceHierarchy = instanceHierarchies.get(name);
		return instanceHierarchy;
	}

	@Override
	public Iterable<AMLInstanceHierarchy> getInstanceHierarchies() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLInstanceHierarchy>(instanceHierarchies.values());
	}

	@Override
	public int getInstanceHierarchiesCount() {
		assertNotDeleted();
		return instanceHierarchies.values().size();
	}

	@Override
	public AMLInstanceHierarchy createInstanceHierarchy(String name) throws AMLValidationException {
		return createInstanceHierarchy(name, null);
	}

	AMLInstanceHierarchyImpl _createInstanceHierarchy(String name) {

		// getDocumentManager().addInterfaceClassLibraryName(this, libraryName);

		// Modell modifizieren
		AMLInstanceHierarchyImpl instanceHierarchy = new AMLInstanceHierarchyImpl(this, name);
		instanceHierarchies.put(name, instanceHierarchy);

		// Changelistener notifizieren
		// TODO

		return instanceHierarchy;
	}

	private AMLValidationResultList _validateCreateInstanceHierarchy(String name) {

		if (getInstanceHierarchy(name) != null)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name already exists");

		AMLValidator validator = getSession().getValidator();
		if (validator != null)
			return validator.validateInstanceHierarchyCreate(this, name);

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	boolean _hasInstanceHierarchy(String newName) {
		return instanceHierarchies.containsKey(newName);
	}

	void _renameInstanceHierarchy(AMLInstanceHierarchyImpl instanceHierarchy, String newName) throws AMLDocumentScopeInvalidException {

		// getDocumentManager().renameInterfaceClassLibrary(library, newName);
		String oldName = instanceHierarchy.getName();
		instanceHierarchies.remove(oldName);
		instanceHierarchies.put(newName, instanceHierarchy);
	}

	void _removeInstanceHierarchy(String name) {
		instanceHierarchies.remove(name);
	}

	@Override
	public AMLValidationResultList validateCreateInstanceHierarchy(String name) {
		return _validateCreateInstanceHierarchy(name);
	}

	@Override
	public int getInterfaceClassLibrariesCount() {
		return interfaceClassLibraries.size();
	}

	@Override
	public int getRoleClassLibrariesCount() {
		return roleClassLibraries.size();
	}

	@Override
	public int getSystemUnitClassLibrariesCount() {
		return systemUnitClassLibraries.size();
	}

	public void incrementChangesCount() {
		boolean wasDirty = isDirty();
		changesCount++;
		if (wasDirty != isDirty())
			notifyDocumentDirtyStateChanged();
	}

	public void decrementChangesCount() {
		boolean wasDirty = isDirty();
		changesCount--;
		if (wasDirty != isDirty())
			notifyDocumentDirtyStateChanged();
	}

	@Override
	public boolean isDirty() {
		return changesCount != 0;
	}

	@Override
	public void unsetDirty() {
		if (!isDirty())
			return;
		changesCount = 0;
		notifyDocumentDirtyStateChanged();
	}

	@Override
	public void setDirty() {
		if (isDirty())
			return;
		changesCount++;
		notifyDocumentDirtyStateChanged();
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public void reparent(AMLDocumentElement oldParentElement, AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) throws AMLValidationException {

		if (isDeleted())
			return;

		AMLValidationResultList validationResult = validateReparent(oldParentElement, newParentElement, beforeElement, afterElement);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		// 1. remove document from parent
		getDocumentManager().removeExplicitDocumentReference((AMLDocument)oldParentElement, this);

		// 2. set new document reference
		getDocumentManager().addExplicitDocumentReference((AMLDocument)newParentElement, this, (AMLDocument)beforeElement, (AMLDocument)afterElement);
	}

	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement, AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		if (!(newParentElement instanceof AMLDocument))
			return new AMLValidationResultListImpl(this, Severity.VALIDATION_ERROR, "New parent Element is not a document");

		if (newParentElement != null && newParentElement.getSession() != getSession())
			return new AMLValidationResultListImpl(this, Severity.VALIDATION_ERROR, "New parent Element does not belong to this Session");
		if (beforeElement != null && beforeElement.getSession() != getSession())
			return new AMLValidationResultListImpl(this, Severity.VALIDATION_ERROR, "Before Element does not belong to this Session");
		if (afterElement != null && afterElement.getSession() != getSession())
			return new AMLValidationResultListImpl(this, Severity.VALIDATION_ERROR, "After Element does not belong to this Session");

		try {
			getDocumentManager().checkIfAddingDocumentCreatesACycle((AMLDocument)newParentElement, this);
		} catch (AMLDocumentScopeInvalidException e) {
			return new AMLValidationResultListImpl(this, Severity.VALIDATION_ERROR, "New parent creates a cycle");
		}


//		if (validator != null) {
//			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateReparent(this);
//			validationResult2.assertNotInternalValidationSeverity();
//			return validationResult2;
//		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	AMLInstanceHierarchyImpl _getInstanceHierarchyBefore(AMLInstanceHierarchyImpl instanceHierarchy) {
		ListIterator<String> iterator = new ArrayList<String>(instanceHierarchies.keySet()).listIterator();
		while (iterator.hasNext()) {
			if (instanceHierarchy.getName().equals(iterator.next())) {
				AMLInstanceHierarchyImpl previous = (AMLInstanceHierarchyImpl) instanceHierarchies.get(iterator.previous());
				if (previous.equals(instanceHierarchy))
					return null;
				else
					return previous;
			}
		}
		return null;
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
	public void validateIfIsInDocumentScope(AMLDocumentElement elementToReparent,
			AMLDocumentImpl document, AMLValidationResultListImpl result, Set<AMLDocumentElement> checkedElements) {
		if (checkedElements.contains(this))
			return;
		checkedElements.add(this);
		for (AMLInstanceHierarchy instanceHierarchy : instanceHierarchies.values()) {
			((AMLInstanceHierarchyImpl)instanceHierarchy).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
	}

	void _addRoleClassLibrary(
			AMLRoleClassLibraryImpl roleClassLibrary,
			AMLRoleClassLibraryImpl beforeElement,
			AMLRoleClassLibraryImpl afterElement) {

		if (beforeElement == null && afterElement == null) {
			this.roleClassLibraries.put(roleClassLibrary.getName(), roleClassLibrary);
			return;
		}
		Map<String, AMLRoleClassLibrary> roleClassLibraries = new LinkedHashMap<String, AMLRoleClassLibrary>();
		for (String name : this.roleClassLibraries.keySet()) {
			if (beforeElement != null && beforeElement.getName().equals(name))
				roleClassLibraries.put(roleClassLibrary.getName(), roleClassLibrary);
			roleClassLibraries.put(name, this.roleClassLibraries.get(name));
			if (afterElement != null && afterElement.getName().equals(name))
				roleClassLibraries.put(roleClassLibrary.getName(), roleClassLibrary);
		}
		this.roleClassLibraries.clear();
		this.roleClassLibraries.putAll(roleClassLibraries);
	}

	AMLDocumentElement _getRoleClassLibraryBefore(
			AMLRoleClassLibraryImpl roleClassLibray) {
		ListIterator<String> iterator = new ArrayList<String>(roleClassLibraries.keySet()).listIterator();
		while (iterator.hasNext()) {
			if (roleClassLibray.getName().equals(iterator.next())) {
				AMLRoleClassLibraryImpl previous = (AMLRoleClassLibraryImpl) roleClassLibraries.get(iterator.previous());
				if (previous.equals(roleClassLibray))
					return null;
				else
					return previous;
			}
		}
		return null;
	}

	void _addInterfaceClassLibrary(
			AMLInterfaceClassLibraryImpl interfaceClassLibrary,
			AMLInterfaceClassLibraryImpl beforeElement,
			AMLInterfaceClassLibraryImpl afterElement) {
		if (beforeElement == null && afterElement == null) {
			this.interfaceClassLibraries.put(interfaceClassLibrary.getName(), interfaceClassLibrary);
			return;
		}
		Map<String, AMLInterfaceClassLibrary> interfaceClassLibraries = new LinkedHashMap<String, AMLInterfaceClassLibrary>();
		for (String name : this.interfaceClassLibraries.keySet()) {
			if (beforeElement != null && beforeElement.getName().equals(name))
				interfaceClassLibraries.put(interfaceClassLibrary.getName(), interfaceClassLibrary);
			interfaceClassLibraries.put(name, this.interfaceClassLibraries.get(name));
			if (afterElement != null && afterElement.getName().equals(name))
				interfaceClassLibraries.put(interfaceClassLibrary.getName(), interfaceClassLibrary);
		}
		this.interfaceClassLibraries.clear();
		this.interfaceClassLibraries.putAll(interfaceClassLibraries);
	}

	AMLDocumentElement _getInterfaceClassLibraryBefore(
			AMLInterfaceClassLibraryImpl interfaceClassLibrary) {
		ListIterator<String> iterator = new ArrayList<String>(interfaceClassLibraries.keySet()).listIterator();
		while (iterator.hasNext()) {
			if (interfaceClassLibrary.getName().equals(iterator.next())) {
				AMLInterfaceClassLibraryImpl previous = (AMLInterfaceClassLibraryImpl) interfaceClassLibraries.get(iterator.previous());
				if (previous.equals(interfaceClassLibrary))
					return null;
				else
					return previous;
			}
		}
		return null;
	}

	void _addSystemUnitClassLibrary(
			AMLSystemUnitClassLibraryImpl systemUnitClassLibrary,
			AMLSystemUnitClassLibraryImpl beforeElement,
			AMLSystemUnitClassLibraryImpl afterElement) {
		if (beforeElement == null && afterElement == null) {
			this.systemUnitClassLibraries.put(systemUnitClassLibrary.getName(), systemUnitClassLibrary);
			return;
		}
		Map<String, AMLSystemUnitClassLibrary> systemUnitClassLibraries = new LinkedHashMap<String, AMLSystemUnitClassLibrary>();
		for (String name : this.systemUnitClassLibraries.keySet()) {
			if (beforeElement != null && beforeElement.getName().equals(name))
				systemUnitClassLibraries.put(systemUnitClassLibrary.getName(), systemUnitClassLibrary);
			systemUnitClassLibraries.put(name, this.systemUnitClassLibraries.get(name));
			if (afterElement != null && afterElement.getName().equals(name))
				systemUnitClassLibraries.put(systemUnitClassLibrary.getName(), systemUnitClassLibrary);
		}
		this.systemUnitClassLibraries.clear();
		this.systemUnitClassLibraries.putAll(systemUnitClassLibraries);
	}

	AMLDocumentElement _getSystemUnitClassLibraryBefore(
			AMLSystemUnitClassLibraryImpl systemUnitClassLibrary) {
		ListIterator<String> iterator = new ArrayList<String>(systemUnitClassLibraries.keySet()).listIterator();
		while (iterator.hasNext()) {
			if (systemUnitClassLibrary.getName().equals(iterator.next())) {
				AMLSystemUnitClassLibraryImpl previous = (AMLSystemUnitClassLibraryImpl) systemUnitClassLibraries.get(iterator.previous());
				if (previous.equals(systemUnitClassLibrary))
					return null;
				else
					return previous;
			}
		}
		return null;
	}

	@Override
	public AMLValidationResultList validateRemoveExplicitExternalReference(
			AMLDocument referencedDocument) {
		if (getDocumentManager().isDocumentReferencedBy(this, referencedDocument, referencedDocument))
			return new AMLValidationResultListImpl(this, Severity.OK, "");

		AMLValidationResultListImpl results = new AMLValidationResultListImpl();

		((AMLDocumentImpl)referencedDocument).isReferencedByDocumentElements(this, referencedDocument, results);

		return results;
	}

	@Override
	public boolean isReferencedByDocumentElements(AMLDocument document, AMLDocument documentToIgnore,
			AMLValidationResultListImpl results) {

		for (AMLInterfaceClassLibrary interfaceClass : getInterfaceClassLibraries()) {
			((AbstractAMLDocumentElement) interfaceClass).isReferencedByDocumentElements(document, documentToIgnore, results);
		}
		for (AMLRoleClassLibrary roleClass : getRoleClassLibraries()) {
			((AbstractAMLDocumentElement) roleClass).isReferencedByDocumentElements(document, documentToIgnore, results);
		}
		for (AMLSystemUnitClassLibrary systemUnitClass : getSystemUnitClassLibraries()) {
			((AbstractAMLDocumentElement) systemUnitClass).isReferencedByDocumentElements(document, documentToIgnore, results);
		}

		for (AMLDocument amlDocument : getDocumentManager().getExplicitlyReferencedDocuments(this)) {
			if (!getDocumentManager().isDocumentReferencedBy(document, amlDocument, documentToIgnore))
				((AbstractAMLDocumentElement) amlDocument).isReferencedByDocumentElements(document, documentToIgnore, results);
		}

		return results.isAnyOperationNotPermitted();
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkFrom) throws AMLValidationException {
		for (AMLInstanceHierarchy instanceHierarchy : getInstanceHierarchies()) {
			((AbstractAMLDocumentElement) instanceHierarchy)._doUnlink(unlinkFrom);
		}
		for (AMLInterfaceClassLibrary interfaceClassLibrary : getInterfaceClassLibraries()) {
			((AbstractAMLDocumentElement) interfaceClassLibrary)._doUnlink(unlinkFrom);
		}
		for (AMLRoleClassLibrary roleClassLibrary : getRoleClassLibraries()) {
			((AbstractAMLDocumentElement) roleClassLibrary)._doUnlink(unlinkFrom);
		}
		for (AMLSystemUnitClassLibrary systemUnitClassLibrary : getSystemUnitClassLibraries()) {
			((AbstractAMLDocumentElement) systemUnitClassLibrary)._doUnlink(unlinkFrom);
		}
	}

	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {

		for (AMLInstanceHierarchy instanceHierarchy : getInstanceHierarchies()) {
			((AbstractAMLDocumentElement) instanceHierarchy).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		for (AMLInterfaceClassLibrary interfaceClassLibrary : getInterfaceClassLibraries()) {
			((AbstractAMLDocumentElement) interfaceClassLibrary).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		for (AMLRoleClassLibrary roleClassLibrary : getRoleClassLibraries()) {
			((AbstractAMLDocumentElement) roleClassLibrary).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		for (AMLSystemUnitClassLibrary systemUnitClassLibrary : getSystemUnitClassLibraries()) {
			((AbstractAMLDocumentElement) systemUnitClassLibrary).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
	}

	@Override
	protected void _doDeepDelete(AMLDocumentElement baseElement)
			throws AMLValidationException {
		while (getInstanceHierarchies().iterator().hasNext()) {
			((AbstractAMLDocumentElement) getInstanceHierarchies().iterator().next())._doDeepDelete(baseElement);
		}
		while (getInterfaceClassLibraries().iterator().hasNext()) {
			((AbstractAMLDocumentElement) getInterfaceClassLibraries().iterator().next())._doDeepDelete(baseElement);
		}
		while (getRoleClassLibraries().iterator().hasNext()) {
			((AbstractAMLDocumentElement) getRoleClassLibraries().iterator().next())._doDeepDelete(baseElement);
		}
		while (getSystemUnitClassLibraries().iterator().hasNext()) {
			((AbstractAMLDocumentElement) getSystemUnitClassLibraries().iterator().next())._doDeepDelete(baseElement);
		}
		delete();
	}

	@Override
	public AMLInstanceHierarchy createInstanceHierarchy(String name,
			AMLInstanceHierarchy instanceHierarchy)
			throws AMLValidationException {

		assertNotDeleted();

		AMLValidationResultList resultList = validateCreateInstanceHierarchy(name);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLNameAlreadyInUseException(resultList);

		AMLInstanceHierarchyImpl copy = _createInstanceHierarchy(name);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			CreateInstanceHierarchyChange change = new CreateInstanceHierarchyChange(copy);
			getSavepointManager().addChange(change);
		}

		notifyElementCreated(copy, this);

		if (instanceHierarchy != null) {
			copy.deepCopy(instanceHierarchy, copy, instanceHierarchy, new HashMap<AMLDocumentElement, AMLDocumentElement>());
		}

		return copy;
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement, AMLDocumentElement rootCopyElement,
			AMLDocumentElement original, Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {

		mapping.put(original, this);

		for (AMLInterfaceClassLibrary interfaceClassLibrary : ((AMLDocument) original).getInterfaceClassLibraries()) {
			AMLInterfaceClassLibrary copyInterfaceClassLibrary = createInterfaceClassLibrary(interfaceClassLibrary.getName());
			((AbstractAMLDocumentElement) copyInterfaceClassLibrary).deepCopy(rootElement, rootCopyElement, interfaceClassLibrary, mapping);
		}

		for (AMLRoleClassLibrary roleClassLibrary : ((AMLDocument) original).getRoleClassLibraries()) {
			AMLRoleClassLibrary copyRoleClassLibrary = createRoleClassLibrary(roleClassLibrary.getName());
			((AbstractAMLDocumentElement) copyRoleClassLibrary).deepCopy(rootElement, rootCopyElement, roleClassLibrary, mapping);
		}

		for (AMLSystemUnitClassLibrary systemUnitClassLibrary : ((AMLDocument) original).getSystemUnitClassLibraries()) {
			AMLSystemUnitClassLibrary copySystemUnitClassLibrary = createSystemUnitClassLibrary(systemUnitClassLibrary.getName());
			((AbstractAMLDocumentElement) copySystemUnitClassLibrary).deepCopy(rootElement, rootCopyElement, systemUnitClassLibrary, mapping);
		}

		for (AMLInstanceHierarchy instanceHierarchy : ((AMLDocument) original).getInstanceHierarchies()) {
			AMLInstanceHierarchy copyInstanceHierarchy = createInstanceHierarchy(instanceHierarchy.getName());
			((AbstractAMLDocumentElement) copyInstanceHierarchy).deepCopy(rootElement, rootCopyElement, instanceHierarchy, mapping);
		}

	}

	@Override
	public AMLInterfaceClassLibrary createInterfaceClassLibrary(String name,
			AMLInterfaceClassLibrary interfaceClassLibrary)
			throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList resultList = _validateCreateInterfaceClassLibrary(name);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		notifyElementValidated(resultList);

		AMLInterfaceClassLibraryImpl copy = _createInterfaceClassLibrary(name);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			CreateInterfaceClassLibraryChange change = new CreateInterfaceClassLibraryChange(copy);
			getSavepointManager().addChange(change);
		}

		notifyElementCreated(copy, this);

		if (interfaceClassLibrary != null)
			copy.deepCopy(interfaceClassLibrary, copy, interfaceClassLibrary, new HashMap<AMLDocumentElement, AMLDocumentElement>());

		return copy;
	}

	@Override
	public AMLRoleClassLibrary createRoleClassLibrary(String name,
			AMLRoleClassLibrary roleClassLibrary) throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList resultList = _validateCreateRoleClassLibrary(name);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		notifyElementValidated(resultList);

		AMLRoleClassLibraryImpl library = _createRoleClassLibrary(name);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			CreateRoleClassLibraryChange change = new CreateRoleClassLibraryChange(library);
			getSavepointManager().addChange(change);
		}

		notifyElementCreated(library, this);

		if (roleClassLibrary != null)
			library.deepCopy(roleClassLibrary, library, roleClassLibrary, new HashMap<AMLDocumentElement, AMLDocumentElement>());

		return library;
	}

	@Override
	public AMLSystemUnitClassLibrary createSystemUnitClassLibrary(String libraryName,
			AMLSystemUnitClassLibrary systemUnitClassLibrary)
			throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList resultList = _validateCreateSystemUnitClassLibrary(libraryName);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			validator.validateSystemUnitClassLibraryCreate(this, libraryName);
		}

		notifyElementValidated(resultList);

		AMLSystemUnitClassLibraryImpl library = _createSystemUnitClassLibrary(libraryName);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			CreateSystemUnitClassLibraryChange change = new CreateSystemUnitClassLibraryChange(library);
			getSavepointManager().addChange(change);
		}

		notifyElementCreated(library, this);

		if (systemUnitClassLibrary != null)
			library.deepCopy(systemUnitClassLibrary, library, systemUnitClassLibrary, new HashMap<AMLDocumentElement, AMLDocumentElement>());

		return library;
	}

	@Override
	protected AMLDocumentElement _getAfter() {
		return null;
	}

	AMLDocumentElement _getInstanceHierarchyAfter(
			AMLInstanceHierarchyImpl instanceHierarchy) {
		ListIterator<String> iterator = new ArrayList<String>(instanceHierarchies.keySet()).listIterator();
		while (iterator.hasNext()) {
			if (instanceHierarchy.getName().equals(iterator.next())) {
				if (iterator.hasNext()) {
					AMLInstanceHierarchyImpl after = (AMLInstanceHierarchyImpl) instanceHierarchies.get(iterator.next());
					if (after.equals(instanceHierarchy))
						return null;
					else
						return after;
				}
				return null;
			}
		}
		return null;
	}

	AMLDocumentElement _getInterfaceClassLibraryAfter(
			AMLInterfaceClassLibraryImpl amlInterfaceClassLibraryImpl) {
		ListIterator<String> iterator = new ArrayList<String>(interfaceClassLibraries.keySet()).listIterator();
		while (iterator.hasNext()) {
			if (amlInterfaceClassLibraryImpl.getName().equals(iterator.next())) {
				if (iterator.hasNext()) {
					AMLInterfaceClassLibraryImpl after = (AMLInterfaceClassLibraryImpl) interfaceClassLibraries.get(iterator.next());
					if (after.equals(amlInterfaceClassLibraryImpl))
						return null;
					else
						return after;
				}
				return null;
			}
		}
		return null;
	}

	AMLDocumentElement _getRoleClassLibraryAfter(
			AMLRoleClassLibraryImpl amlRoleClassLibraryImpl) {
		ListIterator<String> iterator = new ArrayList<String>(roleClassLibraries.keySet()).listIterator();
		while (iterator.hasNext()) {
			if (amlRoleClassLibraryImpl.getName().equals(iterator.next())) {
				if (iterator.hasNext()) {
					AMLRoleClassLibraryImpl after = (AMLRoleClassLibraryImpl) roleClassLibraries.get(iterator.next());
					if (after.equals(amlRoleClassLibraryImpl))
						return null;
					else
						return after;
				}
				return null;
			}
		}
		return null;
	}

	AMLDocumentElement _getSystemUnitClassLibraryAfter(
			AMLSystemUnitClassLibraryImpl amlSystemUnitClassLibraryImpl) {
		ListIterator<String> iterator = new ArrayList<String>(systemUnitClassLibraries.keySet()).listIterator();
		while (iterator.hasNext()) {
			if (amlSystemUnitClassLibraryImpl.getName().equals(iterator.next())) {
				if (iterator.hasNext()) {
					AMLSystemUnitClassLibraryImpl after = (AMLSystemUnitClassLibraryImpl) systemUnitClassLibraries.get(iterator.next());
					if (after.equals(amlSystemUnitClassLibraryImpl))
						return null;
					else
						return after;
				}
				return null;
			}
		}
		return null;
	}

	public <T> T getClassByPath(String path, T clazz) throws AMLValidationException {
		if (clazz instanceof AMLRoleClass) {
			return (T)getRoleClassByPath(path);
		}
		if (clazz instanceof AMLSystemUnitClass) {
			return (T)getSystemUnitClassByPath(path);
		}
		if (clazz instanceof AMLInterfaceClass) {
			return (T)getInterfaceClassByPath(path);
		}
		return null;
	}


}
