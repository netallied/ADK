/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml;

import java.util.LinkedHashMap;
import java.util.Map;

public class AMLElementNames {
	public static final String ATTRIBUTE_VALUE_AUTOMATION_ML_VERSION = "2.0";
	public static final String ATTRIBUTE_AUTOMATION_ML_VERSION = "AutomationMLVersion";
	public static final String ELEMENT_ADDITIONAL_INFORMATION = "AdditionalInformation";
	public static final String ATTRIBUTE_VALUE_XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String ATTRIBUTE_XMLNS_XSI = "xmlns:xsi";
	public static final String ATTRIBUTE_CAEX_CLASS_MODEL = "CAEX_ClassModel_V2.15.xsd";
	public static final String ATTRIBUTE_XSI_NO_NAMESPACE_SCHEMA_LOCATION = "xsi:noNamespaceSchemaLocation";
	public static final String ATTRIBUTE_VALUE_SCHEMA_VERSION = "2.15";
	public static final String ATTRIBUTE_SCHEMA_VERSION = "SchemaVersion";
	public static final String ATTRIBUTE_FILE_NAME = "FileName";
	public static final String ELEMENT_CAEX_FILE = "CAEXFile";
	public static final String ELEMENT_WRITER_PROJECT_ID = "WriterProjectID";
	public static final String ELEMENT_WRITER_PROJECT_TITLE = "WriterProjectTitle";
	public static final String ELEMENT_LAST_WRITING_DATE_TIME = "LastWritingDateTime";
	public static final String ELEMENT_WRITER_RELEASE = "WriterRelease";
	public static final String ELEMENT_WRITER_VERSION = "WriterVersion";
	public static final String ELEMENT_WRITER_VENDOR_URL = "WriterVendorURL";
	public static final String ELEMENT_WRITER_VENDOR = "WriterVendor";
	public static final String ELEMENT_WRITER_ID = "WriterID";
	public static final String ELEMENT_WRITER_NAME = "WriterName";
	public static final String ELEMENT_WRITER_HEADER = "WriterHeader";
	public static final String ELEMENT_INSTANCE_HIERARCHY = "InstanceHierarchy";
	public static final String ATTRIBUTE_NAME = "Name";
	public static final String ELEMENT_DESCRIPTION = "Description";
	public static final String ELEMENT_VERSION = "Version";
	public static final String ELEMENT_COPYRIGHT = "Copyright";
	public static final String ELEMENT_INTERNAL_ELEMENT = "InternalElement";
	public static final String ATTRIBUTE_ID = "ID";
	public static final String ELEMENT_ATTRIBUTE = "Attribute";
	public static final String ATTRIBUTE_UNIT = "Unit";
	public static final String ATTRIBUTE_ATTRIBUTE_DATA_TYPE = "AttributeDataType";
	public static final String ELEMENT_DEFAULT_VALUE = "DefaultValue";
	public static final String ELEMENT_VALUE = "Value";
	public static final String ELEMENT_SYSTEM_UNIT_CLASS_LIB = "SystemUnitClassLib";
	public static final String ELEMENT_ROLE_CLASS_LIB = "RoleClassLib";
	public static final String ELEMENT_INTERFACE_CLASS_LIB = "InterfaceClassLib";
	public static final String ATTRIBUTE_REF_BASE_CLASS_PATH = "RefBaseClassPath";
	public static final String ATTRIBUTE_REF_ROLE_CLASS_PATH = "RefRoleClassPath";
	public static final String ELEMENT_ROLE_CLASS = "RoleClass";
	public static final String ELEMENT_INTERFACE_CLASS = "InterfaceClass";
	public static final String ELEMENT_EXTERNAL_REFERENCE = "ExternalReference";
	public static final String ATTRIBUTE_PATH = "Path";
	public static final String ATTRIBUTE_ALIAS = "Alias";
	public static final String ELEMENT_SYSTEM_UNIT_CLASS = "SystemUnitClass";
	public static final String ATTRIBUTE_REF_BASE_SYSTEM_UNIT_PATH = "RefBaseSystemUnitPath";
	public static final String ELEMENT_EXTERNAL_INTERFACE = "ExternalInterface";
	public static final String ELEMENT_ROLE_REQUIREMENTS = "RoleRequirements";
	public static final String ELEMENT_CONSTRAINT = "Constraint";
	public static final String ELEMENT_INTERNAL_LINK = "InternalLink";
	public static final String ELEMENT_REVISION = "Revision";
	public static final String ELEMENT_SUPPORTED_ROLE_CLASS = "SupportedRoleClass";
	public static final String ELEMENT_MAPPING_OBJECT = "MappingObject";
	public static final String ELEMENT_ATTRIBUTE_NAME_MAPPING = "AttributeNameMapping";
	public static final String ATTRIBUTE_SYSTEM_UNIT_ATTRIBUTE_NAME = "SystemUnitAttributeName";
	public static final String ATTRIBUTE_ROLE_ATTRIBUTE_NAME = "RoleAttributeName";
	public static final String ATTRIBUTE_REF_BASE_ROLE_CLASS_PATH = "RefBaseRoleClassPath";
	public static final String ATTRIBUTE_REF_PARTNER_SIDE_A = "RefPartnerSideA";
	public static final String ATTRIBUTE_REF_PARTNER_SIDE_B = "RefPartnerSideB";
	public static final String ELEMENT_REVISION_DATE = "RevisionDate";
	public static final String ELEMENT_OLD_VERSION = "OldVersion";
	public static final String ELEMENT_NEW_VERSION = "NewVersion";
	public static final String ELEMENT_AUTHOR_NAME = "AuthorName";
	public static final String ELEMENT_COMMENT = "Comment";
	public static final String ELEMENT_UNKNOWN_TYPE = "UnknownType";
	public static final String ELEMENT_REQUIREMENTS = "Requirements";
	public static final String ELEMENT_NOMINAL_SCALED_TYPE = "NominalScaledType";
	public static final String ELEMENT_REQUIRED_VALUE = "RequiredValue";
	public static final String ELEMENT_ORDINAL_SCALED_TYPE = "OrdinalScaledType";
	public static final String ELEMENT_REQUIRED_MAX_VALUE = "RequiredMaxValue";
	public static final String ELEMENT_REQUIRED_MIN_VALUE = "RequiredMinValue";
	public static final String ELEMENT_REF_SEMANTIC = "RefSemantic";
	public static final String ATTRIBUTE_CORRESPONDING_ATTRIBUTE_PATH = "CorrespondingAttributePath";
	
	private static Map<String, AMLElementType> nameToElementType = new LinkedHashMap<String, AMLElementType>();
	
	static {
		register(ELEMENT_ADDITIONAL_INFORMATION, AMLElementType.ELEMENT_ADDITIONAL_INFORMATION);
		register(ELEMENT_CAEX_FILE, AMLElementType.ELEMENT_CAEX_FILE);
		register(ELEMENT_WRITER_PROJECT_ID, AMLElementType.ELEMENT_WRITER_PROJECT_ID);
		register(ELEMENT_WRITER_PROJECT_TITLE, AMLElementType.ELEMENT_WRITER_PROJECT_TITLE);
		register(ELEMENT_LAST_WRITING_DATE_TIME, AMLElementType.ELEMENT_LAST_WRITING_DATE_TIME);
		register(ELEMENT_WRITER_RELEASE, AMLElementType.ELEMENT_WRITER_RELEASE);
		register(ELEMENT_WRITER_VERSION, AMLElementType.ELEMENT_WRITER_VERSION);
		register(ELEMENT_WRITER_VENDOR_URL, AMLElementType.ELEMENT_WRITER_VENDOR_URL);
		register(ELEMENT_WRITER_VENDOR, AMLElementType.ELEMENT_WRITER_VENDOR);
		register(ELEMENT_WRITER_ID, AMLElementType.ELEMENT_WRITER_ID);
		register(ELEMENT_WRITER_NAME, AMLElementType.ELEMENT_WRITER_NAME);
		register(ELEMENT_WRITER_HEADER, AMLElementType.ELEMENT_WRITER_HEADER);
		register(ELEMENT_INSTANCE_HIERARCHY, AMLElementType.ELEMENT_INSTANCE_HIERARCHY);
		register(ELEMENT_DESCRIPTION, AMLElementType.ELEMENT_DESCRIPTION);
		register(ELEMENT_VERSION, AMLElementType.ELEMENT_VERSION);
		register(ELEMENT_COPYRIGHT, AMLElementType.ELEMENT_COPYRIGHT);
		register(ELEMENT_INTERNAL_ELEMENT, AMLElementType.ELEMENT_INTERNAL_ELEMENT);
		register(ELEMENT_ATTRIBUTE, AMLElementType.ELEMENT_ATTRIBUTE);
		register(ELEMENT_DEFAULT_VALUE, AMLElementType.ELEMENT_DEFAULT_VALUE);
		register(ELEMENT_VALUE, AMLElementType.ELEMENT_VALUE);
		register(ELEMENT_SYSTEM_UNIT_CLASS_LIB, AMLElementType.ELEMENT_SYSTEM_UNIT_CLASS_LIB);
		register(ELEMENT_ROLE_CLASS_LIB, AMLElementType.ELEMENT_ROLE_CLASS_LIB);
		register(ELEMENT_INTERFACE_CLASS_LIB, AMLElementType.ELEMENT_INTERFACE_CLASS_LIB);
		register(ELEMENT_ROLE_CLASS, AMLElementType.ELEMENT_ROLE_CLASS);
		register(ELEMENT_INTERFACE_CLASS, AMLElementType.ELEMENT_INTERFACE_CLASS);
		register(ELEMENT_EXTERNAL_REFERENCE, AMLElementType.ELEMENT_EXTERNAL_REFERENCE);
		register(ELEMENT_SYSTEM_UNIT_CLASS, AMLElementType.ELEMENT_SYSTEM_UNIT_CLASS);
		register(ELEMENT_EXTERNAL_INTERFACE, AMLElementType.ELEMENT_EXTERNAL_INTERFACE);
		register(ELEMENT_ROLE_REQUIREMENTS, AMLElementType.ELEMENT_ROLE_REQUIREMENTS);
		register(ELEMENT_CONSTRAINT, AMLElementType.ELEMENT_CONSTRAINT);
		register(ELEMENT_INTERNAL_LINK, AMLElementType.ELEMENT_INTERNAL_LINK);
		register(ELEMENT_REVISION, AMLElementType.ELEMENT_REVISION);
		register(ELEMENT_SUPPORTED_ROLE_CLASS, AMLElementType.ELEMENT_SUPPORTED_ROLE_CLASS);
		register(ELEMENT_MAPPING_OBJECT, AMLElementType.ELEMENT_MAPPING_OBJECT);
		register(ELEMENT_ATTRIBUTE_NAME_MAPPING, AMLElementType.ELEMENT_ATTRIBUTE_NAME_MAPPING);
		register(ELEMENT_REVISION_DATE, AMLElementType.ELEMENT_REVISION_DATE);
		register(ELEMENT_OLD_VERSION, AMLElementType.ELEMENT_OLD_VERSION);
		register(ELEMENT_NEW_VERSION, AMLElementType.ELEMENT_NEW_VERSION);
		register(ELEMENT_AUTHOR_NAME, AMLElementType.ELEMENT_AUTHOR_NAME);
		register(ELEMENT_COMMENT, AMLElementType.ELEMENT_COMMENT);
		register(ELEMENT_UNKNOWN_TYPE, AMLElementType.ELEMENT_UNKNOWN_TYPE);
		register(ELEMENT_REQUIREMENTS, AMLElementType.ELEMENT_REQUIREMENTS);
		register(ELEMENT_NOMINAL_SCALED_TYPE, AMLElementType.ELEMENT_NOMINAL_SCALED_TYPE);
		register(ELEMENT_REQUIRED_VALUE, AMLElementType.ELEMENT_REQUIRED_VALUE);
		register(ELEMENT_ORDINAL_SCALED_TYPE, AMLElementType.ELEMENT_ORDINAL_SCALED_TYPE);
		register(ELEMENT_REQUIRED_MAX_VALUE, AMLElementType.ELEMENT_REQUIRED_MAX_VALUE);
		register(ELEMENT_REQUIRED_MIN_VALUE, AMLElementType.ELEMENT_REQUIRED_MIN_VALUE);
		register(ELEMENT_REF_SEMANTIC, AMLElementType.ELEMENT_REF_SEMANTIC);
	
	}

	private static void register(String name, AMLElementType elementType) {
		nameToElementType.put(name, elementType);
	}

	public static AMLElementType getElementType(String name) {
		AMLElementType elementType = nameToElementType.get(name);
		return elementType;
	}

}
