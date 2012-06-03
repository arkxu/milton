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
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * webDAV MOVE
 */
public interface MoveableResource  extends Resource {

    /**
     *
     * @param rDest is the destination folder to move to.
     * @param name is the new name of the moved resource
     * @throws ConflictException if the destination already exists, or the operation
     * could not be completed because of some other persisted state
     */
    void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException;
    
}
