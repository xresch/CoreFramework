package com.xresch.cfw.features.core.auth;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw.features.contextsettings.AbstractContextSettings;

public abstract class SSOProviderSettings extends AbstractContextSettings {

	// will be added to session attributes by ServletLogin.doSSORedirect()
	public static final String PROPERTY_SSO_PROVIDER_ID = "ssoProviderID";

	public abstract URI createRedirectURI(HttpServletRequest request, HttpServletResponse response, String targetURL);

	protected abstract boolean isDefined();

	protected abstract String getSettingsType();

}
