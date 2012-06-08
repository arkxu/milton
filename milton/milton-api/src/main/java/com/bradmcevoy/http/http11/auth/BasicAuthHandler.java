/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package com.bradmcevoy.http.http11.auth;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Auth.Scheme;
import com.bradmcevoy.http.AuthenticationHandler;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class BasicAuthHandler implements AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger( BasicAuthHandler.class );

	@Override
    public boolean supports( Resource r, Request request ) {
        Auth auth = request.getAuthorization();
        if( auth == null ) {
            return false;
        }
        log.trace( "supports: {}", auth.getScheme() );
        return auth.getScheme().equals( Scheme.BASIC );
    }

	@Override
    public Object authenticate( Resource resource, Request request ) {
        log.trace( "authenticate" );
        Auth auth = request.getAuthorization();
        Object o = resource.authenticate( auth.getUser(), auth.getPassword() );
        log.trace( "result: {}", o );
        return o;
    }

	@Override
    public String getChallenge( Resource resource, Request request ) {
		if( resource == null ) {
			throw new RuntimeException("Can't generate challenge because resource is null, so can't get realm");
		}
        return "Basic realm=\"" + resource.getRealm() + "\"";
    }

	@Override
    public boolean isCompatible( Resource resource ) {
        return true;
    }
}
