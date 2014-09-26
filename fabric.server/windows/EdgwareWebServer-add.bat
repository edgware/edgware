REM **************************************************************************
REM Licensed Materials - Property of IBM
REM
REM (C) Copyright IBM Corp. 2014
REM
REM LICENSE: Eclipse Public License v1.0
REM http://www.eclipse.org/legal/epl-v10.html
REM **************************************************************************

%FABRIC_HOME%\server\windows\prunsrv //IS//EdgwareWebServer ^
	--Description="Edgware Web Server" ^
	--DisplayName="Edgware Web Server" ^
	--Install="%FABRIC_HOME%\server\windows\prunsrv" ^
	--Jvm auto ^
	--Startup auto ^
	--StartMode jvm ^
	--StartClass fabric.server.windows.WindowsService ^
	--StartMethod start ^
	--StartParams webserver ++StartParams default ^
	--StopMode jvm ^
	--StopClass fabric.server.windows.WindowsService ^
	--StopMethod stop ^
	--Classpath %FABRIC_HOME%\server\windows\WindowsService.jar ^
	--LogPath %FABRIC_HOME%\log\server ^
	--LogPrefix EdgwareWebServer-procrun ^
	--LogLevel Info ^
	--StdOutput auto ^
	--StdError auto ^
	--DependsOn EdgwareRegistry