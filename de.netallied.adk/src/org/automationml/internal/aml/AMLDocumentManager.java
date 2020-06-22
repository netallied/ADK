/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.automationml.Deserializer;
import org.automationml.DocumentLocation;
import org.automationml.URLDocumentLocation;
import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentChangeListener;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLValidationResult;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.internal.ReadOnlyIterable;
import org.automationml.internal.aml.persistence.AMLDeserializer;

public class AMLDocumentManager {

	public static class MemoryDocumentLocation implements DocumentLocation {

		@Override
		public OutputStream createOutputStream() {
			return new ByteArrayOutputStream();
		}

		@Override
		public boolean isPersistent() {
			return true;
		}
	}

	private static class DocumentProperties {
		// private final String documentType;
		DocumentLocation location;
		final Set<AMLDocument> referencedDocumentsCache = new HashSet<AMLDocument>();
		final Set<AMLDocument> referencingDocumentsCache = new HashSet<AMLDocument>();
		final Set<AMLDocument> explicitExternalReferences = new LinkedHashSet<AMLDocument>();
		final Collection<AMLDocumentChangeListener> documentChangeListeners = new HashSet<AMLDocumentChangeListener>();
		public int documentChangeTransactionsCounter;
		String alias;

		// private Map<AMLDocument, Set<AMLDocumentScope>> cachedDocumentContainedInScopes = new LinkedHashMap<AMLDocument, Set<AMLDocumentScope>>();

		public DocumentProperties(String documentType, DocumentLocation location) {
			super();
			// this.documentType = documentType;
			this.location = location;
		}
	}

	private static class ImplicitDocumentReference {
		AMLDocument referrer;
		AMLDocument referenced;
		int refCount;

		private ImplicitDocumentReference(AMLDocument referrer, AMLDocument referenced) {
			super();
			this.referrer = referrer;
			this.referenced = referenced;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((referenced == null) ? 0 : referenced.hashCode());
			result = prime * result + ((referrer == null) ? 0 : referrer.hashCode());
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
			ImplicitDocumentReference other = (ImplicitDocumentReference) obj;
			if (referenced == null) {
				if (other.referenced != null) {
					return false;
				}
			} else if (!referenced.equals(other.referenced)) {
				return false;
			}
			if (referrer == null) {
				if (other.referrer != null) {
					return false;
				}
			} else if (!referrer.equals(other.referrer)) {
				return false;
			}
			return true;
		}
	}

	public static final String TYPE_AML = "AML";

	Map<AMLDocument, DocumentProperties> documentToProperties = new LinkedHashMap<AMLDocument, DocumentProperties>();
	Map<DocumentLocation, AMLDocument> locationToDocument = new LinkedHashMap<DocumentLocation, AMLDocument>();

	private Map<ImplicitDocumentReference, ImplicitDocumentReference> implicitExternalReferencesCache = new LinkedHashMap<ImplicitDocumentReference, ImplicitDocumentReference>();
	private Map<AMLDocument, AMLDocumentScope> cachedDocumentScopes = new LinkedHashMap<AMLDocument, AMLDocumentScope>();

	private AMLSessionImpl session;

	public AMLDocumentManager(AMLSessionImpl session) {
		this.session = session;
	}

	public void registerDocument(AMLDocument document, DocumentLocation location) {
		DocumentProperties properties = new DocumentProperties(TYPE_AML, location);
		documentToProperties.put(document, properties);
		locationToDocument.put(location, document);
	}

	private DocumentProperties getDocumentProperties(AMLDocument document) {
		return documentToProperties.get(document);
	}

	public void registerDocument(AMLDocument document) {
		DocumentLocation location = new MemoryDocumentLocation();
		registerDocument(document, location);
	}

	public void assertDocumentNotReferenced(AMLDocument document) throws AMLDocumentScopeInvalidException {
		if (getDocumentsDirectlyReferencing(document).iterator().hasNext())
			throw new AMLDocumentScopeInvalidException(document, "Document is still referenced");
	}

	public void unregisterDocument(AMLDocument document) throws AMLDocumentScopeInvalidException {
		assertDocumentNotReferenced(document);

		invalidateScopesContaining(document);

		cachedDocumentScopes.remove(document);

		DocumentProperties documentProperties = documentToProperties.get(document);

		if (documentProperties != null)
			locationToDocument.remove(documentProperties.location);

		documentToProperties.remove(document);

	}

	public AMLDocument getDocument(DocumentLocation location) {
		AMLDocument document = locationToDocument.get(location);
		return document;
	}

	public AMLDocument openDocument(URL url) throws Exception {

		url.openStream().close(); // check for availability

		Deserializer deserializer = new AMLDeserializer();
		deserializer.deserialize(url, session);

		DocumentLocation location = new URLDocumentLocation(url);
		AMLDocument document = getDocument(location);

		return document;
	}

	public boolean saveDocument(AMLDocument document, URL urlToSave) throws URISyntaxException {

		if (!documentToProperties.containsKey(document))
			return false;

		DocumentProperties properties = documentToProperties.get(document);
		if (properties == null)
			return false;

		if (urlToSave == null) {
			DocumentLocation location = properties.location;

			if (location instanceof URLDocumentLocation)
				urlToSave = ((URLDocumentLocation) location).getUrl();
		}
		if (urlToSave == null)
			return false;

//		Serializer serializer = new AMLSerializer();
//
//		File file = new File(urlToSave.toURI());
		// return serializer.serialize(document, file);
		return true;
	}

	public void addExplicitDocumentReference(AMLDocument referrer, AMLDocument referenced) throws AMLDocumentScopeInvalidException {
		if (referrer == referenced)
			AMLDocumentScope.throwDependencyCycleException(referrer);
		DocumentProperties documentProperties = getDocumentProperties(referrer);
		documentProperties.explicitExternalReferences.add(referenced);
		addCachedDocumentReference(referrer, referenced);
	}
	
	public void addExplicitDocumentReference(AMLDocument referrer, AMLDocument referenced, AMLDocument before, AMLDocument after) throws AMLDocumentScopeInvalidException {
		if (before == null && after == null) {
			addExplicitDocumentReference(referrer, referenced);
			return;
		}
		
		if (referrer == referenced)
			AMLDocumentScope.throwDependencyCycleException(referrer);
		DocumentProperties documentProperties = getDocumentProperties(referrer);
		Set<AMLDocument> explicitExternalReferences = documentProperties.explicitExternalReferences;
		Set<AMLDocument> newReferences = new LinkedHashSet<AMLDocument>();
		for (AMLDocument amlDocument : explicitExternalReferences) {
			if (before != null && before.equals(amlDocument))
				newReferences.add(referenced);
			newReferences.add(amlDocument);
			if (after != null && after.equals(amlDocument))
				newReferences.add(referenced);
		}
		documentProperties.explicitExternalReferences.clear();
		for (AMLDocument amlDocument : newReferences) {
			documentProperties.explicitExternalReferences.add(amlDocument);
		}
		//documentProperties.explicitExternalReferences.add(referenced);
		addCachedDocumentReference(referrer, referenced);
	}

	private void addImplicitDocumentReference(AMLDocument referrer, AMLDocument referenced) throws AMLDocumentScopeInvalidException {
		if (referrer == referenced)
			return;
		ImplicitDocumentReference implicitDocumentReference = getImplicitDocumentReference(referrer, referenced);
		if (implicitDocumentReference == null) {
			implicitDocumentReference = new ImplicitDocumentReference(referrer, referenced);
			implicitExternalReferencesCache.put(implicitDocumentReference, implicitDocumentReference);
		}
		implicitDocumentReference.refCount++;

		addCachedDocumentReference(referrer, referenced);
	}

	private void addCachedDocumentReference(AMLDocument referrer, AMLDocument referenced) throws AMLDocumentScopeInvalidException {
		DocumentProperties documentProperties = getDocumentProperties(referrer);
		Set<AMLDocument> referencedDocuments = documentProperties.referencedDocumentsCache;

		if (referencedDocuments.contains(referenced))
			return;

		checkIfAddingDocumentCreatesACycle(referrer, referenced);

		referencedDocuments.add(referenced);

		documentProperties = getDocumentProperties(referenced);
		Set<AMLDocument> referencingDocuments = documentProperties.referencingDocumentsCache;
		referencingDocuments.add(referrer);

		for (AMLDocumentScope scope : cachedDocumentScopes.values()) {
			if (scope.contains(referrer) || scope.contains(referenced))
				scope.invalidate();
		}

		// invalidateScopesContaining(referrer);
		// invalidateScopesContaining(referenced);
	}

	public void checkIfAddingDocumentCreatesACycle(AMLDocument referrer,
			AMLDocument referenced) throws AMLDocumentScopeInvalidException {
		AMLDocumentScope referrerScope = getDocumentScope(referrer);
		AMLDocumentScope referencedScope = getDocumentScope(referenced);
		
//		// check if document of referenced scope is already in document scope of referrer
//		if (referrerScope.contains(referenced) == false)
			referrerScope.mergeWith(referencedScope);
	}

	public void removeExplicitDocumentReference(AMLDocument referrer, AMLDocument referenced) throws AMLDocumentScopeInvalidException {
		DocumentProperties documentProperties = getDocumentProperties(referrer);
		Set<AMLDocument> documentReferences = documentProperties.explicitExternalReferences;
		if (documentReferences != null)
			documentReferences.remove(referenced);

		if (getImplicitDocumentReference(referrer, referenced) != null)
			return;
		removeCachedDocumentReference(referrer, referenced);
	}

	private ImplicitDocumentReference getImplicitDocumentReference(AMLDocument referrer, AMLDocument referenced) {
		ImplicitDocumentReference implicitDocumentReference = new ImplicitDocumentReference(referrer, referenced);
		implicitDocumentReference = implicitExternalReferencesCache.get(implicitDocumentReference);
		return implicitDocumentReference;
	}

	private void removeImplicitDocumentReference(AMLDocument referrer, AMLDocument referenced) throws AMLDocumentScopeInvalidException {
		if (referrer == referenced)
			return;
		ImplicitDocumentReference implicitDocumentReference = getImplicitDocumentReference(referrer, referenced);
		if (implicitDocumentReference != null) {
			implicitDocumentReference.refCount--;
			if (implicitDocumentReference.refCount == 0) {
				implicitExternalReferencesCache.remove(implicitDocumentReference);
				implicitDocumentReference = null;
			}
		}

		DocumentProperties documentProperties = getDocumentProperties(referrer);
		if (implicitDocumentReference != null || !documentProperties.explicitExternalReferences.isEmpty())
			return;

		removeCachedDocumentReference(referrer, referenced);
	}

	private void removeCachedDocumentReference(AMLDocument referrer, AMLDocument referenced) throws AMLDocumentScopeInvalidException {
		DocumentProperties documentProperties = getDocumentProperties(referrer);
		Set<AMLDocument> referencedDocuments = documentProperties.referencedDocumentsCache;

		boolean changed = false;
		if (referencedDocuments != null) {
			changed |= referencedDocuments.remove(referenced);
		}

		documentProperties = getDocumentProperties(referenced);
		Set<AMLDocument> referencingDocuments = documentProperties.referencingDocumentsCache;
		if (referencingDocuments != null) {
			changed |= referencingDocuments.remove(referrer);
		}

		if (changed)
			invalidateScopesContaining(referrer);
	}

	Iterable<AMLDocument> getDocumentsDirectlyReferencedFrom(AMLDocument document) {
		DocumentProperties documentProperties = getDocumentProperties(document);
		Set<AMLDocument> documents = documentProperties.referencedDocumentsCache;
		return new ReadOnlyIterable<AMLDocument>(documents);
	}

	Iterable<AMLDocument> getDocumentsDirectlyReferencing(AMLDocument document) {
		DocumentProperties documentProperties = getDocumentProperties(document);
		Set<AMLDocument> documents = documentProperties.referencingDocumentsCache;
		if (documents == null)
			return Collections.emptyList();
		return new ReadOnlyIterable<AMLDocument>(documents);
	}

	public Iterable<AMLDocument> getExplicitlyReferencedDocuments(AMLDocument document) {
		DocumentProperties documentProperties = getDocumentProperties(document);
		Set<AMLDocument> explicitDocumentReferences = documentProperties.explicitExternalReferences;
		return new ReadOnlyIterable<AMLDocument>(explicitDocumentReferences);
	}

	public AMLDocumentScope getDocumentScope(AMLDocument rootDocument) {
		AMLDocumentScope cachedDocumentScope = cachedDocumentScopes.get(rootDocument);
		if (cachedDocumentScope == null) {
			cachedDocumentScope = new AMLDocumentScope(rootDocument);
			cachedDocumentScopes.put(rootDocument, cachedDocumentScope);
		}
		return cachedDocumentScope;
	}

	private void invalidateScopesContaining(AMLDocument document) throws AMLDocumentScopeInvalidException {
		for (AMLDocumentScope scope : new ArrayList<AMLDocumentScope>(cachedDocumentScopes.values())) {
			if (scope.contains(document)) {
				scope.invalidate();
			}
		}
		// for (AMLDocumentScope scope : getScopesContaining(document)) {
		// scope.invalidate();
		// }
	}

	// private Set<AMLDocumentScope> getScopesContaining(AMLDocument document) {
	// Set<AMLDocumentScope> scopes = cachedDocumentContainedInScopes.get(document);
	// return (Set<AMLDocumentScope>) (scopes == null ? Collections.emptySet() : scopes);
	// }

	public void addInterfaceClassLibraryName(AMLDocument document, String libraryName) throws AMLDocumentScopeInvalidException {
		for (AMLDocumentScope scope : cachedDocumentScopes.values()) {
			if (scope.contains(document)) {
				scope.addInterfaceClassLibraryName(document, libraryName);
			}
		}
		// for (AMLDocumentScope scope : getScopesContaining(document)) {
		// scope.addInterfaceClassLibraryName(document, libraryName);
		// }

	}

	public void addRoleClassLibraryName(AMLDocument document, String libraryName) throws AMLDocumentScopeInvalidException {
		for (AMLDocumentScope scope : cachedDocumentScopes.values()) {
			if (scope.contains(document)) {
				scope.addRoleClassLibraryName(document, libraryName);
			}
		}
	}

	public void addSystemUnitClassLibraryName(AMLDocument document, String libraryName) throws AMLDocumentScopeInvalidException {
		for (AMLDocumentScope scope : cachedDocumentScopes.values()) {
			if (scope.contains(document)) {
				scope.addSystemUnitClassLibraryName(document, libraryName);
			}
		}
	}

	public void removeInterfaceClassLibraryName(AMLDocument document, String libraryName) throws AMLDocumentScopeInvalidException {
		for (AMLDocumentScope scope : cachedDocumentScopes.values()) {
			if (scope.contains(document)) {
				scope.removeInterfaceClassLibraryName(document, libraryName);
			}
		}
		// for (AMLDocumentScope scope : getScopesContaining(document)) {
		// scope.removeInterfaceClassLibraryName(document, libraryName);
		// }
	}

	public void removeRoleClassLibraryName(AMLDocument document, String libraryName) throws AMLDocumentScopeInvalidException {
		for (AMLDocumentScope scope : cachedDocumentScopes.values()) {
			if (scope.contains(document)) {
				scope.removeRoleClassLibraryName(document, libraryName);
			}
		}
	}

	public void removeSystemUnitClassLibraryName(AMLDocument document, String libraryName) throws AMLDocumentScopeInvalidException {
		for (AMLDocumentScope scope : cachedDocumentScopes.values()) {
			if (scope.contains(document)) {
				scope.removeSystemUnitClassLibraryName(document, libraryName);
			}
		}
	}

	public void addImplicitReference(AMLDocumentElement referrer, AMLDocumentElement referenced) throws AMLDocumentScopeInvalidException {
		if (referrer.getDocument() != referenced.getDocument()) {
			addImplicitDocumentReference(referrer.getDocument(), referenced.getDocument());
		}
	}

	public void removeImplicitReference(AMLDocumentElement referrer, AMLDocumentElement referenced) throws AMLDocumentScopeInvalidException {
		removeImplicitDocumentReference(referrer.getDocument(), referenced.getDocument());
	}

	public void addDocumentChangeListener(AMLDocument document, AMLDocumentChangeListener changeListener) {
		DocumentProperties documentProperties = getDocumentProperties(document);
		if (documentProperties == null)
			return;
		documentProperties.documentChangeListeners.add(changeListener);
	}

	public void removeDocumentChangeListener(AMLDocument document, AMLDocumentChangeListener changeListener) {
		DocumentProperties documentProperties = getDocumentProperties(document);
		documentProperties.documentChangeListeners.remove(changeListener);
	}

	public void notifyDocumentChangeTransactionBegin(AMLDocument document) {
		if (!session.getNotifyEnable())
			return;
		DocumentProperties documentProperties = getDocumentProperties(document);
		_notifyDocumentChangeTransactionBegin(documentProperties);
	}
	
	public void notifyDocumentChangeDirtyStateChanged(AMLDocument document) {
		if (!session.getNotifyEnable())
			return;
		DocumentProperties documentProperties = getDocumentProperties(document);
		_notifyDocumentChangeTransactionBegin(documentProperties);
		for (AMLDocumentChangeListener changeListener : documentProperties.documentChangeListeners) {
			changeListener.documentChangeDirtyStateChanged(document);
		}
		_notifyDocumentChangeTransactionEnd(documentProperties);
	}

	private void _notifyDocumentChangeTransactionBegin(DocumentProperties documentProperties) {
		documentProperties.documentChangeTransactionsCounter++;
		if (documentProperties.documentChangeTransactionsCounter != 1)
			return;
		for (AMLDocumentChangeListener changeListener : documentProperties.documentChangeListeners) {
			changeListener.documentChangeTransactionBegin();
		}
	}

	public void notifyDocumentChangeTransactionEnd(AMLDocument document) {
		if (!session.getNotifyEnable())
			return;
		DocumentProperties documentProperties = getDocumentProperties(document);
		_notifyDocumentChangeTransactionEnd(documentProperties);
	}

	private void _notifyDocumentChangeTransactionEnd(DocumentProperties documentProperties) {
		documentProperties.documentChangeTransactionsCounter--;
		if (documentProperties.documentChangeTransactionsCounter != 0)
			return;
		for (AMLDocumentChangeListener changeListener : documentProperties.documentChangeListeners) {
			changeListener.documentChangeTransactionEnd();
		}
	}

	public void notifyElementCreated(AMLDocument document, AMLDocumentElement element, AMLDocumentElement parent) {
		if (!session.getNotifyEnable())
			return;
		DocumentProperties documentProperties = getDocumentProperties(document);
		_notifyDocumentChangeTransactionBegin(documentProperties);
		for (AMLDocumentChangeListener changeListener : documentProperties.documentChangeListeners) {
			changeListener.documentChangeElementCreated(element, parent);
		}
		_notifyDocumentChangeTransactionEnd(documentProperties);
	}

	public void notifyElementModified(AMLDocument document, AMLDocumentElement element) {
		if (!session.getNotifyEnable())
			return;
		DocumentProperties documentProperties = getDocumentProperties(document);
		_notifyDocumentChangeTransactionBegin(documentProperties);
		for (AMLDocumentChangeListener changeListener : documentProperties.documentChangeListeners) {
			changeListener.documentChangeElementModified(element);
		}
		_notifyDocumentChangeTransactionEnd(documentProperties);
	}

	public void notifyElementReparented(AMLDocumentElement element, AMLDocumentElement oldParent,  AMLDocumentElement newParent) {
		if (!session.getNotifyEnable())
			return;
		DocumentProperties documentProperties = getDocumentProperties(element.getDocument());
		_notifyDocumentChangeTransactionBegin(documentProperties);
		for (AMLDocumentChangeListener changeListener : documentProperties.documentChangeListeners) {
			changeListener.documentChangeElementReparented(element, oldParent, newParent);
		}
		_notifyDocumentChangeTransactionEnd(documentProperties);
	}

	public void notifyElementDeleting(AMLDocumentElement documentElement, AMLDocumentElement element) {
		if (!session.getNotifyEnable())
			return;
		DocumentProperties documentProperties = getDocumentProperties(documentElement.getDocument());
		_notifyDocumentChangeTransactionBegin(documentProperties);
		for (AMLDocumentChangeListener changeListener : documentProperties.documentChangeListeners) {
			changeListener.documentChangeElementDeleting(documentElement, element);
		}
		_notifyDocumentChangeTransactionEnd(documentProperties);
	}
	

	public void notifyElementValidated(AMLValidationResultList validationResult) {
		if (!session.getNotifyEnable())
			return;
		if (validationResult == null)
			return;
		Set<AMLDocument> documents = new HashSet<AMLDocument>();
		Iterable<AMLValidationResult> validationResults = validationResult.getValidationResults();
		if (validationResults == null)
			return;
		for (AMLValidationResult result : validationResults) {
			AMLDocumentElement documentElement = result.getDocumentElement();

			if (documentElement == null)
				continue;

			AMLDocument document = documentElement.getDocument();
			DocumentProperties documentProperties = getDocumentProperties(document);
			if (documents.add(document))
				_notifyDocumentChangeTransactionBegin(documentProperties);

			for (AMLDocumentChangeListener changeListener : documentProperties.documentChangeListeners) {
				changeListener.documentChangeElementValidated(result);
			}
		}
		for (AMLDocument document : documents) {
			DocumentProperties documentProperties = getDocumentProperties(document);
			_notifyDocumentChangeTransactionEnd(documentProperties);
		}
	}

	public Iterable<AMLDocument> getDocuments() {
		return documentToProperties.keySet();
	}

	public int getDocumentsCount() {
		return documentToProperties.size();
	}

	public DocumentLocation getDocumentLocation(AMLDocument document) {
		DocumentProperties documentProperties = getDocumentProperties(document);
		return documentProperties == null ? null : documentProperties.location;
	}

	public Iterable<AMLDocument> getReferencedDocuments(AMLDocument document) {
		AMLDocumentScope scope = cachedDocumentScopes.get(document);
		if (scope == null)
			return null;
		return new ReadOnlyIterable<AMLDocument>(scope.getReferencedDocuments());
	}
	
	public int getReferencedDocumentsCount(AMLDocument document) {
		AMLDocumentScope scope = cachedDocumentScopes.get(document);
		if (scope == null || scope.getReferencedDocuments() == null)
			return 0;
		return scope.getReferencedDocuments().size();
	}

	public boolean isInterfaceClassLibraryNameDefined(AbstractAMLClassLibraryImpl library, String libraryName) throws AMLDocumentScopeInvalidException {
		AMLDocumentImpl document = library.getDocument();
		AMLDocumentScope scope = getDocumentScope(document);
		// for (AMLDocumentScope scope : cachedDocumentScopes.values()) {
		// if (!scope.contains(document))
		// continue;

		if (scope.isInterfaceClassLibraryNameDefined(libraryName))
			return true;
		// }
		return false;
	}

	public boolean isRoleClassLibraryNameDefined(AbstractAMLClassLibraryImpl library, String libraryName) throws AMLDocumentScopeInvalidException {
		AMLDocumentImpl document = library.getDocument();
		AMLDocumentScope scope = getDocumentScope(document);

		if (scope.isRoleClassLibraryNameDefined(libraryName))
			return true;

		return false;
	}

	public boolean isSystemUnitClassLibraryNameDefined(AbstractAMLClassLibraryImpl library, String libraryName) throws AMLDocumentScopeInvalidException {
		AMLDocumentImpl document = library.getDocument();
		AMLDocumentScope scope = getDocumentScope(document);

		if (scope.isSystemUnitClassLibraryNameDefined(libraryName))
			return true;

		return false;
	}

	public void renameInterfaceClassLibrary(AbstractAMLClassLibraryImpl library, String libraryName) throws AMLDocumentScopeInvalidException {
		AMLDocumentImpl document = library.getDocument();
		String oldName = library.getName();
		AMLDocumentScope scope = getDocumentScope(document);
		// for (AMLDocumentScope scope : cachedDocumentScopes.values()) {
		// if (!scope.contains(document))
		// continue;
		scope.removeInterfaceClassLibraryName(document, oldName);
		scope.addInterfaceClassLibraryName(document, libraryName);
		// }
	}

	public void renameRoleClassLibrary(AbstractAMLClassLibraryImpl library, String libraryName) throws AMLDocumentScopeInvalidException {
		AMLDocumentImpl document = library.getDocument();
		String oldName = library.getName();
		AMLDocumentScope scope = getDocumentScope(document);

		scope.removeRoleClassLibraryName(document, oldName);
		scope.addRoleClassLibraryName(document, libraryName);
	}

	public void renameSystemUnitClassLibrary(AbstractAMLClassLibraryImpl library, String libraryName) throws AMLDocumentScopeInvalidException {
		AMLDocumentImpl document = library.getDocument();
		String oldName = library.getName();
		AMLDocumentScope scope = getDocumentScope(document);

		scope.removeSystemUnitClassLibraryName(document, oldName);
		scope.addSystemUnitClassLibraryName(document, libraryName);
	}

	public void addUniqueId(AMLDocumentElement documentElement, UUID id) throws AMLDocumentScopeInvalidException {
		for (AMLDocumentScope scope : cachedDocumentScopes.values()) {
			if (scope.contains(documentElement.getDocument())) {
				scope.addUniqueId(documentElement.getDocument(), id);
			}
		}
	}

	public void removeUniqueId(AMLDocumentImpl document, UUID id) throws AMLDocumentScopeInvalidException {
		for (AMLDocumentScope scope : cachedDocumentScopes.values()) {
			if (scope.contains(document)) {
				scope.removeUniqueId(document, id);
			}
		}
	}

	public boolean isUniqueIdDefined(AMLDocumentElement container, UUID id) throws AMLDocumentScopeInvalidException {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();
		AMLDocumentScope scope = getDocumentScope(document);
		if (scope.isUniqueIdDefined(id))
			return true;
		return false;
	}
	
	public void setAlias(AMLDocument document, String alias){
		DocumentProperties documentProperties = getDocumentProperties(document);
		if( documentProperties != null)
			documentProperties.alias = alias;
	}

	public String getAlias(AMLDocument document){
		DocumentProperties documentProperties = getDocumentProperties(document);
		return documentProperties == null ? null : documentProperties.alias;
	}

	public void relocate(AMLDocument document, URL newUrl) {
		DocumentLocation oldLocation = getDocumentLocation(document);
		URLDocumentLocation newLocation = new URLDocumentLocation(newUrl);
		if( oldLocation.equals(newLocation))
			return;
		locationToDocument.remove(oldLocation);
		locationToDocument.put(newLocation, document);
		getDocumentProperties(document).location = newLocation;
		((AMLDocumentImpl)document).notifyElementModified(document);
	}

	public AMLDocument getRootDocument(AMLDocument documentInScope) {
		AMLDocument rootDocument = documentInScope;
		
		AMLDocumentScope scope = cachedDocumentScopes.get(documentInScope);
		if (scope == null)
			return rootDocument;
		
		rootDocument = documentInScope;
		for (AMLDocument referencingDocument : scope.getRefereningDocuments()) {
			rootDocument = getRootDocument(referencingDocument);
			break;
		}
		
		return rootDocument;
	}

	public boolean isDocumentReferencedBy(AMLDocument referrer, AMLDocument referenceToTest, AMLDocument explicitReferenceToIgnore) {
		
		Set<AMLDocument> documents = new HashSet<AMLDocument>();
		
		for (AMLDocument document : getExplicitlyReferencedDocuments(referrer)) {
			if (explicitReferenceToIgnore == null)
				documents.add(document);
			else if (!document.equals(explicitReferenceToIgnore)
					&&(!document.equals(referenceToTest) || !explicitReferenceToIgnore.equals(referenceToTest)))
				documents.add(document);
		}
		
		while (!documents.isEmpty()) {
			Set<AMLDocument> nextDocuments = new HashSet<AMLDocument>();
			for (AMLDocument amlDocument : documents) {
				if (amlDocument.equals(referenceToTest))
					return true;
				for (AMLDocument document : getExplicitlyReferencedDocuments(amlDocument)) {					
					nextDocuments.add(document);
				}
			}
			documents = nextDocuments;
		}
		return false;
	}
}
