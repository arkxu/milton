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

package com.bradmcevoy.http.http11;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.entity.PartialEntity;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.io.StreamUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PartialGetHelper {

	private static final Logger log = LoggerFactory.getLogger(PartialGetHelper.class);
	private final Http11ResponseHandler responseHandler;
	private int maxMemorySize = 100000;

	public PartialGetHelper(Http11ResponseHandler responseHandler) {
		this.responseHandler = responseHandler;
	}

	public List<Range> getRanges(String rangeHeader) {
		if (rangeHeader == null || rangeHeader.length() == 0) {
			log.trace("getRanges: no range header");
			return null;
		}
		if (rangeHeader.startsWith("bytes=")) {
			rangeHeader = rangeHeader.substring(6);
			String[] arr = rangeHeader.split(",");
			List<Range> list = new ArrayList<Range>();
			for (String s : arr) {
				final Matcher matcher = Pattern.compile("\\s*(\\d+)-(\\d+)").matcher(s);
				if (matcher.matches()) {
					Range r = new Range(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)));
					list.add(r);
				}
			}
			if (log.isTraceEnabled()) {
				log.trace("getRanges: header: " + rangeHeader + " parsed ranges: " + list.size());
			}
			return list;

		} else {
			return null;
		}
	}

	public void sendPartialContent(GetableResource resource, Request request, Response response, List<Range> ranges, Map<String, String> params) throws NotAuthorizedException, BadRequestException, IOException, NotFoundException {
		log.trace("sendPartialContent");
		if (ranges.size() == 1) {
			log.trace("partial get, single range");
			Range r = ranges.get(0);
			responseHandler.respondPartialContent(resource, response, request, params, r);
		} else {
			log.trace("partial get, multiple ranges");
			File temp = File.createTempFile("milton_partial_get", null);
			FileOutputStream fout = null;
			try {
				fout = new FileOutputStream(temp);
				BufferedOutputStream bufOut = new BufferedOutputStream(fout);
				resource.sendContent(bufOut, null, params, request.getContentTypeHeader());
				bufOut.flush();
				fout.flush();
			} finally {
				StreamUtils.close(fout);
			}
            response.setEntity(
               new PartialEntity(ranges, temp)
            );
		}
	}

	public int getMaxMemorySize() {
		return maxMemorySize;
	}

	public void setMaxMemorySize(int maxMemorySize) {
		this.maxMemorySize = maxMemorySize;
	}

}
