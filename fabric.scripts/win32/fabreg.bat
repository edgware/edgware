@ECHO OFF
rem **************************************************************************
rem Licensed Materials - Property of IBM
rem
rem (C) Copyright IBM Corp. 2012
rem
rem LICENSE: Eclipse Public License v1.0
rem http://www.eclipse.org/legal/epl-v10.html
rem **************************************************************************

REM **************************************************************************
REM Invoke the Registry command line tool.
REM **************************************************************************

SetLocal EnableDelayedExpansion

IF NOT EXIST "%FABRIC_HOME%" (
	echo ^%FABRIC_HOME^% not set
	goto :EOF
)

set CLASSPATH=%FABRIC_HOME%\lib\db-derby\lib\derbyclient.jar;%FABRIC_HOME%\lib\oslib\commons-cli-1.2.jar
for %%F in (%FABRIC_HOME%\lib\fabric\fabric.admin_*.jar) DO set CLASSPATH=!CLASSPATH!;%%F

java fabric.tools.RegistryTool %*
