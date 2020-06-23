package com.pengtoolbox.cfw.tests.performance;

import com.pengtoolbox.cfw.db.spaces.Space;
import com.pengtoolbox.cfw.db.spaces.SpaceGroup;
import com.pengtoolbox.cfw.features.usermgmt.Role;
import com.pengtoolbox.cfw.features.usermgmt.User;

public class TestPerformanceObjectCreation {

	
	public static void main(String[] args) {

		long start = System.nanoTime();
        
		//-----------------------------------
		// 
		start = System.nanoTime();
		
		for(int i = 0; i < 1000; i++) {
			SpaceGroup group = new SpaceGroup("Test");
		}
		
		System.out.println("SpaceGroup: "+(System.nanoTime() - start)/1000000);

		//-----------------------------------
		// 
		start = System.nanoTime();
		
		for(int i = 0; i < 1000; i++) {
			Role role = new Role("Test", "test");
		}
		
		System.out.println("Role: "+(System.nanoTime() - start)/1000000);

		//-----------------------------------
		// 
		start = System.nanoTime();
		
		for(int i = 0; i < 1000; i++) {
			User user = new User("Test");
		}
		
		System.out.println("User: "+(System.nanoTime() - start)/1000000);

		//-----------------------------------
		// 
		start = System.nanoTime();
		
		for(int i = 0; i < 1000; i++) {
			Space space = new Space(1, "Test");
		}
		
		System.out.println("Space: "+(System.nanoTime() - start)/1000000);
	}

}
