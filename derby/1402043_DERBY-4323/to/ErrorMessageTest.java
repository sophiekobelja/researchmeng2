/*
 * Class org.apache.derbyTesting.functionTests.tests.lang.ErrorMessageTest
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

package org.apache.derbyTesting.functionTests.tests.lang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.derbyTesting.functionTests.util.Barrier;
import org.apache.derbyTesting.junit.BaseJDBCTestCase;
import org.apache.derbyTesting.junit.CleanDatabaseTestSetup;
import org.apache.derbyTesting.junit.DatabasePropertyTestSetup;
import org.apache.derbyTesting.junit.JDBC;

public class ErrorMessageTest extends BaseJDBCTestCase {
    public ErrorMessageTest(String name) {
        super(name);
    }

    /**
     * Create a test suite with all the tests in this class. The tests are only
     * run in embedded mode since they test the messages generated by the
     * embedded driver.
     */
    public static Test suite() {

        if (JDBC.vmSupportsJSR169()) {
            // Foundation 1.1 doesn't support the regex classes. Return an
            // empty test suite.
            return new TestSuite("ErrorMessageTest");
        }

        Test test = new TestSuite(ErrorMessageTest.class, "ErrorMessageTest");
        // create some data to work on
        test = new CleanDatabaseTestSetup(test) {
            protected void decorateSQL(Statement s) throws SQLException {
                s.executeUpdate("create table t (id int primary key, " +
                                "text varchar(10))");
                s.executeUpdate("insert into t (id) values 1, 2");
            }
        };
        Properties prop = new Properties();
        // set timeouts so that the tests finish sooner
        prop.setProperty("derby.locks.waitTimeout", "4");
        prop.setProperty("derby.locks.deadlockTimeout", "2");
        // make sure lock table is dumped on wait timeout
        prop.setProperty("derby.locks.deadlockTrace", "true");
        test = new DatabasePropertyTestSetup(test, prop);
        return test;
    }

    /**
     * Test that a wait timeout prints the lock table correctly when the
     * <code>derby.locks.deadlockTrace</code> property is set. DERBY-2817
     *
     * After fix for DERBY-5564, the sql state for a lock timeout will be
     * the same whether diagnostics are on or not (ie. 40XL1).  
     */
    public void testWaitTimeout() throws SQLException {
        getConnection().setAutoCommit(false);
        Statement s = createStatement();
        assertUpdateCount(s, 1, "update t set text='xxx' where id=1");
        Connection c2 = openDefaultConnection();
        Statement s2 = c2.createStatement();
        try {
            // the first transaction has locked row with id=1, so this query
            // will time out
            JDBC.assertDrainResults(
                    s2.executeQuery("select * from t where id=1"));
            fail("Expected lock timeout");
        } catch (SQLException e) {
            assertSQLState("Not a timeout", "40XL1", e);

            // check that information about the victim is printed
            String[] msg = e.getMessage().split("\n");
            assertEquals("*** The following row is the victim ***", msg[4]);
            assertEquals("*** The above row is the victim ***", msg[6]);
            String[] victim = msg[5].split(" *\\|");
            assertTrue("Invalid XID string: " + victim[0],
                       victim[0].matches("\\d+"));
            assertEquals("Victim should be a row lock", "ROW", victim[1]);
            assertEquals("Victim should be a shared lock", "S", victim[2]);
            assertEquals("Victim should be waiting", "WAIT", victim[5]);

            // check that the rest of the lock table is dumped
            boolean locksDumped = false;
            for (int i = 7; i < msg.length - 1; i++) {
                String[] tokens = msg[i].split(" *\\|");
                assertTrue("Invalid XID string: " + tokens[0],
                           tokens[0].matches("\\d+"));
                assertTrue("Unexpected lock type: " + tokens[1],
                           tokens[1].matches("ROW|TABLE"));
                assertTrue("Unexpected lock mode: " + tokens[2],
                           tokens[2].matches("S|X|IX|IS"));
                assertEquals("Expected lock to be granted", "GRANT", tokens[5]);
                locksDumped = true;
            }
            assertTrue("No locks dumped", locksDumped);
        }
        s.close();
        s2.close();
        c2.close();
    }

    /**
     * Test that the error message from a deadlock timeout contains information
     * about the locks involved in the deadlock. DERBY-2817
     */
    public void testDeadlockTimeout()
            throws SQLException, InterruptedException {
        setAutoCommit(false);

        // Make the main transaction (T1) lock row 1 exclusively
        Statement s = createStatement();
        assertUpdateCount(s, 1, "update t set text='xxx' where id=1");

        // Start another transaction (T2) that locks row 2 exclusively
        Connection c2 = openDefaultConnection();
        c2.setAutoCommit(false);
        Statement s2 = c2.createStatement();
        assertUpdateCount(s2, 1, "update t set text='yyy' where id=2");

        // Prepare statements for T1 to lock row 2 (shared), and for T2 to
        // lock row 1 (shared).
        PreparedStatement ps1 = prepareStatement("select * from t where id=2");
        final PreparedStatement ps2 =
                c2.prepareStatement("select * from t where id=1");

        // Create a barrier for the two threads to synchronize.
        final Barrier barrier = new Barrier(2);

        final SQLException[] holder = new SQLException[2];
        final Throwable[] unexpected = new Throwable[1];
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        // Let the main thread know the helper thread has
                        // started. The race for the locks can start.
                        barrier.await();

                        // This statement will be blocked because T1 holds
                        // an exclusive lock on the row we want.
                        JDBC.assertDrainResults(ps2.executeQuery());
                    } catch (SQLException e) {
                        holder[0] = e;
                    } catch (Throwable t) {
                        unexpected[0] = t;
                    }
                }
            });
        t.start();

        // Wait until the helper thread has started. Once the call returns,
        // both threads are ready, and the race for the locks can start.
        barrier.await();

        // This statement will be blocked because T2 holds an exclusive lock
        // on the row we want. So now we have T1 waiting for T2, and T2 waiting
        // for T1, and one of the transactions should be terminated because of
        // the deadlock.
        try {
            JDBC.assertDrainResults(ps1.executeQuery());
        } catch (SQLException e) {
            holder[1] = e;
        }

        // Wait for the helper thread to complete.
        t.join();

        // If the helper thread failed with something other than an
        // SQLException, report it.
        if (unexpected[0] != null) {
            fail("Helper thread failed unexpectedly", unexpected[0]);
        }

        // Check that exactly one of the threads failed, and that the failure
        // was caused by a deadlock. It is not deterministic which of the two
        // threads will be terminated.
        String msg;
        if (holder[0] != null) {
            assertSQLState("Not a deadlock", "40001", holder[0]);
            assertNull("Only one of the waiters should be aborted", holder[1]);
            msg = holder[0].getMessage();
        } else {
            assertSQLState("Not a deadlock", "40001", holder[1]);
            msg = holder[1].getMessage();
        }

        String[] lines = msg.split("\n");
        assertEquals("Unexpected number of lines in message", 8, lines.length);

        Pattern[] patterns = new Pattern[] {
            Pattern.compile("Lock : ROW, T, \\(\\d+,\\d+\\)"),
            Pattern.compile(" *Waiting XID : \\{\\d+, S\\} , APP, " +
                            "select \\* from t where id=(1|2)"),
            Pattern.compile(" *Granted XID : \\{\\d+, X\\} *"),
        };

        // check the descriptions of the two locks involved in the deadlock
        for (int i = 0; i < patterns.length * 2; i++) {
            String line = lines[i+1];
            Matcher m = patterns[i%patterns.length].matcher(line);
            assertTrue("mismatch: " + line, m.matches());
        }

        s.close();
        s2.close();
        c2.rollback();
        c2.close();
    }
}
