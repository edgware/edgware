--------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2014
--
-- LICENSE: Eclipse Public License v1.0
-- http://www.eclipse.org/legal/epl-v10.html
--------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- Adds default platforms, systems and services
-------------------------------------------------------------------------------

CONNECT 'jdbc:derby://localhost:6414/FABRIC;create=true;user=gaiandb;password=passw0rd;';

-------------------------------------------------------------------------------
-- FABRIC.PLATFORM_TYPES
--
-- Basic, default, platform types.
--
-- Column 1: NAME (key)
--    The name of the type.
--
-- Column 2: DESCRIPTION
--    The description of the type.
--
-- Column 3: ATTRIUBTES
--    The type attributes.
--
-- Column 4: ATTRIUBTES_URI
--    The URI of additional type attributes.
-------------------------------------------------------------------------------

INSERT INTO FABRIC.PLATFORM_TYPES VALUES ('app', 'Built-in platform type for applications', '{"persistent":"true"}', null);
INSERT INTO FABRIC.PLATFORM_TYPES VALUES ('service', 'Built-in platform type for services', '{"persistent":"true"}', null);
INSERT INTO FABRIC.PLATFORM_TYPES VALUES ('sensor', 'Built-in generic sensor platform type', '{"persistent":"true"}', null);

-------------------------------------------------------------------------------
-- FABRIC.SYSTEM_TYPES
--
-- Basic, default, system types.
--
-- Column 1: NAME (key)
--    The name of the type.
--
-- Column 2: DESCRIPTION
--    The description of the type.
--
-- Column 3: ATTRIUBTES
--    The type attributes, i.e. the names of the service types that it supports.
--
-- Column 4: ATTRIUBTES_URI
--    The URI of additional type attributes.
-------------------------------------------------------------------------------

INSERT INTO FABRIC.SERVICE_TYPES VALUES ('simple_system', 'Default simple system', '{"persistent":"true","serviceTypes":"untyped_output_feed,untyped_input_feed,untyped_solicit_response,untyped_request_response,untyped_notification,untyped_listener"}', null);

-------------------------------------------------------------------------------
-- FABRIC.SERVICE_TYPES
--
-- Basic, default, service types.
--
-- Column 1: NAME (key)
--    The name of the type.
--
-- Column 2: DESCRIPTION
--    The description of the type.
--
-- Column 3: ATTRIUBTES
--    The type attributes, i.e. the mode of the type, one of INPUT_FEED, OUTPUT_FEED,
--    SOLICIT_RESPONSE, REQUEST_RESPONSE, NOTIFICATION or LISTENER.
--
-- Column 4: ATTRIUBTES_URI
--    The URI of additional type attributes.
-------------------------------------------------------------------------------

INSERT INTO FABRIC.FEED_TYPES VALUES('untyped_output_feed', 'Built-in untyped output feed service type', '{"persistent":"true","mode":"output-feed"}', null);
INSERT INTO FABRIC.FEED_TYPES VALUES('untyped_input_feed', 'Built-in untyped input feed service type', '{"persistent":"true","mode":"input-feed"}', null);
INSERT INTO FABRIC.FEED_TYPES VALUES('untyped_solicit_response', 'Built-in untyped solicit-response service type', '{"persistent":"true","mode":"solicit-response"}', null);
INSERT INTO FABRIC.FEED_TYPES VALUES('untyped_request_response', 'Built-in untyped request-response service type', '{"persistent":"true","mode":"request-response"}', null);
INSERT INTO FABRIC.FEED_TYPES VALUES('untyped_notification', 'Built-in untyped notification service type', '{"persistent":"true","mode":"notification"}', null);
INSERT INTO FABRIC.FEED_TYPES VALUES('untyped_listener', 'Built-in untyped listener service type', '{"persistent":"true","mode":"listener"}', null);

-------------------------------------------------------------------------------
DISCONNECT;
EXIT;
