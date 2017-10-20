/*

   Derby - Class org.apache.derbyTesting.functionTests.tests.jdbcapi.metadata_test

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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.math.BigDecimal;

import java.util.Properties;
import java.util.StringTokenizer;

import java.lang.reflect.Method;

import org.apache.derby.tools.ij;
import org.apache.derbyTesting.functionTests.util.TestUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * Test of database meta-data.  This program simply calls each of the meta-data
 * methods, one by one, and prints the results.  The test passes if the printed
 * results match a previously stored "master".  Thus this test cannot actually
 * discern whether it passes or not.
 *
 */

public abstract class metadata_test {

	// Ids for the Derby internal procedures that are used to fetch
	// some of the metadata.

	protected static final int GET_PROCEDURES = 0;
 	protected static final int GET_PROCEDURE_COLUMNS = 1;
 	protected static final int GET_TABLES = 2;
 	protected static final int GET_COLUMNS = 3;
 	protected static final int GET_COLUMN_PRIVILEGES = 5;
 	protected static final int GET_TABLE_PRIVILEGES = 6;
 	protected static final int GET_BEST_ROW_IDENTIFIER = 7;
 	protected static final int GET_VERSION_COLUMNS = 8;
 	protected static final int GET_PRIMARY_KEYS = 9;
 	protected static final int GET_IMPORTED_KEYS = 10;
 	protected static final int GET_EXPORTED_KEYS = 11;
 	protected static final int GET_CROSS_REFERENCE = 12;
 	protected static final int GET_TYPE_INFO = 13;
 	protected static final int GET_INDEX_INFO = 14;

	protected static final int IGNORE_PROC_ID = -1;
	
	//Used for JSR169
	private static boolean HAVE_DRIVER_CLASS;
	static{
		try{
			Class.forName("java.sql.Driver");
			HAVE_DRIVER_CLASS = true;
		}
		catch(ClassNotFoundException e){
			//Used for JSR169
			HAVE_DRIVER_CLASS = false;
		}
	}

	// We leave it up to the classes which extend this one to
	// initialize the following fields at construct time.
	public Connection con;
	public static Statement s;
	
	/*
	** Escaped function testing
	*/
	private static final String[][] NUMERIC_FUNCTIONS =
	{
		// Section C.1 JDBC 3.0 spec.
		{ "ABS", "-25.67" },
		{ "ACOS", "0.0707" },
		{ "ASIN", "0.997" },
		{ "ATAN", "14.10" },
		{ "ATAN2", "0.56", "1.2" },
		{ "CEILING", "3.45" },
		{ "COS", "1.2" },
		{ "COT", "3.4" },
		{ "DEGREES", "2.1" },
		{ "EXP", "2.3" },
		{ "FLOOR", "3.22" },
		{ "LOG", "34.1" },
		{ "LOG10", "18.7" },
		{ "MOD", "124", "7" },
		{ "PI" },
		{ "POWER", "2", "3" },
		{ "RADIANS", "54" },
		{ "RAND", "17" }, 
		{ "ROUND", "345.345", "1" }, 
		{ "SIGN", "-34" },
		{ "SIN", "0.32" },
		{ "SQRT", "6.22" },
		{ "TAN", "0.57", },
		{ "TRUNCATE", "345.395", "1" }
	};
	
	private static final String[][] TIMEDATE_FUNCTIONS =
	{	
		// Section C.3 JDBC 3.0 spec.
		{ "CURDATE" },
		{ "CURTIME" },
		{ "DAYNAME", "{d '1995-12-19'h}" },
		{ "DAYOFMONTH", "{d '1995-12-19'}" },
		{ "DAYOFWEEK", "{d '1995-12-19'}" },
		{ "DAYOFYEAR", "{d '1995-12-19'}" },
		{ "HOUR", "{t '16:13:03'}" },
		{ "MINUTE", "{t '16:13:03'}" },
		{ "MONTH", "{d '1995-12-19'}" },
		{ "MONTHNAME", "{d '1995-12-19'}" },
		{ "NOW" },
		{ "QUARTER", "{d '1995-12-19'}" },
		{ "SECOND", "{t '16:13:03'}" },
		{ "TIMESTAMPADD", "SQL_TSI_DAY", "7", "{ts '1995-12-19 12:15:54'}" },
		{ "TIMESTAMPDIFF", "SQL_TSI_DAY", "{ts '1995-12-19 12:15:54'}", "{ts '1997-11-02 00:15:23'}" },
		{ "WEEK", "{d '1995-12-19'}" },
		{ "YEAR", "{d '1995-12-19'}" },
		
	};

	private static final String[][] SYSTEM_FUNCTIONS =
	{	
		// Section C.4 JDBC 3.0 spec.
		{ "DATABASE" },
		{ "IFNULL", "'this'", "'that'" },
		{ "USER"},
		};	
	
	private static final String[][] STRING_FUNCTIONS =
	{	
		// Section C.2 JDBC 3.0 spec.
		{ "ASCII" , "'Yellow'" },
		{ "CHAR", "65" },
		{ "CONCAT", "'hello'", "'there'" },
		{ "DIFFERENCE", "'Pires'", "'Piers'" },
		{ "INSERT", "'Bill Clinton'", "4", "'William'" },
		{ "LCASE", "'Fernando Alonso'" },
		{ "LEFT", "'Bonjour'", "3" },
		{ "LENGTH", "'four    '" } ,
		{ "LOCATE", "'jour'", "'Bonjour'" },
		{ "LTRIM", "'   left trim   '"},
		{ "REPEAT", "'echo'", "3" },
		{ "REPLACE", "'to be or not to be'", "'be'", "'England'" },
		{ "RTRIM", "'  right trim   '"},
		{ "SOUNDEX", "'Derby'" },
		{ "SPACE", "12"},
		{ "SUBSTRING", "'Ruby the Rubicon Jeep'", "10", "7", },
		{ "UCASE", "'Fernando Alonso'" }
		};

	public void runTest() {

		DatabaseMetaData met;
		ResultSet rs;
		ResultSetMetaData rsmet;

		System.out.println("Test metadata starting");

		try
		{
			//Cleanup any leftover database objects from previous test run
			cleanUp(s);

			// test decimal type and other numeric types precision, scale,
			// and display width after operations, beetle 3875, 3906
			s.execute("create table t (i int, s smallint, r real, "+
				"d double precision, dt date, t time, ts timestamp, "+
				"c char(10), v varchar(40) not null, dc dec(10,2))");
			s.execute("insert into t values(1,2,3.3,4.4,date('1990-05-05'),"+
						 "time('12:06:06'),timestamp('1990-07-07 07:07:07.07'),"+
						 "'eight','nine', 11.1)");

			// test decimal type and other numeric types precision, scale,
			// and display width after operations, beetle 3875, 3906
			//rs = s.executeQuery("select dc from t where tn = 10 union select dc from t where i = 1");
			rs = s.executeQuery("select dc from t where dc = 11.1 union select dc from t where i = 1");
			rsmet = rs.getMetaData();
			metadata_test.showNumericMetaData("Union Result", rsmet, 1);
			rs.close();

			rs = s.executeQuery("select dc, r, d, r+dc, d-dc, dc-d from t");
			rsmet = rs.getMetaData();
			metadata_test.showNumericMetaData("dec(10,2)", rsmet, 1);
			metadata_test.showNumericMetaData("real", rsmet, 2);
			metadata_test.showNumericMetaData("double", rsmet, 3);
			metadata_test.showNumericMetaData("real + dec(10,2)", rsmet, 4);
			metadata_test.showNumericMetaData("double precision - dec(10,2)", rsmet, 5);
			metadata_test.showNumericMetaData("dec(10,2) - double precision", rsmet, 6);

			while (rs.next())
				System.out.println("result row: " + rs.getString(1) + " "
						+ rs.getString(2) + " " + rs.getString(3)
						+ " " + rs.getString(4) + " " + rs.getString(5)
						+ " " + rs.getString(6)
						);
			rs.close();

			rsmet = s.executeQuery("VALUES CAST (0.0 AS DECIMAL(10,0))").getMetaData();
			metadata_test.showNumericMetaData("DECIMAL(10,0)", rsmet, 1);
			
			rsmet = s.executeQuery("VALUES CAST (0.0 AS DECIMAL(10,10))").getMetaData();
			metadata_test.showNumericMetaData("DECIMAL(10,10)", rsmet, 1);
			
			rsmet = s.executeQuery("VALUES CAST (0.0 AS DECIMAL(10,2))").getMetaData();
			metadata_test.showNumericMetaData("DECIMAL(10,2)", rsmet, 1);

			s.execute("insert into t values(1,2,3.3,4.4,date('1990-05-05'),"+
						 "time('12:06:06'),timestamp('1990-07-07 07:07:07.07'),"+
						 "'eight','nine', 11.11)");

			// test decimal/integer static column result scale consistent
			// with result set metadata after division, beetle 3901
			rs = s.executeQuery("select dc / 2 from t");
			rsmet = rs.getMetaData();
			System.out.println("Column result scale after division is: " + rsmet.getScale(1));
			while (rs.next())
				System.out.println("dc / 2 = " + rs.getString(1));
			rs.close();


			s.execute("create table louie (i int not null default 10, s smallint not null, " +
				      "c30 char(30) not null, " +
					  "vc10 varchar(10) not null default 'asdf', " +
					  "constraint PRIMKEY primary key(vc10, i), " +
					  "constraint UNIQUEKEY unique(c30, s), " + 
					  "ai bigint generated always as identity (start with -10, increment by 2001))");

			// Create another unique index on louie
			s.execute("create unique index u1 on louie(s, i)");
			// Create a non-unique index on louie
			s.execute("create index u2 on louie(s)");
			// Create a view on louie
			s.execute("create view screwie as select * from louie");

			// Create a foreign key
			s.execute("create table reftab (vc10 varchar(10), i int, " +
					  "s smallint, c30 char(30), " +
					  "s2 smallint, c302 char(30), " +
					  "dprim decimal(5,1) not null, dfor decimal(5,1) not null, "+
					  "constraint PKEY_REFTAB	primary key (dprim), " + 
					  "constraint FKEYSELF 		foreign key (dfor) references reftab, "+
					  "constraint FKEY1 		foreign key(vc10, i) references louie, " + 
				  	  "constraint FKEY2 		foreign key(c30, s2) references louie (c30, s), "+
				  	  "constraint FKEY3 		foreign key(c30, s) references louie (c30, s))");

			s.execute("create table reftab2 (t2_vc10 varchar(10), t2_i int, " +
					  "constraint T2_FKEY1 		foreign key(t2_vc10, t2_i) references louie)");

			// Create a table with all types
			s.execute("create table alltypes ( "+
							//"bitcol16_______ bit(16), "+
							//"bitvaryingcol32 bit varying(32), "+ 
							//"tinyintcol tinyint, "+
							"smallintcol smallint, "+
							"intcol int default 20, "+
							"bigintcol bigint, "+
							"realcol real, "+
							"doublepreccol double precision default 10, "+
							"floatcol float default 8.8, "+
							"decimalcol10p4s decimal(10,4), "+
							"numericcol20p2s numeric(20,2), "+
							"char8col___ char(8), "+
							"char8forbitcol___ char(8) for bit data, "+
							"varchar9col varchar(9), "+
							"varchar9bitcol varchar(9) for bit data, "+
							"longvarcharcol long varchar,"+
							"longvarbinarycol long varchar for bit data, "+
							//"nchar10col nchar(10)"
					  //+ ", nvarchar8col nvarchar(8)"
					  //+ ", longnvarchar long nvarchar"
					  //+ ", 
							"blobcol blob(3K), "+
							"clobcol clob(3K), "+
							"datecol date, "+
							"timecol time, "+
							"tscol timestamp"
					  + ")" );
			// test for beetle 4620
			s.execute("CREATE TABLE INFLIGHT(FLT_NUM CHAR(20) NOT NULL," + 
						"FLT_ORIGIN CHAR(6), " +
						"FLT_DEST CHAR(6),  " +
						"FLT_AIRCRAFT CHAR(20), " +
						"FLT_FLYING_TIME VARCHAR(22), "+
						"FLT_DEPT_TIME CHAR(8),  "+
						"FLT_ARR_TIME CHAR(8),  "+
						"FLT_NOTES VARCHAR(510), "+ 
						"FLT_DAYS_OF_WK CHAR(14), "+ 
						"FLT_CRAFT_PIC VARCHAR(32672), "+
						"PRIMARY KEY(FLT_NUM))");

			// Create procedures so we can test 
			// getProcedureColumns()
                        s.execute("create procedure GETPCTEST1 (" +
				// for creating, the procedure's params do not need to exactly match the method's
				"out outb VARCHAR(3), a VARCHAR(3), b NUMERIC, c SMALLINT, " +
				"e SMALLINT, f INTEGER, g BIGINT, h FLOAT, i DOUBLE PRECISION, " +
				"k DATE, l TIME, T TIMESTAMP )"+
				"language java external name " +
				"'org.apache.derbyTesting.functionTests.tests.jdbcapi.metadata.getpc'" +
							" parameter style java"); 
                        s.execute("create procedure GETPCTEST2 (pa INTEGER, pb BIGINT)"+
				"language java external name " +
				"'org.apache.derbyTesting.functionTests.tests.jdbcapi.metadata.getpc'" +
				" parameter style java"); 
                        s.execute("create procedure GETPCTEST3A (STRING1 VARCHAR(5), out STRING2 VARCHAR(5))"+
				"language java external name " +
				"'org.apache.derbyTesting.functionTests.tests.jdbcapi.metadata.getpc'" +
				" parameter style java"); 
                        s.execute("create procedure GETPCTEST3B (in STRING3 VARCHAR(5), inout STRING4 VARCHAR(5))"+
				"language java external name " +
				"'org.apache.derbyTesting.functionTests.tests.jdbcapi.metadata.getpc'" +
				" parameter style java"); 
                        s.execute("create procedure GETPCTEST4A()  "+
				"language java external name " +
				"'org.apache.derbyTesting.functionTests.tests.jdbcapi.metadata.getpc4a'"+
				" parameter style java"); 
                        s.execute("create procedure GETPCTEST4B() "+
				"language java external name " +
				"'org.apache.derbyTesting.functionTests.tests.jdbcapi.metadata.getpc4b'" +
				" parameter style java"); 
                        s.execute("create procedure GETPCTEST4Bx(out retparam INTEGER) "+
				"language java external name " +
				"'org.apache.derbyTesting.functionTests.tests.jdbcapi.metadata.getpc4b'" +
				" parameter style java"); 

						// Create functions so we can test
						// getFunctions()
						s.execute("CREATE FUNCTION DUMMY1 ( X SMALLINT ) "+
								  "RETURNS SMALLINT PARAMETER STYLE JAVA "+
								  "NO SQL LANGUAGE JAVA EXTERNAL "+
								  "NAME 'java.some.func'");
						s.execute("CREATE FUNCTION DUMMY2 ( X INTEGER, Y "+
								  "SMALLINT ) RETURNS INTEGER PARAMETER STYLE"+
								  " JAVA NO SQL LANGUAGE JAVA "+
								  "EXTERNAL NAME 'java.some.func'");
						s.execute("CREATE FUNCTION DUMMY3 ( X VARCHAR(16), "+
								  "Y INTEGER ) RETURNS VARCHAR(16) PARAMETER"+
								  " STYLE JAVA NO SQL LANGUAGE"+
								  " JAVA EXTERNAL NAME 'java.some.func'");
						s.execute("CREATE FUNCTION DUMMY4 ( X VARCHAR(128), "+
								  "Y INTEGER ) RETURNS INTEGER PARAMETER "+
								  "STYLE JAVA NO SQL LANGUAGE "+
								  "JAVA EXTERNAL NAME 'java.some.func'");
						
			met = con.getMetaData();

			System.out.println("JDBC Driver '" + met.getDriverName() +
							   "', version " + met.getDriverMajorVersion() +
							   "." + met.getDriverMinorVersion() +
							   " (" + met.getDriverVersion() + ")");

			boolean pass = false;
			try {
				pass = TestUtil.compareURL(met.getURL());				 
			}catch (NoSuchMethodError msme) {
				// DatabaseMetaData.getURL not present - correct for JSR169
				if(!TestUtil.HAVE_DRIVER_CLASS)
					pass = true;
			} catch (Throwable err) {
			    System.out.println("%%getURL() gave the exception: " + err);
			}
			
			if(pass)
				System.out.println("DatabaseMetaData.getURL test passed");
			else
				System.out.println("FAIL: DatabaseMetaData.getURL test failed");

			System.out.println("allTablesAreSelectable(): " +
							   met.allTablesAreSelectable());
			
			System.out.println("maxColumnNameLength(): " + met.getMaxColumnNameLength());

			System.out.println();
			System.out.println("getSchemas():");
			dumpRS(met.getSchemas());

			testGetSchemasWithTwoParams(met);

			System.out.println();
			System.out.println("getCatalogs():");
			dumpRS(met.getCatalogs());

			System.out.println("getSearchStringEscape(): " +
							   met.getSearchStringEscape());

			System.out.println("getSQLKeywords(): " +
							   met.getSQLKeywords());

			System.out.println("getDefaultTransactionIsolation(): " +
							   met.getDefaultTransactionIsolation());

			System.out.println("getProcedures():");
			dumpRS(GET_PROCEDURES, getMetaDataRS(met, GET_PROCEDURES,
				new String [] {null, "%", "GETPCTEST%"},
				null, null, null));

			// Using reflection to check if we have getFunctions in the
			// the current version of Derby
			try {
				Class s = "".getClass();
				
				// Make sure the method is actually implemented
				java.lang.reflect.Method gf = 
					met.getClass().getMethod("getFunctions", 
											 new Class [] { s, s, s });
				if (!java.lang.reflect.Modifier.isAbstract(gf.getModifiers())){
					// Any function in any schema in any catalog
					System.out.println("getFunctions(null,null,null):");
					dumpRS(IGNORE_PROC_ID, (ResultSet)gf.
						   invoke(met, new String [] {null, null, null}));

					// Any function in any schema in "Dummy
					// Catalog". Same as above since the catalog
					// argument is ignored (is always null)
					System.out.println("getFunctions(\"Dummy Catalog\",null,"+
									   "null):");
					dumpRS(IGNORE_PROC_ID, (ResultSet)gf.
						   invoke(met, new String [] {"Dummy Catalog", 
													  null, null}));

					// Any function in a schema starting with "SYS"
					System.out.println("getFunctions(null,\"%SYS%\",null):");
					dumpRS(IGNORE_PROC_ID, (ResultSet)gf.
						   invoke(met, new String [] {null, "SYS%", null}));

					// All functions containing "GET" in any schema
					// (and any catalog)
					System.out.println("getFunctions(null,null,\"%GET%\"):");
					dumpRS(IGNORE_PROC_ID, (ResultSet)gf.
						   invoke(met, new String [] {null, null, "%GET%"}));

					// Any function that belongs to NO schema and
					// NO catalog (none)
					System.out.println("getFunctions(\"\",\"\",null):");
					dumpRS(IGNORE_PROC_ID, (ResultSet)gf.
						   invoke(met, new String [] {"", "", null}));

				}
				
				// Test getFunctionParameters(String,String,String,String)
				java.lang.reflect.Method gfp = 
					met.getClass().getMethod("getFunctionParameters", 
											 new Class [] { s, s, s, s });

				if (!java.lang.reflect.Modifier.
					isAbstract(gfp.getModifiers())){
					System.out.println("getFunctionParameters(null,"+
									   "null,null,null):");
					dumpRS(IGNORE_PROC_ID, (ResultSet)gfp.
						   invoke(met, 
								  new String [] {null, null, null, null}));

					System.out.println("getFunctionParameters(null,\"APP\","+
									   "\"DUMMY%\",\"X\"):");
					dumpRS(IGNORE_PROC_ID, (ResultSet)gfp.
						   invoke(met, 
								  new String [] {null, "APP", "DUMMY%", "X"}));

					System.out.println("getFunctionParameters(null,\"APP\","+
									   "\"DUMMY%\",\"\"):");
					dumpRS(IGNORE_PROC_ID, (ResultSet)gfp.
						   invoke(met, 
								  new String [] {null, "APP", "DUMMY%", ""}));

				}
			} 
			catch (NoSuchMethodException e) {
				if (org.apache.derby.iapi.services.info.JVMInfo.JDK_ID >= 
					org.apache.derby.iapi.services.info.JVMInfo.J2SE_16) {
					e.printStackTrace();
				}
			}
			catch (Exception e) { e.printStackTrace(); }

			System.out.println("getUDTs() with user-named types null :");
 			dumpRS(met.getUDTs(null, null, null, null));

			System.out.println("getUDTs() with user-named types in ('JAVA_OBJECT') :");
 			int[] userNamedTypes = new int[1];
 			userNamedTypes[0] = java.sql.Types.JAVA_OBJECT;
 			dumpRS(met.getUDTs("a", null, null, userNamedTypes));      

 			System.out.println("getUDTs() with user-named types in ('STRUCT') :");
 			userNamedTypes[0] = java.sql.Types.STRUCT;
 			dumpRS(met.getUDTs("b", null, null, userNamedTypes));

 			System.out.println("getUDTs() with user-named types in ('DISTINCT') :");
 			userNamedTypes[0] = java.sql.Types.DISTINCT;
 			dumpRS(met.getUDTs("c", null, null, userNamedTypes));

			System.out.println("getUDTs() with user-named types in ('JAVA_OBJECT', 'STRUCT') :");
 			userNamedTypes = new int[2];
 			userNamedTypes[0] = java.sql.Types.JAVA_OBJECT;
 			userNamedTypes[1] = java.sql.Types.STRUCT;
 			dumpRS(met.getUDTs("a", null, null, userNamedTypes));

			testGetClientInfoProperties(met);

			/*
			 * any methods that were not tested above using code written
			 * specifically for it will now be tested in a generic way.
			 */


			System.out.println("allProceduresAreCallable(): " +
							   met.allProceduresAreCallable());
			System.out.println("getUserName(): " +
							   met.getUserName());
			System.out.println("isReadOnly(): " +
							   met.isReadOnly());
			System.out.println("nullsAreSortedHigh(): " +
							   met.nullsAreSortedHigh());
			System.out.println("nullsAreSortedLow(): " +
							   met.nullsAreSortedLow());
			System.out.println("nullsAreSortedAtStart(): " +
							   met.nullsAreSortedAtStart());
			System.out.println("nullsAreSortedAtEnd(): " +
							   met.nullsAreSortedAtEnd());


			System.out.println("getDatabaseProductName(): " + met.getDatabaseProductName());

			String v = met.getDatabaseProductVersion();
			int l = v.indexOf('(');
			if (l<0) l = v.length();
			v = v.substring(0,l);
			System.out.println("getDatabaseProductVersion(): " + v);
			System.out.println("getDriverVersion(): " +
							   met.getDriverVersion());
			System.out.println("usesLocalFiles(): " +
							   met.usesLocalFiles());
			System.out.println("usesLocalFilePerTable(): " +
							   met.usesLocalFilePerTable());
			System.out.println("supportsMixedCaseIdentifiers(): " +
							   met.supportsMixedCaseIdentifiers());
			System.out.println("storesUpperCaseIdentifiers(): " +
							   met.storesUpperCaseIdentifiers());
			System.out.println("storesLowerCaseIdentifiers(): " +
							   met.storesLowerCaseIdentifiers());
			System.out.println("storesMixedCaseIdentifiers(): " +
							   met.storesMixedCaseIdentifiers());
			System.out.println("supportsMixedCaseQuotedIdentifiers(): " +
							   met.supportsMixedCaseQuotedIdentifiers());
			System.out.println("storesUpperCaseQuotedIdentifiers(): " +
							   met.storesUpperCaseQuotedIdentifiers());
			System.out.println("storesLowerCaseQuotedIdentifiers(): " +
							   met.storesLowerCaseQuotedIdentifiers());
			System.out.println("storesMixedCaseQuotedIdentifiers(): " +
							   met.storesMixedCaseQuotedIdentifiers());
			System.out.println("getIdentifierQuoteString(): " +
							   met.getIdentifierQuoteString());
			System.out.println("getNumericFunctions(): " +
							   met.getNumericFunctions());
			System.out.println("getStringFunctions(): " +
							   met.getStringFunctions());
			System.out.println("getSystemFunctions(): " +
							   met.getSystemFunctions());
			System.out.println("getTimeDateFunctions(): " +
							   met.getTimeDateFunctions());
			System.out.println("getExtraNameCharacters(): " +
							   met.getExtraNameCharacters());
			System.out.println("supportsAlterTableWithAddColumn(): " +
							   met.supportsAlterTableWithAddColumn());
			System.out.println("supportsAlterTableWithDropColumn(): " +
							   met.supportsAlterTableWithDropColumn());
			System.out.println("supportsColumnAliasing(): " +
							   met.supportsColumnAliasing());
			System.out.println("nullPlusNonNullIsNull(): " +
							   met.nullPlusNonNullIsNull());
			System.out.println("supportsConvert(): " +
							   met.supportsConvert());
			System.out.println("supportsConvert(Types.INTEGER, Types.SMALLINT): " +
							   met.supportsConvert(Types.INTEGER, Types.SMALLINT));
			System.out.println("supportsTableCorrelationNames(): " +
							   met.supportsTableCorrelationNames());
			System.out.println("supportsDifferentTableCorrelationNames(): " +
							   met.supportsDifferentTableCorrelationNames());
			System.out.println("supportsExpressionsInOrderBy(): " +
							   met.supportsExpressionsInOrderBy());
			System.out.println("supportsOrderByUnrelated(): " +
							   met.supportsOrderByUnrelated());
			System.out.println("supportsGroupBy(): " +
							   met.supportsGroupBy());
			System.out.println("supportsGroupByUnrelated(): " +
							   met.supportsGroupByUnrelated());
			System.out.println("supportsGroupByBeyondSelect(): " +
							   met.supportsGroupByBeyondSelect());
			System.out.println("supportsLikeEscapeClause(): " +
							   met.supportsLikeEscapeClause());
			System.out.println("supportsMultipleResultSets(): " +
							   met.supportsMultipleResultSets());
			System.out.println("supportsMultipleTransactions(): " +
							   met.supportsMultipleTransactions());
			System.out.println("supportsNonNullableColumns(): " +
							   met.supportsNonNullableColumns());
			System.out.println("supportsMinimumSQLGrammar(): " +
							   met.supportsMinimumSQLGrammar());
			System.out.println("supportsCoreSQLGrammar(): " +
							   met.supportsCoreSQLGrammar());
			System.out.println("supportsExtendedSQLGrammar(): " +
							   met.supportsExtendedSQLGrammar());
			System.out.println("supportsANSI92EntryLevelSQL(): " +
							   met.supportsANSI92EntryLevelSQL());
			System.out.println("supportsANSI92IntermediateSQL(): " +
							   met.supportsANSI92IntermediateSQL());
			System.out.println("supportsANSI92FullSQL(): " +
							   met.supportsANSI92FullSQL());
			System.out.println("supportsIntegrityEnhancementFacility(): " +
							   met.supportsIntegrityEnhancementFacility());
			System.out.println("supportsOuterJoins(): " +
							   met.supportsOuterJoins());
			System.out.println("supportsFullOuterJoins(): " +
							   met.supportsFullOuterJoins());
			System.out.println("supportsLimitedOuterJoins(): " +
							   met.supportsLimitedOuterJoins());
			System.out.println("getSchemaTerm(): " +
							   met.getSchemaTerm());
			System.out.println("getProcedureTerm(): " +
							   met.getProcedureTerm());
			System.out.println("getCatalogTerm(): " +
							   met.getCatalogTerm());
			System.out.println("isCatalogAtStart(): " +
							   met.isCatalogAtStart());
			System.out.println("getCatalogSeparator(): " +
							   met.getCatalogSeparator());
			System.out.println("supportsSchemasInDataManipulation(): " +
							   met.supportsSchemasInDataManipulation());
			System.out.println("supportsSchemasInProcedureCalls(): " +
							   met.supportsSchemasInProcedureCalls());
			System.out.println("supportsSchemasInTableDefinitions(): " +
							   met.supportsSchemasInTableDefinitions());
			System.out.println("supportsSchemasInIndexDefinitions(): " +
							   met.supportsSchemasInIndexDefinitions());
			System.out.println("supportsSchemasInPrivilegeDefinitions(): " +
							   met.supportsSchemasInPrivilegeDefinitions());
			System.out.println("supportsCatalogsInDataManipulation(): " +
							   met.supportsCatalogsInDataManipulation());
			System.out.println("supportsCatalogsInProcedureCalls(): " +
							   met.supportsCatalogsInProcedureCalls());
			System.out.println("supportsCatalogsInTableDefinitions(): " +
							   met.supportsCatalogsInTableDefinitions());
			System.out.println("supportsCatalogsInIndexDefinitions(): " +
							   met.supportsCatalogsInIndexDefinitions());
			System.out.println("supportsCatalogsInPrivilegeDefinitions(): " +
							   met.supportsCatalogsInPrivilegeDefinitions());
			System.out.println("supportsPositionedDelete(): " +
							   met.supportsPositionedDelete());
			System.out.println("supportsPositionedUpdate(): " +
							   met.supportsPositionedUpdate());
			System.out.println("supportsSelectForUpdate(): " +
							   met.supportsSelectForUpdate());
			System.out.println("supportsStoredProcedures(): " +
							   met.supportsStoredProcedures());
			System.out.println("supportsSubqueriesInComparisons(): " +
							   met.supportsSubqueriesInComparisons());
			System.out.println("supportsSubqueriesInExists(): " +
							   met.supportsSubqueriesInExists());
			System.out.println("supportsSubqueriesInIns(): " +
							   met.supportsSubqueriesInIns());
			System.out.println("supportsSubqueriesInQuantifieds(): " +
							   met.supportsSubqueriesInQuantifieds());
			System.out.println("supportsCorrelatedSubqueries(): " +
							   met.supportsCorrelatedSubqueries());
			System.out.println("supportsUnion(): " +
							   met.supportsUnion());
			System.out.println("supportsUnionAll(): " +
							   met.supportsUnionAll());
			System.out.println("supportsOpenCursorsAcrossCommit(): " +
							   met.supportsOpenCursorsAcrossCommit());
			System.out.println("supportsOpenCursorsAcrossRollback(): " +
							   met.supportsOpenCursorsAcrossRollback());
			System.out.println("supportsOpenStatementsAcrossCommit(): " +
							   met.supportsOpenStatementsAcrossCommit());
			System.out.println("supportsOpenStatementsAcrossRollback(): " +
							   met.supportsOpenStatementsAcrossRollback());
			System.out.println("getMaxBinaryLiteralLength(): " +
							   met.getMaxBinaryLiteralLength());
			System.out.println("getMaxCharLiteralLength(): " +
							   met.getMaxCharLiteralLength());
			System.out.println("getMaxColumnsInGroupBy(): " +
							   met.getMaxColumnsInGroupBy());
			System.out.println("getMaxColumnsInIndex(): " +
							   met.getMaxColumnsInIndex());
			System.out.println("getMaxColumnsInOrderBy(): " +
							   met.getMaxColumnsInOrderBy());
			System.out.println("getMaxColumnsInSelect(): " +
							   met.getMaxColumnsInSelect());
			System.out.println("getMaxColumnsInTable(): " +
							   met.getMaxColumnsInTable());
			System.out.println("getMaxConnections(): " +
							   met.getMaxConnections());
			System.out.println("getMaxCursorNameLength(): " +
							   met.getMaxCursorNameLength());
			System.out.println("getMaxIndexLength(): " +
							   met.getMaxIndexLength());
			System.out.println("getMaxSchemaNameLength(): " +
							   met.getMaxSchemaNameLength());
			System.out.println("getMaxProcedureNameLength(): " +
							   met.getMaxProcedureNameLength());
			System.out.println("getMaxCatalogNameLength(): " +
							   met.getMaxCatalogNameLength());
			System.out.println("getMaxRowSize(): " +
							   met.getMaxRowSize());
			System.out.println("doesMaxRowSizeIncludeBlobs(): " +
							   met.doesMaxRowSizeIncludeBlobs());
			System.out.println("getMaxStatementLength(): " +
							   met.getMaxStatementLength());
			System.out.println("getMaxStatements(): " +
							   met.getMaxStatements());
			System.out.println("getMaxTableNameLength(): " +
							   met.getMaxTableNameLength());
			System.out.println("getMaxTablesInSelect(): " +
							   met.getMaxTablesInSelect());
			System.out.println("getMaxUserNameLength(): " +
							   met.getMaxUserNameLength());
			System.out.println("supportsTransactions(): " +
							   met.supportsTransactions());
			System.out.println("supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE): " +
							   met.supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE));
			System.out.println("supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ): " +
							   met.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ));
			System.out.println("supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE): " +
							   met.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE));
			System.out.println("supportsDataDefinitionAndDataManipulationTransactions(): " +
							   met.supportsDataDefinitionAndDataManipulationTransactions());
			System.out.println("supportsDataManipulationTransactionsOnly(): " +
							   met.supportsDataManipulationTransactionsOnly());
			System.out.println("dataDefinitionCausesTransactionCommit(): " +
							   met.dataDefinitionCausesTransactionCommit());
			System.out.println("dataDefinitionIgnoredInTransactions(): " +
							   met.dataDefinitionIgnoredInTransactions());

			System.out.println("Test the metadata calls related to visibility of changes made by others for different resultset types");
			System.out.println("Since Derby materializes a forward only ResultSet incrementally, it is possible to see changes");
			System.out.println("made by others and hence following 3 metadata calls will return true for forward only ResultSets.");
			System.out.println("othersUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY)? " + met.othersUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
			System.out.println("othersDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY)? " + met.othersDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
			System.out.println("othersInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY)? " + met.othersInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
			System.out.println("Scroll insensitive ResultSet by their definition do not see changes made by others and hence following metadata calls return false");
			System.out.println("othersUpdatesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE)? " + met.othersUpdatesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
			System.out.println("othersDeletesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE)? " + met.othersDeletesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
			System.out.println("othersInsertsAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE)? " + met.othersInsertsAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
			System.out.println("Derby does not yet implement scroll sensitive resultsets and hence following metadata calls return false");
			System.out.println("othersUpdatesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE)? " + met.othersUpdatesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
			System.out.println("othersDeletesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE)? " + met.othersDeletesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
			System.out.println("othersInsertsAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE)? " + met.othersInsertsAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));

 			System.out.println("Test the metadata calls related to visibility of *own* changes for different resultset types");
 			System.out.println("ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY)? " + met.ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
 			System.out.println("ownDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY)? " + met.ownDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
 			System.out.println("ownInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY)? " + met.ownInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
 			System.out.println("Scroll insensitive ResultSet see updates and deletes, but not inserts");
 			System.out.println("ownUpdatesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE)? " + met.ownUpdatesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
 			System.out.println("ownDeletesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE)? " + met.ownDeletesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
 			System.out.println("ownInsertsAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE)? " + met.ownInsertsAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
 			System.out.println("Derby does not yet implement scroll sensitive resultsets and hence following metadata calls return false");
 			System.out.println("ownUpdatesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE)? " + met.ownUpdatesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
 			System.out.println("ownDeletesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE)? " + met.ownDeletesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
 			System.out.println("ownInsertsAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE)? " + met.othersInsertsAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
 
 			System.out.println("Test the metadata calls related to detectability of visible changes for different resultset types");
 			System.out.println("Expect true for updates and deletes of TYPE_SCROLL_INSENSITIVE, all others should be false");
 			System.out.println("updatesAreDetected(ResultSet.TYPE_FORWARD_ONLY)? " + met.updatesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
 			System.out.println("deletesAreDetected(ResultSet.TYPE_FORWARD_ONLY)? " + met.deletesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
 			System.out.println("insertsAreDetected(ResultSet.TYPE_FORWARD_ONLY)? " + met.insertsAreDetected(ResultSet.TYPE_FORWARD_ONLY));
 			System.out.println("updatesAreDetected(ResultSet.TYPE_SCROLL_INSENSITIVE)? " + met.updatesAreDetected(ResultSet.TYPE_SCROLL_INSENSITIVE));
 			System.out.println("deletesAreDetected(ResultSet.TYPE_SCROLL_INSENSITIVE)? " + met.deletesAreDetected(ResultSet.TYPE_SCROLL_INSENSITIVE));
 			System.out.println("insertsAreDetected(ResultSet.TYPE_SCROLL_INSENSITIVE)? " + met.insertsAreDetected(ResultSet.TYPE_SCROLL_INSENSITIVE));
 			System.out.println("updatesAreDetected(ResultSet.TYPE_SCROLL_SENSITIVE)? " + met.updatesAreDetected(ResultSet.TYPE_SCROLL_SENSITIVE));
 			System.out.println("deletesAreDetected(ResultSet.TYPE_SCROLL_SENSITIVE)? " + met.deletesAreDetected(ResultSet.TYPE_SCROLL_SENSITIVE));
 			System.out.println("insertsAreDetected(ResultSet.TYPE_SCROLL_SENSITIVE)? " + met.insertsAreDetected(ResultSet.TYPE_SCROLL_SENSITIVE));
            
			if (!TestUtil.isJCCFramework()) { // gives false on all.. bug
				int[] types = {ResultSet.TYPE_FORWARD_ONLY, 
							   ResultSet.TYPE_SCROLL_INSENSITIVE,
							   ResultSet.TYPE_SCROLL_SENSITIVE};
	 
				int[] conc  = {ResultSet.CONCUR_READ_ONLY, 
							   ResultSet.CONCUR_UPDATABLE};

				String[] typesStr = {"TYPE_FORWARD_ONLY", 
									 "TYPE_SCROLL_INSENSITIVE",
									 "TYPE_SCROLL_SENSITIVE"};
			
				String[] concStr  = {"CONCUR_READ_ONLY", 
									 "CONCUR_UPDATABLE"};
	 
				for (int i = 0; i < types.length ; i++) {
					for (int j = 0; j < conc.length; j++) {
						System.out.println
							("SupportsResultSetConcurrency: " +
							 typesStr[i] + "," + concStr[j] + ": " +
							 met.supportsResultSetConcurrency(types[i], 
															  conc[j]));
					}
				}
			}

			System.out.println("getConnection(): "+
					   ((met.getConnection()==con)?"same connection":"different connection") );
			System.out.println("getProcedureColumns():");
			dumpRS(GET_PROCEDURE_COLUMNS, getMetaDataRS(met, GET_PROCEDURE_COLUMNS,
				new String [] {null, "%", "GETPCTEST%", "%"},
				null, null, null));

 			System.out.println("getTables() with TABLE_TYPE in ('SYSTEM TABLE') :");
 			String[] tabTypes = new String[1];
 			tabTypes[0] = "SYSTEM TABLE";
 			dumpRS(GET_TABLES, getMetaDataRS(met, GET_TABLES,
				new String [] {null, null, null},
 				tabTypes, null, null));

			System.out.println("getTables() with no types:");
 			dumpRS(GET_TABLES, getMetaDataRS(met, GET_TABLES,
				new String [] {"", null, "%"},
				null, null, null));

 			System.out.println("getTables() with TABLE_TYPE in ('VIEW','TABLE') :");
 			tabTypes = new String[2];
 			tabTypes[0] = "VIEW";
 			tabTypes[1] = "TABLE";
 			dumpRS(GET_TABLES, getMetaDataRS(met, GET_TABLES,
				new String [] {null, null, null},
 				tabTypes, null, null));


			System.out.println("getTableTypes():");
			dumpRS(met.getTableTypes());

			System.out.println("getColumns():");
			dumpRS(GET_COLUMNS, getMetaDataRS(met, GET_COLUMNS,
				new String [] {"", null, "", ""},
				null, null, null));

			System.out.println("getColumns('SYSTABLES'):");
			dumpRS(GET_COLUMNS, getMetaDataRS(met, GET_COLUMNS,
				new String [] {"", "SYS", "SYSTABLES", null},
				null, null, null));

			System.out.println("getColumns('ALLTYPES'):");
			dumpRS(GET_COLUMNS, getMetaDataRS(met, GET_COLUMNS,
				new String [] {"", "APP", "ALLTYPES", null},
				null, null, null));

			System.out.println("getColumns('LOUIE'):");
			dumpRS(GET_COLUMNS, getMetaDataRS(met, GET_COLUMNS,
				new String [] {"", "APP", "LOUIE", null},
				null, null, null));

			// test for beetle 4620
			System.out.println("getColumns('INFLIGHT'):");
			dumpRS(GET_COLUMNS, getMetaDataRS(met, GET_COLUMNS,
				new String [] {"", "APP", "INFLIGHT", null},
				null, null, null));

			System.out.println("getColumnPrivileges():");
			dumpRS(GET_COLUMN_PRIVILEGES, getMetaDataRS(met, GET_COLUMN_PRIVILEGES,
				new String [] {"Huey", "Dewey", "Louie", "Frooey"},
				null, null, null));

			System.out.println("getTablePrivileges():");
			dumpRS(GET_TABLE_PRIVILEGES, getMetaDataRS(met, GET_TABLE_PRIVILEGES,
				new String [] {"Huey", "Dewey", "Louie"},
				null, null, null));

			System.out.println("getBestRowIdentifier(\"\",null,\"LOUIE\"):");
			dumpRS(GET_BEST_ROW_IDENTIFIER, getMetaDataRS(met, GET_BEST_ROW_IDENTIFIER,
				new String [] {"", null, "LOUIE"}, null,
				new int [] {DatabaseMetaData.bestRowTransaction},
				new boolean [] {true}));

			System.out.println("getBestRowIdentifier(\"\",\"SYS\",\"SYSTABLES\"):");
			dumpRS(GET_BEST_ROW_IDENTIFIER, getMetaDataRS(met, GET_BEST_ROW_IDENTIFIER,
				new String [] {"", "SYS", "SYSTABLES"}, null,
				new int [] {DatabaseMetaData.bestRowTransaction},
				new boolean [] {true}));

			System.out.println("getVersionColumns():");
			dumpRS(GET_VERSION_COLUMNS, getMetaDataRS(met, GET_VERSION_COLUMNS,
				new String [] {"Huey", "Dewey", "Louie"},
				null, null, null));

			System.out.println("getPrimaryKeys():");
			dumpRS(GET_PRIMARY_KEYS, getMetaDataRS(met, GET_PRIMARY_KEYS,
				new String [] {"", "%", "LOUIE"},
				null, null, null));

			//beetle 4571
			System.out.println("getPrimaryKeys(null, null, tablename):");
			dumpRS(GET_PRIMARY_KEYS, getMetaDataRS(met, GET_PRIMARY_KEYS,
				new String [] {null, null, "LOUIE"},
				null, null, null));

			System.out.println("getImportedKeys():");
			dumpRS(GET_IMPORTED_KEYS, getMetaDataRS(met, GET_IMPORTED_KEYS,
				new String [] {null, null, "%"},
				null, null, null));

			System.out.println("getExportedKeys():");
			dumpRS(GET_EXPORTED_KEYS, getMetaDataRS(met, GET_EXPORTED_KEYS,
				new String [] {null, null, "%"},
				null, null, null));

			System.out.println("---------------------------------------");
			System.out.println("getCrossReference('',null,'louie','',null,'reftab' ):");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", null, "LOUIE", "", null, "REFTAB"},
				null, null, null));

			System.out.println("\ngetCrossReference('','APP','reftab','',null,'reftab' ):");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", "APP", "REFTAB", "", null, "REFTAB"},
				null, null, null));

			System.out.println("\ngetCrossReference('',null,null,'','APP','reftab' ):");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", null, "%", "", "APP", "REFTAB"},
				null, null, null));

			System.out.println("\ngetImportedKeys('',null,null,'','APP','reftab' ):");
			dumpRS(GET_IMPORTED_KEYS, getMetaDataRS(met, GET_IMPORTED_KEYS,
				new String [] {"", "APP", "REFTAB"},
				null, null, null));

			System.out.println("\ngetCrossReference('',null,'louie','','APP',null):");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", null, "LOUIE", "", "APP", "%"},
				null, null, null));

			System.out.println("\ngetExportedKeys('',null,'louie,'','APP',null ):");
			dumpRS(GET_EXPORTED_KEYS, getMetaDataRS(met, GET_EXPORTED_KEYS,
				new String [] {"", null, "LOUIE"},
				null, null, null));

			System.out.println("\ngetCrossReference('','badschema','LOUIE','','APP','REFTAB' ):");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", "BADSCHEMA", "LOUIE", "", "APP", "REFTAB"},
				null, null, null));

			System.out.println("getTypeInfo():");
			dumpRS(GET_TYPE_INFO, getMetaDataRS(met, GET_TYPE_INFO, null, null, null, null));

			/* NOTE - we call getIndexInfo() only on system tables here
 			 * so that there will be no diffs due to generated names.
			 */
			// unique indexes on SYSCOLUMNS
			System.out.println("getIndexInfo():");
			dumpRS(GET_INDEX_INFO, getMetaDataRS(met, GET_INDEX_INFO,
				new String [] {"", "SYS", "SYSCOLUMNS"},
				null, null, new boolean [] {true, false}));

			// all indexes on SYSCOLUMNS
			System.out.println("getIndexInfo():");
			dumpRS(GET_INDEX_INFO, getMetaDataRS(met, GET_INDEX_INFO,
				new String [] {"", "SYS", "SYSCOLUMNS"},
				null, null, new boolean [] {false, false}));

			System.out.println("getIndexInfo():");
			dumpRS(GET_INDEX_INFO, getMetaDataRS(met, GET_INDEX_INFO,
				new String [] {"", "SYS", "SYSTABLES"},
				null, null, new boolean [] {true, false}));

			rs = s.executeQuery("SELECT * FROM SYS.SYSTABLES");

			System.out.println("getColumns('SYSTABLES'):");
			dumpRS(GET_COLUMNS, getMetaDataRS(met, GET_COLUMNS,
				new String [] {"", "SYS", "SYSTABLES", null},
				null, null, null));
			
			try {
				if (!rs.next()) {
					System.out.println("FAIL -- user result set closed by"+
					" intervening getColumns request");
				}
			} catch (SQLException se) {
				if (this instanceof metadata) {
					System.out.println("FAIL -- user result set closed by"+
					" intervening getColumns request");
				}
				else {
					System.out.println("OK -- user result set closed by"+
					" intervening OBDC getColumns request; this was" +
					" expected because of the way the test works.");
				}
			}
			rs.close();
			
			System.out.println("Test escaped numeric functions - JDBC 3.0 C.1");
			testEscapedFunctions(con, NUMERIC_FUNCTIONS, met.getNumericFunctions());
			
			System.out.println("Test escaped string functions - JDBC 3.0 C.2");
			testEscapedFunctions(con, STRING_FUNCTIONS, met.getStringFunctions());

			System.out.println("Test escaped date time functions - JDBC 3.0 C.3");
			testEscapedFunctions(con, TIMEDATE_FUNCTIONS, met.getTimeDateFunctions());

			System.out.println("Test escaped system functions - JDBC 3.0 C.4");
			testEscapedFunctions(con, SYSTEM_FUNCTIONS, met.getSystemFunctions());

			//
			// Test referential actions on delete
			//
			System.out.println("---------------------------------------");
			//create tables to test that we get the delete and update 
			// referential action correct
			System.out.println("Referential action values");
			System.out.println("RESTRICT = "+ DatabaseMetaData.importedKeyRestrict);
			System.out.println("NO ACTION = "+ DatabaseMetaData.importedKeyNoAction);
			System.out.println("CASCADE = "+ DatabaseMetaData.importedKeyCascade);
			System.out.println("SETNULL = "+ DatabaseMetaData.importedKeySetNull);
			System.out.println("SETDEFAULT = "+ DatabaseMetaData.importedKeySetDefault);
			s.execute("create table refaction1(a int not null primary key)");
			s.execute("create table refactnone(a int references refaction1(a))");
			s.execute("create table refactrestrict(a int references refaction1(a) on delete restrict)");
			s.execute("create table refactnoaction(a int references refaction1(a) on delete no action)");
			s.execute("create table refactcascade(a int references refaction1(a) on delete cascade)");
			s.execute("create table refactsetnull(a int references refaction1(a) on delete set null)");
			System.out.println("getCrossReference('','APP','REFACTION1','','APP','REFACTIONNONE' ):");
			s.execute("create table refactupdrestrict(a int references refaction1(a) on update restrict)");
			s.execute("create table refactupdnoaction(a int references refaction1(a) on update no action)");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", "APP", "REFACTION1", "", "APP", "REFACTNONE"},
				null, null, null));
			System.out.println("\ngetCrossReference('','APP','REFACTION1','','APP','REFACTRESTRICT' ):");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", "APP", "REFACTION1", "", "APP", "REFACTRESTRICT"},
				null, null, null));
			System.out.println("\ngetCrossReference('','APP','REFACTION1','','APP','REFACTNOACTION' ):");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", "APP", "REFACTION1", "", "APP", "REFACTNOACTION"},
				null, null, null));
			System.out.println("\ngetCrossReference('','APP','REFACTION1','','APP','REFACTCASCADE' ):");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", "APP", "REFACTION1", "", "APP", "REFACTCASCADE"},
				null, null, null));
			System.out.println("\ngetCrossReference('','APP','REFACTION1','','APP','REFACTSETNULL' ):");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", "APP", "REFACTION1", "", "APP", "REFACTSETNULL"},
				null, null, null));
			System.out.println("\ngetCrossReference('','APP','REFACTION1','','APP','REFACTUPDRESTRICT' ):");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", "APP", "REFACTION1", "", "APP", "REFACTUPDRESTRICT"},
				null, null, null));
			System.out.println("\ngetCrossReference('','APP','REFACTION1','','APP','REFACTUPDNOACTION' ):");
			dumpRS(GET_CROSS_REFERENCE, getMetaDataRS(met, GET_CROSS_REFERENCE,
				new String [] {"", "APP", "REFACTION1", "", "APP", "REFACTUPDNOACTION"},
				null, null, null));

			ResultSet refrs = getMetaDataRS(met, GET_IMPORTED_KEYS,
				new String [] {"", "APP", "REFACTNONE"}, null, null, null);

			if (refrs.next())
			{
				//check update rule
				if (refrs.getShort(11) != DatabaseMetaData.importedKeyNoAction)
					System.out.println("\ngetImportedKeys - none update Failed - action = " + refrs.getShort(11) + " required value = " + DatabaseMetaData.importedKeyNoAction);
				else
					System.out.println("\ngetImportedKeys - none update Passed");
				//check delete rule
				if (refrs.getShort(11) != DatabaseMetaData.importedKeyNoAction)
					System.out.println("\ngetImportedKeys - none delete Failed - action = " + refrs.getShort(11) + " required value = " + DatabaseMetaData.importedKeyNoAction);
				else
					System.out.println("\ngetImportedKeys - none delete Passed");
			}
			else
					System.out.println("\ngetImportedKeys - none Failed no rows");
					
			refrs.close();
			refrs = getMetaDataRS(met, GET_IMPORTED_KEYS,
				new String [] {"", "APP", "REFACTRESTRICT"}, null, null, null);

			if (refrs.next())
			{
				if (refrs.getShort(11) != DatabaseMetaData.importedKeyRestrict)
					System.out.println("\ngetImportedKeys - delete Restrict Failed - action = " + refrs.getShort(11) + " required value = " + DatabaseMetaData.importedKeyRestrict);
				else
					System.out.println("\ngetImportedKeys - delete Restrict Passed");
			}
			else
					System.out.println("\ngetImportedKeys - delete Restrict Failed no rows");

			refrs.close();
			refrs = getMetaDataRS(met, GET_IMPORTED_KEYS,
				new String [] {"", "APP", "REFACTNOACTION"}, null, null, null);

			if (refrs.next())
			{
				if (refrs.getShort(11) != DatabaseMetaData.importedKeyNoAction)
					System.out.println("\ngetImportedKeys - delete NO ACTION Failed - action = " + refrs.getShort(11) + " required value = " + DatabaseMetaData.importedKeyNoAction);
				else
					System.out.println("\ngetImportedKeys - delete NO ACTION Passed");
			}
			else
					System.out.println("\ngetImportedKeys - delete NO ACTION Failed no rows");

			refrs.close();
			refrs = getMetaDataRS(met, GET_IMPORTED_KEYS,
				new String [] {"", "APP", "REFACTCASCADE"}, null, null, null);

			if (refrs.next())
			{
				if (refrs.getShort(11) != DatabaseMetaData.importedKeyCascade)
					System.out.println("\ngetImportedKeys - delete CASCADE Failed - action = " + refrs.getShort(11) + " required value = " + DatabaseMetaData.importedKeyCascade);
				else
					System.out.println("\ngetImportedKeys - delete CASCADE Passed");
			}
			else
					System.out.println("\ngetImportedKeys - delete CASCADE Failed no rows");

			refrs.close();
			refrs = getMetaDataRS(met, GET_IMPORTED_KEYS,
				new String [] {"", "APP", "REFACTSETNULL"}, null, null, null);

			if (refrs.next())
			{
				if (refrs.getShort(11) != DatabaseMetaData.importedKeySetNull)
					System.out.println("\ngetImportedKeys - delete SET NULL Failed - action = " + refrs.getShort(11) + " required value = " + DatabaseMetaData.importedKeySetNull);
				else
					System.out.println("\ngetImportedKeys - delete SET NULL Passed");
			}
			else
					System.out.println("\ngetImportedKeys - SET NULL Failed no rows");

			refrs.close();
			refrs = getMetaDataRS(met, GET_IMPORTED_KEYS,
				new String [] {"", "APP", "REFACTRESTRICT"}, null, null, null);

			if (refrs.next())
			{
				// test update rule
				if (refrs.getShort(11) != DatabaseMetaData.importedKeyRestrict)
					System.out.println("\ngetImportedKeys - update Restrict Failed - action = " + refrs.getShort(11) + " required value = " + DatabaseMetaData.importedKeyRestrict);
				else
					System.out.println("\ngetImportedKeys - update Restrict Passed");
			}
			else
					System.out.println("\ngetImportedKeys - update Restrict Failed no rows");

			refrs.close();
			refrs = getMetaDataRS(met, GET_IMPORTED_KEYS,
				new String [] {"", "APP", "REFACTNOACTION"}, null, null, null);

			if (refrs.next())
			{
				if (refrs.getShort(11) != DatabaseMetaData.importedKeyNoAction)
					System.out.println("\ngetImportedKeys - update NO ACTION Failed - action = " + refrs.getShort(11) + " required value = " + DatabaseMetaData.importedKeyNoAction);
				else
					System.out.println("\ngetImportedKeys - update NO ACTION Passed");
			}
			else
					System.out.println("\ngetImportedKeys - update NO ACTION Failed no rows");
			refrs.close();

			System.out.println("\ngetExportedKeys('',null,null,'','APP','REFACTION1' ):");
			dumpRS(GET_EXPORTED_KEYS, getMetaDataRS(met, GET_EXPORTED_KEYS,
				new String [] {"", "APP", "REFACTION1"},
				null, null, null));

			System.out.println("---------------------------------------");

			// drop referential action test tables
			s.execute("drop table refactnone");
			s.execute("drop table refactupdrestrict");
			s.execute("drop table refactupdnoaction");
			s.execute("drop table refactrestrict");
			s.execute("drop table refactnoaction");
			s.execute("drop table refactcascade");
			s.execute("drop table refactsetnull");
			s.execute("drop table inflight");
			s.execute("drop table refaction1");

			// test beetle 5195
			s.execute("create table t1 (c1 int not null, c2 int, c3 int default null, c4 char(10) not null, c5 char(10) default null, c6 char(10) default 'NULL', c7 int default 88)");

			String schema = "APP";
			String tableName = "T1";
			DatabaseMetaData dmd = con.getMetaData();

			System.out.println("getColumns for '" + tableName + "'");

			rs = getMetaDataRS(dmd, GET_COLUMNS,
				new String [] {null, schema, tableName, null},
				null, null, null);

			try
			{
				while (rs.next())
				{
					String col = rs.getString(4);
					String type = rs.getString(6);
					String defval = rs.getString(13);
					if (defval == null)
						System.out.println("  Next line is real null.");
					System.out.println("defval for col " + col + 
						" type " + type + " DEFAULT '" + defval + "' wasnull " + rs.wasNull());
				}
		
			}
			finally
			{
				if (rs != null)
					rs.close();
			}
			s.execute("drop table t1");

			// test DERBY-655, DERBY-1343
			// If a table has duplicate backing index, then it will share the 
			// physical conglomerate with the existing index, but the duplicate
			// indexes should have their own unique logical congomerates 
			// associated with them. That way, it will be possible to 
			// distinguish the 2 indexes in SYSCONGLOMERATES from each other.
			s.execute("CREATE TABLE Derby655t1(c11_ID BIGINT NOT NULL)");
			s.execute("CREATE TABLE Derby655t2 (c21_ID BIGINT NOT NULL primary key)");
			s.execute("ALTER TABLE Derby655t1 ADD CONSTRAINT F_12 Foreign Key (c11_ID) REFERENCES Derby655t2 (c21_ID) ON DELETE CASCADE ON UPDATE NO ACTION");
			s.execute("CREATE TABLE Derby655t3(c31_ID BIGINT NOT NULL primary key)");
			s.execute("ALTER TABLE Derby655t2 ADD CONSTRAINT F_443 Foreign Key (c21_ID) REFERENCES Derby655t3(c31_ID) ON DELETE CASCADE ON UPDATE NO ACTION");
			dmd = con.getMetaData();
			System.out.println("\ngetImportedKeys('',null,null,'','APP','Derby655t1' ):");
			dumpRS(met.getImportedKeys("", "APP", "DERBY655T1"));
			s.execute("drop table Derby655t1");
			s.execute("drop table Derby655t2");
			s.execute("drop table Derby655t3");

			// tiny test moved over from no longer used metadata2.sql
			// This checks for a bug where you get incorrect behavior on a nested connection.
			// if you do not get an error, the bug does not occur.			
            if(HAVE_DRIVER_CLASS){
            	s.execute("create procedure isReadO() language java external name " +
				"'org.apache.derbyTesting.functionTests.tests.jdbcapi.metadata.isro'" +
				" parameter style java"); 
            	s.execute("call isReadO()");
            }
			cleanUp(s);
	
			s.close();

			if (con.getAutoCommit() == false)
				con.commit();

			
			con.close();

		}
		catch (SQLException e) {
			dumpSQLExceptions(e);
		}
		catch (Throwable e) {
			System.out.println("FAIL -- unexpected exception:");
			e.printStackTrace(System.out);
		}

		System.out.println("Test metadata finished");
    }

	/**
	 * Test escaped functions. Working from the list of escaped functions defined
	 * by JDBC, compared to the list returned by the driver.
	 * <OL>
	 * <LI> See that all functions defined by the driver are in the spec list
	 * and that they work.
	 * <LI> See that only functions defined by the spec are in the driver's list.
	 * <LI> See that any functions defined by the spec that work are in the driver's list.
	 * </OL>
	 * FAIL will be printed for any issues.
	 * @param conn
	 * @param specList
	 * @param metaDataList
	 * @throws SQLException
	 */
	private static void testEscapedFunctions(Connection conn, String[][] specList, String metaDataList)
	throws SQLException
	{
		boolean[] seenFunction = new boolean[specList.length];
		
		System.out.println("TEST FUNCTIONS DECLARED IN DATABASEMETADATA LIST");
		StringTokenizer st = new StringTokenizer(metaDataList, ",");
		while (st.hasMoreTokens())
		{
			String function = st.nextToken();
			
			// find this function in the list
			boolean isSpecFunction = false;
			for (int f = 0; f < specList.length; f++)
			{
				String[] specDetails = specList[f];
				if (function.equals(specDetails[0]))
				{
					// Matched spec.
					if (seenFunction[f])
						System.out.println("FAIL Function in list twice: " + function);
					seenFunction[f] = true;
					isSpecFunction = true;
					
					if (!executeEscaped(conn, specDetails))
						System.out.println("FAIL Function failed to execute "+ function);
					break;
				}
			}
			
			if (!isSpecFunction)
			{
				System.out.println("FAIL Non-JDBC spec function in list: " + function);
			}
		}
		
		// Now see if any speced functions are not in the metadata list
		System.out.println("TEST FUNCTIONS NOT DECLARED IN DATABASEMETADATA LIST");
		for (int f = 0; f < specList.length; f++)
		{
			if (seenFunction[f])
				continue;
			String[] specDetails = specList[f];
			if (executeEscaped(conn, specDetails))
				System.out.println("FAIL function works but not declared in list: " + specDetails[0]);
			
		}
	}
	
	private static boolean executeEscaped(Connection conn, String[] specDetails)
	{
		
		String sql = "VALUES { fn " + specDetails[0] + "(";
		
		for (int p = 0; p < specDetails.length - 1; p++)
		{
			if (p != 0)
				sql = sql + ", ";
			
			sql = sql + specDetails[p + 1];
		}
		
		sql = sql + ") }";
		
		// Special processing for functions that return
		// current date, time or timestamp. This is to
		// ensure we don't have output that depends on
		// the time the test is run.
		if ("CURDATE".equals(specDetails[0]))
			sql = "VALUES CASE WHEN { fn CURDATE()} = CURRENT_DATE THEN 'OK' ELSE 'wrong' END";
		else if ("CURTIME".equals(specDetails[0]))
			sql = "VALUES CASE WHEN { fn CURTIME()} = CURRENT_TIME THEN 'OK' ELSE 'wrong' END";
		else if ("NOW".equals(specDetails[0]))
			sql = "VALUES CASE WHEN { fn NOW()} = CURRENT_TIMESTAMP THEN 'OK' ELSE 'wrong' END";
		
		
		System.out.print("Executing " + sql + " -- ");
			
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next())
			{
				// truncate numbers to avoid multiple master files
				// with double values.
				String res = rs.getString(1);
				
				switch (rs.getMetaData().getColumnType(1))
				{
				case Types.DOUBLE:
				case Types.REAL:
				case Types.FLOAT:
					if (res.length() > 4)
						res = res.substring(0, 4);
					break;
				default:
					break;
				}
				System.out.print("  = >" + res + "< ");
			}
			rs.close();
			ps.close();
			System.out.println(" << ");
			return true;
		} catch (SQLException e) {
			System.out.println("");
			showSQLExceptions(e);
			return false;
		}
		
	}

    /**
     * Run tests for <code>getSchemas()</code> with two
     * parameters. (New method introduced by JDBC 4.0.)
     *
     * @param dmd a <code>DatabaseMetaData</code> object
     */
    private void testGetSchemasWithTwoParams(DatabaseMetaData dmd) {
        // not implemented in JCC
        if (TestUtil.isJCCFramework()) return;

        Class[] paramTypes = { String.class, String.class };

        Method method = null;
        try {
            method = dmd.getClass().getMethod("getSchemas", paramTypes);
        } catch (NoSuchMethodException nsme) { }

        if (method == null || Modifier.isAbstract(method.getModifiers())) {
            System.out.println("DatabaseMetaData.getSchemas(String, String) " +
                               "is not available.");
            return;
        }

        System.out.println();
        System.out.println("getSchemas(String, String):");

        // array of argument lists
        String[][] args = {
            // no qualifiers
            { null, null },
            // wildcard
            { null, "SYS%" },
            // exact match
            { null, "APP" },
            // no match
            { null, "BLAH" },
        };

        for (int i = 0; i < args.length; ++i) {
            try {
                dumpRS((ResultSet) method.invoke(dmd, args[i]));
            } catch (Exception e) {
                dumpAllExceptions(e);
            }
        }
    }

    /**
     * Run tests for <code>getClientInfoProperties()</code> introduced
     * by JDBC 4.0.
     *
     * @param dmd a <code>DatabaseMetaData</code> object
     */
    private void testGetClientInfoProperties(DatabaseMetaData dmd) {
        // not implemented in JCC
        if (TestUtil.isJCCFramework()) return;

        Method method = null;
        try {
            method = dmd.getClass().getMethod("getClientInfoProperties", null);
        } catch (NoSuchMethodException nsme) {}

        if (method == null || Modifier.isAbstract(method.getModifiers())) {
            System.out.println("DatabaseMetaData.getClientInfoProperties() " +
                               "is not available.");
            return;
        }

        System.out.println();
        System.out.println("getClientInfoProperties():");

        try {
            dumpRS((ResultSet) method.invoke(dmd, null));
        } catch (Exception e) {
            dumpAllExceptions(e);
        }
    }

	static private void showSQLExceptions (SQLException se) {
		while (se != null) {
			System.out.println("SQLSTATE("+se.getSQLState()+"): " + se.getMessage());
			se = se.getNextException();
		}
	}
	
	static protected void dumpSQLExceptions (SQLException se) {
		System.out.println("FAIL -- unexpected exception");
		while (se != null) {
			System.out.print("SQLSTATE("+se.getSQLState()+"):");
			se.printStackTrace(System.out);
			se = se.getNextException();
		}
	}

    /**
     * Print the entire exception chain.
     *
     * @param t a <code>Throwable</code>
     */
    private static void dumpAllExceptions(Throwable t) {
        System.out.println("FAIL -- unexpected exception");
        do {
            t.printStackTrace(System.out);
            if (t instanceof SQLException) {
                t = ((SQLException) t).getNextException();
            } else if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException) t).getTargetException();
            } else {
                break;
            }
        } while (t != null);
    }

	/**
	 * This method is responsible for executing a metadata query and returning
	 * the result set.  We do it like this so that the metadata.java and
	 * odbc_metadata.java classes can implement this method in their
	 * own ways (which is needed because we have to extra work to
	 * get the ODBC versions of the metadata).
	 */
	abstract protected ResultSet getMetaDataRS(DatabaseMetaData dmd, int procId,
		String [] sArgs, String [] argArray, int [] iArgs, boolean [] bArgs)
		throws SQLException;

	/**
	 * Dump the values in the received result set to output.
	 */
	protected void dumpRS(ResultSet rs) throws SQLException {
		dumpRS(IGNORE_PROC_ID, rs);
	}

	/**
	 * Dump the values in the received result set to output.
	 */
	abstract protected void dumpRS(int procId, ResultSet s) throws SQLException;

	/**
	 * Create a connect based on the test arguments passed in.
	 */
	protected Connection createConnection(String[] args) throws Exception {

		Connection con;

		// use the ij utility to read the property file and
		// make the initial connection.
		ij.getPropertyArg(args);
		con = ij.startJBMS();
		//con.setAutoCommit(true); // make sure it is true
		con.setAutoCommit(false);

		return con;

	}

	protected void cleanUp(Statement stmt) throws SQLException {
		con.setAutoCommit(true);
		String[] testObjects = {"table t", "table t1", "view screwie", 
			"table reftab", "table reftab2", "table inflight" , "table alltypes", 
			"table louie",
			"procedure getpctest1", "procedure getpctest2",
			"procedure getpctest3a", "procedure getpctest3b",
			"procedure getpctest4a", "procedure getpctest4b", "procedure getpctest4bx",
			"procedure isreadO", "FUNCTION DUMMY1", "FUNCTION DUMMY2", 
			"FUNCTION DUMMY3", "FUNCTION DUMMY4" };
		TestUtil.cleanUpTest(stmt, testObjects);
	}
	
	/**
	 * Display the numeric JDBC metadata for a column
	 * @param expression Description of the expression
	 * @param rsmd thje meta data
	 * @param col which column
	 * @throws SQLException
	 */
	private static void showNumericMetaData(String expression,
			ResultSetMetaData rsmd, int col)
	   throws SQLException
   {
		System.out.print(expression);
		System.out.print(" --");
		
		System.out.print(" precision: ");
		System.out.print(rsmd.getPrecision(col));
		
		System.out.print(" scale: ");
		System.out.print(rsmd.getScale(col));
		
		System.out.print(" display size: ");
		System.out.print(rsmd.getColumnDisplaySize(col));

		System.out.print(" type name: ");
		System.out.print(rsmd.getColumnTypeName(col));
		
		System.out.println("");
		
   }
}

