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

package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Rn extends AbstractConsoleCommand {

    private static final Logger log = LoggerFactory.getLogger( Rn.class );

    public Rn( List<String> args, String host, String currentDir, ConsoleResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    @Override
    public Result execute() {
        try {
            String srcPath = args.get( 0 );
            String destName = args.get( 1 );
            log.debug( "rename: " + srcPath + "->" + destName );

            Path pSrc = Path.path( srcPath );

            Cursor sourceCursor = cursor.find( pSrc );
            Resource target = sourceCursor.getResource();

            if( target == null ) {
                log.debug( "target not found: " + srcPath );
                return result( "target not found: " + srcPath );
            } else {
                if( target instanceof MoveableResource ) {

                    CollectionResource currentParent = (CollectionResource) sourceCursor.getParent().getResource();
                    MoveableResource mv = (MoveableResource) target;
                    try {
                        mv.moveTo( currentParent, destName );
                    } catch( NotAuthorizedException e ) {
                        return result( "not authorised" );
                    } catch( BadRequestException e ) {
                        return result( "bad request" );
                    } catch( ConflictException ex ) {
                        return result( "conflict exception" );
                    }

                    Cursor newCursor = sourceCursor.getParent().find( destName );
                    return result( "created: <a href='" + newCursor.getPath() + "'>" + destName + "</a>" );
                } else {
                    return result( "resource is not moveable" );
                }
            }
        } catch (NotAuthorizedException ex) {
            log.error("not authorised", ex);
            return result(ex.getLocalizedMessage());
        } catch (BadRequestException ex) {
            log.error("bad req", ex);
            return result(ex.getLocalizedMessage());
        }
    }
}
