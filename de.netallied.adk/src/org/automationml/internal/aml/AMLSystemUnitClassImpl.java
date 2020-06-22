/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.automationml.aml.AMLAttribute;
import org.automationml.aml.AMLAttributeContainer;
import org.automationml.aml.AMLCOLLADAInterface;
import org.automationml.aml.AMLCOLLADAInterface.RefType;
import org.automationml.aml.AMLCOLLADAInterfaceContainer;
import org.automationml.aml.AMLDocumentElement;
import org.automationml.aml.AMLExternalInterface;
import org.automationml.aml.AMLExternalInterfaceContainer;
import org.automationml.aml.AMLFrameAttribute;
import org.automationml.aml.AMLGroup;
import org.automationml.aml.AMLInterfaceClass;
import org.automationml.aml.AMLInternalElement;
import org.automationml.aml.AMLInternalElementContainer;
import org.automationml.aml.AMLInternalLink;
import org.automationml.aml.AMLMappingObject;
import org.automationml.aml.AMLMirrorObject;
import org.automationml.aml.AMLRoleClass;
import org.automationml.aml.AMLSupportedRoleClass;
import org.automationml.aml.AMLSupportedRoleClassContainer;
import org.automationml.aml.AMLSystemUnitClass;
import org.automationml.aml.AMLSystemUnitClassContainer;
import org.automationml.aml.AMLValidationException;
import org.automationml.aml.AMLValidationResult.Severity;
import org.automationml.aml.AMLValidationResultList;
import org.automationml.aml.AMLValidator;
import org.automationml.internal.Change;
import org.automationml.internal.ReadOnlyIterable;

public class AMLSystemUnitClassImpl extends AbstractAMLClassImpl<AMLSystemUnitClassImpl> implements AMLSystemUnitClass, AMLInternalInternalElementContainer,
		AMLInternalSupportedRoleClassContainer, AMLInternalExternalInterfaceContainer, AMLInternalGroupContainer, 
		AMLInternalCOLLADAInterfaceContainer, AMLInternalFrameAttributeContainer, AMLInternalInternalLinkContainer {

	private AMLFrameAttribute frameAttribute;
	private LinkedHashMap<UUID, AMLInternalElement> internalElements = new LinkedHashMap<UUID, AMLInternalElement>();
	private LinkedHashMap<UUID, AMLGroup> groups = new LinkedHashMap<UUID, AMLGroup>();
	private Map<AMLRoleClass, AMLSupportedRoleClass> supportedRoleClasses = new LinkedHashMap<AMLRoleClass, AMLSupportedRoleClass>();
	private Map<UUID, AMLExternalInterface> externalInterfaces = new LinkedHashMap<UUID, AMLExternalInterface>();
	private AMLCOLLADAInterface colladaInterface;
	private Set<AMLInternalLink> internalLinks = new HashSet<AMLInternalLink>();

	AMLSystemUnitClassImpl(AMLSystemUnitClassContainer classContainer) {
		super((AbstractAMLClassContainer<AMLSystemUnitClassImpl>) classContainer);
	}

	@Override
	public void setBaseSystemUnitClass(AMLSystemUnitClass baseclass) throws AMLValidationException {
		super.setBaseClass((AMLSystemUnitClassImpl) baseclass);
	}

	@Override
	public void unsetBaseSystemUnitClass() throws AMLValidationException {
		super.unsetBaseClass();
	}

	@Override
	public AMLSystemUnitClass getBaseSystemUnitClass() {
		return super.getBaseClass();
	}
	
	@Override
	public AMLValidationResultList validateSetBaseSystemUnitClass(
			AMLSystemUnitClass baseclass) {
		return super.validateSetBaseClass((AMLSystemUnitClassImpl) baseclass);
	}

	protected AMLSystemUnitClassImpl _newClass() throws AMLValidationException {
		return new AMLSystemUnitClassImpl(this);
	}

	@Override
	public AMLSystemUnitClass createSystemUnitClass(String name) throws AMLValidationException {
		return createSystemUnitClass(name, null);
	}

	protected Change createDeleteChange() {
		Change change = new DeleteClassChange<AMLSystemUnitClassImpl>(this);
		return change;
	}

	@Override
	public AMLValidationResultList validateCreateSystemUnitClass(String name) {
		return validateNameChange(name);
	}

	@Override
	public AMLSystemUnitClass getSystemUnitClass(String name) {
		return super.getClass(name);
	}

	@Override
	public Iterable<AMLSystemUnitClass> getSystemUnitClasses() {
		return (Iterable<AMLSystemUnitClass>) super.getClasses();
	}
	
	@Override
	public int getSystemUnitClassesCount() {
		return super.getClassesCount();
	}

	@Override
	public AMLValidationResultList validateCreateAttribute(String newName) {
		return AMLAttributeContainerHelper._validateCreateAttribute(this, newName);
	}

	@Override
	protected AMLValidationResultList _validateNameChange(AMLValidator validator, String newName) {
		return validator.validateSystemUnitClassSetName(this, newName);
	}

	@Override
	protected AMLValidationResultList doValidateDelete(AMLValidator validator) {

		if (getInternalElements().iterator().hasNext())
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "SystemUnitClass has InternalElements");

		if (validator != null)
			return validator.validateSystemUnitClassDelete(this);
		return new AMLValidationResultListImpl(this, Severity.OK, "");
	}

	@Override
	protected AMLValidationResultList _validateCreateClass(AMLValidator validator, String name) {
		return validator.validateSystemUnitClassCreate(this, name);
	}

	@Override
	public AMLInternalElement createInternalElement() throws AMLValidationException {
		return createInternalElement((AMLInternalElement)null);
	}

	@Override
	public AMLInternalElement createInternalElement(UUID id) throws AMLValidationException {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.createInternalElement(this, id);
	}

	@Override
	public Iterable<AMLInternalElement> getInternalElements() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLInternalElement>(internalElements.values());
	}

	@Override
	public int getInternalElementsCount() {
		assertNotDeleted();
		return internalElements.size();
	}

	@Override
	public AMLValidationResultList validateInternalElementCreate() {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.validateCreateInternalElement(this);
	}

	@Override
	public AMLValidationResultList validateInternalElementCreate(UUID id) throws AMLValidationException {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.validateCreateInternalElement(this, id);
	}

	@Override
	public LinkedHashMap<UUID, AMLInternalElement> _getInternalElements() {
		return internalElements;
	}

	public void addReferrer(AMLInternalElement internalElement) throws AMLValidationException {
		super.addReferrer(internalElement);
	}

	public void removeReferrer(AMLInternalElement internalElement) throws AMLValidationException {
		super.removeReferrer(internalElement);
	}

//	@Override
//	public AMLSupportedRoleClass createSupportedRoleClass(AMLRoleClass roleClass, AMLMappingObject mappings) throws AMLValidationException {
//		assertNotDeleted();
//		return AMLSupportedRoleClassContainerHelper.createSupportedRoleClass(this, roleClass, mappings);
//	}

	@Override
	public AMLSupportedRoleClass getSupportedRoleClass(AMLRoleClass roleClass) {
		assertNotDeleted();
		return supportedRoleClasses.get(roleClass);
	}

	@Override
	public Map<AMLRoleClass, AMLSupportedRoleClass> _getSupportedRoleClasses() {
		return supportedRoleClasses;
	}

	@Override
	public AMLValidationResultList validateCreateSupportedRoleClass(AMLRoleClass roleClass)  {
		assertNotDeleted();
		return AMLSupportedRoleClassContainerHelper.validateCreateSupportedRoleClass(this, roleClass);
	}

	@Override
	public AMLMappingObject createMappingObject(AMLRoleClass roleClass) {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.createMappingObject(this, roleClass);
	}

	@Override
	public Iterable<AMLSupportedRoleClass> getSupportedRoleClasses() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLSupportedRoleClass>(supportedRoleClasses.values());
	}

	@Override
	public int getSupportedRoleClassesCount() {
		assertNotDeleted();
		return supportedRoleClasses.size();
	}

	@Override
	public AMLSupportedRoleClass createSupportedRoleClass(AMLRoleClass roleClass) throws AMLValidationException {
		assertNotDeleted();
		return AMLSupportedRoleClassContainerHelper.createSupportedRoleClass(this, roleClass);
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
	public AMLGroup createGroup() throws AMLValidationException {
		assertNotDeleted();
		return AMLGroupContainerHelper.createGroup(this);
	}

	@Override
	public Iterable<AMLGroup> getGroups() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLGroup>(groups.values());
	}

	@Override
	public int getGroupsCount() {
		assertNotDeleted();
		return groups.size();
	}
	
	@Override
	public LinkedHashMap<UUID, AMLGroup> _getGroups() {
		return groups;
	}

	@Override
	public AMLGroup createGroup(UUID id) throws AMLValidationException {
		assertNotDeleted();
		return AMLGroupContainerHelper.createGroup(this, id);
	}

	@Override
	public AMLFrameAttribute getFrameAttribute() throws AMLValidationException {
		assertNotDeleted();
		if (frameAttribute == null)
			frameAttribute = AMLAttributeContainerHelper.createFrameAttribute(this);
		return frameAttribute;
	}

	@Override
	public AMLValidationResultList validateCreateFrameAttribute() {
		return AMLAttributeContainerHelper._validateCreateFrameAttribute(this);
	}

	@Override
	public void _setFrameAttribute(AMLFrameAttributeImpl frameAttribute) {
		this.frameAttribute = frameAttribute;
	}

	@Override
	public boolean hasFrameAttribute() {
		return frameAttribute != null;
	}
		
	@Override
	protected AMLValidationResultList doValidateReparent(
			AMLValidator validator, AMLDocumentElement oldParentElement,
			AMLDocumentElement newParentElement,
			AMLDocumentElement beforeElement, AMLDocumentElement afterElement) {
		
		if (!(newParentElement instanceof AMLSystemUnitClassContainer))
			return new AMLValidationResultListImpl(this, Severity.AML_ERROR, "new parent is not a container for InterfaceClasses");
		
		if (validator != null) {
			AMLValidationResultListImpl validationResult2 = (AMLValidationResultListImpl) validator.validateSystemUnitClassReparent((AMLSystemUnitClassContainer)oldParentElement, (AMLSystemUnitClassContainer)newParentElement, this);
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
					if (documentElement instanceof AMLInternalElement) {
						((AMLInternalElement) documentElement).unsetBaseSystemUnitClass();
						reinitialize = true;
					} else if (documentElement instanceof AMLSystemUnitClass) {
						((AMLSystemUnitClass) documentElement).unsetBaseSystemUnitClass();
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
			AMLValidationResultListImpl validationResult = (AMLValidationResultListImpl) validator.validateSystemUnitClassDelete(this);
			validationResult.assertNotInternalValidationSeverity();
			validationResultList.addDocumentElementValidationResultList(validationResult);
		}

		for (AMLInternalElement internalElement : getInternalElements()) {
			((AbstractAMLDocumentElement)internalElement).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		for (AMLGroup group : getGroups()) {
			((AbstractAMLDocumentElement)group).doValidateDeepDelete(validator, baseElement, validationResultList);
		}		
		for (AMLExternalInterface externalInterface : getExternalInterfaces()) {
			((AbstractAMLDocumentElement)externalInterface).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		for (AMLSupportedRoleClass supportedRoleClass : getSupportedRoleClasses()) {
			((AbstractAMLDocumentElement)supportedRoleClass).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
		for (AMLInternalLink internalLink : getInternalLinks()) {
			((AbstractAMLDocumentElement)internalLink).doValidateDeepDelete(validator, baseElement, validationResultList);
		}
	
		super.doValidateDeepDelete(validator, baseElement, validationResultList);
	}

	@Override
	public AMLInternalElement createInternalElement(
			AMLInternalElement internalElement) throws AMLValidationException {
		assertNotDeleted();
		AMLInternalElementImpl internalElementImpl = AMLInternalElementContainerHelper.createInternalElement(this);
		
		if (internalElement != null)
			internalElementImpl.deepCopy(internalElement, internalElementImpl, internalElement, new HashMap<AMLDocumentElement, AMLDocumentElement>());
		
		return internalElementImpl;
	}
	
	@Override
	public AMLSystemUnitClass createSystemUnitClass(String name,
			AMLSystemUnitClass systemUnitClass) throws AMLValidationException {
		AMLSystemUnitClassImpl sysetmUnitClassImpl = (AMLSystemUnitClassImpl) super.createClass(name);
		
		if (systemUnitClass != null)
			sysetmUnitClassImpl.deepCopy(systemUnitClass, sysetmUnitClassImpl, systemUnitClass, new HashMap<AMLDocumentElement, AMLDocumentElement>());
		
		return sysetmUnitClassImpl;
	}

	@Override
	public void _removeFrameAttribute() {
		frameAttribute = null;		
	}

	@Override
	public void _addFrameAttribute(AMLFrameAttributeImpl amlFrameAttributeImpl) {
		this.frameAttribute = amlFrameAttributeImpl;
	}

	@Override
	public void deepCopy(AMLDocumentElement rootElement,
			AMLDocumentElement rootCopyElement, AMLDocumentElement original,
			Map<AMLDocumentElement, AMLDocumentElement> mapping)
			throws AMLValidationException {
		super.deepCopy(rootElement, rootCopyElement, original, mapping);
		
		AMLFrameAttribute frameAttribute = ((AMLSystemUnitClass)original).getFrameAttribute();
		((AMLFrameAttributeImpl)getFrameAttribute()).deepCopy(rootElement, rootCopyElement, frameAttribute, mapping);
		
		for (AMLInternalElement internalElement : ((AMLInternalElementContainer) original).getInternalElements()) {
			if (internalElement.equals(rootCopyElement))
				continue;
			AMLInternalElement copyInternalElement = createInternalElement();
			((AbstractAMLDocumentElement) copyInternalElement).deepCopy(rootElement, rootCopyElement, internalElement, mapping);
		}
		
				
		for (AMLExternalInterface externalInterface : ((AMLExternalInterfaceContainer) original).getExternalInterfaces()) {
			//AMLExternalInterface copyExternalInterface = createExternalInterface(externalInterface.getInterfaceClass());
			AMLExternalInterface copyExternalInterface = AMLExternalInterfaceContainerHelper.createExternalInterface(this, externalInterface.getInterfaceClass(), false);
			((AbstractAMLDocumentElement) copyExternalInterface).deepCopy(rootElement, rootCopyElement, externalInterface, mapping);
		}
		
		
		for (AMLInternalLink internalLink : ((AMLInternalInternalLinkContainer) original).getInternalLinks()) {
			AMLExternalInterface a = internalLink.getRefPartnerSideA();
			AMLExternalInterface b = internalLink.getRefPartnerSideB();
			if (mapping.containsKey(a))
				a = (AMLExternalInterface) mapping.get(a);
			else if (!((AbstractAMLDocumentElement) a).isDescendantOf(original.getDocument()))
				a = null;
			if (mapping.containsKey(b))
				b = (AMLExternalInterface) mapping.get(b);
			else if (!((AbstractAMLDocumentElement) b).isDescendantOf(original.getDocument()))
				b = null;
			if (a != null && b != null) {
				AMLInternalLink copyInternalLink = createInternalLink(internalLink.getName(), a, b);
				((AbstractAMLDocumentElement) copyInternalLink).deepCopy(rootElement, rootCopyElement, internalLink, mapping);
			}
		}
		
		for (AMLAttribute attribute	: ((AMLAttributeContainer) original).getAttributes()) {
			AMLAttribute copyAttribute = createAttribute(attribute.getName());
			((AbstractAMLDocumentElement)copyAttribute).deepCopy(rootElement, rootCopyElement, attribute, mapping);
		}
		
		for (AMLSupportedRoleClass supportedRoleClass : ((AMLSupportedRoleClassContainer) original).getSupportedRoleClasses()) {
			AMLSupportedRoleClass copySupportedRoleClass = createSupportedRoleClass(supportedRoleClass.getRoleClass());
			((AbstractAMLDocumentElement) copySupportedRoleClass).deepCopy(rootElement, rootCopyElement, supportedRoleClass, mapping);
		}
		
		AMLCOLLADAInterface colladaInterface = ((AMLCOLLADAInterfaceContainer)original).getCOLLADAInterface();
		if (colladaInterface != null) {
			createCOLLADAInterface(colladaInterface.getRefType());
			((AMLCOLLADAInterfaceImpl)getCOLLADAInterface()).deepCopy(rootElement, rootCopyElement, colladaInterface, mapping);
		}
	}

	@Override
	protected void _doDeepDelete(AMLDocumentElement baseElement)
			throws AMLValidationException {
		
		
		while (getInternalElements().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getInternalElements().iterator().next())._doDeepDelete(baseElement);
		}
		
		while (getGroups().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getGroups().iterator())._doDeepDelete(baseElement);
		}
		
		while (getExternalInterfaces().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getExternalInterfaces().iterator().next())._doDeepDelete(baseElement);
		}
		
		while (getSupportedRoleClasses().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getSupportedRoleClasses().iterator().next())._doDeepDelete(baseElement);
		}
		
		while (getInternalLinks().iterator().hasNext()) {
			((AbstractAMLDocumentElement)getInternalLinks().iterator().next())._doDeepDelete(baseElement);
		}
		
		if (frameAttribute != null)
			((AbstractAMLDocumentElement)getFrameAttribute())._doDeepDelete(baseElement);	
		
		if (colladaInterface != null)
			((AbstractAMLDocumentElement)getCOLLADAInterface())._doDeepDelete(baseElement);
		
		super._doDeepDelete(baseElement);
	}

	@Override
	public AMLGroup createGroup(AMLGroup group) throws AMLValidationException {
		assertNotDeleted();
		AMLGroupImpl copy = (AMLGroupImpl) AMLGroupContainerHelper.createGroup(this);
		
		if (group != null) {
			Map<AMLDocumentElement, AMLDocumentElement> mapping = new HashMap<AMLDocumentElement, AMLDocumentElement>();
			copy.deepCopy(group, copy, group, mapping);
			copy.deepCopyMirrors(group, copy, group, mapping);
		}
		return copy;
	}
	
	@Override
	public AMLValidationResultList validateGroupCreate() {
		assertNotDeleted();
		return AMLGroupContainerHelper.validateCreateGroup(this); 
	}

	@Override
	public AMLInternalElement createInternalElement(
			AMLSystemUnitClass systemUnitClass) throws AMLValidationException {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.createInternalElement(this, systemUnitClass);		
	}

	@Override
	public AMLValidationResultList validateInternalElementCreate(
			AMLSystemUnitClass systemUnitClass) {
		assertNotDeleted();
		return AMLInternalElementContainerHelper.validateCreateInternalElement(this, systemUnitClass);		
	}

	@Override
	public AMLValidationResultList validateCreateCOLLADAInterface(
			RefType refType) {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.validateCreateCOLLADAInterface(this, refType);
	}

	@Override
	public AMLCOLLADAInterface createCOLLADAInterface(RefType refType)
			throws AMLValidationException {
		assertNotDeleted();
		return AMLExternalInterfaceContainerHelper.createCOLLADAInterface(this, refType);
	}

	@Override
	public AMLCOLLADAInterface getCOLLADAInterface() {
		assertNotDeleted();
		return colladaInterface;
	}

	@Override
	public void _setCOLLADAInterface(AMLCOLLADAInterfaceImpl colladaInterface) {
		this.colladaInterface = colladaInterface;
	}

	@Override
	public AMLInternalLink createInternalLink(String linkName, AMLExternalInterface refPartnerSideA,
			AMLExternalInterface refPartnerSideB) throws AMLValidationException {
		assertNotDeleted();
		return AMLInternalLinkContainerHelper.createInternalLink(this, linkName, refPartnerSideA, refPartnerSideB);
	}

	@Override
	public AMLValidationResultList validateCreateInternalLink(String linkName, AMLExternalInterface refPartnerSideA,
			AMLExternalInterface refPartnerSideB) {
		assertNotDeleted();
		return AMLInternalLinkContainerHelper.validateCreateInternalLink(this, linkName, refPartnerSideA, refPartnerSideB);
	}

	@Override
	public Iterable<AMLInternalLink> getInternalLinks() {
		assertNotDeleted();
		return new ReadOnlyIterable<AMLInternalLink>(internalLinks);
	}

	@Override
	public Set<AMLInternalLink> _getInternalLinks() {
		return internalLinks;
	}

	@Override
	public void removeInternalLink(AMLInternalLink internalLink) {
		internalLinks.remove(internalLink);
		
	}

	@Override
	public AMLInternalLinkImpl _createInternalLink(String name, AMLExternalInterfaceImpl refPartnerSideA,
			AMLExternalInterfaceImpl refPartnerSideB) {
		return AMLInternalLinkContainerHelper._createInternalLink(this, name, refPartnerSideA, refPartnerSideB);
	}
	
	@Override
	public int getInternalLinksCount() {
		assertNotDeleted();
		return internalLinks.size();
	}

}
