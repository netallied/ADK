/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttributeContainer;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLExternalInterfaceContainer;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLRoleClassContainer;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.Change;

public class AMLRoleClassImpl extends AbstractAMLClassImpl<AMLRoleClassImpl> implements AMLRoleClass, AMLInternalExternalInterfaceContainer {

	@Override
	public void deepCopy(AMLDocumentElement rootElement, AMLDocumentElement rootCopyElement,
			AMLDocumentElement original, Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		super.deepCopy(rootElement, rootCopyElement, original, mapping);
		
		for (AMLExternalInterface externalInterface : ((AMLExternalInterfaceContainer) original).getExternalInterfaces()) {
			//AMLExternalInterface copyExternalInterface = createExternalInterface(externalInterface.getInterfaceClass());
			AMLInterfaceClass interfaceClass = null;
			if (getSession().equals(rootElement.getSession())) {
				interfaceClass = externalInterface.getInterfaceClass();
			} else {
				String path = externalInterface.getInterfaceClass().getClassPath();
				interfaceClass = ((AMLDocumentImpl)getDocument()).getInterfaceClassByPath(path);
			}		
			if (interfaceClass == null)
				throw new AMLValidationException(new AMLValidationResultListImpl(this, Severity.AML_ERROR, "referenced interface class not known in scope!"));
			AMLExternalInterface copyExternalInterface = AMLExternalInterfaceContainerHelper.createExternalInterface(this, interfaceClass, false);
			((AbstractAMLDocumentElement) copyExternalInterface).deepCopy(rootElement, rootCopyElement, externalInterface, mapping);
		}
		
		for (AMLAttribute attribute	: ((AMLAttributeContainer) original).getAttributes()) {
			AMLAttribute copyAttribute = createAttribute(attribute.getName());
			((AbstractAMLDocumentElement)copyAttribute).deepCopy(rootElement, rootCopyElement, attribute, mapping);
		}
	}

	private Map<UUID, AMLExternalInterface> externalInterfaces = new LinkedHashMap<UUID, AMLExternalInterface>();

	AMLRoleClassImpl(AMLRoleClassContainer classContainer) {
		super((AbstractAMLClassContainer<AMLRoleClassImpl>) classContainer);
	}

	@Override
	public void setBaseRoleClass(AMLRoleClass baseclass) throws AMLValidationException {
		super.setBaseClass((AMLRoleClassImpl) baseclass);
	}

	@Override
	public void unsetBaseRoleClass() throws AMLValidationException {
		super.unsetBaseClass();
	}

	@Override
	public AMLRoleClass getBaseRoleClass() {
		return super.getBaseClass();
	}
	
	@Override
	public AMLValidationResultList validateSetBaseRoleClass(
			AMLRoleClass baseClass) {
		return super.validateSetBaseClass((AMLRoleClassImpl) baseClass);
	}

	protected AMLRoleClassImpl _newClass() throws AMLValidationException {
		return new AMLRoleClassImpl(this);
	}

	@Override
	public AMLRoleClass createRoleClass(String name) throws AMLValidationException {
		return createRoleClass(name, null);
	}

	protected Change createDeleteChange() {
		Change change = new DeleteClassChange<AMLRoleClassImpl>(this);
		return change;
	}

	@Override
	public AMLValidationResultList validateCreateRoleClass(String name) {
		return validateNameChange(name);
	}

	@Override
	public AMLRoleClass getRoleClass(String name) {
		return super.getClass(name);
	}

	@Override
	public Iterable<AMLRoleClass> getRoleClasses() {
		return (Iterable<AMLRoleClass>) super.getClasses();
	}
	
	@Override
	public int getRoleClassesCount() {
		return super.getClassesCount();
	}

	@Override
	public AMLValidationResultList validateCreateAttribute(String newName) {
//		for (AMLDocumentElement documentElement : getReferrers()) {
//			if (documentElement instanceof AMLSupportedRoleClass) {
//				AMLSupportedRoleClass supportedRoleClass = (AMLSupportedRoleClass) documentElement;
//				AMLAttributeContainer container = (AMLAttributeContainer) supportedRoleClass.getParent();
//				if (container.getAttribute(newName) == null)
//					return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Attribute not found in all SupportedRoleClasses elements");				
//			}
//		}
		return AMLAttributeContainerHelper._validateCreateAttribute(this, newName);
	}

	@Override
	protected AMLValidationResultList _validateNameChange(AMLValidator validator, String newName) {
		return validator.validateRoleClassSetName(this, newName);
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		if (validator != null)
			return validator.validateRoleClassDelete(this);
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected AMLValidationResultList _validateCreateClass(AMLValidator validator, String name) {
		return validator.validateRoleClassCreate(this, name);
	}

	@Override
	public AMLValidationResultList validateCreateExternalInterface(AMLInterfaceClass interfaceClass) {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.validateCreateExternalInterface(this, interfaceClass);
	}

	@Override
	public Iterable<AMLExternalInterface> getExternalInterfaces() {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.getExternalInterfaces(externalInterfaces);
	}
	
	@Override
	public int getExternalInterfacesCount() {
		assertNotDeleted();
		return externalInterfaces.size();
	}

	@Override
	public AMLExternalInterface getExternalInterface(UUID id) {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.getExternalInterface(this, id);
	}

	@Override
	public AMLExternalInterface createExternalInterface(AMLInterfaceClass interfaceClass) throws AMLValidationException {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.createExternalInterface(this, interfaceClass, true);
	}

	@Override
	public AMLExternalInterface createExternalInterface(UUID id, AMLInterfaceClass interfaceClass) throws AMLValidationException {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.createExternalInterface(this, interfaceClass, id, true);
	}

	@Override
	public Map<UUID, AMLExternalInterface> _getExternalInterfaces() {
		return externalInterfaces;
	}

	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		
		if (!(newParentElement instanceof AMLRoleClassContainer))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "new parent is not a container for InterfaceClasses");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateRoleClassReparent((AMLRoleClassContainer)oldParentElement, (AMLRoleClassContainer)newParentElement, this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}
		
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}
	
	@Override
	protected void _doUnlink(AMLDocumentElement unlinkForm)
			throws AMLValidationException {
		boolean reinitialize = true;
		while (reinitialize) {
			reinitialize = false;
			for (AMLDocumentElement documentElement : getReferrers()) {
				if (!((AbstractAMLDocumentElement)documentElement).isDescendantOf(unlinkForm)) {
					if (documentElement instanceof AMLSupportedRoleClass) {
						documentElement.delete();
						reinitialize = true;
					} else if (documentElement instanceof AMLRoleClass) {
						((AMLRoleClass) documentElement).unsetBaseRoleClass();
						reinitialize = true;
					}
				}	
				if (reinitialize)
					break;
			}			
		}
		
		super._doUnlink(unlinkForm);
	}
	
	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateRoleClassDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
		super.doValidateDeepDelete(validator, baseElement, validationResultList);
	}
	
	@Override
	public AMLRoleClass createRoleClass(String name, AMLRoleClass roleClass)
			throws AMLValidationException {
		AMLRoleClassImpl roleClassImpl = (AMLRoleClassImpl) super.createClass(name);
		
		if (roleClass != null)
			roleClassImpl.deepCopy(roleClass, roleClassImpl, roleClass, new HashMap<AMLDocumentElement, AMLDocumentElement>());
		
		return roleClassImpl;
	}
}
