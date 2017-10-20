/*
 *
 * Derby - Class BaseJDBCTestCase
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific 
 * language governing permissions and limitations under the License.
 */
package org.apache.derbyTesting.junit;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.net.URL;
import java.sql.*;

import junit.framework.AssertionFailedError;

import org.apache.derby.iapi.services.info.JVMInfo;
import org.apache.derby.tools.ij;


/**
 * Base class for JDBC JUnit tests.
 * A method for getting a default connection is provided, along with methods
 * for telling if a specific JDBC client is used.
 */
public abstract class BaseJDBCTestCase
    extends BaseTestCase {

    /**
     * Maintain a single connection to the default
     * database, opened at the first call to getConnection.
     * Typical setup will just require a single connection.
     * @see BaseJDBCTestCase#getConnection()
     */
    private Connection conn;
    
    /**
     * Create a test case with the given name.
     *
     * @param name of the test case.
     */
    public BaseJDBCTestCase(String name) {
        super(name);
    }
    
    /**
     * Obtain the connection to the default database.
     * This class maintains a single connection returned
     * by this class, it is opened on the first call to
     * this method. Subsequent calls will return the same
     * connection object unless it has been closed. In that
     * case a new connection object will be returned.
     * <P>
     * The tearDown method will close the connection if
     * it is open.
     * <BR>
     * The connection will be initialized by calling initializeConnection.
     * A sub-class may provide an implementation of initializeConnection
     * to ensure its connections are in a consistent state that is different
     * to the default.
     * @see #openDefaultConnection()
     */
    public Connection getConnection() throws SQLException
    {
        if (conn != null)
        {
            if (!conn.isClosed())
                return conn;
            conn = null;
        }
        return conn = openDefaultConnection();
    }
    
    /**
     * Allow a sub-class to initialize a connection to provide
     * consistent connection state for its tests. Called once
     * for each time these method calls open a connection:
     * <UL>
     * <LI> getConnection()
     * <LI> openDefaultConnection()
     * <LI> openConnection(database)
     * <LI> getDefaultConnection(String connAttrs)
     * </UL>
     * Default action is to not modify the connection's state from
     * the initialization provided by the data source.
     * @param conn Connection to be intialized
     * @throws SQLException Error setting the initial state.
     */
    protected void initializeConnection(Connection conn) throws SQLException
    {
    }
    
    /**
     * Utility method to create a Statement using the connection
     * returned by getConnection.
     * @return Statement object from getConnection.createStatement()
     * @throws SQLException
     */
    public Statement createStatement() throws SQLException
    {
        return getConnection().createStatement();
    }

    /**
     * Utility method to create a Statement using the connection
     * returned by getConnection.
     * @return Statement object from
     * getConnection.createStatement(resultSetType, resultSetConcurrency)
     * @throws SQLException
     */
    public Statement createStatement(int resultSetType,
            int resultSetConcurrency) throws SQLException
    {
        return getConnection().createStatement(resultSetType, resultSetConcurrency);
    }
    /**
     * Utility method to create a PreparedStatement using the connection
     * returned by getConnection.
     * @return Statement object from
     * getConnection.prepareStatement(sql)
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        return getConnection().prepareStatement(sql);
    }    
    /**
     * Utility method to create a PreparedStatement using the connection
     * returned by getConnection and a flag that signals the driver whether
     * the auto-generated keys produced by this Statement object should be
     * made available for retrieval.
     * @return Statement object from
     * prepareStatement(sql, autoGeneratedKeys)
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
        throws SQLException
    {
        return getConnection().prepareStatement(sql, autoGeneratedKeys);
    }    

    /**
     * Utility method to create a CallableStatement using the connection
     * returned by getConnection.
     * @return Statement object from
     * getConnection().prepareCall(sql)
     * @throws SQLException
     */
    public CallableStatement prepareCall(String sql) throws SQLException
    {
        return getConnection().prepareCall(sql);
    }
    
    /**
     * Utility method to commit using the connection
     * returned by getConnection.
     * @throws SQLException
     */
    public void commit() throws SQLException
    {
        getConnection().commit();
    }  
    /**
     * Utility method to rollback using the connection
     * returned by getConnection.
     * @throws SQLException
     */
    public void rollback() throws SQLException
    {
        getConnection().rollback();
    } 
    /**
     * Tear down this fixture, sub-classes should call
     * super.tearDown(). This cleanups & closes the connection
     * if it is open.
     */
    protected void tearDown()
    throws java.lang.Exception
    {
        JDBC.cleanup(conn);
        conn = null;
    }

    /**
     * Open a connection to the default database.
     * If the database does not exist, it will be created.
     * A default username and password will be used for the connection.
     * 
     * The connection will be initialized by calling initializeConnection.
     * A sub-class may provide an implementation of initializeConnection
     * to ensure its connections are in a consistent state that is different
     * to the default.
     *
     * @return connection to default database.
     * @see TestConfiguration#openDefaultConnection()
     * @see BaseJDBCTestCase#initializeConnection(Connection)
     */
    public Connection openDefaultConnection()
        throws SQLException {
        Connection conn =  getTestConfiguration().openDefaultConnection();
        initializeConnection(conn);
        return conn;
    }
    
    /**
     * Open a connection to the current default database using the
     * specified user name and password.
     * <BR>
     * This connection is not
     * automaticaly closed on tearDown, the text fixture must
     * ensure the connection is closed.
     * 
     * The connection will be initialized by calling initializeConnection.
     * A sub-class may provide an implementation of initializeConnection
     * to ensure its connections are in a consistent state that is different
     * to the default.
     * @see BaseJDBCTestCase#initializeConnection(Connection)
     */
    public Connection openDefaultConnection(String user, String password)
    throws SQLException
    {
        Connection conn =  getTestConfiguration().openDefaultConnection(user,
                password);
        initializeConnection(conn);
        return conn;        
    }
    
    /**
     * Open a connection to the current default database using the
     * specified user name. The password is a function of
     * the user name and the password token setup by the
     * builtin authentication decorators.
     * <BR>
     * If the fixture is not wrapped in one of the decorators
     * that setup BUILTIN authentication then the password
     * is a function of the user name and the empty string
     * as the password token. This mode is not recommended.
     * 
     * <BR>
     * This connection is not
     * automaticaly closed on tearDown, the text fixture must
     * ensure the connection is closed.
     * <BR>
     * The connection will be initialized by calling initializeConnection.
     * A sub-class may provide an implementation of initializeConnection
     * to ensure its connections are in a consistent state that is different
     * to the default.
     * 
     * @see DatabasePropertyTestSetup#builtinAuthentication(Test, String[], String)
     * @see TestConfiguration#sqlAuthorizationDecorator(Test, String[], String)
     * @see BaseJDBCTestCase#initializeConnection(Connection)
     */
    public Connection openUserConnection(String user) throws SQLException
    {
        return openDefaultConnection(user,
                getTestConfiguration().getPassword(user));
    }
    
    /**
     * Open a connection to the specified database.
     * If the database does not exist, it will be created.
     * A default username and password will be used for the connection.
     * Requires that the test has been decorated with a
     * additionalDatabaseDecorator with the matching name.
     * <BR>
     * The connection will be initialized by calling initializeConnection.
     * A sub-class may provide an implementation of initializeConnection
     * to ensure its connections are in a consistent state that is different
     * to the default.
     * @return connection to default database.
     * @see TestConfiguration#additionalDatabaseDecorator(Test, String)
     * @see BaseJDBCTestCase#initializeConnection(Connection)
     */
    public Connection openConnection(String databaseName)
        throws SQLException {
        Connection conn =  getTestConfiguration().openConnection(databaseName);
        initializeConnection(conn);
        return conn;
    }
    
    /**
     * Run a SQL script through ij discarding the output
     * using this object's default connection. Intended for
     * setup scripts.
     * @throws UnsupportedEncodingException 
     * @throws SQLException 
     */
    public int runScript(InputStream script, String encoding)
        throws UnsupportedEncodingException, SQLException
    {
        // Sink output.
        OutputStream sink = new OutputStream() {
            public void write(byte[] b, int off, int len) {}
            public void write(int b) {}
        };
        
        // Use the same encoding as the input for the output.    
        return ij.runScript(getConnection(), script, encoding,
                sink, encoding);       
    }
    
    /**
     * Run a SQL script through ij discarding the output
     * using this object's default connection. Intended for
     * setup scripts.
     * @return Number of errors executing the script
     * @throws UnsupportedEncodingException 
     * @throws PrivilegedActionException
     * @throws SQLException 
     */
    public int runScript(String resource,String encoding)
        throws UnsupportedEncodingException, SQLException,
        PrivilegedActionException,IOException
    {
        
        URL sql = getTestResource(resource);
        assertNotNull("SQL script missing: " + resource, sql);
        InputStream sqlIn = openTestResource(sql);
        Connection conn = getConnection();
        int numErrors = runScript(sqlIn,encoding);
        sqlIn.close();
        
        if (!conn.isClosed() && !conn.getAutoCommit())
            conn.commit();
        
        return numErrors;
    }
    
    /**
     * Run a set of SQL commands from a String discarding the output.
     * Commands are separated by a semi-colon. Connection used
     * is this objects default connection.
     * @param sqlCommands
     * @return Number of errors executing the script.
     * @throws UnsupportedEncodingException
     * @throws SQLException
     */
    public int runSQLCommands(String sqlCommands)
        throws UnsupportedEncodingException, SQLException
    {
        byte[] raw = sqlCommands.getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(raw);
        
        return runScript(in, "UTF-8");
    }
    
    /**
     * Tell if the client is embedded.
     *
     * @return <code>true</code> if using the embedded client
     *         <code>false</code> otherwise.
     */
     public static boolean usingEmbedded() {
         return TestConfiguration.getCurrent().getJDBCClient().isEmbedded();
     }
    
    /**
    * Tell if the client is DerbyNetClient.
    *
    * @return <code>true</code> if using the DerbyNetClient client
    *         <code>false</code> otherwise.
    */
    public static boolean usingDerbyNetClient() {
        return TestConfiguration.getCurrent().getJDBCClient().isDerbyNetClient();
    }
    
    /**
    * Tell if the client is DerbyNet.
    *
    * @return <code>true</code> if using the DerbyNet client
    *         <code>false</code> otherwise.
    */
    public static boolean usingDerbyNet() {
        return TestConfiguration.getCurrent().getJDBCClient().isDB2Client();
    }

    /**
     * Assert equality between two <code>Blob</code> objects.
     * If both input references are <code>null</code>, they are considered
     * equal. The same is true if both blobs have <code>null</code>-streams.
     *
     * @param b1 first <code>Blob</code>.
     * @param b2 second <code>Blob</code>.
     * @throws AssertionFailedError if blobs are not equal.
     * @throws IOException if reading or closing a stream fails
     * @throws SQLException if obtaining a stream fails
     */
    public static void assertEquals(Blob b1, Blob b2)
            throws IOException, SQLException {
        if (b1 == null || b2 == null) {
            assertNull("Blob b2 is null, b1 is not", b1);
            assertNull("Blob b1 is null, b2 is not", b2);
            return;
        }
        assertEquals("Blobs have different lengths",
                     b1.length(), b2.length());
        InputStream is1 = b1.getBinaryStream();
        InputStream is2 = b2.getBinaryStream();

        if (is1 == null || is2 == null) {
            assertNull("Blob b2 has null-stream, blob b1 doesn't", is1);
            assertNull("Blob b1 has null-stream, blob b2 doesn't", is2);
            return;
        }
        
        // wrap buffered stream around the binary stream
        is1 = new BufferedInputStream(is1);
        is2 = new BufferedInputStream(is2);
 
        long index = 1;
        int by1 = is1.read();
        int by2 = is2.read();
        do {
            // Avoid string concatenation for every byte in the stream.
            if (by1 != by2) {
                assertEquals("Blobs differ at index " + index,
                        by1, by2);
            }
            index++;
            by1 = is1.read();
            by2 = is2.read();
        } while ( by1 != -1 || by2 != -1);
        is1.close();
        is2.close();
    }

    /**
     * Assert equality between two <code>Clob</code> objects.
     * If both input references are <code>null</code>, they are considered
     * equal. The same is true if both clobs have <code>null</code>-streams.
     *
     * @param c1 first <code>Clob</code>.
     * @param c2 second <code>Clob</code>.
     * @throws AssertionFailedError if clobs are not equal.
     * @throws IOException if reading or closing a stream fails
     * @throws SQLException if obtaining a stream fails
     */
    public static void assertEquals(Clob c1, Clob c2)
            throws IOException, SQLException {
        if (c1 == null || c2 == null) {
            assertNull("Clob c2 is null, c1 is not", c1);
            assertNull("Clob c1 is null, c2 is not", c2);
            return;
        }
        assertEquals("Clobs have different lengths",
                     c1.length(), c2.length());
        Reader r1 = c1.getCharacterStream();
        assertNotNull(r1); // java.sql.Blob object cannot represent NULL
        Reader r2 = c2.getCharacterStream();
        assertNotNull(r2); // java.sql.Blob object cannot represent NULL

        // wrap buffered reader around the character stream
        r1 = new BufferedReader(r1);
        r2 = new BufferedReader(r2);

        long index = 1;
        int ch1 = r1.read();
        int ch2 = r2.read();
        do {
            // Avoid string concatenation for every char in the stream.
            if (ch1 != ch2) {
                assertEquals("Clobs differ at index " + index,
                        ch1, ch2);
            }
            index++;
            ch1 = r1.read();
            ch2 = r2.read();
        } while (ch1 != -1 || ch2 != -1);
        r1.close();
        r2.close();
    }

    /**
     * Assert that SQLState is as expected.  If the SQLState for
     * the top-level exception doesn't match, look for nested
     * exceptions and, if there are any, see if they have the
     * desired SQLState.
     *
     * @param message message to print on failure.
     * @param expected the expected SQLState.
     * @param exception the exception to check the SQLState of.
     */
    public static void assertSQLState(String message, 
                                      String expected, 
                                      SQLException exception) {
        // Make sure exception is not null. We want to separate between a
        // null-exception object, and a null-SQLState.
        assertNotNull("Exception cannot be null when asserting on SQLState", 
                      exception);
        
        try {
            String state = exception.getSQLState();
            
            if ( state != null )
                assertTrue("The exception's SQL state must be five characters long",
                        state.length() == 5);
            
            if ( expected != null )
                assertTrue("The expected SQL state must be five characters long",
                    expected.length() == 5);
            
            assertEquals(message, expected, state);
        } catch (AssertionFailedError e) {
            
            // Save the SQLException
            try {
                Method m = Throwable.class.getMethod(
                    "initCause", new Class[] { Throwable.class } );
                m.invoke(e, new Object[] { exception });
            } catch (Throwable t) {
                // Some VMs don't support initCause(). It is OK if they fail.
            }

            if (usingDerbyNetClient())
            {
                /* For chained exceptions the Derby Client just concatenates
                 * them into the exception message.  So search the message
                 * for the desired SQLSTATE.  This isn't ideal, but it
                 * should work...
                 */
                if (exception.getMessage().
                    indexOf("SQLSTATE: " + expected) == -1)
                {
                    throw e;
                }
            }
            else if (usingDerbyNet())
            {
                /* For JCC the error message is a series of tokens representing
                 * different things like SQLSTATE, SQLCODE, nested SQL error
                 * message, and nested SQL state.  Based on observation it
                 * appears that the last token in the message is the SQLSTATE
                 * of the nested exception, and it's preceded by a colon.
                 * So using that (hopefully consistent?) rule, try to find
                 * the target SQLSTATE.
                 */
                String msg = exception.getMessage();
                if (!msg.substring(msg.lastIndexOf(":")+1)
                    .trim().equals(expected))
                {
                    throw e;
                }
            }
            else
            {
                // Check nested exceptions to see if any of them is
                // the one we're looking for.
                exception = exception.getNextException();
                if (exception != null)
                    assertSQLState(message, expected, exception);
                else
                    throw e;
            }
        }
    }

    /**
     * Assert that SQLState is as expected.
     *
     * @param expected the expected SQLState.
     * @param exception the exception to check the SQLState of.
     */
    public static void assertSQLState(String expected, SQLException exception) {
        assertSQLState("Unexpected SQL state.", expected, exception);
    }

    /**
     * Assert that the query does not compile and throws
     * a SQLException with the expected state.
     * 
     * @param sqlState expected sql state.
     * @param query the query to compile.
     */
    public void assertCompileError(String sqlState, String query) {

        try {
            PreparedStatement pSt = prepareStatement(query);
            if (usingDerbyNet())
            {
                /* For JCC the prepares are deferred until execution,
                 * so we have to actually execute in order to see the
                 * expected error.  Note that we don't need to worry
                 * about binding the parameters (if any); the compile
                 * error should occur before the execution-time error
                 * about unbound parameters.
                 */
                pSt.execute();
            }
            fail("expected compile error: " + sqlState);
        } catch (SQLException se) {
            assertSQLState(sqlState, se);
        }
    }
    
    /**
     * Check the table using SYSCS_UTIL.SYSCS_CHECK_TABLE.
     */
    public void assertCheckTable(String table) throws SQLException
    {
        PreparedStatement ps = prepareStatement(
                "VALUES SYSCS_UTIL.SYSCS_CHECK_TABLE(?, ?)");
        
        ps.setString(1, getTestConfiguration().getUserName());
        ps.setString(2, table);
        
        ResultSet rs = ps.executeQuery();
        JDBC.assertSingleValueResultSet(rs, "1");
        ps.close();
    }
    
    /**
     * Assert that the number of rows in a table is an expected value.
     * Query uses a SELECT COUNT(*) FROM "table".
     * 
     * @param table Name of table in current schema, will be quoted
     * @param rowCount Number of rows expected in the table
     * @throws SQLException Error accessing the database.
     */
    protected void assertTableRowCount(String table, int rowCount) throws SQLException
    {
        assertEscapedTableRowCount(JDBC.escape(table), rowCount);
    }

    /**
     * Assert that the number of rows in a table is an expected value.
     * Query uses a SELECT COUNT(*) FROM table.
     * 
     * @param escapedTableName Escaped name of table, will be used as-is.
     * @param rowCount Number of rows expected in the table
     * @throws SQLException Error accessing the database.
     */
    private void assertEscapedTableRowCount(String escapedTableName, int rowCount)
       throws SQLException
    {
    
        Statement s = createStatement();
        ResultSet rs = s.executeQuery(
                "SELECT COUNT(*) FROM " + escapedTableName);
        rs.next();
        assertEquals(escapedTableName + " row count:",
            rowCount, rs.getInt(1));
        rs.close();
        s.close();
    }

    /**
     * Assert that the query fails (either in compilation,
     * execution, or retrieval of results--doesn't matter)
     * and throws a SQLException with the expected state.
     *
     * Assumption is that 'query' does *not* have parameters
     * that need binding and thus can be executed using a
     * simple Statement.execute() call.
     * 
     * @param sqlState expected sql state.
     * @param st Statement object on which to execute.
     * @param query the query to compile and execute.
     */
    public static void assertStatementError(String sqlState,
        Statement st, String query)
    {
        try {
            boolean haveRS = st.execute(query);
            fetchAndDiscardAllResults(st, haveRS);
            fail("Expected error '" + sqlState +
                "' but no error was thrown.");
        } catch (SQLException se) {
            assertSQLState(sqlState, se);
        }
    }

    /**
     * Assert that execution of the received PreparedStatement
     * object fails (either in execution or when retrieving
     * results) and throws a SQLException with the expected
     * state.
     * 
     * Assumption is that "pSt" is either a PreparedStatement
     * or a CallableStatement that has already been prepared
     * and whose parameters (if any) have already been bound.
     * Thus the only thing left to do is to call "execute()"
     * and look for the expected SQLException.
     * 
     * @param sqlState expected sql state.
     * @param pSt A PreparedStatement or CallableStatement on
     *  which to call "execute()".
     */
    public static void assertStatementError(String sqlState,
        PreparedStatement pSt)
    {
        try {
            boolean haveRS = pSt.execute();
            fetchAndDiscardAllResults(pSt, haveRS);
            fail("Expected error '" + sqlState +
                "' but no error was thrown.");
        } catch (SQLException se) {
            assertSQLState(sqlState, se);
        }
    }


    /**
     * Executes the Callable statement that is expected to fail and verifies
     * that it throws the expected SQL exception.
     * @param expectedSE The expected SQL exception
     * @param conn The Connection handle
     * @param callSQL The SQL to execute
     * @throws SQLException
     */
    public static void assertCallError(String expectedSE, Connection conn, String callSQL)
    throws SQLException
    {
        try {
            CallableStatement cs = conn.prepareCall(callSQL);
            cs.execute();
            fail("FAIL - SQL expected to throw exception");
        } catch (SQLException se) {
            assertSQLState(expectedSE, se.getSQLState(), se);
        }
    }
    /**
     * Perform a fetch on the ResultSet with an expected failure
     * 
     * @param sqlState Expected SQLState
     * @param rs   ResultSet upon which next() will be called
     */
    public static void assertNextError(String sqlState,ResultSet rs)
    {
    	try {
    		rs.next();
    		fail("Expected error on next()");
    	}catch (SQLException se){
    		assertSQLState(sqlState,se);
    	}
    }
    
    /**
     * Perform getInt(position) with expected error
     * @param position  position argument to pass to getInt
     * @param sqlState  sqlState of expected error
     * @param rs ResultSet upon which to call getInt(position)
     */
    public static void assertGetIntError(int position, String sqlState, ResultSet rs)
    {
    	try {
    		rs.getInt(position);
    		fail("Expected exception " + sqlState);
    	} catch (SQLException se){
    		assertSQLState(sqlState,se);
    	}
    			
    	
    }
    /**
     * Take a Statement object and a SQL query, execute it
     * via the "executeUpdate()" method, and assert that the
     * resultant row count matches the received row count.
     *
     * Assumption is that 'query' does *not* have parameters
     * that need binding and that it can be executed using a
     * simple Statement.executeUpdate() call.
     * 
     * @param st Statement object on which to execute.
     * @param expectedRC Expected row count.
     * @param query Query to execute.
     */
    public static void assertUpdateCount(Statement st,
        int expectedRC, String query) throws SQLException
    {
        assertEquals("Update count does not match:",
            expectedRC, st.executeUpdate(query));
    }

    /**
     * Assert that a call to "executeUpdate()" on the received
     * PreparedStatement object returns a row count that matches
     * the received row count.
     *
     * Assumption is that "pSt" is either a PreparedStatement
     * or a CallableStatement that has already been prepared
     * and whose parameters (if any) have already been bound.
     * Also assumes the statement's SQL is such that a call
     * executeUpdate() is allowed.  Thus the only thing left
     * to do is to call the "executeUpdate" method.
     * 
     * @param pSt The PreparedStatement on which to execute.
     * @param expectedRC The expected row count.
     */
    public static void assertUpdateCount(PreparedStatement pSt,
        int expectedRC) throws SQLException
    {
        assertEquals("Update count does not match:",
            expectedRC, pSt.executeUpdate());
    }

    /**
     * Take the received Statement--on which a query has been
     * executed--and fetch all rows of all result sets (if any)
     * returned from execution.  The rows themselves are
     * discarded.  This is useful when we expect there to be
     * an error when processing the results but do not know
     * (or care) at what point the error occurs.
     *
     * @param st An already-executed statement from which
     *  we get the result set to process (if there is one).
     * @param haveRS Whether or not the the statement's
     *  first result is a result set (as opposed to an
     *  update count).
     */
    private static void fetchAndDiscardAllResults(Statement st,
        boolean haveRS) throws SQLException
    {
        ResultSet rs = null;
        while (haveRS || (st.getUpdateCount() != -1))
        {
            // If we have a result set, iterate through all
            // of the rows.
            if (haveRS)
                JDBC.assertDrainResults(st.getResultSet(), -1);
            haveRS = st.getMoreResults();
        }
    }

    /**
     * Assert that the two (2) passed-in SQLException's are equals and
     * not just '=='.
     *
     * @param se1 first SQLException to compare
     * @param se2 second SQLException to compare
     */
    public static void assertSQLExceptionEquals(SQLException se1,
                                                SQLException se2) {
        // Ensure non-null SQLException's are being passed.
        assertNotNull(
            "Passed-in SQLException se1 cannot be null",
            se1);
        assertNotNull(
            "Passed-in SQLException se2 cannot be null",
            se2);

        // Now verify that the passed-in SQLException's are of the same type
        assertEquals("SQLException class types are different",
                     se1.getClass().getName(), se2.getClass().getName());

        // Here we check that the detailed message of both
        // SQLException's is the same
        assertEquals(
                "Detailed messages of the SQLException's are different",
                 se1.getMessage(), se2.getMessage());

        // Now if we're running in a java runtime that supports chained
        // exception, then let's compare these 2 SQLException's and
        // whatever chained SQLException there can be through the beauty
        // of recursion
        if (JVMInfo.JDK_ID >= JVMInfo.J2SE_14)
        {
            // Here we check that the detailed message of both
            // SQLException's throwable "cause" is the same.
            // getCause() was introduced as part of Java 4.
            // Save the SQLException
            Throwable se1Cause = null, se2Cause = null;
            Method m = null;
            try
            {
                m = Throwable.class.getMethod("getCause", new Class[] {});
                se1Cause = (Throwable) m.invoke(se1, new Object[] {});
            }
            catch (Throwable t)
            {
                // Throwable.getCause() should have succeeded
                fail("Unexpected error: " + t.getMessage());
            }
            if (se1Cause != (Throwable) null)
            {
                try
                {
                    se2Cause = (Throwable) m.invoke(se2, new Object[] {});
                }
                catch (Throwable t)
                {
                    // Throwable.getCause() should have succeeded
                    fail("Unexpected error: " + t.getMessage());
                }
                assertThrowableEquals(se1Cause, se2Cause);
            }
            else // se2.getCause() should not return any Cause then
                assertNull(se2Cause);

            if (se1.getNextException() != null)
            {
                assertSQLExceptionEquals(se1.getNextException(),
                                         se2.getNextException());
            }
        }
    }

    /**
     * Compares two JDBC types to see if they are equivalent.
     * DECIMAL and NUMERIC and DOUBLE and FLOAT are considered
     * equivalent.
     * @param expectedType Expected jdbctype from java.sql.Types
     * @param type         Actual type from metadata
     */
    public static void assertEquivalentDataType(int expectedType, int type)
    {
     if (expectedType == type)
         return;
     if (expectedType == java.sql.Types.DECIMAL && 
                 type == java.sql.Types.NUMERIC)
         return;
     if (expectedType == java.sql.Types.NUMERIC && 
             type == java.sql.Types.DECIMAL)
         return;
     if (expectedType == java.sql.Types.DOUBLE && 
                 type == java.sql.Types.FLOAT)
         return;
     if (expectedType == java.sql.Types.FLOAT && 
             type == java.sql.Types.DOUBLE)
     return;
     fail("types:" + expectedType + " and " + type + " are not equivalent");
     
    }
    
} // End class BaseJDBCTestCase


