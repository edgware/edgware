@ECHO OFF 

REM **************************************************************************
REM Licensed Materials - Property of IBM
REM
REM (C) Copyright IBM Corp. 2014
REM
REM LICENSE: Eclipse Public License v1.0
REM http://www.eclipse.org/legal/epl-v10.html
REM **************************************************************************

REM **************************************************************************
REM Check that the pre-reqs are available
REM **************************************************************************

if not exist "%ZIP%" (
	echo Environment variable ZIP not set
	goto:EOF
)

SET OUTPUT_FILE= "fabprereqs_zip.out"

SET have_prereqs=0

if not exist "prereqs\db-derby-10.8.3.0-bin.zip" (
	echo db-derby-10.8.3.0-bin.zip does not exist
	SET have_prereqs=1
)

if not exist "prereqs\equinox-SDK-KeplerSR2.zip" (
	echo equinox-SDK-KeplerSR2.zip does not exist
	SET have_prereqs=1
)

if not exist "prereqs\eclipse-platform-3.7.2-win32.zip" (
	echo eclipse-platform-3.7.2-win32.zip does not exist
	SET have_prereqs=1
)

if not exist "prereqs\commons-cli-1.2-bin.zip" (
	echo commons-cli-1.2-bin.zip does not exist
	SET have_prereqs=1
)

if not exist "prereqs\commons-codec-1.8-bin.zip" (
	echo commons-codec-1.8-bin.zip does not exist
	SET have_prereqs=1
)

if not exist "prereqs\commons-daemon-1.0.15-bin.zip" (
	echo commons-daemon-1.0.15-bin.zip does not exist
	SET have_prereqs=1
)

if not exist "prereqs\GAIANDB-1.5.zip" (
	echo GAIAN-1.5.zip does not exist
	SET have_prereqs=1
)

if not exist "prereqs\mqtt-client-0.4.0.jar" (
	echo mqtt-client-0.4.0.jar does not exist
	SET have_prereqs=1
)

if not exist "prereqs\mqttws31.js" (
	echo mqttws31.js does not exist
	SET have_prereqs=1
)

if not exist "prereqs\jackson-annotations-2.2.3.jar" (
	echo jackson-annotations-2.2.3.jar does not exist
	SET have_prereqs=1
)

if not exist "prereqs\jackson-core-2.2.3.jar" (
	echo jackson-core-2.2.3.jar does not exist
	SET have_prereqs=1
)

if not exist "prereqs\jackson-databind-2.2.3.jar" (
	echo jackson-databind-2.2.3.jar does not exist
	SET have_prereqs=1
)

if not exist "prereqs\jetty-distribution-9.1.5.v20140505.zip" (
	echo jetty-distribution-9.1.5.v20140505.zip does not exist
	SET have_prereqs=1
)

if not exist "prereqs\d3.zip" (
	echo d3.zip does not exist
	SET have_prereqs=1
)

if not exist "prereqs\jquery-1.8.2.min.js" (
	echo jquery-1.8.2.min.js does not exist
	SET have_prereqs=1
)

if %have_prereqs% EQU 1 (
echo Exiting
	GOTO:EOF
)

REM **************************************************************************
REM Unpack and deploy packages
REM **************************************************************************

echo Unpacking and deploying Derby
"%ZIP%" x prereqs\db-derby-10.8.3.0-bin.zip -oprereqs\derby_temp\ -y  > %OUTPUT_FILE%
copy /y prereqs\derby_temp\db-derby-10.8.3.0-bin\lib\derbyclient.jar osgi\derbyclient.jar
copy /y prereqs\derby_temp\db-derby-10.8.3.0-bin\lib\derbyclient.jar prereqs\webapps\WEB-INF\lib\derbyclient.jar
xcopy prereqs\derby_temp\db-derby-10.8.3.0-bin lib\db-derby /s /e /h /y /q
rmdir "prereqs\derby_temp\" /s /q

echo Unpacking and deploying Equinox
"%ZIP%" x "prereqs\equinox-SDK-KeplerSR2.zip" -o"prereqs\equinox_temp\" -y >> %OUTPUT_FILE%
copy /y prereqs\equinox_temp\plugins\org.eclipse.osgi.services_3.3.100.v20130513-1956.jar osgi\org.eclipse.osgi.services_3.3.100.v20130513-1956.jar
copy /y prereqs\equinox_temp\plugins\org.eclipse.equinox.common_3.6.200.v20130402-1505.jar osgi\org.eclipse.equinox.common_3.6.200.v20130402-1505.jar
copy /y prereqs\equinox_temp\plugins\org.eclipse.osgi_3.9.1.v20140110-1610.jar osgi\org.eclipse.osgi_3.9.1.v20140110-1610.jar
copy /y prereqs\equinox_temp\plugins\org.apache.commons.codec_1.4.0.v201209201156.jar osgi\org.apache.commons.codec_1.4.0.v201209201156.jar
rmdir "prereqs\equinox_temp\" /s /q

echo Unpacking and deploying Commons CLI
"%ZIP%" x "prereqs\commons-cli-1.2-bin.zip" -o"prereqs\commons_temp\" -y >> %OUTPUT_FILE%
copy /y prereqs\commons_temp\commons-cli-1.2\commons-cli-1.2.jar lib\oslib\commons-cli-1.2.jar
copy /y prereqs\commons_temp\commons-cli-1.2\commons-cli-1.2.jar prereqs\webapps\WEB-INF\lib\commons-cli-1.2.jar
rmdir "prereqs\commons_temp\" /s /q

echo Unpacking and deploying Commons Codec
"%ZIP%" x "prereqs\commons-codec-1.8-bin.zip" -o"prereqs\commons_temp\" -y >> %OUTPUT_FILE%
copy /y prereqs\commons_temp\commons-codec-1.8\commons-codec-1.8.jar lib\oslib\commons-codec-1.8.jar
copy /y prereqs\commons_temp\commons-codec-1.8\commons-codec-1.8.jar osgi\commons-codec-1.8.jar
copy /y prereqs\commons_temp\commons-codec-1.8\commons-codec-1.8.jar prereqs\webapps\WEB-INF\lib\commons-codec-1.8.jar
rmdir "prereqs\commons_temp\" /s /q

echo Unpacking and deploying Commons DAEMON
"%ZIP%" x "prereqs\commons-daemon-1.0.15-bin.zip" -o"prereqs\commons_temp\" -y >> %OUTPUT_FILE%
copy /y prereqs\commons_temp\commons-daemon-1.0.15\commons-daemon-1.0.15.jar lib\oslib\commons-daemon-1.0.15.jar
rmdir "prereqs\commons_temp\" /s /q

echo Unpacking and deploying GAIAN
"%ZIP%" x "prereqs\GAIANDB-1.5.zip" -o"prereqs\gaian_temp\gaiandb\" -y  >> %OUTPUT_FILE%
xcopy prereqs\gaian_temp\gaiandb lib\gaiandb /s /e /h /y /q
rmdir "prereqs\gaian_temp\" /s /q

echo Unpacking and deploying Eclipse
"%ZIP%" x "prereqs\eclipse-platform-3.7.2-win32.zip" -o"prereqs\eclipse_temp\" -y  >> %OUTPUT_FILE%
copy /y prereqs\eclipse_temp\eclipse\plugins\org.eclipse.update.configurator_3.3.100.v20100512.jar osgi\org.eclipse.update.configurator_3.3.100.v20100512.jar
rmdir "prereqs\eclipse_temp\" /s /q

echo Unpacking and deploying D3
"%ZIP%" x "prereqs\d3.zip" -o"prereqs\d3_temp\d3\" -y >> %OUTPUT_FILE%
copy /y prereqs\d3_temp\d3\d3.js prereqs\webapps\js\d3.js
rmdir "prereqs\d3_temp\" /s /q


echo Unpacking and deploying MQTT
copy /y "prereqs\mqtt-client-0.4.0.jar" "lib\oslib\mqtt-client-0.4.0.jar"
copy /y "prereqs\mqtt-client-0.4.0.jar" "prereqs\bundles\bundle.paho.mqtt\jars\mqtt-client-0.4.0.jar"
cd prereqs\bundles\bundle.paho.mqtt
jar cfm ../../../osgi/bundle.paho.mqtt.jar META-INF/MANIFEST.MF .
cd ../../..

echo Unpacking and deploying Jackson
copy /y "prereqs\jackson-annotations-2.2.3.jar" "lib\oslib\jackson-annotations-2.2.3.jar"
copy /y "prereqs\jackson-annotations-2.2.3.jar" "prereqs\bundles\bundle.jackson\jars\jackson-annotations-2.2.3.jar"
copy /y "prereqs\jackson-core-2.2.3.jar" "lib\oslib\jackson-core-2.2.3.jar"
copy /y "prereqs\jackson-core-2.2.3.jar" "prereqs\bundles\bundle.jackson\jars\jackson-core-2.2.3.jar"
copy /y "prereqs\jackson-databind-2.2.3.jar" "lib\oslib\jackson-databind-2.2.3.jar"
copy /y "prereqs\jackson-databind-2.2.3.jar" "prereqs\bundles\bundle.jackson\jars\jackson-databind-2.2.3.jar"
cd prereqs/bundles/bundle.jackson
jar cfm ../../../osgi/bundle.jackson.jar META-INF/MANIFEST.MF .
cd ../../..

echo Unpacking and deploying jetty
"%ZIP%" x "prereqs\jetty-distribution-9.1.5.v20140505.zip" -o"prereqs\jetty_temp\" -y  >> %OUTPUT_FILE%
xcopy prereqs\jetty_temp\jetty-distribution-9.1.5.v20140505 web /s /e /h /y /q
copy /y "prereqs\jetty_temp\jetty-distribution-9.1.5.v20140505\lib\websocket\websocket-api-9.1.5.v20140505.jar" "prereqs\webapps\WEB-INF\lib\websocket-api-9.1.5.v20140505.jar"
copy /y "prereqs\jetty_temp\jetty-distribution-9.1.5.v20140505\lib\websocket\websocket-api-9.1.5.v20140505.jar" "lib\oslib\websocket-api-9.1.5.v20140505.jar"
copy /y "prereqs\jackson-annotations-2.2.3.jar" "prereqs\webapps\WEB-INF\lib\jackson-annotations-2.2.3.jar"
copy /y "prereqs\jackson-core-2.2.3.jar" "prereqs\webapps\WEB-INF\lib\jackson-core-2.2.3.jar"
copy /y "prereqs\jackson-databind-2.2.3.jar" "prereqs\webapps\WEB-INF\lib\jackson-databind-2.2.3.jar"
copy /y "prereqs\mqtt-client-0.4.0.jar" "prereqs\webapps\WEB-INF\lib\mqtt-client-0.4.0.jar"
copy /y "prereqs\jquery-1.8.2.min.js" "prereqs\webapps\js\jquery-1.8.2.min.js"
copy /y "prereqs\mqttws31.js" "prereqs\webapps\js\mqttws31.js"
jar uf web/webapps/rest.war -C prereqs\webapps .

if not exist web\webapps\ROOT (
	mkdir web\webapps\ROOT
)
copy /y "prereqs\index.html" "web\webapps\ROOT\index.html"
rmdir "prereqs\jetty_temp\" /s /q



