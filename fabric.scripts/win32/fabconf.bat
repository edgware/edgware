@ECHO OFF
rem **************************************************************************
rem (C) Copyright IBM Corp. 2012, 2014
rem
rem LICENSE: Eclipse Public License v1.0
rem http://www.eclipse.org/legal/epl-v10.html
rem **************************************************************************

REM **************************************************************************
REM Invokes the Fabric configuration tool.
REM **************************************************************************

SetLocal EnableDelayedExpansion

if not exist "!FABRIC_HOME!" (
	echo Environment variable FABRIC_HOME not set
	goto :EOF
)

REM Remove any spaces from the FABRIC_HOME path
for %%H in ("!FABRIC_HOME!") do set FABRIC_HOME=%%~sH

set CLASSPATH=!FABRIC_HOME!\lib\db-derby\lib\derbyclient.jar;!FABRIC_HOME!\lib\oslib\commons-cli-1.2.jar
for %%F in (!FABRIC_HOME!\lib\fabric\fabric.admin_*.jar) DO set CLASSPATH=!CLASSPATH!;%%F

java fabric.tools.FabricConfig %*
