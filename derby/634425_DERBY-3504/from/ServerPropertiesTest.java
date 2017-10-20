/*

   Derby - Class org.apache.derbyTesting.functionTests.tests.derbynet.ServerPropertiesTest

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
package org.apache.derbyTesting.functionTests.tests.derbynet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.AccessController;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.derbyTesting.junit.BaseJDBCTestCase;
import org.apache.derbyTesting.junit.Derby;
import org.apache.derbyTesting.junit.JDBC;
import org.apache.derbyTesting.junit.NetworkServerTestSetup;
import org.apache.derbyTesting.junit.SecurityManagerSetup;
import org.apache.derbyTesting.junit.SupportFilesSetup;
import org.apache.derbyTesting.junit.TestConfiguration;
import org.apache.derbyTesting.junit.Utilities;

/** 
 * This test tests the derby.properties, system properties and command line
 * parameters to make sure the pick up settings in the correct order. 
 * Search order is:
 *     command line parameters
 *     System properties
 *     derby.properties
 *     default     
 * The command line should take precedence
 * 
 * The test also tests start server by specifying system properties without
 * values; in this case the server will use default values.
 */

public class ServerPropertiesTest  extends BaseJDBCTestCase {
    
    // helper state for intercepting server error messages;
    // needed by fixture testToggleTrace
    private InputStream[]  _inputStreamHolder;
    
    //create own policy file
    private static String POLICY_FILE_NAME = 
        "functionTests/tests/derbynet/ServerPropertiesTest.policy";
    private static String TARGET_POLICY_FILE_NAME = "server.policy";
    private int[] portsSoFar;
    
    public ServerPropertiesTest(String name) {
        super(name);
        _inputStreamHolder = new InputStream[1];
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite("ServerPropertiesTest");
          
        if (!Derby.hasServer()) return suite;
        // don't run with JSR169 for 1. this is network server and
        // 2. the java executable may be named differently
        if (JDBC.vmSupportsJSR169()) return suite;
        
        // this fixture doesn't use a client/server setup, instead does the 
        // relevant starting/stopping inside the test
        // Add security manager policy that allows executing java commands
        suite.addTest(decorateTest("ttestSetPortPriority", 
                new String[] {}, new String[] {}, false));
        
        // test unfinished properties settings. 
        // decorateTest adds policy file and sets up properties
        // the properties settings are incorrect i.e. they have no value
        String[] badServerProperties = {
                "derby.drda.logConnections=",
                "derby.drda.traceAll=",
                "derby.drda.traceDirectory=",
                "derby.drda.keepAlive=",
                "derby.drda.timeSlice=",
                "derby.drda.host=",
                "derby.drda.portNumber=",
                "derby.drda.minThreads=",
                "derby.drda.maxThreads=",
                "derby.drda.startNetworkServer=",
                "derby.drda.debug="
                };
        // fixture hits error DRDA_MissingNetworkJar (Cannot find derbynet.jar) so,
        // only run with jars
        if (TestConfiguration.loadingFromJars())
            suite.addTest(decorateTest("ttestDefaultProperties", 
                badServerProperties, new String[] {}, true));
        
        // The other fixtures, testToggleTrace (trace on/off), 
        // testToggleLogConnections (logconnections on/off) , and
        // testWrongCommands can all use the default setup with adjusted policy
        
        // need english locale so we can compare command output for those tests 
        if (!Locale.getDefault().getLanguage().equals("en"))
            return suite;
        
        Test test = TestConfiguration
            .clientServerSuite(ServerPropertiesTest.class);
        
        // Install a security manager using the special policy file.
        test = decorateWithPolicy(test);
        suite.addTest(test);
        return suite;
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        POLICY_FILE_NAME = null;
        TARGET_POLICY_FILE_NAME = null;
        _inputStreamHolder = null;
        if (portsSoFar != null)
        {
            for (int i = 0 ; i < portsSoFar.length ; i++)
            {
                try {
                    shutdownServer(portsSoFar[i], true);
                } catch (SQLException e) {
                    fail("could not shutdown server at port " + portsSoFar[i]);
                }
            }
            portsSoFar=null;
        }
    }
    
    /**
     * <p>
     * Compose the required decorators to bring up the server in the correct
     * configuration.
     * </p>
     */
    private static Test decorateTest(String testName, 
            String[] startupProperties, String[] startupArgs,
            boolean startServer)
    {
        ServerPropertiesTest spt = new ServerPropertiesTest(testName);
        String [] startupProps;
        if (startupProperties == null)
            startupProps = new String[] {};
        else
            startupProps = startupProperties;
        if (startupArgs == null)
            startupArgs = new String[]{};
        NetworkServerTestSetup networkServerTestSetup;
        if (startServer)
        {
            // start networkServer as a process
            networkServerTestSetup = new NetworkServerTestSetup(
                spt, startupProps, startupArgs, true, spt._inputStreamHolder);
        }
        else
        {
            // get networkserver setup but don't start anything
            networkServerTestSetup = new NetworkServerTestSetup(
                spt, true, false);
        }
        Test test = decorateWithPolicy(networkServerTestSetup);
        test = TestConfiguration.defaultServerDecorator(test);
        return test;
    }   
    
    /**
     * Construct the name of the server policy file.
     */
    private String makeServerPolicyName()
    {
        try {
            String  userDir = getSystemProperty( "user.dir" );
            String  fileName = userDir + File.separator + SupportFilesSetup.EXTINOUT + File.separator + TARGET_POLICY_FILE_NAME;
            File      file = new File( fileName );
            String  urlString = file.toURL().toExternalForm();

            return urlString;
        }
        catch (Exception e)
        {
            System.out.println( "Unexpected exception caught by makeServerPolicyName(): " + e );

            return null;
        }
    }
    
    // grant ALL FILES execute, and getPolicy permissions,
    // as well as write for the trace files.
    private static Test decorateWithPolicy(Test test) {
        String serverPolicyName = new ServerPropertiesTest("test").makeServerPolicyName();
        //
        // Install a security manager using the initial policy file.
        //
        test = new SecurityManagerSetup(test,serverPolicyName );
        // Copy over the policy file we want to use.
        //
        test = new SupportFilesSetup(
            test, null, new String[] {POLICY_FILE_NAME},
            null, new String[] {TARGET_POLICY_FILE_NAME}
        );
        return test;
    }

    private static void verifyProperties(String[] expectedValues) { 
        Properties p;
        try {
            p = NetworkServerTestSetup.getNetworkServerControl().getCurrentProperties();
        } catch (Exception e) {
            p = null; // should be ok to set to null (to satisfy compiler)
            // as fail will exit without further checks.
            e.printStackTrace();
            fail("unexpected exception getting properties from server");
        }
        
        Enumeration e = p.propertyNames();
        // for debugging:
        for (int i=0 ; i<expectedValues.length; i++){
            println("expV: " + expectedValues[i]);
        }
        assertEquals(expectedValues.length , p.size());
        for ( int i = 0 ; i < p.size() ; i++)
        {
            String propName = (String)e.nextElement();
            // next line for debugging
            println("propName: " + propName);
            String propval = (String)p.get(propName);
            assertEquals(expectedValues[i], propval);
        }
        p = null;
    }
    
    public int getAlternativePort() throws SQLException {

        Exception failException = null;
        // start with the default port + 1
        // there may be a smarter way to get the starting point...
        int possiblePort = TestConfiguration.getCurrent().getPort();
        if (!(possiblePort > 0))
            possiblePort = 1528;
        else
            possiblePort = possiblePort + 1;
        try {
            boolean portOK = false;
            while (!portOK) {
                // check for first one in use
                NetworkServerControl networkServer =
                    new NetworkServerControl(InetAddress.getByName("localhost"), possiblePort);
                // Ping and wait for the network server to reply
                boolean started = false;

                try {
                    networkServer.ping();
                    // If ping throws no exception the server is running
                    started = true;
                } catch(Exception e) {         
                    failException = e;
                }
                // Check if we got a reply on ping
                if (!started) {
                    // we'll assume we can use this port. 
                    // If there was some other problem with the pinging, it'll
                    // become clear when someone attempts to use the port
                    portOK = true;
                }
                else { // this port's in use.
                    possiblePort = possiblePort + 1;
                }
            }
        } catch (Exception e) {
            SQLException se = new SQLException("Error pinging network server");
            se.initCause(failException);
            throw se;
        }        
        return possiblePort;
    }
    
    /**
     *  Ping for the server to be up - or down.
     *  @param port port number to be used in the ping
     *  @param expectServerUp indicator whether the server is expected to be up
     */
    private boolean canPingServer(int port, boolean expectServerUp) 
    throws SQLException {
        
        boolean serverUp = false;
        try {
            serverUp = NetworkServerTestSetup.pingForServerUp(
                NetworkServerTestSetup.getNetworkServerControl(port), null,
                expectServerUp);
        } catch (Exception e) {
            fail("unexpected Exception while pinging");
        }
        return serverUp;
    }
    
    // obtain & shutdown the network server;
    // port needs to be passed in to verify it's down;
    private String shutdownServer(int port, boolean specifyPort) 
    throws SQLException {
        try {
            if (specifyPort)
            {
                NetworkServerControl nsctrl = 
                    NetworkServerTestSetup.getNetworkServerControl(port);
                nsctrl.shutdown();
            }
            else
            {
                NetworkServerControl nsctrl = 
                    NetworkServerTestSetup.getNetworkServerControlDefault();
                nsctrl.shutdown();
            }
        } catch (Exception e) {
            return "failed to shutdown server with API parameter (" + e + ")";
        }
        if (canPingServer(port,false)) {
            return "Can still ping server";
        }
        return null;
    }


    // obtain & start the network server without specifying port;
    // port needs to be passed in to verify it's up.
    public String startServer(int port, boolean specifyPort) 
    throws SQLException {
        try {
            if (specifyPort)
            {
                NetworkServerControl nsctrl = 
                    NetworkServerTestSetup.getNetworkServerControl(port);
                // For debugging, to make output come to console uncomment:
                //nsctrl.start(new PrintWriter(System.out, true));
                // and comment out:
                nsctrl.start(null);
                NetworkServerTestSetup.waitForServerStart(nsctrl);
            }
            else
            {
                NetworkServerControl nsctrl = 
                    NetworkServerTestSetup.getNetworkServerControlDefault();
                // For debugging, to make output come to console uncomment:
                //nsctrl.start(new PrintWriter(System.out, true));
                // and comment out:
                nsctrl.start(null);
                NetworkServerTestSetup.waitForServerStart(nsctrl);
            }
        } catch (Exception e) {
            return "failed to start server with port " + port;
        }
        // check that we have this server up now
        if (!canPingServer(port, true)) {
            return "Cannot ping server started with port set to " + port;
        }
        return null;
    }
    
   public void checkWhetherNeedToShutdown(int[] ports, String failReason) {
       
       portsSoFar = ports;
       if (!(failReason == null))
       {
           fail(failReason);
       }
   }
   
   /**
    * Execute command and verify that it completes successfully
    * @param Cmd array of java arguments for command
    * @throws InterruptedException
    * @throws IOException
    */
   private void  assertSuccessfulCmd(String expectedString, String[] Cmd) throws InterruptedException, IOException {
       assertExecJavaCmdAsExpected(new String[] {expectedString}, Cmd, 0);
   }

    /**
     *  Test port setting priority
     */
    public void ttestSetPortPriority() 
    throws SQLException, InterruptedException, IOException {
        // default is 1527. The test harness configuration would
        // use the API and add the port number. We want to test all
        // 4 mechanisms for specifying the port.
        // To ensure getting a unique port number, this test leaves open
        // each server for a bit.
        // as we need to test the default as well as with setting various
        // properties, this test can't rely on the testsetup.

        // So, first, bring default server down if up
        // Note: if the harness gets modified to accomodate splitting
        //    over different networkservers, there maybe something more
        //    appropriate than shutting down the default server.
        // we really expect the server to be down, let's
        // not do any waiting around
        NetworkServerTestSetup.setWaitTime(0);
        if (canPingServer(1527, false)) {
            // for now, shutdown
            shutdownServer(1527, false);
        }
        NetworkServerTestSetup.setDefaultWaitTime();

        // start the default, which at this point should be localhost and 1527
        String actionResult = startServer(1527, false);
        checkWhetherNeedToShutdown(new int[] {1527}, actionResult);
        
        // set derby.drda.portNumber to an alternate number in derby.properties
        int firstAlternatePort = getAlternativePort();
        final Properties derbyProperties = new Properties();
        derbyProperties.put("derby.drda.portNumber", 
                new Integer(firstAlternatePort).toString());

        final String derbyHome = getSystemProperty("derby.system.home");
        Boolean b = (Boolean)AccessController.doPrivileged
        (new java.security.PrivilegedAction(){
            public Object run(){
                boolean fail = false;
                try {
                    FileOutputStream propFile = 
                        new FileOutputStream(derbyHome + File.separator + "derby.properties");
                    derbyProperties.store(propFile,"testing derby.properties");
                    propFile.close();
                } catch (IOException ioe) {
                    fail = true;
                }
                return new Boolean(fail);
            }
        });
        if (b.booleanValue())
        {
            checkWhetherNeedToShutdown(new int[] {1527}, "failed to write derby.properties");
        }
        // have to shutdown engine to force read of derby.properties
        TestConfiguration.getCurrent().shutdownEngine();
        actionResult = startServer(firstAlternatePort, false);
        checkWhetherNeedToShutdown(new int[] {1527, firstAlternatePort}, actionResult);

        final int secondAlternatePort = getAlternativePort();
        // Now set system properties.
        setSystemProperty("derby.drda.portNumber", 
            new Integer(secondAlternatePort).toString());
        actionResult = startServer(secondAlternatePort, false);
        checkWhetherNeedToShutdown( new int[] {1527, firstAlternatePort, secondAlternatePort},
            actionResult);
        
        // now try with specifying port
        // Note that we didn't unset the system property yet, nor did
        // we get rid of derby.properties...
        // command line parameter should take hold
        int thirdAlternatePort = getAlternativePort();
        actionResult = startServer(thirdAlternatePort, true);
        checkWhetherNeedToShutdown(new int[] {1527, firstAlternatePort, secondAlternatePort,
            thirdAlternatePort}, actionResult);

        // now with -p. 
        int fourthAlternatePort = getAlternativePort();
        String[] commandArray = {"-Dderby.system.home=" + derbyHome,
            "org.apache.derby.drda.NetworkServerControl", "-p",
            String.valueOf(fourthAlternatePort).toString(), 
            "-noSecurityManager", "start"};
        Utilities.execJavaCmd(commandArray);
        
        if (!canPingServer(fourthAlternatePort, true)) {
            actionResult = "Can not ping server specified with -p";
        }
        checkWhetherNeedToShutdown(new int[] {1527, firstAlternatePort, secondAlternatePort,
            thirdAlternatePort, fourthAlternatePort}, actionResult);
                        
        // shutdown with -p
        commandArray = new String[] {"-Dderby.system.home=" + derbyHome,
                "org.apache.derby.drda.NetworkServerControl", "-p",
                String.valueOf(fourthAlternatePort).toString(), 
                "-noSecurityManager", "shutdown"};
        Utilities.execJavaCmd(commandArray);

        if (canPingServer(fourthAlternatePort, false)) {
            actionResult = "Can still ping server specified with -p";
        }
        checkWhetherNeedToShutdown(new int[] {1527, firstAlternatePort, secondAlternatePort,
            thirdAlternatePort, fourthAlternatePort}, actionResult);
            
        // shutdown with port specified in constructor
        actionResult = shutdownServer(thirdAlternatePort, true);
        checkWhetherNeedToShutdown( new int[] {1527, firstAlternatePort, secondAlternatePort,
            thirdAlternatePort}, actionResult);
        
        // shutdown using System property
        actionResult = shutdownServer(secondAlternatePort, false);
        checkWhetherNeedToShutdown ( new int[] {1527, firstAlternatePort, secondAlternatePort},
            actionResult);
        // remove system property
        removeSystemProperty("derby.drda.portNumber");

        // shutdown server with port set in derby.properties
        actionResult = shutdownServer(firstAlternatePort, false);
        checkWhetherNeedToShutdown ( new int[] {1527, firstAlternatePort},
            actionResult);
        // remove derby.properties
        Boolean ret = (Boolean) AccessController.doPrivileged
        (new java.security.PrivilegedAction() {
            public Object run() {
                return Boolean.valueOf((new File(
                    derbyHome+File.separator + "derby.properties")).delete());
            }
        }
        );
        if (ret.booleanValue() == false) {
            checkWhetherNeedToShutdown ( new int[] {1527, firstAlternatePort},
                "unable to remove derby.properties");
        }
        // have to shutdown engine to force re-evaluation of derby.properties
        TestConfiguration.getCurrent().shutdownEngine();
        
        // shutdown the default server
        actionResult = shutdownServer(1527, false);
        checkWhetherNeedToShutdown ( new int[] {1527}, actionResult);
    }
    
    /**
     *   Test start server specifying system properties without values
     */
    public void ttestDefaultProperties() throws SQLException
    {
        //check that default properties are used
        verifyProperties(new String[] {
                // getProperties returns properties in sequence:
                // maxThreads; sslMode; keepAlive; minThreads; portNumber;
                // logConnections; timeSlice; startNetworkServer; host; traceAll 
                "0", "off", "true", "0", 
                String.valueOf(TestConfiguration.getCurrent().getPort()),
                "false", "0", "false", 
                String.valueOf(TestConfiguration.getCurrent().getHostName()), 
                "false"});     
    }
       
    /**
     *   Test trace command on - property traceAll should get set
     */
    public void testToggleTrace() 
    throws SQLException, IOException, InterruptedException
    {        
        String[] expectedTraceOff = new String[] {
                // getProperties returns properties in sequence:
                // traceDirectory; maxThreads; sslMode; keepAlive; minThreads; 
                // portNumber; logConnections; timeSlice; startNetworkServer;
                // host; traceAll
                getSystemProperty("derby.system.home"),
                "0", "off", "true", "0", 
                String.valueOf(TestConfiguration.getCurrent().getPort()),
                "false", "0", "false", 
                //String.valueOf(TestConfiguration.getCurrent().getHostName()),
                "127.0.0.1", 
                "false"};     
        String[] expectedTraceOn = new String[] {
                // getProperties returns properties in sequence:
                // traceDirectory; maxThreads; sslMode; keepAlive; minThreads; 
                // portNumber; logConnections; timeSlice; startNetworkServer;
                // host; traceAll
                getSystemProperty("derby.system.home"),
                "0", "off", "true", "0", 
                String.valueOf(TestConfiguration.getCurrent().getPort()),
                "false", "0", "false", 
                //String.valueOf(TestConfiguration.getCurrent().getHostName()),
                "127.0.0.1", 
                "true"};     
        
        verifyProperties(expectedTraceOff);     

        String[] traceCmd = new String[] {
            "org.apache.derby.drda.NetworkServerControl", "trace", "on" };
        assertSuccessfulCmd("Trace turned on for all sessions.", traceCmd);
        verifyProperties(expectedTraceOn);     

        traceCmd = new String[] {
                "org.apache.derby.drda.NetworkServerControl", "trace", "off" };
        assertSuccessfulCmd("Trace turned off for all sessions", traceCmd);
        // traceAll should be back to false
        verifyProperties(expectedTraceOff);     
    }

    /**
     *   Test logconnections on
     */
    public void testToggleLogConnections() 
    throws SQLException, IOException, InterruptedException
    {
        String[] expectedLogConnectionsOff = new String[] {
                // getProperties returns properties in sequence:
                // traceDirectory; maxThreads; sslMode; keepAlive; minThreads; 
                // portNumber; logConnections; timeSlice; startNetworkServer;
                // host; traceAll
                getSystemProperty("derby.system.home"),
                "0", "off", "true", "0", 
                String.valueOf(TestConfiguration.getCurrent().getPort()),
                "false", "0", "false", 
                //String.valueOf(TestConfiguration.getCurrent().getHostName()),
                "127.0.0.1", 
                "false"};     
        String[] expectedLogConnectionsOn = new String[] {
                // getProperties returns properties in sequence:
                // traceDirectory; maxThreads; sslMode; keepAlive; minThreads; 
                // portNumber; logConnections; timeSlice; startNetworkServer;
                // host; traceAll
                getSystemProperty("derby.system.home"),
                "0", "off", "true", "0", 
                String.valueOf(TestConfiguration.getCurrent().getPort()),
                "true", "0", "false", 
                //String.valueOf(TestConfiguration.getCurrent().getHostName()),
                "127.0.0.1", 
                "false"};     
        
        verifyProperties(expectedLogConnectionsOff);     

        String[] cmd = new String[] {
            "org.apache.derby.drda.NetworkServerControl", "logconnections", "on" };
        assertSuccessfulCmd("Log Connections changed to on.", cmd);
        verifyProperties(expectedLogConnectionsOn);     

        cmd = new String[] {
                "org.apache.derby.drda.NetworkServerControl", "logconnections", "off" };
        assertSuccessfulCmd("Log Connections changed to off.", cmd);
        // traceAll should be back to false
        verifyProperties(expectedLogConnectionsOff);    
    }

    
    /**
     *   Test other commands. These should all give a helpful error and the
     *   usage message
     *   Note: maybe these test cases should be moved to another Test,
     *   as they don't actually test any properties. 
     */
    public void testWrongCommands() 
    throws SQLException, IOException, InterruptedException
    {
        String nsc = "org.apache.derby.drda.NetworkServerControl";
        // no arguments
        String[] cmd = new String[] {nsc};
        // we'll assume that we get the full message if we get 'Usage'
        // because sometimes, the message gets returned with carriage return,
        // and sometimes it doesn't, checking for two different parts...
        assertExecJavaCmdAsExpected(new String[] 
            {"No arguments given.", "Usage: "}, cmd, 1);
        //Unknown command
        cmd = new String[] {nsc, "unknowncmd"};
        assertExecJavaCmdAsExpected(new String[] 
            {"Command unknowncmd is unknown.", "Usage: "}, cmd, 0);
        // wrong number of arguments
        cmd = new String[] {nsc, "ping", "arg1"};
        assertExecJavaCmdAsExpected(new String[] 
            {"Invalid number of arguments for command ping.",
             "Usage: "}, cmd, 1);
    }
}
