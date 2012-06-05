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

package com.ettrema.common;

import org.slf4j.Logger;


/**
 *
 * @author HP
 */
public class LogUtils {
    public static void trace(Logger log, Object ... args) {
        if( log.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            for(Object o : args) {
                sb.append(o).append(", ");
            }
            log.trace(sb.toString());
        }
    }
	
    public static void debug(Logger log, Object ... args) {
        if( log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for(Object o : args) {
                sb.append(o).append(", ");
            }
            log.debug(sb.toString());
        }
    }	
	
    public static void warn(Logger log, Object ... args) {
        if( log.isWarnEnabled()) {
            StringBuilder sb = new StringBuilder();
            for(Object o : args) {
                sb.append(o).append(", ");
            }
            log.warn(sb.toString());
        }
    }		
}
