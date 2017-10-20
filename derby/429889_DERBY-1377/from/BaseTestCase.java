/*
 *
 * Derby - Class BaseTestCase
 *
 * Copyright 2006 The Apache Software Foundation or its 
 * licensors, as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific 
 * language governing permissions and limitations under the License.
 */
package org.apache.derbyTesting.functionTests.util;

import junit.framework.TestCase;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.PrintStream;
import java.net.URL;
import java.sql.SQLException;
import java.security.AccessController;

import java.security.PrivilegedActionException;

/**
 * Base class for JUnit tests.
 */
public abstract class BaseTestCase
    extends TestCase {

    /**
     * Configuration for the test case.
     * The configuration is created based on system properties.
     *
     * @see TestConfiguration
     */
    public static final TestConfiguration CONFIG = 
        TestConfiguration.DERBY_TEST_CONFIG;
    
    /**
     * No argument constructor made private to enforce naming of test cases.
     * According to JUnit documentation, this constructor is provided for
     * serialization, which we don't currently use.
     *
     * @see #BaseTestCase(String)
     */
    private BaseTestCase() {}

    /**
     * Create a test case with the given name.
     *
     * @param name name of the test case.
     */
    public BaseTestCase(String name) {
        super(name);
    }
    
    /**
     * Run the test and force installation of a security
     * manager with the default test policy file.
     * Individual tests can run without a security
     * manager or with a different policy file using
     * the decorators obtained from SecurityManagerSetup.
     * <BR>
     * Method is final to ensure security manager is
     * enabled by default. Tests should not need to
     * ovveride runTest, instead use test methods
     * setUp, tearDown methods and decorators.
     */
    public final void runBare() throws Throwable {
    	// Not ready for prime time!
    	// SecurityManagerSetup.installSecurityManager();
    	super.runBare();
    }
    
    /**
     * Print alarm string
     * @param text String to print
     */
    public static void alarm(final String text) {
        out.println("ALARM: " + text);
    }

    /**
     * Print debug string.
     * @param text String to print
     */
    public static void println(final String text) {
        if (CONFIG.isVerbose()) {
            out.println("DEBUG: " + text);
        }
    }

    /**
     * Print debug string.
     * @param t Throwable object to print stack trace from
     */
    public static void printStackTrace(Throwable t) 
    {
        while ( t!= null) {
            t.printStackTrace(out);
            
            if (t instanceof SQLException)  {
                t = ((SQLException) t).getNextException();
            } else {
                break;
            }
        }
    }

    private final static PrintStream out = System.out;
    
    /**
     * Set system property
     *
     * @param name name of the property
     * @param value value of the property
     */
    protected static void setSystemProperty(final String name, 
					    final String value)
	throws PrivilegedActionException {
	
	AccessController.doPrivileged
	    (new java.security.PrivilegedAction(){
		    
		    public Object run(){
			System.setProperty( name, value);
			return null;
			
		    }
		    
		}
	     );
	
    }
    /**
     * Remove system property
     *
     * @param name name of the property
     */
    protected static void removeSystemProperty(final String name)
	throws PrivilegedActionException {
	
	AccessController.doPrivileged
	    (new java.security.PrivilegedAction(){
		    
		    public Object run(){
			System.getProperties().remove(name);
			return null;
			
		    }
		    
		}
	     );
	
    }    
    /**
     * Get system property.
     *
     * @param name name of the property
     */
    protected static String getSystemProperty(final String name)
	throws PrivilegedActionException {

	return (String )AccessController.doPrivileged
	    (new java.security.PrivilegedAction(){

		    public Object run(){
			return System.getProperty(name);

		    }

		}
	     );
    }
    
    /**
     * Obtain the URL for a test resource, e.g. a policy
     * file or a SQL script.
     * @param name Resource name, typically - org.apache.derbyTesing.something
     * @return URL to the resource, null if it does not exist.
     * @throws PrivilegedActionException
     */
    protected static URL getTestResource(final String name)
	throws PrivilegedActionException {

	return (URL)AccessController.doPrivileged
	    (new java.security.PrivilegedAction(){

		    public Object run(){
			return BaseTestCase.class.getClassLoader().
			    getResource(name);

		    }

		}
	     );
    }  
    
    /**
     * Assert a security manager is installed.
     *
     */
    public static void assertSecurityManager()
    {
    	assertNotNull("No SecurityManager installed",
    			System.getSecurityManager());
    }

    /**
     * Compare the contents of two streams.
     * The streams are closed after they are exhausted.
     *
     * @param is1 the first stream
     * @param is2 the second stream
     * @throws IOException if reading from the streams fail
     * @throws AssertionFailedError if the stream contents are not equal
     */
    public static void assertEquals(InputStream is1, InputStream is2)
            throws IOException {
        if (is1 == null || is2 == null) {
            assertNull("InputStream is2 is null, is1 is not", is1);
            assertNull("InputStream is1 is null, is2 is not", is2);
            return;
        }
        long index = 0;
        int b1 = is1.read();
        int b2 = is2.read();
        do {
            // Avoid string concatenation for every byte in the stream.
            if (b1 != b2) {
                assertEquals("Streams differ at index " + index, b1, b2);
            }
            index++;
            b1 = is1.read();
            b2 = is2.read();
        } while (b1 != -1 || b2 != -1);
        is1.close();
        is2.close();
    }

    /**
     * Compare the contents of two readers.
     * The readers are closed after they are exhausted.
     *
     * @param r1 the first reader
     * @param r2 the second reader
     * @throws IOException if reading from the streams fail
     * @throws AssertionFailedError if the reader contents are not equal
     */
    public static void assertEquals(Reader r1, Reader r2)
            throws IOException {
        long index = 0;
        if (r1 == null || r2 == null) {
            assertNull("Reader r2 is null, r1 is not", r1);
            assertNull("Reader r1 is null, r2 is not", r2);
            return;
        }
        int c1 = r1.read();
        int c2 = r2.read();
        do {
            // Avoid string concatenation for every char in the stream.
            if (c1 != c2) {
                assertEquals("Streams differ at index " + index, c1, c2);
            }
            index++;
            c1 = r1.read();
            c2 = r2.read();
        } while (c1 != -1 || c2 != -1);
        r1.close();
        r2.close();
    }
} // End class BaseTestCase
