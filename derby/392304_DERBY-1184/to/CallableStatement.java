/*

   Derby - Class org.apache.derby.client.am.CallableStatement

   Copyright (c) 2001, 2005 The Apache Software Foundation or its licensors, where applicable.

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

package org.apache.derby.client.am;

import java.sql.SQLException;
import org.apache.derby.shared.common.reference.SQLState;

public class CallableStatement extends PreparedStatement
        implements java.sql.PreparedStatement,
        java.sql.CallableStatement,
        PreparedStatementCallbackInterface {
    //---------------------navigational members-----------------------------------

    //---------------------navigational cheat-links-------------------------------
    // Cheat-links are for convenience only, and are not part of the conceptual model.
    // Warning:
    //   Cheat-links should only be defined for invariant state data.
    //   That is, the state data is set by the constructor and never changes.

    public MaterialPreparedStatement materialCallableStatement_ = null;

    //-----------------------------state------------------------------------------

    // last retrieved result was a sql NULL, NOT_NULL, or UNSET.
    private int wasNull_ = WAS_NULL_UNSET;
    static final private int WAS_NULL = 1;
    static final private int WAS_NOT_NULL = 2;
    static final private int WAS_NULL_UNSET = 0;

    //---------------------constructors/finalizer---------------------------------

    private void initCallableStatement() {
        materialCallableStatement_ = null;
        wasNull_ = WAS_NULL_UNSET;
    }

    public void reset(boolean fullReset) throws SqlException {
        if (fullReset) {
            connection_.resetPrepareCall(this);
        } else {
            super.reset(fullReset);
        }
        wasNull_ = WAS_NULL_UNSET;
    }

    // Common constructor for jdbc 2 callable statements with scroll attributes.
    // Called by material statement constructor.
    public CallableStatement(Agent agent,
                             Connection connection,
                             String sql,
                             int type, int concurrency, int holdability) throws SqlException {
        super(agent, connection, sql, type, concurrency, holdability, java.sql.Statement.NO_GENERATED_KEYS, null);
        initCallableStatement();
    }

    public void resetCallableStatement(Agent agent,
                                       Connection connection,
                                       String sql,
                                       int type, int concurrency, int holdability) throws SqlException {
        super.resetPreparedStatement(agent, connection, sql, type, concurrency, holdability, java.sql.Statement.NO_GENERATED_KEYS, null);
        initCallableStatement();
    }

    public void resetCallableStatement(Agent agent,
                                       Connection connection,
                                       String sql,
                                       Section section) throws SqlException {
        super.resetPreparedStatement(agent, connection, sql, section);
        initCallableStatement();
    }


    public void resetCallableStatement(Agent agent,
                                       Connection connection,
                                       String sql,
                                       Section section,
                                       ColumnMetaData parameterMetaData,
                                       ColumnMetaData resultSetMetaData) throws SqlException {
        super.resetPreparedStatement(agent, connection, sql, section, parameterMetaData, resultSetMetaData);
        initCallableStatement();
    }

    protected void finalize() throws java.lang.Throwable {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "finalize");
        }
        super.finalize();
    }

    //---------------------------entry points-------------------------------------

    public boolean execute() throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "execute");
                }
                boolean b = executeX();
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "execute", b);
                }
                return b;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    // also used by SQLCA
    boolean executeX() throws SqlException {
        super.flowExecute(executeMethod__);
        return resultSet_ != null;
    }

    public java.sql.ResultSet executeQuery() throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "executeQuery");
                }
                ResultSet resultSet = executeQueryX();
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "executeQuery", resultSet);
                }
                return resultSet;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    // also used by DBMD methods
    ResultSet executeQueryX() throws SqlException {
        super.flowExecute(executeQueryMethod__);
        super.checkExecuteQueryPostConditions("java.sql.CallableStatement");
        return resultSet_;
    }

    public int executeUpdate() throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "executeUpdate");
                }
                int updateValue = executeUpdateX();
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "executeUpdate", updateValue);
                }
                return updateValue;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    int executeUpdateX() throws SqlException {
        super.flowExecute(executeUpdateMethod__);

        super.checkExecuteUpdatePostConditions("java.sql.CallableStatement");
        // make sure update count >= 0 even if derby don't support update count for call
        //return (updateCount_ < 0) ? 0 : updateCount_;
        return updateCount_;
    }


    public void clearParameters() throws SQLException {
        synchronized (connection_) {
            if (agent_.loggingEnabled()) {
                agent_.logWriter_.traceEntry(this, "clearParameters");
            }
            super.clearParameters();
            outputRegistered_ = false; // this variable is only used by Batch
        }
    }

    public void registerOutParameter(int parameterIndex, int jdbcType) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "registerOutParameter", parameterIndex, jdbcType);
                }
                registerOutParameterX(parameterIndex, jdbcType);
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    // also used by Sqlca
    void registerOutParameterX(int parameterIndex, int jdbcType) throws SqlException {
        super.checkForClosedStatement();
        int scale = 0; // default scale to 0 for non numeric and non decimal type
        registerOutParameterX(parameterIndex, jdbcType, scale);
    }

    private int guessScaleForDecimalOrNumeric(int parameterIndex) throws SqlException {
        parameterIndex = checkForEscapedCallWithResult(parameterIndex);
        // Types.DECIMAL with no supplied scale will use the scale supplied by the setter method if input BigDecimal is not null
        if (parameterMetaData_.types_[parameterIndex - 1] == Types.DECIMAL &&
                parameters_[parameterIndex - 1] != null) {
            return parameterMetaData_.sqlScale_[parameterIndex - 1];
        }
        return 8; // default to scale of 8 if not specified
    }

    public void registerOutParameter(int parameterIndex, int jdbcType, int scale) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "registerOutParameter", parameterIndex, jdbcType, scale);
                }
                super.checkForClosedStatement();
                registerOutParameterX(parameterIndex, jdbcType, scale);
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    private void registerOutParameterX(int parameterIndex, int jdbcType, int scale) throws SqlException {
        parameterIndex = checkForEscapedCallWithResult(parameterIndex, jdbcType);
        // if the parameter is the return clause of the call statement
        if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
            return;
        }
        super.checkForValidParameterIndex(parameterIndex);
        checkForValidScale(scale);
        outputRegistered_ = true; // this variable is only used by Batch
        //parameterSetOrRegistered_[parameterIndex - 1] = true;
        parameterRegistered_[parameterIndex - 1] = true;
    }

    public void registerOutParameter(int parameterIndex, int jdbcType, String typeName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "registerOutParameter", parameterIndex, jdbcType, typeName);
        }
        throw jdbcMethodNotImplemented();
    }

    public boolean wasNull() throws SQLException {
        try
        {
            if (agent_.loggingEnabled()) {
                agent_.logWriter_.traceEntry(this, "wasNull");
            }
            boolean result = wasNullX();
            if (agent_.loggingEnabled()) {
                agent_.logWriter_.traceExit(this, "wasNull", result);
            }
            return result;
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    private boolean wasNullX() throws SqlException {
        super.checkForClosedStatement();
        if (wasNull_ == WAS_NULL_UNSET) {
            throw new SqlException(agent_.logWriter_, 
                new MessageId(SQLState.WASNULL_INVALID));
        }
        return wasNull_ == WAS_NULL;
    }

    //--------------------------------getter methods------------------------------

    public boolean getBoolean(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getBoolean", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                boolean result;
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    result = agent_.crossConverters_.getBooleanFromInt(returnValueFromProcedure_);
                    this.wasNull_ = this.WAS_NOT_NULL;
                    if (agent_.loggingEnabled()) {
                        agent_.logWriter_.traceExit(this, "getBoolean", result);
                    }
                    return result;
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                result = wasNullX() ? false : singletonRowData_.getBoolean(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getBoolean", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public byte getByte(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getByte", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                byte result;
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    result = agent_.crossConverters_.getByteFromInt(returnValueFromProcedure_);
                    this.wasNull_ = this.WAS_NOT_NULL;
                    if (agent_.loggingEnabled()) {
                        agent_.logWriter_.traceExit(this, "getByte", result);
                    }
                    return result;
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                result = wasNullX() ? 0 : singletonRowData_.getByte(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getByte", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public short getShort(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getShort", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                short result;
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    result = agent_.crossConverters_.getShortFromInt(returnValueFromProcedure_);
                    this.wasNull_ = this.WAS_NOT_NULL;
                    if (agent_.loggingEnabled()) {
                        agent_.logWriter_.traceExit(this, "getShort", result);
                    }
                    return result;
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                result = wasNullX() ? 0 : singletonRowData_.getShort(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getShort", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public int getInt(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getInt", parameterIndex);
                }
                int result = getIntX(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getInt", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    // also used by SQLCA
    int getIntX(int parameterIndex) throws SqlException {
        super.checkForClosedStatement();
        parameterIndex = checkForEscapedCallWithResult(parameterIndex);
        if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
            this.wasNull_ = this.WAS_NOT_NULL;
            return returnValueFromProcedure_;
        }
        checkGetterPreconditions(parameterIndex);
        setWasNull(parameterIndex);
        return wasNullX() ? 0 : singletonRowData_.getInt(parameterIndex);
    }

    public long getLong(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getLong", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                long result;
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    result = (long) returnValueFromProcedure_;
                    this.wasNull_ = this.WAS_NOT_NULL;
                    if (agent_.loggingEnabled()) {
                        agent_.logWriter_.traceExit(this, "getLong", result);
                    }
                    return result;
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                result = wasNullX() ? 0 : singletonRowData_.getLong(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getLong", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public float getFloat(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getFloat", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                float result;
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    result = (float) returnValueFromProcedure_;
                    this.wasNull_ = this.WAS_NOT_NULL;
                    if (agent_.loggingEnabled()) {
                        agent_.logWriter_.traceExit(this, "getFloat", result);
                    }
                    return result;
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                result = wasNullX() ? 0 : singletonRowData_.getFloat(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getFloat", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public double getDouble(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getDouble", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                double result;
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    result = (double) returnValueFromProcedure_;
                    this.wasNull_ = this.WAS_NOT_NULL;
                    if (agent_.loggingEnabled()) {
                        agent_.logWriter_.traceExit(this, "getDouble", result);
                    }
                    return result;
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                result = wasNullX() ? 0 : singletonRowData_.getDouble(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getDouble", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public java.math.BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException, ArithmeticException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceDeprecatedEntry(this, "getBigDecimal", parameterIndex, scale);
                }
                super.checkForClosedStatement();
                checkForValidScale(scale);
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                java.math.BigDecimal result;
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    result = java.math.BigDecimal.valueOf(returnValueFromProcedure_).setScale(scale);
                    this.wasNull_ = this.WAS_NOT_NULL;
                    if (agent_.loggingEnabled()) {
                        agent_.logWriter_.traceDeprecatedExit(this, "getBigDecimal", result);
                    }
                    return result;
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                result = wasNullX() ? null : singletonRowData_.getBigDecimal(parameterIndex);
                if (result != null) {
                    result = result.setScale(scale, java.math.BigDecimal.ROUND_DOWN);
                }
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceDeprecatedExit(this, "getBigDecimal", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public java.math.BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getBigDecimal", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                java.math.BigDecimal result;
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    result = java.math.BigDecimal.valueOf(returnValueFromProcedure_);
                    this.wasNull_ = this.WAS_NOT_NULL;
                    if (agent_.loggingEnabled()) {
                        agent_.logWriter_.traceExit(this, "getBigDecimal", result);
                    }
                    return result;
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                result = wasNullX() ? null : singletonRowData_.getBigDecimal(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getBigDecimal", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public java.sql.Date getDate(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getDate", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    throw new SqlException(agent_.logWriter_, 
                        new MessageId(SQLState.INVALID_PARAM_USE_GETINT));
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                java.sql.Date result = wasNullX() ? null : singletonRowData_.getDate(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getDate", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public java.sql.Date getDate(int parameterIndex, java.util.Calendar cal) throws SQLException {
        synchronized (connection_) {
            if (agent_.loggingEnabled()) {
                agent_.logWriter_.traceEntry(this, "getDate", parameterIndex, cal);
            }
            if (cal == null) {
                throw new SqlException(agent_.logWriter_, 
                    new MessageId(SQLState.CALENDAR_IS_NULL)).getSQLException();
            }
            java.sql.Date result = getDate(parameterIndex);
            if (result != null) {
                java.util.Calendar targetCalendar = java.util.Calendar.getInstance(cal.getTimeZone());
                targetCalendar.clear();
                targetCalendar.setTime(result);
                java.util.Calendar defaultCalendar = java.util.Calendar.getInstance();
                defaultCalendar.clear();

                defaultCalendar.setTime(result);
                long timeZoneOffset =
                        targetCalendar.get(java.util.Calendar.ZONE_OFFSET) - defaultCalendar.get(java.util.Calendar.ZONE_OFFSET) +
                        targetCalendar.get(java.util.Calendar.DST_OFFSET) - defaultCalendar.get(java.util.Calendar.DST_OFFSET);
                result.setTime(result.getTime() - timeZoneOffset);
            }
            if (agent_.loggingEnabled()) {
                agent_.logWriter_.traceExit(this, "getDate", result);
            }
            return result;
        }
    }

    public java.sql.Time getTime(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getTime", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    throw new SqlException(agent_.logWriter_, 
                        new MessageId(SQLState.INVALID_PARAM_USE_GETINT));
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                java.sql.Time result = wasNullX() ? null : singletonRowData_.getTime(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getTime", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public java.sql.Time getTime(int parameterIndex, java.util.Calendar cal) throws SQLException {
        synchronized (connection_) {
            if (agent_.loggingEnabled()) {
                agent_.logWriter_.traceEntry(this, "getTime", parameterIndex, cal);
            }
            if (cal == null) {
                throw new SqlException(agent_.logWriter_, 
                    new MessageId(SQLState.CALENDAR_IS_NULL)).getSQLException();
            }
            java.sql.Time result = getTime(parameterIndex);
            if (result != null) {
                java.util.Calendar targetCalendar = java.util.Calendar.getInstance(cal.getTimeZone());
                targetCalendar.clear();
                targetCalendar.setTime(result);
                java.util.Calendar defaultCalendar = java.util.Calendar.getInstance();
                defaultCalendar.clear();
                defaultCalendar.setTime(result);
                long timeZoneOffset =
                        targetCalendar.get(java.util.Calendar.ZONE_OFFSET) - defaultCalendar.get(java.util.Calendar.ZONE_OFFSET) +
                        targetCalendar.get(java.util.Calendar.DST_OFFSET) - defaultCalendar.get(java.util.Calendar.DST_OFFSET);
                result.setTime(result.getTime() - timeZoneOffset);
            }
            if (agent_.loggingEnabled()) {
                agent_.logWriter_.traceExit(this, "getTime", result);
            }
            return result;
        }
    }

    public java.sql.Timestamp getTimestamp(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getTimestamp", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    throw new SqlException(agent_.logWriter_, 
                        new MessageId(SQLState.INVALID_PARAM_USE_GETINT));
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                java.sql.Timestamp result = wasNullX() ? null : singletonRowData_.getTimestamp(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getTimestamp", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public java.sql.Timestamp getTimestamp(int parameterIndex, java.util.Calendar cal) throws SQLException {
        synchronized (connection_) {
            if (agent_.loggingEnabled()) {
                agent_.logWriter_.traceEntry(this, "getTimestamp", parameterIndex, cal);
            }
            if (cal == null) {
                throw new SqlException(agent_.logWriter_, 
                    new MessageId(SQLState.CALENDAR_IS_NULL)).getSQLException();
            }
            java.sql.Timestamp result = getTimestamp(parameterIndex);
            if (result != null) {
                int nano = result.getNanos();
                java.util.Calendar targetCalendar = java.util.Calendar.getInstance(cal.getTimeZone());
                targetCalendar.clear();
                targetCalendar.setTime(result);
                java.util.Calendar defaultCalendar = java.util.Calendar.getInstance();
                defaultCalendar.clear();
                defaultCalendar.setTime(result);
                long timeZoneOffset =
                        targetCalendar.get(java.util.Calendar.ZONE_OFFSET) - defaultCalendar.get(java.util.Calendar.ZONE_OFFSET) +
                        targetCalendar.get(java.util.Calendar.DST_OFFSET) - defaultCalendar.get(java.util.Calendar.DST_OFFSET);
                result.setTime(result.getTime() - timeZoneOffset);
                result.setNanos(nano);
            }
            if (agent_.loggingEnabled()) {
                agent_.logWriter_.traceExit(this, "getTimestamp", result);
            }
            return result;
        }
    }

    public String getString(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getString", parameterIndex);
                }
                String result = getStringX(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getString", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    // also used by SQLCA
    String getStringX(int parameterIndex) throws SqlException {
        super.checkForClosedStatement();
        parameterIndex = checkForEscapedCallWithResult(parameterIndex);
        if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
            this.wasNull_ = this.WAS_NOT_NULL;
            return Integer.toString(returnValueFromProcedure_);
        }
        checkGetterPreconditions(parameterIndex);
        setWasNull(parameterIndex);
        return wasNullX() ? null : singletonRowData_.getString(parameterIndex);
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getBytes", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    throw new SqlException(agent_.logWriter_, 
                        new MessageId(SQLState.INVALID_PARAM_USE_GETINT));
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                byte[] result = wasNullX() ? null : singletonRowData_.getBytes(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getBytes", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public java.sql.Blob getBlob(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getBlob", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    throw new SqlException(agent_.logWriter_, 
                        new MessageId(SQLState.INVALID_PARAM_USE_GETINT));
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                java.sql.Blob result = wasNullX() ? null : singletonRowData_.getBlob(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getBlob", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public java.sql.Clob getClob(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    throw new SqlException(agent_.logWriter_, 
                        new MessageId(SQLState.INVALID_PARAM_USE_GETINT));
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                java.sql.Clob result = wasNullX() ? null : singletonRowData_.getClob(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getClob", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public java.sql.Array getArray(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getArray", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    throw new SqlException(agent_.logWriter_, 
                        new MessageId(SQLState.INVALID_PARAM_USE_GETINT));                    
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                java.sql.Array result = wasNullX() ? null : singletonRowData_.getArray(parameterIndex);
                if (true) {
                    throw new SqlException(agent_.logWriter_, 
                        new MessageId(SQLState.JDBC_METHOD_NOT_IMPLEMENTED));
                }
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getArray", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public java.sql.Ref getRef(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getRef", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    throw new SqlException(agent_.logWriter_, 
                        new MessageId(SQLState.INVALID_PARAM_USE_GETINT));
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                java.sql.Ref result = wasNullX() ? null : singletonRowData_.getRef(parameterIndex);
                if (true) {
                    throw new SqlException(agent_.logWriter_, 
                        new MessageId(SQLState.JDBC_METHOD_NOT_IMPLEMENTED));
                }
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getRef", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public Object getObject(int parameterIndex) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getObject", parameterIndex);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                Object result;
                if (parameterIndex == 0 && escapedProcedureCallWithResult_) {
                    result = new Integer(returnValueFromProcedure_);
                    this.wasNull_ = this.WAS_NOT_NULL;
                    if (agent_.loggingEnabled()) {
                        agent_.logWriter_.traceExit(this, "getObject", result);
                    }
                    return result;
                }
                checkGetterPreconditions(parameterIndex);
                setWasNull(parameterIndex);
                result = wasNullX() ? null : singletonRowData_.getObject(parameterIndex);
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getObject", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    public Object getObject(int parameterIndex, java.util.Map map) throws SQLException {
        try
        {
            synchronized (connection_) {
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceEntry(this, "getObject", parameterIndex, map);
                }
                super.checkForClosedStatement();
                parameterIndex = checkForEscapedCallWithResult(parameterIndex);
                Object result;
                checkGetterPreconditions(parameterIndex);
                if (true) {
                    throw new SqlException(agent_.logWriter_, 
                        new MessageId(SQLState.JDBC_METHOD_NOT_IMPLEMENTED));
                }
                if (agent_.loggingEnabled()) {
                    agent_.logWriter_.traceExit(this, "getObject", result);
                }
                return result;
            }
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
    }

    //--------------------------JDBC 3.0------------------------------------------

    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "registerOutParameter", parameterName, sqlType);
        }
        throw jdbcMethodNotImplemented();
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "registerOutParameter", parameterName, sqlType, scale);
        }
        throw jdbcMethodNotImplemented();
    }

    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "registerOutParameter", parameterName, sqlType, typeName);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.net.URL getURL(int parameterIndex) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getURL", parameterIndex);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setURL(String parameterName, java.net.URL x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setURL", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setNull", parameterName, sqlType);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setBoolean", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setByte(String parameterName, byte x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setByte", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setShort(String parameterName, short x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setShort", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setInt(String parameterName, int x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setInt", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setLong(String parameterName, long x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setLong", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setFloat", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setDouble", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setBigDecimal(String parameterName, java.math.BigDecimal x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setBigDecimal", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setString(String parameterName, String x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setString", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setBytes(String parameterName, byte x[]) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setBytes", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setDate(String parameterName, java.sql.Date x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setDate", parameterName, x);
        }
    }

    public void setTime(String parameterName, java.sql.Time x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setTime", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setTimestamp(String parameterName, java.sql.Timestamp x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setTimestamp", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setAsciiStream(String parameterName, java.io.InputStream x, int length) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setAsciiStream", parameterName, x, length);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setBinaryStream(String parameterName, java.io.InputStream x, int length) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setBinaryStream", parameterName, x, length);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setObject", parameterName, x, targetSqlType, scale);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setObject", parameterName, x, targetSqlType);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setObject", parameterName, x);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setCharacterStream(String parameterName, java.io.Reader reader, int length) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setCharacterStream", parameterName, reader, length);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setDate(String parameterName, java.sql.Date x, java.util.Calendar calendar) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setDate", parameterName, x, calendar);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setTime(String parameterName, java.sql.Time x, java.util.Calendar calendar) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setTime", parameterName, x, calendar);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setTimestamp(String parameterName, java.sql.Timestamp x, java.util.Calendar calendar) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setTimestamp", parameterName, x, calendar);
        }
        throw jdbcMethodNotImplemented();
    }

    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "setNull", parameterName, sqlType, typeName);
        }
        throw jdbcMethodNotImplemented();
    }

    public String getString(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getString", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public boolean getBoolean(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getBoolean", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public byte getByte(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getByte", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public short getShort(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getShort", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public int getInt(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getInt", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public long getLong(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getLong", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public float getFloat(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getFloat", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public double getDouble(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getDouble", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getBytes", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.sql.Date getDate(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getDate", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.sql.Time getTime(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getTime", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.sql.Timestamp getTimestamp(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getTimestamp", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public Object getObject(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getObject", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.math.BigDecimal getBigDecimal(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getBigDecimal", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public Object getObject(String parameterName, java.util.Map map) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getObject", parameterName, map);
            }
        throw jdbcMethodNotImplemented();
    }

    public java.sql.Ref getRef(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getRef", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.sql.Blob getBlob(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getBlob", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.sql.Clob getClob(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getClob", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.sql.Array getArray(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getArray", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.sql.Date getDate(String parameterName, java.util.Calendar calendar) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getDate", parameterName, calendar);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.sql.Time getTime(String parameterName, java.util.Calendar calendar) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getTime", parameterName, calendar);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.sql.Timestamp getTimestamp(String parameterName, java.util.Calendar calendar) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getTimestamp", parameterName, calendar);
        }
        throw jdbcMethodNotImplemented();
    }

    public java.net.URL getURL(String parameterName) throws SQLException {
        if (agent_.loggingEnabled()) {
            agent_.logWriter_.traceEntry(this, "getURL", parameterName);
        }
        throw jdbcMethodNotImplemented();
    }

    //----------------------------helper methods----------------------------------

    private int checkForEscapedCallWithResult(int parameterIndex) throws SqlException {
        if (escapedProcedureCallWithResult_) {
            parameterIndex--;
        }
        return parameterIndex;
    }

    private int checkForEscapedCallWithResult(int parameterIndex, int jdbcType) throws SqlException {
        if (escapedProcedureCallWithResult_) {
            if (parameterIndex == 1 && jdbcType != java.sql.Types.INTEGER) {
                throw new SqlException(agent_.logWriter_, 
                    new MessageId(SQLState.RETURN_PARAM_MUST_BE_INT));
            } else {
                parameterIndex--;
            }
        }
        return parameterIndex;
    }

    private void checkGetterPreconditions(int parameterIndex) throws SqlException {
        super.checkForValidParameterIndex(parameterIndex);
        checkForValidOutParameter(parameterIndex);
    }

    private void checkForValidOutParameter(int parameterIndex) throws SqlException {
        if (parameterMetaData_ == null || parameterMetaData_.sqlxParmmode_[parameterIndex - 1] < java.sql.ParameterMetaData.parameterModeInOut) {
            throw new SqlException(agent_.logWriter_, 
                new MessageId(SQLState.PARAM_NOT_OUT_OR_INOUT), 
                new Integer(parameterIndex));
        }
    }

    private void setWasNull(int parameterIndex) {
        if (singletonRowData_ == null) {
            wasNull_ = WAS_NULL_UNSET;
        } else {
            wasNull_ = singletonRowData_.isNull_[parameterIndex - 1] ? WAS_NULL : WAS_NOT_NULL;
        }
    }
    
    private SQLException jdbcMethodNotImplemented() throws SQLException
    {
        try
        {
            super.checkForClosedStatement();
        }
        catch ( SqlException se )
        {
            throw se.getSQLException();
        }
        return new SqlException(agent_.logWriter_, 
            new MessageId(SQLState.JDBC_METHOD_NOT_IMPLEMENTED)).getSQLException();
    }
}

