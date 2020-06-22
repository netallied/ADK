/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttributeContainer;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLFacet;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLNameAlreadyInUseException;
import org.automationml.aml.AMLRoleRequirements;
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

//FIXME 
public class AMLAttributeImpl extends AbstractAMLDocumentElement implements AMLAttribute, AMLInternalAttributeContainer {

	private static class DeleteAttributeChange extends AbstractDeleteDocumentElementChange<AMLAttributeImpl> {
		private String name;
		private String dataType;
		private String unit;
		private String description;
		private String defaultValue;
		private String value;

		public DeleteAttributeChange(AMLAttributeImpl element) {
			super(element);
			name = element.getName();
			dataType = element.getDataType();
			unit = element.getUnit();
			description = element.getDescription();
			defaultValue = element.getDefaultValue();
			value = element.getValue();
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement documentElement = (AbstractAMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLInternalAttributeContainer parent = (AMLInternalAttributeContainer) documentElement;

			AMLAttributeImpl newCreatedAttribute = (AMLAttributeImpl) AMLAttributeContainerHelper._createAttribute(parent, name);
			identifier.setDocumentElement(newCreatedAttribute);
			newCreatedAttribute.setDataType(dataType);
			newCreatedAttribute.setUnit(unit);
			newCreatedAttribute.setDescription(description);
			newCreatedAttribute.setDefaultValue(defaultValue);
			newCreatedAttribute.setValue(value);
		}
	}
	
	private static class DeleteRoleRequirementsAttributeChange extends AbstractDeleteDocumentElementChange<AMLAttributeImpl> {
		private String name;
		private String dataType;
		private String unit;
		private String description;
		private String defaultValue;
		private String value;
		private Identifier<AMLSupportedRoleClassImpl> supportedRoleClassIdentifier;
		private Identifier<AMLAttributeImpl> baseAttributeIdentifier;

		public DeleteRoleRequirementsAttributeChange(AMLAttributeImpl element) {
			super(element);
			name = element.getName();
			dataType = element.getDataType();
			unit = element.getUnit();
			description = element.getDescription();
			defaultValue = element.getDefaultValue();
			value = element.getValue();
			baseAttributeIdentifier = (Identifier<AMLAttributeImpl>) getIdentifierManager().getIdentifier((AbstractAMLDocumentElement) element.getBaseAttribute());
			AMLRoleRequirementsImpl parent = (AMLRoleRequirementsImpl) element.getParent();
			supportedRoleClassIdentifier = (Identifier<AMLSupportedRoleClassImpl>) getIdentifierManager().getIdentifier((AbstractAMLDocumentElement) parent.getSupportedRoleClassOfAttribute(element));
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement documentElement = (AbstractAMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLRoleRequirementsImpl parent = (AMLRoleRequirementsImpl) documentElement;
			AMLSupportedRoleClassImpl supportedRoleClass = supportedRoleClassIdentifier.getDocumentElement();
			AMLAttributeImpl baseAttribute = baseAttributeIdentifier.getDocumentElement();

			AMLAttributeImpl newCreatedAttribute = (AMLAttributeImpl) parent._createAttribute(supportedRoleClass, baseAttribute);
			identifier.setDocumentElement(newCreatedAttribute);
			newCreatedAttribute.setDataType(dataType);
			newCreatedAttribute.setUnit(unit);
			newCreatedAttribute.setDescription(description);
			newCreatedAttribute.setDefaultValue(defaultValue);
			newCreatedAttribute.setValue(value);
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

	private static class ModifyAttributeChange extends AbstractDocumentElementChange<AMLAttributeImpl> {

		public final static int CHANGE_NAME = 1;
		public final static int CHANGE_DATA_TYPE = 2;
		public final static int CHANGE_UNIT = 3;
		public final static int CHANGE_DESCRIPTION = 4;
		public final static int CHANGE_DEFAULT_VALUE = 5;
		public final static int CHANGE_VALUE = 6;
		private String oldValue;
		private String newValue;
		private int changedItem;

		public ModifyAttributeChange(int changedItem, AMLAttributeImpl element) {
			super(element);
			this.changedItem = changedItem;
			AMLSessionImpl session = (AMLSessionImpl) element.getSession();
			IdentifierManager identifierManager = session.getIdentifierManager();
			_initializeIdentifiers(identifierManager);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
		}

		protected void _initializeIdentifiers(IdentifierManager identifierManager) {
			AMLAttributeImpl attribute = (AMLAttributeImpl) getDocumentElement();
			switch (changedItem) {
			case CHANGE_NAME:
				this.oldValue = attribute.getName();
				break;
			case CHANGE_DATA_TYPE:
				this.oldValue = attribute.getDataType();
				break;
			case CHANGE_UNIT:
				this.oldValue = attribute.getUnit();
				break;
			case CHANGE_DESCRIPTION:
				this.oldValue = attribute.getDescription();
				break;
			case CHANGE_DEFAULT_VALUE:
				this.oldValue = attribute.getDefaultValue();
				break;
			case CHANGE_VALUE:
				this.oldValue = attribute.getValue();
				break;
			default:
				break;
			}

		}

		public void setNewValue(String newName) {
			this.newValue = newName;
		}

		@Override
		public void undo() throws Exception {
			AMLAttributeImpl attribute = (AMLAttributeImpl) getDocumentElement();
			switch (changedItem) {
			case CHANGE_NAME:
				if (newValue == null)
					newValue = attribute.getName();
				attribute._setName(oldValue);
				break;
			case CHANGE_DATA_TYPE:
				if (newValue == null)
					newValue = attribute.getDataType();
				attribute._setDataType(oldValue);
				break;
			case CHANGE_UNIT:
				if (newValue == null)
					newValue = attribute.getUnit();
				attribute._setUnit(oldValue);
				break;
			case CHANGE_DESCRIPTION:
				if (newValue == null)
					newValue = attribute.getDescription();
				attribute._setDescription(oldValue);
				break;
			case CHANGE_DEFAULT_VALUE:
				if (newValue == null)
					newValue = attribute.getDefaultValue();
				attribute._setDefaultValue(oldValue);
				break;
			case CHANGE_VALUE:
				if (newValue == null)
					newValue = attribute.getValue();
				attribute._setValue(oldValue);
				break;
			default:
				break;
			}

		}

		@Override
		public void redo() throws Exception {
			AMLAttributeImpl attribute = (AMLAttributeImpl) getDocumentElement();

			switch (changedItem) {
			case CHANGE_NAME:
				attribute._setName(newValue);
				break;
			case CHANGE_DATA_TYPE:
				attribute._setDataType(newValue);
				break;
			case CHANGE_UNIT:
				attribute._setUnit(newValue);
				break;
			case CHANGE_DESCRIPTION:
				attribute._setDescription(newValue);
				break;
			case CHANGE_DEFAULT_VALUE:
				attribute._setDefaultValue(newValue);
				break;
			case CHANGE_VALUE:
				attribute._setValue(newValue);
				break;
			default:
				break;
			}
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyAttributeChange))
				return false;
			ModifyAttributeChange change = (ModifyAttributeChange) _change;
			change.setNewValue(newValue);
			return true;
		}

		@Override
		protected void _delete() {
			// this is not really required, but helpful for debugging
			oldValue = null;
			newValue = null;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + changedItem;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ModifyAttributeChange other = (ModifyAttributeChange) obj;
			if (changedItem != other.changedItem)
				return false;
			return true;
		}

	}
	
	
	private static class AddRefSemanticChange extends AbstractDocumentElementChange<AMLAttributeImpl> {

		private String refSemantic;
		
		public AddRefSemanticChange(AbstractAMLDocumentElement documentElement) {
			super(documentElement);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
		}

		@Override
		protected void _delete() {
			// this is not really required, but helpful for debugging
			refSemantic = null;
		}

		@Override
		public void undo() throws Exception {
			AMLAttributeImpl attribute = (AMLAttributeImpl) getDocumentElement();		
			attribute._removeRefSemantic(refSemantic);
		}

		@Override
		public void redo() throws Exception {
			AMLAttributeImpl attribute = (AMLAttributeImpl) getDocumentElement();
			attribute._addRefSemantic(refSemantic);
		}

		@Override
		public boolean mergeInto(Change change) {
			if (!(change instanceof AddRefSemanticChange))
				return false;
			AddRefSemanticChange _change = (AddRefSemanticChange) change;
			_change.setRefSemantic(refSemantic);
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
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void notifyChangeListenersAfterRedo() {
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
			document.notifyElementModified(getDocumentElement());
		}

		public void setRefSemantic(String refSemantic) {
			this.refSemantic = refSemantic;			
		}
		
	}
	
	private static class RemoveRefSemanticChange extends AbstractDocumentElementChange<AMLAttributeImpl> {

		private String refSemantic;
		
		public RemoveRefSemanticChange(AbstractAMLDocumentElement documentElement) {
			super(documentElement);
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
		}

		@Override
		protected void _delete() {
			// this is not really required, but helpful for debugging
			refSemantic = null;
		}

		@Override
		public void undo() throws Exception {
			AMLAttributeImpl attribute = (AMLAttributeImpl) getDocumentElement();		
			attribute._addRefSemantic(refSemantic);
		}

		@Override
		public void redo() throws Exception {
			AMLAttributeImpl attribute = (AMLAttributeImpl) getDocumentElement();
			attribute._removeRefSemantic(refSemantic);
		}

		@Override
		public boolean mergeInto(Change change) {
			if (!(change instanceof AddRefSemanticChange))
				return false;
			AddRefSemanticChange _change = (AddRefSemanticChange) change;
			_change.setRefSemantic(refSemantic);
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
			AMLDocumentImpl document = (AMLDocumentImpl) getDocumentElement().getDocument();
			document.notifyElementModified(getDocumentElement());
			
		}

		@Override
		protected void notifyChangeListenersAfterRedo() {
			
		}

		public void setRefSemantic(String refSemantic) {
			this.refSemantic = refSemantic;			
		}
		
	}
	
	private static class CreateConstraintChange extends AbstractCreateDocumentElementChange {
		private String name;
		private Class clazz;

		public CreateConstraintChange(ConstraintImpl documentElement) {
			super(documentElement);
			this.name = documentElement.getName();
			clazz = documentElement.getClass();
		}

		@Override
		public void redo() throws Exception {
			AMLAttributeImpl attribute = (AMLAttributeImpl) parentIdentifier.getDocumentElement();
			ConstraintImpl constraint = null;
			if (clazz.equals(UnknownConstraintImpl.class)) {
				UnknownConstraintImpl uc = attribute._createUnknownConstraint(name);
				constraint = uc;
			} else if (clazz.equals(OrdinalScaledConstraintImpl.class)) {
				OrdinalScaledConstraintImpl oc = attribute._createOrdinalScaledConstraint(name);
				constraint = oc;
			} else if (clazz.equals(NominalScaledConstraintImpl.class)) {
				NominalScaledConstraintImpl nc = attribute._createNominalScaledConstraint(name);
				constraint = nc;
			}
			identifier.setDocumentElement(constraint);
		}
	}
	
	private static class RemoveConstraintChange extends AbstractDeleteDocumentElementChange<ConstraintImpl> {

		private String name;
		private String requirement;	
		private String requiredMaxValue;
		private String requiredMinValue;
		private String requiredValue;
		private List<String> requiredValues = new ArrayList<String>();
		private Class clazz;
		
		public RemoveConstraintChange(ConstraintImpl documentElement) {
			super(documentElement);
			this.name = documentElement.getName();
			clazz = documentElement.getClass();
			if (clazz.equals(UnknownConstraintImpl.class)) {
				this.requirement = ((UnknownConstraintImpl)documentElement).getRequirement();
			} else if (clazz.equals(OrdinalScaledConstraintImpl.class)) {
				requiredMaxValue = ((OrdinalScaledConstraintImpl)documentElement).getRequiredMaxValue();
				requiredMinValue = ((OrdinalScaledConstraintImpl)documentElement).getRequiredMinValue();
				requiredValue = ((OrdinalScaledConstraintImpl)documentElement).getRequiredValue();
			} else if (clazz.equals(NominalScaledConstraintImpl.class)) {
				for (String value : ((NominalScaledConstraintImpl)documentElement).getRequiredValues())
					requiredValues.add(value);
			}
		}

		@Override
		public void undo() throws Exception {
			AbstractAMLDocumentElement documentElement = (AbstractAMLDocumentElement) parentIdentifier.getDocumentElement();
			AMLAttributeImpl attribute = (AMLAttributeImpl) documentElement;
			ConstraintImpl constraint = null;
			if (clazz.equals(UnknownConstraintImpl.class)) {
				UnknownConstraintImpl uc = attribute._createUnknownConstraint(name);
				uc.setRequirement(requirement);
				constraint = uc;
			} else if (clazz.equals(OrdinalScaledConstraintImpl.class)) {
				OrdinalScaledConstraintImpl oc = attribute._createOrdinalScaledConstraint(name);
				oc.setRequiredMaxValue(requiredMaxValue);
				oc.setRequiredMinValue(requiredMinValue);
				oc.setRequiredValue(requiredValue);
				constraint = oc;
			} else if (clazz.equals(NominalScaledConstraintImpl.class)) {
				NominalScaledConstraintImpl nc = attribute._createNominalScaledConstraint(name);
				for (String value : requiredValues)
					nc.addRequiredValue(value);
				constraint = nc;
			}
			identifier.setDocumentElement(constraint);			
		}
		
	}
	
	private static class ModifyConstraintChange extends AbstractDocumentElementChange<ConstraintImpl> {

		private String oldname;
		private String oldrequirement;	
		private String oldrequiredMaxValue;
		private String oldrequiredMinValue;
		private String oldrequiredValue;
		private List<String> oldrequiredValues = new ArrayList<String>();
		public String newname;
		public String newrequirement;	
		public String newrequiredMaxValue;
		public String newrequiredMinValue;
		public String newrequiredValue;
		public List<String> newrequiredValues = new ArrayList<String>();
		private Class clazz;
		
		public ModifyConstraintChange(AbstractAMLDocumentElement documentElement) {
			super(documentElement);
			AMLSessionImpl session = (AMLSessionImpl) documentElement.getSession();
			clazz = documentElement.getClass();
			ConstraintImpl constraint = (ConstraintImpl) getDocumentElement();
			this.oldname = constraint.getName();
			if (clazz.equals(UnknownConstraintImpl.class)) {
				this.oldrequirement = ((UnknownConstraintImpl)constraint).getRequirement();
			} else if (clazz.equals(OrdinalScaledConstraintImpl.class)) {
				this.oldrequiredMaxValue = ((OrdinalScaledConstraintImpl)constraint).getRequiredMaxValue();
				this.oldrequiredMinValue = ((OrdinalScaledConstraintImpl)constraint).getRequiredMinValue();
				this.oldrequiredValue = ((OrdinalScaledConstraintImpl)constraint).getRequiredValue();				
			} else if (clazz.equals(NominalScaledConstraintImpl.class)) {
				for (String value : ((NominalScaledConstraintImpl)constraint).getRequiredValues())
				oldrequiredValues.add(value);
			}
		}

		@Override
		protected void initializeIdentifiers(IdentifierManager identifierManager) {
		}

		@Override
		protected void _delete() {
			oldname = null;
			oldrequirement = null;	
			oldrequiredMaxValue = null;
			oldrequiredMinValue = null;
			oldrequiredValue = null;
			oldrequiredValues = null;
			newname = null;
			newrequirement = null;	
			newrequiredMaxValue = null;
			newrequiredMinValue = null;
			newrequiredValue = null;
			newrequiredValues = null;
			clazz = null;
		}

		@Override
		public void undo() throws Exception {
			ConstraintImpl constraint = (ConstraintImpl) getDocumentElement();
			if (newname == null)
				newname = constraint.getName();
			constraint._setName(oldname);
			if (clazz.equals(UnknownConstraintImpl.class)) {
				if (newrequirement == null)
					newrequirement = ((UnknownConstraintImpl)constraint).getRequirement();
				((UnknownConstraintImpl)constraint)._setRequirement(this.oldrequirement);
			} else if (clazz.equals(OrdinalScaledConstraintImpl.class)) {
				if (newrequiredMaxValue == null)
					newrequiredMaxValue = ((OrdinalScaledConstraintImpl)constraint).getRequiredMaxValue();
				((OrdinalScaledConstraintImpl)constraint)._setRequiredMaxValue(oldrequiredMaxValue);
				if (newrequiredMinValue == null)
					newrequiredMinValue = ((OrdinalScaledConstraintImpl)constraint).getRequiredMinValue();
				((OrdinalScaledConstraintImpl)constraint)._setRequiredMinValue(oldrequiredMinValue);
				if (newrequiredValue == null)
					newrequiredValue = ((OrdinalScaledConstraintImpl)constraint).getRequiredValue();
				((OrdinalScaledConstraintImpl)constraint)._setRequiredValue(oldrequiredValue);
			} else if (clazz.equals(NominalScaledConstraintImpl.class)) {
				if (newrequiredValues == null)
					for (String value : ((NominalScaledConstraintImpl)constraint).getRequiredValues())
						newrequiredValues.add(value);
				((NominalScaledConstraintImpl)constraint)._clearRequiredValues();
				for (String value : oldrequiredValues)
					((NominalScaledConstraintImpl)constraint)._addRequiredValue(value);
			}
		}

		@Override
		public void redo() throws Exception {
			ConstraintImpl constraint = (ConstraintImpl) getDocumentElement();
			constraint._setName(newname);
			if (clazz.equals(UnknownConstraintImpl.class)) {
				((UnknownConstraintImpl)constraint)._setRequirement(this.newrequirement);
			} else if (clazz.equals(OrdinalScaledConstraintImpl.class)) {
				((OrdinalScaledConstraintImpl)constraint)._setRequiredMaxValue(newrequiredMaxValue);
				((OrdinalScaledConstraintImpl)constraint)._setRequiredMinValue(newrequiredMinValue);
				((OrdinalScaledConstraintImpl)constraint)._setRequiredValue(newrequiredValue);
			} else if (clazz.equals(NominalScaledConstraintImpl.class)) {
				((NominalScaledConstraintImpl)constraint)._clearRequiredValues();
				for (String value : newrequiredValues)
					((NominalScaledConstraintImpl)constraint)._addRequiredValue(value);
			}
			
		}

		@Override
		public boolean mergeInto(Change _change) {
			if (!(_change instanceof ModifyConstraintChange))
				return false;
			ModifyConstraintChange change = (ModifyConstraintChange) _change;
			change.newname = newname;
			change.newrequirement = newrequirement;
			change.newrequiredMaxValue = newrequiredMaxValue;
			change.newrequiredMinValue = newrequiredMinValue;
			change.newrequiredValue = newrequiredValue;
			change.newrequiredValues.clear();
			for (String value : newrequiredValues)
				change.newrequiredValues.add(value);
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
	
	private abstract class ConstraintImpl extends AbstractAMLDocumentElement implements Constraint {
		
		private String name;
		private AMLAttributeImpl attribute;
		
		public ConstraintImpl(String name, AMLAttributeImpl attribute) {
			super();
			this.name = name;
			this.attribute = attribute;
		}

		@Override
		public String getName() {
			assertNotDeleted();
			return name;
		}

		@Override
		public AMLSessionImpl getSession() {
			return attribute.getSession();
		}

		@Override
		public AMLDocumentImpl getDocument() {
			return attribute.getDocument();
		}

		@Override
		public AMLDocumentElement getParent() {
			return attribute;
		}

		@Override
		public AMLValidationResultList validateDelete() {
			AMLValidator validator = getSession().getValidator();
			return doValidateDelete(validator);
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
				RemoveConstraintChange change = new RemoveConstraintChange(this);
				getSavepointManager().addChange(change);
			}

			getDocument().notifyElementDeleting(this, getParent());

			_delete();
		}

		@Override
		protected void _doDelete() throws AMLValidationException {
			attribute._removeConstraint(this);
		}

		@Override
		protected AMLValidationResultList doValidateDelete(
				AMLValidator validator) {
			return new AMLValidationResultListImpl(this, Severity.OK, "");
		}

		@Override
		protected AMLValidationResultList doValidateReparent(
				AMLValidator validator, AMLDocumentElement oldParentElement,
				AMLDocumentElement newParentElement,
				AMLDocumentElement beforeElement,
				AMLDocumentElement afterElement) {
			return null;
		}

		@Override
		protected void _reparent(AMLDocumentElement oldParentElement,
				AMLDocumentElement newParentElement,
				AMLDocumentElement beforeElement,
				AMLDocumentElement afterElement) {
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
				result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "constraint is not part of document scope"));
			
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
		public AMLValidationResultList validateNameChange(String newName) {
			return _validateNameChange(newName);
		}

		
		protected AMLValidationResultList _validateNameChange(String newName) {
			if (newName != null && newName.equals(this.name))
				return new AMLValidationResultListImpl(this, Severity.OK, "");
			if (newName.isEmpty())
				return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name must not be empty");

			AMLValidationResultList resultList = attribute.validateRenameConstraint(newName);
			if (!resultList.isOk())
				return resultList;
			AMLValidator validator = getSession().getValidator();
			if (validator == null)
				return resultList;
			return validator.validateConstraintSetName(this, newName);
		}

		@Override
		public void setName(String newName) throws AMLValidationException {
			assertNotDeleted();
			if (newName != null && newName.equals(this.name))
				return;

			AMLValidationResultList validationResult = _validateNameChange(newName);
			if (validationResult.isAnyOperationNotPermitted())
				throw new AMLValidationException(validationResult);

			getDocument().notifyElementValidated(validationResult);

			if (getSession().getSavepointManager().hasCurrentSavepoint()) {
				ModifyConstraintChange change = new ModifyConstraintChange(this);
				change.newname = newName;
				getSavepointManager().addChange(change);
			}
			_setName(newName);
			getDocument().notifyElementModified(this);
		}

		public void _setName(String name) throws AMLValidationException {
			attribute._renameConstraint(this, name);
			this.name = name;
		}
		
		@Override
		public void deepCopy(AMLDocumentElement rootElement,
				AMLDocumentElement rootCopyElement,
				AMLDocumentElement original,
				Map<AMLDocumentElement, AMLDocumentElement> mapping)
				throws AMLValidationException {
			// TODO Auto-generated method stub
			
		}		
	}
	
	private class UnknownConstraintImpl extends ConstraintImpl implements UnknownConstraint {
		private String requirement;	
		
		public UnknownConstraintImpl(String name, AMLAttributeImpl attribute) {
			super(name, attribute);
		}

		public void _setRequirement(String requirement) {
			this.requirement = requirement;			
		}

		@Override
		public void setRequirement(String requirement) {
			assertNotDeleted();

			if (getSession().getSavepointManager().hasCurrentSavepoint()) {
				ModifyConstraintChange change = new ModifyConstraintChange(this);
				change.newrequirement = requirement;
				getSavepointManager().addChange(change);
			}

			_setRequirement(requirement);
			getDocument().notifyElementModified(this);
		}

		@Override
		public String getRequirement() {
			assertNotDeleted();
			return requirement;
		}

		@Override
		public void deepCopy(AMLDocumentElement rootElement,
				AMLDocumentElement rootCopyElement,
				AMLDocumentElement original,
				Map<AMLDocumentElement, AMLDocumentElement> mapping)
				throws AMLValidationException {
			setRequirement(((UnknownConstraintImpl) original).getRequirement());
		}		
	}
	
	private class NominalScaledConstraintImpl extends ConstraintImpl implements NominalScaledConstraint {
		
		private List<String> requiredValues = new ArrayList<String>();
	
		public NominalScaledConstraintImpl(String name,
				AMLAttributeImpl attribute) {
			super(name, attribute);
		}

		public void _addRequiredValue(String value) {
			requiredValues.add(value);
		}

		public void _clearRequiredValues() {
			requiredValues.clear();
		}

		@Override
		public void addRequiredValue(String string) {
			assertNotDeleted();

			if (getSession().getSavepointManager().hasCurrentSavepoint()) {
				ModifyConstraintChange change = new ModifyConstraintChange(this);
				for (String value : requiredValues)
					change.newrequiredValues.add(value);
				change.newrequiredValues.add(string);
				getSavepointManager().addChange(change);
			}

			_addRequiredValue(string);
			getDocument().notifyElementModified(this);
		}

		@Override
		public Iterable<String> getRequiredValues() {
			return new ReadOnlyIterable<String>(requiredValues);
		}

		@Override
		public void deepCopy(AMLDocumentElement rootElement,
				AMLDocumentElement rootCopyElement,
				AMLDocumentElement original,
				Map<AMLDocumentElement, AMLDocumentElement> mapping)
				throws AMLValidationException {
			for (String value : ((NominalScaledConstraintImpl)original).getRequiredValues())
				addRequiredValue(value);			
		}

		@Override
		public void removeRequiredValue(String string) {
			assertNotDeleted();

			if (getSession().getSavepointManager().hasCurrentSavepoint()) {
				ModifyConstraintChange change = new ModifyConstraintChange(this);
				for (String value : requiredValues)
					change.newrequiredValues.add(value);
				change.newrequiredValues.remove(string);
				getSavepointManager().addChange(change);
			}

			_removeRequiredValue(string);
			getDocument().notifyElementModified(this);
		}

		private void _removeRequiredValue(String string) {
			requiredValues.remove(string);
		}

	}
	
	private class OrdinalScaledConstraintImpl extends ConstraintImpl implements OrdinalScaledConstraint {
		private String requiredMaxValue;
		private String requiredMinValue;
		private String requiredValue;
		
		public OrdinalScaledConstraintImpl(String name,
				AMLAttributeImpl attribute) {
			super(name, attribute);
		}

		public void _setRequiredValue(String requiredValue) {
			this.requiredValue = requiredValue; 
		}

		public void _setRequiredMinValue(String requiredMinValue) {
			this.requiredMinValue = requiredMinValue; 
		}

		public void _setRequiredMaxValue(String requiredMaxValue) {
			this.requiredMaxValue = requiredMaxValue; 			
		}

		@Override
		public void setRequiredValue(String string) {
			assertNotDeleted();

			if (getSession().getSavepointManager().hasCurrentSavepoint()) {
				ModifyConstraintChange change = new ModifyConstraintChange(this);
				change.newrequiredValue = string;
				getSavepointManager().addChange(change);
			}

			_setRequiredValue(string);
			getDocument().notifyElementModified(this);
		}

		@Override
		public void setRequiredMaxValue(String string) {
			assertNotDeleted();

			if (getSession().getSavepointManager().hasCurrentSavepoint()) {
				ModifyConstraintChange change = new ModifyConstraintChange(this);
				change.newrequiredMaxValue = string;
				getSavepointManager().addChange(change);
			}

			_setRequiredMaxValue(string);
			getDocument().notifyElementModified(this);
		}

		@Override
		public void setRequiredMinValue(String string) {
			assertNotDeleted();

			if (getSession().getSavepointManager().hasCurrentSavepoint()) {
				ModifyConstraintChange change = new ModifyConstraintChange(this);
				change.newrequiredMinValue = string;
				getSavepointManager().addChange(change);
			}

			_setRequiredMinValue(string);
			getDocument().notifyElementModified(this);
		}

		@Override
		public String getRequiredValue() {
			assertNotDeleted();
			return requiredValue;
		}

		@Override
		public String getRequiredMaxValue() {
			assertNotDeleted();
			return requiredMaxValue;
		}

		@Override
		public String getRequiredMinValue() {
			assertNotDeleted();
			return requiredMinValue;
		}

		@Override
		public void deepCopy(AMLDocumentElement rootElement,
				AMLDocumentElement rootCopyElement,
				AMLDocumentElement original,
				Map<AMLDocumentElement, AMLDocumentElement> mapping)
				throws AMLValidationException {
			setRequiredValue(((OrdinalScaledConstraintImpl) original).getRequiredValue());
			setRequiredMaxValue(((OrdinalScaledConstraintImpl) original).getRequiredMaxValue());
			setRequiredMinValue(((OrdinalScaledConstraintImpl) original).getRequiredMinValue());			
		}		
	}
	
	
	private AMLInternalAttributeContainer attributeContainer;
	private String name;
	private String dataType;
	private String unit;
	private String description;
	private String defaultValue;
	private String value;
	
	private Set<String> refSemantics = new LinkedHashSet<String>();
	private Map<String,  Constraint> constraints = new LinkedHashMap<String, Constraint>();
	
	private Map<String, AMLAttribute> attributes = new LinkedHashMap<String, AMLAttribute>();
	
	private Set<AMLAttributeImpl> cachedReferrers = new HashSet<AMLAttributeImpl>();
	private AMLAttributeImpl baseAttribute;

	AMLAttributeImpl(AMLInternalAttributeContainer attributeContainer) {
		this.attributeContainer = attributeContainer;
		name = "";
		dataType = "";
		unit = "";
		description = "";
		defaultValue = "";
		value = "";
	}
	
	void _renameConstraint(ConstraintImpl constraint, String newName) {
		Constraint ic = constraints.get(constraint.getName());
		if( ic == null){
			constraints.put(newName, ic);
			return;
		}
		
		// preserve order
		LinkedHashMap<String, Constraint> newConstraints = new LinkedHashMap<String, Constraint>();
		for (Constraint oldClass : constraints.values()) {
			if( oldClass == ic)
				newConstraints.put(newName, oldClass);
			else
				newConstraints.put(oldClass.getName(), oldClass);
		}
		constraints = newConstraints;
	}

	AMLValidationResultList validateRenameConstraint(String newName) {
		if (constraints.containsKey(newName))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Name already exists");
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	protected void addReferrer(AMLAttributeImpl referrer) throws AMLValidationException {
		getDocumentManager().addImplicitReference(referrer, this);
		cachedReferrers.add(referrer);
		referrer.baseAttribute = this;
	}

	protected void removeReferrer(AMLAttributeImpl referrer) throws AMLValidationException {
		getDocumentManager().removeImplicitReference(referrer, this);
		if (cachedReferrers == null)
			return;
		cachedReferrers.remove(referrer);
		referrer.baseAttribute = null;
	}
	

	public boolean isReferenced() {
		return cachedReferrers != null && !cachedReferrers.isEmpty();
	}

	public Iterable<AMLAttributeImpl> getReferrers() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLAttributeImpl>(cachedReferrers);
	}

	void _removeConstraint(ConstraintImpl constraintImpl) {
		constraints.remove(constraintImpl.getName());
	}

	void _removeRefSemantic(String refSemantic) {
		refSemantics.remove(refSemantic);
	}

	public void _setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		if (this.defaultValue == null)
			this.defaultValue = "";
	}

	public void _setDataType(String dataType) {
		this.dataType = dataType;
		if (this.dataType == null)
			this.dataType = "";
	}

	public void _setUnit(String unit) {
		this.unit = unit;
		if (this.unit == null)
			this.unit = "";
	}

	public void _setDescription(String description) {
		this.description = description;
		if (this.description == null)
			this.description = "";
	}

	public void _setValue(String value) {
		this.value = value;
		if (this.value == null)
			this.value = "";
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

	public AMLValidationResultList validateCreateAttribute(String newName) {
		AMLAttributeContainer container = this;
		List<String> attributePath = new ArrayList<String>();
		attributePath.add(newName);
		while (container instanceof AMLAttribute) {
			attributePath.add(0, ((AMLAttribute) container).getName());
			container = (AMLAttributeContainer) container.getParent();
		}
		
		if (container instanceof AMLRoleRequirements) {
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Attributes of RoleRequirements can't be changed");
		}
		
//		if (container instanceof AMLRoleClass) {
//			AMLRoleClassImpl roleClass = (AMLRoleClassImpl) container;
//			for (AMLDocumentElement documentElement : roleClass.getReferrers()) {
//				if (documentElement instanceof AMLSupportedRoleClass) {
//					AMLSupportedRoleClass supportedRoleClass = (AMLSupportedRoleClass) documentElement;
//					AMLAttributeContainer attributeContainer = (AMLAttributeContainer) supportedRoleClass.getParent();
//					for (String name : attributePath) {
//						AMLAttribute attribute = attributeContainer.getAttribute(name);
//						if (attribute == null)
//							return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Attribute not found in all SupportedRoleClasses elements");
//						attributeContainer = (AMLAttributeContainer) attribute;
//					}
//				}
//			}
//		}

		return AMLAttributeContainerHelper._validateCreateAttribute(this, newName);
	}

	public AMLValidationResultList validateNameChange(String newName) {
		AMLAttributeContainer container = attributeContainer;
		List<String> attributePath = new ArrayList<String>();

//		if (container instanceof AMLRoleClass) {
//			attributePath.add(newName);
//			while (container instanceof AMLAttribute) {
//				attributePath.add(0, ((AMLAttribute) container).getName());
//				container = (AMLAttributeContainer) container.getParent();
//			}
//			AMLRoleClassImpl roleClass = (AMLRoleClassImpl) container;
//			for (AMLDocumentElement documentElement : roleClass.getReferrers()) {
//				if (documentElement instanceof AMLSupportedRoleClass) {
//					AMLSupportedRoleClass supportedRoleClass = (AMLSupportedRoleClass) documentElement;
//					AMLMappingObject mappingObject = supportedRoleClass.getMappingObject();
//					AMLAttributeContainer attributeContainer = (AMLAttributeContainer) supportedRoleClass.getParent();
//					for (String name : attributePath) {
//						AMLAttribute attribute = mappingObject.getMappedAttribute(this);
//						if (attribute == null) {
//							attribute = attributeContainer.getAttribute(name);
//							if (attribute == null)
//								return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Attribute not found in all SupportedRoleClasses elements");
//						}
//						attributeContainer = (AMLAttributeContainer) attribute;
//					}
//				}
//			}
//		} else if (container instanceof AMLInternalSupportedRoleClassContainer) {
//			attributePath.add(getName());
//			while (container instanceof AMLAttribute) {
//				attributePath.add(0, ((AMLAttribute) container).getName());
//				container = (AMLAttributeContainer) container.getParent();
//			}
//			AMLInternalSupportedRoleClassContainer supportedRoleClassSontainer = (AMLInternalSupportedRoleClassContainer) container;
//			for (AMLSupportedRoleClass supportedRoleClass : supportedRoleClassSontainer.getSupportedRoleClasses()) {
//				AMLRoleClass roleClass = supportedRoleClass.getRoleClass();
//				AMLAttributeContainer attributeContainer = roleClass;
//				for (int i = 0; i < attributePath.size(); i++) {
//					AMLAttribute attribute = attributeContainer.getAttribute(attributePath.get(i));
//					if (attribute != null && attributePath.size() - 1 == i)
//						return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Renaming unallowed because of RoleClass");
//					attributeContainer = (AMLAttributeContainer) attribute;
//				}
//			}
//		} else 
		if (container instanceof AMLInterfaceClass) {
			attributePath.add(newName);
			while (container instanceof AMLAttribute) {
				attributePath.add(0, ((AMLAttribute) container).getName());
				container = (AMLAttributeContainer) container.getParent();
			}
			AMLInterfaceClassImpl interfaceClass = (AMLInterfaceClassImpl) container;
			for (AMLDocumentElement documentElement : interfaceClass.getReferrers()) {
				if (documentElement instanceof AMLExternalInterface) {
					AMLExternalInterface externalInterface = (AMLExternalInterface) documentElement;
					AMLAttributeContainer attributeContainer = (AMLAttributeContainer) externalInterface;
					for (String name : attributePath) {
						AMLAttribute attribute = attributeContainer.getAttribute(name);
						if (attribute == null)
							return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Attribute not found in all ExternalInterface elements");
						attributeContainer = (AMLAttributeContainer) attribute;
					}
				}
			}
		} else if (container instanceof AMLExternalInterface) {
			attributePath.add(getName());
			while (container instanceof AMLAttribute) {
				attributePath.add(0, ((AMLAttribute) container).getName());
				container = (AMLAttributeContainer) container.getParent();
			}
			AMLExternalInterface externalInterface = (AMLExternalInterface) container;
			AMLInterfaceClass interfaceClass = externalInterface.getInterfaceClass();
			AMLAttributeContainer attributeContainer = interfaceClass;
			for (int i = 0; i < attributePath.size(); i++) {
				AMLAttribute attribute = attributeContainer.getAttribute(attributePath.get(i));
				if (attribute != null && attributePath.size() - 1 == i)
					return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Renaming unallowed because of InterfaceClass");
				attributeContainer = (AMLAttributeContainer) attribute;
			}
		} else if (container instanceof AMLInternalElement) {
			Iterable<AMLFacet> facets = ((AMLInternalElement) container).getFacets();
			for (AMLFacet facet : facets) {
				if (facet.getAttribute(getName()) == this)
					return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Renaming unallowed because of Facet");
			}
		} else if (container instanceof AMLRoleRequirements) {
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Attributes of Rolerequirments can't change name");
		}

		return AMLAttributeContainerHelper.validateRenameAttribute(attributeContainer, this, newName);
	}

	public void _setName(String name) throws AMLNameAlreadyInUseException {
		AMLAttributeContainerHelper.renameAttribute(attributeContainer, this, name);		
		this.name = name;	
		for (AMLAttributeImpl referrer : cachedReferrers) {
			referrer._setName(name);
		}
	}

	public void setName(String newName) throws AMLValidationException {
		assertNotDeleted();

		AMLValidationResultList validationResult = validateNameChange(newName);
		if (validationResult.isAnyOperationNotPermitted())
			throw new AMLValidationException(validationResult);

		getDocument().notifyElementValidated(validationResult);

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyAttributeChange change = new ModifyAttributeChange(ModifyAttributeChange.CHANGE_NAME, this);
			change.setNewValue(newName);
			getSavepointManager().addChange(change);
		}

		_setName(newName);
		getDocument().notifyElementModified(this);
		
		for (AMLAttributeImpl referrer : cachedReferrers) {
			getDocument().notifyElementModified(referrer);
		}
		// if (cachedReferrers != null) {
		// Iterator<AMLAttributeImpl> referrers = cachedReferrers.iterator();
		// while (referrers.hasNext()) {
		// AMLAttributeImpl nextAttribute = referrers.next();
		// AMLInternalAttributeContainer parent = (AMLInternalAttributeContainer) nextAttribute.getParent();
		// parent.createAttribute(this);
		// }
		// }
	}

	public void setDefaultValue(String defaultValue) {
		assertNotDeleted();
		if (defaultValue != null && defaultValue.equals(this.defaultValue))
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyAttributeChange change = new ModifyAttributeChange(ModifyAttributeChange.CHANGE_DEFAULT_VALUE, this);
			change.setNewValue(defaultValue);
			getSavepointManager().addChange(change);
		}
		_setDefaultValue(defaultValue);
		getDocument().notifyElementModified(this);
	}

	public void setDataType(String dataType) {
		assertNotDeleted();
		if (dataType != null && dataType.equals(this.dataType))
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyAttributeChange change = new ModifyAttributeChange(ModifyAttributeChange.CHANGE_DATA_TYPE, this);
			change.setNewValue(dataType);
			getSavepointManager().addChange(change);
		}
		_setDataType(dataType);
		getDocument().notifyElementModified(this);
	}

	public void setUnit(String unit) {
		assertNotDeleted();
		if (unit != null && unit.equals(this.unit))
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyAttributeChange change = new ModifyAttributeChange(ModifyAttributeChange.CHANGE_UNIT, this);
			change.setNewValue(unit);
			getSavepointManager().addChange(change);
		}
		_setUnit(unit);
		getDocument().notifyElementModified(this);
	}

	public void setDescription(String description) {
		assertNotDeleted();
		if (description != null && description.equals(this.description))
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyAttributeChange change = new ModifyAttributeChange(ModifyAttributeChange.CHANGE_DESCRIPTION, this);
			change.setNewValue(description);
			getSavepointManager().addChange(change);
		}
		_setDescription(description);
		getDocument().notifyElementModified(this);
	}

	public void setValue(String value) {
		assertNotDeleted();
		if (value != null && value.equals(this.value))
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			ModifyAttributeChange change = new ModifyAttributeChange(ModifyAttributeChange.CHANGE_VALUE, this);
			change.setNewValue(value);
			getSavepointManager().addChange(change);
		}
		_setValue(value);
		getDocument().notifyElementModified(this);
	}

	@Override
	public Iterable<AMLAttribute> getAttributes() {
		return AMLAttributeContainerHelper.getAttributes(attributes);
	}

	@Override
	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public String getUnit() {
		return unit;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public AMLSessionImpl getSession() {
		return getDocument().getSession();
	}

	@Override
	public AMLDocumentImpl getDocument() {
		return (AMLDocumentImpl) attributeContainer.getDocument();
	}

	@Override
	public AMLDocumentElement getParent() {
		assertNotDeleted();
		return attributeContainer;
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
			if (getParent() instanceof AMLRoleRequirements) {
				DeleteRoleRequirementsAttributeChange change = new DeleteRoleRequirementsAttributeChange(this);
				getSavepointManager().addChange(change);
			} else {
				DeleteAttributeChange change = new DeleteAttributeChange(this);
				getSavepointManager().addChange(change);
			}
		}

		getDocument().notifyElementDeleting(this, getParent());

		_delete();
	}

	@Override
	public int getAttributesCount() {
		assertNotDeleted();
		return attributes.size();
	}

	@Override
	protected void _doDelete() throws AMLValidationException {
		AMLAttributeContainerHelper._removeAttribute(attributeContainer, this);
		if (baseAttribute != null)
			baseAttribute.removeReferrer(this);
	}

	@Override
	public AMLAttribute getAttribute(String name) {
		return AMLAttributeContainerHelper.getAttribute(this, name);
	}

	@Override
	public Map<String, AMLAttribute> _getAttributes() {
		return attributes;
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		AMLValidationResultList validationResult = AMLAttributeContainerHelper.validateDeleteAttribute(this);
		if (validationResult.isAnyOperationNotPermitted())
			return validationResult;

		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateAttributeDelete(this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateDelete() {
		AMLValidator validator = getSession().getValidator();
		return doValidateDelete(validator);
	}

	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
	
		if (!(newParentElement instanceof AMLAttributeContainer))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "new parent is not an attribute container");
		
		AMLInternalAttributeContainer newParent = (AMLInternalAttributeContainer)newParentElement;
		if (!oldParentElement.equals(newParentElement) && newParent.getAttribute(getName()) != null)
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "an attribute with name " + getName() + " already exists");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateAttributeReparent((AMLAttributeContainer)oldParentElement, (AMLAttributeContainer)newParentElement, this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}
		
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected void _reparent(AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		AMLInternalAttributeContainer oldParent = (AMLInternalAttributeContainer)oldParentElement;
		AMLInternalAttributeContainer newParent = (AMLInternalAttributeContainer)newParentElement;
		AMLAttributeContainerHelper._removeAttribute(oldParent, this);
		this.attributeContainer = newParent;
		AMLAttributeContainerHelper._addAttribute(newParent, this, (AMLAttribute)beforeElement, (AMLAttribute)afterElement);
	}

	@Override
	protected AMLDocumentElement _getBefore() {
		AMLInternalAttributeContainer attributeContainer = (AMLInternalAttributeContainer) getParent();
		return AMLAttributeContainerHelper._getAttributeBefore(attributeContainer, this);
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
			result.addDocumentElementValidationResult(new AMLValidationResultImpl(this, Severity.AML_ERROR, "attribute is not part of document scope"));
		
		for (AMLAttribute attribute : attributes.values()) {
			((AMLAttributeImpl)attribute).validateIfIsInDocumentScope(elementToReparent, document, result, checkedElements);
		}
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkFrom)
			throws AMLValidationException {
		return;
	}

	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateAttributeDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
		
		for (AMLAttribute attribute : getAttributes()) {
			((AbstractAMLDocumentElement)attribute).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		
		for (Constraint constraint : getConstraints()) {
			((AbstractAMLDocumentElement)constraint).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		
		mapping.put(original, this);
		
		setDataType(((AMLAttribute) original).getDataType());
		setUnit(((AMLAttribute) original).getUnit());
		setDescription(((AMLAttribute) original).getDescription());
		setDefaultValue(((AMLAttribute) original).getDefaultValue());
		setValue(((AMLAttribute) original).getValue());

		for (AMLAttribute attribute	: ((AMLAttribute) original).getAttributes()) {
			AMLAttribute copyAttribute = createAttribute(attribute.getName());
			((AbstractAMLDocumentElement)copyAttribute).deepCopy(rootElement, rootCopyElement, attribute, mapping);
		}	
		
		for (String refSemantic : ((AMLAttribute)original).getRefSemantics()) {
			addRefSemantic(refSemantic);
		}
		
		for (Constraint constraint : ((AMLAttribute)original).getConstraints()) {
			Constraint copyConstraint = null;
			if (constraint instanceof OrdinalScaledConstraint)
				copyConstraint = createOrdinalScaledConstraint(constraint.getName());
			else if (constraint instanceof NominalScaledConstraint)
				copyConstraint = createNominalScaledConstraint(constraint.getName());
			else if (constraint instanceof UnknownConstraint)
				copyConstraint = createUnknownConstraint(constraint.getName());
			((ConstraintImpl)copyConstraint).deepCopy(rootElement, rootCopyElement, constraint, mapping);
		}
	}

	@Override
	protected AMLDocumentElement _getAfter() {
		AMLInternalAttributeContainer attributeContainer = (AMLInternalAttributeContainer) getParent();
		return AMLAttributeContainerHelper._getAttributeAfter(attributeContainer, this);
	}

	@Override
	protected void _doDeepDelete(AMLDocumentElement baseElement)
			throws AMLValidationException {
		
		while (getAttributes().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getAttributes().iterator().next())._doDeepDelete(baseElement);
		}
		
		while (getRefSemantics().iterator().hasNext()) {
			removeRefSemantic(getRefSemantics().iterator().next());
		}
		
		while (getConstraints().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getConstraints().iterator().next())._doDeepDelete(baseElement);
		}
		
		delete();
	}

	@Override
	public void addRefSemantic(String refSemantic)
			throws AMLValidationException {
		assertNotDeleted();
		
		AMLValidationResultList result = validateAddRefSemantic(refSemantic);
		if (result.isAnyOperationNotPermitted())
			throw new AMLValidationException(result);
		
		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			AddRefSemanticChange change = new AddRefSemanticChange(this);
			change.setRefSemantic(refSemantic);
			getSavepointManager().addChange(change);
		}
		_addRefSemantic(refSemantic);
		getDocument().notifyElementModified(this);
	}

	private void _addRefSemantic(String refSemantic) {
		refSemantics.add(refSemantic);
	}

	@Override
	public Iterable<String> getRefSemantics() {
		return new ReadOnlyIterable<String>(refSemantics);
	}

	@Override
	public AMLValidationResultList validateAddRefSemantic(String refSemantic) {
		if (refSemantic != null && refSemantics.contains(refSemantic))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "RefSemantic already exists");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateAddRefSemantic(this, refSemantic);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateRemoveRefSemantic(String refSemantic) {
		if (refSemantic != null && !refSemantics.contains(refSemantic))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "RefSemantic doesn't exist");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateRemoveRefSemantic(this, refSemantic);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public void removeRefSemantic(String refSemantic)
			throws AMLValidationException {
		assertNotDeleted();
		if (refSemantic == null || !refSemantics.contains(refSemantic))
			return;

		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			RemoveRefSemanticChange change = new RemoveRefSemanticChange(this);
			change.setRefSemantic(refSemantic);
			getSavepointManager().addChange(change);
		}
		_removeRefSemantic(refSemantic);
		getDocument().notifyElementModified(this);
	}

	@Override
	public ReadOnlyIterable<Constraint> getConstraints() {
		return new ReadOnlyIterable<Constraint>(constraints.values());
	}

	@Override
	public Constraint getConstraint(String constraintName) {
		assertNotDeleted();
		Constraint constraint = constraints.get(constraintName);
		return constraint;
	}

	@Override
	public boolean hasRefSemantic(String refSemantic) {
		return refSemantics.contains(refSemantic);
	}

	@Override
	public boolean hasConstraint(Constraint constraint) {
		return constraints.containsKey(constraint.getName());
	}

	@Override
	public NominalScaledConstraint createNominalScaledConstraint(String name) throws AMLValidationException {
		assertNotDeleted();
		
		AMLValidationResultList resultList = validateCreateNominalScaledConstraint(name);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		getDocument().notifyElementValidated(resultList);
		
		NominalScaledConstraintImpl constraint = _createNominalScaledConstraint(name);
		
		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			CreateConstraintChange change = new CreateConstraintChange(constraint);
			getSavepointManager().addChange(change);
		}
		
		getDocument().notifyElementModified(this);
		return constraint;
	}

	NominalScaledConstraintImpl _createNominalScaledConstraint(
			String name) {
		NominalScaledConstraintImpl constraint =  new NominalScaledConstraintImpl(name, this);
		constraints.put(name, constraint);
		return constraint;

	}

	@Override
	public OrdinalScaledConstraint createOrdinalScaledConstraint(String name) throws AMLValidationException {
		assertNotDeleted();
		
		AMLValidationResultList resultList = validateCreateOrdinalScaledConstraint(name);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		getDocument().notifyElementValidated(resultList);
		
		OrdinalScaledConstraintImpl constraint = _createOrdinalScaledConstraint(name);
		
		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			CreateConstraintChange change = new CreateConstraintChange(constraint);
			getSavepointManager().addChange(change);
		}
		getDocument().notifyElementModified(this);
		return constraint;
	}

	OrdinalScaledConstraintImpl _createOrdinalScaledConstraint(
			String name) {
		OrdinalScaledConstraintImpl constraint =  new OrdinalScaledConstraintImpl(name, this);
		constraints.put(name, constraint);
		return constraint;
	}

	@Override
	public UnknownConstraint createUnknownConstraint(String name) throws AMLValidationException {
		assertNotDeleted();
		
		AMLValidationResultList resultList = validateCreateUnknownConstraint(name);
		if (resultList.isAnyOperationNotPermitted())
			throw new AMLValidationException(resultList);

		getDocument().notifyElementValidated(resultList);
		
		UnknownConstraintImpl constraint = _createUnknownConstraint(name);
		
		if (getSession().getSavepointManager().hasCurrentSavepoint()) {
			CreateConstraintChange change = new CreateConstraintChange(constraint);
			getSavepointManager().addChange(change);
		}
	
		getDocument().notifyElementModified(this);
		
		return constraint;
	}

	UnknownConstraintImpl _createUnknownConstraint(String name) {
		UnknownConstraintImpl constraint =  new UnknownConstraintImpl(name, this);
		constraints.put(name, constraint);
		return constraint;
	}

	@Override
	public AMLValidationResultList validateCreateNominalScaledConstraint(
			String constraint) {
		if (constraint != null && constraints.containsKey(constraint))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Constraint already exists");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateCreateNominalScalesConstraint(this, constraint);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateCreateOrdinalScaledConstraint(
			String constraint) {
		if (constraint != null && constraints.containsKey(constraint))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Constraint already exists");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateCreateOrdinalScalesConstraint(this, constraint);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	public AMLValidationResultList validateCreateUnknownConstraint(
			String constraint) {
		if (constraint != null && constraints.containsKey(constraint))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Constraint already exists");

		AMLValidator validator = getSession().getValidator();
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateCreateUnknownConstraint(this, constraint);
			validationResult.assertNotInternalValidationSeverity();
			return validationResult;
		}

		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	public AMLAttribute getBaseAttribute() {
		return baseAttribute;
	}

	@Override
	public int getRefSemanticCount() {
		assertNotDeleted();
		return refSemantics.size();
	}

	
}
