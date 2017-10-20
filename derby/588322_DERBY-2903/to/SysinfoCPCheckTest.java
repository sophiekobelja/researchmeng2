/*

   Derby - Class org.apache.derbyTesting.functionTests.tests.tools.SysinfoCPCheckTest

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

package org.apache.derbyTesting.functionTests.tests.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.derbyTesting.junit.BaseJDBCTestCase;
import org.apache.derbyTesting.junit.Derby;

public class SysinfoCPCheckTest extends BaseJDBCTestCase {

    public SysinfoCPCheckTest(String name) { 
        super(name); 
    }

    final String outputEncoding = "US-ASCII";

    private ByteArrayOutputStream rawBytes;

    private static boolean isClient = true;
    private static boolean isServer = true;
    
    public static Test suite() {

        if (!Derby.hasTools())
            return new TestSuite("empty: no tools support");
        
        // check to see if we have derbynet.jar or derbyclient.jar
        // before starting the security manager...
        if (!Derby.hasServer())
            isServer=false;
        if (!Derby.hasClient())
            isClient=false;

        Test suite = new TestSuite(SysinfoCPCheckTest.class, "Sysinfo ClassPath Checker");        
        return suite;
    }

    /**
     * Test sysinfo.testClassPathChecker()
     *
     * Tests sysinfo classpathtester
     * This test compares expected output strings; expected language is en_US
     * 
     */
    /**
     *  Test Classpath Checker output for 3 supported variations
     */
    public void testClassPathChecker() throws IOException {
        String Success = "SUCCESS: All Derby related classes found in class path.";
        // for testing the -cp with valid class
        String thisclass = "org.apache.derbyTesting.functionTests.tests.tools." +
        "SysinfoCPCheckTest.class";
        // initialize the array of arguments and expected return strings
        // The purpose of the values in the inner level is:
        // {0: argument to be passed with -cp,
        //  1: line number in the output to compare with,
        //  2: string to compare the above line with
        //  3: optional string to search for in addition to the above line
        //  4: a string/number to unravel the output
        final String[][] tstargs = {
                // empty string; should check all; what to check? Just check top
                // to ensure it recognizes it needs to check all.
                {null, "0", "Testing for presence of all Derby-related librari" +
                    "es; typically, only some are needed.", null, "0"},
                    // incorrect syntax, or 'args' - should return usage
                    {"a", "0", "USAGE: java org.apache.derby.tools.sysinfo -cp [" +
                        " [ embedded ][ server ][ client] [ db2driver ] [ tools ]" +
                        " [  anyClass.class ] ]", null, "1"},
                        {"embedded", "6", Success, "derby.jar", "2"}, 
                        {"server", "10", Success, "derbynet.jar", "3"},
                        {"tools", "6", Success, "derbytools.jar", "4"},
                        {"client", "6", Success, "derbyclient.jar", "5"},
                        // let's not test the following valid value, it's likely to fail:
                        // {"db2driver", "6", Success, "db2jcc.jar"},
                        {thisclass, "6", Success, "SysinfoCPCheckTest", "6"},
                        // neg tst, hope this doesn't exist
                        {"nonexist.class", "6", "    (nonexist not found.)", null, "7"}
        };

        // First obtain the output of all sysinfo commands we want to test
        // we print a number for each of the commands to test for unraveling
        // them later.

        final String outputEncoding = "US-ASCII";

        PrintStream out = System.out;
        try {
            PrintStream testOut = new PrintStream(getOutputStream(),
                    false, outputEncoding);
            setSystemOut(testOut);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        for (int tst=0; tst<tstargs.length ; tst++)
        {
            if (!checkClientOrServer(tstargs[tst][0]))
                continue;

            // print out a number to unravel the fulloutput later
            System.out.println(tstargs[tst][4]);

            // The first command has only 1 arg
            if (tstargs[tst][0] == null)
                org.apache.derby.tools.sysinfo.main(
                    new String[] {"-cp"} );
            else
                org.apache.derby.tools.sysinfo.main(
                    new String[] {"-cp", tstargs[tst][0]} );
        }

        setSystemOut(out);

        rawBytes.flush();
        rawBytes.close();

        byte[] testRawBytes = rawBytes.toByteArray();

        String s = null;

        try {

            BufferedReader sysinfoOutput = new BufferedReader(
                new InputStreamReader(
                    new ByteArrayInputStream(testRawBytes),
                        outputEncoding));

            s = sysinfoOutput.readLine();

            // evaluate the output
            for (int tst=0; tst<tstargs.length ; tst++)
            {
                //System.out.println("cp command: -cp " + tstargs[tst][0]);

                // compare the sentence picked

                // first one is a bit different - is classpath dependent, so
                // we're not going to look through all lines.
                if (tstargs[tst][4].equals("0"))
                {
                    s=sysinfoOutput.readLine();
                    assertEquals(tstargs[tst][2], s);
                    while (!s.equals(tstargs[tst+1][4]))
                    {
                        s=sysinfoOutput.readLine();
                    }
                    continue;
                }

                if (!checkClientOrServer(tstargs[tst][0]))
                    continue;

                if (!s.equals(tstargs[tst][4]))
                    fail("out of sync with expected lines, indicates a problem");

                // get the appropriate line for the full line comparison
                int linenumber = Integer.parseInt(tstargs[tst][1]);

                boolean found = false;

                for (int i=0; i<linenumber; i++)
                {
                    s = sysinfoOutput.readLine();
                    if (tstargs[tst][3] != null)
                    {
                        // do the search for the optional string comparison
                        if (s.indexOf(tstargs[tst][3])>0)
                            found = true;
                    }
                }
                if (tstargs[tst][3] != null && !found)
                    fail ("did not find the string searched for: " + 
                            tstargs[tst][3] + " for command -cp: " + tstargs[tst][0]);

                // read the line to be compared
                s = sysinfoOutput.readLine();

                if (s == null)
                    fail("encountered unexpected null strings");
                else
                {
                    assertEquals(tstargs[tst][2], s);
                }

                // read one more line - should be the next command's sequence number
                s = sysinfoOutput.readLine();
            }

            sysinfoOutput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkClientOrServer(String kind)
    {
        if (kind == null)
            return true;
        // if there is no derbynet.jar, the syntax should still
        // work, but the comparisons will fail. So never mind.
        if (kind.equals("server")) 
            return isServer;
        // same for derbyclient.jar
        if (kind.equals("client"))
            return isClient;
        return true;
    }

    /**
     * Need to capture System.out so that we can compare it.
     * @param out
     */
    private void setSystemOut(final PrintStream out)
    {
        AccessController.doPrivileged
        (new java.security.PrivilegedAction(){

            public Object run(){
                System.setOut(out);
                return null;
            }
        }
        );       
    }

    OutputStream getOutputStream() {
        return rawBytes = new ByteArrayOutputStream(20 * 1024);
    }

    protected void tearDown() throws Exception {
        rawBytes = null;
        super.tearDown();
    }
}