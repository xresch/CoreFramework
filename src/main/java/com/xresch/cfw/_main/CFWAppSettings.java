package com.xresch.cfw._main;

public class CFWAppSettings {
	
	
	private static boolean enableContextSettings = false;
	private static boolean enableDashboarding = false;
	private static boolean enableSpaces = false;
	
	public static boolean isContextSettingsEnabled() {
		return enableContextSettings;
	}
	
	public static void enableContextSettings(boolean enableContextSettings) {
		CFWAppSettings.enableContextSettings = enableContextSettings;
	}

	public static boolean isDashboardingEnabled() {
		return enableDashboarding;
	}

	public static void enableDashboarding(boolean enableDashboarding) {
		CFWAppSettings.enableDashboarding = enableDashboarding;
	}
	
	public static boolean isSpacesEnabled() {
		return enableSpaces;
	}

	public static void enableSpaces(boolean enableSpaces) {
		CFWAppSettings.enableSpaces = enableSpaces;
	}
	

}
