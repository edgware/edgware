@echo off
rem **************************************************************************
rem Licensed Materials - Property of IBM
rem
rem (C) Copyright IBM Corp. 2006, 2014
rem
rem LICENSE: Eclipse Public License v1.0
rem http://www.eclipse.org/legal/epl-v10.html
rem **************************************************************************

set FABRIC_DEBUG=false

SetLocal EnableDelayedExpansion
goto :main

:usage
    echo Usage: fabinstall [registry^|noregistry] [gaian^|distributed]
    exit /b 1

:configRegistryJars

	for %%F in ("!FABRIC_HOME!\lib\fabric\fabric.registry.monitor*.jar") do set monitor_jar=%%F
	for %%F in ("!FABRIC_HOME!\lib\fabric\fabric.lib*.jar") do set lib_jar=%%F
	for %%F in ("!FABRIC_HOME!\lib\fabric\fabric.core*.jar") do set core_jar=%%F
	for %%F in ("!FABRIC_HOME!\lib\oslib\mqtt-client-*.jar") do set mqtt_jar=%%F
	for %%F in ("!FABRIC_HOME!\lib\db-derby\lib\derbyclient*.jar") do set derby_client_jar=%%F

	if exist "!FABRIC_HOME!\lib\sql\80_REGMON_JARS_ALL_LOAD.sql" (
		del "!FABRIC_HOME!\lib\sql\80_REGMON_JARS_ALL_LOAD.sql"
	)
	for /f "usebackq tokens=* " %%l in ("!FABRIC_HOME!\lib\sql\80_REGMON_JARS_ALL_LOAD.template") do call :expand %%l >> "!FABRIC_HOME!\lib\sql\80_REGMON_JARS_ALL_LOAD.sql"

	goto:EOF
	
:expand
    @set line=%*
    @set line=%line:@@MONITOR_JAR@@=!monitor_jar!%
    @set line=%line:@@LIB_JAR@@=!lib_jar!%
    @set line=%line:@@CORE_JAR@@=!core_jar!%
    @set line=%line:@@MQTT_JAR@@=!mqtt_jar!%
    @set line=%line:@@DERBY_CLIENT_JAR@@=!derby_client_jar!%
    @echo %line%
    @goto:eof

rem **************************************************************************
rem Install and configure the Fabric ready for use
rem **************************************************************************

:main

rem - Set FABRIC_HOME to the location of the fabinstall script
set FABRIC_HOME=%~d0%~p0
set PATH=%PATH%;%FABRIC_HOME%\bin\win32

rem - Confirm acceptance of the license agreement

rem cmd /c license
rem set LICENSE_ACCEPTED=%ERRORLEVEL%
rem
rem if not "%LICENSE_ACCEPTED%" == "0" (
rem     echo Installation terminated.
rem     exit /b 1
rem )

echo USE OF THE CONTENT IS GOVERNED BY THE TERMS AND CONDITIONS OF THE INCLUDED
echo LICENSE AGREEMENT AND/OR THE TERMS AND CONDITIONS OF LICENSE AGREEMENTS OR
echo NOTICES INDICATED IN THE INCLUDED NOTICES FILE.

rem - Determine installation type

if "%1" NEQ "" (
	if "%1"=="registry" (
		set INSTALL=registry
	)
	if "%1"=="noregistry" (
		set INSTALL=noregistry
	)
	if "!INSTALL!"=="" (
		call:usage
	)
) else (
	set INSTALL=registry
)

if "%2" NEQ "" (
	if "%2"=="distributed" (
		set DBTYPE=distributed
	)
	if "%2"=="gaian" (
		set DBTYPE=gaian
	)
	if "!DBTYPE!"=="" (
		call:usage
	)
) else (
	set DBTYPE=distributed
)

echo.
echo Installing !DBTYPE! !INSTALL!.
echo Log files will be written to %FABRIC_HOME%\log.

if not exist "%FABRIC_HOME%\log" mkdir "%FABRIC_HOME%\log"

if "!INSTALL!"=="registry" (

	echo Initialising the Registry.

	set FABRIC_TRIGGERS=false

	rem - Configure the SQL to install Fabric JARs into Derby
	call:configRegistryJars
	
	rem - Initialise the Registry
	cmd /c fabadmin.bat --registry -clean -!DBTYPE! >"%FABRIC_HOME%\log\fabregistry_reset.log" 2>&1
	
	if !ERRORLEVEL!==0 (
	    echo Registry initialised.
	) else (
		echo Failed to initialise the Registry. Check %FABRIC_HOME%\log for details.
		exit /b 1
	)
)

echo.
echo Installation complete, now please:
echo 1. Set the following environment variables:
echo         FABRIC_HOME=%FABRIC_HOME%
echo         PATH=%%PATH%%;%%FABRIC_HOME%%\bin\win32
echo 2. Start the Registry using the command:
echo         fabadmin -s -!DBTYPE! -r ^<node-name^>
echo    Where:
echo         ^<node-name^> is the name of the Fabric node that will run on this system
echo 3. Display the list of available network adapters on this system using the command:
echo         fabadmin -i
echo 4. Configure the Fabric node to use one or more of the available network adapters
echo    displayed in step 3 (this only needs to be done once) using the command:
echo         fabreg -sn ^<node-name^> fabric.node.interfaces ^<network-adapter-list^>
echo    Where:
echo         ^<network-adapter-list^> is a comma separated list ^(no spaces^) of network
echo         adapters e.g. "en0,en5"
echo    The Fabric node will discover and connect to neighbours using these adapters.
echo 5. Ensure that the Mosquitto broker is running with a configuration matching:
echo         %FABRIC_HOME%\etc\broker.conf
echo 6. You can now start the Fabric node using the command:
echo         fabadmin -s -n ^<node-name^>
