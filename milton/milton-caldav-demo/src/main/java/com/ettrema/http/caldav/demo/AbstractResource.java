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

package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.ReportableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestGenerator;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import java.util.Date;

/**
 * BM: added reportable so that all these resource classes work with REPORT
 *
 * @author alex
 */
public class AbstractResource implements Resource, ReportableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractResource.class);
    protected String name;
    protected Date modDate;
    protected Date createdDate;
    protected TFolderResource parent;

    public AbstractResource(TFolderResource parent, String name) {
        this.parent = parent;
        this.name = name;
        modDate = new Date();
        createdDate = new Date();
        if (parent != null) {
            checkAndRemove(parent, name);
            parent.children.add(this);
        }
    }

	TCalDavPrincipal getUser() {
		TFolderResource p = parent;
		while( p != null ) {
			if( p instanceof TCalDavPrincipal ) {
				return (TCalDavPrincipal) p;
			} else {
				p = p.parent;
			}
		}
		return null;
	}
	
	@Override
    public Object authenticate(String user, String requestedPassword) {
		TCalDavPrincipal p = TResourceFactory.findUser(user);
		if( p != null ) {
			if( p.getPassword().equals(requestedPassword)) {
				return p;
			} else {
				log.warn("that password is incorrect. Try 'password'");
			}
		} else {
			log.warn("user not found: " + user + " - try 'userA'");
		}
		return null;
			
    }

    public Object authenticate(DigestResponse digestRequest) {
		TCalDavPrincipal p = TResourceFactory.findUser(digestRequest.getUser());
		if( p != null ) {
			DigestGenerator gen = new DigestGenerator();
			String actual = gen.generateDigest(digestRequest, p.getPassword());
			if( actual.equals(digestRequest.getResponseDigest())) {
				return p;
			} else {
				log.warn("that password is incorrect. Try 'password'");
			}
		} else {
			log.warn("user not found: " + digestRequest.getUser() + " - try 'userA'");
		}
		return null;

    }

	@Override
    public String getUniqueId() {
        return this.hashCode() + "";
    }

	@Override
    public String checkRedirect(Request request) {
        return null;
    }

	@Override
    public String getName() {
        return name;
    }

	@Override
    public boolean authorise(Request request, Method method, Auth auth) {
        log.debug("authorise");
        return auth != null;
    }

	@Override
    public String getRealm() {
        return "testrealm@host.com";
    }

	@Override
    public Date getModifiedDate() {
        return modDate;
    }

    private void checkAndRemove(TFolderResource parent, String name) {
        TResource r = (TResource) parent.child(name);
        if (r != null) {
            parent.children.remove(r);
        }
    }

}
