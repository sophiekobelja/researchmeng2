/*

   Derby - Class org.apache.derby.impl.tools.planexporter.AccessDatabase

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

package org.apache.derby.impl.tools.planexporter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class will perform the database connection establishment,
 * querying the database, shut downing the database.
 * Created under DERBY-4587-PlanExporter tool
 */
public class AccessDatabase {

    private Connection conn = null;
    private Statement stmt = null;
    private TreeNode[] data;
    private String dbURL = null;
    private String schema = null;
    private String query = null;
    /**
     * @param query the stmt_id to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the stmt_id
     */
    public String getQuery() {
        return query;
    }
    private int depth = 0;
    public int getDepth() {
        return depth;
    }
    private String xmlDetails="";

    //set of variables to identify values of XPlain tables
    private static final int ID =0 ;
    private static final int P_ID =1;
    private static final int NODE_TYPE=2;
    private static final int NO_OF_OPENS=3;
    private static final int INPUT_ROWS=4;
    private static final int RETURNED_ROWS=5;
    private static final int VISITED_PAGES=6;
    private static final int SCAN_QUALIFIERS=7;
    private static final int NEXT_QUALIFIERS=8;
    private static final int SCANNED_OBJECT=9;
    private static final int SCAN_TYPE=10;
    private static final int SORT_TYPE=11;
    private static final int NO_OF_OUTPUT_ROWS_BY_SORTER=12;


    

    /**
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void createConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
    {

        if(dbURL.indexOf("://") != -1)
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();

        else
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();

        //Get a connection
        conn = DriverManager.getConnection(dbURL);

    }

    
}
