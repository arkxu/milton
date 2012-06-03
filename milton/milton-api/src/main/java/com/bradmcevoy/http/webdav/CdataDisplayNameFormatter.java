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

import com.bradmcevoy.http.PropFindableResource;

/**
 * Decorator which wraps the underlying display name within a CDATA tag
 *
 * Provide the underlying DisplayNameFormatter as a constructor argument
 *
 * @author brad
 */
public class CdataDisplayNameFormatter implements DisplayNameFormatter{

    private final DisplayNameFormatter wrapped;

    public CdataDisplayNameFormatter( DisplayNameFormatter wrapped ) {
        this.wrapped = wrapped;
    }

    public String formatDisplayName( PropFindableResource res ) {
        return "<![CDATA[" + wrapped.formatDisplayName( res ) + "]]>";
    }

}
