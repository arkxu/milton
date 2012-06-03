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

package com.bradmcevoy.http.entity;

import com.bradmcevoy.http.Response;

import java.io.OutputStream;
import java.io.PrintWriter;

public class StringEntity implements Response.Entity{

    private String string;

    public StringEntity(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    @Override
    public void write(Response response, OutputStream outputStream) throws Exception {
        PrintWriter pw = new PrintWriter(outputStream, true);
        pw.print(string);
        pw.flush();
    }
}
