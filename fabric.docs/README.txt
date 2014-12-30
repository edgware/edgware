-------------------------------------------------------------------------------
Edgware Fabric v0.4.0
README
-------------------------------------------------------------------------------

1 INSTALLATION

Edgware has two software pre-requisites:

- Java: a Java 2 Standard Edition (J2SE) Java Runtime Environment (JRE) is
  required to run the Fabric. To develop Fabric extensions a J2SE Java
  Development Kit (JDK) is also required. The tested version for both the JRE
  and the JDK is 7.x.
  
- Mosquitto broker: Edgware can be configured for a variety of MQTT-compatable
  brokers, however out-of-the-box it will work with the Mosquitto open source
  message broker. You can get the latest version for your system (including
  Raspberry Pi) from http://mosquitto.org/download.
  
Both of these pre-requisites must be available on your system before installing
Edgware.

1.1 INSTALL EDGWARE

Download the latest stable release of Edgware from http://edgware-fabric.org.
The .zip or .gzip contains a top-level folder called edgware-X.Y.Z where X.Y.Z
is the version number. Once extracted, from within that top-level folder, run
the appropriate command for your operating system:

Linux and OS X:

	$ ./fabinstall.sh

Windows:

	> fabinstall

Note: it is important to choose the appropriate file for your system, .zip for
Windows or .gzip for Linux, to ensure that file permissions are set correctly
when the file is unpacked.

At the end of the installation script a message will be displayed indicating
that two environment variables must be set:

- JAVA_HOME: the Java home directory (may already be set for your system).

- FABRIC_HOME: the root directory for the Edgware installation; this is the
  directory containing the fabinstall script.

Both of these are required for Edgware to install and run.


2. RUNNING EDGWARE

The Edgware software stack consists of three main components:

- A Java OSGI container to run the services that make up an Edgware node

- An Apache Derby database to hold the Edgware Registry

- An MQTT-enabled broker used to form the MQTT backbone of the bus

In addition there is an optional Web server supporting the HTTP interface to
Edgware.

Each of these components can be started from either the command line or as
system services. Using the command line, each component must be started
individually via the fabadmin command, in the order shown below. To start the
components in the background add the -daemon command line option.

To start the Edgware components as system services, see the readme.txt files
under $FABRIC_HOME/server/linux or %FABRIC_HOME%\server\windows.

Note: these instructions assume that the Mosquitto broker is already running,
either from the command line or as a system service. See the Mosquitto
documentation for more information.

STARTING THE REGISTRY:

	$ fabadmin -s -r node-name

STARTING THE EDGWARE NODE:

	$fabadmin -s -n node-name

node-name is the name that you would like to assign to this Edgware node. This
is the name by which it will be known on the bus. It could be, but does not
have to be, the hostname.

STARTING THE WEB SERVER (OPTIONAL):

	$ fabadmin -s -w

By default you can then access the Edgware Web server at
http://localhost:8080/rest.

If you would like to start the Edgware components as system services, see the
readme.txt files under $FABRIC_HOME/server/linux or
%FABRIC_HOME%\server\windows.


3. EDGWARE LOG FILES

Edgware logging is a useful source of diagnostic information for use in problem
determination. Logging information is written to the console, and saved in a
file in the following directory:

	Windows: %FABRIC_HOME%\log

	Linux and OS X: $FABRIC_HOME/log

3.1 CONFIGURING LOGGING

The level of detail included in log output can be controlled via the
configuration file fabricConfig_default.properties in the directory:

	Windows: %FABRIC_HOME%\osgi\configuration

	Linux and OS X: $FABRIC_HOME/osgi\configuration

See the on-line Edgware documentation for more information.


4 AUTO-DISCOVERY

4.1 AUTO-DISCOVERY BROADCAST PROPAGATION

The auto-discovery UDP broadcast used by Edgware to request a connection will
typically only propagate as far as any routers in the network. Most routers
will block broadcasts, thus restricting the visibility of the request. This is
mitigated by use of multicast, andthe ability to statically define known nodes
if required.


5. KNOWN PROBLEMS

5.1 INSTALLATION USING A DIRECTORY PATH CONTAINING SPACES

It is not currently possible to install Edgware into a directory that contains
one or more spaces in its path (for example: "C:\Program Files\Edgware".)

Please select an alternative directory whose path does not contain spaces.
