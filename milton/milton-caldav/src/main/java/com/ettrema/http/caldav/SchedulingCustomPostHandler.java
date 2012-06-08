/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package com.ettrema.http.caldav;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.entity.StringEntity;
import com.bradmcevoy.http.http11.CustomPostHandler;
import com.ettrema.http.SchedulingOutboxResource;
import com.ettrema.http.SchedulingResponseItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulingCustomPostHandler implements CustomPostHandler {

    private static final Logger log = LoggerFactory.getLogger(SchedulingCustomPostHandler.class);
    private final SchedulingXmlHelper schedulingHelper = new SchedulingXmlHelper();

    @Override
    public boolean supports(Resource resource, Request request) {
        boolean b = resource instanceof SchedulingOutboxResource && contentTypeIsCalendar(request);
        log.trace("supports: " + b);
        return b;
    }

    @Override
    public void process(Resource resource, Request request, Response response) {
        log.trace("process");
        try {
            SchedulingOutboxResource outbox = (SchedulingOutboxResource) resource;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(request.getInputStream(), bout);
            String iCalText = bout.toString("UTF-8");
            log.trace(iCalText);
            List<SchedulingResponseItem> respItems = outbox.queryFreeBusy(iCalText);

            String xml = schedulingHelper.generateXml(respItems);

            response.setStatus(Response.Status.SC_OK);
            response.setDateHeader(new Date());
            response.setContentTypeHeader("application/xml; charset=\"utf-8\"");
            response.setContentLengthHeader((long)xml.length());
            response.setEntity(new StringEntity(xml));
            // TODO: THIS IS NOT CALLED WITHIN THE STANDARDFILTER? DO WE NEED TO FLUSH HERE AGAIN?
            //response.close();


        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean contentTypeIsCalendar(Request r) {
        String s = r.getContentTypeHeader();
        return "text/calendar".equals(s);
    }
}
