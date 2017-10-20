/*

   Derby - Class org.apache.derby.iapi.services.sanity.AssertFailure

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

package org.apache.derby.shared.common.sanity;

import java.io.*;

/**
 * AssertFailure is raised when an ASSERT check fails.
 * Because assertions are not used in production code,
 * are never expected to fail, and recovering from their
 * failure is expected to be hard, they are under
 * RuntimeException so that no one needs to list them
 * in their throws clauses.  An AssertFailure at the
 * outermost system level will result in system shutdown.
 **/
public class AssertFailure extends RuntimeException
{
	private Throwable nestedException;

	/**
	 * This constructor takes the pieces of information
	 * expected for each error.
	 *
	 * @param message the message associated with
	 * the error.
	 *
	 * @param nestedError errors can be nested together;
	 * if this error has another error associated with it,
	 * it is specified here. The 'outermost' error should be
	 * the most sever error; inner errors should be providing
	 * additional information about what went wrong.
	 **/
	public AssertFailure(String message, Throwable nestedError)
	{
		super(message);
		nestedException = nestedError;
	}

	/**
	 * This constructor expects no arguments or nested error.
	 **/
	public AssertFailure(String message)
	{
		super(message);
	}

	public void printStackTrace() {
		super.printStackTrace();
		if (nestedException != null)
			nestedException.printStackTrace();
	}
	public void printStackTrace(PrintStream s) {
		super.printStackTrace(s);
		if (nestedException != null)
			nestedException.printStackTrace(s);
	}
	public void printStackTrace(PrintWriter s) {
		super.printStackTrace(s);
		if (nestedException != null)
			nestedException.printStackTrace(s);
	}
}
