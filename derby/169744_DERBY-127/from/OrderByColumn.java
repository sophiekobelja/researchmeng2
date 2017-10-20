/*

   Derby - Class org.apache.derby.impl.sql.compile.OrderByColumn

   Copyright 1997, 2004 The Apache Software Foundation or its licensors, as applicable.

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

package	org.apache.derby.impl.sql.compile;

import org.apache.derby.iapi.types.TypeId;

import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.reference.SQLState;

import org.apache.derby.iapi.services.sanity.SanityManager;

import org.apache.derby.iapi.sql.compile.NodeFactory;
import org.apache.derby.iapi.sql.compile.C_NodeTypes;

/**
 * An OrderByColumn is a column in the ORDER BY clause.  An OrderByColumn
 * can be ordered ascending or descending.
 *
 * We need to make sure that the named columns are
 * columns in that query, and that positions are within range.
 *
 * @author ames
 */
public class OrderByColumn extends OrderedColumn {

	private ResultColumn	resultCol;
	private boolean			ascending = true;
	private ValueNode expression;


    	/**
	 * Initializer.
	 *
	 * @param expression            Expression of this column
	 */
	public void init(Object expression)
	{
		this.expression = (ValueNode)expression;
	}
	
	/**
	 * Convert this object to a String.  See comments in QueryTreeNode.java
	 * for how this should be done for tree printing.
	 *
	 * @return	This object as a String
	 */
	public String toString() {
		if (SanityManager.DEBUG) {
			return expression.toString();
		} else {
			return "";
		}
	}

	/**
	 * Mark the column as descending order
	 */
	public void setDescending() {
		ascending = false;
	}

	/**
	 * Get the column order.  Overrides 
	 * OrderedColumn.isAscending.
	 *
	 * @return true if ascending, false if descending
	 */
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * Get the underlying ResultColumn.
	 *
	 * @return The underlying ResultColumn.
	 */
	ResultColumn getResultColumn()
	{
		return resultCol;
	}

	/**
	 * Get the underlying expression, skipping over ResultColumns that
	 * are marked redundant.
	 */
	ValueNode getNonRedundantExpression()
	{
		ResultColumn	rc;
		ValueNode		value;
		ColumnReference	colref = null;

		for (rc = resultCol; rc.isRedundant(); rc = colref.getSource())
		{
			value = rc.getExpression();

			if (value instanceof ColumnReference)
			{
				colref = (ColumnReference) value;
			}
			else
			{
				if (SanityManager.DEBUG)
				{
					SanityManager.THROWASSERT(
						"value should be a ColumnReference, but is a " +
						value.getClass().getName());
				}
			}
		}

		return rc.getExpression();
	}

	/**
	 * Bind this column.
	 *
	 * @param target	The result set being selected from
	 *
	 * @exception StandardException		Thrown on error
	 * @exception StandardException		Thrown when column not found
	 */
	public void bindOrderByColumn(ResultSetNode target)
				throws StandardException 
	{
		if(expression instanceof ColumnReference){
		
			ColumnReference cr = (ColumnReference) expression;
			
			resultCol = resolveColumnReference(target,
							   cr);
			
			columnPosition = resultCol.getColumnPosition();

		}else if(isReferedColByNum(expression)){
			
			ResultColumnList targetCols = target.getResultColumns();
			columnPosition = ((Integer)expression.getConstantValueAsObject()).intValue();
			resultCol = targetCols.getOrderByColumn(columnPosition);
			
			if (resultCol == null) {
				throw StandardException.newException(SQLState.LANG_COLUMN_OUT_OF_RANGE, 
								     String.valueOf(columnPosition));
			}

		}else{
			ResultColumnList targetCols = target.getResultColumns();
			ResultColumn col = null;
			int i = 1;
			
			for(i = 1;
			    i <= targetCols.size();
			    i  ++){
				
				col = targetCols.getOrderByColumn(i);
				if(col != null && 
				   col.getExpression() == expression){
					
					break;
				}
			}
			
			resultCol = col;
			columnPosition = i;
		    
		}

		// Verify that the column is orderable
		resultCol.verifyOrderable();
	}

	/**
	 * Pull up this orderby column if it doesn't appear in the resultset
	 *
	 * @param target	The result set being selected from
	 *
	 */
	public void pullUpOrderByColumn(ResultSetNode target)
				throws StandardException 
	{
		if(expression instanceof ColumnReference){

			ColumnReference cr = (ColumnReference) expression;

			ResultColumnList targetCols = target.getResultColumns();
			resultCol = targetCols.getOrderByColumn(cr.getColumnName(),
								cr.tableName != null ? 
								cr.tableName.getFullTableName():
								null);

			if(resultCol == null){
				resultCol = (ResultColumn) getNodeFactory().getNode(C_NodeTypes.RESULT_COLUMN,
										    cr.getColumnName(),
										    cr,
										    getContextManager());
				targetCols.addResultColumn(resultCol);
				targetCols.incOrderBySelect();
			}
			
		}else if(!isReferedColByNum(expression)){
			ResultColumnList	targetCols = target.getResultColumns();
			resultCol = (ResultColumn) getNodeFactory().getNode(C_NodeTypes.RESULT_COLUMN,
									    null,
									    expression,
									    getContextManager());
			targetCols.addResultColumn(resultCol);
			targetCols.incOrderBySelect();
		}
	}

	/**
	 * Order by columns now point to the PRN above the node of interest.
	 * We need them to point to the RCL under that one.  This is useful
	 * when combining sorts where we need to reorder the sorting
	 * columns.
	 *
	 * @return Nothing.
	 */
	void resetToSourceRC()
	{
		if (SanityManager.DEBUG)
		{
			if (! (resultCol.getExpression() instanceof VirtualColumnNode))
			{
				SanityManager.THROWASSERT(
					"resultCol.getExpression() expected to be instanceof VirtualColumnNode " +
					", not " + resultCol.getExpression().getClass().getName());
			}
		}

		VirtualColumnNode vcn = (VirtualColumnNode) resultCol.getExpression();
		resultCol = vcn.getSourceResultColumn();
	}

	/**
	 * Is this OrderByColumn constant, according to the given predicate list?
	 * A constant column is one where all the column references it uses are
	 * compared equal to constants.
	 */
	boolean constantColumn(PredicateList whereClause)
	{
		ValueNode sourceExpr = resultCol.getExpression();

		return sourceExpr.constantExpression(whereClause);
	}

	/**
	 * Remap all the column references under this OrderByColumn to their
	 * expressions.
	 *
	 * @exception StandardException		Thrown on error
	 */
	void remapColumnReferencesToExpressions() throws StandardException
	{
		resultCol.setExpression(
			resultCol.getExpression().remapColumnReferencesToExpressions());
	}

	private static boolean isReferedColByNum(ValueNode expression) 
	throws StandardException{
		
		if(!expression.isConstantExpression()){
			return false;
		}
		
		return expression.getConstantValueAsObject() instanceof Integer;
	}

	
	private static ResultColumn resolveColumnReference(ResultSetNode target,
							   ColumnReference cr)
	throws StandardException{
		
		ResultColumn resultCol = null;
		
		int					sourceTableNumber = -1;
		
		//bug 5716 - for db2 compatibility - no qualified names allowed in order by clause when union/union all operator is used 

		if (target instanceof SetOperatorNode && cr.getTableName() != null){
			String fullName = cr.getSQLColumnName();
			throw StandardException.newException(SQLState.LANG_QUALIFIED_COLUMN_NAME_NOT_ALLOWED, fullName);
		}

		if(cr.getTableNameNode() != null){
			TableName tableNameNode = cr.getTableNameNode();

			FromTable fromTable = target.getFromTableByName(tableNameNode.getTableName(),
									(tableNameNode.hasSchema() ?
									 tableNameNode.getSchemaName():null),
									true);
			if(fromTable == null){
				fromTable = target.getFromTableByName(tableNameNode.getTableName(),
								      (tableNameNode.hasSchema() ?
								       tableNameNode.getSchemaName():null),
								      false);
				if(fromTable == null){
					String fullName = cr.getTableNameNode().toString();
					throw StandardException.newException(SQLState.LANG_EXPOSED_NAME_NOT_FOUND, fullName);
				}
			}

			/* HACK - if the target is a UnionNode, then we have to
			 * have special code to get the sourceTableNumber.  This is
			 * because of the gyrations we go to with building the RCLs
			 * for a UnionNode.
			 */
			if (target instanceof SetOperatorNode)
			{
				sourceTableNumber = ((FromTable) target).getTableNumber();
			}
			else
			{
				sourceTableNumber = fromTable.getTableNumber();
			}
			
		}

		ResultColumnList	targetCols = target.getResultColumns();

		resultCol = targetCols.getOrderByColumn(cr.getColumnName(),
							cr.getTableName(),
							sourceTableNumber);
							
		if (resultCol == null || resultCol.isNameGenerated()){
			String errString = cr.columnName;
			throw StandardException.newException(SQLState.LANG_ORDER_BY_COLUMN_NOT_FOUND, errString);
		}

		return resultCol;

	}

}
