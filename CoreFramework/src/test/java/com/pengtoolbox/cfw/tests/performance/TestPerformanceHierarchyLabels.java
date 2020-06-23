package com.pengtoolbox.cfw.tests.performance;

public class TestPerformanceHierarchyLabels {

	private static String[] labels = new String[] { 
			"P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8", "P9", "P10",
			"P11", "P12", "P13", "P14", "P15", "P16", "P17", "P18", "P19", "P20", "P21", "P22", "P23", "P24", "P25",
			"P26", "P27", "P28", "P29", };

	public static void main(String[] args) {

		// ------------------------------------
		//
		long start = System.nanoTime();

		for (int i = 0; i < 1000000; i++) {
			for (int j = 0; j < 25; j++) {
				String s = "P" + j;
			}
		}

		System.out.println("No Arrays: " + (System.nanoTime() - start) / 1000000);

		// ------------------------------------
		//
		start = System.nanoTime();

		for (int i = 0; i < 1000000; i++) {
			for (int j = 0; j < 25; j++) {
				String s = labels[j];
			}
		}

		System.out.println("Arrays: " + (System.nanoTime() - start) / 1000000);
	}

}
