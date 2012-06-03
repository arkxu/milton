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

package com.ettrema.http.fck;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FckResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger(FckResourceFactory.class);

    private final ResourceFactory wrappedFactory;

    public FckResourceFactory(ResourceFactory wrappedFactory) {
        this.wrappedFactory = wrappedFactory;
    }

    @Override
    public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException {
        Path path = Path.path(url);
        if (FckFileManagerResource.URL.equals(path)) {
            CollectionResource h = getParent(host, path.getParent());
            if( h == null ) return null;
            FckFileManagerResource fck = new FckFileManagerResource(h);
            return fck;
        } else if (FckQuickUploaderResource.URL.equals(path)) {
            CollectionResource h = getParent(host, path.getParent());
            if( h == null ) return null;
            FckQuickUploaderResource fck = new FckQuickUploaderResource(h);
            return fck;
        } else {
            return null;
        }
    }
    

    private CollectionResource getParent( String host, Path path ) throws NotAuthorizedException, BadRequestException {
        Resource r = wrappedFactory.getResource( host, path.toString() );
        if( r instanceof CollectionResource ) {
            return (CollectionResource) r;
        } else {
            log.warn( "Could not locate a CollectionResource at: http://" + host + "/" + path);
            return null;
        }
    }
        
}
