/*
 *
 * Derby - Class org.apache.derbyTesting.junit.DriverManagerConnector
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific 
 * language governing permissions and limitations under the License.
 */
package org.apache.derbyTesting.junit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connection factory using DriverManager.
 *
 */
public class DriverManagerConnector implements Connector {

    private TestConfiguration config;

    public DriverManagerConnector() {
    }

    public void setConfiguration(TestConfiguration config) {
        this.config = config;
    }

    public Connection openConnection() throws SQLException {
        return openConnection(config.getUserName(), config.getUserPassword());
    }

    /**
     * Open a connection using the DriverManager.
     * <BR>
     * The JDBC driver is only loaded if DriverManager.getDriver()
     * for the JDBC URL throws an exception indicating no driver is loaded.
     * <BR>
     * If the connection request fails with SQLState XJ004
     * (database not found) then the connection is retried
     * with attributes create=true.
     */
    public Connection openConnection(String user, String password)
            throws SQLException {

        String url = config.getJDBCUrl();

        try {
            DriverManager.getDriver(url);
        } catch (SQLException e) {
            loadJDBCDriver();
        }

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            
            // Expected state for database not found.
            // For the client the generic 08004 is returned,
            // will just retry on that.
            String expectedState = 
                config.getJDBCClient().isEmbedded() ? "XJ004" : "08004";

            // If there is a database not found exception
            // then retry the connection request with
            // a create attribute.
            if (!expectedState.equals(e.getSQLState()))
                throw e;
            
            url = url.concat(";create=true");
            return DriverManager.getConnection(url, user, password);
        }
    }

    /**
     * Shutdown the database using the attributes shutdown=true
     * with the user and password defined by the configuration.
     */
    public void shutDatabase() throws SQLException {
        String url = config.getJDBCUrl();
        url = url.concat(";shutdown=true");
        DriverManager.getConnection(url,
                config.getUserName(),
                config.getUserPassword());
    }

    /**
     * Shutdown the engine using the attributes shutdown=true
     * and no database name with the user and password defined
     * by the configuration.
     */
    public void shutEngine() throws SQLException {
        String url = config.getJDBCClient().getUrlBase();
        url = url.concat(";shutdown=true");
        DriverManager.getConnection("jdbc:derby:;shutdown",
                config.getUserName(),
                config.getUserPassword());        
    }

    /**
     * Load the JDBC driver defined by the JDBCClient for
     * the configuration.
     *
     * @throws SQLException if loading the driver fails.
     */
    private void loadJDBCDriver() throws SQLException {
        String driverClass = config.getJDBCClient().getJDBCDriverName();
        try {
            Class.forName(driverClass).newInstance();
        } catch (ClassNotFoundException cnfe) {
            throw new SQLException("Failed to load JDBC driver '" + driverClass
                    + "': " + cnfe.getMessage());
        } catch (IllegalAccessException iae) {
            throw new SQLException("Failed to load JDBC driver '" + driverClass
                    + "': " + iae.getMessage());
        } catch (InstantiationException ie) {
            throw new SQLException("Failed to load JDBC driver '" + driverClass
                    + "': " + ie.getMessage());
        }
    }
}
