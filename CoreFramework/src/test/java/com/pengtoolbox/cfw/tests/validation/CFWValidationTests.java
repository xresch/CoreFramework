package com.pengtoolbox.cfw.tests.validation;

import java.sql.Date;
import java.sql.Timestamp;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.pengtoolbox.cfw.validation.AbstractValidatable;
import com.pengtoolbox.cfw.validation.BooleanValidator;
import com.pengtoolbox.cfw.validation.EmailValidator;
import com.pengtoolbox.cfw.validation.EpochOrTimeValidator;
import com.pengtoolbox.cfw.validation.IValidatable;
import com.pengtoolbox.cfw.validation.IntegerValidator;
import com.pengtoolbox.cfw.validation.LengthValidator;
import com.pengtoolbox.cfw.validation.PasswordValidator;

@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
public class CFWValidationTests {

	@Test
	public void testBooleanValidator() {
		
		IValidatable validateThis = new AbstractValidatable() {};
		BooleanValidator bv = new BooleanValidator(validateThis);
		
		
		Assertions.assertTrue(validateThis.setValueValidated(true),
				" Boolean value 'true' is recognized as boolean.");
		
		Assertions.assertTrue(validateThis.setValueValidated(false),
				" Boolean value 'true' is recognized as boolean.");
		
		Assertions.assertTrue(validateThis.setValueValidated("true"),
				"String value 'true' is recognized as boolean.");
		
		Assertions.assertTrue(validateThis.setValueValidated("FALSE"),
				"String value 'FALSE' is recognized as boolean.");
		
		Assertions.assertFalse(validateThis.setValueValidated("NotABoolean"),
				"String value 'NotABoolean' is not a boolean.");
		
	}
	
	@Test
	public void testIntegerValidator() {
		
		IValidatable validateThis = new AbstractValidatable() {};
		IntegerValidator bv = new IntegerValidator(validateThis);
		
		Assertions.assertTrue(validateThis.setValueValidated(1),
				" Integer value '1' is recognized as Integer.");
		
		Assertions.assertTrue(validateThis.setValueValidated(-1),
				" Integer value '-1' is recognized as Integer.");
		
		Assertions.assertTrue(validateThis.setValueValidated("123456"),
				" String value '123456' is recognized as Integer.");
		
		Assertions.assertTrue(validateThis.setValueValidated("-123456"),
				" String value '-123456' is recognized as Integer.");
		
		Assertions.assertTrue(validateThis.setValueValidated("+123456"),
				" String value '+123456' is recognized as Integer.");
		
		Assertions.assertTrue(validateThis.setValueValidated("0"),
				" String value '0' is recognized as Integer.");
		
		Assertions.assertFalse(validateThis.setValueValidated("1.23456"),
				" String value '1.23456' is not recognized as Integer.");
		
		Assertions.assertFalse(validateThis.setValueValidated("1,23456"),
				" String value '1,23456' is not recognized as Integer.");
		
		Assertions.assertFalse(validateThis.setValueValidated("a1"),
				" String value 'a1' is not recognized as Integer.");
	}
	
	@Test
	public void testLengthValidator() {
		
		IValidatable validateThis = new AbstractValidatable() {};
		LengthValidator lv = new LengthValidator(validateThis, 5, 10);
		
		Assertions.assertFalse(validateThis.setValueValidated("4444"), "Value is invalid below min length.");
		Assertions.assertTrue(validateThis.setValueValidated("55555"), "Value is valid at min length.");	
		Assertions.assertTrue(validateThis.setValueValidated("1010101010"), "Value is valid at max length.");
		Assertions.assertFalse(validateThis.setValueValidated("11111111111"), "Value is invalid above max length.");
	}
	
	@Test
	public void testPasswordValidator() {
		
		IValidatable validateThis = new AbstractValidatable() {};
		PasswordValidator pv = new PasswordValidator(validateThis);
		
		Assertions.assertTrue( validateThis.setValueValidated("Aa123456"), "Is a valid password");
		Assertions.assertTrue( validateThis.setValueValidated("Aa------"), "Is a valid password");
		Assertions.assertTrue( validateThis.setValueValidated("A-aaaaaa"), "Is a valid password");
		Assertions.assertTrue( validateThis.setValueValidated("-aaaaaaA"), "Is a valid password");
		
		Assertions.assertFalse(validateThis.setValueValidated("Aa12345"), "Password is invalid below min length.");
		Assertions.assertFalse(validateThis.setValueValidated("Aaaaaaaa"), "Password is invalid when missing non-letter character.");
		Assertions.assertFalse(validateThis.setValueValidated("A1234567"), "Password is invalid when missing small letter character.");
		Assertions.assertFalse(validateThis.setValueValidated("a1234567"), "Password is invalid when missing capital letter character.");
	}
	

	@Test
	public void testEmailValidator() {
		
		IValidatable validateThis = new AbstractValidatable() {};
		EmailValidator pv = new EmailValidator(validateThis);
		
		Assertions.assertTrue( validateThis.setValueValidated("testika@testonia.com"), "Is a valid email address");
		Assertions.assertTrue( validateThis.setValueValidated("a@a.c"), "Is a valid email address");
		Assertions.assertTrue( validateThis.setValueValidated("a.b.c@c.d.com"), "Is a valid email address");
		Assertions.assertTrue( validateThis.setValueValidated("dash-dash@test-dash.com"), "Is a valid email address");
		Assertions.assertTrue( validateThis.setValueValidated("____underscore___@test.com"), "Is a valid email address");
		Assertions.assertTrue( validateThis.setValueValidated("email@123.123.123.123"), "Is a valid email address");
		Assertions.assertTrue( validateThis.setValueValidated("email@[123.123.123.123]"), "Is a valid email address");
		Assertions.assertTrue( validateThis.setValueValidated("\"email\"@domain.com"), "Is a valid email address");
		Assertions.assertTrue( validateThis.setValueValidated("1234567890@domain.com"), "Is a valid email address");
		//Assertions.assertTrue( validateThis.setValueValidated("UPPERCASE@TEST.COM"), "Is a valid email address");
		
		Assertions.assertFalse(validateThis.setValueValidated("plaintext"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("#@%^%#$@#$@#.com"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("@domain.com"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("Joe Smith <email@domain.com>"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("email.domain.com"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("email@domain@domain.com"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated(".email@domain.com"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("email.@domain.com"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("email..email@domain.com"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("あいうえお@domain.com"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("email@domain.com (Joe Smith)"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("email@domain"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("email@-domain.com"), "Is an invalid email address");
		//Assertions.assertFalse(validateThis.setValueValidated("email@111.222.333.44444"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("email@domain"), "Is an invalid email address");
		Assertions.assertFalse(validateThis.setValueValidated("email@domain..com"), "Is an invalid email address");
		
	}
	
	@Test
	public void testEpochOrTimeValidator() {
		
		IValidatable validateThis = new AbstractValidatable() {};
		EpochOrTimeValidator bv = new EpochOrTimeValidator(validateThis);
		
		
		Assertions.assertTrue(validateThis.setValueValidated(null),
				" Null validates to true");
				
		Assertions.assertTrue(validateThis.setValueValidated(1580053600000L),
				" Long value '1580053600000' is recognized as epoch time.");
		
		Assertions.assertTrue(validateThis.setValueValidated("1580053600000"),
				" String value '1580053600000' is recognized as epoch time.");
		
		Assertions.assertTrue(validateThis.setValueValidated(new Date(1580053600000L)),
				" Date is recognized as epoch time.");
		
		Assertions.assertTrue(validateThis.setValueValidated(new Timestamp(1580053600000L)),
				" Timestamp is recognized as epoch time.");
		
		Assertions.assertFalse(validateThis.setValueValidated(1),
				" Integers is not recognized as epoch time.");
		
		bv.setNullAllowed(false);
		Assertions.assertFalse(validateThis.setValueValidated(null),
				" Null validates to false.");
		
		
	}
	
}
