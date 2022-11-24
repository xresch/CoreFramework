package com.xresch.cfw.features.core.auth;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw.features.contextsettings.AbstractContextSettings;

public abstract class SSOProviderSettings extends AbstractContextSettings {

	public abstract URI createRedirectURI(HttpServletRequest request, String targetURL);

	protected abstract boolean isDefined();

	protected abstract String getSettingsType();

}
