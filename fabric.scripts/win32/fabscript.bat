@echo off
rem **************************************************************************
rem (C) Copyright IBM Corp. 2014, 2016
rem
rem LICENSE: Eclipse Public License v1.0
rem http://www.eclipse.org/legal/epl-v10.html
rem **************************************************************************

SetLocal EnableDelayedExpansion

set CLASSPATH=""
for %%F in (!FABRIC_HOME!\lib\db-derby\lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%F
for %%F in (!FABRIC_HOME!\lib\fabric\*.jar) do set CLASSPATH=!CLASSPATH!;%%F
for %%F in (!FABRIC_HOME!\lib\gaiandb\lib`*.jar) do set CLASSPATH=!CLASSPATH!;%%F
for %%F in (!FABRIC_HOME!\lib\oslib\*.jar) do set CLASSPATH=!CLASSPATH!;%%F

set NODE=!COMPUTERNAME!
set GET_NODE=0
set IP=127.0.0.1
set GET_IP=0
set SCRIPT=
set GET_SCRIPT=0

:loop

	if not "%1"=="" (
	
		set _T=%1
		
		if "!_T!"=="-n" (
			set GET_NODE=1
			set GET_IP=0
			set GET_SCRIPT=0
		)
		
		if "!_T!"=="-i" (
			set GET_IP=1
			set GET_NODE=0
			set GET_SCRIPT=0
		)
		
		if "!_T!"=="-s" (
			set GET_SCRIPT=1
			set GET_IP=0
			set GET_NODE=0
		)
		
		if "!_T!"=="-help" (
			echo Usage: fabscript.bat -n node -i node-ip -s script-file
			exit /b

		)
		
		if "!GET_NODE!"=="1" (
			if not "%2"=="" (
				set _T=%2
				if not "!_T:~0,1!"=="-" (
					set NODE=!_T!
					shift
				)
			)
			set GET_NODE=0
		)
		
		if "!GET_IP!"=="1" (
			if not "%2"=="" (
				set _T=%2
				if not "!_T:~0,1!"=="-" (
					set IP=!_T!
					shift
				)
			)
			set GET_IP=
		)
		
		if "!GET_SCRIPT!"=="1" (
			if not "%2"=="" (
				set _T=%2
				if not "!_T:~0,1!"=="-" (
					set SCRIPT=!_T!
					shift
				)
			)
			set GET_SCRIPT=
		)

		shift
		goto :loop
	)
)

if "!NODE!" neq "" (
	if "!IP!" neq "" (
		if "!SCRIPT!" neq "" (
			title Fabric Script
			java -Xmx128m fabric.script.RunScripte !NODE! !IP! FABRIC-%RANDOM% !SCRIPT!
			goto:EOF
		)
	)
)

echo Usage: fabscript.bat -n node -i node-ip -s script-file
exit /b 1
