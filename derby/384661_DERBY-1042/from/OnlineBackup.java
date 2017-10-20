/*

Derby - Class org.apache.derbyTesting.functionTests.store.OnlineBackup

   Copyright 2005 The Apache Software Foundation or its licensors, as applicable.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.derbyTesting.functionTests.tests.store;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.derbyTesting.functionTests.util.TestUtil;

/**
 * This class provides  functionalty for tests to perform 
 * online backup  in a separate thread. And functions to 
 * create/restore/rollforard recovery from the backup. 
 *
 * @author <a href="mailto:suresh.thalamati@gmail.com">Suresh Thalamati</a>
 * @version 1.0
 */

public class OnlineBackup implements Runnable{

	private static final String backupPath = "extinout/onlinebackuptest";
	
	private String dbName; // name of the database to backup
	private boolean beginBackup = false;
	private boolean endBackup = false;

	OnlineBackup(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * implementation of run() method in the Runnable interface, which
	 * is invoked when a thread is started using this class object. 
	 * 
	 *  Performs online backup. 
	 * 
	 */
	public void run()	{
		try {
			performBackup();
		} catch (SQLException sqle) {
			org.apache.derby.tools.JDBCDisplayUtil.ShowSQLException(System.out, sqle);
			sqle.printStackTrace(System.out);
		}
	}

	/**
	 * Backup the database
	 */
	void performBackup() throws SQLException {
        Connection conn = getConnection(dbName , "");
		CallableStatement backupStmt = 	
			conn.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
		backupStmt.setString(1, backupPath);
			
		synchronized(this)	{
			beginBackup = true;
			endBackup = false;
			notifyAll();
		}

		backupStmt.execute();
		backupStmt.close();
		conn.close();

		synchronized(this)	{
			beginBackup = false;
			endBackup = true;
			notifyAll();
		}
	}

	/**
	 * Wait for the backup to start.
	 */

	public void waitForBackupToBegin() throws InterruptedException{
		synchronized(this) {
			//wait for backup to begin
			while(!beginBackup) {
					wait();
			}
		}
	}
	
	/*
	 * Wait for the backup to finish.
	 */
	public void waitForBackupToEnd() throws InterruptedException{
		synchronized(this) {
			if (!endBackup) {
				// check if a backup has actually started by the test
				if (!beginBackup) {
					System.out.println("BACKUP IS NOT STARTED BY THE TEST YET");	
				} else {

					//wait for backup to finish
					while(!endBackup) {
						wait();
					}
				}
			}

		}
	}

	/**
	 * Check if backup is running ?
	 * @return     <tt>true</tt> if backup is running.
	 *             <tt>false</tt> otherwise.
	 */
	public synchronized boolean isRunning() {
		return beginBackup;
	}
	
	/**
	 * Create a new database from the backup copy taken earlier.
	 * @param  newDbName   name of the database to be created.
	 */
	public void createFromBackup(String newDbName) throws SQLException {
		
        Connection conn = getConnection(newDbName,  
                                        "createFrom=" +
                                        backupPath + "/" + 
                                        dbName);
        conn.close();
        
    }

	
    /**
     * Restore the  database from the backup copy taken earlier.
     */
    public void restoreFromBackup() throws SQLException {
       
        Connection conn = getConnection(dbName,  
                                        "restoreFrom=" +
                                        backupPath + "/" + 
                                        dbName);

		conn.close();
    }

    
    /**
     * Get connection to the given database.
     *
     * @param databaseName the name of the database 
	 * @param connAttrs  connection Attributes.
     *
     */
    private Connection getConnection(String databaseName, 
                                     String connAttrs) 
        throws SQLException 
    {
    	Connection conn;
    	if(TestUtil.HAVE_DRIVER_CLASS)
			conn = DriverManager.getConnection("jdbc:derby:" + databaseName 
												+ ";" + connAttrs );
    	else {
	    	Properties prop = new Properties();
	        prop.setProperty("databaseName", databaseName);
	        prop.setProperty("connectionAttributes", connAttrs);
	        conn = TestUtil.getDataSourceConnection(prop);
    	}
        return conn;
    }
}
