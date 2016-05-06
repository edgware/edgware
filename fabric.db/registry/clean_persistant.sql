--------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2016
--
-- LICENSE: Eclipse Public License v1.0
-- http://www.eclipse.org/legal/epl-v10.html
--------------------------------------------------------------------------------

-- SQL to clean the Registry of all data, including persistant data, except that
-- required by the Fabric.
--
-- Other information that is not removed includes:
--    - Configuration information (default, node, platform and actor)
--    - Plugins
--    - Routes

CONNECT 'jdbc:derby://localhost:6414/FABRIC;create=false;user=fabric;password=fabric;';
DELETE FROM ACTORS WHERE ACTOR_ID NOT LIKE '$%';
DELETE FROM ACTOR_TYPES WHERE TYPE_ID NOT LIKE '$%';
DELETE FROM BEARERS;
DELETE FROM COMPOSITE_PARTS;
DELETE FROM COMPOSITE_SERVICES;
DELETE FROM DATA_FEEDS WHERE PLATFORM_ID NOT LIKE '$%';
DELETE FROM FEED_TYPES WHERE ( TYPE_ID NOT LIKE '$%' AND TYPE_ID NOT LIKE 'untyped_%' );
DELETE FROM NODES;
DELETE FROM NODE_IP_MAPPING;
DELETE FROM NODE_NEIGHBOURS;
DELETE FROM NODE_TYPES WHERE TYPE_ID NOT LIKE 'default_node';
DELETE FROM PLATFORMS WHERE PLATFORM_ID NOT LIKE '$%';
DELETE FROM PLATFORM_TYPES WHERE ( TYPE_ID NOT LIKE '$%' AND TYPE_ID NOT LIKE 'app' AND TYPE_ID NOT LIKE 'service' and TYPE_ID NOT LIKE 'sensor' );
DELETE FROM SERVICES WHERE PLATFORM_ID NOT LIKE '$%';
DELETE FROM SERVICE_TYPES WHERE ( TYPE_ID NOT LIKE '$%' AND TYPE_ID NOT LIKE 'simple_system' );
DELETE FROM SERVICE_WIRING WHERE TO_SERVICE_PLATFORM_ID NOT LIKE '$%';
DELETE FROM TASKS WHERE ( TASK_ID NOT LIKE '$%' AND TASK_ID NOT LIKE '$def' AND TASK_ID NOT LIKE '$fab');
DELETE FROM TASK_SERVICES WHERE PLATFORM_ID NOT LIKE '$%';
DELETE FROM TASK_SUBSCRIPTIONS;
DISCONNECT;
EXIT;
