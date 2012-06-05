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

package com.ettrema.ftp;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 * Wraps a standard milton securitymanager to implement the UserService required
 * by FTP.
 *
 * Note that not all FTP methods are implemented, but enough for users to use
 * FTP.
 *
 * @author brad
 */
public class WrappingFtpSecurityManager implements com.bradmcevoy.http.SecurityManager, UserService{
    private final com.bradmcevoy.http.SecurityManager wrapped;

    public WrappingFtpSecurityManager( com.bradmcevoy.http.SecurityManager wrapped ) {
        this.wrapped = wrapped;
    }

    /**
     * Not supported
     *
     * @param name
     */
    public void delete( String name ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    /**
     * Not supported
     *
     * @param name
     * @return
     */
    public boolean doesExist( String name ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    /**
     * Not supported
     *
     * @return
     */
    public String[] getAllUserNames() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public MiltonUser getUserByName( String name, String domain ) {
        return new MiltonUser( name, name, domain );
    }

    public void save( MiltonUser user ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Object authenticate( DigestResponse dr ) {
        return wrapped.authenticate( dr );
    }

    public Object authenticate( String string, String string1 ) {
        return wrapped.authenticate( string, string1 );
    }

    public boolean authorise( Request rqst, Method method, Auth auth, Resource rsrc ) {
        return wrapped.authorise( rqst, method, auth, rsrc );
    }

    public String getRealm( String string ) {
        return wrapped.getRealm( string );
    }

	public boolean isDigestAllowed() {
		return true;
	}

}
