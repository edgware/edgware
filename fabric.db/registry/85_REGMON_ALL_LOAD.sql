--------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2006, 2014
--
-- LICENSE: Eclipse Public License v1.0
-- http://www.eclipse.org/legal/epl-v10.html
--------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- Adds triggers to the Registry to report updates via the Fabric.
-------------------------------------------------------------------------------

CONNECT 'jdbc:derby://localhost:6414/FABRIC;create=true;user=gaiandb;password=passw0rd;';

-------------------------------------------------------------------------------
-- Configure the services used to distribute Registry update notifications
-------------------------------------------------------------------------------

INSERT INTO FABRIC.PLATFORM_TYPES VALUES ('$virtual', 'Built-in platform type for virtual systems', '{"persistence":"static"}', null);
INSERT INTO FABRIC.PLATFORMS VALUES ('$fabric', '$virtual', '$virtual', null, null, 'DEPLOYED', 'AVAILABLE', 0.0, 0.0, 0.0, 0.0, 0.0, 'Built-in platform for Registry virtual systems', '{"persistence":"static"}', null);
    
INSERT INTO FABRIC.SERVICE_TYPES VALUES ('$registry', 'Registry update system type', '$registry_updates', null);
INSERT INTO FABRIC.SERVICES VALUES ('$fabric', '$registry', '$registry', 'SERVICE', null, 'DEPLOYED', 'AVAILABLE', 0.0, 0.0, 0.0, 0.0, 0.0, 'The Fabric Registry update system', '{"persistence":"static"}', null);

INSERT INTO FABRIC.FEED_TYPES VALUES('$registry_updates', 'Registry update data feed', 'output-feed', null);
INSERT INTO FABRIC.DATA_FEEDS VALUES('$fabric', '$registry', '$registry_updates', '$registry_updates', 'output', null, 'AVAILABLE', 'The Fabric Registry update service', '{"persistence":"static"}', null);

INSERT INTO FABRIC.TASKS VALUES ('$fabric', null, null, 'Built-in Fabric task', '{"persistence":"static"}', null);
INSERT INTO FABRIC.TASK_SERVICES VALUES('DEFAULT', '$fabric', '$registry', '$registry_updates', 'The Fabric Registry update service', null, '{"persistence":"static"}');

INSERT INTO FABRIC.ACTOR_TYPES VALUES ('$daemon', 'Fabric built-in system user type', '{"persistence":"static"}', null);
INSERT INTO FABRIC.ACTORS VALUES ('$fabric', '$daemon', '$fabric', null, null, 'Fabric built-in system user', '{"persistence":"static"}', null);
    
-------------------------------------------------------------------------------
-- Configure the triggers
-------------------------------------------------------------------------------

-- Function to be invoked by the triggers
DROP FUNCTION FUNC_UPDATE_TRIGGER;
CREATE FUNCTION FUNC_UPDATE_TRIGGER(TABLE_NAME VARCHAR(255), PK_COLUMN VARCHAR(255), ID VARCHAR(32672), ACTION_NAME VARCHAR(6)) RETURNS INT LANGUAGE JAVA EXTERNAL NAME 'fabric.registry.trigger.TableUpdate.entryModified' PARAMETER STYLE JAVA READS SQL DATA;

-- Drop existing triggers (if installed)

DROP TRIGGER NODES_DT;
DROP TRIGGER NODES_IT;
DROP TRIGGER NODES_UT;

DROP TRIGGER NEIGHBOURS_DT;
DROP TRIGGER NEIGHBOURS_IT;
DROP TRIGGER NEIGHBOURS_UT;

DROP TRIGGER PLATFORMS_DT;
DROP TRIGGER PLATFORMS_IT;
DROP TRIGGER PLATFORMS_UT;

DROP TRIGGER SYSTEMS_DT;
DROP TRIGGER SYSTEMS_IT;
DROP TRIGGER SYSTEMS_UT;

DROP TRIGGER SERVICES_DT;
DROP TRIGGER SERVICES_IT;
DROP TRIGGER SERVICES_UT;

DROP TRIGGER SUBSCRIPTIONS_DT;
DROP TRIGGER SUBSCRIPTIONS_IT;
DROP TRIGGER SUBSCRIPTIONS_UT;

-- Add triggers to each table that we want to monitor

-- NODES

CREATE TRIGGER nodes_dt AFTER DELETE ON Fabric.Nodes
REFERENCING OLD AS oldRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('NODES', 'NODE_ID:TYPE_ID', oldRow.NODE_ID || ':' || oldRow.TYPE_ID, 'DELETE') FROM sysibm.sysdummy1;

CREATE TRIGGER nodes_it AFTER INSERT ON Fabric.Nodes
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('NODES', 'NODE_ID:TYPE_ID', newRow.NODE_ID || ':' || newRow.TYPE_ID, 'INSERT') FROM sysibm.sysdummy1;

CREATE TRIGGER nodes_ut AFTER UPDATE ON Fabric.Nodes
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('NODES', 'NODE_ID:TYPE_ID', newRow.NODE_ID || ':' || newRow.TYPE_ID, 'UPDATE') FROM sysibm.sysdummy1;

-- NODE_NEIGHBOURS

CREATE TRIGGER neighbours_dt AFTER DELETE ON Fabric.Node_Neighbours
REFERENCING OLD AS oldRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('NODE_NEIGHBOURS', 'NODE_ID/NEIGHBOUR_ID', oldRow.NODE_ID || '/' || oldRow.NEIGHBOUR_ID, 'DELETE') FROM sysibm.sysdummy1;

CREATE TRIGGER neighbours_it AFTER INSERT ON Fabric.Node_Neighbours
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('NODE_NEIGHBOURS', 'NODE_ID/NEIGHBOUR_ID', newRow.NODE_ID || '/' || newRow.NEIGHBOUR_ID, 'INSERT') FROM sysibm.sysdummy1;

CREATE TRIGGER neighbours_ut AFTER UPDATE ON Fabric.Node_Neighbours
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('NODE_NEIGHBOURS', 'NODE_ID/NEIGHBOUR_ID', newRow.NODE_ID || '/' || newRow.NEIGHBOUR_ID, 'UPDATE') FROM sysibm.sysdummy1;

-- PLATFORMS

CREATE TRIGGER platforms_dt AFTER DELETE ON Fabric.Platforms
REFERENCING OLD AS oldRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('PLATFORMS', 'PLATFORM_ID:TYPE_ID', oldRow.PLATFORM_ID || ':' || oldRow.TYPE_ID, 'DELETE') FROM sysibm.sysdummy1;

CREATE TRIGGER platforms_it AFTER INSERT ON Fabric.Platforms
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('PLATFORMS', 'PLATFORM_ID:TYPE_ID', newRow.PLATFORM_ID || ':' || newRow.TYPE_ID, 'INSERT') FROM sysibm.sysdummy1;

CREATE TRIGGER platforms_ut AFTER UPDATE ON Fabric.Platforms
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('PLATFORMS', 'PLATFORM_ID:TYPE_ID', newRow.PLATFORM_ID || ':' || newRow.TYPE_ID, 'UPDATE') FROM sysibm.sysdummy1;

-- SYSTEMS

CREATE TRIGGER systems_dt AFTER DELETE ON Fabric.Services
REFERENCING OLD AS oldRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('SYSTEMS', 'PLATFORM_ID/ID:TYPE_ID:AVAILABILITY', oldRow.PLATFORM_ID || '/' || oldRow.ID || ':' || oldRow.TYPE_ID || ':' || oldRow.AVAILABILITY, 'DELETE') FROM sysibm.sysdummy1;

CREATE TRIGGER systems_it AFTER INSERT ON Fabric.Services
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('SYSTEMS', 'PLATFORM_ID/ID:TYPE_ID:AVAILABILITY', newRow.PLATFORM_ID || '/' || newRow.ID || ':' || newRow.TYPE_ID || ':' || newRow.AVAILABILITY, 'INSERT') FROM sysibm.sysdummy1;

CREATE TRIGGER systems_ut AFTER UPDATE ON Fabric.Services
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('SYSTEMS', 'PLATFORM_ID/ID:TYPE_ID:AVAILABILITY', newRow.PLATFORM_ID || '/' || newRow.ID || ':' || newRow.TYPE_ID || ':' || newRow.AVAILABILITY, 'UPDATE') FROM sysibm.sysdummy1;

-- SERVICES

CREATE TRIGGER services_dt AFTER DELETE ON Fabric.Data_Feeds
REFERENCING OLD AS oldRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('SERVICES', 'PLATFORM_ID/SERVICE_ID/ID:TYPE_ID', oldRow.PLATFORM_ID || '/' || oldRow.SERVICE_ID || '/' || oldRow.ID || ':' || oldRow.TYPE_ID, 'DELETE') FROM sysibm.sysdummy1;

CREATE TRIGGER services_it AFTER INSERT ON Fabric.Data_Feeds
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('SERVICES', 'PLATFORM_ID/SERVICE_ID/ID:TYPE_ID', newRow.PLATFORM_ID || '/' || newRow.SERVICE_ID || '/' || newRow.ID || ':' || newRow.TYPE_ID, 'INSERT') FROM sysibm.sysdummy1;

CREATE TRIGGER services_ut AFTER UPDATE ON Fabric.Data_Feeds
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('SERVICES', 'PLATFORM_ID/SERVICE_ID/ID:TYPE_ID', newRow.PLATFORM_ID || '/' || newRow.SERVICE_ID || '/' || newRow.ID || ':' || newRow.TYPE_ID, 'UPDATE') FROM sysibm.sysdummy1;

-- TASK_SUBSCRIPTIONS

CREATE TRIGGER subscriptions_dt AFTER DELETE ON Fabric.Task_Subscriptions
REFERENCING OLD AS oldRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('TASK_SUBSCRIPTIONS', 'TASK_ID/ACTOR_ID/PLATFORM_ID/SERVICE_ID/DATA_FEED_ID/ACTOR_PLATFORM_ID', oldRow.TASK_ID || '/' || oldRow.ACTOR_ID || '/' || oldRow.PLATFORM_ID || '/' || oldRow.SERVICE_ID || '/' || oldRow.DATA_FEED_ID || '/' || oldRow.ACTOR_PLATFORM_ID, 'DELETE') FROM sysibm.sysdummy1;

CREATE TRIGGER subscriptions_it AFTER INSERT ON Fabric.Task_Subscriptions
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('TASK_SUBSCRIPTIONS', 'TASK_ID/ACTOR_ID/PLATFORM_ID/SERVICE_ID/DATA_FEED_ID/ACTOR_PLATFORM_ID', newRow.TASK_ID || '/' || newRow.ACTOR_ID || '/' || newRow.PLATFORM_ID || '/' || newRow.SERVICE_ID || '/' || newRow.DATA_FEED_ID || '/' || newRow.ACTOR_PLATFORM_ID, 'INSERT') FROM sysibm.sysdummy1;

CREATE TRIGGER subscriptions_ut AFTER UPDATE ON Fabric.Task_Subscriptions
REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL SELECT FUNC_UPDATE_TRIGGER('TASK_SUBSCRIPTIONS', 'TASK_ID/ACTOR_ID/PLATFORM_ID/SERVICE_ID/DATA_FEED_ID/ACTOR_PLATFORM_ID', newRow.TASK_ID || '/' || newRow.ACTOR_ID || '/' || newRow.PLATFORM_ID || '/' || newRow.SERVICE_ID || '/' || newRow.DATA_FEED_ID || '/' || newRow.ACTOR_PLATFORM_ID, 'UPDATE') FROM sysibm.sysdummy1;

-------------------------------------------------------------------------------
DISCONNECT;
EXIT;
