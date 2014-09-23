#!/bin/bash

# **************************************************************************
# Licensed Materials - Property of IBM
#
# (C) Copyright IBM Corp. 2014
#
# LICENSE: Eclipse Public License v1.0
# http://www.eclipse.org/legal/epl-v10.html
# **************************************************************************

function configProperties {
	sed -i "" 's/registry.type=distributed/registry.type=gaian/' "$FABRIC_HOME/osgi/configuration/fabricConfig_default.properties"
}

# **************************************************************************
# Check that the pre-reqs are available
# **************************************************************************

# Set FABRIC_HOME based upon the location of the script (which should be
# <fabric-home>/prereqs)
export FABRIC_HOME=$(dirname $(cd ${0%/*} && echo $PWD/${0##*/}))/..
cd "$FABRIC_HOME"

missing_prereqs=false

if [ ! -e prereqs/GAIANDB-1.5.zip ]
then
	echo "GAIAN-1.5.zip does not exist"
	missing_prereqs=true
fi

if [ "$missing_prereqs" == "true" ]
then
	exit 1
fi

# **************************************************************************
# Unpack and deploy packages
# **************************************************************************

echo "Unpacking and deploying the Gaian Database."
unzip -q -o prereqs/GAIANDB-1.5.zip -d prereqs/gaiandb 
cp -r prereqs/gaiandb lib
rm -rf prereqs/gaian_temp/

# **************************************************************************
# Ensure that the Gaian Database is the default
# **************************************************************************

configProperties

# **************************************************************************
# Now ready to install
# **************************************************************************

echo -e "To install the Fabric now run the following command from the Fabric root"
echo -e "directory ($(pwd)):"
echo -e "\t./fabinstall.sh registry gaian"
