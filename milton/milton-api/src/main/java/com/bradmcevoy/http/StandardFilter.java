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

package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public class StandardFilter implements Filter {

    private Logger log = LoggerFactory.getLogger( StandardFilter.class );
    public static final String INTERNAL_SERVER_ERROR_HTML = "<html><body><h1>Internal Server Error (500)</h1></body></html>";

    public StandardFilter() {
    }

	@Override
    public void process( FilterChain chain, Request request, Response response ) {
        HttpManager manager = chain.getHttpManager();
        try {
            Request.Method method = request.getMethod();

            Handler handler = manager.getMethodHandler( method );
            if( handler == null ) {
                log.trace( "No handler for: " + method );
                manager.getResponseHandler().respondMethodNotImplemented( null, response, request );
            } else {
                if( log.isTraceEnabled() ) {
                    log.trace( "delegate to method handler: " + handler.getClass().getCanonicalName() );
                }
                handler.process( manager, request, response );
                if (response.getEntity() != null) {
                    manager.sendResponseEntity(response);
                }
            }

        } catch( BadRequestException ex ) {
            log.warn( "BadRequestException: " + ex.getReason(), ex );
            manager.getResponseHandler().respondBadRequest( ex.getResource(), response, request );
        } catch( ConflictException ex ) {
            log.warn( "conflictException: " + ex.getMessage() );
            manager.getResponseHandler().respondConflict( ex.getResource(), response, request, INTERNAL_SERVER_ERROR_HTML );
        } catch( NotAuthorizedException ex ) {
            log.warn( "NotAuthorizedException" );
            manager.getResponseHandler().respondUnauthorised( ex.getResource(), response, request );
        } catch( Throwable e ) {
            log.error( "process", e );
            try {
                manager.getResponseHandler().respondServerError( request, response, INTERNAL_SERVER_ERROR_HTML );
            } catch( Throwable ex ) {
                log.error( "Exception generating server error response, setting response status to 500", ex );
                response.setStatus( Response.Status.SC_INTERNAL_SERVER_ERROR );
            }
        } finally {
            manager.closeResponse(response);
        }
    }
}
