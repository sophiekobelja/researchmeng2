/*
 
   Derby - Class org.apache.derby.client.net.NetConnection40
 
   Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 
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

package org.apache.derby.client.net;

import org.apache.derby.client.am.SQLExceptionFactory;
import org.apache.derby.client.am.SqlException;
import org.apache.derby.jdbc.InternalDriver;
import java.sql.Blob;
import java.sql.ClientInfoException;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Properties;


public class  NetConnection40 extends org.apache.derby.client.net.NetConnection {
    
    /*
     *-------------------------------------------------------
     * JDBC 4.0 
     *-------------------------------------------------------
    */

    public NetConnection40(NetLogWriter netLogWriter,
                         String databaseName,
                         java.util.Properties properties) throws SqlException {
	super(netLogWriter,databaseName,properties);
    }
    public NetConnection40(NetLogWriter netLogWriter,
                         org.apache.derby.jdbc.ClientDataSource dataSource,
                         String user,
                         String password) throws SqlException {
	super(netLogWriter,dataSource,user,password);
    }
     public NetConnection40(NetLogWriter netLogWriter,
                         int driverManagerLoginTimeout,
                         String serverName,
                         int portNumber,
                         String databaseName,
                         java.util.Properties properties) throws SqlException{
	super(netLogWriter,driverManagerLoginTimeout,serverName,portNumber,databaseName,properties);
     }
     public NetConnection40(NetLogWriter netLogWriter,
                         String user,
                         String password,
                         org.apache.derby.jdbc.ClientDataSource dataSource,
                         int rmId,
                         boolean isXAConn) throws SqlException{
	super(netLogWriter,user,password,dataSource,rmId,isXAConn);
    }
    public NetConnection40(NetLogWriter netLogWriter,
                         String ipaddr,
                         int portNumber,
                         org.apache.derby.jdbc.ClientDataSource dataSource,
                         boolean isXAConn) throws SqlException{
        super(netLogWriter,ipaddr,portNumber,dataSource,isXAConn);
    }
    

    
    
    public Clob createClob() throws SQLException {
        throw SQLExceptionFactory.notImplemented ("createClob ()");
    }

    public Blob createBlob() throws SQLException {
        throw SQLExceptionFactory.notImplemented ("createBlob ()");
    }
    
    public NClob createNClob() throws SQLException {
        throw SQLExceptionFactory.notImplemented ("createNClob ()");
    }

    public SQLXML createSQLXML() throws SQLException {
        throw SQLExceptionFactory.notImplemented ("createSQLXML ()");
    }

    public boolean isValid(int timeout) throws SQLException {
        throw SQLExceptionFactory.notImplemented ("isValid ()");
    }

    public void setClientInfo(String name, String value)
		throws SQLException{
	throw SQLExceptionFactory.notImplemented ("setClientInfo (String, String)");
    }
	
    public void setClientInfo(Properties properties)
		throws ClientInfoException {
	SQLException temp= SQLExceptionFactory.notImplemented ("setClientInfo ()");
	ClientInfoException clientInfoException = new ClientInfoException
	(temp.getMessage(),temp.getSQLState(),(Properties) null);
	throw clientInfoException; 
    }
	
    public String getClientInfo(String name)
		throws SQLException{
	throw SQLExceptionFactory.notImplemented ("getClientInfo (String)");
    }
	
    public Properties getClientInfo()
		throws SQLException{
	throw SQLExceptionFactory.notImplemented ("getClientInfo (Properties)");
    }
    
    public <T> T createQueryObject(Class<T> ifc) throws SQLException {
        throw SQLExceptionFactory.notImplemented ("createQueryObject (Class<T>)");
    }
    
    public java.util.Map<String,Class<?>> getTypeMap(){
        throw new java.lang.UnsupportedOperationException("getTypeMap()");
    }
    
}
