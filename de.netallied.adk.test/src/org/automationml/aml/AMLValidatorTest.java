/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.aml;

import org.automationml.aml.TestValidator.TestValidatorFactory;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

import static org.fest.assertions.Fail.fail;

public class AMLValidatorTest extends AbstractAMLTest {

	@Test
	public void valid_unsetValidator() throws Exception {
		TestValidatorFactory validatorFactory = new TestValidatorFactory();
		session.setValidator(validatorFactory);
		TestValidator testValidator = (TestValidator) session.getValidator();
		assertThat(testValidator.disposed).isFalse();
		session.unsetValidator();
		assertThat(testValidator.disposed).isTrue();
		session.setValidator(validatorFactory);
	}

	@Test
	public void invalid_setSecondValidator_withoutUnset() throws Exception {
		TestValidatorFactory validatorFactory = new TestValidatorFactory();
		session.setValidator(validatorFactory);
		try {
			session.setValidator(validatorFactory);
			fail();
		} catch (AMLValidatorException e) {
		}
	}
}
