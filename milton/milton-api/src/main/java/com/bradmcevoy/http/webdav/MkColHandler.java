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

package com.bradmcevoy.http.webdav;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.event.NewFolderEvent;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MkColHandler implements Handler {

	private static final Logger log = LoggerFactory.getLogger(MkColHandler.class);
	private final WebDavResponseHandler responseHandler;
	private final HandlerHelper handlerHelper;
	private CollectionResourceCreator collectionResourceCreator = new DefaultCollectionResourceCreator();

	public MkColHandler(WebDavResponseHandler responseHandler, HandlerHelper handlerHelper) {
		this.responseHandler = responseHandler;
		this.handlerHelper = handlerHelper;
	}

	@Override
	public String[] getMethods() {
		return new String[]{Method.MKCOL.code};
	}

	@Override
	public boolean isCompatible(Resource r) {
		return (r instanceof MakeCollectionableResource);
	}

	@Override
	public void process(HttpManager manager, Request request, Response response) throws ConflictException, NotAuthorizedException, BadRequestException {
		try {
			process(manager, request, response, collectionResourceCreator);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void process(HttpManager manager, Request request, Response response, CollectionResourceCreator creator) throws ConflictException, NotAuthorizedException, BadRequestException, IOException {
		if (!handlerHelper.checkExpects(responseHandler, request, response)) {
			return;
		}
		String host = request.getHostHeader();
		String finalurl = HttpManager.decodeUrl(request.getAbsolutePath());
		String name;
		if (log.isDebugEnabled()) {
			log.debug("process request: host: " + host + " url: " + finalurl);
		}

		Path finalpath = Path.path(finalurl); //this is the parent collection it goes in
		name = finalpath.getName();
		Path parent = finalpath.getParent();
		String parenturl = parent.toString();

		Resource parentcol = manager.getResourceFactory().getResource(host, parenturl);
		if (parentcol != null) {
			log.debug("process: resource: " + parentcol.getClass().getName());

			if (handlerHelper.isLockedOut(request, parentcol)) {
				log.warn("isLockedOut");
				response.setStatus(Status.SC_LOCKED);
				return;
			}
			Resource dest = manager.getResourceFactory().getResource(host, finalpath.toString());

			if (dest != null && handlerHelper.isLockedOut(request, dest)) {
				responseHandler.respondLocked(request, response, dest);
				return;
			} else if (handlerHelper.missingLock(request, parentcol)) {
				response.setStatus(Status.SC_PRECONDITION_FAILED); //notowner_modify wants this code here
				return;
			}

			if (parentcol instanceof CollectionResource) {
				CollectionResource col = (CollectionResource) parentcol;
				processMakeCol(manager, request, response, col, name, creator);
			} else {
				log.warn("parent collection is no a CollectionResource: " + parentcol.getName());
				responseHandler.respondConflict(parentcol, response, request, "not a collection");
			}

		} else {
			log.warn("parent does not exist: " + parenturl);
			manager.getResponseHandler().respondConflict(parentcol, response, request, name);
		}
	}

	private void processMakeCol(HttpManager manager, Request request, Response response, CollectionResource resource, String newName, CollectionResourceCreator creator) throws ConflictException, NotAuthorizedException, BadRequestException, IOException {
		if (!handlerHelper.checkAuthorisation(manager, resource, request)) {
			responseHandler.respondUnauthorised(resource, response, request);
			return;
		}

		handlerHelper.checkExpects(responseHandler, request, response);

		MakeCollectionableResource existingCol = (MakeCollectionableResource) resource;
		try {
			//For litmus test and RFC support
			if (request.getInputStream().read() > -1) //This should be empty
			{
				response.setStatus(Response.Status.SC_UNSUPPORTED_MEDIA_TYPE);
				return;
			}
		} catch (Exception ex) {
			//Per RFC2518 MKCOL request-content is undefined and it is therefore MANDATORY to return 415 if it exists.
			if (request.getContentLengthHeader() > 0) {
				response.setStatus(Response.Status.SC_UNSUPPORTED_MEDIA_TYPE);
				return;
			}
		}
		
		if( !isCompatible(existingCol)) {
			responseHandler.respondMethodNotImplemented(existingCol, response, request);
			return ;
		}
		
		Resource existingChild = existingCol.child(newName);
		if (existingChild != null) {
			log.warn("item already exists: " + existingChild.getName());
			//throw new ConflictException( existingChild );
			// See http://www.ettrema.com:8080/browse/MIL-86
			// 405 (Method Not Allowed) - MKCOL can only be executed on a deleted/non-existent resource.
			responseHandler.respondMethodNotAllowed(existingChild, response, request);
			return;
		}
		CollectionResource made = creator.createResource(existingCol, newName, request);
		if (made == null) {
			log.warn("createCollection returned null. In resource class: " + existingCol.getClass());
			response.setStatus(Response.Status.SC_METHOD_NOT_ALLOWED);
		} else {
			manager.getEventManager().fireEvent(new NewFolderEvent(resource));
			response.setStatus(Response.Status.SC_CREATED);
		}
	}

	public interface CollectionResourceCreator {

		CollectionResource createResource(MakeCollectionableResource existingCol, String newName, Request request) throws ConflictException, NotAuthorizedException, BadRequestException, IOException;
	}

	private class DefaultCollectionResourceCreator implements CollectionResourceCreator {

		@Override
		public CollectionResource createResource(MakeCollectionableResource existingCol, String newName, Request request) throws ConflictException, NotAuthorizedException, BadRequestException, IOException {
			CollectionResource made = existingCol.createCollection(newName);
			return made;
		}
	}
}
