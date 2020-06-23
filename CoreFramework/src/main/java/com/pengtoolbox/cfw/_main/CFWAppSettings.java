package com.pengtoolbox.cfw._main;

public class CFWAppSettings {
	
	private static boolean enableDashboarding = false;
	private static boolean enableContextSettings = false;
	
	public static boolean isContextSettingsEnabled() {
		return enableContextSettings;
	}
	
	public static void setEnableContextSettings(boolean enableContextSettings) {
		CFWAppSettings.enableContextSettings = enableContextSettings;
	}

	public static boolean isDashboardingEnabled() {
		return enableDashboarding;
	}

	public static void setEnableDashboarding(boolean enableDashboarding) {
		CFWAppSettings.enableDashboarding = enableDashboarding;
	}
	
	
	

}
