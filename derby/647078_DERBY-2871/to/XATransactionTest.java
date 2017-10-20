/*

   Derby - Class org.apache.derbyTesting.functionTests.tests.jdbcapi.XATransactionTest

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

package org.apache.derbyTesting.functionTests.tests.jdbcapi;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.derby.client.ClientXid;
import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.shared.common.reference.SQLState;

import org.apache.derbyTesting.junit.DatabasePropertyTestSetup;
import org.apache.derbyTesting.junit.JDBC;
import org.apache.derbyTesting.junit.J2EEDataSource;
import org.apache.derbyTesting.junit.BaseJDBCTestCase;
import org.apache.derbyTesting.junit.TestConfiguration;

/** The test of the properties of the XA transaction interface implementation.
  */
public class XATransactionTest extends BaseJDBCTestCase {

    /** Tests whether it is possible to reconstruct the original Xid value
      * correctly from SYSCS_DIAG.TRANSACTION_TABLE. */
    public void testGlobalXIDinTransactionTable() throws Exception {
        Statement stm = getConnection().createStatement();
        stm.execute("create table XATT2 (i int, text char(10))");

        XADataSource xaDataSource = J2EEDataSource.getXADataSource();
        XAConnection xaConn = xaDataSource.getXAConnection();
        XAResource xaRes = xaConn.getXAResource();
        Connection conn = xaConn.getConnection();

        // create large enough xid
        byte[] gid = new byte[64];
        byte[] bid = new byte[64];
        for (int i=0; i < 64; i++) {
            gid[i] = (byte) i;
            bid[i] = (byte) (64 - i);
        }
        Xid xid = new ClientXid(0x1234, gid, bid);

        // get the stuff required to execute the global transaction
        xaConn = xaDataSource.getXAConnection();
        xaRes = xaConn.getXAResource();
        conn = xaConn.getConnection();

        // start the transaction with that xid
        xaRes.start(xid, XAResource.TMNOFLAGS);

        // do some work
        stm = conn.createStatement();
        stm.execute("insert into XATT2 values (1234, 'Test_Entry')");
        stm.close();

        // end the wotk on the transaction branch
        xaRes.end(xid, XAResource.TMSUCCESS);

        ResultSet rs = null;
        stm = null;

        try {
            // check the output of the global xid in 
            // syscs_diag.transaction_table
            stm = getConnection().createStatement();

            String query = "select global_xid from syscs_diag.transaction_table"
                         + " where global_xid is not null";

            // execute the query to obtain the xid of the global transaction
            rs = stm.executeQuery(query);

            // there should be at least one globaltransaction in progress
            assertTrue(rs.next());

            // check whether the xid obtained matches the original xid
            Xid rXid = parseXid(rs.getString(1));
            assertEquals(xid, rXid);

            // there should be at most one global transaction in progress
            assertFalse(rs.next());

        } catch (Exception ex) {
            try {
                // close all the stuff
                if (rs != null)
                    rs.close();
                if (stm != null)
                    stm.close();

                // rollback the global transaction
                xaRes.rollback(xid);
                // close the connection
                xaConn.close();
            } catch (Exception e) {
                // ignore the exception because it
                // would hide the original exception
            }
            // throw the stuff further
            throw ex;
        }

        // close all the stuff
        rs.close();
        stm.close();

        // rollback the global transaction
        xaRes.rollback(xid);

        // close the connection
        xaConn.close();
    }


    /** Tests the functionality of the XA transaction timeout.
      * <p>
      * It executes 66 global transactions during the test. Everyone
      * of them just inserts a row into XATT table. The rows inserted
      * by the transactions are different. Some of these transactions
      * are committed and some of them are left in different stages.
      * The stage of the transaction in which it is left is chosed
      * depending on division remainders.
      * </p>
      * <p>
      * After finishing these 1000 transactions a select statement is executed
      * on that table. However, if there are still some unfinished transactions
      * that were not aborted they will hold a lock on a XATT table until they
      * will get rolled back by the transaction timeout. The number of rows
      * in the XATT table is calculated. It is then compared with the excepted
      * number of rows (the transaction we know we have committed).
      * </p>
      * <p>
      * The call to xaRes.setTransactionTimeout(5) before the call
      * to xaRes.start() makes the transactions to be rolled back
      * due to timeout.
      * </p>
      */
    public void testXATransactionTimeout() throws Exception {

        /* The number of statements to execute in timeout related test. */
        int timeoutStatementsToExecute = 66;

        /* Specifies the number of total executed statements per one
           commited statement in timeout related test. */
        int timeoutCommitEveryStatement = 3;

        /* Specifies the number of statements that should be commited
           during a timeout related test. */
        int timeoutStatementsCommitted
            = (timeoutStatementsToExecute + timeoutCommitEveryStatement - 1)
                / timeoutCommitEveryStatement;

        Statement stm = getConnection().createStatement();
        stm.execute("create table XATT (i int, text char(10))");

        XADataSource xaDataSource = J2EEDataSource.getXADataSource();
        XAConnection[] xaConn = new XAConnection[timeoutStatementsToExecute];
        XAResource xaRes = null;
        Connection conn = null;

        for (int i=0; i < timeoutStatementsToExecute; i++) {
            xaConn[i] = xaDataSource.getXAConnection();
            xaRes = xaConn[i].getXAResource();
            conn = xaConn[i].getConnection();

            Xid xid = createXid(123, i);
            xaRes.setTransactionTimeout(8);
            xaRes.start(xid, XAResource.TMNOFLAGS);

            stm = conn.createStatement();
            stm.execute("insert into XATT values (" + i + ", 'Test_Entry')");

            if (i % timeoutCommitEveryStatement == 0) {
                stm.close();
                xaRes.end(xid, XAResource.TMSUCCESS);
                xaRes.prepare(xid);
                xaRes.commit(xid, false);
            } else if (i % 11 != 0) {
                // check the tiemout for transactions disassociated
                // with failure.
                try {
                    xaRes.end(xid, XAResource.TMFAIL);
                    fail();
                } catch (XAException ex) {
                    if (ex.errorCode < XAException.XA_RBBASE
                        || ex.errorCode > XAException.XA_RBEND)
                    {
                        throw ex;
                    }
                }
                stm.close();
            } else if (i % 2 == 0) {
                // check the timeout for transactions disassociated
                // with success.
                xaRes.end(xid, XAResource.TMSUCCESS);
                stm.close();
            } 
        }

        ResultSet rs = null;

        stm = getConnection().createStatement();
        rs = stm.executeQuery("select count(*) from XATT");
        rs.next();

        // Check whether the correct number of transactions
        // was rolled back
        assertTrue(rs.getInt(1) == timeoutStatementsCommitted);

        // test the timeout during the statement run
        XAConnection xaConn2 = xaDataSource.getXAConnection();
        xaRes = xaConn2.getXAResource();
        conn = xaConn2.getConnection();

        Xid xid = createXid(124, 100);
        xaRes.setTransactionTimeout(10);
        xaRes.start(xid, XAResource.TMNOFLAGS);

        stm = conn.createStatement();

        // Check whether the statement was correctly timed out
        // and the appropriate exception was thrown
        try {
            // Run this kind of statement just to be sure
            // it will not finish before it will time out
            rs = stm.executeQuery(
                 "select count(*) from sys.syscolumns a, sys.syscolumns b, "
               + "sys.syscolumns c, sys.syscolumns d, sys.syscolumns e "
               + "group by a.referenceid, b.referenceid, c.referenceid, "
               + "d.referenceid");
            fail("An exception is expected here");
        } catch (SQLException ex) {
            // Check the sql state of the thrown exception
            assertSQLState(
                SQLState.LANG_STATEMENT_CANCELLED_OR_TIMED_OUT.substring(0,5),
                ex);
        }

        // perform a select on the table just to be sure that all
        // the transactions were rolled back.
        stm = getConnection().createStatement();
        rs = stm.executeQuery("select count(*) from XATT");
        rs.next();

        // Go throught the XA Connections just to be sure that no code
        // optimization would garbage collect them before (and thus
        // the transactions might get rolled back by a different
        // code).
        for (int i=0; i < timeoutStatementsToExecute; i++) {
            assertNotNull(xaConn[i]);
            xaConn[i].close();
        }

        // Again, check whether the correct number of transactions
        // was rolled back
        assertTrue(rs.getInt(1) == timeoutStatementsCommitted);
    }


    /* ------------------- end helper methods  -------------------------- */

    /** Create the Xid object for global transaction identification
      * with the specified identification values.
      * @param gtrid Global Transaction ID
      * @param bqual Branch Qualifier
      */
    static Xid createXid(int gtrid, int bqual) throws XAException {
        byte[] gid = new byte[2]; gid[0]= (byte) (gtrid % 256); gid[1]= (byte) (gtrid / 256);
        byte[] bid = new byte[2]; bid[0]= (byte) (bqual % 256); bid[1]= (byte) (bqual / 256);
        Xid xid = new ClientXid(0x1234, gid, bid);
        return xid;
    }

    /** Parses the xid value from the string. The format of the input string is
      * the same as the global_xid column in syscs_diag.transaction_table table -
      * '(formatid_in_dec,global_transaction_id_in_hex,branch_qualifier_in_hex)'
      * @param str Global Transaction ID converted to a string.
      * @return The xid object corresponding to the xid specified in a string.
      */
    private static Xid parseXid(String str) {
        assertNotNull(str);
        assertTrue(str.matches("\\(\\p{Digit}+,\\p{XDigit}+,\\p{XDigit}+\\)"));

        String formatIdS = str.substring(1, str.indexOf(','));
        String gtidS = str.substring(str.indexOf(',')+1, str.lastIndexOf(','));
        String bqualS = str.substring(str.lastIndexOf(',')+1, str.length()-1);

        assertTrue(gtidS.length() % 2 == 0);
        assertTrue(bqualS.length() % 2 == 0);

        int fmtid = Integer.parseInt(formatIdS);
        byte[] gtid = new byte[gtidS.length()/2];
        byte[] bqual = new byte[bqualS.length()/2];

        for (int i=0; i < gtid.length; i++) {
            gtid[i] = (byte) Integer.parseInt(gtidS.substring(2*i, 2*i + 2), 16);
        }

        for (int i=0; i < bqual.length; i++) {
            bqual[i] = (byte) Integer.parseInt(bqualS.substring(2*i, 2*i + 2), 16);
        }

        // Using ClientXid is ok also for embedded driver
        // since it does not contain any related code
        // and there is no implementation of Xid iterface
        // for embedded driver
        return new ClientXid(fmtid, gtid, bqual);
    }

    public XATransactionTest(String name) {
        super(name);
    }

    public static Test suite() {
        // the test requires XADataSource to run
        if (JDBC.vmSupportsJDBC3()) {
            Test test = TestConfiguration.defaultSuite(XATransactionTest.class);
            // Set the lock timeout back to the default, because when
            // running in a bigger suite the value may have been
            // altered by an earlier test
            test = DatabasePropertyTestSetup.setLockTimeouts(test, 20, 60);
            return test;
        }

        return new TestSuite("XATransactionTest cannot run without XA support");
    }
}
