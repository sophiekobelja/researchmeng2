/**
 *  Derby - Class org.apache.derbyTesting.functionTests.tests.lang.CollationTest
 *  
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.derbyTesting.functionTests.tests.lang;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.derbyTesting.junit.Decorator;


public class FullCollationTests extends CollationTest {
    
    public FullCollationTests(String name) {
        super(name);
       
    }

    
    /**
     * Return a suite that uses a single use database with
     * a primary fixture from this test plus all tests
     * @param locale Locale to use for the database
     * @param baseFixture Base fixture from this test.
     * @return suite of tests to run for the given locale
     */
    public static Test suite() 
    {
        TestSuite suite = new TestSuite("FullCollationTests:territory=" +"no_NO");
        suite.addTest(new CollationTest("testNorwayCollation"));
        suite.addTest(org.apache.derbyTesting.functionTests.tests.lang._Suite.suite());
        suite.addTest(org.apache.derbyTesting.functionTests.tests.jdbcapi._Suite.suite());
        suite.addTest(org.apache.derbyTesting.functionTests.tests.store._Suite.suite());
        return Decorator.territoryCollatedDatabase(suite, "no_NO");
    }

}
