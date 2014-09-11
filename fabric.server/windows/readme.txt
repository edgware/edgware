**************************************************************************
Licensed Materials - Property of IBM

(C) Copyright IBM Corp. 2014. All Rights Reserved.

LICENSE: Eclipse Public License v1.0
http://www.eclipse.org/legal/epl-v10.html

US Government Users Restricted Rights - Use, duplication or
disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
**************************************************************************

Windows Services for Edgware Introduction
========================================

This readme describes using Edgware with the Windows Services framework.  It allows for the three Edgware components of the Edgware Registry, the Edgware Node and the Edgware Web Server to be run as Windows Services.



Installation and Configuration
==============================

Each of the three Edgware components that has the ability to run as a Windows Service is supplied with a set of batch files for installation and configuration.

Batch files ending in:
 -add.bat    : add the service to the Windows Services framework
 -delete.bat : delete the service from the Windows Services framework
 -edit.bat   : provide more detailed editing capabilities than is available via the Windows Services properties context menu
 
The following four sub-sections describe how to install and configure Edgware for use as Windows Services.


1. Pre-requisites
-----------------
The procrun.exe and procmgr.exe programs are required.  They are provided by Apache Commons Daemon, see Apache Commons Daemon web site [1] with more information about procrun on the procrun page [2].

Download the commons-daemon zip for Windows from the Windows Download Area [3].

Extract the procrun.exe and procmgr.exe programs from the zip file and place these into the Edgware server directory alongside this readme and other files in this directory. Note that you should use the 64-bit procsrv.exe if your Windows system is 64-bit and the 32-bit version otherwise.


2. Edgware Registry
------------------

Run the edgwareregistry-add.bat file to install the Edgware Registry as a Windows Service.

Go to the Windows Services GUI (press F5 to refresh if you already have it open) and a new Edgware Registry option will now be available.  Start and stop the service in the normal way as for other Windows Services.

The Edgware Registry is configured by default to start automatically when the machine boots.  You can modify this behaviour in the Windows Services GUI if you wish.

The Edgware Registry type is configured by default to be gaian. If you have setup a distributed registry you would need to edit the edgwareregistry-add.bat file.
You would change a line from "	--StartParams registry ++StartParams gaian;default ^" to "--StartParams registry ++StartParams distributed;default ^" as this is the parameter that controls which type of registry is started.

The Edgware Registry is configured by default to send trigger messages to the default Node. If you wish to send these triggers to a different node you would need to edit the edgwareregistry-add.bat file.
You would change a line from "	--StartParams registry ++StartParams gaian;default ^" to "	--StartParams registry ++StartParams gaian;ExampleNode" as this is the parameter that controls which Edgware Node the triggers are sent to.










3. Broker
---------

Run the broker-add.bat file to install the Broker as a Windows Service.

Go to the Windows Services GUI (press F5 to refresh if you already have it open) and a new Fabric Broker option will now be available.  Start and stop the service in the normal way as for other Windows Services.

The Broker is configured by default to start automatically when the machine boots.  You can modify this behaviour in the Windows Services GUI if you wish.


4. Fabric Node(s)
-----------------

By default the defaultnode-add.bat file will install the default Fabric node as a Windows Service.

Go to the Windows Services GUI (press F5 to refresh if you already have it open) and a new Fabric Default Node option will now be available.  Start and stop the service in the normal way as for other Windows Services.

The Default Node is configured by default to start automatically when the machine boots.  You can modify this behaviour in the Windows Services GUI if you wish.

If you wish to run additional nodes or nodes other than the default node then these can each be installed separately as a Windows Service.  You must first set up and configure the additional Fabric nodes.  The following example explains how to add a node you have already set up called "ExampleNode" as a Windows Service.

Copy the defaultnode-add.bat file to examplenode-add.bat and edit the new examplenode-add.bat file.  It will contain an entry similar to the following:

%FABRIC_HOME%\server\windows\prunsrv //IS//FabricDefaultNode ^
	--Description="Fabric Default Node" ^
	--DisplayName="Fabric Default Node" ^
	--Install="%FABRIC_HOME%\server\windows\prunsrv" ^
	--Jvm auto ^
	--Startup auto ^
	--StartMode jvm ^
	--StartClass fabric.server.windows.WindowsService ^
	--StartMethod start ^
	--StartParams node ++StartParams default ^
	--StopMode jvm ^
	--StopClass fabric.server.windows.WindowsService ^
	--StopMethod stop ^
	--Classpath %FABRIC_HOME%\server\windows\WindowsService.jar ^
	--LogPath %FABRIC_HOME%\log\server ^
	--LogPrefix fabricdefaultnode-procrun ^
	--LogLevel Info ^
	--StdOutput auto ^
	--StdError auto ^
	--DependsOn FabricBroker
	
Change this to:

%FABRIC_HOME%\server\windows\prunsrv //IS//FabricExampleNode ^
	--Description="Fabric Example Node" ^
	--DisplayName="Fabric Example Node" ^
	--Install="%FABRIC_HOME%\server\windows\prunsrv" ^
	--Jvm auto ^
	--Startup auto ^
	--StartMode jvm ^
	--StartClass fabric.server.windows.WindowsService ^
	--StartMethod start ^
	--StartParams node ++StartParams ExampleNode ^
	--StopMode jvm ^
	--StopClass fabric.server.windows.WindowsService ^
	--StopMethod stop ^
	--Classpath %FABRIC_HOME%\server\windows\WindowsService.jar ^
	--LogPath %FABRIC_HOME%\log\server ^
	--LogPrefix fabricdefaultnode-procrun ^
	--LogLevel Info ^
	--StdOutput auto ^
	--StdError auto ^
	--DependsOn FabricBroker

The important change in the above configuration is the change from "++StartParams default" to "++StartParams ExampleNode" as this is the parameter that controls which Fabric Node will be started by the associated Windows Service.  The other configuration entries are for human readability only.  The change to //IS//FabricExampleNode is also necessary since the short names of services inside Windows must be unique on each machine.  Note also that we specifically add a dependency on the Fabric Broker since this service is required by all Fabric Nodes.

For more information on these configuration options please refer to the procrun documentation [2].


Logging
=======

Logs for all Fabric components running as Windows Services will appear in the directory %FABRIC_HOME%\log\server

Each service will have three separate log files:
  1) A log from Apache Commons Daemon itself
  2) A log from the service containing standard output
  3) A log from the service containing standard error output

  

Managing Services
=================

Services can and should be managed using the Windows Services framework in the same way other Windows Services are managed.  However, should more detailed management or configuration be required (i.e. if you need to alter the parameters used to install the service in some way) then run the appropriate *-edit.bat file.  This calls procmgr and will show a dialog similar to the properties dialog for the Windows Service but with additional configuration tabs and options otherwise not available.  Changes here would be considered advanced usage and should only be made if you were, for example, to need to modify the classpath or other specific parameters.


  
Deleting Services
=================

Services can be removed from the Windows Services framework by running the appropriate *-delete.bat file.  This simply calls procrun to remove the Windows Service and does not actually delete anything on the machine itself.



References
==========

[1] Apache Commons Daemon Web Site http://commons.apache.org/proper/commons-daemon/
[2] Procrun Documentation http://commons.apache.org/proper/commons-daemon/procrun.html
[3] Apache Commons Daemon Windows Download http://apache.mirror.anlx.net//commons/daemon/binaries/windows/



Troubleshooting
===============

1) Problem: a service fails to start when using the IBM JRE.

If the logs show the error "Failed creating java" the issue can be resolved as follows:
  1.1) run the *-edit.bat file for the service to edit the configuration
  1.2) go to the Java tab
  1.3) remove the check for "Use default"
  1.4) select a new Java Virtual Machine by clicking on the button with 3 dots
  1.5) select the IBM J9 jvm.dll inside your JRE directory, a typical path for this file is C:\Program Files\IBM\Java71\jre\bin\j9vm\jvm.dll
  1.6) click OK to apply the new setting and close the window
