/*

  Derby - Class org.apache.derbyTesting.functionTests.tests.lang.RoutinesDefinersRightsTest

  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to you under the Apache License, Version 2.0
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

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.derbyTesting.junit.BaseJDBCTestCase;
import org.apache.derbyTesting.junit.DatabasePropertyTestSetup;
import org.apache.derbyTesting.junit.TestConfiguration;
import org.apache.derbyTesting.junit.CleanDatabaseTestSetup;
import org.apache.derbyTesting.junit.JDBC;

/**
 * Test that routines declared with EXTERNAL SECURITY DEFINER/INVOKER behaves
 * correctly. In addition to tests found here, see also the test in
 * org.apache.derbyTesting.functionTests.tests.upgradeTests.
 * Changes10_7#testExternalSecuritySpecification, which checks that the feature
 * will not work if SQL authorization is not enabled.
 *
 */
public class RoutinesDefinersRightsTest extends BaseJDBCTestCase
{
    private final static String pwSuffix = "pwSuffix";

    /* SQL states */
    private final static String NOEXECUTEPERMISSION  = "42504";
    private final static String NOCOLUMNPERMISSION = "42502";
    private final static String MULTIPLE_ELEMENTS = "42613";

    // job categories
    private final static int BOSS = 0;
    private final static int MIDDLEMANAGER = 1;
    private final static int FOOTSOLDIER = 2;


    private final static String[] users = {"test_dbo", "PHB", "Dilbert"};
    private final static double[] wages = {1000.0, 500.0, 100.0};
    private final static int[] categories = {BOSS, MIDDLEMANAGER, FOOTSOLDIER};
    /**
     * Create a new instance of RoutinesDefinersRightsTest.
     *
     * @param name Fixture name
     */
    public RoutinesDefinersRightsTest(String name)
    {
        super(name);
    }


    /**
     * Construct top level suite in this JUnit test
     *
     * @return A suite containing embedded and client suites.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("RoutinesDefinersRightsTest");

        if (!JDBC.vmSupportsJSR169()) {
            // JSR169 cannot run with tests with stored procedures
            // that do database access - for they require a
            // DriverManager connection to jdbc:default:connection;
            // DriverManager is not supported with JSR169.
            suite.addTest(makeSuite());

            // suite.addTest(
            //     TestConfiguration.clientServerDecorator(makeSuite()));
        }


        return suite;
    }

    /**
     * Construct suite of tests
     *
     * @return A suite containing the test cases.
     */
    private static Test makeSuite()
    {
        /* Tests running with sql authorization set.  First decorate
         * with clean database, then with authentication +
         * sqlAuthorization.
         */
        Test clean = new CleanDatabaseTestSetup(
            new TestSuite(RoutinesDefinersRightsTest.class)) {
                protected void decorateSQL(Statement s)
                        throws SQLException {

                    s.execute("create role middleManager");
                    s.execute("create table s1.wages(employeeId int," +
                              "                   category int," +
                              "                   salary double," +
                              "                   name VARCHAR(20))");
                    PreparedStatement ps = s.getConnection().prepareStatement(
                        "insert into s1.wages values (?,?,?,?)");

                    for (int i=BOSS; i <= FOOTSOLDIER; i++) {
                        ps.setInt(1, 1000 + i);
                        ps.setInt(2, i);
                        ps.setDouble(3, wages[i]);
                        ps.setString(4, users[i]);
                        ps.execute();
                    }

                    ps.close();

                    s.execute
                        ("create function s1.lookupWageFootSoldier(int) " +
                         "returns double " +
                         "language java parameter style java external name " +
                         "'org.apache.derbyTesting.functionTests.tests.lang." +
                         "RoutinesDefinersRightsTest.lookupWageFootSoldier' " +
                         "EXTERNAL SECURITY DEFINER " +
                         "reads sql data called on null input");

                    s.execute
                        ("grant execute on function s1.lookupWageFootSoldier " +
                         "   to phb");

                    s.execute
                        ("create function s1.lookupWageFootSoldierI(int) " +
                         "returns double " +
                         "language java parameter style java external name " +
                         "'org.apache.derbyTesting.functionTests.tests.lang." +
                         "RoutinesDefinersRightsTest.lookupWageFootSoldier' " +
                         "EXTERNAL SECURITY INVOKER " +
                         "reads sql data called on null input");

                    s.execute
                        ("grant execute on function " +
                         "    s1.lookupWageFootSoldierI to phb");

                    s.execute
                        ("create procedure s1.spTestBuiltins() " +
                         "language java parameter style java external name " +
                         "'org.apache.derbyTesting.functionTests.tests.lang." +
                         "RoutinesDefinersRightsTest.spTestBuiltins' " +
                         "EXTERNAL SECURITY DEFINER " +
                         "reads sql data");

                    s.execute
                        ("grant execute on procedure s1.spTestBuiltins " +
                         "   to phb");

                    // Table function
                    s.execute
                        ("create function s1.selectFootSoldiers() " +
                         "returns table (" +
                         "    employeeId int," +
                         "    category int," +
                         "    salary double," +
                         "    name varchar(20))" +
                         "language java parameter style " +
                         "    derby_jdbc_result_set " +
                         "external name " +
                         "'org.apache.derbyTesting.functionTests.tests.lang." +
                         "RoutinesDefinersRightsTest.selectFootSoldiers' " +
                         "EXTERNAL SECURITY DEFINER " +
                         "reads sql data");

                    // PHB needs to set his role to be able to use
                    // s1.selectFootSoldiers. Just to vary a bit..
                    s.execute
                        ("grant execute on function s1.selectFootSoldiers " +
                         "    to middleManager");
                    s.execute
                        ("grant middleManager to phb");

                    s.execute
                        ("create function s1.selectFootSoldiersI() " +
                         "returns table (" +
                         "    employeeId int," +
                         "    category int," +
                         "    salary double," +
                         "    name varchar(20))" +
                         "language java parameter style " +
                         "    derby_jdbc_result_set " +
                         "external name " +
                         "'org.apache.derbyTesting.functionTests.tests.lang." +
                         "RoutinesDefinersRightsTest.selectFootSoldiers' " +
                         "EXTERNAL SECURITY INVOKER " +
                         "reads sql data");
                    s.execute
                        ("grant execute on function s1.selectFootSoldiersI " +
                         "    to middleManager");

                    s.execute
                        ("create schema phb authorization phb");
                    s.execute
                        ("create function phb.lookupDilbertWage() " +
                         "returns double " +
                         "language java parameter style java external name " +
                         "'org.apache.derbyTesting.functionTests.tests.lang." +
                         "RoutinesDefinersRightsTest.lookupDilbertWage' " +
                         "EXTERNAL SECURITY DEFINER " +
                         "reads sql data called on null input");
                    s.execute
                        ("grant execute on function phb.lookupDilbertWage " +
                         "    to dilbert");

                    // Check confliciting clauses
                    assertStatementError(
                        new String[]{MULTIPLE_ELEMENTS},
                        s,
                        "create function phb.lookupDilbertWage() " +
                        "returns double " +
                        "language java parameter style java external name " +
                        "'org.apache.derbyTesting.functionTests.tests.lang." +
                        "RoutinesDefinersRightsTest.lookupDilbertWage' " +
                        "EXTERNAL SECURITY DEFINER " +
                        "EXTERNAL SECURITY INVOKER " +
                        "reads sql data called on null input");
                }
            };

        return
            TestConfiguration.sqlAuthorizationDecorator(
                    DatabasePropertyTestSetup.builtinAuthentication(
                        clean, users, pwSuffix));
    }


    public void testDefinersRights() throws SQLException
    {
        Connection c = null;
        ResultSet rs = null;
        Statement stm = null;

        // Shut down database, just so we are sure the routine info is
        // persisted into the on-disk dictionary correctly before we try to use
        // it.
        TestConfiguration.getCurrent().shutdownDatabase();

        // Try as vainly as dilbert to execute lookupWageFootSoldier
        c = openUserConnection("Dilbert");
        stm = c.createStatement();
        assertStatementError(
            new String[]{NOEXECUTEPERMISSION},
            stm,
            "values s1.lookupWageFootSoldier(1002)");
        stm.close();
        c.close();

        // Try as PHB
        c = openUserConnection("PHB");
        stm = c.createStatement();
        rs = stm.executeQuery("values s1.lookupWageFootSoldier(1002)");
        JDBC.assertSingleValueResultSet(rs, "100.0");
        stm.close();
        c.close();

        // dbo can execute lookupWageFootSoldier, since he has select
        // privileges on the wages table.
        Connection dboConn = getConnection();
        Statement dbos = dboConn.createStatement();

        rs = dbos.executeQuery("values s1.lookupWageFootSoldier(1002)");
        JDBC.assertSingleValueResultSet(rs, "100.0");

        // We see that the method will not return wages for anything but foot
        // soldiers, even for the dbo:
        rs = dbos.executeQuery("values s1.lookupWageFootSoldier(1001)");
        JDBC.assertSingleValueResultSet(rs, "-1.0");

        dbos.close();
        dboConn.close();

        // ----------------------------------------
        // Now try with a table function

        c = openUserConnection("PHB");

        stm = c.createStatement();
        stm.executeUpdate("set role middlemanager");
        rs = stm.executeQuery(
            "select * from table (s1.selectFootSoldiers()) as t");
        JDBC.assertFullResultSet(
            rs,
            new String[][]{{"1002", "2", "100.0", "Dilbert"}});

        // Check that user and role is unchanged after call
        rs = stm.executeQuery(
            "values (current_user, session_user, current_role)");
        JDBC.assertFullResultSet(
            rs,
            new String[][]{{"PHB", "PHB", "\"MIDDLEMANAGER\""}});

        stm.close();
        c.close();

        // ------------------------------------
        // Try with two levels of routines with different definers
        c = openUserConnection("Dilbert");
        stm = c.createStatement();
        rs = stm.executeQuery("values phb.lookupDilbertWage()");
        JDBC.assertSingleValueResultSet(rs, "100.0");

        // sanity when we return
        rs = stm.executeQuery(
            "values (current_user, session_user, current_role)");
        JDBC.assertFullResultSet(
            rs,
            new String[][]{{"DILBERT", "DILBERT", null}});

        stm.close();
        c.close();

    }

    public void testBuiltins() throws SQLException{
        Connection c = openUserConnection("PHB");
        Statement stm = c.createStatement();
        stm.executeUpdate("set role middlemanager");
        stm.executeUpdate("call s1.spTestBuiltins()");
        stm.close();
        c.close();
    }


    public void testInvokersRights() throws SQLException{
        Connection c = null;
        ResultSet rs = null;
        Statement stm = null;

        // Try as vainly as dilbert to execute lookupWageFootSoldier
        c = openUserConnection("Dilbert");
        stm = c.createStatement();
        assertStatementError(
            new String[]{NOEXECUTEPERMISSION},
            stm,
            "values s1.lookupWageFootSoldierI(1002)");
        stm.close();
        c.close();

        // Try as PHB, vainly too, since no definer's rights now.
        c = openUserConnection("PHB");
        stm = c.createStatement();
        assertStatementError(
            new String[]{NOCOLUMNPERMISSION},
            stm,
            "values s1.lookupWageFootSoldierI(1002)");
        stm.close();
        c.close();

        // dbo can execute lookupWageFootSoldier, since she has select
        // privileges on the wages table.
        Connection dboConn = getConnection();
        Statement dbos = dboConn.createStatement();

        rs = dbos.executeQuery("values s1.lookupWageFootSoldierI(1002)");
        JDBC.assertSingleValueResultSet(rs, "100.0");

        dbos.close();
        dboConn.close();

        // ----------------------------------------
        // Now try with a table function

        c = openUserConnection("PHB");

        stm = c.createStatement();
        stm.executeUpdate("set role middlemanager");
        assertStatementError(
            new String[]{NOCOLUMNPERMISSION},
            stm,
            "select * from table (s1.selectFootSoldiersI()) as t");

        // Check that user and role is unchanged after failed call
        rs = stm.executeQuery(
            "values (current_user, session_user, current_role)");
        JDBC.assertFullResultSet(
            rs,
            new String[][]{{"PHB", "PHB", "\"MIDDLEMANAGER\""}});

        stm.close();
        c.close();

    }

    public static double lookupWageFootSoldier(int employee)
            throws SQLException
    {
        Connection c = null;

        c = DriverManager.getConnection("jdbc:default:connection");
        Statement cStmt = c.createStatement();

        // Can only look up wages of foot soldiers
        ResultSet rs = cStmt.executeQuery(
            "select salary from s1.wages where employeeId=" +
            employee + " and category = " + FOOTSOLDIER);

        if (rs.next()) {
            double result = rs.getDouble(1);
            cStmt.close();
            rs.close();
            c.close();
            return result;
        } else {
            cStmt.close();
            rs.close();
            c.close();
            return -1.0;
        }
    }

    public static ResultSet selectFootSoldiers()
            throws SQLException
    {
        Connection c = null;

        c = DriverManager.getConnection("jdbc:default:connection");
        Statement cStmt = c.createStatement();

        // Can only look up wages of foot soldiers
        ResultSet rs = cStmt.executeQuery(
            "select * from s1.wages where category = " +
            FOOTSOLDIER);

        return rs;
    }


    public static double lookupDilbertWage()
            throws SQLException
    {
        Connection c = null;

        c = DriverManager.getConnection("jdbc:default:connection");
        Statement stm = c.createStatement();

        // We are executing as middle manager so we can look up Dilberts wage
        // for him.
        c.commit(); // Need to be no transaction active when we set role
        stm.executeUpdate("set role middlemanager");

        // Check that definer is PHB even though we created function as dbo.
        ResultSet rs = stm.executeQuery(
            "values (current_user, session_user, current_role)");

        JDBC.assertFullResultSet(
            rs,
            new String[][]{{"PHB", "DILBERT", "\"MIDDLEMANAGER\""}});

        rs = stm.executeQuery(
            "select salary from table (s1.selectFootSoldiers()) as t " +
            "    where name='Dilbert'");
        rs.next();
        double result = rs.getDouble(1);

        rs.close();
        stm.close();
        c.close();

        return result;
    }


    public static void spTestBuiltins()
            throws SQLException
    {
        Connection c = null;

        c = DriverManager.getConnection("jdbc:default:connection");
        Statement cStmt = c.createStatement();

        ResultSet rs = cStmt.executeQuery(
            "values (user, current_user, current schema, " +
            "        session_user, current_role)");
        JDBC.assertFullResultSet(
            rs,
            new String[][]{{"TEST_DBO", "TEST_DBO", "TEST_DBO", "PHB", null}});

        cStmt.close();
        c.close();
    }
}

