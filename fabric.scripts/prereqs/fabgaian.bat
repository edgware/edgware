@echo off 

rem **************************************************************************
rem Licensed Materials - Property of IBM
rem
rem (C) Copyright IBM Corp. 2014
rem
rem LICENSE: Eclipse Public License v1.0
rem http://www.eclipse.org/legal/epl-v10.html
rem **************************************************************************

SetLocal EnableDelayedExpansion
goto :main

rem **************************************************************************
rem Function declarations
rem **************************************************************************

:configProperties

	set props_file=!FABRIC_HOME!\osgi\configuration\fabricConfig_default.properties
	copy "!props_file!" "!props_file!_src"
	for /f "usebackq tokens=* " %%l in ("!props_file!_src") do call :replace %%l >> "!props_file!"
	del "!props_file!_src"

	goto:EOF
	
:replace
    @set line=%*
    @set line=%line:registry.type=distributed=registry.type=gaian%
    @echo %line%
    @goto:eof

rem **************************************************************************
rem Check that the pre-reqs are available
rem **************************************************************************

:main

rem Set FABRIC_HOME based upon the location of the script (which should be
rem <fabric-home>\prereqs)
set FABRIC_HOME=%~d0%~p0\..
rem Remove any spaces from the FABRIC_HOME path
for %%H in ("!FABRIC_HOME!") do set FABRIC_HOME=%%~sH
cd !FABRIC_HOME!

set missing_prereqs=0

if not exist "prereqs\GAIANDB-1.5.zip" (
	echo GAIAN-1.5.zip does not exist
	set missing_prereqs=1
)

if %missing_prereqs% EQU 1 (
	exit /b 1
)

rem **************************************************************************
rem Unpack and deploy packages
rem **************************************************************************

if not exist "%ZIP%" (
	echo Environment variable ZIP not set
	goto:EOF
)

echo Unpacking and deploying the Gaian Database.
"%ZIP%" x "prereqs\GAIANDB-1.5.zip" -o"prereqs\gaian_temp\gaiandb\" -y
xcopy prereqs\gaian_temp\gaiandb lib\gaiandb /s /e /h /y /q
rmdir "prereqs\gaian_temp\" /s /q

rem **************************************************************************
rem Ensure that the Gaian Database is the default
rem **************************************************************************

call:configProperties

rem **************************************************************************
rem Now ready to install
rem **************************************************************************

echo To install the Fabric now run the following command from the Fabric root
echo directory ^(%CD%^):
echo         fabinstall.bat registry gaian
