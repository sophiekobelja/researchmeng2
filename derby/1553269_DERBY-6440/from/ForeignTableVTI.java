/*

   Derby - Class org.apache.derby.vti.ForeignTableVTI

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.derby.vti;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.derby.iapi.services.io.ArrayUtil;
import org.apache.derby.iapi.util.IdUtil;

/**
 * <p>
 * This class contains a table function which can be used to bulk-import data
 * from a foreign database. Because the table function is a RestrictedVTI, it
 * can also be used to periodically and efficiently integrate data streams from
 * a foreign database.
 * </p>
 *
 * <p>
 * If you need to siphon data out of the foreign database on an ongoing basis, you
 * can restrict the data you SELECT. Note that the local views are backed by
 * RestrictedVTIs. That means that the actual query sent to the foreign database
 * will only involve the columns you SELECT. In addition, the query will include the WHERE clause,
 * provided that it is simple enough (see the javadoc for RestrictedVTI):
 * </p>
 *
 * <p>
 * The following script shows how to use this table function:
 * </p>
 *
 * <pre>
 * -- create a foreign database with a table in it
 * connect 'jdbc:derby:memory:db;create=true;user=test_dbo;password=test_dbopassword';
 * 
 * call syscs_util.syscs_create_user( 'test_dbo', 'test_dbopassword' );
 * 
 * create table employee
 * (
 *     firstName   varchar( 50 ),
 *     lastName    varchar( 50 ),
 *     employeeID  int primary key
 * );
 * 
 * insert into employee values ( 'Billy', 'Goatgruff', 1 );
 * insert into employee values ( 'Mary', 'Hadalittlelamb', 2 );
 * 
 * connect 'jdbc:derby:memory:db;shutdown=true';
 * 
 * -- now create the database where we will do our work
 * connect 'jdbc:derby:memory:db1;create=true';
 * 
 * -- register a table function with the shape of the foreign table
 * create function employeeFunction
 * (
 *     schemaName  varchar( 32672 ),
 *     tableName   varchar( 32672 ),
 *     connectionURL        varchar( 32672 )
 * )
 * returns table
 * (
 *     firstName   varchar( 50 ),
 *     lastName    varchar( 50 ),
 *     employeeID  int    
 * )
 * language java parameter style derby_jdbc_result_set no sql
 * external name 'org.apache.derby.vti.ForeignTableVTI.readForeignTable'
 * ;
 * 
 * -- create a convenience view to factor out the function parameters
 * create view foreignEmployee
 * as select firstName, lastName, employeeID
 * from table
 * (
 *     employeeFunction
 *     (
 *         'TEST_DBO',
 *         'EMPLOYEE',
 *         'jdbc:derby:memory:db;user=test_dbo;password=test_dbopassword'
 *     )
 * ) s;
 * 
 * -- now select from the view as though it were a local table
 * select * from foreignEmployee;
 * select lastName from foreignEmployee where employeeID = 2;
 * </pre>
 */
public	class   ForeignTableVTI extends VTITemplate implements  RestrictedVTI
{
    ////////////////////////////////////////////////////////////////////////
    //
    //	CONSTANTS
    //
    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    //
    //	STATE
    //
    ////////////////////////////////////////////////////////////////////////

    private static  HashMap<String,Connection> _connections = new HashMap<String,Connection>();

    private String  _foreignSchemaName;
    private String  _foreignTableName;
    private String  _connectionURL;

    private String[]    _columnNames;
    private Restriction _restriction;

    // this maps Derby columns (0-based) to foreign column numbers (1-based) in
    // the actual query
    private int[]               _columnNumberMap;
    private PreparedStatement   _foreignPreparedStatement;
    private ResultSet           _foreignResultSet;

    ////////////////////////////////////////////////////////////////////////
    //
    //	CONSTRUCTOR
    //
    ////////////////////////////////////////////////////////////////////////

    protected  ForeignTableVTI
        (
         String foreignSchemaName,
         String foreignTableName,
         String connectionURL
         )
    {
        _foreignSchemaName = foreignSchemaName;
        _foreignTableName = foreignTableName;
        _connectionURL = connectionURL;
    }

    ////////////////////////////////////////////////////////////////////////
    //
    //	TABLE FUNCTION
    //
    ////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Table function to read a table in a foreign database.
     * </p>
     *
     * @param   foreignSchemaName   Case-sensitive name of foreign schema
     * @param   foreignTableName    Case-sensitive name of foreign table
     * @param   connectionURL       URL for connecting to foreign database via DriverManager.getConnection()
     */
    public  static  ForeignTableVTI readForeignTable
        (
         String foreignSchemaName,
         String foreignTableName,
         String connectionURL
         )
    {
        return new ForeignTableVTI( foreignSchemaName, foreignTableName, connectionURL );
    }

    ////////////////////////////////////////////////////////////////////////
    //
    //	ResultSet BEHAVIOR
    //
    ////////////////////////////////////////////////////////////////////////

    public  void    close() throws SQLException
    {
        if ( !isClosed() )
        {
            _foreignSchemaName = null;
            _foreignTableName = null;
            _connectionURL = null;
            _columnNames = null;
            _restriction = null;
            _columnNumberMap = null;

            if ( _foreignResultSet != null ) { _foreignResultSet.close(); }
            if ( _foreignPreparedStatement != null ) { _foreignPreparedStatement.close(); }

            _foreignResultSet = null;
            _foreignPreparedStatement = null;
        }
    }

    public  boolean next()  throws SQLException
    {
        if ( !isClosed() && (_foreignResultSet == null) )
        {
            _foreignPreparedStatement = prepareStatement
                ( getForeignConnection( _connectionURL ), makeQuery() );
            _foreignResultSet = _foreignPreparedStatement.executeQuery();
        }

        return _foreignResultSet.next();
    }

    public boolean isClosed() { return (_connectionURL == null); }

    public  boolean wasNull()   throws SQLException
    { return _foreignResultSet.wasNull(); }

    public  ResultSetMetaData   getMetaData()   throws SQLException
    { return _foreignResultSet.getMetaData(); }

    public  InputStream 	getAsciiStream(int i) throws SQLException
    { return _foreignResultSet.getAsciiStream( mapColumnNumber( i ) ); }
    
    public  BigDecimal 	getBigDecimal(int i) throws SQLException
    { return _foreignResultSet.getBigDecimal( mapColumnNumber( i ) ); }

    @Deprecated
    public  BigDecimal 	getBigDecimal(int i, int scale) throws SQLException
    { return _foreignResultSet.getBigDecimal( mapColumnNumber( i ), scale ); }
    
    public  InputStream 	getBinaryStream(int i)  throws SQLException
    { return _foreignResultSet.getBinaryStream( mapColumnNumber( i ) ); }
    
    public  Blob 	getBlob(int i)  throws SQLException
    { return _foreignResultSet.getBlob( mapColumnNumber( i ) ); }
    
    public  boolean 	getBoolean(int i) throws SQLException
    { return _foreignResultSet.getBoolean( mapColumnNumber( i ) ); }
    
    public  byte 	getByte(int i)    throws SQLException
    { return _foreignResultSet.getByte( mapColumnNumber( i ) ); }
    
    public  byte[] 	getBytes(int i) throws SQLException
    { return _foreignResultSet.getBytes( mapColumnNumber( i ) ); }
    
    public  Reader 	getCharacterStream(int i) throws SQLException
    { return _foreignResultSet.getCharacterStream( mapColumnNumber( i ) ); }

    public  Clob 	getClob(int i)  throws SQLException
    { return _foreignResultSet.getClob( mapColumnNumber( i ) ); }

    public  Date 	getDate(int i)  throws SQLException
    { return _foreignResultSet.getDate( mapColumnNumber( i ) ); }

    public  Date 	getDate(int i, Calendar cal)    throws SQLException
    { return _foreignResultSet.getDate( mapColumnNumber( i ), cal ); }

    public  double 	getDouble(int i)    throws SQLException
    { return _foreignResultSet.getDouble( mapColumnNumber( i ) ); }

    public  float 	getFloat(int i) throws SQLException
    { return _foreignResultSet.getFloat( mapColumnNumber( i ) ); }

    public  int 	getInt(int i)   throws SQLException
    { return _foreignResultSet.getInt( mapColumnNumber( i ) ); }

    public  long 	getLong(int i)  throws SQLException
    { return _foreignResultSet.getLong( mapColumnNumber( i ) ); }

    public  Object 	getObject(int i)    throws SQLException
    { return _foreignResultSet.getObject( mapColumnNumber( i ) ); }

    public  short 	getShort(int i) throws SQLException
    { return _foreignResultSet.getShort( mapColumnNumber( i ) ); }

    public  String 	getString(int i)    throws SQLException
    { return _foreignResultSet.getString( mapColumnNumber( i ) ); }

    public  Time 	getTime(int i)  throws SQLException
    { return _foreignResultSet.getTime( mapColumnNumber( i ) ); }

    public  Time 	getTime(int i, Calendar cal)    throws SQLException
    { return _foreignResultSet.getTime( mapColumnNumber( i ), cal ); }

    public  Timestamp 	getTimestamp(int i) throws SQLException
    { return _foreignResultSet.getTimestamp( mapColumnNumber( i ) ); }

    public  Timestamp 	getTimestamp(int i, Calendar cal)   throws SQLException
    { return _foreignResultSet.getTimestamp( mapColumnNumber( i ), cal ); }

    ////////////////////////////////////////////////////////////////////////
    //
    //	RestrictedVTI BEHAVIOR
    //
    ////////////////////////////////////////////////////////////////////////

    public  void    initScan
        ( String[] columnNames, Restriction restriction )
        throws SQLException
    {
        _columnNames = ArrayUtil.copy( columnNames );
        _restriction = restriction;

        int columnCount = _columnNames.length;

        _columnNumberMap = new int[ columnCount ];
        int foreignColumnID = 1;
        for ( int i = 0; i < columnCount; i++ )
        {
            if ( _columnNames[ i ] != null ) { _columnNumberMap[ i ] = foreignColumnID++; }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //
    //	Connection MANAGEMENT
    //
    ////////////////////////////////////////////////////////////////////////

    private static  Connection  getForeignConnection
        ( String connectionURL )
        throws SQLException
    {
        Connection  conn = _connections.get( connectionURL );

        if ( conn == null )
        {
            conn = DriverManager.getConnection( connectionURL );

            if ( conn != null ) { _connections.put( connectionURL, conn ); }
        }

        return conn;
    }

    ////////////////////////////////////////////////////////////////////////
    //
    //	QUERY FACTORY
    //
    ////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Build the query which will be sent to the foreign database.
     * </p>
     */
    private String  makeQuery()
    {
        StringBuilder    buffer = new StringBuilder();

        buffer.append( "select " );

        int possibleCount = _columnNames.length;
        int actualCount = 0;
        for ( int i = 0; i < possibleCount; i++ )
        {
            String  rawName = _columnNames[ i ];
            if ( rawName == null ) { continue; }

            if ( actualCount > 0 ) { buffer.append( ", " ); }
            actualCount++;
            
            buffer.append( delimitedID( rawName ) );
        }

        buffer.append( "\nfrom " );
        buffer.append( delimitedID( _foreignSchemaName ) );
        buffer.append( '.' );
        buffer.append( delimitedID( _foreignTableName ) );

        if ( _restriction != null )
        {
            String  clause = _restriction.toSQL();

            if (clause != null)
            {
                clause = clause.trim();
                if ( clause.length() != 0 )
                {
                    buffer.append( "\nwhere " + clause );
                }
            }
        }

        return buffer.toString();
    }

    private static  String  delimitedID( String text )  { return IdUtil.normalToDelimited( text ); }

    private static  PreparedStatement   prepareStatement
        ( Connection conn, String text )
        throws SQLException
    {
        return conn.prepareStatement( text );
    }

    ////////////////////////////////////////////////////////////////////////
    //
    //	UTILITY METHODS
    //
    ////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Map a 1-based Derby column number to a 1-based column number in the
     * foreign query.
     * </p>
     */
    private int mapColumnNumber( int derbyNumber )
    {
        return _columnNumberMap[ derbyNumber - 1 ];
    }
}

