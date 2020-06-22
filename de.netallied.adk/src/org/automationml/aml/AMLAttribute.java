/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;


public interface AMLAttribute extends AMLAttributeContainer, AMLRenamable {
	
	public interface Constraint extends AMLDocumentElement,  AMLRenamable{
		public String getName();
		
	}
	
	public interface UnknownConstraint  extends Constraint {

		void setRequirement(String string);

		String getRequirement();
	}
	
	public interface OrdinalScaledConstraint extends Constraint {

		void setRequiredValue(String string);

		void setRequiredMaxValue(String string);

		void setRequiredMinValue(String string);
		
		String getRequiredValue();

		String getRequiredMaxValue();

		String getRequiredMinValue();
	}
	
	public interface NominalScaledConstraint extends Constraint {

		void addRequiredValue(String string);
		
		Iterable<String> getRequiredValues();
		
		void removeRequiredValue(String value);
	}

	void setDescription(String description);

	void setDataType(String dataType);

	void setUnit(String unit);

	void setDefaultValue(String defaultValue);

	void setValue(String value);

	String getDescription();

	String getDataType();

	String getUnit();

	String getDefaultValue();

	String getValue();

	AMLValidationResultList validateDelete();

	void addRefSemantic(String refSemantic) throws AMLValidationException;

	Iterable<String> getRefSemantics();

	AMLValidationResultList validateAddRefSemantic(String refSemantic);

	AMLValidationResultList validateRemoveRefSemantic(String refSemantic);

	void removeRefSemantic(String refSemantic) throws AMLValidationException;

	Iterable<Constraint> getConstraints();

	Constraint getConstraint(String constraintName);

	boolean hasRefSemantic(String refSemantic);

	boolean hasConstraint(Constraint constraint);

	NominalScaledConstraint createNominalScaledConstraint(String name) throws AMLValidationException;

	OrdinalScaledConstraint createOrdinalScaledConstraint(String name) throws AMLValidationException;

	UnknownConstraint createUnknownConstraint(String name) throws AMLValidationException;

	AMLValidationResultList validateCreateNominalScaledConstraint(String constraint);
	
	AMLValidationResultList validateCreateOrdinalScaledConstraint(String constraint);
	
	AMLValidationResultList validateCreateUnknownConstraint(String constraint);

	int getRefSemanticCount();
}
