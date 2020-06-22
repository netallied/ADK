/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttributeContainer;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLMappingObject;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLSupportedRoleClassContainer;
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

public class AMLMappingObjectImpl extends AMLElementImpl implements AMLMappingObject {

	private static class MappingObjectMapAttributeChange extends AbstractDocumentElementChange<AMLMappingObjectImpl> {

		Identifier<AMLAttributeImpl> roleAttribute;
		Identifier<AMLAttributeImpl> attribute;
		IdentifierManager identifierManager;

		public MappingObjectMapAttributeChange(AMLMappingObjectImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			this.identifierManager = identifierManager;
		}

		public void mapAttribute(AMLAttribute roleAttribute, AMLAttribute attribute) {
			if (this.roleAttribute != null)
				this.roleAttribute.release();
			this.roleAttribute = null;

			if (this.attribute != null)
				this.attribute.release();
			this.attribute = null;
			this.roleAttribute = (Identifier<AMLAttributeImpl>) identifierManager.getIdentifier((AMLAttributeImpl) roleAttribute);
			this.attribute = (Identifier<AMLAttributeImpl>) identifierManager.getIdentifier((AMLAttributeImpl) attribute);
		}

		@Override
		public void undo() throws Exception {
			AMLMappingObjectImpl mappingObject = (AMLMappingObjectImpl) getDocumentElement();
			mappingObject._unmapAttribute(roleAttribute.getDocumentElement());
		}

		@Override
		public void redo() throws Exception {
			AMLMappingObjectImpl mappingObject = (AMLMappingObjectImpl) getDocumentElement();
			mappingObject._mapAttribute(roleAttribute.getDocumentElement(), attribute.getDocumentElement());
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof MappingObjectMapAttributeChange))
				return false;
			MappingObjectMapAttributeChange change = (MappingObjectMapAttributeChange) _change;

			change.mapAttribute(roleAttribute.getDocumentElement(), attribute.getDocumentElement());
			return true;
		}

		@Override
		protected void _delete() {
			if (roleAttribute != null)
				roleAttribute.release();
			roleAttribute = null;

			if (attribute != null)
				attribute.release();
			attribute = null;
		}

		@Override
		protected void notifyChangeListenersBeforeUndo() {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
			document.notifyElementModified(getDocumentElement());
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

	private static class MappingObjectUnmapAttributeChange extends AbstractDocumentElementChange<AMLMappingObjectImpl> {

		Identifier<AMLAttributeImpl> oldRoleAttribute;
		Identifier<AMLAttributeImpl> oldAttribute;
		IdentifierManager identifierManager;

		public MappingObjectUnmapAttributeChange(AMLMappingObjectImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			this.identifierManager = identifierManager;
		}

		public void unmapAttribute(AMLAttribute roleAttribute, AMLAttribute attribute) {
			if (this.oldRoleAttribute != null)
				this.oldRoleAttribute.release();
			this.oldRoleAttribute = null;

			if (this.oldAttribute != null)
				this.oldAttribute.release();
			this.oldAttribute = null;

			this.oldRoleAttribute = (Identifier<AMLAttributeImpl>) identifierManager.getIdentifier((AMLAttributeImpl) roleAttribute);
			this.oldAttribute = (Identifier<AMLAttributeImpl>) identifierManager.getIdentifier((AMLAttributeImpl) attribute);
		}

		@Override
		public void undo() throws Exception {
			AMLMappingObjectImpl mappingObject = (AMLMappingObjectImpl) getDocumentElement();
			mappingObject._mapAttribute(oldRoleAttribute.getDocumentElement(), oldAttribute.getDocumentElement());
		}

		@Override
		public void redo() throws Exception {
			AMLMappingObjectImpl mappingObject = (AMLMappingObjectImpl) getDocumentElement();
			mappingObject._unmapAttribute(oldRoleAttribute.getDocumentElement());
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof MappingObjectUnmapAttributeChange))
				return false;
			MappingObjectUnmapAttributeChange change = (MappingObjectUnmapAttributeChange) _change;
			change.unmapAttribute(oldRoleAttribute.getDocumentElement(), oldAttribute.getDocumentElement());
			return true;
		}

		@Override
		protected void _delete() {
			if (oldRoleAttribute != null)
				oldRoleAttribute.release();
			oldRoleAttribute = null;

			if (oldAttribute != null)
				oldAttribute.release();
			oldAttribute = null;
		}

		@Override
		protected void notifyChangeListenersBeforeUndo() {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
			document.notifyElementModified(getDocumentElement());
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

	protected static class DeleteMappingObjectChange extends AbstractDeleteDocumentElementChange<AMLMappingObjectImpl> {

		private Identifier<AMLRoleClassImpl> roleClassIdentifier;
		private Identifier<AMLSupportedRoleClassImpl> srcIdentifier;
		private Map<Identifier<AMLAttributeImpl>, Identifier<AMLAttributeImpl>> mapping;

		public DeleteMappingObjectChange(AMLMappingObjectImpl element) {
			super(element);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
			super.initializeIdentifiers(identifierManager);
			AMLRoleClassImpl ref = (AMLRoleClassImpl) getDocumentElement().getRoleClass();
			AMLSupportedRoleClassImpl ref2 = (AMLSupportedRoleClassImpl) getDocumentElement().getSupportedRoleClass();
			if (ref != null)
				roleClassIdentifier = (Identifier<AMLRoleClassImpl>) identifierManager.getIdentifier(ref);
			
			if (ref2 != null)
				srcIdentifier = (Identifier<AMLSupportedRoleClassImpl>) identifierManager.getIdentifier(ref2);
			
			mapping = new HashMap<Identifier<AMLAttributeImpl>, Identifier<AMLAttributeImpl>>();
			for (AMLAttribute attribute : getDocumentElement().attributeMapping.keySet()) {
				AMLAttributeImpl keyAttr = (AMLAttributeImpl)attribute;
				AMLAttributeImpl valAttr = (AMLAttributeImpl)getDocumentElement().attributeMapping.get(attribute);
				Identifier<AMLAttributeImpl> keyId = (Identifier<AMLAttributeImpl>) identifierManager.getIdentifier(keyAttr);
				Identifier<AMLAttributeImpl> valId = (Identifier<AMLAttributeImpl>) identifierManager.getIdentifier(valAttr);
				mapping.put(keyId, valId);
			}
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement documentElement = (AbstractAMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLInternalSupportedRoleClassContainer parent = (AMLInternalSupportedRoleClassContainer) documentElement;

			AMLRoleClass roleClass = roleClassIdentifier.getDocumentElement();
			AMLSupportedRoleClassImpl src = srcIdentifier.getDocumentElement();
			AMLMappingObjectImpl newMappingObjectClass = (AMLMappingObjectImpl) AMLInternalElementContainerHelper._createMappingObject(parent, roleClass);
			identifier.setDocumentElement(newMappingObjectClass);
			src.mappingObject = newMappingObjectClass;
			newMappingObjectClass.setSupportedRoleClass(src);
			for (Identifier<AMLAttributeImpl> attId : mapping.keySet()) {
				Identifier<AMLAttributeImpl> valId = mapping.get(attId);
				AMLAttributeImpl keyAttr = attId.getDocumentElement();
				AMLAttributeImpl valAttr = valId.getDocumentElement();
				
				newMappingObjectClass.mapAttribute(keyAttr, valAttr);
				
			}
		}
		
		@Override
		protected void _delete() {
			if (roleClassIdentifier != null)
				roleClassIdentifier.release();
			if (srcIdentifier != null)
				srcIdentifier.release();
			for (Identifier<AMLAttributeImpl> attId : mapping.keySet()) {
				Identifier<AMLAttributeImpl> valId = mapping.get(attId);
				attId.release();
				valId.release();
			}
			super._delete();
		}
	}

	private AMLRoleClassImpl roleClass;
	private AMLSupportedRoleClassContainer supportedRoleClassContainer;
	private AMLSupportedRoleClass supportedRoleClass;
	private Map<AMLAttribute, AMLAttribute> attributeMapping = new LinkedHashMap<AMLAttribute, AMLAttribute>();

	public AMLMappingObjectImpl(AMLSupportedRoleClassContainer supportedRoleClassContainer, AMLRoleClass roleClass) {
		this.supportedRoleClassContainer = supportedRoleClassContainer;
		this.roleClass = (AMLRoleClassImpl) roleClass;
	}

	private void _unmapAttribute(AMLAttribute roleAttribute) {
		attributeMapping.remove(roleAttribute);
	}

	public void setSupportedRoleClass(AMLSupportedRoleClass supportedRoleClass) {
		this.supportedRoleClass = supportedRoleClass;
	}
	
	public AMLSupportedRoleClass getSupportedRoleClass() {
		return supportedRoleClass;
	}

	@Override
	public AMLDocumentElement getParent() {
		return supportedRoleClassContainer;
	}

	@Override
	public void delete() throws AMLValidationException {
		if (isDeleted())
			return;

		// AMLValidationResultList validationResult = AMLSupportedRoleClassContainerHelper.validateDeleteSupportedRoleClass(this);
		// if (validationResult.isAnyOperationNotPermitted())
		// throw new AMLValidationException(validationResult);
		//
		// getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			DeleteMappingObjectChange change = new DeleteMappingObjectChange(this);
			getSavepointManager().addChange(change);
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
	}

	@Override
	public void mapAttribute(AMLAttribute roleAttribute, AMLAttribute attribute) throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList resultList = validateMapAttribute(roleAttribute, attribute);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			MappingObjectMapAttributeChange change = new MappingObjectMapAttributeChange(this);
			change.mapAttribute(roleAttribute, attribute);
			getSavepointManager().addChange(change);
		}
		_mapAttribute(roleAttribute, attribute);
		getDocument().notifyElementModified(this);
	}

	@Override
	public AMLValidationResultList validateMapAttribute(AMLAttribute roleAttribute, AMLAttribute attribute) {
		assertNotDeleted();

		AMLAttributeContainer roleAttributeContainer = (AMLAttributeContainer) roleAttribute.getParent();
		while (roleAttributeContainer.getParent() instanceof AMLAttributeContainer) {
			if (roleAttributeContainer == roleClass)
				break;
		}

		AMLAttributeContainer attributeContainer = (AMLAttributeContainer) attribute.getParent();
		while (attributeContainer.getParent() instanceof AMLAttributeContainer) {
			if (attributeContainer == supportedRoleClassContainer)
				break;
		}

		if (roleAttributeContainer != roleClass || attributeContainer != supportedRoleClassContainer)
			return new AMLValidationResultListImpl(supportedRoleClassContainer, Severity.AML_ERROR, "Invalid mapping object");
		
		if (attributeMapping.get(roleAttribute) != null)
			return new AMLValidationResultListImpl(supportedRoleClassContainer, Severity.AML_ERROR, "Attribute already mapped");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateMapAttribute(this, roleAttribute, attribute);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	private void _mapAttribute(AMLAttribute roleAttribute, AMLAttribute attribute) {
		attributeMapping.put(roleAttribute, attribute);
	}

	@Override
	public AMLDocumentImpl getDocument() {
		return (AMLDocumentImpl) supportedRoleClassContainer.getDocument();
	}

	@Override
	protected void _doDelete() throws AMLValidationException {

	}

	@Override
	public AMLAttribute getMappedAttribute(AMLAttribute attribute) {
		assertNotDeleted();
		return attributeMapping.get(attribute);
	}

	public AMLRoleClassImpl getRoleClass() {
		return roleClass;
	}

	@Override
	public Iterable<AMLAttribute> getMappedRoleAttributes() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLAttribute>(attributeMapping.keySet());
	}

	@Override
	public void unmapAttribute(AMLAttribute roleAttribute) throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList resultList = validateUnmapAttribute(roleAttribute);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			MappingObjectUnmapAttributeChange change = new MappingObjectUnmapAttributeChange(this);
			AMLAttribute mappedAttribute = attributeMapping.get(roleAttribute);
			change.unmapAttribute(roleAttribute, mappedAttribute);
			getSavepointManager().addChange(change);
		}
		_unmapAttribute(roleAttribute);
		getDocument().notifyElementModified(this);
	}

	@Override
	public AMLValidationResultList validateUnmapAttribute(AMLAttribute roleAttribute) {
		assertNotDeleted();

		AMLAttribute mappedAttribute = attributeMapping.get(roleAttribute);
		if (mappedAttribute == null)
			return new AMLValidationResultListImpl(this, Severity.OK, "");

		AMLAttributeContainer roleAttributeContainer = (AMLAttributeContainer) roleAttribute.getParent();
		while (roleAttributeContainer.getParent() instanceof AMLAttributeContainer) {
			if (roleAttributeContainer == roleClass)
				break;
		}

		AMLAttributeContainer attributeContainer = (AMLAttributeContainer) mappedAttribute.getParent();
		while (attributeContainer.getParent() instanceof AMLAttributeContainer) {
			if (attributeContainer == supportedRoleClassContainer)
				break;
		}

		if (roleAttributeContainer != roleClass || attributeContainer != supportedRoleClassContainer)
			return new AMLValidationResultListImpl(supportedRoleClassContainer, Severity.AML_ERROR, "Invalid mapping object");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateUnmapAttribute(this, roleAttribute);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public boolean isAttributeMapped(AMLAttribute attribute) {
		return attributeMapping.containsValue(attribute);
	}

	@Override
	public boolean hasAttributeMappings() {
		return !attributeMapping.isEmpty();
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "A mappingobject mustn't be reparented");
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
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkFrom)
			throws AMLValidationException {
	}

	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		mapping.put(original, this);

		for (AMLAttribute attribute	: ((AMLMappingObjectImpl) original).attributeMapping.keySet()) {
			AMLAttribute mappedAttribute = ((AMLMappingObjectImpl) original).attributeMapping.get(attribute);
			
			AMLAttribute newAttribute = (AMLAttribute) mapping.get(attribute);
			AMLAttribute newMappedAttribute = (AMLAttribute) mapping.get(mappedAttribute);
			if (newAttribute == null)
				newAttribute = attribute;
			if (newMappedAttribute != null)
				mapAttribute(newAttribute, newMappedAttribute);
		}		
	}

	@Override
	public int getMappedRoleAttributeCount() {
		return attributeMapping.size();
	}
}
