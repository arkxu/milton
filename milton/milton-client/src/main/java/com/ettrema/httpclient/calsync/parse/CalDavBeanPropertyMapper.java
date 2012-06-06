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
package com.ettrema.httpclient.calsync.parse;

import com.ettrema.httpclient.calsync.parse.annotation.*;
import com.ettrema.httpclient.sync.PropertyAccessor;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class CalDavBeanPropertyMapper {

    private final Map<Class, Mapper> mapOfMappers;
    private final PropertyAccessor propertyAccessor;

    public CalDavBeanPropertyMapper(PropertyAccessor propertyAccessor) {
        this.propertyAccessor = propertyAccessor;
        mapOfMappers = new HashMap<Class, Mapper>();
        addMapper(Uid.class, new UidMapper());
        addMapper(Location.class, new LocationMapper());
        addMapper(Summary.class, new SummaryMapper());
        addMapper(Description.class, new DescriptionMapper());
        addMapper(EndDate.class, new EndDateMapper());
        addMapper(StartDate.class, new StartDateMapper());
        addMapper(Timezone.class, new TimezoneMapper());
        addMapper(Organizer.class, new OrganizerMapper());
    }

    private void addMapper(Class c, Mapper m) {
        mapOfMappers.put(c, m);
    }

    public void toBean(Object bean, String icalText) {
        ByteArrayInputStream fin = null;
        try {
            fin = new ByteArrayInputStream(icalText.getBytes("UTF-8"));
            CalendarBuilder builder = new CalendarBuilder();
            net.fortuna.ical4j.model.Calendar cal4jCalendar;
            try {
                cal4jCalendar = builder.build(fin);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ParserException ex) {
                throw new RuntimeException(ex);
            }
            PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(bean);
            for (PropertyDescriptor pd : pds) {
                if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                    Method read = pd.getReadMethod();
                    Annotation[] annotations = read.getAnnotations();
                    for (Annotation anno : annotations) {
                        Mapper mapper = mapOfMappers.get(anno.annotationType());
                        if (mapper != null) {
                            mapper.mapToBean(cal4jCalendar, bean, pd);
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(fin);
        }
    }

    /**
     * Find a property with the given annotation and return its value
     *
     * @param bean
     * @param annotationClass
     * @return
     */
    public <T> T getProperty(Object bean, Class annotationClass, Class<T> valueClass) {
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(bean);
        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                Method read = pd.getReadMethod();
                Annotation[] annotations = read.getAnnotations();
                for (Annotation anno : annotations) {
                    if (anno.annotationType() == annotationClass) {
                        return propertyAccessor.get(bean, read, valueClass);
                    }
                }
            }
        }
        return null;
    }

    public String toVCard(Object bean) {
        net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
        calendar.getProperties().add(new ProdId("-//spliffy.org//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        VEvent vevent = new VEvent();
        calendar.getComponents().add(vevent);

        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(bean);
        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                Method read = pd.getReadMethod();
                Annotation[] annotations = read.getAnnotations();
                for (Annotation anno : annotations) {
                    Mapper mapper = mapOfMappers.get(anno.annotationType());
                    if (mapper != null) {
                        mapper.mapToCard(calendar, bean, pd);
                    }
                }
            }
        }


        CalendarOutputter outputter = new CalendarOutputter();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            outputter.output(calendar, bout);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        }
        return bout.toString();
    }

    private VEvent event(net.fortuna.ical4j.model.Calendar cal) {
        return (VEvent) cal.getComponent("VEVENT");
    }

    private String getPropValue(Property prop) {
        if (prop == null) {
            return null;
        }
        return prop.getValue();
    }

    private net.fortuna.ical4j.model.Date getDateValue(DateProperty prop) {
        if (prop == null) {
            return null;
        }
        return prop.getDate();
    }

    public abstract class Mapper {

        abstract void mapToBean(net.fortuna.ical4j.model.Calendar calEvent, Object bean, PropertyDescriptor pd);

        abstract void mapToCard(net.fortuna.ical4j.model.Calendar calEvent, Object bean, PropertyDescriptor pd);
    }

    public class UidMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            Method m = pd.getWriteMethod();
            Property uidProp = vevent.getProperty("UID");
            String uid = null;
            if (uidProp != null) {
                uid = uidProp.getValue();
            }
            if (uid == null) {
                uid = UUID.randomUUID().toString();
            }
            propertyAccessor.set(bean, m, uid);
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            String uid = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            VEvent vevent = event(cal);
            vevent.getProperties().add(new net.fortuna.ical4j.model.property.Uid(uid));
        }
    }

    public class TimezoneMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            Method m = pd.getWriteMethod();
            String tzId = getPropValue(cal.getProperty(Property.TZID));
            propertyAccessor.set(bean, m, tzId);
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
            String tzId = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            TimeZone timezone = null;
            if (tzId != null && tzId.length() > 0) {
                timezone = registry.getTimeZone(tzId); // Eg Pacific/Auckland
            }
            VTimeZone tz = timezone.getVTimeZone();
            cal.getComponents().add(tz);
        }
    }

    public class LocationMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            String s = getPropValue(vevent.getLocation());
            propertyAccessor.set(bean, pd.getWriteMethod(), s);
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            VEvent vevent = event(cal);
            net.fortuna.ical4j.model.property.Location d = new net.fortuna.ical4j.model.property.Location(s);
            vevent.getProperties().add(d);
        }
    }    
    
    public class OrganizerMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            String s = getPropValue(vevent.getOrganizer());
            propertyAccessor.set(bean, pd.getWriteMethod(), s);
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            VEvent vevent = event(cal);
            net.fortuna.ical4j.model.property.Organizer d;
            try {
                d = new net.fortuna.ical4j.model.property.Organizer(s);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(s, ex);
            }
            vevent.getProperties().add(d);
        }
    }        
    
    public class DescriptionMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            String desc = getPropValue(vevent.getDescription());
            propertyAccessor.set(bean, pd.getWriteMethod(), desc);
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            VEvent vevent = event(cal);
            net.fortuna.ical4j.model.property.Description d = new net.fortuna.ical4j.model.property.Description();
            d.setValue(s);
            vevent.getProperties().add(d);
        }
    }

    public class SummaryMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            String s = getPropValue(vevent.getSummary());
            propertyAccessor.set(bean, pd.getWriteMethod(), s);

        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            net.fortuna.ical4j.model.property.Summary d = new net.fortuna.ical4j.model.property.Summary(s);
            vevent.getProperties().add(d);
        }
    }

    public class EndDateMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            Date dt = getDateValue(vevent.getEndDate());
            propertyAccessor.set(bean, pd.getWriteMethod(), dt);

        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            Date d = propertyAccessor.get(bean, pd.getReadMethod(), Date.class);
            net.fortuna.ical4j.model.Date dt = new net.fortuna.ical4j.model.Date(d);
            net.fortuna.ical4j.model.property.DtEnd p = new net.fortuna.ical4j.model.property.DtEnd(dt);
            vevent.getProperties().add(p);
        }
    }

    public class StartDateMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            Date dt = getDateValue(vevent.getStartDate());
            propertyAccessor.set(bean, pd.getWriteMethod(), dt);

        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            Date d = propertyAccessor.get(bean, pd.getReadMethod(), Date.class);
            net.fortuna.ical4j.model.Date dt = new net.fortuna.ical4j.model.Date(d);
            net.fortuna.ical4j.model.property.DtStart p = new net.fortuna.ical4j.model.property.DtStart(dt);
            vevent.getProperties().add(p);
        }
    }
}
