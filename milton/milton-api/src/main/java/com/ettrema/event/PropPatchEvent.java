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

package com.ettrema.event;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.webdav.PropFindResponse;

/**
 *
 * @author brad
 */
public class PropPatchEvent implements ResourceEvent {
    private final Resource res;
    private final PropFindResponse resp;

    public PropPatchEvent( Resource res, PropFindResponse resp ) {
        this.res = res;
        this.resp = resp;
    }


    @Override
    public Resource getResource() {
        return res;
    }

    public PropFindResponse getResponse() {
        return resp;
    }



}
