/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import java.util.ArrayList;
import java.util.List;

import org.automationml.Savepoint;
import org.automationml.internal.SavepointManager;
import org.automationml.internal.aml.AMLSessionImpl;
import org.junit.After;
import org.junit.Before;

import static org.automationml.aml.AMLSessionManager.amlSessionManager;

import static org.fest.assertions.Assertions.assertThat;

public abstract class AbstractAMLTest {
	protected AMLSession session;
	private Savepoint savepoint;
	protected TestSessionChangeListener sessionChangeListener;
	protected TestDocumentChangeListener documentChangeListener;

	static class TestSessionChangeListener implements AMLSessionChangeListener {
		StringBuilder result = new StringBuilder();
		AMLDocumentChangeListener documentChangeListener = new TestDocumentChangeListener();

		@Override
		public void sessionChangeTransactionBegin() {
			result.append("begin session changes\n");
		}

		@Override
		public void sessionChangeTransactionEnd() {
			result.append("end session changes\n");
		}

		@Override
		public String toString() {
			return result.toString();
		}

		public void comment(String comment) {
			result.append(comment).append("\n");
		}

		@Override
		public void sessionChangeDocumentAdded(AMLDocument document) {
			result.append("documentAdded\n");
			//			document.addDocumentChangeListener(documentChangeListener);
		}

		@Override
		public void sessionChangeDocumentRemoving(AMLDocument document) {
			result.append("documentRemoved\n");
			
			assertThat(document).isNotNull();
			assertThat(document.isDeleted()).isFalse();
		}
	}

	static class TestDocumentChangeListener implements AMLDocumentChangeListener {
		StringBuilder result = new StringBuilder();

		@Override
		public void documentChangeTransactionBegin() {
			result.append("begin\n");
		}

		@Override
		public void documentChangeTransactionEnd() {
			result.append("end\n");
		}

		@Override
		public String toString() {
			return result.toString();
		}

		public void comment(String comment) {
			result.append(comment).append("\n");
		}

		@Override
		public void documentChangeElementDeleting(AMLDocumentElement aMLDocumentElement, AMLDocumentElement aMLParentElement) {
			result.append("Deleting ");
			result.append(aMLDocumentElement.getClass().getSimpleName());
			result.append("\n");
		}

		@Override
		public void documentChangeElementModified(AMLDocumentElement aMLDocumentElement) {
			result.append("Modified ");
			result.append(aMLDocumentElement.getClass().getSimpleName());
			result.append("\n");
		}

		@Override
		public void documentChangeElementCreated(AMLDocumentElement aMLDocumentElement, AMLDocumentElement aMLParentElement) {
			result.append("Created ");
			result.append(aMLDocumentElement.getClass().getSimpleName());
			result.append("\n");
		}

		@Override
		public void documentChangeElementValidated(AMLValidationResult validationResult) {
			result.append("Validated ");
			result.append(validationResult.getDocumentElement().getClass().getSimpleName());
			result.append(" ");
			result.append(validationResult.getSeverity());
			result.append(" ");
			result.append(validationResult.getMessage());
			result.append("\n");
		}

		@Override
		public void documentChangeDirtyStateChanged(AMLDocument document) {
		}

		@Override
		public void documentChangeElementReparented(
				AMLDocumentElement amlDocumentElement,
				AMLDocumentElement oldParent, AMLDocumentElement newParent) {
			result.append("Reparented ");
			result.append(amlDocumentElement.getClass().getSimpleName());
			result.append("\n");
		}
	}

	@Before
	public void createSession() throws Exception {
		session = amlSessionManager.createSession();
		sessionChangeListener = new TestSessionChangeListener();
		documentChangeListener = new TestDocumentChangeListener();
		session.addChangeListener(sessionChangeListener);
		savepoint = session.createSavepoint();
	}

	@After
	public void assertSessionClean() {
		savepoint.delete();
		AMLSessionImpl sessionImpl = (AMLSessionImpl) session;
		SavepointManager savepointManager = sessionImpl.getSavepointManager();
		assertThat(savepointManager.hasCurrentSavepoint()).isFalse();
		assertThat(savepointManager.getSavepoints()).isEmpty();
		assertThat(sessionImpl.getIdentifierManager().isEmpty()).describedAs("IdentifierManager is not cleaned up").isTrue();
	}

	protected void attachDocumentChangeListener(AMLDocument a) {
		a.addDocumentChangeListener(documentChangeListener);
	}

	protected void assertSessionHasNoDocuments() {
		assertThat(session.getDocuments()).hasSize(0);
	}

	protected void assertSessionHasDocuments(int count) {
		assertThat(session.getDocuments()).hasSize(count);
	}

	protected void assertChangesNotified() {
		assertThat(sessionChangeListener.result.toString()).isNotEmpty();
	}

	protected void assertNoChangesNotified() {
		//		assertThat(sessionChangeListener.result.toString()).isEmpty();
	}

	protected void resetChangeListener() {
		sessionChangeListener.result = new StringBuilder();
		documentChangeListener.result = new StringBuilder();
	}

	public static List<String> getNamesOfInterfaceClassLibraries(AMLDocument a) {
		List<String> names = new ArrayList<String>();
		for (AMLInterfaceClassLibrary lib : a.getInterfaceClassLibraries()) {
			names.add(lib.getName());
		}
		return names;
	}

	public static List<String> getNamesOfInterfaceClasses(AMLInterfaceClassLibrary lib) {
		List<String> names = new ArrayList<String>();
		for (AMLInterfaceClass interfaceClass : lib.getInterfaceClasses()) {
			names.add(interfaceClass.getName());
		}
		return names;
	}

	public static List<String> toList(String... strings) {
		List<String> list = new ArrayList<String>(strings.length);
		for (String string : strings) {
			list.add(string);
		}
		return list;
	}

}
