/*
 
   Derby - Class org.apache.derby.jdbc.EmbeddedXADataSource40
 
   Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
 */

package org.apache.derby.jdbc;

import java.sql.BaseQuery;
import java.sql.QueryObjectFactory;
import java.sql.QueryObjectGenerator;
import org.apache.derby.iapi.jdbc.ResourceAdapter;

import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

/**
 * This class is JDBC4.0 implementation of XADataSource
 * This class extends from  EmbeddedDataSource40 so it inherits the
 * new method introduced in ConnectionPoolDataSource from EmbeddedDataSource40
 */
public class EmbeddedXADataSource40 extends EmbeddedXADataSource {
    /** Creates a new instance of EmbeddedXADataSource40 */
    public EmbeddedXADataSource40() {
        super();
    }
        
    /**
     * returns null indicating that no driver specific implementation for 
     * QueryObjectGenerator available
     * @return null
     */
    public QueryObjectGenerator getQueryObjectGenerator() throws SQLException {
        return null;
    }
    
    /**
     * This method forwards all the calls to default query object provided by 
     * the jdk.
     * @param ifc interface to generated concreate class
     * @return concreat class generated by default qury object generator
     */
    public <T extends BaseQuery> T createQueryObject(Class<T> ifc) 
                                                    throws SQLException {
        return QueryObjectFactory.createDefaultQueryObject (ifc, this);
    } 
    
    /**
     * Intantiate and returns EmbedXAConnection.
     * @param user 
     * @param password 
     * @return XAConnection
     */
        protected XAConnection createXAConnection (ResourceAdapter ra, 
                String user, String password,
                boolean requestPassword)  throws SQLException {
            return new EmbedXAConnection40 (this, ra, user, 
                    password, requestPassword);
        }
}
