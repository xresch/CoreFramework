package com.xresch.cfw.tests.features.usermgmt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestSessionData extends DBTestMaster {
	
	@Test
	public void testSerialization() throws IOException, ClassNotFoundException {
		
		CFWSessionData originalData = new CFWSessionData("sessionID-1234-abcd");
		originalData.setUser(CFW.DB.Users.selectByUsernameOrMail("admin"));
		originalData.setClientIP("1.2.3.4");
		originalData.triggerLogin();
		
		FileOutputStream fileOutputStream
	      = new FileOutputStream("./temp/TestSessionData.testSerialization.txt");
	    ObjectOutputStream objectOutputStream 
	      = new ObjectOutputStream(fileOutputStream);
	    objectOutputStream.writeObject(originalData);
	    objectOutputStream.flush();
	    objectOutputStream.close();

	    FileInputStream fileInputStream 
	      = new FileInputStream("./temp/TestSessionData.testSerialization.txt");
	    ObjectInputStream objectInputStream 
	      = new ObjectInputStream(fileInputStream);
	    CFWSessionData dataFromFile = (CFWSessionData) objectInputStream.readObject();
	    objectInputStream.close();
	    
	    Assertions.assertTrue(dataFromFile.isLoggedIn(), "User is logged in.");
	    Assertions.assertEquals("sessionID-1234-abcd", dataFromFile.getSessionID(), "SessionID deserialized successfully.");
	    Assertions.assertEquals("1.2.3.4", dataFromFile.getClientIP(), "ClientIP deserialized successfully.");
	    Assertions.assertEquals("admin", dataFromFile.getUser().username(), "User was read from database.");
	    
	    
		
		
	}
}
