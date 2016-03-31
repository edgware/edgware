--------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2012
--
-- LICENSE: Eclipse Public License v1.0
-- http://www.eclipse.org/legal/epl-v10.html
--------------------------------------------------------------------------------

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

