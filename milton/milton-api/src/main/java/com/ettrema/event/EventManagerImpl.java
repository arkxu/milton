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

package com.ettrema.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class EventManagerImpl implements EventManager {

    private final static Logger log = LoggerFactory.getLogger( EventManagerImpl.class );
    private final Map<Class, List<EventListener>> listenersMap = new HashMap<Class, List<EventListener>>();

    @Override
    public void fireEvent( Event e ) {
        if( log.isTraceEnabled() ) {
            log.trace( "fireEvent: " + e.getClass().getCanonicalName() );
        }
        List<EventListener> list = listenersMap.get( e.getClass() );
        if( list == null ) return;
        for( EventListener l : Collections.unmodifiableCollection(list) ) {
            if( log.isTraceEnabled() ) {
                log.trace( "  firing on: " + l.getClass() );
            }
            l.onEvent( e );
        }
    }

    @Override
    public synchronized <T extends Event> void registerEventListener( EventListener l, Class<T> c ) {
        log.info( "registerEventListener: " + l.getClass().getCanonicalName() + " - " + c.getCanonicalName() );
        List<EventListener> list = listenersMap.get( c );
        if( list == null ) {
            list = new ArrayList<EventListener>();
            listenersMap.put( c, list );
        }
        list.add( l );
    }
}
