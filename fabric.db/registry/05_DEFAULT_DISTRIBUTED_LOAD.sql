--------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2014
--
-- LICENSE: Eclipse Public License v1.0
-- http://www.eclipse.org/legal/epl-v10.html
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- Initialise the Fabric distributed (non-Gaian) Registry.
--------------------------------------------------------------------------------

CONNECT 'jdbc:derby://localhost:6414/FABRIC;create=true;user=gaiandb;password=passw0rd;';

-- Insert the distributed query Fablet into the Registry, to run on all nodes
INSERT INTO FABRIC.FABLET_PLUGINS 
VALUES ('*', 
		'fabric.registry.persistence.distributed.DistributedPersistenceFablet', 
		'DEFAULT_FABLETS', 
		'Handles Distributed Persistence Query request and results', 
		null);

DISCONNECT;


EXIT;

