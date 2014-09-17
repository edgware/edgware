--------------------------------------------------------------------------------
-- Licensed Materials - Property of IBM
--
-- (C) Copyright IBM Corp. 2006, 2014
--
-- LICENSE: Eclipse Public License v1.0
-- http://www.eclipse.org/legal/epl-v10.html
--------------------------------------------------------------------------------

CONNECT 'jdbc:derby://localhost:6414/FABRIC;create=false;user=fabric;password=fabric;';

-------------------------------------------------------------------------------
-- FABRIC.DEFAULT_CONFIG
--
-- The set of default configuration parameters used in order to configure, and
-- to connect to, the Fabric.
--
-- Column 1: NAME (key)
--    The name of the Fabric configuration property (e.g. "node.name")
--
-- Column 2: VALUE
--    The value of the configuration property
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- G e n e r a l   S e t t i n g s
-------------------------------------------------------------------------------

-- Default routing factory
insert into fabric.routes values ('*', '*', 999, 'factory=fabric.bus.routing.DynamicRoutingFactory');

-- Default Fabric task
insert into fabric.tasks values ('DEFAULT', null, null, 'Default task. All auto-discovered sensors are assigned to this task.', '{"persistence"="static"}', null);

-- Default node settings
insert into fabric.default_config values ('node.port','1883');

-- Fabric locale settings.
insert into fabric.default_config values ('sys.country', 'gb');
insert into fabric.default_config values ('sys.language', 'en');

-------------------------------------------------------------------------------
-- L o g g i n g   S e t t i n g s
--
-- This section describes logging and instrumentation settings.
-------------------------------------------------------------------------------

-- Enable/disable instrumentation.
insert into fabric.default_config values ('instrumentation.enable', 'false');

-- Instrumentation file persistence, where:
--    {0}: the Fabric node name.
insert into fabric.default_config values ('instrumentation.fileName', 'Fabric_{0}.dat');

-- The number of instrumentation messages buffered before being persisted.
insert into fabric.default_config values ('instrumentation.buffer', '100');

-------------------------------------------------------------------------------
-- R e g i s t r y   S e t t i n g s
--
-- This section describes the type of, and connection to, the Fabric Registry.
-------------------------------------------------------------------------------

-- The Registry type:
--
--    distributed: a distributed Registry.
--    gaian:       a distributed Registry using the Gaian Database.
--    singleton:   a single, centralized, Registry.
--
--insert into fabric.default_config values ('registry.type', 'singleton');

-- The type of connection to be made to the Registry:
--
--    jdbc:      a direct JDBC connection.
--    messaging: an indirect connection via a messaging interface to a proxy
--               node (for use when a direct JDBC connection is unavailable).
--
-- (Note that the messaging protocol is currently unsupported.)
--
--insert into fabric.default_config values ('registry.protocol', 'jdbc');

-- The Registry JDBC connection string (registry.protocol=jdbc) or Fabric proxy
-- node (registry.protocol=messaging).
--insert into fabric.default_config values ('registry.address', 'jdbc:derby://localhost:6414/FABRIC;user=fabric;password=fabric');

-- Flag controlling whether the Fabric should attempt to reestablish
-- a Registry connection in the event the Registry is not available at runtime.
-- This does not effect retries on startup which will always be attempted.
--insert into fabric.default_config values ('registry.reconnect', 'true');

-------------------------------------------------------------------------------
-- F a b r i c   S e r v i c e   C o n f i g u r a t i o n
--
-- This section contains configuration settings that are specific to Fabric
-- services.
-------------------------------------------------------------------------------

-- Flag indicating if the Connection Manager should action connection/
-- disconnection messages (fabric.connectionManager.fireActionMessages=true) or
-- ignore them (fabric.connectionManager.fireActionMessages=false). The effect
-- of not actioning them is that there will be no Fabric-level termination/
-- restoration of subscriptions or Registry maintenance. Instead the
-- communications layer will will indefinitely re-try connections to the
-- disconnected nodes.
insert into fabric.default_config values ('fabric.connectionManager.fireActionMessages', 'true');

-------------------------------------------------------------------------------
-- F a b r i c    M e s s a g e    F o r w a r d i n g    S e r v i c e
--
-- This section defines a number of variables used to configure the message
-- forwarding service, responsible for forwarding feed messages from node to
-- node.
-------------------------------------------------------------------------------

-- When the message queue is empty, the length of the interval (in
-- milliseconds) before checking for new messages
insert into fabric.default_config values ('fabric.messageForwarding.sleepInterval', '1000');

-------------------------------------------------------------------------------
-- F a b r i c   B u s
--
-- This section defines publish/subscribe configuration settings:
--    - The name of the Fabric home node.
--    - Keep-alive settings
--    - The broker IP settings.
--    - The topics to receive data feeds onto the Fabric and publish data feeds
--      to subscribers connected to the Fabric.
--    - The topics to move command and data feed messages across the Fabric.
--    - The topics to send and receive connection and disconnection messages.
-------------------------------------------------------------------------------

-- The Fabric node name.
insert into fabric.default_config values ('fabric.node', 'DEFAULT');
--The default Node Type
insert into fabric.default_config values ('node.type', 'default_node');
--The default Node Affiliation
insert into fabric.default_config values ('node.affiliation', 'none');
--The default Node description
insert into fabric.default_config values ('node.description', 'Default Node');

-- The topic on which the Fabric Manager listens for commands, where:
--
--    {0}: the Fabric node name.
--
-- Note that this is also the topic name that the Fabric Manager will use to
-- send command messages to its neighbouring Fabric Managers.
insert into fabric.default_config values ('fabric.commands.bus', '$fabric/{0}/$commands/$bus');
        
-- The topic on which the Fabric Manager send commands to locally connected
-- clients, where:
--
--    {0}: the Fabric node name.
--    {1}: the client's actor ID.
--    {2}: the actor's platform ID.
--
insert into fabric.default_config values ('fabric.commands.clients', '$fabric/{0}/$commands/$clients/{1}/{2}');

-- The topic used by the Fabric Manager for connection/disconnection messages,
-- where:
--
--    {0}: the Fabric node name.
--
insert into fabric.default_config values ('fabric.commands.topology', '$fabric/{0}/$commands/$topology');

-- The topic on which the Fabric Manager send commands to locally connected
-- platforms, where:
--
--    {0}: the Fabric node name.
--    {1}: the platform ID.
--
insert into fabric.default_config values ('fabric.commands.platforms', '$fabric/{0}/$commands/$platforms/{1}');

-- The topic on which the Fabric Manager send commands to locally connected
-- systems, where:
--
--    {0}: the Fabric node name.
--    {1}: the platform ID.
--    {2}: the system ID.
--
insert into fabric.default_config values ('fabric.commands.services', '$fabric/{0}/$commands/$systems/{1}/{2} ');


-- The base topic on which the Fabric Manager listens for feed messages from
-- locally connected data feeds, where:
--
--    {0}: the Fabric node name.
--
-- Note that at run-time this topic will be further qualified with a sub-topic
-- of the form:
--
--    /<platform>/<service>/<feed> 
insert into fabric.default_config values ('fabric.feeds.onramp', '$fabric/{0}/$feeds/$onramp');

-- The base topic on which the Fabric Manager listens for local replay data
-- feed messages, where:
--
--    {0}: the Fabric node name.
--
-- Note that at run-time this topic will be further qualified with a sub-topic
-- of the form:
--
--    /<platform>/<service>/<feed> 
insert into fabric.default_config values ('fabric.feeds.replay', '$fabric/{0}/$feeds/$replay');
        
-- The base topic on which the Fabric Manager listens for feed messages en
-- route across the Fabric, where:
--
--    {0}: the Fabric node name.
--
-- Note that at run-time this topic will be further qualified with a sub-topic
-- of the form:
--
--    /<platform>/<service>/<feed> 
--
-- Note that this is also the topic name that the Fabric Manager will use when
-- send feed messages to its neighbouring Fabric Managers.
insert into fabric.default_config values ('fabric.feeds.bus', '$fabric/{0}/$feeds/$bus');

-- The base topic on which the Fabric Manager publishes feed messages for
-- consumption by locally connected subscription clients, where:
--
--    {0}: the Fabric node name.
--
-- Note that at run-time this topic will be further qualified with a sub-topic
-- of the form:
--
--    <client-id>/<task-id>/<platform>/<service>/<feed> 
insert into fabric.default_config values ('fabric.feeds.offramp', '$fabric/{0}/$feeds/$offramp');

-----------------------------------------------------------------------------
-- M Q T T   C o n f i g u r a t i o n
--
-- This section defines the MQTT/broker configuration settings.
-----------------------------------------------------------------------------

-- The broker IP address (as seen both locally and remotely).
insert into fabric.default_config values ('mqtt.ip.port.remote', '1883');
insert into fabric.default_config values ('mqtt.ip.port.local', '1884');

-- Broker QoS connection settings, where the QoS value must be one of:
--
--     QOS_0: fire and forget, i.e. no verification of receipt
--     QOS_1: the message is delivered at least once
--     QOS_2: the message is delivered once and only once
insert into fabric.default_config values ('mqtt.qos', 'QOS_2');

-- Broker connection client ID prefix
insert into fabric.default_config values ('mqtt.clientId', 'FAB_');

-- The number of times to try re-establishing a connection. (-1 is forever) 
insert into fabric.default_config values ('mqtt.connectRetries', '3');
-- The interval between trying to re-establishing a connection. (milliseconds) */
insert into fabric.default_config values ('mqtt.connectRetries.interval', '1000');

-- MQTT-S enabled setting (only to be used if the broker is MQTT-S enabled).
insert into fabric.default_config values ('mqtts.enabled', 'false');

-- The maximum size of message (in bytes) that can be sent via MQTT-S/UDP, generally the size that will fit in a single
-- packet (only effective if mqtts.enabled=true).
insert into fabric.default_config values ('mqtts.maxPayload', '500');

-- The default message QoS setting:
--
--     reliable: send messages using a reliable (and typically slower) protocol
--     best-effort: send messages using a best-effort (not guaranteed, but typically faster) protocol
--
-- Note that support for best-effort requires a compatible broker.
insert into fabric.default_config values ('io.defaultQos', 'best-effort');

-------------------------------------------------------------------------------
-- F a b r i c   D i s c o v e r y
--
-- This section contains configuration settings that are specified to Fabric
-- autodiscovery.
-------------------------------------------------------------------------------

-- Configuration settings

-- The interface(s) to which the node will connect (comma-separated list)
insert into fabric.default_config values ('fabric.node.interfaces', 'lo0');

insert into fabric.default_config values ('autodiscovery.port','61883');
insert into fabric.default_config values ('autodiscovery.frequency','30000');
insert into fabric.default_config values ('autodiscovery.timeout','600000');
insert into fabric.default_config values ('autodiscovery.sweeper.interval','5510');
insert into fabric.default_config values ('autodiscovery.group','225.0.18.83');
insert into fabric.default_config values ('autodiscovery.request','enabled');
insert into fabric.default_config values ('autodiscovery.listen','enabled');
insert into fabric.default_config values ('autodiscovery.purgeNeighbours','false');
-- Topic for discovery messages
insert into fabric.default_config values ('fabric.discovery.topic','$fabric/{0}/$discovery');

-- Autodiscovery Fablets
insert into fabric.fablet_plugins values ('*', 'fabric.fablets.autodiscovery.AutoDiscoveryFablet', 'DEFAULT_FABLETS', 'Modifies the Registry based on auto-discovery of Fabric assets.', null);
insert into fabric.fablet_plugins values ('*', 'fabric.fablets.autodiscovery.AutoDiscoveryListenerFablet', 'DEFAULT_FABLETS', 'Listens for discovery requests from Fabric assets.', null);
insert into fabric.fablet_plugins values ('*', 'fabric.fablets.autodiscovery.AutoDiscoveryRequestFablet', 'DEFAULT_FABLETS', 'Publishes node discovery requests, either via broadcast or multicast.', null);

-- Heartbeat Fablet
insert into fabric.fablet_plugins values ('*', 'fabric.fablets.heartbeat.HeartbeatFablet', 'DEFAULT_FABLETS', 'Heartbeat data feed used by assets to determine node availability.', null);

-------------------------------------------------------------------------------
-- F a b r i c    M Q T T    A d a p t e r
--
-- This section defines a number of variables used to configure the Fabric
-- MQTT adapter.
-------------------------------------------------------------------------------

-- The base topic on which the adapter listens for JSON operations, where:
--
--    {0}: the Fabric node name.
--
-- Note that at run-time adapter clients must further qualify this topic with
-- a sub-topic corresponding to their MQTT client ID.
insert into fabric.default_config values ('fabric.adapters.mqtt.intopic','$fabric/{0}/$adapters/$mqtt/$in');

-- The base topic on which the adapter sends responses in reply to JSON
-- operations, where:
--
--    {0}: the Fabric node name.
--
-- Note that at run-time this topic will be further qualified with a sub-topic
-- corresponding to the MQTT client ID upon which the corresponding op message
-- was received.
insert into fabric.default_config values ('fabric.adapters.mqtt.outtopic','$fabric/{0}/$adapters/$mqtt/$out');

-------------------------------------------------------------------------------

DISCONNECT;
EXIT;
