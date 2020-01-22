CREATE DATABASE object_store_test;

--REVOKE CONNECT ON DATABASE object_store_test FROM PUBLIC;
--REVOKE CREATE ON SCHEMA public FROM PUBLIC;

CREATE SCHEMA seqdb;

-- depends on the "seqdb" schema
CREATE ROLE migrate NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN PASSWORD 'migrate';
GRANT CONNECT ON DATABASE object_store_test TO migrate;
GRANT USAGE, CREATE ON SCHEMA seqdb TO migrate;
ALTER DEFAULT PRIVILEGES IN SCHEMA seqdb GRANT ALL PRIVILEGES ON TABLES TO migrate;

CREATE ROLE test NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN PASSWORD 'test';;
GRANT CONNECT ON DATABASE object_store_test TO test;
GRANT USAGE ON SCHEMA seqdb TO test;
GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON ALL TABLES IN SCHEMA seqdb TO test;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA seqdb TO test;

ALTER DEFAULT PRIVILEGES FOR USER migrate IN SCHEMA seqdb GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON TABLES TO test;
ALTER DEFAULT PRIVILEGES FOR USER migrate IN SCHEMA seqdb GRANT SELECT, USAGE ON SEQUENCES TO test;
--FOR USER seqdb_migration is not a typo here, it means something like "when user seqdb_migration do something (create), grant the following permissions.
