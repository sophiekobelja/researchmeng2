/*

   Derby - Class org.apache.derby.iapi.jdbc.BrokeredConnection

   Copyright 2002, 2004 The Apache Software Foundation or its licensors, as applicable.

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

package org.apache.derby.iapi.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;

import org.apache.derby.impl.jdbc.EmbedSQLWarning;
import org.apache.derby.impl.jdbc.Util;

import java.io.ObjectOutput;
import java.io.ObjectInput;

import java.lang.reflect.*;

import org.apache.derby.iapi.reference.JDBC30Translation;
import org.apache.derby.iapi.reference.SQLState;
import org.apache.derby.iapi.error.PublicAPI;
import org.apache.derby.iapi.error.StandardException;

/**
 * This is a rudimentary connection that delegates
 * EVERYTHING to Connection.
 */
public class BrokeredConnection implements EngineConnection
{
	
	// default for Derby
	protected int stateHoldability = JDBC30Translation.HOLD_CURSORS_OVER_COMMIT;

	protected final BrokeredConnectionControl control;
	private boolean isClosed;

	/**
		Maintain state as seen by this Connection handle, not the state
		of the underlying Connection it is attached to.
	*/
	private int stateIsolationLevel;
	private boolean stateReadOnly;
	private boolean stateAutoCommit;

	/////////////////////////////////////////////////////////////////////////
	//
	//	CONSTRUCTORS
	//
	/////////////////////////////////////////////////////////////////////////

	public	BrokeredConnection(BrokeredConnectionControl control)
	{
		this.control = control;
	}

	public final void setAutoCommit(boolean autoCommit) throws SQLException 
	{
		try {
			control.checkAutoCommit(autoCommit);

			getRealConnection().setAutoCommit(autoCommit);

			stateAutoCommit = autoCommit;
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}
	public final boolean getAutoCommit() throws SQLException 
	{
		try {
			return getRealConnection().getAutoCommit();
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}
	public final Statement createStatement() throws SQLException 
	{
		try {
			return control.wrapStatement(getRealConnection().createStatement());
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final PreparedStatement prepareStatement(String sql)
	    throws SQLException 
	{
		try {
			return control.wrapStatement(getRealConnection().prepareStatement(sql), sql, null);
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final CallableStatement prepareCall(String sql) throws SQLException 
	{
		try {
			return control.wrapStatement(getRealConnection().prepareCall(sql), sql);
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final String nativeSQL(String sql) throws SQLException
	{
		try {
			return getRealConnection().nativeSQL(sql);
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final void commit() throws SQLException 
	{
		try {
			control.checkCommit();
			getRealConnection().commit();
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final void rollback() throws SQLException 
	{
		try {
			control.checkRollback();
			getRealConnection().rollback();
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final void close() throws SQLException 
	{ 
		if (isClosed)
			return;

		try {
			if (!control.closingConnection()) {
				isClosed = true;
				return;
			}

			isClosed = true;


			getRealConnection().close();
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final boolean isClosed() throws SQLException 
	{
		if (isClosed)
			return true;
		try {
			boolean realIsClosed = getRealConnection().isClosed();
			if (realIsClosed) {
				control.closingConnection();
				isClosed = true;
			}
			return realIsClosed;
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final SQLWarning getWarnings() throws SQLException 
	{
		try {
			return getRealConnection().getWarnings();
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final void clearWarnings() throws SQLException 
	{
		try {
			getRealConnection().clearWarnings();
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final DatabaseMetaData getMetaData() throws SQLException 
	{
		try {
			return getRealConnection().getMetaData();
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final void setReadOnly(boolean readOnly) throws SQLException 
	{
		try {
			getRealConnection().setReadOnly(readOnly);
			stateReadOnly = readOnly;
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final boolean isReadOnly() throws SQLException 
	{
		try {
			return getRealConnection().isReadOnly();
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final void setCatalog(String catalog) throws SQLException 
	{
		try {
			getRealConnection().setCatalog(catalog);
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final String getCatalog() throws SQLException 
	{
		try {
			return getRealConnection().getCatalog();
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final void setTransactionIsolation(int level) throws SQLException 
	{
		try {
			getRealConnection().setTransactionIsolation(level);
			stateIsolationLevel = level;
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

	public final int getTransactionIsolation() throws SQLException
	{
		try {
			return getRealConnection().getTransactionIsolation();
		} catch (SQLException sqle) {
			notifyException(sqle);
			throw sqle;
		}
	}

    public final Statement createStatement(int resultSetType, int resultSetConcurrency) 
      throws SQLException
	{
		try
		{
			return control.wrapStatement(getRealConnection().
				createStatement(resultSetType, resultSetConcurrency));
		}
		catch (SQLException se)
		{
			notifyException(se);
			throw se;
		}
	}


	public final PreparedStatement prepareStatement(String sql, int resultSetType, 
					int resultSetConcurrency)
       throws SQLException
	{
		try
		{
			return control.wrapStatement(getRealConnection().
				prepareStatement(sql, resultSetType, resultSetConcurrency), sql, null);
		}
		catch (SQLException se)
		{
			notifyException(se);
			throw se;
		}
	}

    public final CallableStatement prepareCall(String sql, int resultSetType, 
				 int resultSetConcurrency) throws SQLException
	{
		try
		{
			return control.wrapStatement(getRealConnection().
				prepareCall(sql, resultSetType, resultSetConcurrency), sql);
		}
		catch (SQLException se)
		{
			notifyException(se);
			throw se;
		}
	}

    public final java.util.Map getTypeMap() throws SQLException
	{
		try
		{
			return getRealConnection().getTypeMap();
		}
		catch (SQLException se)
		{
			notifyException(se);
			throw se;
		}
	}

    public final void setTypeMap(java.util.Map map) throws SQLException
	{
		try
		{
			getRealConnection().setTypeMap(map);
		}
		catch (SQLException se)
		{
			notifyException(se);
			throw se;
		}
	}

	/////////////////////////////////////////////////////////////////////////
	//
	//	MINIONS
	//
	/////////////////////////////////////////////////////////////////////////

	/**
	  *	A little indirection for getting the real connection. 
	  *
	  *	@return	the current connection
	  */
	final EngineConnection getRealConnection() throws SQLException {
		if (isClosed)
			throw Util.noCurrentConnection();

		return control.getRealConnection();
	}

	protected final void notifyException(SQLException sqle) {
		if (!isClosed)
			control.notifyException(sqle);
	}

	/**
		Sync up the state of the underlying connection
		with the state of this new handle.
	*/
	public void syncState() throws SQLException {
		EngineConnection conn = getRealConnection();

		stateIsolationLevel = conn.getTransactionIsolation();
		stateReadOnly = conn.isReadOnly();
		stateAutoCommit = conn.getAutoCommit();
        stateHoldability = conn.getHoldability(); 
	}

	/**
		Isolation level state in BrokeredConnection can get out of sync
		if the isolation is set using SQL rather than JDBC. In order to
		ensure correct state level information, this method is called
		at the start and end of a global transaction.
	*/
	public void getIsolationUptoDate() throws SQLException {
		if (control.isIsolationLevelSetUsingSQLorJDBC()) {
			stateIsolationLevel = getRealConnection().getTransactionIsolation();
			control.resetIsolationLevelFlag();
		}
	}
	/**
		Set the state of the underlying connection according to the
		state of this connection's view of state.

		@param complete If true set the complete state of the underlying
		Connection, otherwise set only the Connection related state (ie.
		the non-transaction specific state).


	*/
	public void setState(boolean complete) throws SQLException {
		Class[] CONN_PARAM = { Integer.TYPE };
		Object[] CONN_ARG = { new Integer(stateHoldability)};

		Connection conn = getRealConnection();

		if (complete) {
			conn.setTransactionIsolation(stateIsolationLevel);
			conn.setReadOnly(stateReadOnly);
			conn.setAutoCommit(stateAutoCommit);
			// make the underlying connection pick my holdability state
			// since holdability is a state of the connection handle
			// not the underlying transaction.
			// jdk13 does not have Connection.setHoldability method and hence using
			// reflection to cover both jdk13 and higher jdks
			try {
				Method sh = conn.getClass().getMethod("setHoldability", CONN_PARAM);
				sh.invoke(conn, CONN_ARG);
			} catch( Exception e)
			{
				throw PublicAPI.wrapStandardException( StandardException.plainWrapException( e));
			}
		}
	}

	public BrokeredStatement newBrokeredStatement(BrokeredStatementControl statementControl) throws SQLException {
		// DERBY-3513 - Possible IBM 1.5 JIT bug workaround.
		// create local variable for jdbcLevel
		int jdbcLevel = getJDBCLevel();
		return new BrokeredStatement(statementControl, jdbcLevel);
	}
	public BrokeredPreparedStatement newBrokeredStatement(BrokeredStatementControl statementControl, String sql, Object generatedKeys) throws SQLException {
		//DERBY-3513 - Possible IBM 1.5 JIT bug workaround.
		// create local variable for jdbcLevel
		int jdbcLevel = getJDBCLevel();
		return new BrokeredPreparedStatement(statementControl, jdbcLevel, sql);
	}
	public BrokeredCallableStatement newBrokeredStatement(BrokeredStatementControl statementControl, String sql) throws SQLException {
		// DERBY-3513 - Possible IBM 1.5 JIT bug workaround.
		// create local variable for jdbcLevel
		int jdbcLevel = getJDBCLevel();
		return new BrokeredCallableStatement(statementControl, jdbcLevel, sql);
	}

	/**
	 *  set the DrdaId for this connection. The drdaID prints with the 
	 *  statement text to the errror log
	 *  @param drdaID  drdaID to be used for this connection
	 *
	 */
	public final void setDrdaID(String drdaID)
	{
        try {
		    getRealConnection().setDrdaID(drdaID);
        } catch (SQLException sqle)
        {
            // connection is closed, just ignore drdaId
            // since connection cannot be used.
        }
	}

	/**
	 *  Set the internal isolation level to use for preparing statements.
	 *  Subsequent prepares will use this isoalation level
	 * @param level - internal isolation level 
	 * @throws SQLException
	 * @see EmbedConnection#setPrepareIsolation
	 * 
	 */
	public final void setPrepareIsolation(int level) throws SQLException
	{
        getRealConnection().setPrepareIsolation(level);
	}

	/**
	 * get the isolation level that is currently being used to prepare 
	 * statements (used for network server)
	 * 
	 * @throws SQLException
	 * @return current prepare isolation level 
	 * @see EmbedConnection#getPrepareIsolation
	 */
	public final int getPrepareIsolation() throws SQLException
	{
		return getRealConnection().getPrepareIsolation();
	}
    
    /**
     * Add a SQLWarning to this Connection object.
     * @throws SQLException 
     */
    public final void addWarning(SQLWarning w) throws SQLException
    {
        getRealConnection().addWarning(w);
    }
            
    /**
     * Get the string representation for the underlying physical
     * connection.
     *
     *  When a physical connection is created, it is assigned a unique id 
     *  that is unchanged for the lifetime of the connection. When an 
     *  application calls Connection.toString(), it gets the string 
     *  representation of the underlying physical connection, regardless 
     *  of whether the application has a reference to the physical connection 
     *  itself or a reference to a proxy connection (aka brokered connection) 
     *  that wraps the physical connection.
     *
     *  Since this BrokeredConnection is a proxy connection, we return the
     *  string value of its underlying physical connection
     * 
     * @return unique string representation of the underlying
     *   physical connection
     */
    public String toString() 
    {
        try
        {
            return getRealConnection().toString();
        }
        catch ( SQLException e )
        {
            return "<no connection>";
        }
    }

	protected int getJDBCLevel() { return 2;}

    /*
     * JDBC 3.0 methods that are exposed through EngineConnection.
     */
    
    /**
     * Prepare statement with explicit holdability.
     */
    public final PreparedStatement prepareStatement(String sql,
            int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
    	try {
            resultSetHoldability = statementHoldabilityCheck(resultSetHoldability);
    		
    		return control.wrapStatement(
    			getRealConnection().prepareStatement(sql, resultSetType,
                        resultSetConcurrency, resultSetHoldability), sql, null);
    	}
    	catch (SQLException se)
    	{
    		notifyException(se);
    		throw se;
    	}
    }

    /**
     * Get the holdability for statements created by this connection
     * when holdability is not passed in.
     */
    public final int getHoldability() throws SQLException {
    	try {
    		return getRealConnection().getHoldability();
    	}
    	catch (SQLException se)
    	{
    		notifyException(se);
    		throw se;
    	}
    }
    
    /*
    ** Methods private to the class.
    */
    
    /**
     * Check the result set holdability when creating a statement
     * object. Section 16.1.3.1 of JDBC 4.0 (proposed final draft)
     * says the driver may change the holdabilty and add a SQLWarning
     * to the Connection object.
     * 
     * This work-in-progress implementation throws an exception
     * to match the old behaviour just as part of incremental development.
     */
    final int statementHoldabilityCheck(int resultSetHoldability)
        throws SQLException
    {
        int holdability = control.checkHoldCursors(resultSetHoldability, true);
        if (holdability != resultSetHoldability) {
            SQLWarning w =
                 EmbedSQLWarning.newEmbedSQLWarning(SQLState.HOLDABLE_RESULT_SET_NOT_AVAILABLE);
            
            addWarning(w);
        }
        
        return holdability;
        
    }
}
