/*

   Derby - Class org.apache.derbyTesting.functionTests.tests.lang.derby4095Test

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

package org.apache.derbyTesting.functionTests.tests.lang;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import org.apache.derby.tools.ij;

public class derby4095Test {

    public static void main(String args[]) throws Exception  {
	/* Load the JDBC Driver class */
    	// use the ij utility to read the property file and
    	// make the initial connection.
    	ij.getPropertyArg(args);
    	Connection conn = ij.startJBMS();
	testDerby4095OldTriggerRows(conn);
	testDerby4095NewTriggerRows(conn);
    }


    /**
     * Test that a nested loop join that accesses the 
     * TriggerOldTransitionRowsVTI can reopen the ResultSet properly 
     * when it re-executes.
     * @param conn Connection to use
     * @throws SQLException
     */
    public static void testDerby4095OldTriggerRows(Connection conn) throws SQLException {
        Statement s = conn.createStatement();
        
        s.executeUpdate("CREATE TABLE APP.TAB (I INT)");
        s.executeUpdate("CREATE TABLE APP.LOG (I INT, NAME VARCHAR(30), DELTIME TIMESTAMP)");
        s.executeUpdate("CREATE TABLE APP.NAMES(ID INT, NAME VARCHAR(30))");

        
        s.executeUpdate("CREATE TRIGGER  APP.MYTRIG AFTER DELETE ON APP.TAB REFERENCING OLD_TABLE AS OLDROWS FOR EACH STATEMENT MODE DB2SQL INSERT INTO APP.LOG(i,name,deltime) SELECT OLDROWS.I, NAMES.NAME, CURRENT_TIMESTAMP FROM --DERBY-PROPERTIES joinOrder=FIXED\n NAMES, OLDROWS --DERBY-PROPERTIES joinStrategy = NESTEDLOOP\n WHERE (OLDROWS.i = NAMES.ID) AND (1 = 1)");
        
        s.executeUpdate("insert into APP.tab values(1)");
        s.executeUpdate("insert into APP.tab values(2)");
        s.executeUpdate("insert into APP.tab values(3)");

        s.executeUpdate("insert into APP.names values(1,'Charlie')");
        s.executeUpdate("insert into APP.names values(2,'Hugh')");
        s.executeUpdate("insert into APP.names values(3,'Alex')");

        // Now delete a row so we fire the trigger.
        s.executeUpdate("delete from tab where i = 1");
        // Check the log to make sure the trigger fired ok
        ResultSet loggedDeletes = s.executeQuery("SELECT * FROM APP.LOG");
	// make sure this is one row.
	checkNumRows(loggedDeletes,1);
        s.executeUpdate("DROP TABLE APP.TAB");
        s.executeUpdate("DROP TABLE APP.LOG");
        s.executeUpdate("DROP TABLE APP.NAMES");
        
    }
    
    /**
     * Test that a nested loop join that accesses the 
     * TriggerNewTransitionRowsVTI can reopen the ResultSet properly 
     * when it re-executes.
     * @param conn Connection to use
     * @throws SQLException
     */
    public static void testDerby4095NewTriggerRows(Connection conn) throws SQLException {
        Statement s = conn.createStatement();
        s.executeUpdate("CREATE TABLE APP.TAB (I INT)");
        s.executeUpdate("CREATE TABLE APP.LOG (I INT, NAME VARCHAR(30), UPDTIME TIMESTAMP, NEWVALUE INT)");
        s.executeUpdate("CREATE TABLE APP.NAMES(ID INT, NAME VARCHAR(30))");

        
        s.executeUpdate("CREATE TRIGGER  APP.MYTRIG AFTER UPDATE ON APP.TAB REFERENCING OLD_TABLE AS OLDROWS NEW_TABLE AS NEWROWS FOR EACH STATEMENT MODE DB2SQL INSERT INTO APP.LOG(i,name,updtime,newvalue) SELECT OLDROWS.I, NAMES.NAME, CURRENT_TIMESTAMP, NEWROWS.I  FROM --DERBY-PROPERTIES joinOrder=FIXED\n NAMES, NEWROWS --DERBY-PROPERTIES joinStrategy = NESTEDLOOP\n ,OLDROWS WHERE (NEWROWS.i = NAMES.ID) AND (1 = 1)");
        
        s.executeUpdate("insert into tab values(1)");
        s.executeUpdate("insert into tab values(2)");
        s.executeUpdate("insert into tab values(3)");

        s.executeUpdate("insert into names values(1,'Charlie')");
        s.executeUpdate("insert into names values(2,'Hugh')");
        s.executeUpdate("insert into names values(3,'Alex')");

        // Now update a row to fire the trigger
        s.executeUpdate("update tab set i=1 where i = 1");

        // Check the log to make sure the trigger fired ok
        ResultSet loggedUpdates = s.executeQuery("SELECT * FROM APP.LOG");
        checkNumRows(loggedUpdates, 1);        
        
        s.executeUpdate("DROP TABLE APP.TAB");
        s.executeUpdate("DROP TABLE APP.LOG");
        s.executeUpdate("DROP TABLE APP.NAMES");
    }

    private static void checkNumRows(ResultSet rs, int expectedCount) throws SQLException {

	int rowcount = 0;
	while (rs.next()) {
	    rowcount++;
	}
	
	if (rowcount == expectedCount) {
	    System.out.println("PASS: Got expected row count:" + rowcount);
	}
	else {	    
	    System.out.println("FAILED: Expected " + expectedCount + " rows but got " +  rowcount);
	}
                
    }

}
