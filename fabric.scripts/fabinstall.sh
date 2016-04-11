#!/bin/bash

# **************************************************************************
# (C) Copyright IBM Corp. 2006, 2012
#
# LICENSE: Eclipse Public License v1.0
# http://www.eclipse.org/legal/epl-v10.html
# **************************************************************************

function usage {
    echo "Usage: fabinstall [registry|noregistry] [gaian|distributed]"
    exit 1
}

function configRegistryJars {

	monitor_jar=`ls $FABRIC_HOME/lib/fabric/fabric.registry.monitor*.jar`
	lib_jar=`ls $FABRIC_HOME/lib/fabric/fabric.lib*.jar`
	core_jar=`ls $FABRIC_HOME/lib/fabric/fabric.core*.jar`
	mqtt_jar=`ls $FABRIC_HOME/lib/oslib/org.eclipse.paho.client.mqtt*.jar`
	derby_client_jar=`ls $FABRIC_HOME/lib/db-derby/lib/derbyclient.jar`

	rm -f "$FABRIC_HOME/lib/sql/80_REGMON_JARS_ALL_LOAD.sql"

	sed "s^@@MONITOR_JAR@@^$monitor_jar^" "$FABRIC_HOME/lib/sql/80_REGMON_JARS_ALL_LOAD.template" | \
	sed "s^@@LIB_JAR@@^$lib_jar^" | sed "s^@@CORE_JAR@@^$core_jar^" | sed "s^@@MQTT_JAR@@^$mqtt_jar^" | \
	sed "s^@@DERBY_CLIENT_JAR@@^$derby_client_jar^" > "$FABRIC_HOME/lib/sql/80_REGMON_JARS_ALL_LOAD.sql"

}

# **************************************************************************
# Install and configure the Fabric ready for use
# **************************************************************************

# Set FABRIC_HOME to the location of the fabinstall script
export FABRIC_HOME=`dirname $(cd ${0%/*} && echo $PWD/${0##*/})`
export PATH="$PATH:$FABRIC_HOME/bin/linux"

# Confirm acceptance of the license agreement
#license
#LICENSE_ACCEPTED=$?
#
#if [ ! $LICENSE_ACCEPTED -eq 0 ]
#then
#	echo "Installation terminated."
#	exit 1;
#fi

echo "USE OF THE CONTENT IS GOVERNED BY THE TERMS AND CONDITIONS OF THE INCLUDED"
echo "LICENSE AGREEMENT AND/OR THE TERMS AND CONDITIONS OF LICENSE AGREEMENTS OR"
echo "NOTICES INDICATED IN THE INCLUDED NOTICES FILE."

# Determine installation type

if [ ! "$1" == "" ]
then
	case "$1" in
		'registry')
			install=registry
			;;
		'noregistry')
			install=noregistry
			;;
		*)
			usage
			;;
	esac
else
	install=registry
fi

if [ ! "$2" == "" ]
then
	case "$2" in
		'distributed')
			dbtype="distributed"
			;;
		'gaian')
			dbtype="gaian"
			;;
		*)
			usage
			;;
	esac
else
	dbtype="gaian"
fi

echo
echo "Installing $dbtype $install."
echo "Log files will be written to $FABRIC_HOME/log."

if [ ! -d "$FABRIC_HOME/log" ]
then
    mkdir "$FABRIC_HOME/log"
fi

PLATFORM=`uname`

if [ "$install" == "registry" ]
then
	echo "Initialising the Registry."
	
	export FABRIC_TRIGGERS=false
	
	# Configure the SQL to install Fabric JARs into Derby
	configRegistryJars
	
	# Initialise the Registry
	fabadmin --registry -clean -$dbtype >"$FABRIC_HOME/log/fabregistry_reset.log" 2>&1
	
	if [ $? -eq 0 ]
	then
		echo "Registry initialised."
	else
		echo "Failed to initialise the Registry. Check $FABRIC_HOME/log for details."
		exit 1
	fi
fi

echo
echo -e "Installation complete, now please:"
echo -e "1. Set the following environment variables:"
if [ $(uname) == "Darwin" ]
then
	echo -e "\tFABRIC_HOME=$FABRIC_HOME"
	echo -e "\tPATH=\$PATH:\$FABRIC_HOME/bin/linux"
else
	echo -e "\tJAVA_HOME=<java_location>"
	echo -e "\t(where <java_location> is the home directory of your system\'s JRE"
	echo -e "\tPATH=\$JAVA_HOME/jre/bin:\$PATH:\$FABRIC_HOME/bin/linux\n";
fi
echo -e "2. Start the Registry using the command:"
echo -e "\tfabadmin -s -$dbtype -r <node-name>"
echo -e "   Where:"
echo -e "\t<node-name> is the name of the Fabric node that will run on this system"
echo -e "3. Display the list of available network adapters on this system using the command:"
echo -e "\tfabadmin -i"
echo -e "4. Configure the Fabric node to use one or more of the available network adapters"
echo -e "   displayed in step 3 (this only needs to be done once) using the command:"
echo -e "\tfabreg -sn <node-name> fabric.node.interfaces <network-adapter-list>"
echo -e "   Where:"
echo -e "\t<network-adapter-list> is a comma separated list (no spaces) of network"
echo -e "\tadapters e.g. \"en0,en5\""
echo -e "   The Fabric node will discover and connect to neighbours using these adapters."
echo -e "5. Ensure that the Mosquitto broker is running with a configuration matching:"
echo -e "\t$FABRIC_HOME/etc/broker.conf"
echo -e "6. You can now start the Fabric node using the command:"
echo -e "\tfabadmin -s -n <node-name>"
