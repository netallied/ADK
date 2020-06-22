/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.ArrayList;
import java.util.List;

import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLInternalElementContainer;
import org.automationml.aml.AMLInternalLink;
import org.automationml.aml.AMLInternalLinkContainer;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.internal.AbstractCreateDocumentElementChange;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;

public class AMLInternalLinkContainerHelper {
	
	private static class CreateInternalLinkChange extends AbstractCreateDocumentElementChange {

		private Identifier<AMLExternalInterfaceImpl> refPartnerSideA;
		private Identifier<AMLExternalInterfaceImpl> refPartnerSideB;
		private String name;

		public CreateInternalLinkChange(AMLInternalLinkImpl documentElement) {
			super(documentElement);
			name = documentElement.getName();
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLExternalInterfaceImpl refPartnerSideA = (AMLExternalInterfaceImpl) ((AMLInternalLinkImpl) getDocumentElement()).getRefPartnerSideA();
			this.refPartnerSideA = (Identifier<AMLExternalInterfaceImpl>) identifierManager.getIdentifier(refPartnerSideA);
			AMLExternalInterfaceImpl refPartnerSideB = (AMLExternalInterfaceImpl) ((AMLInternalLinkImpl) getDocumentElement()).getRefPartnerSideB();
			this.refPartnerSideB = (Identifier<AMLExternalInterfaceImpl>) identifierManager.getIdentifier(refPartnerSideB);
		}

		@Override
		public void redo() throws Exception {
			AMLInternalElementImpl internalElement = (AMLInternalElementImpl) parentIdentifier.getDocumentElement();
			AMLExternalInterfaceImpl refPartnerSideA = this.refPartnerSideA.getDocumentElement();
			AMLExternalInterfaceImpl refPartnerSideB = this.refPartnerSideB.getDocumentElement();
			AMLInternalLinkImpl internalLink = internalElement._createInternalLink(name, refPartnerSideA, refPartnerSideB);
			identifier.setDocumentElement(internalLink);
		}

		@Override
		protected void _delete() {
			super._delete();
			if (refPartnerSideA != null)
				refPartnerSideA.release();
			refPartnerSideA = null;
			if (refPartnerSideB != null)
				refPartnerSideB.release();
			refPartnerSideB = null;
		}
	}

	public static AMLInternalLink createInternalLink(AMLInternalInternalLinkContainer container, String linkName,
			AMLExternalInterface refPartnerSideA, AMLExternalInterface refPartnerSideB) throws AMLValidationException {
		AMLDocumentImpl document = (AMLDocumentImpl) container.getDocument();

		AMLValidationResultList resultList = validateCreateInternalLink(container, linkName, refPartnerSideA, refPartnerSideB);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		document.notifyElementValidated(resultList);

		AMLInternalLinkImpl internalLink = _createInternalLink(container, linkName, refPartnerSideA, refPartnerSideB);

		if (((AMLSessionImpl) container.getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateInternalLinkChange change = new CreateInternalLinkChange(internalLink);
			((AMLSessionImpl) container.getSession()).getSavepointManager().addChange(change);
		}

		document.notifyElementCreated(internalLink, container);

		return internalLink;
	}

	public static AMLInternalLinkImpl _createInternalLink(AMLInternalInternalLinkContainer container, String linkName,
			AMLExternalInterface refPartnerSideA, AMLExternalInterface refPartnerSideB) {
		AMLInternalLinkImpl internalLink = new AMLInternalLinkImpl(container, linkName, refPartnerSideA, refPartnerSideB);
		AMLExternalInterfaceImpl externalInterface = (AMLExternalInterfaceImpl) refPartnerSideA;
		externalInterface._addInternalLink(internalLink);
		externalInterface = (AMLExternalInterfaceImpl) refPartnerSideB;
		externalInterface._addInternalLink(internalLink);
		container._getInternalLinks().add(internalLink);
		return internalLink;
	}

	static AMLValidationResultList validateCreateInternalLink(AMLInternalInternalLinkContainer container, String linkName,
			AMLExternalInterface refPartnerSideA, AMLExternalInterface refPartnerSideB) {
		if (refPartnerSideA == null || refPartnerSideB == null)
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "ExternalInterface elements not specified");

		List<AMLInternalLinkContainer> parentsA = new ArrayList<AMLInternalLinkContainer>();
		AMLInternalLinkContainer container1 = (AMLInternalLinkContainer) refPartnerSideA.getParent();
		while (container1.getParent() instanceof AMLInternalLinkContainer) {
			parentsA.add(0, (AMLInternalLinkContainer) container1);
			container1 = (AMLInternalLinkContainer) container1.getParent();
		}
		if (container1 instanceof AMLInternalLinkContainer)
			parentsA.add(0, (AMLInternalLinkContainer) container1);
		List<AMLInternalLinkContainer> parentsB = new ArrayList<AMLInternalLinkContainer>();
		container1 = (AMLInternalLinkContainer) refPartnerSideB.getParent();
		while (container1.getParent() instanceof AMLInternalLinkContainer) {
			parentsB.add(0, (AMLInternalLinkContainer) container1);
			container1 = (AMLInternalLinkContainer) container1.getParent();
		}
		if (container1 instanceof AMLInternalLinkContainer)
			parentsB.add(0, (AMLInternalLinkContainer) container1);
		int i = 0;
		for (; i < parentsA.size() && i < parentsB.size(); i++) {
			if (parentsA.get(i) != parentsB.get(i))
				return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Wrong InternalElement selected");
			if (parentsA.get(i) == container)
				break;
		}
		if (parentsA.get(i) != container)
			return new AMLValidationResultListImpl(container, Severity.AML_ERROR, "Wrong InternalElement selected");

		AMLValidator validator = container.getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInternalLinkCreate(container, linkName, refPartnerSideA,
					refPartnerSideB);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(container, Severity.OK, "");
	}
}
