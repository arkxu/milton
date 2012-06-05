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

package com.ettrema.json;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forwards the POST request to the move method on the wrapped
 * resource
 *
 * @author brad
 */
public class MoveJsonResource extends JsonResource implements PostableResource {

    private static final Logger log = LoggerFactory.getLogger( MoveJsonResource.class );
    private final String host;
    private final ResourceFactory resourceFactory;
    private final MoveableResource wrapped;

    public MoveJsonResource( String host, MoveableResource copyableResource, ResourceFactory resourceFactory ) {
        super(copyableResource, Request.Method.COPY.code, null);
        this.host = host;
        this.wrapped = copyableResource;
        this.resourceFactory = resourceFactory;
    }
    public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) throws BadRequestException, NotAuthorizedException {
        String dest = parameters.get( "destination");
        if( dest == null ) {
            throw new BadRequestException(this, "The destination parameter is null");
        } else if (dest.trim().length() == 0 ) {
            throw new BadRequestException(this, "The destination parameter is empty");
        }
        Path pDest = Path.path( dest );
        if( pDest == null ) {
            throw new BadRequestException(this, "Couldnt parse the destination header, returned null from: " + dest);
        }
        String parentPath = "/";
        if( pDest.getParent() != null ) {
            parentPath = pDest.getParent().toString();
        }
        Resource rDestParent = resourceFactory.getResource( host, parentPath);
        if( rDestParent == null ) throw new BadRequestException( wrapped, "The destination parent does not exist");
        if(rDestParent instanceof CollectionResource ) {
            CollectionResource colDestParent = (CollectionResource) rDestParent;
            if( colDestParent.child( pDest.getName()) == null ) {
                try {
                    wrapped.moveTo( colDestParent, pDest.getName() );
                } catch( ConflictException ex ) {
                    log.warn( "Exception copying to: " + pDest.getName(), ex);
                    throw new BadRequestException( rDestParent, "conflict: " + ex.getMessage());
                }
                return null;
            } else {
                log.warn( "destination already exists: " + pDest.getName() + " in folder: " + colDestParent.getName());
                throw new BadRequestException( rDestParent, "File already exists");
            }
        } else {
            throw new BadRequestException( wrapped, "The destination parent is not a collection resource");
        }
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        // nothing to do
    }

    @Override
    public Method applicableMethod() {
        return Method.MOVE;
    }

}
