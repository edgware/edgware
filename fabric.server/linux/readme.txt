**************************************************************************
(C) Copyright IBM Corp. 2014. All Rights Reserved.

LICENSE: Eclipse Public License v1.0
http://www.eclipse.org/legal/epl-v10.html

US Government Users Restricted Rights - Use, duplication or
disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
**************************************************************************


Linux Services for Edgware Introduction
======================================

This readme describes using Edgware with various Linux Services frameworks.  It allows for the three Edgware components of the Edgware Registry, the Edgware Node and the Edgware Web Server to be run as daemons.

Configurable Variables
======================
The following variables are configurable and should match your installation. Exact locations for setting these variables are described in the section appropriate to your Linux Service Framework.

FABRIC_HOME - location of your Edgware installation (/opt/ibm/edgware-0.4.1)

REGISTRY_TYPE - type of registry installed distributed or gaian

REGISTRY_NODE - Node name for receiving registry trigger notifications (default)

WEBSERVER_NODE - Edgware Node Name for the Web Server content to communicate with (default)

The scripts start an Edgware Node called "default".  You should edit this name to reflect your intended Edgware Node Name.
In addition your PATH should include $FABRIC_HOME/bin/linux

System V Init
=============

System V Init is a common mechanism to denote which software to start at boot time.  It is highly likely to be compatible with your version of Linux.  If you're not sure which set of scripts to use, these are probably the right ones.

Edgware scripts related to System V Init are located in $FABRIC_HOME/server/linux/sysvinit


Generic Installation Instructions
---------------------------------

These instructions serve as a generic installation guide for fully manual installation of the Edgware components as Linux daemons.  It is likely your Linux distribution has some commands to help, see the following sections for examples.

1) Edit the $FABRIC_HOME/server/linux/sysconfig file and set the variables as appropriate for your Edgware installation

2) Copy the 3 scripts edgware-registry, edgware-nodes and edgware-webserver to the /etc/rc.d/init.d/ directory, ensure they are executable and check the variables in the scripts match your installation

3) Set the scripts to start when the appropriate runlevel is reached at boot time.  The following example (to be run as root) demonstrates enabling the scripts for runlevel 3:

   3.1) cd /etc/rc.d/rc3.d/
   
   3.2) ln -s ../init.d/edgware-registry S97edgware-registry
   
   3.3) ln -s ../init.d/edgware-nodes S98edgware-nodes
   
   3.4) ln -s ../init.d/edgware-webserver S99edgware-webserver
   
4) Set the scripts to stop when the appropriate runlevel is reached at shutdown.  The following example (to be run as root) demonstrates disabling the scripts for runlevel 0:

   4.1) cd /etc/rc.d/rc0.d/
   
   4.2) ln -s ../init.d/edgware-registry S03edgware-registry
   
   4.3) ln -s ../init.d/edgware-nodes S02edgware-nodes
   
   4.4) ln -s ../init.d/edgware-webserver S01edgware-webserver
   
5) To start the edgware components call the script with the start parameter e.g. for the Edgware Registry "/etc/rc.d/init.d/edgware-registry start"
   
6) To stop the edgware components call the script with the start parameter e.g. for the Edgware Registry "/etc/rc.d/init.d/edgware-registry stop"
   
7) To query the status of the edgware components call the script with the status parameter e.g. for the Edgware Registry "/etc/rc.d/init.d/edgware-registry status"


Generic LSB Compliant Instructions
----------------------------------

These instructions serve as a generic installation guide for installation of Edgware components as Linux daemons on LSB compliant distributions.  Use these instructions if none of those listed below help.

1) Edit the $FABRIC_HOME/server/linux/sysconfig file and set the variables as appropriate for your Edgware installation

2) Copy the 3 scripts edgware-registry, edgware-nodes and edgware-webserver to the /etc/init.d/ directory, ensure they are executable and check the variables in the scripts match your installation

3) Set the scripts to start at boot time with "/usr/lib/lsb/install_initd edgware-registry && /usr/lib/lsb/install_initd edgware-nodes && /usr/lib/lsb/install_initd edgware-webserver"

4) To start the edgware components e.g. for the Edgware Registry "/etc/init.d/edgware-registry start"

5) To stop the edgware components e.g. for the Edgware Registry "/etc/init.d/edgware-registry stop"
   
6) To query the status of the edgware components e.g. for the Edgware Registry "/etc/init.d/edgware-registry status"



Installation on Red Hat Like Linux Distributions
------------------------------------------------

These instructions serve as a generic installation guide for installation of Edgware components as Linux daemons on Red Hat like distributions.  This includes distributions such as Red Hat Enterprise Linux, CentOS and Fedora.

1) Edit the $FABRIC_HOME/server/linux/sysconfig file and set the variables as appropriate for your Edgware installation

2) Copy the 3 scripts edgware-registry, edgware-nodes and edgware-webserver to the /etc/init.d/ directory, ensure they are executable and check the variables in the scripts match your installation

3) Set the scripts to start at boot time with "chkconfig --add edgware-registry && chkconfig --add edgware-nodes && chkconfig --add edgware-webserver"

4) To start the edgware components e.g. for the Edgware Registry "service edgware-registry start"
   
5) To stop the edgware components e.g. for the Edgware Registry "service edgware-registry stop"
   
6) To query the status of the edgware components e.g. for the Edgware Registry "service edgware-registry status"


Installation on SUSE Like Linux Distributions
---------------------------------------------

These instructions serve as a generic installation guide for installation of Edgware components as Linux daemons on SUSE like distributions.  This includes distributions such as SUSE Linux Enterprise Server and openSUSE.

1) Edit the $FABRIC_HOME/server/linux/sysconfig file and set the variables as appropriate for your Edgware installation

2) Copy the 3 scripts edgware-registry, edgware-nodes and edgware-webserver to the /etc/init.d/ directory, ensure they are executable and check the variables in the scripts match your installation

3) Set the scripts to start at boot time with "insserv edgware-registry && insserv edgware-nodes && insserv edgware-webserver"

4) Create links to start scripts "cd /sbin; ln -s /etc/init.d/edgware-registry rcedgware-registry && ln -s /etc/init.d/edgware-nodes rcedgware-nodes && ln -s /etc/init.d/edgware-webserver rcedgware-webserver"

5) To start the edgware components e.g. for the Edgware Registry "rcedgware-registry start"
   
6) To stop the edgware components e.g. for the Edgware Registry "rcedgware-registry stop"
   
7) To query the status of the edgware components e.g. for the Edgware Registry "rcedgware-registry status"


Installation on Ubuntu
-------------------------

These instructions serve as a generic installation guide for installation of Edgware components as Linux daemons on Ubuntu.

1) Edit the $FABRIC_HOME/server/linux/sysconfig file and set the variables as appropriate for your Edgware installation

2) Copy the 3 scripts edgware-registry, edgware-nodes and edgware-webserver to the /etc/init.d/ directory, ensure they are executable and check the variables in the scripts match your installation

3) Set the scripts to start at boot time with "update-rc.d edgware-registry defaults && update-rc.d edgware-nodes defaults && update-rc.d edgware-webserver defaults"

4) To start the edgware components e.g. for the Edgware Registry "invoke-rc.d edgware-registry start"
   
5) To stop the edgware components e.g. for the Edgware Registry "invoke-rc.d edgware-registry stop"
   
6) To query the status of the edgware components e.g. for the Edgware Registry "invoke-rc.d edgware-registry status"


Systemd
=======

Systemd is a System V Init replacement used in some Linux distributions as a mechanism to denote which software to start based on system events.  If your distributon uses systemd you can take advantage of it with Edgware rather than use the sysvinit scripts.

Edgware scripts related to System V Init are located in $FABRIC_HOME/server/linux/systemd


Installation on Red Hat Like Linux Distributions
------------------------------------------------

These instructions are a guide for installation of Edgware components as Linux daemons on Red Hat like distributions using systemd.  This includes distributions such as Fedora and above Red Hat Enterprise Linux 7 or CentOS 7.

Note: the systemd scripts start a Edgware Node called "default".  You should edit this name and rename the appropriate file to start the name of the Edgware Node you intend to start.  To start multiple Edgware Nodes on the same machine, copy the service file and change the node name, issuing one systemctl command for each Node.

1) Edit the $FABRIC_HOME/server/linux/sysconfig file and set the variables as appropriate for your Edgware installation

2) Copy the 3 files edgware-registry.service, edgware-nodes.service and edgware-webserver.service to the /etc/systemd/ directory

3) Set the scripts to start at boot time with "systemctl enable edgware-registry && systemctl enable edgware-node-default && systemctl enable edgware-webserver"

4) To start the edgware components e.g. for the Edgware Registry "systemctl start edgware-registry"
   
5) To stop the edgware components e.g. for the Edgware Registry "systemctl stop edgware-registry"
   
6) To query the status of the edgware components e.g. for the Edgware Registry "systemctl status edgware-registry"


Upstart
=======

Upstart is a System V Init replacement used in some Linux distributions as a mechanism to denote which software to start based on system events.  If your distributon uses upstart you can take advantage of it with Edgware rather than use the sysvinit scripts.

Edgware scripts related to upstart are located in $FABRIC_HOME/server/linux/upstart

Installation
------------

These instructions are a guide for installation of Edgware components as Linux daemons on upstart enabled systems.  This includes distributions such as older versions of Fedora, Red Hat Enterprise Linux 6, CentOS 6 or Ubuntu.

1) Edit the $FABRIC_HOME/server/sysconfig file and set the variables as appropriate for your Edgware installation

2) Copy the 3 files edgware-registry.conf, edgware-nodes.conf and edgware-webserver.conf to the /etc/init/ directory, check the variables in the scripts match your installation

3) To start the edgware components e.g. for the Edgware Registry "start edgware-registry"
   
4) To stop the edgware components e.g. for the Edgware Registry "stop edgware-registry"
   
5) To query the status of the edgware components e.g. for the Edgware Registry "status edgware-registry"
