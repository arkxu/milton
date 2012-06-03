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

import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.http.webdav.PropFindResponse;
import com.bradmcevoy.http.webdav.PropFindXmlGeneratorHelper;
import java.util.Map;

/**
 *
 * @author bradm
 */
public class PropFindResponseListWriter  implements ValueWriter {

	private final PropFindXmlGeneratorHelper propFindXmlGeneratorHelper;

	public PropFindResponseListWriter(PropFindXmlGeneratorHelper propFindXmlGeneratorHelper) {
		this.propFindXmlGeneratorHelper = propFindXmlGeneratorHelper;
	}
	
	
	@Override
	public boolean supports(String nsUri, String localName, Class c) {
		return PropFindResponseList.class.isAssignableFrom(c);
	}

	@Override
	public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {
		Element outerEl = writer.begin(prefix, localName).open();
		PropFindResponseList list = (PropFindResponseList) val;
		if (list != null) {
			for (PropFindResponse s : list) {
				propFindXmlGeneratorHelper.appendResponse(writer, s, nsPrefixes);
			}
		}
		outerEl.close();
	}

	@Override
	public Object parse(String namespaceURI, String localPart, String value) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
