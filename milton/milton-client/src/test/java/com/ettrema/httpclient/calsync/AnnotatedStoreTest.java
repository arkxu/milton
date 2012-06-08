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
package com.ettrema.httpclient.calsync;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.httpclient.calsync.conflict.PolicyConflictManager;
import com.ettrema.httpclient.calsync.parse.CalDavBeanPropertyMapper;
import com.ettrema.httpclient.sync.PropertyAccessor;
import com.ettrema.httpclient.calsync.store.AnnotationCalendarStore;
import com.ettrema.httpclient.calsync.store.MemoryCalSyncStatusStore;
import com.ettrema.httpclient.calsync.store.MemoryCalendarEventFactory;
import com.ettrema.httpclient.calsync.store.MemoryCalendarEventFactory.EventBean;
import com.ettrema.httpclient.calsync.store.MemoryCalendarStore;
import java.util.Date;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class AnnotatedStoreTest extends TestCase {

    PropertyAccessor propertyAccessor = new PropertyAccessor();
    CalDavBeanPropertyMapper beanMapper = new CalDavBeanPropertyMapper(propertyAccessor);
    MemoryCalendarEventFactory calendarEventFactory = new MemoryCalendarEventFactory();
    
    AnnotationCalendarStore localStore;
    MemoryCalendarStore remoteStore;
    MemoryCalSyncStatusStore statusStore;
    DeltaListener deltaListener;
    PolicyConflictManager conflictManager;
    CalendarDeltaGenerator deltaGenerator;

    @Override
    protected void setUp() throws Exception {
        localStore = new AnnotationCalendarStore("local", beanMapper, calendarEventFactory);
        remoteStore = new MemoryCalendarStore("remote");
        statusStore = new MemoryCalSyncStatusStore();
        deltaListener = new SyncingDeltaListener();
        conflictManager = new PolicyConflictManager();
        conflictManager.setConflictPolicy(PolicyConflictManager.ConflictPolicy.SERVER_WINS);
        deltaGenerator = new CalendarDeltaGenerator(localStore, remoteStore, statusStore, deltaListener, conflictManager);

    }

    public void atest_BothEmpty() throws NotAuthorizedException, BadRequestException {
        assertEquals(0, localStore.getChildren().size());
        assertEquals(0, remoteStore.getChildren().size());

        deltaGenerator.compareCalendars();

        assertEquals(0, localStore.getChildren().size());
        assertEquals(0, remoteStore.getChildren().size());
    }

    public void atest_RemotelyNew() throws NotAuthorizedException, BadRequestException {
        remoteStore.getChildren().add(new MemoryCalendarStore.MemoryCalSyncEvent(1, "a", "ical", new Date()));

        assertEquals(1, remoteStore.getChildren().size());
        assertEquals(0, localStore.getChildren().size());

        deltaGenerator.compareCalendars();

        assertEquals(1, remoteStore.getChildren().size());
        assertEquals(1, localStore.getChildren().size());
        assertEquals(1, statusStore.getEtags().size());
        assertEquals(1, statusStore.getCtags().size());

        // run it again to ensure nothing changes
        localStore.setReadonly(true);
        remoteStore.setReadonly(true);
        deltaGenerator.compareCalendars();

    }

    public void test_LocallyNew() throws NotAuthorizedException, BadRequestException {
        addLocalEventBean("a", "joe", "smith");

        assertEquals(0, remoteStore.getChildren().size());
        assertEquals(1, localStore.getChildren().size());

        deltaGenerator.compareCalendars();

        assertEquals(1, remoteStore.getChildren().size());
        assertEquals(1, localStore.getChildren().size());
        assertEquals(1, statusStore.getEtags().size());
        assertEquals(1, statusStore.getCtags().size());

        // run it again to ensure nothing changes
        localStore.setReadonly(true);
        remoteStore.setReadonly(true);
        deltaGenerator.compareCalendars();

    }

    public void atest_LocallyUpdated() throws NotAuthorizedException, BadRequestException {
        // First prime the sync status
        EventBean localEvent = addLocalEventBean("a", "joe", "smith");
        deltaGenerator.compareCalendars();

        // check sync status and remote store were primed
        assertEquals(1, remoteStore.getChildren().size());
        assertEquals(1, statusStore.getEtags().size());
        MemoryCalendarStore.MemoryCalSyncEvent remoteEvent = (MemoryCalendarStore.MemoryCalSyncEvent) remoteStore.getChildren().get(0);
        assertEquals("ical", remoteEvent.getIcalText());

        // Now update local event
        localEvent.setFirstName("XXXX");
        calendarEventFactory.saveAndUpdateTags("local", localEvent);

        // Perform the update
        deltaGenerator.compareCalendars();

        // And check remote event was updated
        remoteEvent = (MemoryCalendarStore.MemoryCalSyncEvent) remoteStore.getChildren().get(0);
        assertTrue(remoteEvent.getIcalText().contains("XXXX"));
    }

    public void atest_RemotelyUpdated() throws NotAuthorizedException, BadRequestException {
        // First prime the sync status
        MemoryCalendarStore.MemoryCalSyncEvent remoteEvent = new MemoryCalendarStore.MemoryCalSyncEvent(1, "a", "ical", new Date());
        remoteStore.getChildren().add(remoteEvent);
        deltaGenerator.compareCalendars();

        // check sync status and remote store were primed
        assertEquals(1, localStore.getChildren().size());
        assertEquals(1, statusStore.getEtags().size());
        MemoryCalendarStore.MemoryCalSyncEvent localEvent = (MemoryCalendarStore.MemoryCalSyncEvent) localStore.getChildren().get(0);
        assertEquals("ical", remoteEvent.getIcalText());

        // Now update remote event
        remoteStore.setICalData(localEvent, "XXXX");

        // Perform the update
        deltaGenerator.compareCalendars();

        // And check remote event was updated
        localEvent = (MemoryCalendarStore.MemoryCalSyncEvent) localStore.getChildren().get(0);
        assertEquals("XXXX", localEvent.getIcalText());
    }

    public void atest_LocallyDeleted() throws NotAuthorizedException, BadRequestException {
        // First prime the sync status
        EventBean localEvent = addLocalEventBean("a", "joe", "smith");
        deltaGenerator.compareCalendars();
        assertEquals(1, remoteStore.getChildren().size());

        // Now delete local event
        calendarEventFactory.deleteEvent("local", localEvent.getName());

        // Perform the update
        deltaGenerator.compareCalendars();

        // And check remote event was deleted
        assertEquals(0, remoteStore.getChildren().size());
        assertEquals(0, statusStore.getEtags().size());
    }

    public void atest_RemotelyDeleted() throws NotAuthorizedException, BadRequestException {
        // First prime the sync status
        MemoryCalendarStore.MemoryCalSyncEvent remoteEvent = new MemoryCalendarStore.MemoryCalSyncEvent(1, "a", "ical", new Date());
        remoteStore.getChildren().add(remoteEvent);
        deltaGenerator.compareCalendars();
        assertEquals(1, localStore.getChildren().size());

        // Now delete local event
        remoteStore.deleteEvent(remoteEvent);

        // Perform the update
        deltaGenerator.compareCalendars();

        // And check remote event was deleted
        assertEquals(0, localStore.getChildren().size());
        assertEquals(0, statusStore.getEtags().size());
    }

    public void atest_Conflict() throws NotAuthorizedException, BadRequestException {
        // First prime the sync status
        MemoryCalendarStore.MemoryCalSyncEvent remoteEvent = new MemoryCalendarStore.MemoryCalSyncEvent(1, "a", "ical", new Date());
        remoteStore.getChildren().add(remoteEvent);
        deltaGenerator.compareCalendars();
        assertEquals(1, localStore.getChildren().size());
        MemoryCalendarStore.MemoryCalSyncEvent localEvent = (MemoryCalendarStore.MemoryCalSyncEvent) localStore.getChildren().get(0);

        // Now update both local and remote events
        remoteStore.setICalData(remoteEvent, "remoteMod");
        localStore.setICalData(localEvent, "localMod");

        // Perform the update
        deltaGenerator.compareCalendars();

        // make sure there was a conflict detected
        assertEquals(1, conflictManager.getNumConflicts());

        // check we havent added or removed anything
        assertEquals(1, localStore.getChildren().size());
        assertEquals(1, remoteStore.getChildren().size());
        assertEquals(1, statusStore.getEtags().size());

        // we're usign server wins, so local event should have server change
        localEvent = (MemoryCalendarStore.MemoryCalSyncEvent) localStore.getChildren().get(0);
        assertEquals("remoteMod", localEvent.getIcalText());
    }
    
    private EventBean addLocalEventBean(String name, String firstName, String lastName) {
        EventBean b = calendarEventFactory.add("local", name);
        b.setFirstName(firstName);
        b.setLastName(lastName);
        calendarEventFactory.saveAndUpdateTags("local", b);
        return b;
    }
    
}
