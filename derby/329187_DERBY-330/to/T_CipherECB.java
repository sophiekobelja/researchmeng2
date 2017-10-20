/*

   Derby - Class org.apache.derbyTesting.unitTests.crypto.T_CipherECB

   Copyright 2000, 2005 The Apache Software Foundation or its licensors, as applicable.

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

package org.apache.derbyTesting.unitTests.crypto;

/*
	To run, put the following line in derby.properties
	derby.module.test.T_Cipher=org.apache.derbyTesting.unitTests.crypto.T_CipherECB

	and run java org.apache.derbyTesting.unitTests.harness.UnitTestMain

*/
public class T_CipherECB extends T_Cipher
{
    protected String getAlgorithm()
    {
        return "DESede/ECB/NoPadding";
    }

}
