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

package com.bradmcevoy.http.values;

import com.bradmcevoy.http.Utils;
import com.bradmcevoy.http.XmlWriter;
import java.util.Map;

public class ToStringValueWriter implements ValueWriter {

    public boolean supports( String nsUri, String localName, Class c ) {
        return true;
    }

    private String nameEncode( String s ) {
        //return Utils.encode(href, false); // see MIL-31
        return Utils.escapeXml( s );
    }

    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        if( val == null ) {
            writer.writeProperty( prefix, localName );
        } else {
            String s = nameEncode( val.toString() );
            writer.writeProperty( prefix, localName, s );
        }
    }

    public Object parse( String namespaceURI, String localPart, String value ) {
        return value;
    }
}
