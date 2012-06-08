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

package com.mycompany;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.bradmcevoy.http.CustomProperty;
import com.bradmcevoy.http.CustomPropertyResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.ReplaceableResource;

/**
 * Holds binary files like PDFs, jpeg, etc
 *
 * Demonstrates implementing CustomPropertyResource
 *
 * @author brad
 */
public class TBinaryResource extends TResource implements CustomPropertyResource, ReplaceableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TBinaryResource.class);
    byte[] bytes;
    String contentType;
    private Map<String, String> props = new HashMap<String, String>();

    public TBinaryResource(TFolderResource parent, String name, byte[] bytes, String contentType) {
        super(parent, name);
        this.bytes = bytes;
        props.put("someField", "hash:" + this.hashCode());
    }

    @Override
    protected Object clone(TFolderResource newParent) {
        return new TBinaryResource(newParent, name, bytes, contentType);
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        System.out.println("writing binary resource:");
        out.write(bytes);
        System.out.println("wrote bytes: " + bytes.length);
    }

    @Override
    public Long getContentLength() {
        return (long) bytes.length;
    }

    @Override
    public String getContentType(String accept) {
        return contentType;
    }

    public CustomProperty getProperty(String name) {
        if (props.containsKey(name)) {
            return new TResCustomProperty(name);
        } else {
            return null;
        }
    }

    public String getNameSpaceURI() {
        return "http://milton.ettrema.com/demo";
    }

    public Set<String> getAllPropertyNames() {
        return this.props.keySet();
    }

    public void replaceContent(InputStream in, Long length) {
        try {
            ByteArrayOutputStream bos = TFolderResource.readStream(in);
            byte[] newBytes = bos.toByteArray();
            if (length != null) {
                if (newBytes.length != length) {
                    throw new RuntimeException("data corruption. data does not equal expected content length");
                }
            }
            this.bytes = newBytes;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    public class TResCustomProperty implements CustomProperty {

        private final String key;

        public TResCustomProperty(String key) {
            this.key = key;
        }

        public Object getTypedValue() {
            return props.get(key);
        }

        public String getFormattedValue() {
            return props.get(key);
        }

        public void setFormattedValue(String s) {
            log.debug("set value: " + key + " to: " + s);
            props.put(key, s);
        }

        public Class getValueClass() {
            return String.class;
        }
    }
}
