-- Inicializace a migrace databazi Komplexniho validatoru.
--
-- Skript je IDEMPOTENTNI: opakovane spusteni bezpecne doplni chybejici databaze,
-- tabulky, sloupce i vychozi radky kvot a nikdy nemaze ani neprepisuje existujici data.
-- Pri aktualizaci instalace tedy staci skript spustit znovu.
--
-- Spusteni (jako postgres superuser, vyzaduje psql >= 9.6):
--   psql -d postgres -f init_db.sql

SELECT 'CREATE USER validator WITH PASSWORD ''vldtr'''
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'validator')\gexec

SELECT 'CREATE DATABASE kv_quota_service WITH OWNER validator'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'kv_quota_service')\gexec

SELECT 'CREATE DATABASE kv_validation_mgr_service WITH OWNER validator'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'kv_validation_mgr_service')\gexec

SELECT 'CREATE DATABASE kv_user_service WITH OWNER validator'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'kv_user_service')\gexec


-- QUOTAS service
\connect kv_quota_service

CREATE TABLE IF NOT EXISTS quotas (
   quota_name VARCHAR NOT NULL,
   quota_value INT NOT NULL
);
ALTER TABLE quotas OWNER TO validator;

-- unikatni index kvuli ON CONFLICT nize (a ochrane pred duplicitami)
CREATE UNIQUE INDEX IF NOT EXISTS quotas_quota_name_key ON quotas (quota_name);

-- doplni jen chybejici kvoty, existujici hodnoty neprepisuje
INSERT INTO quotas (quota_name, quota_value) VALUES
   ('maxUploadSizeMB', 100),
   ('maxParallelJobs', 2),
   ('maxParallelExtractionJobs', 1),
   ('maxParallelValidationJobs', 1),
   ('maxParallelArchivationJobs', 1),
   ('maxParallelDeletionJobs', 1),
   ('timeToArchiveValidationH', 24),
   ('timeToDeleteValidationH', 48),
   ('userVerifiedMaxActiveJobs', 3),
   ('userVerifiedMaxInactiveJobs', 30),
   ('userUnverifiedMaxActiveJobs', 1),
   ('userUnverifiedMaxInactiveJobs', 10)
ON CONFLICT (quota_name) DO NOTHING;


-- VALIDATION MANAGER service
\connect kv_validation_mgr_service

CREATE TABLE IF NOT EXISTS validations (
   id VARCHAR UNIQUE NOT NULL,
   owner_id VARCHAR NOT NULL,
   state VARCHAR NOT NULL,
   package_name VARCHAR NOT NULL,
   package_size_mb INT NOT NULL,
   dmf_type VARCHAR,
   preferred_dmf_version VARCHAR,
   forced_dmf_version VARCHAR,
   priority INT NOT NULL,
   note VARCHAR,
   ts_created timestamp NULL,
   ts_scheduled timestamp NULL,
   ts_started timestamp NULL,
   ts_ended timestamp NULL
);
ALTER TABLE validations OWNER TO validator;

-- migrace starsich instalaci: doplneni sloupcu, ktere pribyly pozdeji
-- (NOT NULL sloupce dostanou docasny DEFAULT, aby migrace prosla i na naplnene tabulce; existujici radky dostanou prazdnou hodnotu)
ALTER TABLE validations ADD COLUMN IF NOT EXISTS package_name VARCHAR NOT NULL DEFAULT '';
ALTER TABLE validations ALTER COLUMN package_name DROP DEFAULT;
ALTER TABLE validations ADD COLUMN IF NOT EXISTS package_size_mb INT NOT NULL DEFAULT 0;
ALTER TABLE validations ALTER COLUMN package_size_mb DROP DEFAULT;
ALTER TABLE validations ADD COLUMN IF NOT EXISTS dmf_type VARCHAR;
ALTER TABLE validations ADD COLUMN IF NOT EXISTS preferred_dmf_version VARCHAR;
ALTER TABLE validations ADD COLUMN IF NOT EXISTS forced_dmf_version VARCHAR;


-- USER service
\connect kv_user_service

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR UNIQUE NOT NULL,
    email VARCHAR NOT NULL,
    picture_url VARCHAR NULL,
    given_name VARCHAR,
    family_name VARCHAR,
    name VARCHAR,
    verified BOOLEAN NOT NULL,
    admin BOOLEAN NOT NULL,
    institution_name VARCHAR,
    institution_sigla VARCHAR
);
ALTER TABLE users OWNER TO validator;
