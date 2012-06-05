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

/**
 * Parse a string into three components
 *  - username
 *  - domain
 *  - authority
 *
 * The domain field is to support servers which serve multiple domains, where the user list
 * is defined per domain.
 *
 * The authority field is to support hierarchies of domains, where the user from
 * an owning domain will get access to child domains
 *
 * @author brad
 */
public class NameAndAuthority {

    public static final String DELIM_HOST = "#";
    public static final String DELIM_AUTHORITY = ":";

    /**
     * Valid forms:
     * 
     * joe - domain and authority are null
     * joe#www.joe.com - domain and authority are 'www.joe.com'
     * joe#admin.joe.com:www.joes-client.com - authority is 'admin.joe.com', domain is 'www.joes-client.com'
     *
     * @param user - a string in one of the above forms
     * @return
     */
    public static NameAndAuthority parse( String user ) {
        if( user == null ) return new NameAndAuthority( null );
        int pos = user.indexOf( "#" );
        if( pos < 0 ) {
            return new NameAndAuthority( user );
        } else {
            String name = user.substring( 0, pos );
            String authority = user.substring( pos + 1, user.length() );
            pos = authority.indexOf( DELIM_AUTHORITY);
            if( pos < 0 ) {
                return new NameAndAuthority( name, authority,authority );
            } else {
                String domain = authority.substring( pos+1, authority.length());
                authority = authority.substring( 0, pos);
                return new NameAndAuthority( name, authority, domain );
            }
        }
    }
    /**
     * The username part. E.g. brad
     */
    public final String name;
    
    /**
     * The domain they are logging into. Optional. E.g. milton.ettrema.com
     */
    public final String domain;

    /**
     * The name of the authority which grants access. Optional E.g. www.ettrema.com
     *
     * This is typically a domain which owns the domain being logged into.
     *
     * If not given explicitly defaults to the domain
     */
    public final String authority;

    public NameAndAuthority( String name ) {
        this( name, null, null );
    }

    public NameAndAuthority( String name, String authority, String domain ) {
        this.name = name;
        this.authority = authority;
        this.domain = domain;
    }

    public String toMilton() {
        if( authority == null ) {
            return name;
        } else {
            return name + "@" + authority;
        }
    }
}
