/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ettrema.sso;

import com.bradmcevoy.http.AuthenticationHandler;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;

/**
 * This is a post resource-resolution authentication handler. 
 * 
 * It assumes that the SsoResourceFactory has populated the _sso_user
 * request attribute if appropriate
 *
 * @author brad
 */
public class SsoAuthenticationHandler implements AuthenticationHandler {


	
	public boolean supports(Resource r, Request request) {
		boolean b = request.getAttributes().get("_sso_user") != null;		
		return b;
	}

	public Object authenticate(Resource resource, Request request) {
		return request.getAttributes().get("_sso_user");
	}

	public String getChallenge(Resource resource, Request request) {
		return null;
	}

	public boolean isCompatible(Resource resource) {
		return true;
	}	
}
