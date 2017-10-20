/*

   Derby - Class org.apache.derbyTesting.functionTests.util.PrivilegedFileOpsorTests

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
package org.apache.derbyTesting.functionTests.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * A set of operations on {$@link java.io.File} that wraps the
 * operations in privileged block of code. This class is intended to provide
 * these methods for testcases to reduce the hassle of having to wrap file
 * operations in privileged code blocks.
 * <p>
 * Derby needs to use privileged blocks in some places to avoid
 * {@link SecurityException}s being thrown, as the required privileges are
 * often granted to Derby itself, but not the higher level application code.
 * <p>
 */
public class PrivilegedFileOpsForTests {
	
	/**
     * Get the file length.
     *
     * @return byte length of the file.
     * @throws SecurityException if the required permissions to read the file,
     *      or the path it is in, are missing
     * @see File#length
     */
    public static long length(final File file)
            throws SecurityException {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be <null>");
        }
        try {
            return ((Long)AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run() throws SecurityException {
                                return new Long(file.length());
                            }
                        })).longValue();
        } catch (PrivilegedActionException pae) {
            throw (SecurityException)pae.getException();
        }
    }
    
    public static FileInputStream getFileInputStream(final File file) 
    	throws SecurityException, FileNotFoundException {
    	if (file == null) {
            throw new IllegalArgumentException("file cannot be <null>");
        }
        try {
            return ((FileInputStream)AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run() throws SecurityException, FileNotFoundException {
                                return new FileInputStream(file);
                            }
                        }));
        } catch (PrivilegedActionException pae) {
            throw (SecurityException)pae.getException();
        }
    }

    /**
     * Check if the file exists.
     *
     * @return <code>true</code> if file exists, <code>false</code> otherwise
     * @throws SecurityException if the required permissions to read the file,
     *      or the path it is in, are missing
     * @see File#exists
     */
    public static boolean exists(final File file)
            throws SecurityException {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be <null>");
        }
        try {
            return ((Boolean)AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run() throws SecurityException {
                                return new Boolean(file.exists());
                            }
                        })).booleanValue();
        } catch (PrivilegedActionException pae) {
            throw (SecurityException)pae.getException();
        }
    }
}
