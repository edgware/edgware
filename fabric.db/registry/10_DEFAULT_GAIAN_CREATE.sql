--------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2006, 2014
--
-- LICENSE: Eclipse Public License v1.0
-- http://www.eclipse.org/legal/epl-v10.html
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- Creates logical table definitions in the Gaian Database for the Fabric
-- Registry tables.
--
-- This script must be run against the Gaian Database.
--------------------------------------------------------------------------------
	
-- Connect to the Gaian Database
CONNECT 'jdbc:derby://localhost:6414/gaiandb;create=true;user=gaiandb;password=passw0rd;';

-- Set the rdb conn using the previously authorised 'fabric' user ID.
-- Use EmbeddedDriver instead (slight gain in performance).
--call gaiandb.setrdbc('FABRIC', 'org.apache.derby.jdbc.ClientDriver', 'jdbc:derby://localhost:6414/FABRIC;create=true;', 'fabric', 'fabric');
call gaiandb.setrdbc('FABRIC', 'org.apache.derby.jdbc.EmbeddedDriver', 'jdbc:derby://localhost:6414/FABRIC;create=true;', 'fabric', 'fabric');

-- Logical FEEDS table
call gaiandb.setltforrdbtable('DATA_FEEDS', 'FABRIC', 'FABRIC.DATA_FEEDS');

-- Logical TASK_PLUGINS table
call gaiandb.setltforrdbtable('TASK_PLUGINS', 'FABRIC', 'FABRIC.TASK_PLUGINS');

-- Logical TASK_SUBSCRIPTIONS table
call gaiandb.setltforrdbtable('TASK_SUBSCRIPTIONS', 'FABRIC', 'FABRIC.TASK_SUBSCRIPTIONS');

-- Logical NODES table
call gaiandb.setltforrdbtable('NODES', 'FABRIC', 'FABRIC.NODES');

-- Logical ROUTES table
call gaiandb.setltforrdbtable('ROUTES', 'FABRIC', 'FABRIC.ROUTES');

-- Logical SERVICES table
call gaiandb.setltforrdbtable('SERVICES', 'FABRIC', 'FABRIC.SERVICES');

-- Logical ACTOR_PLUGINS table
call gaiandb.setltforrdbtable('ACTOR_PLUGINS', 'FABRIC', 'FABRIC.ACTOR_PLUGINS');

-- Logical NODE_TYPES table
call gaiandb.setltforrdbtable('NODE_TYPES', 'FABRIC', 'FABRIC.NODE_TYPES');

-- Logical PLATFORM_TYPES table
call gaiandb.setltforrdbtable('PLATFORM_TYPES', 'FABRIC', 'FABRIC.PLATFORM_TYPES');

-- Logical NODE_PLUGINS table
call gaiandb.setltforrdbtable('NODE_PLUGINS', 'FABRIC', 'FABRIC.NODE_PLUGINS');

-- Logical ACTORS table
call gaiandb.setltforrdbtable('ACTORS', 'FABRIC', 'FABRIC.ACTORS');

-- Logical TASK_NODES table
call gaiandb.setltforrdbtable('TASK_NODES', 'FABRIC', 'FABRIC.TASK_NODES');

-- Logical FEED_TYPES table
call gaiandb.setltforrdbtable('FEED_TYPES', 'FABRIC', 'FABRIC.FEED_TYPES');

-- Logical ACTOR_TYPES table
call gaiandb.setltforrdbtable('ACTOR_TYPES', 'FABRIC', 'FABRIC.ACTOR_TYPES');

-- Logical TASKS table
call gaiandb.setltforrdbtable('TASKS', 'FABRIC', 'FABRIC.TASKS');

-- Logical SERVICE_TYPES table
call gaiandb.setltforrdbtable('SERVICE_TYPES', 'FABRIC', 'FABRIC.SERVICE_TYPES');

-- Logical PLATFORMS table
call gaiandb.setltforrdbtable('PLATFORMS', 'FABRIC', 'FABRIC.PLATFORMS');

-- Logical FABLET_PLUGINS table
call gaiandb.setltforrdbtable('FABLET_PLUGINS', 'FABRIC', 'FABRIC.FABLET_PLUGINS');

-- Logical SYSTEM_PLUGINS table
call gaiandb.setltforrdbtable('SYSTEM_PLUGINS', 'FABRIC', 'FABRIC.SYSTEM_PLUGINS');

-- Logical NODE_NEIGHBOURS table
call gaiandb.setltforrdbtable('NODE_NEIGHBOURS', 'FABRIC', 'FABRIC.NODE_NEIGHBOURS');

-- Logical TASK_SERVICES table
call gaiandb.setltforrdbtable('TASK_SERVICES', 'FABRIC', 'FABRIC.TASK_SERVICES');

-- Logical NODE_IP_MAPPING table
call gaiandb.setltforrdbtable('NODE_IP_MAPPING', 'FABRIC', 'FABRIC.NODE_IP_MAPPING');

-- Logical BEARERS table
call gaiandb.setltforrdbtable('BEARERS', 'FABRIC', 'FABRIC.BEARERS');

--call gaiandb.listlts();

DISCONNECT;

--------------------------------------------------------------------------------
-- Creates the views used by the Registry API for all Gaian distributed queries.
--
-- This script must be run against the FABRIC database.
--------------------------------------------------------------------------------

-- Connect back to the FABRIC database to create the required views - use authorised fabric/fabric this time
CONNECT 'jdbc:derby://localhost:6414/FABRIC;create=true;user=fabric;password=fabric;';

-- FEED view
create VIEW Fabric.G_DATA_FEEDS as SELECT * FROM new com.ibm.db2j.GaianTable('DATA_FEEDS', 'with_provenance') FLT;

-- TASK_PLUGINS view
create VIEW Fabric.G_TASK_PLUGINS as SELECT * FROM new com.ibm.db2j.GaianTable('TASK_PLUGINS', 'with_provenance') TPLT;

-- TASK_SUBSCRIPTION view
create VIEW Fabric.G_TASK_SUBSCRIPTIONS as SELECT * FROM new com.ibm.db2j.GaianTable('TASK_SUBSCRIPTIONS', 'with_provenance') MCLT;

-- NODES view
create VIEW Fabric.G_NODES as SELECT * FROM new com.ibm.db2j.GaianTable('NODES', 'with_provenance') NLT;

-- ROUTES view
create VIEW Fabric.G_ROUTES as SELECT * FROM new com.ibm.db2j.GaianTable('ROUTES', 'with_provenance') RLT;

-- SERVICES view
create VIEW Fabric.G_SERVICES as SELECT * FROM new com.ibm.db2j.GaianTable('SERVICES', 'with_provenance') SLT;

-- ACTOR_PLUGINS view
create VIEW Fabric.G_ACTOR_PLUGINS as SELECT * FROM new com.ibm.db2j.GaianTable('ACTOR_PLUGINS', 'with_provenance') APLT;

-- NODE_TYPES view
create VIEW Fabric.G_NODE_TYPES as SELECT * FROM new com.ibm.db2j.GaianTable('NODE_TYPES', 'with_provenance') NTLT;

-- PLATFORM_TYPES view
create VIEW Fabric.G_PLATFORM_TYPES as SELECT * FROM new com.ibm.db2j.GaianTable('PLATFORM_TYPES', 'with_provenance') PTLT;

-- NODE_PLUGINS view
create VIEW Fabric.G_NODE_PLUGINS as SELECT * FROM new com.ibm.db2j.GaianTable('NODE_PLUGINS', 'with_provenance') NPLT;

-- ACTORS view
create VIEW Fabric.G_ACTORS as SELECT * FROM new com.ibm.db2j.GaianTable('ACTORS', 'with_provenance') CLT;

-- TASK_NODES view
create VIEW Fabric.G_TASK_NODES as SELECT * FROM new com.ibm.db2j.GaianTable('TASK_NODES', 'with_provenance') MNLT;

-- FEED_TYPES view
create VIEW Fabric.G_FEED_TYPES as SELECT * FROM new com.ibm.db2j.GaianTable('FEED_TYPES', 'with_provenance') FTLT;

-- ACTOR_TYPES view
create VIEW Fabric.G_ACTOR_TYPES as SELECT * FROM new com.ibm.db2j.GaianTable('ACTOR_TYPES', 'with_provenance') CTLT;

-- TASKS view
create VIEW Fabric.G_TASKS as SELECT * FROM new com.ibm.db2j.GaianTable('TASKS', 'with_provenance') MILT;

-- SERVICE_TYPES view
create VIEW Fabric.G_SERVICE_TYPES as SELECT * FROM new com.ibm.db2j.GaianTable('SERVICE_TYPES', 'with_provenance') STLT;

-- PLATFORMS view
create VIEW Fabric.G_PLATFORMS as SELECT * FROM new com.ibm.db2j.GaianTable('PLATFORMS', 'with_provenance') PLT;

-- FABLET_PLUGINS view
create VIEW Fabric.G_FABLET_PLUGINS as SELECT * FROM new com.ibm.db2j.GaianTable('FABLET_PLUGINS', 'with_provenance') FPLT;

-- SYSTEM_PLUGINS view
create VIEW Fabric.G_SYSTEM_PLUGINS as SELECT * FROM new com.ibm.db2j.GaianTable('SYSTEM_PLUGINS', 'with_provenance') SPLT;

-- NODE_NEIGHBOURS view
create VIEW Fabric.G_NODE_NEIGHBOURS as SELECT * FROM new com.ibm.db2j.GaianTable('NODE_NEIGHBOURS', 'with_provenance') NNLT;

-- TASK_SERVICES view
create VIEW Fabric.G_TASK_SERVICES as SELECT * FROM new com.ibm.db2j.GaianTable('TASK_SERVICES', 'with_provenance') MSLT;

-- NODE_IP_MAPPINGS view
create VIEW Fabric.G_NODE_IP_MAPPING as SELECT * FROM new com.ibm.db2j.GaianTable('NODE_IP_MAPPING', 'with_provenance') NIMLT;

-- BEARERS view
create VIEW Fabric.G_BEARERS as SELECT * FROM new com.ibm.db2j.GaianTable('BEARERS', 'with_provenance') BLT;

DISCONNECT;
EXIT;

