@echo off
rem ============================================================================
rem  GaianDB
rem  Copyright IBM Corp. 2010
rem  
rem  LICENSE: Eclipse Public License v1.0
rem  http://www.eclipse.org/legal/epl-v10.html
rem ============================================================================

TITLE GaianDB CLP

if not defined GDBH set GDBH=..
set GDBL=%GDBH%\lib

SET CLASSPATH="%GDBL%\;%GDBL%\GAIANDB.jar;%GDBL%\db2jcutdown.jar;%GDBL%\derby.jar;%GDBL%\derbyclient.jar;%GDBL%\derbytrimmed.jar"

SET CLASSPATH="%CLASSPATH%;C:\Progra~1\IBM\SQLLIB\java\db2jcc.jar;C:\Progra~1\IBM\SQLLIB\java\db2jcc_license_cu.jar"
SET CLASSPATH="%CLASSPATH%;C:\APPS\db282\java\db2jcc.jar;C:\APPS\db282\java\db2jcc_license_cu.jar"

SET ARGS=%*

java -version
java -cp "%CLASSPATH%" com.ibm.gaiandb.tools.SQLUDPRunner %ARGS%
