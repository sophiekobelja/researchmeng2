/**
 *  Derby - Class org.apache.derbyTesting.functionTests.tests.lang.OLAPTest
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
import java.sql.PreparedStatement;
import java.sql.Statement;

import junit.framework.Test;

import org.apache.derbyTesting.junit.BaseJDBCTestCase;
import org.apache.derbyTesting.junit.JDBC;
import org.apache.derbyTesting.junit.TestConfiguration;

/**
 * OLAP functionality test.
 * 
 * Please refer to DERBY-581 for more details.
 */ 
public class OLAPTest extends BaseJDBCTestCase {

	public OLAPTest(String name) {
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

		s.executeUpdate("create table t1 (a int, b int)");
		s.executeUpdate("create table t2 (x int)");
		s.executeUpdate("create table t3 (y int)");
		s.executeUpdate("create table t4 (a int, b int)");

		s.executeUpdate("insert into t1 values (10,100),(20,200),(30,300),(40,400),(50,500)");
		s.executeUpdate("insert into t2 values (1),(2),(3),(4),(5)");
		s.executeUpdate("insert into t3 values (4),(5),(6),(7),(8)");		
		s.executeUpdate("insert into t4 values (10,100),(20,200)");

		/*
		 * Positive testing of Statements
		 *
		 * Simple queries
		 */		
		ResultSet rs = s.executeQuery("select row_number() over (), t1.* from t1");
		String[][] expectedRows = {{"1", "10", "100"}, {"2", "20", "200"}, {"3", "30", "300"}, {"4", "40", "400"}, {"5", "50", "500"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		rs = s.executeQuery("select row_number() over (), t1.* from t1 where a > 30");
		expectedRows = new String[][]{{"1", "40", "400"}, {"2", "50", "500"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		rs = s.executeQuery("select row_number() over (), a from t1 where b > 300");
		expectedRows = new String[][]{{"1", "40"}, {"2", "50"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		rs = s.executeQuery("select row_number() over () as r, a from t1 where b > 300");
		expectedRows = new String[][]{{"1", "40"}, {"2", "50"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		/* Two instances of row_number columns in the same RCL */
		rs = s.executeQuery("select row_number() over (), row_number() over (), b from t1 where b <= 300");
		expectedRows = new String[][]{{"1", "1", "100"}, {"2", "2", "200"}, {"3", "3", "300"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		/* Two instances of row_number columns in the same RCL, reorder columns */
		rs = s.executeQuery("select row_number() over (), b, row_number() over (), a from t1 where b < 300 ");
		expectedRows = new String[][]{{"1", "100", "1", "10"}, {"2", "200", "2", "20"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		/* Pushing predicates (... where r ... ) too far cause this join to fail */
		rs = s.executeQuery("select row_number() over(),x from t2,t3 where x=y");
		expectedRows = new String[][]{{"1", "4"}, {"2", "5"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		/* Ordering */
		rs = s.executeQuery("select row_number() over () as r, t1.* from t1 order by b desc");
		expectedRows = new String[][]{{"1", "50", "500"}, {"2", "40", "400"}, {"3", "30", "300"}, {"4", "20", "200"}, {"5", "10", "100"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		/* Ordering on a column dropped in projection */
		rs = s.executeQuery("select row_number() over () as r, t1.a from t1 order by b desc");
		expectedRows = new String[][]{{"1", "50"}, {"2", "40"}, {"3", "30"}, {"4", "20"}, {"5", "10"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		/* Only expressions in RCL */
		rs = s.executeQuery("select row_number() over (), row_number() over (), 2*t1.a from t1");
		expectedRows = new String[][]{{"1", "1", "20"}, {"2", "2","40"}, {"3", "3","60"}, {"4", "4", "80"}, {"5", "5", "100"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		/*
		 * Subquerys 
		 */ 
			
		/* This query returned no rows at one time */
		rs = s.executeQuery("select * from (select row_number() over () as r,x from t2,t3 where x=y) s(r,x) where r < 3");
		expectedRows = new String[][]{{"1", "4"}, {"2", "5"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		rs = s.executeQuery("select * from (select row_number() over () as r, t1.* from t1) as tr where r < 3");
		expectedRows = new String[][]{{"1", "10", "100"}, {"2", "20", "200"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		rs = s.executeQuery("select * from (select row_number() over () as r, t1.* from t1) as tr where r > 3");
		expectedRows = new String[][]{{"4", "40", "400"}, {"5", "50", "500"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		/* Two instances of row_number columns */
		rs = s.executeQuery("select row_number() over(), tr.* from (select row_number() over () as r, t1.* from t1) as tr where r > 2 and r < 5");
		expectedRows = new String[][]{{"1", "3", "30", "300"}, {"2", "4", "40", "400"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		/* Two instances of row_number columns, with projection */
		rs = s.executeQuery("select row_number() over(), tr.b from (select row_number() over () as r, t1.* from t1) as tr where r > 2 and r < 5");
		expectedRows = new String[][]{{"1", "300"}, {"2", "400"}};
		JDBC.assertFullResultSet(rs, expectedRows);		

		/* Column ordering */
		rs = s.executeQuery("select * from (select t1.b, row_number() over () as r from t1) as tr where r > 3");
		expectedRows = new String[][]{{"400", "4"}, {"500", "5"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		/* Column ordering with projection*/
		rs = s.executeQuery("select b from (select t1.b, row_number() over () as r from t1) as tr where r > 3");
		expectedRows = new String[][]{{"400"}, {"500"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		/*
		 * Aggregates over window functions once failed
		 */
		rs = s.executeQuery("select count(*) from (select row_number() over() from t1) x");
		expectedRows = new String[][]{{"5"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		rs = s.executeQuery("select count(*) from (select row_number() over () as r from t1) as t(r) where r <=3");
		expectedRows = new String[][]{{"3"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		/*
		 * Some other joins with window functions.
		 * Run off a smaller table t4 to reduce expected row count.
		 */
		rs = s.executeQuery("select row_number() over () from t1 union all select row_number() over () from t1");
		expectedRows = new String[][]{{"1"},{"2"},{"3"},{"4"},{"5"},{"1"},{"2"},{"3"},{"4"},{"5"}};
		JDBC.assertFullResultSet(rs, expectedRows);	
		
		rs = s.executeQuery("select 2 * r from (select row_number() over () from t1) x(r)");
		expectedRows = new String[][]{{"2"},{"4"},{"6"},{"8"},{"10"},};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		rs = s.executeQuery("select c3, c1, c2 from " + 
							"(select a, b, row_number() over() as r from t4) x1 (c1, c2, r1), " +
							"(select row_number() over() as r, b, a from t4) x2 (r2, c3, c4)");
		expectedRows = new String[][]{{"100", "10", "100"},
										{"200", "10", "100"},																				
										{"100", "20", "200"},
										{"200", "20", "200"}};										
		JDBC.assertFullResultSet(rs, expectedRows);
					
		rs = s.executeQuery("select c3, c1, c2 from " + 
							"(select a, b, row_number() over() as r from t4) x1 (c1, c2, r1), " +
							"(select row_number() over() as r, b, a from t4) x2 (r2, c3, c4), " +
							"t4");
		expectedRows = new String[][]{{"100", "10", "100"},
										{"100", "10", "100"},																				
										{"200", "10", "100"},
										{"200", "10", "100"},
										{"100", "20", "200"},
										{"100", "20", "200"},
										{"200", "20", "200"},										
										{"200", "20", "200"}};										
		JDBC.assertFullResultSet(rs, expectedRows);

		rs = s.executeQuery("select c3, c1, c2 from "+
							"(select a, b, row_number() over() as r from t4) x1 (c1, c2, r1), "+
							"(select row_number() over() as r, b, a from t4) x2 (r2, c3, c4), "+
							"t4 "+
							"where x1.r1 = 2 * x2.r2");
		expectedRows = new String[][]{{"100", "20", "200"}, {"100", "20", "200"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		rs = s.executeQuery("select c3, c1, c2 from "+
							"(select a, b, row_number() over() as r from t4) x1 (c1, c2, r1), "+
							"(select row_number() over() as r, b, a from t4) x2 (r2, c3, c4), "+
							"t4 "+
							"where x1.r1 = 2 * x2.r2");
		expectedRows = new String[][]{{"100", "20", "200"}, {"100", "20", "200"}};
		JDBC.assertFullResultSet(rs, expectedRows);
				
		/* Two problematic joins reported during development */
		rs = s.executeQuery("select c3, c1, c2 from "+
							"(select a, b, row_number() over() as r from t4) x1 (c1, c2, r1), "+
							"(select row_number() over() as r, b, a from t4) x2 (r2, c3, c4), "+
							"t4 "+
							"where x2.c4 = t4.a");
		expectedRows = new String[][]{{"100", "10", "100"}, 
										{"100", "20", "200"},
										{"200", "10", "100"},
										{"200", "20", "200"}};			
		JDBC.assertFullResultSet(rs, expectedRows);
		
		rs = s.executeQuery("select c3, c1, c2 from "+
							"(select a, b, row_number() over() as r from t1) x1 (c1, c2, r1), "+
							"(select row_number() over() as r, b, a from t1) x2 (r2, c3, c4), "+
							"t1 "+
							"where x1.r1 = 2 * x2.r2 and x2.c4 = t1.a");
		expectedRows = new String[][]{{"100", "20", "200"}, {"200", "40", "400"}};
		JDBC.assertFullResultSet(rs, expectedRows);

		/* Group by and having */
		rs = s.executeQuery("select r from (select a, row_number() over() as r, b from t1) x group by r");
		expectedRows = new String[][]{{"1"}, {"2"}, {"3"}, {"4"}, {"5"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		rs = s.executeQuery("select * from (select a, row_number() over() as r, b from t1) x group by a, b, r");
		expectedRows = new String[][]{{"10", "1", "100"}, 
										{"20", "2", "200"},
										{"30", "3", "300"},
										{"40", "4", "400"},
										{"50", "5", "500"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		rs = s.executeQuery("select * from (select a, row_number() over() as r, b from t1) x group by b, r, a");
		expectedRows = new String[][]{{"10", "1", "100"}, 
										{"20", "2", "200"},
										{"30", "3", "300"},
										{"40", "4", "400"},
										{"50", "5", "500"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		rs = s.executeQuery("select * from "+
							"(select a, row_number() over() as r, b from t1) x "+
							"group by b, r, a "+
							"having r > 2");
		expectedRows = new String[][]{{"30", "3", "300"},
										{"40", "4", "400"}, 
										{"50", "5", "500"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		rs = s.executeQuery("select * from "+
							"(select a, row_number() over() as r, b from t1) x "+
							"group by b, r, a "+
							"having r > 2 and a >=30 "+
							"order by a desc");
		expectedRows = new String[][]{{"50", "5", "500"},
										{"40", "4", "400"}, 
										{"30", "3", "300"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		 
		rs = s.executeQuery("select * from "+
							"(select a, row_number() over() as r, b from t1) x "+
							"group by b, r, a "+
							"having r > 2 and a >=30 "+
							"order by r desc");
		expectedRows = new String[][]{{"50", "5", "500"},
										{"40", "4", "400"}, 
										{"30", "3", "300"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		rs = s.executeQuery("select * from "+
							"(select a, row_number() over() as r, b from t1) x "+
							"group by b, r, a "+
							"having r > 2 and a >=30 "+
							"order by a asc, r desc");
		expectedRows = new String[][]{{"30", "3", "300"},
										{"40", "4", "400"}, 
										{"50", "5", "500"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		/* A couple of distinct queries */
		rs = s.executeQuery("select distinct row_number() over (), 'ABC' from t1");
		expectedRows = new String[][]{{"1", "ABC"},
										{"2", "ABC"},
										{"3", "ABC"},
										{"4", "ABC"},
										{"5", "ABC"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		rs = s.executeQuery("select * from (select distinct row_number() over (), 'ABC' from t1) tmp");
		expectedRows = new String[][]{{"1", "ABC"},
										{"2", "ABC"},
										{"3", "ABC"},
										{"4", "ABC"},
										{"5", "ABC"}};
		JDBC.assertFullResultSet(rs, expectedRows);
		
		/*
		 * Negative testing of Statements
		 */

		// Missing required OVER () 
		assertStatementError("42X01", s, "select row_number() as r, * from t1 where t1.a > 2");

		// Illegal where clause, r not a named column of t1.        
		assertStatementError("42X04", s, "select row_number() over () as r, a from t1 where r < 3");

		// Illegal use of asterix with another column identifier.        
		assertStatementError("42X01", s, "select row_number() over () as r, * from t1 where t1.a > 2");

		/*
		 * Clean up the tables used.
		 */
		s.executeUpdate("drop table t1");
		s.executeUpdate("drop table t2");
		s.executeUpdate("drop table t3");

		s.close();
	}

    private String makeString(int len)
    {
        StringBuffer buf = new StringBuffer(len);
        for (int i = 0; i < len; i++)
            buf.append('a');
        return buf.toString();
    }
        /**
          * Basic test of GROUP BY ROLLUP capability.
          *
          * This test case has a few basic tests of GROUP BY ROLLUP, both
          * positive and negative tests.
          */
    public void testGroupByRollup()
        throws SQLException
    {
        Statement s = createStatement();
        s.executeUpdate("create table ru (a int, b int, c int, d int)");
        s.executeUpdate("insert into ru values (1,1,1,1), (1,2,3,4),"+
                "(1,1,2,2), (4,3,2,1), (4,4,4,4)");
        JDBC.assertUnorderedResultSet( s.executeQuery(
                    "select a,b,c,sum(d) from ru group by rollup(a,b,c)"),
                new String[][]{
                    {"1","1","1","1"},
                    {"1","1","2","2"},
                    {"1","2","3","4"},
                    {"4","3","2","1"},
                    {"4","4","4","4"},
                    {"1","1",null,"3"},
                    {"1","2",null,"4"},
                    {"4","3",null,"1"},
                    {"4","4",null,"4"},
                    {"1",null,null,"7"},
                    {"4",null,null,"5"},
                    {null,null,null,"12"}});
        JDBC.assertFullResultSet( s.executeQuery(
                "select count(*) from ru group by mod(a,b)"),
                new String[][]{ {"3"},{"2"}});

        // Try a few negative tests:
        assertStatementError("42X04", s,
                "select a,b,c,sum(d) from ru group by rollup");
        assertStatementError("42X01", s,
                "select a,b,c,sum(d) from ru group by rollup(");
        assertStatementError("42X01", s,
                "select a,b,c,sum(d) from ru group by rollup)");
        assertStatementError("42X01", s,
                "select a,b,c,sum(d) from ru group by rollup()");

        s.executeUpdate("drop table ru");
        s.close();
    }
    /**
      * Verify that ROLLUP can still be used as the name of a column or table.
      */
    public void testRollupReservedWord()
        throws SQLException
    {
        Statement s = createStatement();
        s.executeUpdate("create table t_roll(rollup int, x int)");
        JDBC.assertEmpty( s.executeQuery(
                    "select rollup, sum(x) from t_roll group by rollup"));
        JDBC.assertEmpty( s.executeQuery(
                    "select count(*) from t_roll group by mod(rollup,x)"));
        JDBC.assertEmpty( s.executeQuery(
                    "select count(*) from t_roll group by mod(x,rollup)"));
        s.executeUpdate("create table rollup(a int, x int)");
        JDBC.assertEmpty( s.executeQuery("select a, x from rollup"));
        s.executeUpdate("insert into rollup(a,x) values(1,2)");
        JDBC.assertUnorderedResultSet( s.executeQuery(
                    "select a,sum(x) from rollup group by rollup(a)"),
                new String[][]{
                    {"1","2"}, {null,"2"}});
        s.executeUpdate("drop table rollup");
        s.executeUpdate("drop table t_roll");
        s.close();
    }
    /**
      * Verify that non-aggregate columns are returned as NULLABLE if ROLLUP.
      *
      * If a GROUP BY ROLLUP is used, the un-aggregated columns may contain
      * NULL values, so we need to verify that the DatabaseMetadata returns
      * the right values for the nullability of the columns.
      */
    public void testRollupColumnNullability()
        throws SQLException
    {
        Statement s = createStatement();
        s.executeUpdate(
                "create table t_notnull(a int not null, b int," +
                "                       c int not null, d int)");
        ResultSet rs = s.executeQuery(
                "select a,b,c,sum(d) from t_notnull group by rollup(a,b,c)");
        JDBC.assertNullability(rs,
                new boolean[]{true, true, true, true});
        rs.close();

        rs = s.executeQuery(
                "select 1,2,3,sum(d) from t_notnull group by rollup(1,2,3)");
        JDBC.assertNullability(rs,
                new boolean[]{true, true, true, true});
        rs.close();

        s.executeUpdate("drop table t_notnull");
        s.close();
    }

    /**
      * Verify the behavior of GROUP BY ROLLUP for empty result sets.
      */
    public void testRollupEmptyTables()
        throws SQLException
    {
        Statement s = createStatement();
        s.executeUpdate("create table ru (a int, b int, c int, d int)");

        JDBC.assertEmpty( s.executeQuery("select sum(a) from ru group by b"));
        JDBC.assertSingleValueResultSet(
                s.executeQuery("select sum(a) from ru"), (String)null);
        s.executeUpdate("insert into ru values (1,1,1,1), (1,2,3,4),"+
                "(1,1,2,2), (4,3,2,1), (4,4,4,4)");
        JDBC.assertEmpty( s.executeQuery(
                    "select b, sum(a) from ru where 1<>1 group by rollup(b)"));

        s.executeUpdate("drop table ru");
        s.close();
    }

    /**
      * A ROLLUP case suggested by Dag in 1-sep-2009 comment on DERBY-3002
      */
    public void testRollupNullabilityCasts()
        throws SQLException
    {
        Statement s = createStatement();
        s.executeUpdate("create table t(c varchar(2) not null," +
                "c2 varchar(2), i integer)");
        s.executeUpdate("insert into t values('aa',null,null)");
        s.executeUpdate("insert into t values('bb',null,null)");
        String [][]rows = 
                new String[][]{
                    {"aa",null,null},
                    {"aa",null,null},
                    {null,null,null},
                    {"bb",null,null},
                    {"bb",null,null}};
        JDBC.assertUnorderedResultSet(
            s.executeQuery("select c,c2,sum(i) from t group by rollup(c,c2)"),
            rows);
        JDBC.assertUnorderedResultSet(s.executeQuery(
                "select cast(c as varchar(2)),c2,sum(i) from t " +
                "group by rollup(c,c2)"),
            rows);
        /* FIXME -- this test currently fails due to improper handling of
           the nullability of the result columns.
        JDBC.assertUnorderedResultSet(s.executeQuery(
                "select cast(x as varchar(2)),y,z from " +
                " (select c,c2,sum(i) from t " +
                "      group by rollup (c,c2)) t(x,y,z)"),
            rows);
            */

        s.executeUpdate("drop table t");
        s.close();
    }

    /**
      * Verify the behavior of GROUP BY ROLLUP when it can use a covering index.
      */
    public void testRollupOfCoveringIndex()
        throws SQLException
    {
        Statement s = createStatement();
        s.executeUpdate("create table ru (a int,b int,c int,d varchar(1000))");
        s.executeUpdate("create index ru_idx on ru(a,b,c)");
        PreparedStatement ps = prepareStatement(
                "insert into ru (a,b,c,d) values (?,?,?,?)");
        for (int i = 0; i < 100; i++)
        {
            ps.setInt(1, (i%5));
            ps.setInt(2, 2*i);
            ps.setInt(3, 100+i);
            ps.setString(4, makeString(900));
            ps.executeUpdate();
        }
        ps.close();
        // FIXME
        //dumpIt(s, 2, "select a,sum(c) from ru group by a");
        //dumpIt(s, 3, "select a,b,sum(c) from ru group by a,b");
        //dumpIt(s, 3, "select a,b,sum(c) from ru group by rollup(a,b)");
        s.executeUpdate("drop table ru");
        s.close();
    }
    private void dumpIt(Statement s, int cols, String sql)
        throws SQLException
    {
        System.out.println(sql);
        ResultSet rs = s.executeQuery(sql);
        while (rs.next())
        {
            StringBuffer buf = new StringBuffer();
            for (int i = 1; i <= cols; i++)
            {
                if (i > 1)
                    buf.append(",");
                buf.append(rs.getString(i));
            }
            System.out.println(buf.toString());
        }
        rs.close();
    }
    /*
     * Various GROUP BY tests, with and without ROLLUP.
     */
    public void testGroupByWithAndWithoutRollup()
        throws SQLException
    {
        Statement s = createStatement();
        // A very simple set of master-detail ORDER and ORDER_ITEM tables,
        // with some fake customer data:
        s.executeUpdate(
                "create table orders(order_id int primary key," +
                "   customer varchar(10)," +
                "   order_date date, " +
                "   shipping int)");
        s.executeUpdate(
                "create table order_items(item_id int primary key," +
                "   order_id int," +
                "   order_item varchar(10), " +
                "   cost int)");
        s.executeUpdate(
                "create table customers(customer varchar(10) primary key," +
                "   name varchar(100), city varchar(100), state varchar(2))");
        s.executeUpdate("insert into customers values " +
                "('ABC','ABC Corporation','ABC City', 'AB')," +
                "('DEF','DEF, Inc.', 'DEFburg', 'DE')");
        s.executeUpdate("insert into orders values(1,'ABC','2009-01-01',40)");
        s.executeUpdate("insert into orders values(2,'ABC','2009-01-02',30)");
        s.executeUpdate("insert into orders values(3,'ABC','2009-01-03',25)");
        s.executeUpdate("insert into orders values(4,'DEF','2009-01-02',10)");
        s.executeUpdate("insert into order_items values(1,1,'Item A',100)");
        s.executeUpdate("insert into order_items values(2,1,'Item B',150)");
        s.executeUpdate("insert into order_items values(3,2,'Item C',125)");
        s.executeUpdate("insert into order_items values(4,2,'Item B',50)");
        s.executeUpdate("insert into order_items values(5,2,'Item H',200)");
        s.executeUpdate("insert into order_items values(6,3,'Item X',100)");
        s.executeUpdate("insert into order_items values(7,4,'Item Y',50)");
        s.executeUpdate("insert into order_items values(8,4,'Item Z',300)");
        // Joining the two tables produces one row per order item:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.order_id, o.customer, o.order_date, " +
            "o.shipping, od.item_id, od.order_item, od.cost " +
            " from orders o inner join order_items od " +
            " on o.order_id = od.order_id"),
            new String[][]{
                    {"1","ABC","2009-01-01","40","1","Item A","100"},
                    {"1","ABC","2009-01-01","40","2","Item B","150"},
                    {"2","ABC","2009-01-02","30","3","Item C","125"},
                    {"2","ABC","2009-01-02","30","4","Item B","50"},
                    {"2","ABC","2009-01-02","30","5","Item H","200"},
                    {"3","ABC","2009-01-03","25","6","Item X","100"},
                    {"4","DEF","2009-01-02","10","7","Item Y","50"},
                    {"4","DEF","2009-01-02","10","8","Item Z","300"},
                });
        // Grouping the items by customer to compute items/customer:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.Customer, count(*) as items_per_customer " +
            " from orders o inner join order_items od " +
            "      on o.order_id = od.order_id " +
            " group by o.customer"),
            new String[][]{
                {"ABC","6"},
                {"DEF","2"},
            });
        // Also include the total cost per customer:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.Customer, count(*) as items_per_customer, " +
            "       sum(od.cost) as order_total " +
            " from orders o inner join order_items od " +
            "      on o.order_id = od.order_id " +
            " group by o.customer"),
            new String[][]{
                {"ABC","6","725"},
                {"DEF","2","350"},
            });
        // ROLLUP the items and costs to grand totals:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.Customer, count(*) as items_per_customer, " +
            "       sum(od.cost) as order_total " +
            " from orders o inner join order_items od " +
            "      on o.order_id = od.order_id " +
            " group by ROLLUP(o.customer)"),
            new String[][]{
                {"ABC","6","725"},
                {"DEF","2","350"},
                {null,"8","1075"},
            });
        // Show a usage of Count(distinct) to compute the orders/customer,
        // which is not the same as the items/customer:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.Customer, count(*) as items_per_customer, " +
            "       count(distinct o.order_id) as orders_per_customer " +
            " from orders o inner join order_items od " +
            "      on o.order_id = od.order_id " +
            " group by o.customer"),
            new String[][]{
                {"ABC","6","3"},
                {"DEF","2","1"},
            });
        // ROLLUP should work for the distinct count, too:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.Customer, count(*) as items_per_customer, " +
            "       count(distinct o.order_id) as orders_per_customer " +
            " from orders o inner join order_items od " +
            "      on o.order_id = od.order_id " +
            " group by ROLLUP(o.customer)"),
            new String[][]{
                {"ABC","6","3"},
                {"DEF","2","1"},
                {null,"8","4"},
            });
        // can we compute the total shipping per customer:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.Customer, count(*) as items_per_customer, " +
            "       sum(od.cost) as order_total, " +
            "       count(distinct o.order_id) as orders_per_customer, " +
            "       sum(o.shipping) as shipping_per_customer " +
            " from orders o inner join order_items od " +
            "      on o.order_id = od.order_id " +
            " group by o.customer"),
            new String[][]{
                {"ABC","6","725","3","195"},
                {"DEF","2","350","1","20"},
            });
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.Customer, count(*) as items_per_customer, " +
            "       sum(od.cost) as order_total, " +
            "       count(distinct o.order_id) as orders_per_customer, " +
            "       sum(o.shipping) as shipping_per_customer " +
            " from orders o inner join order_items od " +
            "      on o.order_id = od.order_id " +
            " group by ROLLUP(o.customer)"),
            new String[][]{
                {"ABC","6","725","3","195"},
                {"DEF","2","350","1","20"},
                {null,"8","1075","4","215"},
            });
        // Show a usage of disinct shipping aggregate, similar to the
        // distinct count aggregate:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.Customer, count(*) as items_per_customer, " +
            "       sum(od.cost) as order_total, " +
            "       count(distinct o.order_id) as orders_per_customer, " +
            "       sum(distinct o.shipping) as shipping_per_customer " +
            " from orders o inner join order_items od " +
            "      on o.order_id = od.order_id " +
            " group by o.customer"),
            new String[][]{
                {"ABC","6","725","3","95"},
                {"DEF","2","350","1","10"},
            });
        // Demonstrate some of the dangers of using distinct aggregates.
        // Duplicate SUM values may be real duplicates from the data, not
        // from duplicate-producing master-detail joins. The COUNT changes
        // from 1 to 2 for customer DEF, but the shipping_per_customer is
        // still 10, which is logically wrong (there are 2 DEF orders, each
        // with value 10, so we "expected" 20 for shipping_per_customer).
        s.executeUpdate("insert into orders values(5,'DEF','2009-01-04',10)");
        s.executeUpdate("insert into order_items values(9,5,'Item J',125)");
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.Customer, count(*) as items_per_customer, " +
            "       sum(od.cost) as order_total, " +
            "       count(distinct o.order_id) as orders_per_customer, " +
            "       sum(distinct o.shipping) as shipping_per_customer " +
            " from orders o inner join order_items od " +
            "      on o.order_id = od.order_id " +
            " group by o.customer"),
            new String[][]{
                {"ABC","6","725","3","95"},
                {"DEF","3","475","2","10"},
            });
        // Same as before, but with ROLLUP:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.Customer, count(*) as items_per_customer, " +
            "       sum(od.cost) as order_total, " +
            "       count(distinct o.order_id) as orders_per_customer, " +
            "       sum(distinct o.shipping) as shipping_per_customer " +
            " from orders o inner join order_items od " +
            "      on o.order_id = od.order_id " +
            " group by ROLLUP(o.customer)"),
            new String[][]{
                {"ABC","6","725","3","95"},
                {"DEF","3","475","2","10"},
                {null,"9","1200","5","105"},
            });
        // Produce the results we expected by constructing a sub-query:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select order_id, count(*) as Items_per_order, " +
            "       sum(cost) as Order_total "+
            " from order_items " +
            " group by order_id"),
            new String[][]{
                {"1","2","250"},
                {"2","3","375"},
                {"3","1","100"},
                {"4","2","350"},
                {"5","1","125"},
            });
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select order_id, count(*) as Items_per_order, " +
            "       sum(cost) as Order_total "+
            " from order_items " +
            " group by ROLLUP(order_id)"),
            new String[][]{
                {"1","2","250"},
                {"2","3","375"},
                {"3","1","100"},
                {"4","2","350"},
                {"5","1","125"},
                {null,"9","1200"},
            });
        // ... then encapsulate that sub-select with a join to the orders:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.order_id, o.Customer, o.Shipping, " +
            "       d.items_per_order, d.order_total " +
            " from orders o inner join (" +
            "   select order_id, count(*) as Items_per_order, " +
            "          sum(cost) as Order_total "+
            "    from order_items " +
            "    group by order_id " +
            "   ) d on o.order_id = d.order_id"),
            new String[][]{
                {"1","ABC","40","2","250"},
                {"2","ABC","30","3","375"},
                {"3","ABC","25","1","100"},
                {"4","DEF","10","2","350"},
                {"5","DEF","10","1","125"},
            });
        // ... and group *THAT* join, in turn, by customer, to get the
        // correct values of shipping_per_customer and items_per_customer.
        // Note that total_per_customer is a SUM(SUM()), while 
        // items_per_customer is a SUM(COUNT()). And no DISTINCT needed.
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.customer, count(*) as orders_per_customer, " +
            "       sum(o.shipping) as shipping_per_customer, " +
            "       sum(d.items_per_order) as items_per_customer, " +
            "       sum(d.order_total) as total_per_customer " +
            " from orders o inner join (" +
            "   select order_id, count(*) as Items_per_order, " +
            "          sum(cost) as Order_total "+
            "    from order_items " +
            "    group by order_id " +
            "   ) d on o.order_id = d.order_id " +
            " group by o.customer"),
            new String[][]{
                {"ABC","3","95","6","725"},
                {"DEF","2","20","3","475"},
            });
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.customer, count(*) as orders_per_customer, " +
            "       sum(o.shipping) as shipping_per_customer, " +
            "       sum(d.items_per_order) as items_per_customer, " +
            "       sum(d.order_total) as total_per_customer " +
            " from orders o inner join (" +
            "   select order_id, count(*) as Items_per_order, " +
            "          sum(cost) as Order_total "+
            "    from order_items " +
            "    group by order_id " +
            "   ) d on o.order_id = d.order_id " +
            " group by ROLLUP(o.customer)"),
            new String[][]{
                {"ABC","3","95","6","725"},
                {"DEF","2","20","3","475"},
                {null,"5","115","9","1200"},
            });
        // Include customer address information. First by joining and grouping:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select o.customer, c.name, c.city, c.state, " +
            "       count(*) as orders_per_customer, " +
            "       sum(o.shipping) as shipping_per_customer, " +
            "       sum(d.items_per_order) as items_per_customer, " +
            "       sum(d.order_total) as total_per_customer " +
            " from orders o inner join (" +
            "   select order_id, count(*) as Items_per_order, " +
            "          sum(cost) as Order_total "+
            "    from order_items " +
            "    group by order_id " +
            "   ) d on o.order_id = d.order_id " +
            "   inner join customers c on o.customer = c.customer " +
            " group by ROLLUP(o.customer,c.name, c.city,c.state)"),
            new String[][]{
                {"ABC","ABC Corporation","ABC City","AB","3","95","6","725"},
                {"DEF","DEF, Inc.","DEFburg","DE","2","20","3","475"},
                {"ABC","ABC Corporation","ABC City",null,"3","95","6","725"},
                {"DEF","DEF, Inc.","DEFburg",null,"2","20","3","475"},
                {"ABC","ABC Corporation",null,null,"3","95","6","725"},
                {"DEF","DEF, Inc.",null,null,"2","20","3","475"},
                {"ABC",null,null,null,"3","95","6","725"},
                {"DEF",null,null,null,"2","20","3","475"},
                {null,null,null,null,"5","115","9","1200"},
            });
        // Then, alternately, by sub-selecting and grouping:
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select c.customer, c.name, c.city, c.state, " +
            "       o.orders_per_customer, o.shipping_per_customer, " +
            "       o.items_per_customer, o.total_per_customer " +
            " from ( " +
            "   select o.customer, count(*) as orders_per_customer, " +
            "          sum(o.shipping) as shipping_per_customer, " +
            "          sum(d.items_per_order) as items_per_customer, " +
            "          sum(d.order_total) as total_per_customer " +
            "    from orders o inner join (" +
            "      select order_id, count(*) as Items_per_order, " +
            "             sum(cost) as Order_total "+
            "       from order_items " +
            "       group by order_id " +
            "      ) d on o.order_id = d.order_id " +
            "    group by o.customer) o " +
            "  inner join customers c on o.customer = c.customer"),
            new String[][]{
                {"ABC","ABC Corporation","ABC City","AB","3","95","6","725"},
                {"DEF","DEF, Inc.","DEFburg","DE","2","20","3","475"},
            });
        // Note that we can put the ROLLUP in the sub-query, but then we
        // need to outer-join with the customers table since the rollup
        // results will have NULL in the join key.
        JDBC.assertUnorderedResultSet( s.executeQuery(
            "select c.customer, c.name, c.city, c.state, " +
            "       o.orders_per_customer, o.shipping_per_customer, " +
            "       o.items_per_customer, o.total_per_customer " +
            " from ( " +
            "   select o.customer, count(*) as orders_per_customer, " +
            "          sum(o.shipping) as shipping_per_customer, " +
            "          sum(d.items_per_order) as items_per_customer, " +
            "          sum(d.order_total) as total_per_customer " +
            "    from orders o inner join (" +
            "      select order_id, count(*) as Items_per_order, " +
            "             sum(cost) as Order_total "+
            "       from order_items " +
            "       group by order_id " +
            "      ) d on o.order_id = d.order_id " +
            "    group by ROLLUP(o.customer)) o " +
            "  left outer join customers c on o.customer = c.customer"),
            new String[][]{
                {"ABC","ABC Corporation","ABC City","AB","3","95","6","725"},
                {"DEF","DEF, Inc.","DEFburg","DE","2","20","3","475"},
                {null,null,null,null,"5","115","9","1200"},
            });

        s.close();
    }

	public static Test suite() {
		return TestConfiguration.defaultSuite(OLAPTest.class);
	}
}
