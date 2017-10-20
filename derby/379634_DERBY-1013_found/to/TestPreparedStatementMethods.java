/*
 
   Derby - Class org.apache.derbyTesting.functionTests.tests.jdbc4.TestPreparedStatementMethods
 
   Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 
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

package org.apache.derbyTesting.functionTests.tests.jdbc4;

import java.io.Reader;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.NClob;
import java.sql.SQLXML;
import org.apache.derby.shared.common.reference.SQLState;


import org.apache.derby.tools.ij;

public class TestPreparedStatementMethods {
    
    Connection conn=null;
    PreparedStatement ps=null;
    
    void t_setRowId() {
        try {
            ps.setRowId(0,null);
            System.out.println("UnImplemented Exception not thrown in code");
        } catch(SQLException e) {
            if(SQLState.NOT_IMPLEMENTED.equals (e.getSQLState())) {
                System.out.println("Unexpected SQLException"+e);
            }
        } catch(Exception e) {
            System.out.println("Unexpected exception thrown in method"+e);
            e.printStackTrace();
        }
    }
    void t_setNString() {
        try {
            ps.setNString(0,null);
            System.out.println("UnImplemented Exception not thrown in code");
        } catch(SQLException e) {
            if(SQLState.NOT_IMPLEMENTED.equals (e.getSQLState())) {
                System.out.println("Unexpected SQLException"+e);
            }
            
        } catch(Exception e) {
            System.out.println("Unexpected exception thrown in method"+e);
            e.printStackTrace();
        }
    }
    void t_setNCharacterStream() {
        try {
            ps.setNCharacterStream(0,null,0);
            System.out.println("UnImplemented Exception not thrown in code");
        } catch(SQLException e) {
            if(SQLState.NOT_IMPLEMENTED.equals (e.getSQLState())) {
                System.out.println("Unexpected SQLException"+e);
            }
            
        } catch(Exception e) {
            System.out.println("Unexpected exception thrown in method"+e);
            e.printStackTrace();
        }
    }
    void t_setNClob1() {
        try {
            ps.setNClob(0,null);
            System.out.println("UnImplemented Exception not thrown in code");
        } catch(SQLException e) {
            if(SQLState.NOT_IMPLEMENTED.equals (e.getSQLState())) {
                System.out.println("Unexpected SQLException"+e);
            }
            
        } catch(Exception e) {
            System.out.println("Unexpected exception thrown in method"+e);
            e.printStackTrace();
        }
    }
    void t_setClob() {
        try {
            ps.setClob(0,null,0);
            System.out.println("UnImplemented Exception not thrown in code");
        } catch(SQLException e) {
            if(SQLState.NOT_IMPLEMENTED.equals (e.getSQLState())) {
                System.out.println("Unexpected SQLException"+e);
            }
            
        } catch(Exception e) {
            System.out.println("Unexpected exception thrown in method"+e);
            e.printStackTrace();
        }
    }
    void t_setBlob() {
        try {
            ps.setBlob(0,null,0);
            System.out.println("UnImplemented Exception not thrown in code");
        } catch(SQLException e) {
            if(SQLState.NOT_IMPLEMENTED.equals (e.getSQLState())) {
                System.out.println("Unexpected SQLException"+e);
            }
            
        } catch(Exception e) {
            System.out.println("Unexpected exception thrown in method"+e);
            e.printStackTrace();
        }
    }
    void t_setNClob2() {
        try {
            ps.setNClob(0,null,0);
            System.out.println("UnImplemented Exception not thrown in code");
        } catch(SQLException e) {
            if(SQLState.NOT_IMPLEMENTED.equals (e.getSQLState())) {
                System.out.println("Unexpected SQLException"+e);
            }
            
        } catch(Exception e) {
            System.out.println("Unexpected exception thrown in method"+e);
            e.printStackTrace();
        }
    }
    void t_setSQLXML() {
        try {
            ps.setSQLXML(0,null);
            System.out.println("UnImplemented Exception not thrown in code");
        } catch(SQLException e) {
            if(SQLState.NOT_IMPLEMENTED.equals (e.getSQLState())) {
                System.out.println("Unexpected SQLException"+e);
            }
            
        } catch(Exception e) {
            System.out.println("Unexpected exception thrown in method"+e);
            e.printStackTrace();
        }
    }
    void t_setPoolable() {
        try {
            ps.setPoolable(false);
            System.out.println("UnImplemented Exception not thrown in code");
        } catch(SQLException e) {
            if(SQLState.NOT_IMPLEMENTED.equals (e.getSQLState())) {
                System.out.println("Unexpected SQLException"+e);
            }
            
        } catch(Exception e) {
            System.out.println("Unexpected exception thrown in method"+e);
            e.printStackTrace();
        }
    }
    void t_isPoolable() {
        try {
            boolean b;
            b = ps.isPoolable();
            System.out.println("UnImplemented Exception not thrown in code");
        } catch(SQLException e) {
            if(SQLState.NOT_IMPLEMENTED.equals (e.getSQLState())) {
                System.out.println("Unexpected SQLException"+e);
            }
            
        } catch(Exception e) {
            System.out.println("Unexpected exception thrown in method"+e);
            e.printStackTrace();
        }
    }
    
    
    void startPreparedStatementMethodTest(Connection conn_in,PreparedStatement ps_in) {
        conn = conn_in;
        ps = ps_in;
        t_setRowId();
        t_setNString();
        t_setNCharacterStream();
        t_setNClob1();
        t_setClob();
        t_setBlob();
        t_setNClob2();
        t_setSQLXML();
        t_setPoolable();
        t_isPoolable();
    }
    
    public static void main(String args[]) {
        TestConnection tc=null;
        Connection conn_main=null;
        PreparedStatement ps_main=null;
        
        try {
            tc = new TestConnection();
            conn_main = tc.createEmbeddedConnection();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        try {
            ps_main = conn_main.prepareStatement("select count(*) from sys.systables");
        } catch(SQLException e) {
            System.out.println(""+e);
            e.printStackTrace();
        }
        
        TestPreparedStatementMethods tpsm = new TestPreparedStatementMethods();
        tpsm.startPreparedStatementMethodTest(conn_main,ps_main);
        
        conn_main=null;
        ps_main=null;
        
        try {
            conn_main = tc.createClientConnection();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        try {
            ps_main = conn_main.prepareStatement("select count(*) from sys.systables");
        } catch(SQLException e) {
            System.out.println(""+e);
            e.printStackTrace();
        }
        
        tpsm.startPreparedStatementMethodTest(conn_main,ps_main);
    }
}
