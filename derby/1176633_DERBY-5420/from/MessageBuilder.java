/*

   Derby - Class org.apache.derbyBuild.MessageBuilder

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

package org.apache.derbyBuild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Echo;


/**
 * <p>
 * This tool generates the engine's message strings (message_en.properties) as well
 * the dita source for the SQLState documentation in the Derby Reference Guide.
 * </p>
 */
public class MessageBuilder extends Task
{
    /////////////////////////////////////////////////////////////////////////
    //
    //  CONSTANTS
    //
    /////////////////////////////////////////////////////////////////////////

    private static  final   String  TAB_STOP = "    ";
    
    private static  final   String  PROPERTIES_BOILERPLATE =
        "###################################################\n" +
        "#\n" +
        "# Licensed to the Apache Software Foundation (ASF) under one or more\n" +
        "# contributor license agreements.  See the NOTICE file distributed with\n" +
        "# this work for additional information regarding copyright ownership.\n" +
        "# The ASF licenses this file to You under the Apache License, Version 2.0\n" +
        "# (the \"License\"); you may not use this file except in compliance with\n" +
        "# the License.  You may obtain a copy of the License at\n" +
        "#\n" +
        "#     http://www.apache.org/licenses/LICENSE-2.0\n" +
        "#\n" +
        "# Unless required by applicable law or agreed to in writing, software\n" +
        "# distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
        "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
        "# See the License for the specific language governing permissions and\n" +
        "# limitations under the License.\n" +
        "#\n" +
        "###################################################\n" +
        "\n" +
        "###################################################\n" +
        "#\n" +
        "# DO NOT EDIT THIS FILE!\n" +
        "#\n" +
        "# Instead, edit messages.xml. The ant MessageBuilder task takes\n" +
        "# messages.xml as input and from it generates this file.\n" +
        "#\n" +
        "###################################################\n";

    private static  final   String  REF_GUIDE_BOILERPLATE =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<!DOCTYPE reference PUBLIC \"-//OASIS//DTD DITA Reference//EN\"\n" +
        " \"../dtd/reference.dtd\">\n";

    private static  final   String  REF_GUIDE_NOTES =
        "<!-- \n" +
        "Licensed to the Apache Software Foundation (ASF) under one or more\n" +
        "contributor license agreements.  See the NOTICE file distributed with\n" +
        "this work for additional information regarding copyright ownership.\n" +
        "The ASF licenses this file to You under the Apache License, Version 2.0\n" +
        "(the \"License\"); you may not use this file except in compliance with\n" +
        "the License.  You may obtain a copy of the License at      \n" +
        "\n" +
        "http://www.apache.org/licenses/LICENSE-2.0  \n" +
        "\n" +
        "Unless required by applicable law or agreed to in writing, software  \n" +
        "distributed under the License is distributed on an \"AS IS\" BASIS,  \n" +
        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  \n" +
        "See the License for the specific language governing permissions and  \n" +
        "limitations under the License.\n" +
        "-->\n" +
        "\n" +
        "<!-- \n" +
        "NOTE: this file is generated by the MessageBuilder task as part of a\n" +
        "Derby build. Please do not hand-edit this file. Instead, please edit\n" +
        "the corresponding text in messages.xml and/or MessageBuilder.\n" +
        "-->\n";
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  STATE
    //
    /////////////////////////////////////////////////////////////////////////

    private String  _xmlSourceFile;
    private String  _propertiesTargetFile;
    private String  _ditaTargetFile;

    /////////////////////////////////////////////////////////////////////////
    //
    //  INNER CLASSES
    //
    /////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * XML-wrigint wrapper around a PrintWriter.
     * </p>
     */
    public  static  final   class   XMLWriter
    {
        // If this boolean is set, then all operations are NOPs.
        private         boolean       _vacuous;
        
        private         FileWriter   _fw;
        private         PrintWriter _pw;
        private         ArrayList<String>    _tagStack;

        /**
         * <p>
         * Special constructor for making a vacuous writer which doesn't do
         * anything. This allows us to write easy-to-read dita-generating code
         * that is not cluttered with "if ( documented )" conditionals.
         * </p>
         */
        public  XMLWriter()
        {
            _vacuous = true;
         }

        /**
         * <p>
         * Create a productive writer which actually flushes text to disk.
         * </p>
         */
        public  XMLWriter( File file )
            throws IOException
        {
            _vacuous = false;
            _fw = new FileWriter( file );
            _pw = new PrintWriter( _fw );
            _tagStack = new ArrayList<String>();
        }

        public  void    flush() throws IOException
        {
            if ( _vacuous ) { return; }
            
            _pw.flush();
            _fw.flush();
        }
        
        public  void    close() throws IOException
        {
            if ( _vacuous ) { return; }

            _pw.close();
            _fw.close();
        }

        /**
         * <p>
         * Indent and write an empty tag.
         * </p>
         */
        public void    writeEmptyTag( String tag )
            throws IOException
        {
            if ( _vacuous ) { return; }

            writeEmptyTag( tag, "" );
        }

        /**
         * <p>
         * Indent and write an empty tag with attributes.
         * </p>
         */
        public void    writeEmptyTag( String tag, String attributes )
            throws IOException
        {
            if ( _vacuous ) { return; }

            indent( );
            if ( attributes.length() >0)
                _pw.println( "<" + tag + " " + attributes + "/>");
            else
                _pw.println( "<" + tag + "/>");
        }

        /**
         * <p>
         * Indent and write an opening tag.
         * </p>
         */
        public void    beginTag( String tag )
            throws IOException
        {
            if ( _vacuous ) { return; }

            beginTag( tag, "" );
        }

        /**
         * <p>
         * Indent and write an opening tag.
         * </p>
         */
        public void    beginTag( String tag, String attributes )
            throws IOException
        {
            if ( _vacuous ) { return; }

            indent();
            if (attributes.length() > 0)
                _pw.println( "<" + tag + " " + attributes + ">");
            else
                _pw.println( "<" + tag + ">");

            _tagStack.add( tag );
        }

        /**
         * <p>
         * Indent and write a closing tag.
         * </p>
         */
        public void    endTag()
            throws IOException
        {
            if ( _vacuous ) { return; }

            String  tag = (String) _tagStack.remove( _tagStack.size() -1 );
        
            indent();

            _pw.println( "</" + tag + ">");
        }

        /**
         * <p>
         * Indent and write a whole element
         * </p>
         */
        public void    writeTextElement( String tag, String text )
            throws IOException
        {
            if ( _vacuous ) { return; }

            writeTextElement( tag, "", text );
        }

        /**
         * <p>
         * Indent and write a whole element
         * </p>
         */
        public void    writeTextElement( String tag, String attributes, String text )
            throws IOException
        {
            if ( _vacuous ) { return; }

            indent();
            if ( attributes.length() > 0 )
                _pw.print( "<" + tag + " " + attributes + ">");
            else
                _pw.print( "<" + tag + ">");
            _pw.print( text );
            _pw.println( "</" + tag + ">");
        }

        /**
         * <p>
         * Indent based on the depth of our tag nesting level.
         * </p>
         */
        public void    indent()
            throws IOException
        {
            if ( _vacuous ) { return; }

            int     tabCount = _tagStack.size();

            for ( int i = 0; i < tabCount; i++ ) { _pw.write( TAB_STOP ); }
        }

                /**
         * <p>
         * Print text.
         * </p>
         */
        public void    println( String text )
            throws IOException
        {
            if ( _vacuous ) { return; }

            _pw.println( text );
        }

    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  CONSTRUCTORS
    //
    /////////////////////////////////////////////////////////////////////////

   /**
     * <p>
     * Let Ant conjure us out of thin air.
     * </p>
     */
    public MessageBuilder()
    {}
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Task BEHAVIOR
    //
    /////////////////////////////////////////////////////////////////////////

        
    /** <p>Let Ant set the input file name.</p>*/
    public void setXmlSourceFile( String fileName ) { _xmlSourceFile = fileName;}

    /** <p>Let Ant set the file name for the message property file we will write.</p>*/
    public void setPropertiesTargetFile( String fileName ) { _propertiesTargetFile = fileName;}

    /** <p>Let Ant set the file name for the SQLState dita file we will write.</p>*/
    public void setDitaTargetFile( String fileName ) { _ditaTargetFile = fileName;}
        
   /**
     * <p>
     * Read the xml message descriptors and output messages_en.properties
     * and the dita source for the SQLState table in the Derby Reference Guide.
     * After setting up arguments using the above setter methods, Ant
     * calls this method in order to run this custom task.
     * </p>
     */
    public  void    execute()
        throws BuildException
    {
        File                 source = new File( _xmlSourceFile );
        File                 targetProperties = new File( _propertiesTargetFile );
        File                 targetDita = new File( _ditaTargetFile );
        FileWriter      propertiesFW = null;
        PrintWriter    propertiesPW = null;
        XMLWriter    ditaWriter = null;

        try {
            propertiesFW = new FileWriter( targetProperties );
            propertiesPW = new PrintWriter( propertiesFW );
            ditaWriter = new XMLWriter( targetDita );

            processMessages( source, propertiesPW, ditaWriter );
        }
        catch (Exception e)
        {
            throw new BuildException( "Could not generate English properties from message descriptors.", e );
        }
        finally
        {
            try {
                finishWriting( propertiesFW, propertiesPW );

                if ( ditaWriter != null )
                {
                    ditaWriter.flush();
                    ditaWriter.close();
                }
            }
            catch (Exception ex)
            {
                throw new BuildException( "Error closing file writers.", ex );
            }
        }
        
    }

    /////////////////////////////////////////////////////////////////////////
    //
    //  MINIONS TO PROCESS MESSAGE DESCRIPTORS
    //
    /////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Loop through descriptors and write appropriate output to the properties
     * and dita files.
     * </p>
     */
    private void    processMessages( File input, PrintWriter propertiesPW, XMLWriter ditaWriter )
        throws Exception
    {
        DocumentBuilderFactory  factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder              builder = factory.newDocumentBuilder();
        Document                          doc = builder.parse( input );
        Element                             root = doc.getDocumentElement();    // framing "messages" element
        NodeList                            sections = root.getElementsByTagName( "section" );             

        propertiesPW.println( PROPERTIES_BOILERPLATE );
        ditaWriter.println( REF_GUIDE_BOILERPLATE );

        ditaWriter.beginTag( "reference", "id=\"rrefexcept71493\" xml:lang=\"en-us\""  );
        {
            ditaWriter.println( REF_GUIDE_NOTES );
            ditaWriter.writeTextElement( "title", "SQL error messages and exceptions" );

            ditaWriter.beginTag( "refbody" );
            {
                ditaWriter.beginTag( "section" );
                {
                    ditaWriter.writeTextElement
                        (
                         "p",
                         "The following tables list <i>SQLStates</i> for exceptions. Exceptions " +
                         "that begin with an <i>X</i> are specific to <ph conref=\"../conrefs.dita#prod/productshortname\"></ph>."
                         );
                }
                ditaWriter.endTag();
                
                ditaWriter.beginTag( "section" );
                {
                    processSections( propertiesPW, ditaWriter, sections );
                }
                ditaWriter.endTag();
                
            }
            ditaWriter.endTag();
        }
        ditaWriter.endTag();
    }

    /**
     * <p>
     * Loop through sections in the message descriptor file..
     * </p>
     */
    private void    processSections( PrintWriter propertiesPW, XMLWriter ditaWriter, NodeList nodes )
        throws Exception
    {
        int     nodeCount = nodes.getLength();
        
        for ( int i = 0; i < nodeCount; i++ )
        {
            Element     node = (Element) nodes.item( i );
            
            processSection( propertiesPW, ditaWriter, node );
        }
    }

    /**
     * <p>
     * Read a section from the message descriptor file.
     * </p>
     */
    private void    processSection( PrintWriter propertiesPW, XMLWriter ditaWriter, Element section )
        throws Exception
    {
        boolean     documented = ( getFirstChild( section, "documented" ) != null );
        NodeList   families = section.getElementsByTagName( "family" );
        int             familyCount = families.getLength();

        //
        // If we don't need to document this section, then we use a vacuous XMLWriter
        // which NOPs all writes.
        //
        if ( !documented ) { ditaWriter = new XMLWriter(); }

        for ( int i = 0; i < familyCount; i++ )
        {
            Element     family = (Element) families.item( i );
            
            processFamily( propertiesPW, ditaWriter, family );
        }
    }
    
    /**
     * <p>
     * Read a family of message descriptors
     * </p>
     */
    private void    processFamily( PrintWriter propertiesPW, XMLWriter ditaWriter, Element family )
        throws Exception
    {
        String        title = squeezeText( getFirstChild( family, "title" ) );
        NodeList   messages = family.getElementsByTagName( "msg" );
        int              messageCount = messages.getLength();

        ditaWriter.beginTag( "table" );
        {
            ditaWriter.writeTextElement( "title", title );
            
            ditaWriter.beginTag( "tgroup", "cols=\"2\"" );
            {
                ditaWriter.writeEmptyTag( "colspec", "colname=\"col1\" colnum=\"1\" colwidth=\"1*\"" );
                ditaWriter.writeEmptyTag( "colspec", "colname=\"col2\" colnum=\"2\" colwidth=\"7.5*\"" );

                ditaWriter.beginTag( "thead" );
                {
                    ditaWriter.beginTag( "row", "valign=\"bottom\"" );
                    {
                        ditaWriter.writeTextElement( "entry", "colname=\"col1\"", "SQLSTATE" );
                        ditaWriter.writeTextElement( "entry", "colname=\"col2\"", "Message Text" );
                    }
                    ditaWriter.endTag();
                }
                ditaWriter.endTag();

                ditaWriter.beginTag( "tbody" );
                {
                    for ( int i = 0; i < messageCount; i++ )
                    {
                        Element     message = (Element) messages.item( i );
                        
                        processMessage( propertiesPW, ditaWriter, message );
                    }
                }
                ditaWriter.endTag();
            }
            ditaWriter.endTag();
        }
        ditaWriter.endTag();
    }
    
    /**
     * <p>
     * Read and process a message.
     * </p>
     */
    private void    processMessage( PrintWriter propertiesPW, XMLWriter ditaWriter, Element message )
        throws Exception
    {
        String        name = squeezeText( getFirstChild( message, "name" ) );
        String        sqlstate = getSQLState( name );
        String        rawText = squeezeText( getFirstChild( message, "text" ) );
        String        propertyText = escapePropertiesText( rawText );
        int             parameterCount = countParameters( rawText );
        String[]     args = getArgs( message );

        if ( parameterCount != args.length )
        {
            throw new Exception( name + " has " + parameterCount + " parameters but " + args.length + " nested args." );
        }

        String displayText;
        if (rawText.indexOf('\'')>=0)
        {
            displayText = replaceSpecialChars( escapeTextWithAQuote( rawText ) );
            displayText = plugInArgs( displayText , args );

        }
        else
        {
            displayText = plugInArgs( replaceSpecialChars( rawText), args ) ;
        }

        ditaWriter.beginTag( "row" );
        {
            ditaWriter.writeTextElement( "entry", "colname=\"col1\"", sqlstate );
            ditaWriter.writeTextElement( "entry", "colname=\"col2\"", displayText );
        }
        ditaWriter.endTag();
        
        propertiesPW.println( name + "=" + propertyText );
    }

    /**
     * <p>
     * Convert a message handle into a SQLState, stripping off trailing
     * encodings as necessary.
     * </p>
     */
    private String  getSQLState( String name )
    {
        if ( name.length() <= 5 ) { return name; }
        else { return name.substring( 0, 5 ); }
    }

    /**
     * <p>
     * Get all of the human-readable parameter names out of the message element.
     * </p>
     */
    private String[]    getArgs( Element message )
        throws Exception
    {
        NodeList   args = message.getElementsByTagName( "arg" );
        int             argCount = args.getLength();
        String[]    retval = new String[ argCount ];

        for ( int i = 0; i < argCount; i++ )
        {
            Element     arg = (Element) args.item( i );
            
            retval[ i ] = squeezeText( arg );
        }

        return retval;
    }

    /**
     * <p>
     * Count the substitutable arguments in an internationalized message string.
     * These arguments have the form {n} where n is a number.
     * </p>
     */
    private int countParameters( String text )
    {
        int     argCount = 0;
        int     argIdx = 0;

        while( true )
        {
            argIdx = text.indexOf( '{', argIdx );

            if ( argIdx >= 0 )
            {
                argCount++;
                argIdx++;
            }
            else { break; }
        }

        return argCount;
    }

    /**
     * <p>
     * Plug arg values into parameter slots in an internationalizable message
     * string.
     * </p>
     */
    private String  plugInArgs( String message, String[] rawArgs )
    {
        int             count = rawArgs.length;
        String[]    cookedArgs = new String[ count ];

        // add xml angle brackets around the args
        for ( int i = 0; i < count; i++ )
        {
            cookedArgs[ i ] = "<varname>&lt;" + rawArgs[ i ] + "&gt;</varname>";
        }

        return MessageFormat.format( message, cookedArgs );
    }

    /////////////////////////////////////////////////////////////////////////
    //
    //  GENERALLY USEFUL MINIONS
    //
    /////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Echo a message to the console.
     * </p>
     */
    private void    echo( String text )
    {
        log( text, Project.MSG_WARN );
    }

    /**
     * <p>
     * Flush and close file writers.
     * </p>
     */
    private void    finishWriting( FileWriter fw, PrintWriter pw )
        throws IOException
    {
        if ( (fw == null) || (pw == null) ) { return; }
        
        pw.flush();
        fw.flush();

        pw.close();
        fw.close();
    }

    ////////////////////////////////////////////////////////
    //
    // XML MINIONS
    //
    ////////////////////////////////////////////////////////

    private Element getFirstChild( Element node, String childName )
        throws Exception
    {
        return (Element) node.getElementsByTagName( childName ).item( 0 );
    }

    /**
     * <p>
     * Squeeze the text out of an Element.
     * </p>
     */
    private String squeezeText( Element node )
        throws Exception
    {
        Node        textChild = node.getFirstChild();
        String      text = textChild.getNodeValue();

        return text;
    }

    /**
     * Replace a substring with some equivalent. For example, we would
     * like to replace "<" with "&lt;" in the error messages.
     * Add any substrings you would like to replace in the code below.
     * Be aware that the first paramter to the replaceAll() method is
     * interpreted as a regular expression.
     *
     * @param input 
     *      A String that may contain substrings that we want to replace
     * @return 
     *      Output String where substrings selected for replacement have been
     *      replaced.
     * @see java.util.regex.Pattern
     */
    private static String replaceSpecialChars(java.lang.String input) {
        String output = input.replaceAll("<", "&lt;");
        output = output.replaceAll(">", "&gt;");
        
        return output;
    }


    /**
     * <p>
     * Replace newlines with the escape sequence needed by properties files.
     * Also, replace single quotes with two single quotes.
     * </p>
     */
    private static String escapePropertiesText( java.lang.String input )
    {
        String output = input.replaceAll( "\n", "\\\\n" );

        output = output.replaceAll( "\'", "\'\'" );
        
        return output;
    }

    /**
     * <p>
     * Replace single quotes with two single quotes.
     * Only needed when there are parameters with quotes.
     * </p>
     */
    private static String escapeTextWithAQuote( java.lang.String input )
    {
        String output = input.replaceAll( "\'", "\'\'" );
        
        return output;
    }



}

