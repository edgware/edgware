@echo off
rem **************************************************************************
rem (C) Copyright IBM Corp. 2012, 2014
rem
rem LICENSE: Eclipse Public License v1.0
rem http://www.eclipse.org/legal/epl-v10.html
rem **************************************************************************

SetLocal EnableDelayedExpansion

set CLASSPATH=""
for %%F in (%FABRIC_HOME%\lib\plugins\*.jar) do set CLASSPATH=!CLASSPATH!;%%F
for %%F in (%FABRIC_HOME%\lib\db-derby\lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%F
for %%F in (%FABRIC_HOME%\lib\fabric\*.jar) do set CLASSPATH=!CLASSPATH!;%%F
for %%F in (%FABRIC_HOME%\lib\oslib\*.jar) do set CLASSPATH=!CLASSPATH!;%%F
   

java -Dconfig=%FABRIC_HOME%\bin\config\CLI.properties -Dfabric.config=%FABRIC_HOME%\osgi\configuration\fabricConfig_default.properties -Djava.util.logging.config.file=%FABRIC_HOME%\osgi\configuration\logging.properties -cp %CLASSPATH% fabric.tools.DistributedRegistryCLI %1

