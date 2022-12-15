create user validator with password 'vldtr';


-- QUOTAS service
CREATE DATABASE validator_quotas WITH OWNER validator;
CREATE TABLE IF NOT EXISTS quotas (
   quota_name VARCHAR NOT NULL,
   quota_value INT NOT NULL
);

INSERT INTO quotas (quota_name, quota_value) VALUES ('maxUploadSizeMB', 100);
INSERT INTO quotas (quota_name, quota_value) VALUES ('maxParallelJobs', 20);
INSERT INTO quotas (quota_name, quota_value) VALUES ('maxParallelExtractionJobs', 4);
INSERT INTO quotas (quota_name, quota_value) VALUES ('maxParallelValidationJobs', 4);
INSERT INTO quotas (quota_name, quota_value) VALUES ('maxParallelArchivationJobs', 3);
INSERT INTO quotas (quota_name, quota_value) VALUES ('maxParallelDeletionJobs', 3);
INSERT INTO quotas (quota_name, quota_value) VALUES ('timeToArchiveValidationH', 24);
INSERT INTO quotas (quota_name, quota_value) VALUES ('timeToDeleteValidationH', 48);


-- VALIDATION MANAGER service
CREATE DATABASE validator_validations WITH OWNER validator;
CREATE TABLE IF NOT EXISTS validations (
   id VARCHAR UNIQUE NOT NULL,
   owner_id VARCHAR NOT NULL,
   priority INT NOT NULL,
   state VARCHAR NOT NULL,
   note VARCHAR,
   ts_created timestamp NULL,
   ts_scheduled timestamp NULL,
   ts_started timestamp NULL,
   ts_ended timestamp NULL
);

-- USER service
CREATE DATABASE validator_users WITH OWNER validator;
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
