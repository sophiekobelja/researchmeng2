package org.apache.derbyTesting.functionTests.tests.jdbcapi;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.derby.tools.ij;
import org.apache.derbyTesting.functionTests.util.Formatters;


/*
 *  Derby - Class org.apache.derbyTesting.functionTests.tests.jdbcapi.LargeDataLocks
 *  
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


/**
 * Test for DERBY-2892/DERBY-255 to ensure that ResultSet methods
 * getString(), getCharacterStream, getBytes() and getBinaryStream()
 * do not hold locks until the end of the transaction.
 * 
 */

public class LargeDataLocks {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        Connection conn = null;
        // TODO Auto-generated method stub
        try
        {
                // use the ij utility to read the property file and
                // make the initial connection.
                ij.getPropertyArg(args);
                conn = ij.startJBMS();
                conn.setAutoCommit(false);
                PreparedStatement ps = null;
                String sql;
                Statement stmt = conn.createStatement();
                sql = "CREATE TABLE t1 (bc CLOB(1M), bincol BLOB(1M), datalen int)";
                stmt.executeUpdate(sql);

                // Insert big and little values
                sql = "INSERT into t1 values(?,?,?)";
                ps = conn.prepareStatement(sql);

                ps.setCharacterStream(1, new java.io.StringReader(Formatters
                        .repeatChar("a", 38000)), 38000);
                ps.setBytes(2, Formatters.repeatChar("a", 38000).getBytes());
                ps.setInt(3, 38000);
                ps.executeUpdate();
                ps.close();
                stmt.close();
                conn.commit();

                testGetCharacterStream(conn);
                
                testGetString(conn);
                testGetBinaryStream(conn);
                testGetBytes(conn);
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }
    /**
     * Test that ResultSet.getCharacterStream does not hold locks after the
     * ResultSet is closed
     * 
     * @param conn Connection to use
     * @throws SQLException
     * @throws IOException
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     */
    public static void testGetCharacterStream(Connection conn) throws SQLException, IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        // getCharacterStream() no locks expected after retrieval
        int numChars = 0;
        Statement stmt = conn.createStatement();
        String sql = "SELECT bc from t1";
        // First with getCharacterStream
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        java.io.Reader characterStream = rs.getCharacterStream(1);
        // Extract all the characters
        int read = characterStream.read();
        while (read != -1) {
            read = characterStream.read();
            numChars++;
        }
        assertEquals(38000, numChars);
        rs.close();
        assertEquals(0, countLocks());
        stmt.close();
        conn.commit();
    }

    /**
     * Verify that getBytes does not hold locks after ResultSet is closed.
     * 
     * @param conn Connection to use
     * @throws SQLException
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     */
    public static void testGetBytes(Connection conn) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        // getBytes() no locks expected after retrieval
        Statement stmt = conn.createStatement();
        String sql = "SELECT bincol from t1";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        byte[] value = rs.getBytes(1);
        assertEquals(38000, value.length);
        rs.close();
        assertEquals(0, countLocks());
        conn.commit();

    }

    /**
     * Verify that getBinaryStream() does not hold locks after retrieval
     * 
     * @param conn Connection to use
     * @throws SQLException
     * @throws IOException
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     */
    public static void testGetBinaryStream(Connection conn) throws SQLException, IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        int numBytes = 0;
        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement();
        String sql = "SELECT bincol from t1";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        InputStream stream = rs.getBinaryStream(1);
        int read = stream.read();
        while (read != -1) {
            read = stream.read();
            numBytes++;
        }
        assertEquals(38000, numBytes);
        rs.close();
        stmt.close();
        assertEquals(0, countLocks());
        conn.commit();
    }

    /**
     * Test that ResultSet.getString() does not hold locks after the ResultSet
     * is closed
     * @param conn  Connnection to use
     * @throws SQLException
     * @throws IOException
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     */
    public static void testGetString(Connection conn) throws SQLException, IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        // getString() no locks expected after retrieval
        Statement stmt = conn.createStatement();
        String sql = "SELECT bc from t1";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        String value = rs.getString(1);
        assertEquals(38000, value.length());
        rs.close();
        stmt.close();
        assertEquals(0, countLocks());
        conn.commit();
    }

    /**
     * Create a new connection and count the number of locks held.
     * 
     * @return number of locks held
     * 
     * @throws SQLExceptions
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     */
    public static int countLocks() throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        Connection conn = ij.startJBMS();
        String sql;
        Statement stmt = conn.createStatement();

        sql = "Select count(*) from new org.apache.derby.diag.LockTable() as LT";
        ResultSet lockrs = stmt.executeQuery(sql);
        lockrs.next();
        int count = lockrs.getInt(1);
        lockrs.close();
        stmt.close();
        conn.close();
        return count;
    }


    private static void assertEquals(int expected, int value) {
        if (expected != value)
        {
            System.out.println("FAIL: " + value + " does not match expected value: " + expected);
            new Exception().printStackTrace();
        }
    }
    
}
