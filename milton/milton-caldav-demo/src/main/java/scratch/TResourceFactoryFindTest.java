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


package scratch;

import com.bradmcevoy.http.Resource;
import com.ettrema.http.caldav.demo.TResourceFactory;

/**
 *
 * @author alex
 */
public class TResourceFactoryFindTest {
  public static void main(String[] args)
  {
    TResourceFactory factory = new TResourceFactory();
    Resource resource = factory.getResource(null, "http://localhost:9080/calendarHome/calendarOne/");
    System.out.println("FOUND : "+resource);
  }
}
