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


package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public class UnlockHandler implements ExistingEntityHandler {

    private Logger log = LoggerFactory.getLogger(UnlockHandler.class);

    private final ResourceHandlerHelper resourceHandlerHelper;

    private final WebDavResponseHandler responseHandler;

    public UnlockHandler( ResourceHandlerHelper resourceHandlerHelper, WebDavResponseHandler responseHandler ) {
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.responseHandler = responseHandler;
    }


    public void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException {
        resourceHandlerHelper.process( httpManager, request, response, this );
    }

    public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.processResource( manager, request, response, r, this );
    }

    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
        LockableResource r = (LockableResource) resource;
        String sToken = request.getLockTokenHeader();        
        sToken = LockHandler.parseToken(sToken);
        
        // this should be checked in processResource now
        
//       	if( r.getCurrentLock() != null &&
//       			!sToken.equals( r.getCurrentLock().tokenId) &&
//       			isLockedOut( request, resource ))
//    	{
//       		//Should this be unlocked easily? With other tokens?
//    		response.setStatus(Status.SC_LOCKED);
//    	    log.info("cant unlock with token: " + sToken);
//    		return;
//    	}

        
        log.debug("unlocking token: " + sToken);
        try {
            r.unlock( sToken );
            responseHandler.respondNoContent( resource, response, request );
        } catch( PreConditionFailedException ex ) {
            responseHandler.respondPreconditionFailed( request, response, resource );
        }
    }
    
    public String[] getMethods() {
        return new String[]{Method.UNLOCK.code};
    }
    
    @Override
    public boolean isCompatible( Resource handler ) {
        return handler instanceof LockableResource;
    }

    
}
