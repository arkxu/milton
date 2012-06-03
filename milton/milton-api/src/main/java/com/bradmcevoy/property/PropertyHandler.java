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

package com.bradmcevoy.property;

import com.bradmcevoy.http.Handler;

/**
 * A type of method handler which  does property permission checking.
 *
 * This interface is just so that the HTTP manager can inject the permission
 * property service
 *
 * @author brad
 */
public interface PropertyHandler extends Handler{
    public PropertyAuthoriser getPermissionService();

    public void setPermissionService( PropertyAuthoriser permissionService );
}
