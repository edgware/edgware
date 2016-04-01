--------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2012
--
-- LICENSE: Eclipse Public License v1.0
-- http://www.eclipse.org/legal/epl-v10.html
--------------------------------------------------------------------------------

CONNECT 'jdbc:derby://localhost:6414/FABRIC;create=true;user=gaiandb;password=passw0rd;';

CREATE TABLE Fabric.Composite_Services (
		ID VARCHAR(32672) NOT NULL,
		Type VARCHAR(32672),
		Affiliation VARCHAR(32672),
		Credentials VARCHAR(32672),
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (ID)
	);

CREATE TABLE Fabric.Composite_Parts (
		Composite_ID VARCHAR(32672) NOT NULL,
		Service_Platform_ID VARCHAR(32672) NOT NULL,
		Service_ID VARCHAR(32672) NOT NULL,
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Composite_ID, Service_Platform_ID, Service_ID)
	);

CREATE TABLE Fabric.Service_Wiring (
		Composite_ID VARCHAR(32672) NOT NULL,
		From_Service_Platform_ID VARCHAR(32672) NOT NULL,
		From_Service_ID VARCHAR(32672) NOT NULL,
		From_Interface_ID VARCHAR(32672) NOT NULL,
		To_Service_Platform_ID VARCHAR(32672) NOT NULL,
		To_Service_ID VARCHAR(32672) NOT NULL,
		To_Interface_ID VARCHAR(32672) NOT NULL,
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Composite_ID, From_Service_Platform_ID, From_Service_ID, From_Interface_ID, To_Service_Platform_ID, To_Service_ID, To_Interface_ID)
	);

DISCONNECT;

