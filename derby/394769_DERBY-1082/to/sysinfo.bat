@REM ---------------------------------------------------------
@REM -- This batch file is an example of how to use sysinfo to get
@REM -- important system information
@REM --
@REM -- REQUIREMENTS:
@REM --
@REM --  This utility will report important system info about 
@REM --  jar files which are in your classpath. Jar files which are not
@REM --  if your classpath will not be reported. 
@REM --
@REM -- Check the setNetworkServerCP.bat to see an example of adding the
@REM -- the Derby jars to your classpath.
@REM -- 
@REM -- This file for use on Windows systems
@REM ---------------------------------------------------------
@echo off
rem set DERBY_INSTALL=

@if "%DERBY_HOME%"=="" set DERBY_HOME=%DERBY_INSTALL%
@if "%DERBY_HOME%"=="" goto noderbyhome

@if "%JAVA_HOME%"=="" goto nojavahome
@if not exist "%JAVA_HOME%\bin\java.exe" goto nojavahome

@if !"%CLASSPATH%"==! call "%DERBY_HOME%"/frameworks/NetworkServer/bin/setNetworkServerCP.bat
@if "%CLASSPATH%" == "" call "%DERBY_HOME%"/frameworks/NetworkServer/bin/setNetworkServerCP.bat

@REM ---------------------------------------------------------
@REM -- start sysinfo
@REM ---------------------------------------------------------
"%JAVA_HOME%\bin\java" org.apache.derby.drda.NetworkServerControl sysinfo %*
@GOTO end

@REM ---------------------------------------------------------
@REM -- To use a different JVM with a different syntax, simply edit
@REM -- this file
@REM ---------------------------------------------------------

:nojavahome
echo JAVA_HOME not set or could not find java executable in JAVA_HOME.
echo Please set JAVA_HOME to the location of a valid Java installation.
goto end

:noderbyhome
echo DERBY_HOME or DERBY_INSTALL not set. Set one of these variables
echo to the location of your Derby installation.
goto end

:end

