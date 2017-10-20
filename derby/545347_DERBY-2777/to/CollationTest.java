/**
 *  Derby - Class org.apache.derbyTesting.functionTests.tests.lang.CollationTest
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import junit.framework.Test;

import org.apache.derbyTesting.junit.XML;
//import org.apache.derby.iapi.types.XML;
import org.apache.derbyTesting.junit.BaseJDBCTestCase;
import org.apache.derbyTesting.junit.JDBC;
import org.apache.derbyTesting.junit.JDBCDataSource;
import org.apache.derbyTesting.junit.TestConfiguration;

public class CollationTest extends BaseJDBCTestCase {

	/*
	 * ToDo test cases
	 * 1)Use a parameter as cast operand and cast that to character type. The
	 * resultant type should get it's collation from the compilation schema
	 * 2)Test conditional if (NULLIF and CASE) with different datatypes to see
	 * how casting works. The compile node for this SQL construct seems to be
	 * dealing with lot of casting code (ConditionalNode)
	 * 3)When doing concatenation testing, check what happens if concatantion
	 * is between non-char types. This is because ConcatenationOperatorNode
	 * in compile package has following comment "If either the left or right 
	 * operands are non-string, non-bit types, then we generate an implicit 
	 * cast to VARCHAR."
	 * 4)Do testing with upper and lower
	 * 5)It looks like node for LIKE ESCAPE which is LikeEscapeOperatorNode
	 * also uses quite a bit of casting. Should include test for LIKE ESCAPE
	 * which will trigger the casting.
	 * 6)Binary arithmetic operators do casting if one of the operands is
	 * string and other is numeric. Test that combination
	 * 7)Looks like import utility does casting (in ColumnInfo class). See
	 * if any testing is required for that.
	 * 8)Do testing with UNION and use the results of UNION in collation
	 * comparison (if there is something like that possible. I didn't put too
	 * much thought into it but wanted to list here so we can do the required
	 * testing if needed).
	 */
    public CollationTest(String name) {
        super(name);
    }
    
    private static final String[] NAMES =
    {
            // Just Smith, Zebra, Acorn with alternate A,S and Z
            "Smith",
            "Zebra",
            "\u0104corn",
            "\u017Bebra",
            "Acorn",
            "\u015Amith",
            "aacorn",
    };
    
  /**
   * Test order by with default collation
   * 
   * @throws SQLException
   */
public void testDefaultCollation() throws SQLException {
      DataSource ds = JDBCDataSource.getDataSourceLogical("defaultdb");
      JDBCDataSource.setBeanProperty(ds, "connectionAttributes", 
                  "create=true");

      
      Connection conn = ds.getConnection();
      conn.setAutoCommit(false);
      Statement s = conn.createStatement();
      PreparedStatement ps;
      ResultSet rs;
      
      setUpTable(s);

      //The collation should be UCS_BASIC for this database
      checkLangBasedQuery(s, 
      		"VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY('derby.database.collation')",
			new String[][] {{"UCS_BASIC"}});

      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER ORDER BY NAME",
      		new String[][] {{"4","Acorn"},{"0","Smith"},{"1","Zebra"},
      		{"6","aacorn"}, {"2","\u0104corn"},{"5","\u015Amith"},{"3","\u017Bebra"} });   

      //COMPARISONS INVOLVING CONSTANTS
      //In default JVM territory, 'aacorn' is != 'Acorn'
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER where 'aacorn' = 'Acorn' ",
      		null);
      //In default JVM territory, 'aacorn' is not < 'Acorn'
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER where 'aacorn' < 'Acorn' ",
      		null);

      //COMPARISONS INVOLVING CONSTANT and PERSISTENT COLUMN
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER WHERE NAME <= 'Smith' ",
      		new String[][] {{"0","Smith"}, {"4","Acorn"} });   
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER WHERE NAME between 'Acorn' and 'Zebra' ",
      		new String[][] {{"0","Smith"}, {"1","Zebra"}, {"4","Acorn"} });
      //After index creation, the query above will return same data but in 
      //different order
      /*s.executeUpdate("CREATE INDEX CUSTOMER_INDEX1 ON CUSTOMER(NAME)");
      s.executeUpdate("INSERT INTO CUSTOMER VALUES (NULL, NULL)");
      checkLangBasedQuery(s, 
      		"SELECT ID, NAME FROM CUSTOMER WHERE NAME between 'Acorn' and " +
			" 'Zebra' ORDER BY NAME",
      		new String[][] {{"4","Acorn"}, {"0","Smith"}, {"1","Zebra"} });
*/
      //For non-collated databases, COMPARISONS OF USER PERSISTENT CHARACTER 
      //COLUMN AND CHARACTER CONSTANT WILL not FAIL IN SYSTEM SCHEMA.
      s.executeUpdate("set schema SYS");
      checkLangBasedQuery(s, "SELECT ID, NAME FROM APP.CUSTOMER WHERE NAME <= 'Smith' ",
      		new String[][] {{"0","Smith"}, {"4","Acorn"} });   

      s.executeUpdate("set schema APP");
      //Following sql will not fail in a database which uses UCS_BASIC for
      //user schemas. Since the collation of user schemas match that of system
      //schema, the following comparison will not fail. It will fail in a 
      //database with territory based collation for user schemas. 
      checkLangBasedQuery(s, "SELECT 1 FROM SYS.SYSTABLES WHERE " +
      		" TABLENAME = 'CUSTOMER' ",
      		new String[][] {{"1"} });    
      //Using cast for persistent character column from system table in the
      //query above won't affect the above sql in any ways. 
      checkLangBasedQuery(s, "SELECT 1 FROM SYS.SYSTABLES WHERE CAST " +
      		" (TABLENAME AS CHAR(15)) = 'CUSTOMER' ",
      		new String[][] {{"1"} });   

      //Do some testing using CASE WHEN THEN ELSE
      //following will work with no problem for a database with UCS_BASIC
      //collation for system and user schemas
      checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE CASE " +
      		" WHEN 1=1 THEN TABLENAME ELSE 'c' END = 'SYSCOLUMNS'",
      		new String[][] {{"SYSCOLUMNS"} });   
      //Using cast for result of CASE expression in the query above would not
      //affect the sql in any ways. 
      checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE CAST " +
      		" ((CASE WHEN 1=1 THEN TABLENAME ELSE 'c' END) AS CHAR(12)) = " +
			" 'SYSCOLUMNS'",
      		new String[][] {{"SYSCOLUMNS"} });   

      //Do some testing using CONCATENATION
      //following will work with no problem for a database with UCS_BASIC
      //collation for system and user schemas
      checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
      		" TABLENAME || ' ' = 'SYSCOLUMNS '",
      		new String[][] {{"SYSCOLUMNS"} });   
      //Using cast for result of CAST expression in the query above would not
      //affect the sql in any ways. 
      checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
      		" CAST (TABLENAME || ' ' AS CHAR(12)) = " +
			" 'SYSCOLUMNS '",
      		new String[][] {{"SYSCOLUMNS"} });   

      //Do some testing using COALESCE
      //following will work with no problem for a database with UCS_BASIC
      //collation for system and user schemas
      checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
      		" COALESCE(TABLENAME, 'c') = 'SYSCOLUMNS'",
      		new String[][] {{"SYSCOLUMNS"} });   
      //Using cast for result of COALESCE expression in the query above would not
      //affect the sql in any ways. 
      checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
      		" CAST (COALESCE (TABLENAME, 'c') AS CHAR(12)) = " +
			" 'SYSCOLUMNS'",
      		new String[][] {{"SYSCOLUMNS"} });   

      //Do some testing using NULLIF
      //following will work with no problem for a database with UCS_BASIC
      //collation for system and user schemas
      checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
		" NULLIF(TABLENAME, 'c') = 'SYSCOLUMNS'",
  		new String[][] {{"SYSCOLUMNS"} });   
      //Using cast for result of NULLIF expression in the query above would not
      //affect the sql in any ways. 
      checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
      		" CAST (NULLIF (TABLENAME, 'c') AS CHAR(12)) = " +
			" 'SYSCOLUMNS'",
      		new String[][] {{"SYSCOLUMNS"} });   

      //Test USER/CURRENT_USER/SESSION_USER
      checkLangBasedQuery(s, "SELECT count(*) FROM CUSTOMER WHERE "+ 
      		"CURRENT_USER = 'APP'",
      		new String[][] {{"7"}});   
      
      //Do some testing with MAX/MIN operators
      checkLangBasedQuery(s, "SELECT MAX(NAME) maxName FROM CUSTOMER ORDER BY maxName ",
      		new String[][] {{"\u017Bebra"}});   
      checkLangBasedQuery(s, "SELECT MIN(NAME) minName FROM CUSTOMER ORDER BY minName ",
      		new String[][] {{"Acorn"}});   

      //Do some testing with CHAR/VARCHAR functions
      s.executeUpdate("set schema SYS");
      checkLangBasedQuery(s, "SELECT CHAR(ID) FROM APP.CUSTOMER WHERE " +
      		" CHAR(ID)='0'", new String[] [] {{"0"}});
      
      s.executeUpdate("set schema APP");
      if (XML.classpathMeetsXMLReqs())
      	checkLangBasedQuery(s, "SELECT XMLSERIALIZE(x as CHAR(10)) " +
      			" FROM xmlTable, SYS.SYSTABLES WHERE " +
				" XMLSERIALIZE(x as CHAR(10)) = TABLENAME",
				null);
      //Start of parameter testing
      //Start with simple ? param in a string comparison
      //Since all schemas (ie user and system) have the same collation, the 
      //following test won't fail.
      s.executeUpdate("set schema APP");
      ps = conn.prepareStatement("SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
      		" ? = TABLENAME");
      ps.setString(1, "SYSCOLUMNS");
      rs = ps.executeQuery();
      JDBC.assertFullResultSet(rs,new String[][] {{"SYSCOLUMNS"}});

      //Since all schemas (ie user and system) have the same collation, the 
      //following test won't fail.
      ps = conn.prepareStatement("SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
		" SUBSTR(?,2) = TABLENAME");
      ps.setString(1, " SYSCOLUMNS");
      rs = ps.executeQuery();
      JDBC.assertFullResultSet(rs,new String[][] {{"SYSCOLUMNS"}});

      //Since all schemas (ie user and system) have the same collation, the 
      //following test won't fail.
      ps = conn.prepareStatement("SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
		" LTRIM(?) = TABLENAME");
      ps.setString(1, " SYSCOLUMNS");
      rs = ps.executeQuery();
      JDBC.assertFullResultSet(rs,new String[][] {{"SYSCOLUMNS"}});
      ps = conn.prepareStatement("SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
		" RTRIM(?) = TABLENAME");
      ps.setString(1, "SYSCOLUMNS  ");
      rs = ps.executeQuery();
      JDBC.assertFullResultSet(rs,new String[][] {{"SYSCOLUMNS"}});

      //Since all schemas (ie user and system) have the same collation, the 
      //following test won't fail.
      ps = conn.prepareStatement("SELECT COUNT(*) FROM CUSTOMER WHERE " + 
      		" ? IN (SELECT TABLENAME FROM SYS.SYSTABLES)");
      ps.setString(1, "SYSCOLUMNS");
      rs = ps.executeQuery();
      JDBC.assertFullResultSet(rs,new String[][] {{"7"}});
      //End of parameter testing
      
      conn.commit();

      dropTable(s);
      s.close();
      conn.close();
      }
      
  /**
   * Test order by with polish collation
   * @throws SQLException
   */
public void testPolishCollation() throws SQLException {
      DataSource ds = JDBCDataSource.getDataSourceLogical("poldb");
      JDBCDataSource.setBeanProperty(ds, "connectionAttributes", 
                  "create=true;territory=pl;collation=TERRITORY_BASED");

      
      Connection conn = ds.getConnection();
      conn.setAutoCommit(false);
      PreparedStatement ps;
      ResultSet rs;
      Statement s = conn.createStatement();
      
      setUpTable(s);

      //The collation should be TERRITORY_BASED for this database
      checkLangBasedQuery(s, 
      		"VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY('derby.database.collation')",
			new String[][] {{"TERRITORY_BASED"}});

      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER ORDER BY NAME",
      		new String[][] {{"6","aacorn"}, {"4","Acorn"}, {"2","\u0104corn"},
      		{"0","Smith"},{"5","\u015Amith"}, {"1","Zebra"},{"3","\u017Bebra"} });
      
      //COMPARISONS INVOLVING CONSTANTS
      //In Polish, 'aacorn' is != 'Acorn'
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER where 'aacorn' = 'Acorn' ",
      		null);
      //In Polish, 'aacorn' is < 'Acorn'
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER where 'aacorn' < 'Acorn'",
      		new String[][] {{"0","Smith"}, {"1","Zebra"}, {"2","\u0104corn"},
      		{"3","\u017Bebra"}, {"4","Acorn"}, {"5","\u015Amith"}, 
			{"6","aacorn"} });

      //COMPARISONS INVOLVING CONSTANT and PERSISTENT COLUMN
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER WHERE NAME <= 'Smith' ",
      		new String[][] {{"0","Smith"}, {"2","\u0104corn"}, {"4","Acorn"}, 
      		{"6","aacorn"} });
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER WHERE NAME between 'Acorn' and 'Zebra' ",
      		new String[][] {{"0","Smith"}, {"1","Zebra"}, {"2","\u0104corn"}, 
      		{"4","Acorn"}, {"5","\u015Amith"} });
      //After index creation, the query above will return same data but in 
      //different order
      /*s.executeUpdate("CREATE INDEX CUSTOMER_INDEX1 ON CUSTOMER(NAME)");
      s.executeUpdate("INSERT INTO CUSTOMER VALUES (NULL, NULL)");
      checkLangBasedQuery(s, 
      		"SELECT ID, NAME FROM CUSTOMER -- derby-properties index=customer_index1 \r WHERE NAME between 'Acorn' and " +
			" 'Zebra'", //ORDER BY NAME",
      		new String[][] {{"4","Acorn"}, {"2","\u0104corn"}, {"0","Smith"}, 
		      		{"5","\u015Amith"}, {"1","Zebra"} });
      */
      //For collated databases, COMPARISONS OF USER PERSISTENT CHARACTER 
      //COLUMN AND CHARACTER CONSTANT WILL FAIL IN SYSTEM SCHEMA.
      s.executeUpdate("set schema SYS");
      assertStatementError("42818", s, "SELECT ID, NAME FROM APP.CUSTOMER WHERE NAME <= 'Smith' ");

      //Do some testing with MAX/MIN operators
      s.executeUpdate("set schema APP");
      checkLangBasedQuery(s, "SELECT MAX(NAME) maxName FROM CUSTOMER ORDER BY maxName ",
      		new String[][] {{"\u017Bebra"}});   
      checkLangBasedQuery(s, "SELECT MIN(NAME) minName FROM CUSTOMER ORDER BY minName ",
      		new String[][] {{"aacorn"}});   

      commonTestingForTerritoryBasedDB(s);

      conn.commit();
      dropTable(s);
      conn.close();
      
      }    
  

  /**
   * Test order by with Norwegian collation
   * 
   * @throws SQLException
   */
public void testNorwayCollation() throws SQLException {
      DataSource ds = JDBCDataSource.getDataSourceLogical("nordb");
      JDBCDataSource.setBeanProperty(ds, "connectionAttributes", 
                  "create=true;territory=no;collation=TERRITORY_BASED");

      
      Connection conn = ds.getConnection();
      conn.setAutoCommit(false);
      Statement s = conn.createStatement();
      PreparedStatement ps;
      ResultSet rs;
      setUpTable(s);

      //The collation should be TERRITORY_BASED for this database
      checkLangBasedQuery(s, 
      		"VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY('derby.database.collation')",
			new String[][] {{"TERRITORY_BASED"}});

      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER ORDER BY NAME",
      		new String[][] {{"4","Acorn"}, {"2","\u0104corn"},{"0","Smith"},
      		{"5","\u015Amith"}, {"1","Zebra"},{"3","\u017Bebra"}, {"6","aacorn"} });
      
      //COMPARISONS INVOLVING CONSTANTS
      //In Norway, 'aacorn' is != 'Acorn'
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER where 'aacorn' = 'Acorn' ",
      		null);
      //In Norway, 'aacorn' is not < 'Acorn'
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER where 'aacorn' < 'Acorn' ",
      		null);

      //COMPARISONS INVOLVING CONSTANT and PERSISTENT COLUMN
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER WHERE NAME <= 'Smith' ",
      		new String[][] {{"0","Smith"}, {"2","\u0104corn"}, {"4","Acorn"} });
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER WHERE NAME between 'Acorn' and 'Zebra' ",
      		new String[][] {{"0","Smith"}, {"1","Zebra"}, {"2","\u0104corn"}, 
      		{"4","Acorn"}, {"5","\u015Amith"} });
      //After index creation, the query above will return same data but in 
      //different order
      /*s.executeUpdate("CREATE INDEX CUSTOMER_INDEX1 ON CUSTOMER(NAME)");
      s.executeUpdate("INSERT INTO CUSTOMER VALUES (NULL, NULL)");
      checkLangBasedQuery(s, 
      		"SELECT ID, NAME FROM CUSTOMER  -- derby-properties index=customer_index1 \r WHERE NAME between 'Acorn' and " +
			" 'Zebra'", //ORDER BY NAME",
      		new String[][] {{"4","Acorn"}, {"2","\u0104corn"}, {"0","Smith"}, 
		      		{"5","\u015Amith"}, {"1","Zebra"} });
      */
      //For collated databases, COMPARISONS OF USER PERSISTENT CHARACTER 
      //COLUMN AND CHARACTER CONSTANT WILL FAIL IN SYSTEM SCHEMA.
      s.executeUpdate("set schema SYS");
      assertStatementError("42818", s, "SELECT ID, NAME FROM APP.CUSTOMER WHERE NAME <= 'Smith' ");

      //Do some testing with MAX/MIN operators
      s.executeUpdate("set schema APP");
      checkLangBasedQuery(s, "SELECT MAX(NAME) maxName FROM CUSTOMER ORDER BY maxName ",
      		new String[][] {{"aacorn"}});   
      checkLangBasedQuery(s, "SELECT MIN(NAME) minName FROM CUSTOMER ORDER BY minName ",
      		new String[][] {{"Acorn"}});   

      commonTestingForTerritoryBasedDB(s);

      conn.commit();

      dropTable(s);
      s.close();
      conn.close();
      }
  

  /**
   * Test order by with English collation
   * 
  * @throws SQLException
  */
public void testEnglishCollation() throws SQLException {
      DataSource ds = JDBCDataSource.getDataSourceLogical("endb");
      JDBCDataSource.setBeanProperty(ds, "connectionAttributes", 
                  "create=true;territory=en;collation=TERRITORY_BASED");
      
      Connection conn = ds.getConnection();
      conn.setAutoCommit(false);
      Statement s = conn.createStatement();
      PreparedStatement ps;
      ResultSet rs;
      setUpTable(s);

      //The collation should be TERRITORY_BASED for this database
      checkLangBasedQuery(s, 
      		"VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY('derby.database.collation')",
			new String[][] {{"TERRITORY_BASED"}});

      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER ORDER BY NAME",
      		new String[][] {{"6","aacorn"},{"4","Acorn"},{"2","\u0104corn"},{"0","Smith"},
      		{"5","\u015Amith"},{"1","Zebra"},{"3","\u017Bebra"} });      

      //COMPARISONS INVOLVING CONSTANTS
      //In English, 'aacorn' != 'Acorn'
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER where 'aacorn' = 'Acorn' ",
      		null);
      //In English, 'aacorn' is < 'Acorn'
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER where 'aacorn' < 'Acorn'",
      		new String[][] {{"0","Smith"}, {"1","Zebra"}, {"2","\u0104corn"},
      		{"3","\u017Bebra"}, {"4","Acorn"}, {"5","\u015Amith"}, 
			{"6","aacorn"} });

      //COMPARISONS INVOLVING CONSTANT and PERSISTENT COLUMN
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER WHERE NAME <= 'Smith' ",
      		new String[][] {{"0","Smith"}, {"2","\u0104corn"}, {"4","Acorn"},
      		{"6","aacorn"} });
      checkLangBasedQuery(s, "SELECT ID, NAME FROM CUSTOMER WHERE NAME between 'Acorn' and 'Zebra' ",
      		new String[][] {{"0","Smith"}, {"1","Zebra"}, {"2","\u0104corn"}, 
      		{"4","Acorn"}, {"5","\u015Amith"} });
      //After index creation, the query above will return same data but in 
      //different order
      /*s.executeUpdate("CREATE INDEX CUSTOMER_INDEX1 ON CUSTOMER(NAME)");
      s.executeUpdate("INSERT INTO CUSTOMER VALUES (NULL, NULL)");
      checkLangBasedQuery(s, 
      		"SELECT ID, NAME FROM CUSTOMER -- derby-properties index=customer_index1 \r WHERE NAME between 'Acorn' and " + 
			" 'Zebra'", //ORDER BY NAME",
      		new String[][] {{"4","Acorn"}, {"2","\u0104corn"}, {"0","Smith"}, 
      		{"5","\u015Amith"}, {"1","Zebra"} });
      */
      //For collated databases, COMPARISONS OF USER PERSISTENT CHARACTER 
      //COLUMN AND CHARACTER CONSTANT WILL FAIL IN SYSTEM SCHEMA.
      s.executeUpdate("set schema SYS");
      assertStatementError("42818", s, "SELECT ID, NAME FROM APP.CUSTOMER WHERE NAME <= 'Smith' ");

      //Do some testing with MAX/MIN operators
      s.executeUpdate("set schema APP");
      checkLangBasedQuery(s, "SELECT MAX(NAME) maxName FROM CUSTOMER ORDER BY maxName ",
      		new String[][] {{"\u017Bebra"}});   
      checkLangBasedQuery(s, "SELECT MIN(NAME) minName FROM CUSTOMER ORDER BY minName ",
      		new String[][] {{"aacorn"}});   

      commonTestingForTerritoryBasedDB(s);

      conn.commit();
      
      dropTable(s);
      s.close();
      conn.close();
      }

private void commonTestingForTerritoryBasedDB(Statement s) throws SQLException{
	PreparedStatement ps;
	ResultSet rs;
    Connection conn = s.getConnection();		

    s.executeUpdate("set schema APP");
    //Following sql will fail because the compilation schema is user schema
    //and hence the character constant "CUSTOMER" will pickup the collation
    //of user schema, which is territory based for this database. But the
    //persistent character columns from sys schema, which is TABLENAME in
    //following query will have the UCS_BASIC collation. Since the 2 
    //collation types don't match, the following comparison will fail
    assertStatementError("42818", s, "SELECT 1 FROM SYS.SYSTABLES WHERE " +
    		" TABLENAME = 'CUSTOMER' ");   
    //To get around the problem in the query above, use cast for persistent 
    //character column from system table and then compare it against a 
    //character constant. Do this when the compilation schema is a user 
    //schema and not system schema. This will ensure that the result 
    //of the casting will pick up the collation of the user schema. And 
    //constant character string will also pick up the collation of user 
    //schema and hence the comparison between the 2 will not fail
    checkLangBasedQuery(s, "SELECT 1 FROM SYS.SYSTABLES WHERE CAST " +
    		" (TABLENAME AS CHAR(15)) = 'CUSTOMER' ",
    		new String[][] {{"1"} });   

    //Do some testing using CASE WHEN THEN ELSE
    //following sql will not work for a database with territory based
    //collation for user schemas. This is because the resultant string type 
    //from the CASE expression below will have collation derivation of NONE.
    //The reason for collation derivation of NONE is that the CASE's 2 
    //operands have different collation types and as per SQL standards, if an
    //aggregate method has operands with different collations, then the 
    //result will have collation derivation of NONE. The right side of =
    //operation has collation type of territory based and hence the following
    //sql fails.
    assertStatementError("42818", s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE CASE " +
    		" WHEN 1=1 THEN TABLENAME ELSE 'c' END = 'SYSCOLUMNS'");
    //CASTing the result of the CASE expression will solve the problem in the
    //query above. Now both the operands around = operation will have 
    //collation type of territory based and hence the sql won't fail
    checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE CAST " +
    		" ((CASE WHEN 1=1 THEN TABLENAME ELSE 'c' END) AS CHAR(12)) = " +
			" 'SYSCOLUMNS'",
    		new String[][] {{"SYSCOLUMNS"} });   

    //Do some testing using CONCATENATION
    //following will fail because result string of concatenation has 
    //collation derivation of NONE. That is because it's 2 operands have
    //different collation types. TABLENAME has collation type of UCS_BASIC
    //but constant character string ' ' has collation type of territory based
    //So the left hand side of = operator has collation derivation of NONE
    //and right hand side has collation derivation of territory based and
    //that causes the = comparison to fail
    assertStatementError("42818", s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
    		" TABLENAME || ' ' = 'SYSCOLUMNS '");   
    //CASTing the result of the concat expression will solve the problem in 
    //the query above. Now both the operands around = operation will have 
    //collation type of territory based and hence the sql won't fail
    checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
    		" CAST (TABLENAME || ' ' AS CHAR(12)) = " +
			" 'SYSCOLUMNS '",
    		new String[][] {{"SYSCOLUMNS"} });   

    //Do some testing using COALESCE
    //following will fail because result string of COALESCE has 
    //collation derivation of NONE. That is because it's 2 operands have
    //different collation types. TABLENAME has collation type of UCS_BASIC
    //but constant character string 'c' has collation type of territory based
    //So the left hand side of = operator has collation derivation of NONE
    //and right hand side has collation derivation of territory based and
    //that causes the = comparison to fail
    assertStatementError("42818", s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
    		" COALESCE(TABLENAME, 'c') = 'SYSCOLUMNS'");   
    //CASTing the result of the COALESCE expression will solve the problem in 
    //the query above. Now both the operands around = operation will have 
    //collation type of territory based and hence the sql won't fail
    checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
    		" CAST (COALESCE (TABLENAME, 'c') AS CHAR(12)) = " +
			" 'SYSCOLUMNS'",
    		new String[][] {{"SYSCOLUMNS"} });   

    //Do some testing using NULLIF
    //following will fail because result string of NULLIF has 
    //collation derivation of NONE. That is because it's 2 operands have
    //different collation types. TABLENAME has collation type of UCS_BASIC
    //but constant character string 'c' has collation type of territory based
    //So the left hand side of = operator has collation derivation of NONE
    //and right hand side has collation derivation of territory based and
    //that causes the = comparison to fail
    assertStatementError("42818", s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
		" NULLIF(TABLENAME, 'c') = 'SYSCOLUMNS'");   
    //CASTing the result of the NULLIF expression will solve the problem in 
    //the query above. Now both the operands around = operation will have 
    //collation type of territory based and hence the sql won't fail
    checkLangBasedQuery(s, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
    		" NULLIF (CAST (TABLENAME AS CHAR(12)), 'c' ) = " +
			" 'SYSCOLUMNS'",
    		new String[][] {{"SYSCOLUMNS"} });   

    //Do some testing with CHAR/VARCHAR functions
    s.executeUpdate("set schema SYS");
    //Following will work because both operands are = have the collation type
    //of UCS_BASIC
    checkLangBasedQuery(s, "SELECT CHAR(ID) FROM APP.CUSTOMER WHERE " +
    		" CHAR(ID)='0'", new String[] [] {{"0"}});
    //Derby does not allow VARCHAR function on numeric columns and hence 
    //this VARCHAR test looks little different than the CHAR test above.
    checkLangBasedQuery(s, "SELECT ID FROM APP.CUSTOMER WHERE " +
    		" VARCHAR(NAME)='Smith'", new String[] [] {{"0"}});
    //Now try a negative test
    s.executeUpdate("set schema APP");
    //following will fail because CHAR(TABLENAME)= TABLENAME is causing compare
    //between 2 character string types with different collation types. The lhs
    //operand has collation of territory based but rhs operand has collation of
    //UCS_BASIC
    assertStatementError("42818", s, "SELECT CHAR(TABLENAME) FROM " +
    		" SYS.SYSTABLES WHERE CHAR(TABLENAME)= TABLENAME AND " + 
			" VARCHAR(TABLENAME) = 'SYSCOLUMNS'");
    //To resolve the problem above, we need to use CAST around TABLENAME
    checkLangBasedQuery(s, "SELECT CHAR(TABLENAME) FROM SYS.SYSTABLES WHERE " +
    		" CHAR(TABLENAME)= (CAST (TABLENAME AS CHAR(12))) AND " + 
			" VARCHAR(TABLENAME) = 'SYSCOLUMNS'",
    		new String[][] {{"SYSCOLUMNS"} });  

    //Test USER/CURRENT_USER/SESSION_USER/CURRENT SCHMEA/ CURRENT ISOLATION
    //following will fail because we are trying to compare UCS_BASIC 
    //(CURRENT_USER) with territory based ("APP" taking it's collation from
    //compilation schema which is user schema at this time). 
    assertStatementError("42818", s, "SELECT count(*) FROM CUSTOMER WHERE "+
    		"CURRENT_USER = 'APP'");  
    //The problem above can be fixed by CASTing CURRENT_USER so that the 
    //collation type will be picked up from compilation schema which is user
    //schema at this point.
    checkLangBasedQuery(s, "SELECT count(*) FROM CUSTOMER WHERE "+ 
    		"CAST(CURRENT_USER AS CHAR(12)) = 'APP'",
    		new String[][] {{"7"}});   
    //following comparison will not cause compilation error because both the
    //operands around = have collation type of UCS_BASIC
    checkLangBasedQuery(s, "SELECT count(*) FROM CUSTOMER WHERE "+ 
    		"SESSION_USER = USER", new String[][] {{"7"}});
    //following will fail because we are trying to compare UCS_BASIC 
    //(CURRENT ISOLATION) with territory based ("CS" taking it's collation from
    //compilation schema which is user schema at this time). 
    assertStatementError("42818", s, "SELECT count(*) FROM CUSTOMER WHERE "+
	"CURRENT ISOLATION = 'CS'");  
    //Following will not give compilation error because both sides in = have 
    //the same collation type 
    checkLangBasedQuery(s, "SELECT count(*) FROM CUSTOMER WHERE "+ 
    		"CAST(CURRENT ISOLATION AS CHAR(12)) = 'CS'",
    		new String[][] {{"7"}});   
    //Following will not cause compilation error because both the operands
    //around the = have collation type of UCS_BASIC. We are in the SYS
    //schema and hence character string constant 'APP' has picked the collation
    //type of SYS schema which is UCS_BASIC
    s.executeUpdate("set schema SYS");
    checkLangBasedQuery(s, "SELECT count(*) FROM APP.CUSTOMER WHERE "+ 
    		"CURRENT SCHEMA = 'SYS'", new String[][] {{"7"}});   
    
    s.executeUpdate("set schema APP");
    if (XML.classpathMeetsXMLReqs()) {
        assertStatementError("42818", s, "SELECT XMLSERIALIZE(x as CHAR(10)) " +
        		" FROM xmlTable, SYS.SYSTABLES WHERE " + 
    			" XMLSERIALIZE(x as CHAR(10)) = TABLENAME");
        checkLangBasedQuery(s, "SELECT XMLSERIALIZE(x as CHAR(10)) FROM " +
        		" xmlTable, SYS.SYSTABLES WHERE XMLSERIALIZE(x as CHAR(10)) = " + 
    			" CAST(TABLENAME AS CHAR(10))",
        		null);
    }

    //Start of parameter testing
    //Start with simple ? param in a string comparison
    //Following will work fine because ? is supposed to take it's collation 
    //from the context which in this case is from TABLENAME and TABLENAME
    //has collation type of UCS_BASIC
    s.executeUpdate("set schema APP");
    ps = conn.prepareStatement("SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
    		" ? = TABLENAME");
    ps.setString(1, "SYSCOLUMNS");
    rs = ps.executeQuery();
    JDBC.assertFullResultSet(rs,new String[][] {{"SYSCOLUMNS"}});      

    //Do parameter testing with SUBSTR
    //Won't work in territory based database because in 
    //SUBSTR(?, int) = TABLENAME
    //? will get the collation of the current schema which is a user
    //schema and hence the collation type of result of SUBSTR will also be 
    //territory based since the result of SUBSTR always picks up the 
    //collation of it's first operand. So the comparison between left hand
    //side with terriotry based and right hand side with UCS_BASIC will fail.
    checkPreparedStatementError(conn, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
    		" SUBSTR(?,2) = TABLENAME", "42818");
    //To fix the problem above, we need to CAST TABLENAME so that the result 
    //of CAST will pick up the collation of the current schema and this will
    //cause both the operands of SUBSTR(?,2) = CAST(TABLENAME AS CHAR(10)) 
    //to have same collation
    ps = conn.prepareStatement("SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
		" SUBSTR(?,2) = CAST(TABLENAME AS CHAR(10))");
    ps.setString(1, "aSYSCOLUMNS");
    rs = ps.executeQuery();
    JDBC.assertFullResultSet(rs,new String[][] {{"SYSCOLUMNS"}});      

    //Do parameter testing with COALESCE
    //following will pass because the ? inside the COALESCE will take the 
    //collation type of the other operand which is TABLENAME. The result of
    //COALESCE will have collation type of UCS_BASIC and that is the same
    //collation that the ? on rhs of = will get.
    ps = conn.prepareStatement("SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
	" COALESCE(TABLENAME, ?) = ?");   
    ps.setString(1, " ");
    ps.setString(2, "SYSCOLUMNS ");
    rs = ps.executeQuery();
    JDBC.assertFullResultSet(rs,new String[][] {{"SYSCOLUMNS"}});

    //Do parameter testing with LTRIM
    //Won't work in territory based database because in 
    //LTRIM(?) = TABLENAME
    //? will get the collation of the current schema which is a user
    //schema and hence the collation type of result of LTRIM will also be 
    //territory based since the result of LTRIM always picks up the 
    //collation of it's operand. So the comparison between left hand
    //side with terriotry based and right hand side with UCS_BASIC will fail.
    checkPreparedStatementError(conn, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
    		" LTRIM(?) = TABLENAME", "42818");
    //To fix the problem above, we need to CAST TABLENAME so that the result 
    //of CAST will pick up the collation of the current schema and this will
    //cause both the operands of LTRIM(?) = CAST(TABLENAME AS CHAR(10)) 
    //to have same collation
    ps = conn.prepareStatement("SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
		" LTRIM(?) = CAST(TABLENAME AS CHAR(10))");
    ps.setString(1, " SYSCOLUMNS");
    rs = ps.executeQuery();
    JDBC.assertFullResultSet(rs,new String[][] {{"SYSCOLUMNS"}});

    //Similar testing for RTRIM
    checkPreparedStatementError(conn, "SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
    		" RTRIM(?) = TABLENAME", "42818");
    ps = conn.prepareStatement("SELECT TABLENAME FROM SYS.SYSTABLES WHERE " +
		" RTRIM(?) = CAST(TABLENAME AS CHAR(10))");
    ps.setString(1, "SYSCOLUMNS  ");
    rs = ps.executeQuery();
    JDBC.assertFullResultSet(rs,new String[][] {{"SYSCOLUMNS"}});

    //Do parameter testing with IN and subquery
    //Following will work just fine because ? will take it's collation from the
    //context which in this case will be collation of TABLENAME which has 
    //collation type of UCS_BASIC. 
    ps = conn.prepareStatement("SELECT COUNT(*) FROM CUSTOMER WHERE ? IN " +
    		" (SELECT TABLENAME FROM SYS.SYSTABLES)");
    ps.setString(1, "SYSCOLUMNS");
    rs = ps.executeQuery();
    JDBC.assertFullResultSet(rs,new String[][] {{"7"}});

    //Testing for NOT IN. Following won't work becuase ? is taking the 
    //collation type from context which will be from the character string
    //literal 'SYSCOLUMNS'. That literal will have the collation type of the
    //current schema which is the user schema and hence it's collation type
    //will be territory based. But that collation does not match the left hand
    //side on IN clause and hence it results in compliation error.
    checkPreparedStatementError(conn, "SELECT TABLENAME FROM SYS.SYSTABLES " +
    		" WHERE TABLENAME NOT IN (?, ' SYSCOLUMNS ') AND " +
			" CAST(TABLENAME AS CHAR(10)) = 'SYSCOLUMNS' ", "42818");
    //We can make the query work in 2 ways
    //1)Be in the SYS schema and then ? will take the collation of UCS_BASIC
    //because that is what the character string literal ' SYSCOLUMNS ' has.
    s.executeUpdate("set schema SYS");
    ps = conn.prepareStatement("SELECT TABLENAME FROM SYS.SYSTABLES " +
    		" WHERE TABLENAME NOT IN (?, ' SYSCOLUMNS ') AND " +
			" CAST(TABLENAME AS CHAR(10)) = 'SYSCOLUMNS' ");
    ps.setString(1, "aSYSCOLUMNS");
    rs = ps.executeQuery();
    JDBC.assertFullResultSet(rs,new String[][] {{"SYSCOLUMNS"}});
    //2)The other way to fix the query would be to do a CAST on TABLENAME so
    //it will have the collation of current schema which is APP 
    s.executeUpdate("set schema APP");
    ps = conn.prepareStatement("SELECT TABLENAME FROM SYS.SYSTABLES WHERE " + 
	" CAST(TABLENAME AS CHAR(10)) NOT IN (?, ' SYSCOLUMNS ') AND " +
	" CAST(TABLENAME AS CHAR(10)) = 'SYSCOLUMNS' ");
    ps.setString(1, "aSYSCOLUMNS");
    rs = ps.executeQuery();
    JDBC.assertFullResultSet(rs,new String[][] {{"SYSCOLUMNS"}});

    //Following will not fail because collation of ? here does not matter 
    //since we are not doing a collation related method 
    s.executeUpdate("set schema SYS");
    ps = conn.prepareStatement("INSERT INTO APP.CUSTOMER(NAME) VALUES(?)");
    ps.setString(1, "SYSCOLUMNS");
    ps.executeUpdate();
    ps.close();
    s.executeUpdate("INSERT INTO APP.CUSTOMER(NAME) VALUES('abc')");
    rs = s.executeQuery("SELECT COUNT(*) FROM APP.CUSTOMER ");
    JDBC.assertFullResultSet(rs,new String[][] {{"9"}});
    s.executeUpdate("DELETE FROM APP.CUSTOMER WHERE NAME = 'abc'");
    rs = s.executeQuery("SELECT COUNT(*) FROM APP.CUSTOMER ");
    JDBC.assertFullResultSet(rs,new String[][] {{"8"}});
    //End of parameter testing
}

private void setUpTable(Statement s) throws SQLException {

    s.execute("CREATE TABLE CUSTOMER(ID INT, NAME VARCHAR(40))");
    
    Connection conn = s.getConnection();

    PreparedStatement ps = conn.prepareStatement("INSERT INTO CUSTOMER VALUES(?,?)");
    for (int i = 0; i < NAMES.length; i++)
    {
            ps.setInt(1, i);
            ps.setString(2, NAMES[i]);
            ps.executeUpdate();
    }

    s.execute("create table xmlTable (x xml)");
    s.executeUpdate("insert into xmlTable values(null)");
    
    conn.commit();
    ps.close();
}

private void dropTable(Statement s) throws SQLException {
	
    s.execute("DROP TABLE APP.CUSTOMER");     
    s.getConnection().commit();
}

/**
 * Make sure that attempt to prepare the statement will give the passed error
 * 
 * @param con Connection on which query should be prepared
 * @param query Query to be prepared
 * @param error Prepared statement will give this error for the passed query
 */
private void checkPreparedStatementError(Connection con, String query, 
		String error)
{
	try{
	    con.prepareStatement(query);
        fail("Expected error '" + error  + "' but no error was thrown.");
	} catch (SQLException sqle) {
        assertSQLState(error, sqle);		
	}
	
}
/**
 * Execute the passed statement and compare the results against the
 * expectedResult 
 *
 * @param s              statement object to use to execute the query
 * @param query          string with the query to execute.
 * @param expectedResult Null for this means that the passed query is 
 * expected to return an empty resultset. If not empty, then the resultset
 * from the query should match this paramter
 *
 * @throws SQLException
 */
private void checkLangBasedQuery(Statement s, String query, String[][] expectedResult) throws SQLException {
    ResultSet rs = s.executeQuery(query);
    if (expectedResult == null) //expecting empty resultset from the query
    	JDBC.assertEmpty(rs);
    else
    	JDBC.assertFullResultSet(rs,expectedResult);
}
    
  /**
   * Tests only need to run in embedded since collation
   * is a server side operation.
   */
  public static Test suite() {

        Test test =  TestConfiguration.embeddedSuite(CollationTest.class);
        test = TestConfiguration.additionalDatabaseDecorator(test, "defaultdb");
        test = TestConfiguration.additionalDatabaseDecorator(test, "endb");
        test = TestConfiguration.additionalDatabaseDecorator(test, "nordb");
        test = TestConfiguration.additionalDatabaseDecorator(test, "poldb");
        return test;
    }

}
