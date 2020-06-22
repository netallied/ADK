/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.automationml.aml.AMLDocument;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLDocumentScopeInvalidException;
import org.automationml.aml.AMLInstanceHierarchy;
import org.automationml.aml.AMLInterfaceClassLibrary;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalElementContainer;
import org.automationml.aml.AMLRoleClassLibrary;
import org.automationml.aml.AMLSystemUnitClass;
import org.automationml.aml.AMLSystemUnitClassLibrary;

public class AMLDocumentScope {

	private final AMLDocument rootDocument;
	private Set<AMLDocument> referencedDocuments;
	private Set<AMLDocument> referencingDocuments;

	private Map<String, AMLDocument> cachedInterfaceClassLibraryNamesFromReferencedDocuments;
	private Map<String, AMLDocument> cachedInterfaceClassLibraryNamesFromReferencingDocuments;

	private Map<String, AMLDocument> cachedRoleClassLibraryNamesFromReferencedDocuments;
	private Map<String, AMLDocument> cachedRoleClassLibraryNamesFromReferencingDocuments;

	private Map<String, AMLDocument> cachedSystemUnitClassLibraryNamesFromReferencedDocuments;
	private Map<String, AMLDocument> cachedSystemUnitClassLibraryNamesFromReferencingDocuments;

	private Set<UUID> cachedUniqueIDsFormReferencedDocments;
	private Set<UUID> cachedUniqueIDsFormReferencingDocments;
	
	private boolean invalid = true;

	public AMLDocumentScope(AMLDocument rootDocument) {
		this.rootDocument = rootDocument;
	}

	private Map<String, AMLDocument> collectInterfaceClassLibraryNames(Set<AMLDocument> documents) {
		Map<String, AMLDocument> libraryNames = new LinkedHashMap<String, AMLDocument>();
		for (AMLDocument document : documents) {
			libraryNames = collectInterfaceClassLibraryNames(libraryNames, document);
		}
		return libraryNames;
	}

	private Map<String, AMLDocument> collectInterfaceClassLibraryNames(Map<String, AMLDocument> libraryNames, AMLDocument document) {
		for (AMLInterfaceClassLibrary library : document.getInterfaceClassLibraries()) {
			if (libraryNames == null)
				libraryNames = new LinkedHashMap<String, AMLDocument>();
			libraryNames.put(library.getName(), document);
		}
		return libraryNames;
	}

	private Map<String, AMLDocument> collectRoleClassLibraryNames(Set<AMLDocument> documents) {
		Map<String, AMLDocument> libraryNames = new LinkedHashMap<String, AMLDocument>();
		for (AMLDocument document : documents) {
			libraryNames = collectRoleClassLibraryNames(libraryNames, document);
		}
		return libraryNames;
	}

	private Map<String, AMLDocument> collectRoleClassLibraryNames(Map<String, AMLDocument> libraryNames, AMLDocument document) {
		for (AMLRoleClassLibrary library : document.getRoleClassLibraries()) {
			if (libraryNames == null)
				libraryNames = new LinkedHashMap<String, AMLDocument>();
			libraryNames.put(library.getName(), document);
		}
		return libraryNames;
	}

	private Map<String, AMLDocument> collectSystemUnitClassLibraryNames(Set<AMLDocument> documents) {
		Map<String, AMLDocument> libraryNames = new LinkedHashMap<String, AMLDocument>();
		for (AMLDocument document : documents) {
			libraryNames = collectSystemUnitClassLibraryNames(libraryNames, document);
		}
		return libraryNames;
	}

	private Map<String, AMLDocument> collectSystemUnitClassLibraryNames(Map<String, AMLDocument> libraryNames, AMLDocument document) {
		for (AMLSystemUnitClassLibrary library : document.getSystemUnitClassLibraries()) {
			if (libraryNames == null)
				libraryNames = new LinkedHashMap<String, AMLDocument>();
			libraryNames.put(library.getName(), document);
		}
		return libraryNames;
	}

	public void mergeWith(AMLDocumentScope scope) throws AMLDocumentScopeInvalidException {
		revalidate();
		scope.revalidate();

		validateThatAddOfExternalReferenceDoesNotCreateACycle(scope.rootDocument);

		if (isEqualTo(scope))
			return;

		// -- interface class
		{
			Map<String, AMLDocument> subset = new LinkedHashMap<String, AMLDocument>();
			if (cachedInterfaceClassLibraryNamesFromReferencedDocuments != null)
				subset.putAll(cachedInterfaceClassLibraryNamesFromReferencedDocuments);
			if (cachedInterfaceClassLibraryNamesFromReferencingDocuments != null)
				subset.putAll(cachedInterfaceClassLibraryNamesFromReferencingDocuments);

			Map<String, AMLDocument> otherNames = new LinkedHashMap<String, AMLDocument>();
			if (scope.cachedInterfaceClassLibraryNamesFromReferencedDocuments != null)
				otherNames.putAll(scope.cachedInterfaceClassLibraryNamesFromReferencedDocuments);

			Set<String> subsetKeys = subset.keySet();
			subsetKeys.retainAll(otherNames.keySet());
			if (!subset.isEmpty()) {
				// check if the documents are different
				for (String string : subsetKeys) {
					if (!subset.get(string).equals(otherNames.get(string)))
						throw new AMLDocumentScopeInvalidException(null, "Duplicate InterfaceClass Library Names in Scope");
				}
				
			}
		}

		// -- role class
		{
			Map<String, AMLDocument> subset = new LinkedHashMap<String, AMLDocument>();
			if (cachedRoleClassLibraryNamesFromReferencedDocuments != null)
				subset.putAll(cachedRoleClassLibraryNamesFromReferencedDocuments);
			if (cachedRoleClassLibraryNamesFromReferencingDocuments != null)
				subset.putAll(cachedRoleClassLibraryNamesFromReferencingDocuments);

			Map<String, AMLDocument> otherNames = new LinkedHashMap<String, AMLDocument>();
			if (scope.cachedRoleClassLibraryNamesFromReferencedDocuments != null)
				otherNames.putAll(scope.cachedRoleClassLibraryNamesFromReferencedDocuments);

			Set<String> subsetKeys = subset.keySet();
			subsetKeys.retainAll(otherNames.keySet());
			if (!subset.isEmpty()) {
				// check if the documents are different
				for (String string : subsetKeys) {
					if (!subset.get(string).equals(otherNames.get(string)))
						throw new AMLDocumentScopeInvalidException(null, "Duplicate Role Class Library Names in Scope");
				}
				
			}
		}

		// -- system unit class
		{
			Map<String, AMLDocument> subset = new LinkedHashMap<String, AMLDocument>();
			if (cachedSystemUnitClassLibraryNamesFromReferencedDocuments != null)
				subset.putAll(cachedSystemUnitClassLibraryNamesFromReferencedDocuments);
			if (cachedSystemUnitClassLibraryNamesFromReferencingDocuments != null)
				subset.putAll(cachedSystemUnitClassLibraryNamesFromReferencingDocuments);

			Map<String, AMLDocument> otherNames = new LinkedHashMap<String, AMLDocument>();
			if (scope.cachedSystemUnitClassLibraryNamesFromReferencedDocuments != null)
				otherNames.putAll(scope.cachedSystemUnitClassLibraryNamesFromReferencedDocuments);

			Set<String> subsetKeys = subset.keySet();
			subsetKeys.retainAll(otherNames.keySet());
			if (!subset.isEmpty()) {
				// check if the documents are different
				for (String string : subsetKeys) {
					if (!subset.get(string).equals(otherNames.get(string)))
						throw new AMLDocumentScopeInvalidException(null, "Duplicate SystemUnit Class Library Names in Scope");
				}
				
			}
		}
		
		// -- internal elements
		{
			Set<UUID> subset = new LinkedHashSet<UUID>();
			if (cachedUniqueIDsFormReferencedDocments != null)
				subset.addAll(cachedUniqueIDsFormReferencedDocments);
			if (cachedUniqueIDsFormReferencingDocments != null)
				subset.addAll(cachedUniqueIDsFormReferencingDocments);

			Set<UUID> otherIds = new LinkedHashSet<UUID>();
			if (scope.cachedUniqueIDsFormReferencedDocments != null)
				otherIds.addAll(scope.cachedUniqueIDsFormReferencedDocments);

			subset.retainAll(otherIds);
			if (!subset.isEmpty())
				throw new AMLDocumentScopeInvalidException(null, "Duplicate InternalElement Id in Scope");
		}

		referencedDocuments.addAll(scope.referencedDocuments);
		referencedDocuments.add(scope.rootDocument);

		scope.referencingDocuments.addAll(this.referencingDocuments);
		scope.referencingDocuments.add(this.rootDocument);

		addCachedInterfaceClassLibraryNamesFromReferencedDocuments(scope.cachedInterfaceClassLibraryNamesFromReferencedDocuments);
		addCachedRoleClassLibraryNamesFromReferencedDocuments(scope.cachedRoleClassLibraryNamesFromReferencedDocuments);
		addCachedSystemUnitClassLibraryNamesFromReferencedDocuments(scope.cachedSystemUnitClassLibraryNamesFromReferencedDocuments);
	}

	private void addCachedInterfaceClassLibraryNamesFromReferencedDocuments(Map<String, AMLDocument> libraryNames) {
		if (libraryNames == null || libraryNames.isEmpty())
			return;
		if (cachedInterfaceClassLibraryNamesFromReferencedDocuments == null)
			cachedInterfaceClassLibraryNamesFromReferencedDocuments = new LinkedHashMap<String, AMLDocument>();
		cachedInterfaceClassLibraryNamesFromReferencedDocuments.putAll(libraryNames);
	}

	private void addCachedRoleClassLibraryNamesFromReferencedDocuments(Map<String, AMLDocument> libraryNames) {
		if (libraryNames == null || libraryNames.isEmpty())
			return;
		if (cachedRoleClassLibraryNamesFromReferencedDocuments == null)
			cachedRoleClassLibraryNamesFromReferencedDocuments = new LinkedHashMap<String, AMLDocument>();
		cachedRoleClassLibraryNamesFromReferencedDocuments.putAll(libraryNames);
	}

	private void addCachedSystemUnitClassLibraryNamesFromReferencedDocuments(Map<String, AMLDocument> libraryNames) {
		if (libraryNames == null || libraryNames.isEmpty())
			return;
		if (cachedSystemUnitClassLibraryNamesFromReferencedDocuments == null)
			cachedSystemUnitClassLibraryNamesFromReferencedDocuments = new LinkedHashMap<String, AMLDocument>();
		cachedSystemUnitClassLibraryNamesFromReferencedDocuments.putAll(libraryNames);
	}

	private void validateThatAddOfExternalReferenceDoesNotCreateACycle(AMLDocument referencedDocument) throws AMLDocumentScopeInvalidException {
		Set<AMLDocument> collected = new LinkedHashSet<AMLDocument>();
		Stack<AMLDocument> path = new Stack<AMLDocument>();
		path.add(rootDocument);
		collectAllReferencedDocumentsRecursively(referencedDocument, collected, path);
	}

	public boolean contains(AMLDocument document) throws AMLDocumentScopeInvalidException {
		revalidate();
		return rootDocument == document || referencedDocuments.contains(document) || referencingDocuments.contains(document);
	}

	public boolean isInterfaceClassLibraryNameDefined(String libraryName) throws AMLDocumentScopeInvalidException {
		revalidate();
		if (cachedInterfaceClassLibraryNamesFromReferencedDocuments != null && cachedInterfaceClassLibraryNamesFromReferencedDocuments.containsKey(libraryName))
			return true;
		if (cachedInterfaceClassLibraryNamesFromReferencingDocuments != null && cachedInterfaceClassLibraryNamesFromReferencingDocuments.containsKey(libraryName))
			return true;
		return false;
	}

	public boolean isRoleClassLibraryNameDefined(String libraryName) throws AMLDocumentScopeInvalidException {
		revalidate();
		if (cachedRoleClassLibraryNamesFromReferencedDocuments != null && cachedRoleClassLibraryNamesFromReferencedDocuments.containsKey(libraryName))
			return true;
		if (cachedRoleClassLibraryNamesFromReferencingDocuments != null && cachedRoleClassLibraryNamesFromReferencingDocuments.containsKey(libraryName))
			return true;
		return false;
	}

	public boolean isSystemUnitClassLibraryNameDefined(String libraryName) throws AMLDocumentScopeInvalidException {
		revalidate();
		if (cachedSystemUnitClassLibraryNamesFromReferencedDocuments != null && cachedSystemUnitClassLibraryNamesFromReferencedDocuments.containsKey(libraryName))
			return true;
		if (cachedSystemUnitClassLibraryNamesFromReferencingDocuments != null
				&& cachedSystemUnitClassLibraryNamesFromReferencingDocuments.containsKey(libraryName))
			return true;
		return false;
	}

	public AMLInterfaceClassLibrary getInterfaceClassLibrary(String libraryName) {
		if (cachedInterfaceClassLibraryNamesFromReferencedDocuments != null && cachedInterfaceClassLibraryNamesFromReferencedDocuments.containsKey(libraryName)) {
			AMLInterfaceClassLibrary library = rootDocument.getInterfaceClassLibrary(libraryName);
			if (library != null)
				return library;
			return findInterfaceClassLibrary(libraryName, referencedDocuments);
		}
		if (cachedInterfaceClassLibraryNamesFromReferencingDocuments != null && cachedInterfaceClassLibraryNamesFromReferencingDocuments.containsKey(libraryName)) {
			return findInterfaceClassLibrary(libraryName, referencingDocuments);
		}
		return null;
	}

	public AMLRoleClassLibrary getRoleClassLibrary(String libraryName) {
		if (cachedRoleClassLibraryNamesFromReferencedDocuments != null && cachedRoleClassLibraryNamesFromReferencedDocuments.containsKey(libraryName)) {
			AMLRoleClassLibrary library = rootDocument.getRoleClassLibrary(libraryName);
			if (library != null)
				return library;
			return findRoleClassLibrary(libraryName, referencedDocuments);
		}
		if (cachedRoleClassLibraryNamesFromReferencingDocuments != null && cachedRoleClassLibraryNamesFromReferencingDocuments.containsKey(libraryName)) {
			return findRoleClassLibrary(libraryName, referencingDocuments);
		}
		return null;
	}

	public AMLSystemUnitClassLibrary getSystemUnitClassLibrary(String libraryName) {
		if (cachedSystemUnitClassLibraryNamesFromReferencedDocuments != null && cachedSystemUnitClassLibraryNamesFromReferencedDocuments.containsKey(libraryName)) {
			AMLSystemUnitClassLibrary library = rootDocument.getSystemUnitClassLibrary(libraryName);
			if (library != null)
				return library;
			return findSystemUnitClassLibrary(libraryName, referencedDocuments);
		}
		if (cachedSystemUnitClassLibraryNamesFromReferencingDocuments != null
				&& cachedSystemUnitClassLibraryNamesFromReferencingDocuments.containsKey(libraryName)) {
			return findSystemUnitClassLibrary(libraryName, referencingDocuments);
		}
		return null;
	}

	private AMLInterfaceClassLibrary findInterfaceClassLibrary(String libraryName, Iterable<AMLDocument> documents) {
		for (AMLDocument document : documents) {
			AMLInterfaceClassLibrary library = document.getInterfaceClassLibrary(libraryName);
			if (library != null)
				return library;
		}
		return null;
	}

	private AMLRoleClassLibrary findRoleClassLibrary(String libraryName, Iterable<AMLDocument> documents) {
		for (AMLDocument document : documents) {
			AMLRoleClassLibrary library = document.getRoleClassLibrary(libraryName);
			if (library != null)
				return library;
		}
		return null;
	}

	private AMLSystemUnitClassLibrary findSystemUnitClassLibrary(String libraryName, Iterable<AMLDocument> documents) {
		for (AMLDocument document : documents) {
			AMLSystemUnitClassLibrary library = document.getSystemUnitClassLibrary(libraryName);
			if (library != null)
				return library;
		}
		return null;
	}

	public void addInterfaceClassLibraryName(AMLDocument document, String libraryName) throws AMLDocumentScopeInvalidException {
		revalidate();
		if (rootDocument == document || referencedDocuments.contains(document)) {
			if (cachedInterfaceClassLibraryNamesFromReferencedDocuments == null)
				cachedInterfaceClassLibraryNamesFromReferencedDocuments = new LinkedHashMap<String, AMLDocument>();
			cachedInterfaceClassLibraryNamesFromReferencedDocuments.put(libraryName, document);
			return;
		}
		if (referencingDocuments.contains(document)) {
			if (cachedInterfaceClassLibraryNamesFromReferencingDocuments == null)
				cachedInterfaceClassLibraryNamesFromReferencingDocuments = new LinkedHashMap<String, AMLDocument>();
			cachedInterfaceClassLibraryNamesFromReferencingDocuments.put(libraryName, document);
			return;
		}
	}

	public void addRoleClassLibraryName(AMLDocument document, String libraryName) throws AMLDocumentScopeInvalidException {
		revalidate();
		if (rootDocument == document || referencedDocuments.contains(document)) {
			if (cachedRoleClassLibraryNamesFromReferencedDocuments == null)
				cachedRoleClassLibraryNamesFromReferencedDocuments = new LinkedHashMap<String, AMLDocument>();
			cachedRoleClassLibraryNamesFromReferencedDocuments.put(libraryName, document);
			return;
		}
		if (referencingDocuments.contains(document)) {
			if (cachedRoleClassLibraryNamesFromReferencingDocuments == null)
				cachedRoleClassLibraryNamesFromReferencingDocuments = new LinkedHashMap<String, AMLDocument>();
			cachedRoleClassLibraryNamesFromReferencingDocuments.put(libraryName, document);
			return;
		}
	}

	public void addSystemUnitClassLibraryName(AMLDocument document, String libraryName) throws AMLDocumentScopeInvalidException {
		revalidate();
		if (rootDocument == document || referencedDocuments.contains(document)) {
			if (cachedSystemUnitClassLibraryNamesFromReferencedDocuments == null)
				cachedSystemUnitClassLibraryNamesFromReferencedDocuments = new LinkedHashMap<String, AMLDocument>();
			cachedSystemUnitClassLibraryNamesFromReferencedDocuments.put(libraryName, document);
			return;
		}
		if (referencingDocuments.contains(document)) {
			if (cachedSystemUnitClassLibraryNamesFromReferencingDocuments == null)
				cachedSystemUnitClassLibraryNamesFromReferencingDocuments = new LinkedHashMap<String, AMLDocument>();
			cachedSystemUnitClassLibraryNamesFromReferencingDocuments.put(libraryName, document);
			return;
		}
	}

	public void removeInterfaceClassLibraryName(AMLDocument document, String libraryName) {
		if (cachedInterfaceClassLibraryNamesFromReferencedDocuments != null)
			cachedInterfaceClassLibraryNamesFromReferencedDocuments.remove(libraryName);
		if (cachedInterfaceClassLibraryNamesFromReferencingDocuments != null)
			cachedInterfaceClassLibraryNamesFromReferencingDocuments.remove(libraryName);
	}

	public void removeRoleClassLibraryName(AMLDocument document, String libraryName) {
		if (cachedRoleClassLibraryNamesFromReferencedDocuments != null)
			cachedRoleClassLibraryNamesFromReferencedDocuments.remove(libraryName);
		if (cachedRoleClassLibraryNamesFromReferencingDocuments != null)
			cachedRoleClassLibraryNamesFromReferencingDocuments.remove(libraryName);
	}

	public void removeSystemUnitClassLibraryName(AMLDocument document, String libraryName) {
		if (cachedSystemUnitClassLibraryNamesFromReferencedDocuments != null)
			cachedSystemUnitClassLibraryNamesFromReferencedDocuments.remove(libraryName);
		if (cachedSystemUnitClassLibraryNamesFromReferencingDocuments != null)
			cachedSystemUnitClassLibraryNamesFromReferencingDocuments.remove(libraryName);
	}
	
	public void removeUniqueId(AMLDocument document, UUID id) {
		if (cachedUniqueIDsFormReferencedDocments != null)
			cachedUniqueIDsFormReferencedDocments.remove(id);
		if (cachedUniqueIDsFormReferencingDocments != null)
			cachedUniqueIDsFormReferencingDocments.remove(id);
	}

	public void invalidateInterfaceClassLibraries() throws AMLDocumentScopeInvalidException {
		revalidate();
		this.cachedInterfaceClassLibraryNamesFromReferencedDocuments = collectInterfaceClassLibraryNames(referencedDocuments);
		this.cachedInterfaceClassLibraryNamesFromReferencedDocuments = collectInterfaceClassLibraryNames(
				cachedInterfaceClassLibraryNamesFromReferencedDocuments, rootDocument);
		this.cachedInterfaceClassLibraryNamesFromReferencingDocuments = collectInterfaceClassLibraryNames(referencingDocuments);
	}

	public void invalidateRoleClassLibraries() throws AMLDocumentScopeInvalidException {
		revalidate();
		this.cachedRoleClassLibraryNamesFromReferencedDocuments = collectRoleClassLibraryNames(referencedDocuments);
		this.cachedRoleClassLibraryNamesFromReferencedDocuments = collectRoleClassLibraryNames(cachedRoleClassLibraryNamesFromReferencedDocuments, rootDocument);
		this.cachedRoleClassLibraryNamesFromReferencingDocuments = collectRoleClassLibraryNames(referencingDocuments);
	}

	public void invalidateSystemUnitClassLibraries() throws AMLDocumentScopeInvalidException {
		revalidate();
		this.cachedSystemUnitClassLibraryNamesFromReferencedDocuments = collectSystemUnitClassLibraryNames(referencedDocuments);
		this.cachedSystemUnitClassLibraryNamesFromReferencedDocuments = collectSystemUnitClassLibraryNames(
				cachedSystemUnitClassLibraryNamesFromReferencedDocuments, rootDocument);
		this.cachedSystemUnitClassLibraryNamesFromReferencingDocuments = collectSystemUnitClassLibraryNames(referencingDocuments);
	}

	public boolean isEqualTo(AMLDocumentScope other) throws AMLDocumentScopeInvalidException {
		return rootDocument == other.rootDocument;
	}

	private void collectAllReferencedDocumentsRecursively(AMLDocument document, Set<AMLDocument> collected, Stack<AMLDocument> path)
			throws AMLDocumentScopeInvalidException {

		for (AMLDocument referencedDocument : getDocumentManager().getDocumentsDirectlyReferencedFrom(document)) {
			if (path.contains(referencedDocument))
				throwDependencyCycleException(document);
			boolean descend = true;
			if (collected != null)
				descend = collected.add(referencedDocument);

			if (descend) {
				path.push(referencedDocument);
				collectAllReferencedDocumentsRecursively(referencedDocument, collected, path);
				path.pop();
			}
		}
	}

	private AMLDocumentManager getDocumentManager() {
		AMLSessionImpl session = ((AMLDocumentImpl) rootDocument).getSession();
		return session.getDocumentManager();
	}

	public static void throwDependencyCycleException(AMLDocument document) throws AMLDocumentScopeInvalidException {
		throw new AMLDocumentScopeInvalidException(document, "The Document Reference Graph contains a Cycle");
	}

	private void collectAllReferencingDocumentsRecursively(AMLDocument document, Set<AMLDocument> collected, Stack<AMLDocument> path)
			throws AMLDocumentScopeInvalidException {
		for (AMLDocument referencingDocument : getDocumentManager().getDocumentsDirectlyReferencing(document)) {
			if (path.contains(referencingDocument))
				throwDependencyCycleException(document);
			boolean descend = true;
			if (collected != null)
				descend = collected.add(referencingDocument);
			if (descend) {
				path.push(referencingDocument);
				collectAllReferencingDocumentsRecursively(referencingDocument, collected, path);
				path.pop();
			}
		}
	}

	public void invalidate() {
		invalid = true;
	}

	private void revalidate() throws AMLDocumentScopeInvalidException {
		if (!invalid)
			return;
		invalid = false;
		this.referencedDocuments = new LinkedHashSet<AMLDocument>();
		this.referencingDocuments = new LinkedHashSet<AMLDocument>();
		collectAllReferencedDocumentsRecursively(rootDocument, referencedDocuments, new Stack<AMLDocument>());
		collectAllReferencingDocumentsRecursively(rootDocument, referencingDocuments, new Stack<AMLDocument>());
		invalidateInterfaceClassLibraries();
		invalidateRoleClassLibraries();
		invalidateSystemUnitClassLibraries();
		invalidateInternalElements();
	}

	public void invalidateInternalElements() throws AMLDocumentScopeInvalidException {
		revalidate();
		this.cachedUniqueIDsFormReferencedDocments = collectUniqueIds(referencedDocuments);
		this.cachedUniqueIDsFormReferencedDocments = collectUniqueIds(cachedUniqueIDsFormReferencedDocments, rootDocument);
		this.cachedUniqueIDsFormReferencingDocments = collectUniqueIds(referencingDocuments);
	}

	private Set<UUID> collectUniqueIds(Set<UUID> internalElementIds, AMLDocument document) {
		Iterator<AMLInstanceHierarchy> instanceHierarchies = document.getInstanceHierarchies().iterator();
		while (instanceHierarchies.hasNext()) {
			AMLInternalElementContainer container = instanceHierarchies.next();
			collectInternalElementIds(internalElementIds, container);
		}

		Iterator<AMLSystemUnitClassLibrary> systemUnitClassLibraries = document.getSystemUnitClassLibraries().iterator();
		while (systemUnitClassLibraries.hasNext()) {
			AMLSystemUnitClassLibrary systemUnitClassLibrary = systemUnitClassLibraries.next();
			Iterator<AMLSystemUnitClass> systemUnitClasses = systemUnitClassLibrary.getSystemUnitClasses().iterator();
			collectInternalElementIds(internalElementIds, systemUnitClasses);
		}
		
		// TODO: collect ExternalInterfaceIDs
		return internalElementIds;
	}

	private Set<UUID> collectInternalElementIds(Set<UUID> internalElementIds, Iterator<AMLSystemUnitClass> systemUnitClasses) {
		while (systemUnitClasses.hasNext()) {
			AMLSystemUnitClass systemUnitClass = systemUnitClasses.next();
			collectInternalElementIds(internalElementIds, systemUnitClass);

			Iterator<AMLSystemUnitClass> nextSystemUnitClasses = systemUnitClass.getSystemUnitClasses().iterator();
			collectInternalElementIds(internalElementIds, nextSystemUnitClasses);
		}
		return internalElementIds;
	}

	private Set<UUID> collectInternalElementIds(Set<UUID> internalElementIds, AMLInternalElementContainer container) {
		Iterator<AMLInternalElement> internalElements = container.getInternalElements().iterator();
		while (internalElements.hasNext()) {
			AMLInternalElement internalElement = internalElements.next();
			internalElementIds.add(internalElement.getId());
			collectInternalElementIds(internalElementIds, internalElement);
		}
		return internalElementIds;
	}

	private Set<UUID> collectUniqueIds(Set<AMLDocument> documents) {
		Set<UUID> internalElementIds = new LinkedHashSet<UUID>();
		for (AMLDocument document : documents) {
			internalElementIds = collectUniqueIds(internalElementIds, document);
		}
		return internalElementIds;
	}

	public Set<AMLDocument> getReferencedDocuments() {
		return referencedDocuments;
	}
	
	public Set<AMLDocument> getRefereningDocuments() {
		return referencingDocuments;
	}

	public AMLInternalElement getInternalElement(UUID id) {
		if (cachedUniqueIDsFormReferencedDocments != null && cachedUniqueIDsFormReferencedDocments.contains(id)) {
			AMLInternalElement internalElement = getInternalElement(rootDocument, id);
			if (internalElement != null)
				return internalElement;
			return findInternalElement(id, referencedDocuments);
		}
		if (cachedUniqueIDsFormReferencingDocments != null && cachedUniqueIDsFormReferencingDocments.contains(id)) {
			return findInternalElement(id, referencingDocuments);
		}
		return null;
	}

	private AMLInternalElement findInternalElement(UUID id, Iterable<AMLDocument> documents) {
		for (AMLDocument document : documents) {
			AMLInternalElement internalElement = getInternalElement(document, id);
			if (internalElement != null)
				return internalElement;
		}
		return null;
	}

	private AMLInternalElement getInternalElement(AMLDocument document, UUID id) {
		Iterator<AMLInstanceHierarchy> instanceHierarchies = document.getInstanceHierarchies().iterator();
		while (instanceHierarchies.hasNext()) {
			AMLInternalElementContainer container = instanceHierarchies.next();
			getInternalElement(container, id);
		}

		Iterator<AMLSystemUnitClassLibrary> systemUnitClassLibraries = document.getSystemUnitClassLibraries().iterator();
		while (systemUnitClassLibraries.hasNext()) {
			AMLSystemUnitClassLibrary systemUnitClassLibrary = systemUnitClassLibraries.next();
			Iterator<AMLSystemUnitClass> systemUnitClasses = systemUnitClassLibrary.getSystemUnitClasses().iterator();
			getInternalElement(systemUnitClasses, id);
		}
		return null;
	}

	private AMLInternalElement getInternalElement(Iterator<AMLSystemUnitClass> systemUnitClasses, UUID id) {
		while (systemUnitClasses.hasNext()) {
			AMLSystemUnitClass systemUnitClass = systemUnitClasses.next();
			AMLInternalElement internalElement = getInternalElement(systemUnitClass, id);
			if (internalElement != null)
				return internalElement;

			Iterator<AMLSystemUnitClass> nextSystemUnitClasses = systemUnitClass.getSystemUnitClasses().iterator();
			internalElement = getInternalElement(nextSystemUnitClasses, id);
			if (internalElement != null)
				return internalElement;
		}
		return null;
	}

	private AMLInternalElement getInternalElement(AMLInternalElementContainer container, UUID id) {
		Iterator<AMLInternalElement> internalElements = container.getInternalElements().iterator();
		while (internalElements.hasNext()) {
			AMLInternalElement internalElement = internalElements.next();
			if (internalElement.getId().equals(id))
				return internalElement;
			internalElement = getInternalElement(internalElement, id);
			if (internalElement != null)
				return internalElement;
		}
		return null;
	}

	public void addUniqueId(AMLDocument document, UUID id) throws AMLDocumentScopeInvalidException {
		revalidate();
		if (rootDocument == document || referencedDocuments.contains(document)) {
			if (cachedUniqueIDsFormReferencedDocments == null)
				cachedUniqueIDsFormReferencedDocments = new LinkedHashSet<UUID>();
			cachedUniqueIDsFormReferencedDocments.add(id);
			return;
		}
		if (referencingDocuments.contains(document)) {
			if (cachedUniqueIDsFormReferencingDocments == null)
				cachedUniqueIDsFormReferencingDocments = new LinkedHashSet<UUID>();
			cachedUniqueIDsFormReferencingDocments.add(id);
			return;
		}
	}

	public boolean isUniqueIdDefined(UUID id) throws AMLDocumentScopeInvalidException {
		revalidate();
		if (cachedUniqueIDsFormReferencedDocments != null && cachedUniqueIDsFormReferencedDocments.contains(id))
			return true;
		if (cachedUniqueIDsFormReferencingDocments != null && cachedUniqueIDsFormReferencingDocments.contains(id))
			return true;
		return false;
	}

	public boolean isInDocumentScope(AMLDocumentElement amlDocumentElement) {
		if (rootDocument.equals(amlDocumentElement.getDocument()))
			return true;
		if (referencedDocuments == null)
			return false;
		return referencedDocuments.contains(amlDocumentElement.getDocument());
	}
}
