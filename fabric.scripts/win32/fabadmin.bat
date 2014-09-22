@echo off
rem **************************************************************************
rem Licensed Materials - Property of IBM
rem
rem (C) Copyright IBM Corp. 2012, 2014
rem
rem LICENSE: Eclipse Public License v1.0
rem http://www.eclipse.org/legal/epl-v10.html
rem **************************************************************************

SetLocal EnableDelayedExpansion

rem - Variable used for function return values
set retval=""

set FABRIC_DEBUG=false

goto :main

rem **************************************************************************
rem Function declarations
rem **************************************************************************

:debug

	if not "!FABRIC_DEBUG!"=="true" goto:EOF
	echo %*
	goto:EOF

:startRegistry

	call:debug [startRegistry] enter: %*
	set retval=
	
	set REGISTRY_HOME=!FABRIC_HOME!\db\REGISTRY
	set REGISTRY_LOG_FILE=!FABRIC_HOME!\log\registry.log
   
	if not exist !REGISTRY_HOME! (
		mkdir !REGISTRY_HOME!
		copy !REGISTRY_HOME!\..\gaiandb_config_fabric.properties.master !REGISTRY_HOME!\gaiandb_config_fabric.properties
	)
	
	if not exist !REGISTRY_HOME!\logging.properties (
		copy !REGISTRY_HOME!\..\logging.properties !REGISTRY_HOME!\logging.properties
	)
	
	if not exist !REGISTRY_HOME!\derby.properties (
		copy !REGISTRY_HOME!\..\derby.properties !REGISTRY_HOME!\derby.properties
	)
	
	echo - Starting the Registry

	set CLASSPATH=""
	for %%F in (!FABRIC_HOME!\lib\plugins\*.jar) do set CLASSPATH=!CLASSPATH!;%%F
	for %%F in (!FABRIC_HOME!\lib\fabric\*.jar) do set CLASSPATH=!CLASSPATH!;%%F
	for %%F in (!FABRIC_HOME!\lib\oslib\*.jar) do set CLASSPATH=!CLASSPATH!;%%F
   
	cd !REGISTRY_HOME!

 	if "!DBTYPE!"=="gaian" (
 	
    	set REGISTRY_PID_FILE=!FABRIC_HOME!\pid\.registry.gaian
 		if not exist !REGISTRY_HOME!\gaiandb_config_fabric.properties (
			copy !REGISTRY_HOME!\..\gaiandb_config_fabric.properties.master !REGISTRY_HOME!\gaiandb_config_fabric.properties
		)
 	
		for %%F in (!FABRIC_HOME!\lib\gaiandb\lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%F
		
		echo. >!REGISTRY_PID_FILE! 2>&1
		
		if "!DAEMON!"=="0" (
			call:debug [startRegistry] starting Gaian DB in the foreground
			title Fabric Registry 
			java -Dfabric.node=!NODE! -Dfabric.feeds.onramp=!COMMAND_TOPIC! -Djava.util.logging.config.file=!REGISTRY_HOME!\logging.properties -Xmx128m com.ibm.gaiandb.GaianNode -c .\gaiandb_config_fabric.properties
			title !comspec!
		) else (
			call:debug [startRegistry] starting Gaian DB in the background
			echo   ^(background^)
			start "Fabric Registry : gaian" /MIN java -Dfabric.node=!NODE! -Dfabric.feeds.onramp=!COMMAND_TOPIC! -Djava.util.logging.config.file=!REGISTRY_HOME!\logging.properties -Xmx128m com.ibm.gaiandb.GaianNode -c .\gaiandb_config_fabric.properties
			call:waitForRegistry
		)
	) else (
    	set REGISTRY_PID_FILE=!FABRIC_HOME!\pid\.registry.distributed
		rem - Start Derby
		rem	set DERBY_HOME=!FABRIC_HOME!\db\REGISTRY

		for %%F in (!FABRIC_HOME!\lib\db-derby\lib\derby*.jar) do set CLASSPATH=!CLASSPATH!;%%F

		if "!DAEMON!"=="0" (
			call:debug [startRegistry] starting Derby in the foreground
			echo. >!REGISTRY_PID_FILE! 2>&1
			java -Dfabric.node=!NODE! -Dfabric.feeds.onramp=!COMMAND_TOPIC! -jar !FABRIC_HOME!\lib\db-derby\lib\derbyrun.jar server start -p 6414 -noSecurityManager
		) else (
			call:debug [startRegistry] starting Derby in the background
			echo. >!REGISTRY_PID_FILE! 2>&1
			echo   ^(background^)
			start "Fabric Registry : distributed" /MIN java -Dfabric.node=!NODE! -Dfabric.feeds.onramp=!COMMAND_TOPIC! -jar !FABRIC_HOME!\lib\db-derby\lib\derbyrun.jar server start -p 6414 -noSecurityManager >!REGISTRY_LOG_FILE! 2>&1
			call:waitForRegistry
		)
	)
	
	call:debug [startRegistry] exit: !retval!
	goto:EOF

:waitForRegistry

	call:debug [waitForRegistry] enter: %*
	set retval=

	echo - Waiting for Registry to start

	:regStartLoop

	timeout /T 1 >NUL
	call:isRegistryRunning
	if "!retval!"=="no" goto :regStartLoop
      
	set retval=0
	call:debug [waitForRegistry] exit: !retval!
	goto:EOF

:isRegistryRunning

	call:debug [isRegistryRunning] enter: %*
	
	cmd /C fabadmin -p --registry -testConnection -!DBTYPE!
	
	if !ERRORLEVEL!==0 (
		set isRunning=yes
		call:debug [isRegistryRunning] Registry running
	) else (
		set isRunning=no
		call:debug [isRegistryRunning] Registry stopped
	)
	
	set retval=!isRunning!
	call:debug [isRegistryRunning] exit: !retval!
	goto:EOF

:stopRegistry

	call:debug [stopRegistry] enter: %*
	set retval=

	call:isRegistryRunning
	
	if "!retval!"=="yes" (
	
		echo - Stopping the Registry
		
 		if "!DBTYPE!"=="gaian" (
 		
 			rem Gaian 2.0+
 			rem call:runLauncher --registry --stop
 			
 			taskkill /fi "WINDOWTITLE eq Fabric Registry*"

 		) else (
 		
			set DERBY_HOME=!FABRIC_HOME!\lib\db-derby
			cmd /c !DERBY_HOME!\bin\stopNetworkServer.bat -user fabric -password fabric -p 6414
			
		)
		
		call:isRegistryRunning
		
		if "!retval!"=="no" (
			echo - Registry stopped
			set registryRunning=0
		) else (
			set registryRunning=1
		)
	) else (
		set registryRunning=0
	)
	
	set retval=!registryRunning!
	call:debug [stopRegistry] exit: !retval!
	goto:EOF

:startNode

	call:debug [startNode] enter: %*
	set retval=

	echo - Starting node

	set NODE_HOME=!FABRIC_HOME!\osgi
	set NODE_CONFIG_DIR=!NODE_HOME!\configuration
	set NODE_LOGGING_FILE=!NODE_CONFIG_DIR!\logging_!NODE!.properties
	
	if not exist !NODE_LOGGING_FILE! (
		set NODE_LOGGING_FILE=!NODE_CONFIG_DIR!\logging.properties
	)
	
	set NODE_CONFIG_FILE=!NODE_CONFIG_DIR!\fabricConfig_!NODE!.properties
	if not exist !NODE_CONFIG_FILE! (
		set NODE_CONFIG_FILE=!NODE_CONFIG_DIR!\fabricConfig_default.properties
		if not exist !NODE_CONFIG_FILE! (
			echo Cannot find configuration file: !NODE_CONFIG_FILE!
			set retval=1
			goto:EOF
		)
	)
	
	cd !NODE_HOME!
	
	for %%F in (org.eclipse.osgi_*.jar) do set OSGI_JAR=%%F
	
	if "!DAEMON!"=="0" (
		title Fabric Manager : !NODE!
		java -Dfabric.config=!NODE_CONFIG_FILE! -Djava.util.logging.config.file=!NODE_LOGGING_FILE! -Dfabric.node=!NODE! -jar !OSGI_JAR! !CONSOLE!
		set retval=!ERRORLEVEL!
		title !comspec!
	) else (
		start "Fabric Manager : !NODE!" /MIN java -Dfabric.config=!NODE_CONFIG_FILE! -Djava.util.logging.config.file=!NODE_LOGGING_FILE! -Dfabric.node=!NODE! -jar !OSGI_JAR!
		set retval=0
	)

	call:debug [startNode] exit: !retval!
	goto:EOF

	
:startWeb

	call:debug [startWeb] enter: %*
	set JETTY_PORT=8080
	set NODE_CONFIG_FILE=!NODE_CONFIG_DIR!\fabricConfig_default.properties
		if not exist !NODE_LOGGING_FILE! (
		set NODE_LOGGING_FILE=!NODE_CONFIG_DIR!\logging.properties
	)
	set retval=
	
	echo - Starting Web server
	set WEBSERVER_PID_FILE=!FABRIC_HOME!\pid\.web.!JETTY_PORT!
	
	echo. >!WEBSERVER_PID_FILE! 2>&1
	
	cd !FABRIC_HOME!\web
	
	if "!DAEMON!"=="0" (
		title Fabric WebServer : !JETTY_PORT!
		cmd /C java -DSTOP.PORT=!PORT! -Dfabric.node=!NODE! -Djava.util.logging.config.file=!NODE_LOGGING_FILE! -DSTOP.KEY=stop_jetty -jar start.jar jetty.port=!JETTY_PORT!
		set retval=!ERRORLEVEL!
		title !comspec!
	) else (
		start "Fabric WebServer : !JETTY_PORT!" /MIN java -Dfabric.node=!NODE! -Djava.util.logging.config.file=!NODE_LOGGING_FILE! -DSTOP.PORT=!PORT! -DSTOP.KEY=stop_jetty -jar start.jar jetty.port=!JETTY_PORT!
		set retval=0
	)
	
	call:debug [startWeb] exit: !retval!
	goto:EOF
	
:stopWeb

	call:debug [stopWeb] enter: %*
	set retval=

	cd !FABRIC_HOME!\web
	
	cmd /C java -DSTOP.PORT=!PORT! -DSTOP.KEY=stop_jetty -jar start.jar --stop
	
	set retval=!ERRORLEVEL!
	call:debug [stopWeb] exit: !retval!
	goto:EOF

:displayInterfaces

	call:runLauncher -p --interfaces
	goto:EOF

:checkStatus

	call:debug [checkStatus] enter: %*
	set retval=

	call:checkBrokerStatus Broker mosquitto Broker
	call:checkComponentStatus Registry registry Registry
	call:checkComponentStatus Manager fm Node
	call:checkComponentStatus WebServer web WebServer

	call:debug [checkStatus] exit: !retval!
	goto:EOF

:checkComponentStatus

	call:debug [checkComponentStatus] enter: %*
	set DONEHEADER=0
	set retval=

	for %%a in (!FABRIC_HOME!\pid\.%2.*) do (
		set p=%%~nxa
		set nn=%%~xa
		set nn=!nn:~1!
		for /F %%G in ('tasklist /FI "WINDOWTITLE eq Fabric %1 : !nn!" /NH') do if not "%%G"=="INFO:" (
			if "!DONEHEADER!"=="0" (
				echo %3
				set DONEHEADER=1
			)
			echo   !nn! : running
		) else (
			del %%a
		)
	)
	if "!DONEHEADER!"=="0" (
	   echo %3 : Not Running
	)
	
	call:debug [checkComponentStatus] exit: !retval!
	goto:EOF

:checkBrokerStatus

	call:debug [checkBrokerStatus] enter: %*
	set DONEHEADER=0
	set retval=

		for /F %%G in ('tasklist /FI "IMAGENAME eq %2.exe" /NH') do if not "%%G"=="INFO:" (
			if "!DONEHEADER!"=="0" (
				echo %3
				set DONEHEADER=1
			)
			echo   %2 : running
		) 
	if "!DONEHEADER!"=="0" (
	   echo Broker %2 : Not Running
	)
		
	call:debug [checkBrokerStatus] exit: !retval!
	goto:EOF
	
:runLauncher

	call:debug [runLauncher] enter: %* 
	set retval=

	rem - Invoke the Fabric launcher directly

	set CLASSPATH=!FABRIC_HOME!\lib\db-derby\lib\derbyclient.jar;!FABRIC_HOME!\lib\oslib\commons-cli-1.2.jar
	for %%F in (!FABRIC_HOME!\lib\fabric\fabric*.jar) do set CLASSPATH=!CLASSPATH!;%%F

 	if "!DBTYPE!"=="gaian" (
		for %%F in (!FABRIC_HOME!\lib\gaiandb\lib\*.jar) do set CLASSPATH=%%F;!CLASSPATH!
	)

	java -Dfabric.node=!NODE! fabric.tools.launchers.FabricLauncher %*
	set retval=!ERRORLEVEL!
	
	call:debug [runLauncher] exit: !retval!
	goto:EOF

rem **************************************************************************

:main

call:debug [main] enter: %*

if not exist "!FABRIC_HOME!" (
	echo Environment variable FABRIC_HOME not set
	goto:EOF
)

REM Remove any spaces from the FABRIC_HOME path
for %%H in ("!FABRIC_HOME!") do set FABRIC_HOME=%%~sH

set DBTYPE=distributed
set DAEMON=0
set ACTION=
set TYPE=
set NODE=default
set CONSOLE=
set PORT=9080
set COMMAND_TOPIC=$fabric/{0}/$feeds/$onramp

set PASSTHROUGH=0
set GET_NODE=0
set GET_PORT=0

:loop

	if not "%1"=="" (
	
		set _T=%1
		
		rem if "!_T:~0,2!"=="--" (
		rem 	set _T=!_T:~1!
		rem )

		if "!_T!"=="--passthrough" (
			set PASSTHROUGH=1
		)
		if "!_T:~0,2!"=="-p" (
			set PASSTHROUGH=1
		)

		if "!_T!"=="--console" (
			set CONSOLE="-console"
		)
		if "!_T!"=="-c" (
			set CONSOLE="-console"
		)
		
		if "!_T!"=="--daemon" (
			set DAEMON=1
		)
		if "!_T!"=="-d" (
			set DAEMON=1
		)

   		if "!_T!"=="--start" (
   			set ACTION=START
   		)
   		if "!_T:~0,2!"=="-s" (
   			set ACTION=START
   		)
   		
   		if "!_T!"=="--stop" (
   			set ACTION=STOP
   		)
   		if "!_T:~0,3!"=="-st" (
   			set ACTION=STOP
   		)
   		
   		if "!_T!"=="-clean" (
   			set ACTION=CLEAN
   		)
   		
   		if "!_T!"=="--interfaces" (
   			set ACTION=INTERFACES
   		)
   		if "!_T!"=="-i" (
   			set ACTION=INTERFACES
   		)
   		
   		if "!_T!"=="-status" (
   			set ACTION=STATUS
   		)
   		
   		if "!_T!"=="--registry" (
   			set TYPE=REGISTRY
   			set GET_NODE=1
   		)
   		if "!_T:~0,2!"=="-r" (
   			set TYPE=REGISTRY
   			set GET_NODE=1
   		)
   		
   		if "!_T!"=="-gaian" (
   			set DBTYPE=gaian
   		)
   		
   		if "!_T!"=="-distributed" (
   			set DBTYPE=distributed
   		)
		
		if "!_T!"=="--node" (
			set TYPE=NODE
			set GET_NODE=1
		)
		if "!_T:~0,2!"=="-n" (
			set TYPE=NODE
			set GET_NODE=1
		)
   		
   		if "!_T!"=="--web" (
   			set TYPE=WEB
   			set GET_NODE=1
   		)
   		if "!_T:~0,2!"=="-w" (
   			set TYPE=WEB
   			set GET_NODE=1
   		)
   		
   		if "!_T!"=="-port" (
   			set GET_PORT=1
   		)
		
		if "!GET_NODE!"=="1" (
			if not "%2"=="" (
				set _T=%2
				if not "!_T:~0,1!"=="-" (
					set NODE=!_T!
					shift
				)
			)
			set GET_NODE=
		)
		
		if "!GET_PORT!"=="1" (
			if not "%2"=="" (
				set _T=%2
				if not "!_T:~0,1!"=="-" (
					set PORT=!_T!
					shift
				)
			)
			set GET_PORT=
		)

		shift
		goto :loop
	)

if "!PASSTHROUGH!"=="1" (

	call:debug [main:passthrough] entering

	call:runLauncher %*
	exit /b !retval!
	
)

if "!ACTION!"=="INTERFACES" (

	call:debug [main:interfaces] entering

	call:displayInterfaces
	exit /b 0
	
)

if "!ACTION!"=="STATUS" (

	call:debug [main:status] entering

	call:checkStatus
	exit /b 0
	
)

if "!ACTION!"=="STOP" (

	call:debug [main:stop] entering

	if "!TYPE!"=="REGISTRY" (
	
		call:debug [main:stop:registry] entering ^(node is !NODE!, database type is !DBTYPE!^)

		call:stopRegistry
		exit /b !retval!
		
	) else if "!TYPE!"=="WEB" (
	
		call:debug [main:stop:web] entering

		call:stopWeb
		exit /b !retval!
		
	) else (
	
		call:debug [main:stop:!TYPE!] entering ^(node is !NODE!^)

		echo - Stopping !TYPE!
		call:runLauncher %*
		exit /b !retval!
		
	)
)

if "!ACTION!"=="START" (

	call:debug [main:start] entering

	if "!TYPE!"=="REGISTRY" (

		call:debug [main:start:registry] entering ^(node is !NODE!, database type is !DBTYPE!^)

		call:startRegistry
		exit /b !retval!
		
	)
	
	if "!TYPE!"=="NODE" (

		call:debug [main:start:node] entering ^(node is !NODE!^)
	
		call:startNode
		exit /b !retval!
		
	)
		
	if "!TYPE!"=="WEB" (
	
		call:debug [main:start:web] entering ^(node is !NODE!^)
		
		call:startWeb
		exit /b !retval!
	
	)
)

if "!ACTION!"=="CLEAN" (
	
	call:debug [main:clean] entering

	if "!TYPE!"=="REGISTRY" (
	
		call:debug [main:clean:registry] entering ^(database type is !DBTYPE!^)

		call:stopRegistry
		if "!retval!"=="0" (
		
			call:debug [main:clean:registry] deleting existing registry
			echo - Deleting existing Registry
			
			call:runLauncher --registry -delete -!DBTYPE!
			if "!retval!"=="0" (
			
				call:debug [main:clean:registry] starting Registry in the background
				cmd /c fabadmin --registry --start --daemon -!DBTYPE! 
				call:waitForRegistry

				call:debug [main:clean:registry] creating the Registry database
				echo - Creating the Registry database
				call:runLauncher --registry -create -!DBTYPE! > !FABRIC_HOME!\log\fabregistry_create.log
				
				call:debug [main:clean:registry] loading Registry default values
				echo - Loading default values into the Registry
				call:runLauncher --registry -load -!DBTYPE! > !FABRIC_HOME!\log\fabregistry_load.log
				
				call:debug [main:clean:registry] stopping the Registry
				cmd /c fabadmin --registry --stop -!DBTYPE!

				exit /b !ERRORLEVEL!
			)

			exit /b !retval!
			
		) else (
		
			call:debug [main:clean:registry] could not stop Registry to clean
			echo - Registry failed to stop - cannot perform clean
			exit /b 1
			
		)
	)
)

rem - If we we get this far then no valid command found
echo Usage:
echo fabadmin -s [-d] -gaian^|-distributed -r node
echo fabadmin -s -n [-d] [-c] node
echo fabadmin -s -w [-d] node
echo fabadmin -st -gaian^|-distributed -r
echo fabadmin -st -n node
echo fabadmin -st w
echo fabadmin -i
echo fabadmin -status
echo fabadmin -clean -gaian^|-distributed -r
echo fabadmin -p [arguments]
