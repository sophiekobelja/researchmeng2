/*

   Derby - Class org.apache.derby.impl.sql.execute.rts.RealUpdateResultSetStatistics

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

package org.apache.derby.impl.sql.execute.rts;

import org.apache.derby.iapi.sql.execute.ResultSetStatistics;
import org.apache.derby.iapi.services.io.StoredFormatIds;

import org.apache.derby.iapi.services.i18n.MessageService;
import org.apache.derby.iapi.reference.SQLState;

import org.apache.derby.iapi.services.io.FormatableHashtable;
import org.apache.derby.catalog.UUID;
import org.apache.derby.impl.sql.catalog.XPLAINResultSetDescriptor;
import org.apache.derby.impl.sql.catalog.XPLAINResultSetTimingsDescriptor;
import org.apache.derby.impl.sql.execute.xplain.XPLAINUtil;
import org.apache.derby.iapi.sql.execute.xplain.XPLAINVisitor;

import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;

/**
  ResultSetStatistics implemenation for UpdateResultSet.


*/
public class RealUpdateResultSetStatistics 
	extends RealNoRowsResultSetStatistics
{


	/* Leave these fields public for object inspectors */
    public	int 					rowCount;
	public  boolean					deferred;
	public	boolean					tableLock;
	public	int						indexesUpdated;

	// CONSTRUCTORS

	/**
	 * 
	 *
	 */
    public	RealUpdateResultSetStatistics(
								int rowCount,
								boolean deferred,
								int indexesUpdated,
								boolean tableLock,
								long executeTime,
								ResultSetStatistics sourceResultSetStatistics
								)
	{
		super(executeTime, sourceResultSetStatistics);
		this.rowCount = rowCount;
		this.deferred = deferred;
		this.indexesUpdated = indexesUpdated;
		this.tableLock = tableLock;
		this.sourceResultSetStatistics = sourceResultSetStatistics;
	}


	// ResultSetStatistics interface

	/**
	 * Return the statement execution plan as a String.
	 *
	 * @param depth	Indentation level.
	 *
	 * @return String	The statement execution plan as a String.
	 */
	public String getStatementExecutionPlanText(int depth)
	{
		initFormatInfo(depth);



		return
			indent + MessageService.getTextMessage(
											SQLState.RTS_UPDATE_RS_USING,
											MessageService.getTextMessage(
												tableLock ?
												SQLState.LANG_TABLE :
												SQLState.LANG_ROW)
											) +
				":\n" +
			indent + MessageService.getTextMessage(SQLState.RTS_DEFERRED) +
				": " + deferred + "\n" + 
			indent + MessageService.getTextMessage(
												SQLState.RTS_ROWS_UPDATED) +
				" = " + rowCount + "\n" +
			indent + MessageService.getTextMessage(
												SQLState.RTS_INDEXES_UPDATED) +
				" = " + indexesUpdated + "\n" +
			dumpTimeStats(indent) + ((sourceResultSetStatistics == null) ? "" :
			sourceResultSetStatistics.getStatementExecutionPlanText(1));
	}

	/**
	 * Return information on the scan nodes from the statement execution 
	 * plan as a String.
	 *
	 * @param depth	Indentation level.
	 * @param tableName if not NULL then print information for this table only
	 *
	 * @return String	The information on the scan nodes from the 
	 *					statement execution plan as a String.
	 */
	public String getScanStatisticsText(String tableName, int depth)
	{
		if (sourceResultSetStatistics == null)
			return "";

		return sourceResultSetStatistics.getScanStatisticsText(tableName, depth);
	}

	// Formatable methods


	// Class implementation
	
	public String toString()
	{
		return getStatementExecutionPlanText(0);
	}
	/**
   * Format for display, a name for this node.
	 *
	 */
  public String getNodeName(){
    return MessageService.getTextMessage(SQLState.RTS_UPDATE);
  }
  
  // -----------------------------------------------------
  // XPLAINable Implementation
  // -----------------------------------------------------
  
    public void accept(XPLAINVisitor visitor) {
        int noChildren = 0;
        if(this.sourceResultSetStatistics!=null) noChildren++;
        
        //inform the visitor
        visitor.setNumberOfChildren(noChildren);

        // pre-order, depth-first traversal
        // me first
        visitor.visit(this);
        // then my child
        if(sourceResultSetStatistics!=null){
            sourceResultSetStatistics.accept(visitor);
		}
    }  
    public String getRSXplainType() { return XPLAINUtil.OP_UPDATE; }
    public Object getResultSetDescriptor(Object rsID, Object parentID,
            Object scanID, Object sortID, Object stmtID, Object timingID)
    {
        return new XPLAINResultSetDescriptor(
           (UUID)rsID,
           getRSXplainType(),
           getRSXplainDetails(),
           null,                              // the number of opens
           new Integer(this.indexesUpdated),
           null,                           // lock mode
           this.tableLock?"T":"R",
           (UUID)parentID,
           null,                             // estimated row count
           null,                             // estimated cost
           new Integer(this.rowCount),
           XPLAINUtil.getYesNoCharFromBoolean(this.deferred),
           null,                              // the input rows
           null,                              // the seen rows left
           null,                              // the seen rows right
           null,                              // the filtered rows
           null,                              // the returned rows
           null,                              // the empty right rows
           null,                           // index key optimization
           (UUID)scanID,
           (UUID)sortID,
           (UUID)stmtID,
           (UUID)timingID);
    }
}
