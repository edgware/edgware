#!/bin/bash

#!/bin/bash

# **************************************************************************
# (C) Copyright IBM Corp. 2006, 2008
#
# LICENSE: Eclipse Public License v1.0
# http://www.eclipse.org/legal/epl-v10.html
# **************************************************************************

# **************************************************************************
# Tests various environment variables and exits if failures occur
# **************************************************************************

ERROR=0

# Is $FABRIC_HOME set?
if [ ! -n "$FABRIC_HOME"  ]
then
	echo "Environment variable FABRIC_HOME not set"
	ERROR=1
else
	# Does the $FABRIC_HOME directory exist?
	if [ ! -d "$FABRIC_HOME"  ]
	then
		echo "The \$FABRIC_HOME directory does not exist ($FABRIC_HOME)"
		ERROR=1
	fi

	# Does $FABRIC_HOME point to a Fabric installation?
	if [ ! -e "$FABRIC_HOME/fabinstall.sh" ]
	then
		echo "The \$FABRIC_HOME directory does not appear to be a Fabric installation ($FABRIC_HOME)"
		ERROR=1
	fi

    # Is $FABRIC_HOME on the PATH?
    echo $PATH | grep "$FABRIC_HOME/bin/linux" > /dev/null
    if [ $? -eq 1 ]
    then
       echo "\$PATH does not include the \$FABRIC_HOME bin directory ($FABRIC_HOME/bin/linux)"
       ERROR=1
    fi
 fi

# Is $JAVA_HOME set?
if [ ! -n "$JAVA_HOME"  ]
then
	echo "\$JAVA_HOME is not set"
	ERROR=1
else
	# Does $JAVA_HOME exist?
	if [ ! -d "$JAVA_HOME"  ]
	then
		echo "The \$JAVA_HOME directory does not exist ($JAVA_HOME)"
		ERROR=1
	fi

	# Does $JAVA_HOME point to an installed Java?
	if [ ! -e "$JAVA_HOME/bin/java" ]
	then
		echo "The \$JAVA_HOME directory does not appear to be a Java installation ($JAVA_HOME)"
		ERROR=1
	fi
fi

# Check the Fabric has been installed!
# log directory is only created during installation 
if [ ! -d "$FABRIC_HOME/log" ]
then
	echo "Error: Fabric not installed (fabinstall.sh has not been run yet)."
	ERROR=1
fi

# If there were errors exit with a failure
if [ $ERROR -eq 1 ]
then
	echo "Errors were detected; exiting"
	exit 1
fi

# Done
exit 0
