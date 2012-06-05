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

package com.ettrema.http.caldav;

import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.webdav.PropFindPropertyBuilder;
import com.bradmcevoy.http.webdav.PropFindResponse;
import com.bradmcevoy.http.webdav.PropFindXmlGenerator;
import com.bradmcevoy.http.webdav.PropertiesRequest;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.ettrema.http.report.Report;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MultiGetReport implements Report {

    private static final Logger log = LoggerFactory.getLogger(MultiGetReport.class);
    private final ResourceFactory resourceFactory;
    private final PropFindPropertyBuilder propertyBuilder;
    private final PropFindXmlGenerator xmlGenerator;
    private final Namespace NS_DAV = Namespace.getNamespace(WebDavProtocol.NS_DAV.getPrefix(), WebDavProtocol.NS_DAV.getName());

    public MultiGetReport(ResourceFactory resourceFactory, PropFindPropertyBuilder propertyBuilder, PropFindXmlGenerator xmlGenerator) {
        this.resourceFactory = resourceFactory;
        this.propertyBuilder = propertyBuilder;
        this.xmlGenerator = xmlGenerator;
    }

    @Override
    public String getName() {
        return "calendar-multiget";
    }

    @Override
    public String process(String host, String path, Resource calendar, Document doc) throws NotAuthorizedException, BadRequestException {
        log.debug("process report: " + getName());
        // The requested properties
        Set<QName> props = getProps(doc);
        // The requested resources
        List<String> hrefs = getHrefs(doc);

        PropertiesRequest parseResult = PropertiesRequest.toProperties(props);

        // Generate the response
        List<PropFindResponse> respProps = new ArrayList<PropFindResponse>();

        for (String href : hrefs) {
            Resource r = resourceFactory.getResource(host, href);
            if (r != null) {
                if (r instanceof PropFindableResource) {
                    PropFindableResource pfr = (PropFindableResource) r;
                    try {
                        respProps.addAll(propertyBuilder.buildProperties(pfr, 0, parseResult, href));
                    } catch (URISyntaxException ex) {
                        throw new RuntimeException("There was an unencoded url requested: " + href, ex);
                    }
                } else {
                    log.warn("requested href is for a non PropFindableResource: " + r.getClass() + " - " + href);
                }
            } else {
                log.warn("requested href not found: " + href + " on resource factory: " + resourceFactory.getClass());
            }
        }

        String xml = xmlGenerator.generate(respProps);
        return xml;
    }

    private List<String> getHrefs(Document doc) {
        List<String> list = new ArrayList<String>();
        for (Object o : doc.getRootElement().getChildren()) {
            if (o instanceof Element) {
                Element el = (Element) o;
                if (el.getName().equals("href")) {
                    String href = el.getText();
                    list.add(href);
                    if( log.isTraceEnabled()) {
                        log.trace(" href: " + href);
                    }
                }
            }
        }
        return list;
    }

    private Set<QName> getProps(Document doc) {
        Element elProp = doc.getRootElement().getChild("prop", NS_DAV);
        if (elProp == null) {
            throw new RuntimeException("No prop element");
        }

        Set<QName> set = new HashSet<QName>();
        for (Object o : elProp.getChildren()) {
            if (o instanceof Element) {
                Element el = (Element) o;
                String local = el.getName();
                String ns = el.getNamespaceURI();
                set.add(new QName(ns, local, el.getNamespacePrefix()));
            }
        }
        return set;
    }
}
