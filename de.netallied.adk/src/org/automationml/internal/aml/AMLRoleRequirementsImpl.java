/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLRoleRequirements;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.AbstractCreateDocumentElementChange;
import org.automationml.internal.AbstractDeleteDocumentElementChange;
import org.automationml.internal.Identifier;
import org.automationml.internal.IdentifierManager;
import org.automationml.internal.ReadOnlyIterable;

public class AMLRoleRequirementsImpl extends AMLElementImpl implements AMLRoleRequirements, AMLInternalAttributeContainer {

	protected static class DeleteRoleRequirementsChange extends AbstractDeleteDocumentElementChange<AMLRoleRequirementsImpl> {
		private Identifier<AMLRoleClassImpl> supportedRoleClassIdentifier;

		public DeleteRoleRequirementsChange(AMLRoleRequirementsImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLSupportedRoleClassImpl ref = (AMLSupportedRoleClassImpl) getDocumentElement().getSupportedRoleClass();
			if (ref != null)
				supportedRoleClassIdentifier = (Identifier<AMLRoleClassImpl>) identifierManager.getIdentifier(ref);
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement documentElement = (AbstractAMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLInternalElementImpl internalElement = (AMLInternalElementImpl) documentElement;

			AMLSupportedRoleClass supportedRoleClass = (AMLSupportedRoleClass) supportedRoleClassIdentifier.getDocumentElement();
			AMLRoleRequirementsImpl newCreatedSupportedRoleClass = internalElement._createRoleRequierments(supportedRoleClass);
			identifier.setDocumentElement(newCreatedSupportedRoleClass);
		}
		
		
		@Override
		protected void _delete() {
			super._delete();
			if (supportedRoleClassIdentifier != null)
				supportedRoleClassIdentifier.release();
			supportedRoleClassIdentifier = null;
		}
	}

	static class CreateAttributeChange extends AbstractCreateDocumentElementChange {
		private String name;
		private Identifier<AMLSupportedRoleClassImpl> supportedRoleClassIdentifier;
		private Identifier<AMLAttributeImpl> baseAttributeIdentifier;

		public CreateAttributeChange(AMLAttributeImpl documentElement, AMLAttributeImpl baseAttribute) {
			super(documentElement);
			this.name = documentElement.getName();
			baseAttributeIdentifier = (Identifier<AMLAttributeImpl>) getIdentifierManager().getIdentifier(baseAttribute);
		}
		
		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLSupportedRoleClassImpl supportedRoleClass = (AMLSupportedRoleClassImpl) ((AMLRoleRequirements) getDocumentElement().getParent()).getSupportedRoleClass();
			supportedRoleClassIdentifier = (Identifier<AMLSupportedRoleClassImpl>) identifierManager.getIdentifier(supportedRoleClass);
		}

		
		@Override
		public void redo() throws Exception {
			AMLAttributeImpl baseAttribute = baseAttributeIdentifier.getDocumentElement(); 
			AMLSupportedRoleClass supportedRoleClass = supportedRoleClassIdentifier.getDocumentElement();
			AMLRoleRequirementsImpl container = (AMLRoleRequirementsImpl) parentIdentifier.getDocumentElement();
			AMLAttributeImpl newAttribute = container._createAttribute(supportedRoleClass, baseAttribute);
			identifier.setDocumentElement(newAttribute);
		}
		
		@Override
		protected void _delete() {
			super._delete();
			if (supportedRoleClassIdentifier != null)
				supportedRoleClassIdentifier.release();
			supportedRoleClassIdentifier = null;
			if (baseAttributeIdentifier != null)
				baseAttributeIdentifier.release();
			baseAttributeIdentifier = null;
		}
	}
	
	private AMLInternalElement internalElement;
	private AMLSupportedRoleClass supportedRoleClass;
	private Map<AMLSupportedRoleClass, Map<String, AMLAttribute>> attributes = new HashMap<AMLSupportedRoleClass, Map<String, AMLAttribute>>();
	private Map<AMLAttribute, AMLSupportedRoleClass> attributeSet = new HashMap<AMLAttribute, AMLSupportedRoleClass>();

	public AMLRoleRequirementsImpl(AMLInternalElement internalElement, AMLSupportedRoleClass supportedRoleClass) {
		this.internalElement = internalElement;
		this.supportedRoleClass = supportedRoleClass;
	}

	@Override
	public AMLSupportedRoleClass getSupportedRoleClass() {
		assertNotDeleted();
		return supportedRoleClass;
	}

	@Override
	public AMLDocumentElement getParent() {
		return internalElement;
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
			DeleteRoleRequirementsChange change = new DeleteRoleRequirementsChange(this);
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
	}

	@Override
	public AMLRoleClass getRoleClass() {
		assertNotDeleted();
		return supportedRoleClass.getRoleClass();
	}

	@Override
	public AMLDocumentImpl getDocument() {
		assertNotDeleted();
		return (AMLDocumentImpl) internalElement.getDocument();
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		((AMLInternalElementImpl) internalElement).removeRoleRequirements();
	}

	@Override
	public AMLValidationResultList validateCreateAttribute(String name) {
		return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "not allowed");
	}

	@Override
	public Iterable<AMLAttribute> getAttributes() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLAttribute>(attributeSet.keySet());
	}

	@Override
	public int getAttributesCount() {
		assertNotDeleted();
		return attributeSet.size();
	}
	
	@Override
	public boolean hasAttributes() {
		return !attributeSet.isEmpty();
	}

	@Override
	public AMLAttribute getAttribute(String name) {
		assertNotDeleted();
		return AMLAttributeContainerHelper.getAttribute(this, name);
	}

	@Override
	public AMLAttribute createAttribute(String name) throws AMLValidationException {
		throw new AMLValidationException(new AMLValidationResultListImpl(this, Severity.AML_ERROR, "not allowed"));
	}

	@Override
	public Map<String, AMLAttribute> _getAttributes() {
		return null;
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateRoleRequirementDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public AMLAttribute createAttribute(String name, AMLAttribute attribute)
			throws AMLValidationException {
		throw new AMLValidationException(new AMLValidationResultListImpl(this, Severity.AML_ERROR, "not allowed"));
	}

	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "A role requirements can't be reparented");
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
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "role requirements is not part of document scope"));
				
		for (Map<String, AMLAttribute> attributeMap : attributes.values()) {
			for (AMLAttribute attribute : attributeMap.values())
				((AMLAttributeImpl)attribute).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkFrom)
			throws AMLValidationException {
	}

	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {

		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateRoleRequirementDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
		
		for (AMLAttribute attribute	: getAttributes()) {
			((AbstractAMLDocumentElement)attribute).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		
		mapping.put(original, this);

		for (AMLAttribute attribute	: ((AMLRoleRequirements) original).getAttributes()) {
			AMLSupportedRoleClass supportedRoleClass = ((AMLRoleRequirementsImpl) original).attributeSet.get(attribute);
			AMLSupportedRoleClass newSupportedRoleClass = (AMLSupportedRoleClass) mapping.get(supportedRoleClass);
			AMLAttribute baseAttribute = ((AMLAttributeImpl)attribute).getBaseAttribute();
			AMLAttribute newBaseAttribute =  (AMLAttributeImpl) mapping.get(baseAttribute);
			if (newBaseAttribute == null)
				newBaseAttribute = baseAttribute;
			AMLAttribute copyAttribute = createAttribute(newSupportedRoleClass, newBaseAttribute.getName());
			
			((AbstractAMLDocumentElement)copyAttribute).deepCopy(rootElement, rootCopyElement, attribute, mapping);
		}		
	}

	@Override
	public AMLValidationResultList validateCreateAttribute(
			AMLSupportedRoleClass supportedRoleClass, String name) {
	
		Map<String, AMLAttribute> map = attributes.get(supportedRoleClass);
		if (map != null && map.containsKey(name))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Attribute already exists for given RoleClass");
		
		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateAttributeCreate(this, supportedRoleClass, name);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public AMLAttribute createAttribute(
			AMLSupportedRoleClass supportedRoleClass, String name)
			throws AMLValidationException {
		
		AMLValidationResultList resultList = validateCreateAttribute(supportedRoleClass, name);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		((AMLDocumentImpl) getDocument()).notifyElementValidated(resultList);

		AMLAttribute baseAttribute = null;
		AMLRoleClass roleClass = supportedRoleClass.getRoleClass();
		while (baseAttribute == null && roleClass != null) {
			baseAttribute = roleClass.getAttribute(name);
			if (roleClass.getBaseRoleClass() != null)
				roleClass = roleClass.getBaseRoleClass();
		}
		AMLAttributeImpl newAttribute = _createAttribute(supportedRoleClass, baseAttribute);
				
//		roleClass = supportedRoleClass.getRoleClass();
//		AMLAttribute attribute = roleClass.getAttribute(name);
		if (((AMLSessionImpl) getSession()).getSavepointManager().hasCurrentSavepoint()) {
			CreateAttributeChange change = new CreateAttributeChange(newAttribute, (AMLAttributeImpl) baseAttribute);
			((AMLSessionImpl) getSession()).getSavepointManager().addChange(change);
		}
		newAttribute.deepCopy(baseAttribute, newAttribute, baseAttribute, new HashMap<AMLDocumentElement, AMLDocumentElement>());
		

		((AMLDocumentImpl) getDocument()).notifyElementCreated(newAttribute, this);

		return newAttribute;
	}

	void _renameAttribute(AMLAttribute attribute, String newName) {
		AMLSupportedRoleClass supportedRoleClass = attributeSet.get(attribute);
		Map<String, AMLAttribute> map = attributes.get(supportedRoleClass);
		map.remove(attribute.getName());
		map.put(newName, attribute);
	}

	@Override
	public AMLAttribute getAttribute(AMLSupportedRoleClass supportedRoleClass,
			String name) {
		Map<String, AMLAttribute> map = attributes.get(supportedRoleClass);
		if (map != null && map.containsKey(name))
			return map.get(name);
		return null;
	}

	public Map<String, AMLAttribute> _getAttributes(AMLSupportedRoleClass supportedRoleClass) {
		return attributes.get(supportedRoleClass);
	}

	void _removeAttribute(AMLAttribute attribute) {
		AMLSupportedRoleClass supportedRoleClass = attributeSet.get(attribute);
		if (supportedRoleClass != null) {
			Map<String, AMLAttribute> map = attributes.get(supportedRoleClass);
			map.remove(attribute.getName());
		}
		attributeSet.remove(attribute);
	}
	
	@Override
	protected void _doDeepDelete(AMLDocumentElement baseElement)
			throws AMLValidationException {
		while (getAttributes().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getAttributes().iterator().next())._doDeepDelete(baseElement);
		}
		super._doDeepDelete(baseElement);
	}

	public AMLAttributeImpl _createAttribute(AMLSupportedRoleClass supportedRoleClass,
			AMLAttribute attribute) throws AMLValidationException {
		
		AMLAttributeImpl newAttribute = new AMLAttributeImpl(this);
		((AMLAttributeImpl)attribute).addReferrer(newAttribute);
		Map<String, AMLAttribute> map = attributes.get(supportedRoleClass);
		if (map == null) {
			map = new HashMap<String, AMLAttribute>();
			attributes.put(supportedRoleClass, map);
		}
		attributeSet.put(newAttribute, supportedRoleClass);
		newAttribute._setName(attribute.getName());
		return newAttribute;
	}

	@Override
	public AMLSupportedRoleClass getSupportedRoleClassOfAttribute(
			AMLAttribute attribute) {
		return attributeSet.get(attribute);	 
	}

	@Override
	public AMLAttribute getAttributeOfSupportedRoleClassAttribute(AMLSupportedRoleClass supportedRoleClass,
			AMLAttribute attribute) {
		Map<String, AMLAttribute> map = attributes.get(supportedRoleClass);
		if (map == null)
			return null;
		return map.get(attribute.getName());		
	}
	
}