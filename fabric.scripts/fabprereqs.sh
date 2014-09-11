#!/bin/bash

# **************************************************************************
# Licensed Materials - Property of IBM
#
# (C) Copyright IBM Corp. 2014
#
# LICENSE: Eclipse Public License v1.0
# http://www.eclipse.org/legal/epl-v10.html
# **************************************************************************

# **************************************************************************
# Check that the pre-reqs are available
# **************************************************************************

missing_prereqs=false

# Checking if prereqs exist
if [ ! -e prereqs/db-derby-10.8.3.0-bin.zip ]
then
	echo "db-derby-10.8.3.0-bin.zip does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/eclipse-platform-3.7.2-win32.zip ]
then
	echo "eclipse-platform-3.7.2-win32.zip does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/equinox-SDK-KeplerSR2.zip ]
then
	echo "equinox-SDK-KeplerSR2.zip does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/commons-cli-1.2-bin.zip ]
then
	echo "commons-cli-1.2-bin.zip does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/commons-codec-1.8-bin.zip ]
then
	echo "commons-codec-1.8-bin.zip does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/commons-daemon-1.0.15-bin.zip ]
then
	echo "prereqs/commons-daemon-1.0.15-bin.zip does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/GAIANDB-1.5.zip ]
then
	echo "GAIAN-1.5.zip does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/mqtt-client-0.4.0.jar ]
then
	echo "mqtt-client-0.4.0.jar does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/mqttws31.js ]
then
	echo "mqttws31.js does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/jackson-annotations-2.2.3.jar ]
then
	echo "jackson-annotations-2.2.3.jar does not exist"
	missing_prereqs=true
fi
	
if [ ! -e prereqs/jackson-core-2.2.3.jar ]
then
	echo "jackson-core-2.2.3.jar does not exist"
	missing_prereqs=true
fi
	
if [ ! -e prereqs/jackson-databind-2.2.3.jar ]
then
	echo "jackson-databind-2.2.3.jar does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/jetty-distribution-9.1.5.v20140505.zip ]
then
	echo "jetty-distribution-9.1.5.v20140505.zip does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/d3.zip ]
then
	echo "d3.zip does not exist"
	missing_prereqs=true
fi

if [ ! -e prereqs/jquery-1.8.2.min.js ]
then
	echo "jquery-1.8.2.min.js does not exist"
	missing_prereqs=true
fi


if  $missing_prereqs
then
	echo "exiting"
	exit 0
fi

# **************************************************************************
# Unpack and deploy packages
# **************************************************************************

echo "Unpacking and deploying Derby"
unzip -q -o prereqs/db-derby-10.8.3.0-bin.zip -d prereqs/derby_temp/
cp -r prereqs/derby_temp/db-derby-10.8.3.0-bin/lib/derbyclient.jar osgi/derbyclient.jar
cp -r prereqs/derby_temp/db-derby-10.8.3.0-bin/lib/derbyclient.jar prereqs/webapps/WEB-INF/lib/derbyclient.jar
cp -r prereqs/derby_temp/db-derby-10.8.3.0-bin/* lib/db-derby/.
rm -rf prereqs/derby_temp

echo "Unpacking and deploying Equinox"
unzip -q -o prereqs/equinox-SDK-KeplerSR2.zip -d prereqs/equinox_temp/
cp -r prereqs/equinox_temp/plugins/org.eclipse.osgi.services_3.3.100.v20130513-1956.jar osgi/org.eclipse.osgi.services_3.3.100.v20130513-1956.jar > nul
cp -r prereqs/equinox_temp/plugins/org.eclipse.equinox.common_3.6.200.v20130402-1505.jar osgi/org.eclipse.equinox.common_3.6.200.v20130402-1505.jar > nul
cp -r prereqs/equinox_temp/plugins/org.eclipse.osgi_3.9.1.v20140110-1610.jar osgi/org.eclipse.osgi_3.9.1.v20140110-1610.jar > nul
cp -r prereqs/equinox_temp/plugins/org.apache.commons.codec_1.4.0.v201209201156.jar osgi/org.apache.commons.codec_1.4.0.v201209201156.jar > nul
rm -rf prereqs/equinox_temp/

echo "Unpacking and deploying Commons CLI"
unzip -q -o prereqs/commons-cli-1.2-bin.zip -d prereqs/commons_temp/
cp -r prereqs/commons_temp/commons-cli-1.2/commons-cli-1.2.jar lib/oslib/commons-cli-1.2.jar
cp -r prereqs/commons_temp/commons-cli-1.2/commons-cli-1.2.jar prereqs/webapps/WEB-INF/lib/commons-cli-1.2.jar
rm -rf prereqs/commons_temp/

echo "Unpacking and deploying Commons Codec"
unzip -q -o prereqs/commons-codec-1.8-bin.zip -d prereqs/commons_temp/
cp -r prereqs/commons_temp/commons-codec-1.8/commons-codec-1.8.jar lib/oslib/commons-codec-1.8.jar
cp -r prereqs/commons_temp/commons-codec-1.8/commons-codec-1.8.jar osgi/commons-codec-1.8.jar
cp -r prereqs/commons_temp/commons-codec-1.8/commons-codec-1.8.jar prereqs/webapps/WEB-INF/lib/commons-codec-1.8.jar
rm -rf prereqs/commons_temp/

echo "Unpacking and deploying Commons Daemon"
unzip -q -o prereqs/commons-daemon-1.0.15-bin.zip -d prereqs/commons_temp/
cp -r prereqs/commons_temp/commons-daemon-1.0.15/commons-daemon-1.0.15.jar lib/oslib/commons-daemon-1.0.15.jar
rm -rf prereqs/commons_temp/

echo "Unpacking and deploying Gaian"
unzip -q -o prereqs/GAIANDB-1.5.zip -d prereqs/gaiandb 
cp -r prereqs/gaiandb lib
rm -rf prereqs/gaian_temp/

echo "Unpacking and deploying Eclipse"
unzip -q -o prereqs/eclipse-platform-3.7.2-win32.zip -d prereqs/eclipse_temp/
cp -r prereqs/eclipse_temp/eclipse/plugins/org.eclipse.update.configurator_3.3.100.v20100512.jar osgi/org.eclipse.update.configurator_3.3.100.v20100512.jar > nul
rm -rf prereqs/eclipse_temp/

echo "Unpacking and deploying D3"
mkdir -p prereqs/d3_temp
unzip -q -o prereqs/d3.zip -d prereqs/d3_temp/d3
cp -r prereqs/d3_temp/d3/d3.js prereqs/webapps/js/d3.js
rm -rf prereqs/d3_temp/


echo "Unpacking and deploying mqtt"
cp -r prereqs/mqtt-client-0.4.0.jar lib/oslib/mqtt-client-0.4.0.jar
cp -r prereqs/mqtt-client-0.4.0.jar prereqs/bundles/bundle.paho.mqtt/jars/mqtt-client-0.4.0.jar
cd prereqs/bundles/bundle.paho.mqtt
jar cfm ../../../osgi/bundle.paho.mqtt.jar META-INF/MANIFEST.MF .
cd ../../..

echo "Unpacking and deploying Jackson"
cp -r prereqs/jackson-annotations-2.2.3.jar lib/oslib/jackson-annotations-2.2.3.jar
cp -r prereqs/jackson-annotations-2.2.3.jar prereqs/bundles/bundle.jackson/jars/jackson-annotations-2.2.3.jar
cp -r prereqs/jackson-core-2.2.3.jar lib/oslib/jackson-core-2.2.3.jar
cp -r prereqs/jackson-core-2.2.3.jar prereqs/bundles/bundle.jackson/jars/jackson-core-2.2.3.jar
cp -r prereqs/jackson-databind-2.2.3.jar lib/oslib/jackson-databind-2.2.3.jar
cp -r prereqs/jackson-databind-2.2.3.jar prereqs/bundles/bundle.jackson/jars/jackson-databind-2.2.3.jar
cd prereqs/bundles/bundle.jackson
jar cfm ../../../osgi/bundle.jackson.jar META-INF/MANIFEST.MF .
cd ../../..

echo "Unpacking and deploying Jetty"
unzip -q -o prereqs/jetty-distribution-9.1.5.v20140505.zip -d prereqs/jetty_temp/
cp -r prereqs/jetty_temp/jetty-distribution-9.1.5.v20140505/* web
cp prereqs/jetty_temp/jetty-distribution-9.1.5.v20140505/lib/websocket/websocket-api-9.1.5.v20140505.jar prereqs/webapps/WEB-INF/lib/websocket-api-9.1.5.v20140505.jar
cp prereqs/jetty_temp/jetty-distribution-9.1.5.v20140505/lib/websocket/websocket-api-9.1.5.v20140505.jar lib/oslib/websocket-api-9.1.5.v20140505.jar
cp prereqs/jackson-annotations-2.2.3.jar prereqs/webapps/WEB-INF/lib/jackson-annotations-2.2.3.jar
cp prereqs/jackson-core-2.2.3.jar prereqs/webapps/WEB-INF/lib/jackson-core-2.2.3.jar
cp prereqs/jackson-databind-2.2.3.jar prereqs/webapps/WEB-INF/lib/jackson-databind-2.2.3.jar
cp prereqs/mqtt-client-0.4.0.jar prereqs/webapps/WEB-INF/lib/mqtt-client-0.4.0.jar
cp prereqs/jquery-1.8.2.min.js prereqs/webapps/js/jquery-1.8.2.min.js
cp prereqs/mqttws31.js prereqs/webapps/js/mqttws31.js
jar uf web/webapps/rest.war -C prereqs/webapps .
mkdir -p web/webapps/ROOT
cp prereqs/index.html web/webapps/ROOT/index.html
rm -rf prereqs/jetty_temp/

