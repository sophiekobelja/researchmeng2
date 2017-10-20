/**
 *  Derby - Class org.apache.derbyTesting.functionTests.tests.lang.NestedWhereSubqueryTest
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Test;

import org.apache.derbyTesting.junit.BaseJDBCTestCase;
import org.apache.derbyTesting.junit.JDBC;
import org.apache.derbyTesting.junit.TestConfiguration;

/**
 * Nested WHERE subquery tests. Tests nested WHERE EXISTS | ANY | IN functionality.
 *
 * Please refer to DERBY-3301 for more details.
 */
public class NestedWhereSubqueryTest extends BaseJDBCTestCase {

	public NestedWhereSubqueryTest(String name) {
		super(name);
	}

	/**
	 * Main test body
	 * 
	 * @throws SQLException
	 */
	public void testBasicOperations()
		throws SQLException {
		Statement s = createStatement();

		/*
		 * Create tables needed for DERBY-3301 regression test
		 */
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE departments ( ");
		sb.append("ID INTEGER NOT NULL, ");
		sb.append("NAME VARCHAR(32) NOT NULL, ");
		sb.append("COMPANYID INTEGER, ");
		sb.append("CONSTRAINT DEPTS_PK PRIMARY KEY (ID) ");
		sb.append(")");
		s.executeUpdate(sb.toString());

		sb = new StringBuffer();
		sb.append("CREATE TABLE employees ( ");
		sb.append("EMPID INTEGER NOT NULL, ");
		sb.append("FIRSTNAME VARCHAR(32) NOT NULL, ");
		sb.append("DEPARTMENT INTEGER, ");
		sb.append("CONSTRAINT PERS_DEPT_FK FOREIGN KEY (DEPARTMENT) REFERENCES departments, ");
		sb.append("CONSTRAINT EMPS_PK PRIMARY KEY (EMPID) ");
		sb.append(")");
		s.executeUpdate(sb.toString());

		sb = new StringBuffer();
		sb.append("CREATE TABLE projects ( ");
		sb.append("PROJID INTEGER NOT NULL, ");
		sb.append("NAME VARCHAR(32) NOT NULL, ");
		sb.append("CONSTRAINT PROJS_PK PRIMARY KEY (PROJID) ");
		sb.append(")");
		s.executeUpdate(sb.toString());

		sb = new StringBuffer();
		sb.append("CREATE TABLE project_employees ( ");
		sb.append("PROJID INTEGER REFERENCES projects NOT NULL, ");
		sb.append("EMPID INTEGER REFERENCES employees NOT NULL ");
		sb.append(")");
		s.executeUpdate(sb.toString());

		/*
		 * Fill some data into the tables
		 */
		s.executeUpdate("INSERT INTO departments VALUES (1, 'Research', 1)");
		s.executeUpdate("INSERT INTO departments VALUES (2, 'Marketing', 1)");

		s.executeUpdate("INSERT INTO employees VALUES (11, 'Alex', 1)");
		s.executeUpdate("INSERT INTO employees VALUES (12, 'Bill', 1)");
		s.executeUpdate("INSERT INTO employees VALUES (13, 'Charles', 1)");
		s.executeUpdate("INSERT INTO employees VALUES (14, 'David', 2)");
		s.executeUpdate("INSERT INTO employees VALUES (15, 'Earl', 2)");

		s.executeUpdate("INSERT INTO projects VALUES (101, 'red')");
		s.executeUpdate("INSERT INTO projects VALUES (102, 'orange')");
		s.executeUpdate("INSERT INTO projects VALUES (103, 'yellow')");

		s.executeUpdate("INSERT INTO project_employees VALUES (102, 13)");
		s.executeUpdate("INSERT INTO project_employees VALUES (101, 13)");
		s.executeUpdate("INSERT INTO project_employees VALUES (102, 12)");
		s.executeUpdate("INSERT INTO project_employees VALUES (103, 15)");
		s.executeUpdate("INSERT INTO project_employees VALUES (103, 14)");
		s.executeUpdate("INSERT INTO project_employees VALUES (101, 12)");
		s.executeUpdate("INSERT INTO project_employees VALUES (101, 11)");

		/*
		 * Preliminary data check
		 */
		ResultSet rs = s.executeQuery("select * from employees");
		String[][] expectedRows = {{"11", "Alex", "1"},
									{"12", "Bill", "1"},
									{"13", "Charles", "1"},
									{"14", "David", "2"},
									{"15", "Earl", "2"}};		
		JDBC.assertUnorderedResultSet(rs, expectedRows);

		rs = s.executeQuery("select * from departments");
		expectedRows = new String [][] {{"1", "Research", "1"},
										{"2","Marketing","1"}};		
		JDBC.assertUnorderedResultSet(rs, expectedRows);

		rs = s.executeQuery("select * from projects");
		expectedRows = new String [][] {{"101","red"},
										{"102","orange"},
										{"103","yellow"}};		
		JDBC.assertUnorderedResultSet(rs, expectedRows);

		rs = s.executeQuery("select * from project_employees");
		expectedRows = new String [][] {{"102","13"},
										{"101","13"},
										{"102","12"},
										{"103","15"},
										{"103","14"},
										{"101","12"},
										{"101","11"}};		
		JDBC.assertUnorderedResultSet(rs, expectedRows);

		/*
		 * DERBY-3301: This query should return 7 rows
		 */
		sb = new StringBuffer();
		sb.append("select unbound_e.empid, unbound_p.projid ");
		sb.append("from departments this, ");
		sb.append("     employees unbound_e, ");
		sb.append("     projects unbound_p ");
		sb.append("where exists ( ");
		sb.append("  select 1 from employees this_employees_e ");
		sb.append("  where exists ( ");
		sb.append("    select 1 from project_employees this_employees_e_projects_p ");
		sb.append("    where this_employees_e_projects_p.empid = this_employees_e.empid ");
		sb.append("    and this_employees_e.department = this.id ");
		sb.append("    and unbound_p.projid = this_employees_e_projects_p.projid ");
		sb.append("    and unbound_e.empid = this_employees_e.empid) ");
		sb.append(" )");

		rs = s.executeQuery(sb.toString());
		expectedRows = new String [][] {{"13", "101"},
										{"12", "101"},
										{"11", "101"},
										{"13", "102"},
										{"12", "102"},
										{"15", "103"},
										{"14", "103"}};
		JDBC.assertUnorderedResultSet(rs, expectedRows);
		
		/* A variation of the above WHERE EXISTS but using IN should return the same rows */
		sb = new StringBuffer();
		sb.append("select unbound_e.empid, unbound_p.projid ");
		sb.append("from departments this, ");
		sb.append("     employees unbound_e, ");
		sb.append("     projects unbound_p ");
		sb.append("where exists ( "); 
		sb.append(" select 1 from employees this_employees_e ");
		sb.append("     where this_employees_e.empid in ( ");
		sb.append("         select this_employees_e_projects_p.empid ");
		sb.append("           from project_employees this_employees_e_projects_p ");
		sb.append("         where this_employees_e_projects_p.empid = this_employees_e.empid ");
		sb.append("         and this_employees_e.department = this.id ");
		sb.append("         and unbound_p.projid = this_employees_e_projects_p.projid ");
		sb.append("         and unbound_e.empid = this_employees_e.empid) ");
		sb.append("     )");

		rs = s.executeQuery(sb.toString());
		JDBC.assertUnorderedResultSet(rs, expectedRows);

		/*
		 * Clean up the tables used.
		 */				
		s.executeUpdate("drop table project_employees");	
		s.executeUpdate("drop table projects");
		s.executeUpdate("drop table employees");
		s.executeUpdate("drop table departments");			
		
		s.close();
	}

	public static Test suite() {
		return TestConfiguration.defaultSuite(NestedWhereSubqueryTest.class);
	}
}