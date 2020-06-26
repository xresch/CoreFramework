package com.xresch.cfw.features.core.auth;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.util.security.Credential;

public class CFWLoginService extends AbstractLoginService {

	@Override
	protected String[] loadRoleInfo(UserPrincipal user) {
		System.out.println("loadRoleInfo:"+user.toString());
		return null;
	}

	@Override
	protected UserPrincipal loadUserInfo(String username) {
		System.out.println("loadUserInfo:"+username);
		UserPrincipal principal = new UserPrincipal(username, Credential.getCredential(""));
		return principal;
	}

}
