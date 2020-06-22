/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;
import java.util.Map;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttributeContainer;
import org.automationml.aml.AMLCOLLADAInterface;
import org.automationml.aml.AMLCOLLADAInterfaceContainer;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLExternalInterfaceContainer;
import org.automationml.aml.AMLFrameAttribute;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInterfaceClassContainer;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalElementContainer;
import org.automationml.aml.AMLInternalLink;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLSupportedRoleClassContainer;
import org.automationml.aml.AMLSystemUnitClass;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.Change;

public class AMLInterfaceClassImpl extends AbstractAMLClassImpl<AMLInterfaceClassImpl> implements AMLInterfaceClass {

//	@Override
//	public AMLAttribute createAttribute(String name) throws AMLValidationException {
//		AMLAttribute attribute = super.createAttribute(name);
//
//		// add attribute to all external interfaces that implement this class
//		Iterator<AMLDocumentElement> referrers = getReferrers().iterator();
//		while (referrers.hasNext()) {
//			AMLDocumentElement documentElement = referrers.next();
//			if (documentElement instanceof AMLExternalInterface) {
//				AMLExternalInterfaceImpl externalInterface = (AMLExternalInterfaceImpl) documentElement;
//				if (externalInterface.getAttribute(name) == null)
//					externalInterface.createAttribute((AMLAttributeImpl)attribute);
//			}
//		}
//		return attribute;
//	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement, AMLDocumentElement rootCopyElement,
			AMLDocumentElement original, Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {

		super.deepCopy(rootElement, rootCopyElement, original, mapping);
		
		for (AMLAttribute attribute	: ((AMLAttributeContainer) original).getAttributes()) {
			AMLAttribute copyAttribute = createAttribute(attribute.getName());
			((AbstractAMLDocumentElement)copyAttribute).deepCopy(rootElement, rootCopyElement, attribute, mapping);
		}
	}

	AMLInterfaceClassImpl(AMLInterfaceClassContainer interfaceClassContainer) {
		super((AbstractAMLClassContainer<AMLInterfaceClassImpl>) interfaceClassContainer);
	}

	@Override
	public void setBaseInterfaceClass(AMLInterfaceClass baseclass) throws AMLValidationException {
		super.setBaseClass((AMLInterfaceClassImpl) baseclass);
	}

	@Override
	public void unsetBaseInterfaceClass() throws AMLValidationException {
		super.unsetBaseClass();
	}

	@Override
	public AMLInterfaceClass getBaseInterfaceClass() {
		return super.getBaseClass();
	}
	
	@Override
	public AMLValidationResultList validateSetBaseInterfaceClass(
			AMLInterfaceClass baseClass) {
		return super.validateSetBaseClass((AMLInterfaceClassImpl) baseClass);
	}

	protected AMLInterfaceClassImpl _newClass() throws AMLValidationException {
		return new AMLInterfaceClassImpl(this);
	}

	@Override
	public AMLInterfaceClass createInterfaceClass(String name) throws AMLValidationException {
		return super.createClass(name);
	}

	@Override
	public AMLInterfaceClass getInterfaceClass(String name) {
		return super.getClass(name);
	}

	@Override
	public Iterable<AMLInterfaceClass> getInterfaceClasses() {
		return (Iterable<AMLInterfaceClass>) super.getClasses();
	}

	@Override
	public int getInterfaceClassesCount() {
		return super.getClassesCount();
	}

	@Override
	public AMLValidationResultList validateCreateInterfaceClass(String name) {
		return validateNameChange(name);
	}

	protected Change createDeleteChange() {
		Change change = new DeleteClassChange<AMLInterfaceClassImpl>(this);
		return change;
	}

	@Override
	public AMLValidationResultList validateCreateAttribute(String newName) {
//		for (AMLDocumentElement documentElement : getReferrers()) {
//			if (documentElement instanceof AMLExternalInterface) {
//				AMLExternalInterface externalInterface = (AMLExternalInterface) documentElement;
//				AMLAttributeContainer container = (AMLAttributeContainer) externalInterface;
//				if (container.getAttribute(newName) == null)
//					return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "Attribute not found in all ExternalInterface elements");				
//			}
//		}
		return AMLAttributeContainerHelper._validateCreateAttribute(this, newName);
	}

	@Override
	protected AMLValidationResultList _validateNameChange(AMLValidator validator, String newName) {
		return validator.validateInterfaceClassSetName(this, newName);
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {
		if (validator != null)
			return validator.validateInterfaceClassDelete(this);
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected AMLValidationResultList _validateCreateClass(AMLValidator validator, String name) {
		return validator.validateInterfaceClassCreate(this, name);
	}
	
	

	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		
		if (!(newParentElement instanceof AMLInterfaceClassContainer))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "new parent is not a container for InterfaceClasses");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateInterfaceClassReparent((AMLInterfaceClassContainer)oldParentElement, (AMLInterfaceClassContainer)newParentElement, this);
			validationResult2.assertNotInternalValidationSeverity();
			return validationResult2;
		}
		
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected void _doUnlink(AMLDocumentElement unlinkFrom)
			throws AMLValidationException {
		boolean reinitialize = true;
		while (reinitialize) {
			reinitialize = false;
			for (AMLDocumentElement documentElement : getReferrers()) {
				if (!((AbstractAMLDocumentElement)documentElement).isDescendantOf(unlinkFrom)) {
					if (documentElement instanceof AMLExternalInterface) {
						((AbstractAMLDocumentElement) documentElement)._doUnlink(unlinkFrom);
						reinitialize = true;
					} else if (documentElement instanceof AMLInterfaceClass) {
						((AMLInterfaceClass) documentElement).unsetBaseInterfaceClass();
						reinitialize = true;
					}
				}
				if (reinitialize)
					break;
			}			
		}
	
		super._doUnlink(unlinkFrom);
	}
	
	@Override
	protected void doValidateDeepDelete(AMLValidator validator,
			AMLDocumentElement baseElement,
			AMLValidationResultListImpl validationResultList) {
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateInterfaceClassDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}
		super.doValidateDeepDelete(validator, baseElement, validationResultList);
	}
	
	@Override
	public AMLInterfaceClass createInterfaceClass(String name,
			AMLInterfaceClass interfaceClass) throws AMLValidationException {
		AMLInterfaceClassImpl interfaceClassImpl = super.createClass(name);
		
		if (interfaceClass != null)
			interfaceClassImpl.deepCopy(interfaceClass, interfaceClassImpl, interfaceClass, new HashMap<AMLDocumentElement, AMLDocumentElement>());
		
		return interfaceClassImpl;
	}
	
}
