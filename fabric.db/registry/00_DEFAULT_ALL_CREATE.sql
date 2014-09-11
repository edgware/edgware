--------------------------------------------------------------------------------
-- Licensed Materials - Property of IBM
--
-- (C) Copyright IBM Corp. 2006, 2014
--
-- LICENSE: Eclipse Public License v1.0
-- http://www.eclipse.org/legal/epl-v10.html
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- Create the Fabric Registry database, if it does not already exist.
-- If it does, deletes the views that reference the Gaian logical tables.
--------------------------------------------------------------------------------

-- Connect to the FABRIC database using the newly authorised credentials
CONNECT 'jdbc:derby://localhost:6414/FABRIC;create=true;user=gaiandb;password=passw0rd;';

-- Authorise the "fabric" user to access this database
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.user.fabric', 'fabric');

--------------------------------------------------------------------------------
-- CREATE all Registry tables
--------------------------------------------------------------------------------

CREATE TABLE Fabric.Data_Feeds (
		Platform_ID VARCHAR(32672) NOT NULL,
		Service_ID VARCHAR(32672) NOT NULL,
		ID VARCHAR(32672) NOT NULL,
		Type_ID VARCHAR(32672) NOT NULL,
		Direction VARCHAR(32672) DEFAULT 'OUTPUT' NOT NULL,
		Credentials VARCHAR(32672),
		Availability VARCHAR(32672),
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Platform_ID, Service_ID, ID),
		CONSTRAINT Direction_CK CHECK (Direction IN ('input', 'output', 'solicit_response', 'request_response', 'notification', 'one_way'))
	);

CREATE TABLE Fabric.Fablet_Plugins (
		Node_ID VARCHAR(32672) NOT NULL,
		Name VARCHAR(32672) NOT NULL,
		Family VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Arguments VARCHAR(32672),
		PRIMARY KEY (Node_ID, Name, Family)
	);

CREATE TABLE Fabric.System_Plugins (
		Node_ID VARCHAR(32672) NOT NULL,
		Name VARCHAR(32672) NOT NULL,
		Family VARCHAR(32672) NOT NULL,
		Type VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Arguments VARCHAR(32672),
		PRIMARY KEY (Node_ID, Name, Family, Type)
	);

CREATE TABLE Fabric.Node_Neighbours (
		Node_ID VARCHAR(32672) NOT NULL,
		Node_Interface VARCHAR(32) NOT NULL,
		Neighbour_ID VARCHAR(32672) NOT NULL,
		Neighbour_Interface VARCHAR(32) NOT NULL,	
		DiscoveredBy VARCHAR(32672),
		Availability VARCHAR(32672),	
		Bearer_ID VARCHAR(32672),
		Connection_Attributes VARCHAR(32672),
		Connection_Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Node_ID, Node_Interface, Neighbour_ID, Neighbour_Interface)
	);

CREATE TABLE Fabric.Actors (
		Actor_ID VARCHAR(32672) NOT NULL,
		Type_ID VARCHAR(32672) NOT NULL,
		Affiliation VARCHAR(32672),
		Roles VARCHAR(32672),
		Credentials VARCHAR(32672),
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Actor_ID)
	);

CREATE TABLE Fabric.Feed_Types (
		Type_ID VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Type_ID)
	);

CREATE TABLE Fabric.Nodes (
		Node_ID VARCHAR(32672) NOT NULL,
		Type_ID VARCHAR(32672) NOT NULL,
		Affiliation VARCHAR(32672),
		Credentials VARCHAR(32672),
		Readiness VARCHAR(32672),
		Availability VARCHAR(32672),
		Latitude DOUBLE,
		Longitude DOUBLE,
		Altitude DOUBLE,
		Bearing DOUBLE,
		Velocity DOUBLE,
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Node_ID)
	);

CREATE TABLE Fabric.Task_Nodes (
		Task_ID VARCHAR(32672) NOT NULL,
		Node_ID VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Configuration VARCHAR(32672),
		Configuration_URI VARCHAR(32672),
		PRIMARY KEY (Task_ID, Node_ID)
	);

CREATE TABLE Fabric.Task_Plugins (
		Node_ID VARCHAR(32672) NOT NULL,
		Task_ID VARCHAR(32672) NOT NULL,
		Name VARCHAR(32672) NOT NULL,
		Family VARCHAR(32672) NOT NULL,
		Type VARCHAR(32672) DEFAULT 'INBOUND' NOT NULL,
		Ordinal INTEGER NOT NULL,
		Platform_ID VARCHAR(32672) NOT NULL,
		Service_ID VARCHAR(32672) NOT NULL,
		Data_Feed_ID VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Arguments VARCHAR(32672),
		PRIMARY KEY (Node_ID, Task_ID, Ordinal, Type, Name, Family, Platform_ID, Service_ID, Data_Feed_ID),
		CONSTRAINT Task_Plugins_CK CHECK (Type IN ('INBOUND', 'OUTBOUND'))
	);

CREATE TABLE Fabric.Actor_Types (
		Type_ID VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Type_ID)
	);

CREATE TABLE Fabric.Node_Plugins (
		Node_ID VARCHAR(32672) NOT NULL,
		Name VARCHAR(32672) NOT NULL,
		Family VARCHAR(32672) NOT NULL,
		Type VARCHAR(32672) DEFAULT 'INBOUND' NOT NULL,
		Ordinal INTEGER NOT NULL,
		Description VARCHAR(32672),
		Arguments VARCHAR(32672),
		PRIMARY KEY (Node_ID, Ordinal, Type, Family, Name),
		CONSTRAINT Node_Plugins_CK CHECK (Type IN ('INBOUND', 'OUTBOUND'))
	);

CREATE TABLE Fabric.Node_Types (
		Type_ID VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Type_ID)
	);

CREATE TABLE Fabric.Task_Subscriptions (
		Task_ID VARCHAR(32672) NOT NULL,
		Actor_ID VARCHAR(32672) NOT NULL,
		Platform_ID VARCHAR(32672) NOT NULL,
		Service_ID VARCHAR(32672) NOT NULL,
		Data_Feed_ID VARCHAR(32672) NOT NULL,
		Actor_Platform_ID VARCHAR(32672) NOT NULL,
		PRIMARY KEY (Task_ID, Actor_ID, Platform_ID, Service_ID, Data_Feed_ID, Actor_Platform_ID)
	);

CREATE TABLE Fabric.Node_IP_Mapping (
		Node_ID VARCHAR(32672) NOT NULL,
		Node_Interface VARCHAR(32) NOT NULL,
		IP VARCHAR(32672) NOT NULL,
		Port INTEGER NOT NULL,
		PRIMARY KEY (Node_ID,Node_Interface)
	);
	
CREATE TABLE Fabric.Bearers (
		Bearer_ID VARCHAR(32672) NOT NULL,
		Availability VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Bearer_ID),
		CONSTRAINT Bearers_CK CHECK (Availability IN ('AVAILABLE', 'UNAVAILABLE', 'UNKNOWN'))
	);
	
CREATE TABLE Fabric.Task_Services (
		Task_ID VARCHAR(32672) NOT NULL,
		Platform_ID VARCHAR(32672) NOT NULL,
		Service_ID VARCHAR(32672) NOT NULL,
		Data_Feed_ID VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Configuration_URI VARCHAR(32672),
		Configuration VARCHAR(32672),
		PRIMARY KEY (Task_ID, Platform_ID, Service_ID, Data_Feed_ID)
	);

CREATE TABLE Fabric.Actor_Plugins (
		Node_ID VARCHAR(32672) NOT NULL,
		Task_ID VARCHAR(32672) NOT NULL,
		Actor_ID VARCHAR(32672) NOT NULL,
		Name VARCHAR(32672) NOT NULL,
		Family VARCHAR(32672) NOT NULL,
		Type VARCHAR(32672) DEFAULT 'INBOUND' NOT NULL,
		Ordinal INTEGER NOT NULL,
		Platform_ID VARCHAR(32672) NOT NULL,
		Service_ID VARCHAR(32672) NOT NULL,
		Data_Feed_ID VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Arguments VARCHAR(32672),
		PRIMARY KEY (Node_ID, Task_ID, Actor_ID, Ordinal, Type, Name, Family, Platform_ID, Service_ID, Data_Feed_ID),
		CONSTRAINT Actor_Plugins_CK CHECK (Type IN ('INBOUND', 'OUTBOUND'))
	);

CREATE TABLE Fabric.Platforms (
		Platform_ID VARCHAR(32672) NOT NULL,
		Type_ID VARCHAR(32672) NOT NULL,
		Node_ID VARCHAR(32672),
		Affiliation VARCHAR(32672),
		Credentials VARCHAR(32672),
		Readiness VARCHAR(32672),
		Availability VARCHAR(32672),
		Latitude DOUBLE,
		Longitude DOUBLE,
		Altitude DOUBLE,
		Bearing DOUBLE,
		Velocity DOUBLE,
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Platform_ID)
	);

CREATE TABLE Fabric.Services (
		Platform_ID VARCHAR(32672) NOT NULL,
		ID VARCHAR(32672) NOT NULL,
		Type_ID VARCHAR(32672),
		Kind VARCHAR(32672) DEFAULT 'SENSOR' NOT NULL,
		Credentials VARCHAR(32672),
		Readiness VARCHAR(32672),
		Availability VARCHAR(32672),
		Latitude DOUBLE,
		Longitude DOUBLE,
		Altitude DOUBLE,
		Bearing DOUBLE,
		Velocity DOUBLE,
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Platform_ID, ID),
		CONSTRAINT Kind_CK CHECK (Kind IN ('SENSOR', 'SERVICE'))		
	);

CREATE TABLE Fabric.Platform_Types (
		Type_ID VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Type_ID)
	);

CREATE TABLE Fabric.Routes (
		Start_Node_ID VARCHAR(32672) NOT NULL,
		End_Node_ID VARCHAR(32672) NOT NULL,
		Ordinal SMALLINT NOT NULL,
		Route VARCHAR(32672) NOT NULL,
		PRIMARY KEY (Start_Node_ID, End_Node_ID, Ordinal)
	);

CREATE TABLE Fabric.Service_Types (
		Type_ID VARCHAR(32672) NOT NULL,
		Description VARCHAR(32672),
		Attributes VARCHAR(32672),
		Attributes_URI VARCHAR(32672),
		PRIMARY KEY (Type_ID)
	);

CREATE TABLE Fabric.Tasks (
		Task_ID VARCHAR(32672) NOT NULL,
		Priority INTEGER,
		Affiliation VARCHAR(32672),
		Description VARCHAR(32672),
		Task_Detail VARCHAR(32672),
		Task_Detail_URI VARCHAR(32672),
		PRIMARY KEY (Task_ID)
	);

-- Default configuration
CREATE TABLE Fabric.Default_Config (
		Name VARCHAR(32672) NOT NULL,
		Value VARCHAR(32672),
		PRIMARY KEY (Name)
	);
	
-- Node specific configuration (overrides default)
CREATE TABLE Fabric.Node_Config (
		Node_ID VARCHAR(32672) NOT NULL,
		Name VARCHAR(32672) NOT NULL,
		Value VARCHAR(32672),
		PRIMARY KEY (Node_ID, Name)
	);

-- Platform specific configuration (overrides node)
CREATE TABLE Fabric.Platform_Config (
		Platform_ID VARCHAR(32672) NOT NULL,
		Name VARCHAR(32672) NOT NULL,
		Value VARCHAR(32672),
		PRIMARY KEY (Platform_ID, Name)
);
	
-- Actor by platform configuration (overrides platform and includes actor-specific config)
CREATE TABLE Fabric.Actor_Config (
		Actor_ID VARCHAR(32672) NOT NULL,
		Platform_ID VARCHAR(32672) NOT NULL,
		Name VARCHAR(32672) NOT NULL,
		Value VARCHAR(32672),
		PRIMARY KEY (Actor_ID, Platform_ID, Name)
);
	
DISCONNECT;
EXIT;

