--------------------------------------------------------------------------------
-- Licensed Materials - Property of IBM
--
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

CONNECT 'jdbc:derby://localhost:6414/gaiandb;create=true;user=gaiandb;password=passw0rd;';

-- logical COMPOSITE_SERVICES table
call gaiandb.setltforrdbtable('COMPOSITE_SERVICES', 'FABRIC', 'FABRIC.COMPOSITE_SERVICES');

-- logical COMPOSITE_PARTS table
call gaiandb.setltforrdbtable('COMPOSITE_PARTS', 'FABRIC', 'FABRIC.COMPOSITE_PARTS');

-- logical SERVICE_WIRING table
call gaiandb.setltforrdbtable('SERVICE_WIRING', 'FABRIC', 'FABRIC.SERVICE_WIRING');

DISCONNECT;

CONNECT 'jdbc:derby://localhost:6414/FABRIC;create=true;user=fabric;password=fabric;';

-- COMPOSITE_SERVICES view
create VIEW Fabric.G_COMPOSITE_SERVICES as SELECT * FROM new com.ibm.db2j.GaianTable('COMPOSITE_SERVICES', 'with_provenance') CSLT;

-- COMPOSITE_PARTS view
create VIEW Fabric.G_COMPOSITE_PARTS as SELECT * FROM new com.ibm.db2j.GaianTable('COMPOSITE_PARTS', 'with_provenance') CPLT;

-- SERVICE_WIRING view
create VIEW Fabric.G_SERVICE_WIRING as SELECT * FROM new com.ibm.db2j.GaianTable('SERVICE_WIRING', 'with_provenance') SWLT;

DISCONNECT;

