package com.pengtoolbox.cfw.tests.datahandling;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.features.usermgmt.Role;

public class TestCFWField {
	protected static Role testgroupA;
	
	
	@Test
	public void testEncryptAndDecrypt() throws SQLException {
		
		//-----------------------
		// Encrypt Value
		CFWField<String> field = CFWField.newString(FormFieldType.NONE, "field")
				.enableEncryption("myTestSalt123")
				.setValue("myValue");
		
		String valueEncrypted = field.getValueEncrypted();
		System.out.println("valueEncrypted: "+valueEncrypted);
		
		Assertions.assertEquals("cfwenc:CayKUvJvRn7ElqXKfwUkEQ==", valueEncrypted, "The value is encoded as expected.");
		
		//-----------------------
		// Decrypt Value
		CFWField<String> decryptField = CFWField.newString(FormFieldType.NONE, "decryptfield")
				.enableEncryption("myTestSalt123")
				.setValue("defaultValue");

		String valueDecrypted = decryptField.decryptValue(valueEncrypted);
		System.out.println("valueDecrypted: "+valueDecrypted);
		
		Assertions.assertEquals("myValue", valueDecrypted, "The value is decoded.");
		Assertions.assertEquals("defaultValue", decryptField.getValue(), "The value of the field is untouched.");
		
		
		//-----------------------
		// Encrypt Value
		CFWField<String> specialfield = CFWField.newString(FormFieldType.NONE, "field")
				.enableEncryption("myTestSalt123")
				.setValue("my:Value\\\\Special!{}Chars!ü()%$*#");
		
		String specialEncrypted = specialfield.getValueEncrypted();
		System.out.println("specialEncrypted: "+specialEncrypted);
		
		Assertions.assertEquals("cfwenc:qUyYuDi/22yaZmQf0pU9Qna7CgIfguHZOqEtjvgqDNnOxQh0InsyCtFmYuwvSega", specialEncrypted, "The value is encoded as expected.");
		
		//-----------------------
		// Decrypt Special Chars
		CFWField<String> decryptSpecialField = CFWField.newString(FormFieldType.NONE, "decryptfield")
				.enableEncryption("myTestSalt123")
				.setValue("defaultValue");

		String specialDecrypted = decryptSpecialField.decryptValue(specialEncrypted);
		System.out.println("specialDecrypted: "+specialDecrypted);
		
		Assertions.assertEquals("my:Value\\\\Special!{}Chars!ü()%$*#", specialDecrypted, "The value is decoded.");
		
		
		
		//-----------------------
		// Encrypt Null Value
		CFWField<String> fieldNull = CFWField.newString(FormFieldType.NONE, "field")
				.enableEncryption("myTestSalt123")
				.setValue(null);
		
		String nullvalueEncrypted = fieldNull.getValueEncrypted();
		System.out.println("nullvalueEncrypted: "+nullvalueEncrypted);
		
		Assertions.assertEquals(null, nullvalueEncrypted, "Null values will not be encrypted.");
		
		//-----------------------
		// Decrypt Null Value
		String nullvalueDecrypted = decryptSpecialField.decryptValue(null);
		System.out.println("nullvalueDecrypted: "+nullvalueDecrypted);
		
		Assertions.assertEquals(null, nullvalueDecrypted, "The null value is returned as is and not decoded.");
		
		
		//-----------------------
		// Encrypt Empty String
		CFWField<String> fieldEmptyString = CFWField.newString(FormFieldType.NONE, "field")
				.enableEncryption("myTestSalt123")
				.setValue(" ");
		
		String emptyStringEncrypted = fieldNull.getValueEncrypted();
		System.out.println("emptyStringEncrypted: "+emptyStringEncrypted);
		
		Assertions.assertEquals(null, emptyStringEncrypted, "Empty String will be encrypted to null.");
		
				
	}
	
	@Test
	public void testEncryptAndDecryptSpecial() throws SQLException {
		
		String valueToEncrypt = "my Test String";
		String salt = "my salt";
		//-----------------------
		// Encrypt Value
		CFWField<String> field = CFWField.newString(FormFieldType.NONE, "field")
				.enableEncryption(salt)
				.setValue(valueToEncrypt);

		//cfwenc:YX3GfH1WA3Pzz+pz+4sREvyCjhalQPEYOkqCoXBqDxQ\u003d
		//cfwenc:bxrnX1Lr3COXiX8qJgs34qukjyJBpgrUcteJ4eh7XdY=
		String valueEncrypted = field.getValueEncrypted();
		String valueDecrypted = field.decryptValue(valueEncrypted);
		System.out.println("Encrypted: "+valueEncrypted);
		System.out.println("Decrypted: "+valueDecrypted);
		
	}
}
