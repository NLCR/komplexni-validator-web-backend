create user validator with password 'vldtr';


-- QUOTAS service
CREATE DATABASE kv_quota_service WITH OWNER validator;
CREATE TABLE IF NOT EXISTS quotas (
   quota_name VARCHAR NOT NULL,
   quota_value INT NOT NULL
);

INSERT INTO quotas (quota_name, quota_value) VALUES ('maxUploadSizeMB', 100);
INSERT INTO quotas (quota_name, quota_value) VALUES ('maxParallelJobs', 2);
INSERT INTO quotas (quota_name, quota_value) VALUES ('maxParallelExtractionJobs', 1);
INSERT INTO quotas (quota_name, quota_value) VALUES ('maxParallelValidationJobs', 1);
INSERT INTO quotas (quota_name, quota_value) VALUES ('maxParallelArchivationJobs', 1);
INSERT INTO quotas (quota_name, quota_value) VALUES ('maxParallelDeletionJobs', 1);
INSERT INTO quotas (quota_name, quota_value) VALUES ('timeToArchiveValidationH', 24);
INSERT INTO quotas (quota_name, quota_value) VALUES ('timeToDeleteValidationH', 48);
INSERT INTO quotas (quota_name, quota_value) VALUES ('userVerifiedMaxActiveJobs', 3);
INSERT INTO quotas (quota_name, quota_value) VALUES ('userVerifiedMaxInactiveJobs', 30);
INSERT INTO quotas (quota_name, quota_value) VALUES ('userUnverifiedMaxActiveJobs', 1);
INSERT INTO quotas (quota_name, quota_value) VALUES ('userUnverifiedMaxInactiveJobs', 10);

-- VALIDATION MANAGER service
CREATE DATABASE kv_validation_mgr_service WITH OWNER validator;
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

-- USER service
CREATE DATABASE kv_user_service WITH OWNER validator;
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
