/*

   Derby - Class org.apache.derby.impl.sql.execute.AddJarConstantAction

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

package org.apache.derby.impl.sql.execute;

import org.apache.derby.iapi.services.sanity.SanityManager;
import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.sql.execute.ConstantAction;
import org.apache.derby.iapi.sql.Activation;
import org.apache.derby.catalog.UUID;

/**
 *	Constant action to Add an external  Jar file to a database. 
 *
 */
class AddJarConstantAction extends DDLConstantAction
{
	private	final String schemaName;
	private	final String sqlName;
	private final String externalPath;


	//////////////////////////////////////////////////////////////
	//
	// CONSTRUCTORS
	//
	//////////////////////////////////////////////////////////////

	/**
	 *	Make the ConstantAction to add a jar file to database.
	 *
	 *	@param	schemaName			The SchemaName for the jar file.
	 *	@param	sqlName			    The sqlName for the jar file.
	 *  @param  externalPath            The name of the file that holds the jar.
	 */
	AddJarConstantAction(
								 String schemaName,
								 String sqlName,
								 String externalPath)
	{
		this.schemaName = schemaName;
		this.sqlName = sqlName;
		this.externalPath = externalPath;
	}


	//////////////////////////////////////////////////////////////
	//
	// OBJECT SHADOWS
	//
	//////////////////////////////////////////////////////////////

	public	String	toString()
	{
		// Do not put this under SanityManager.DEBUG - it is needed for
		// error reporting.
		return "ADD JAR FILE " + schemaName + "." + sqlName;
	}

	//////////////////////////////////////////////////////////////
	//
	// CONSTANT ACTION METHODS
	//
	//////////////////////////////////////////////////////////////


	/**
	 * @see ConstantAction#executeConstantAction
	 * @exception StandardException Thrown on failure
	 */
	public void	executeConstantAction( Activation activation )
						throws StandardException
	{
		JarUtil.add(schemaName,sqlName,externalPath);
	}

}
