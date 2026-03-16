1. Database Setup

CREATE DATABASE hrms_v4;
GO

USE hrms_v4;
GO


-------------------------------------------------


2. Create Tables-

Module Table

CREATE TABLE module (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    module_name VARCHAR(50)
);


Module Master Table

CREATE TABLE module_master (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    module_master_name VARCHAR(50)
);


Action Reason Table


CREATE TABLE action_reason (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    action_reason_name VARCHAR(100),
    action_reason_code VARCHAR(100),
    description VARCHAR(255),
    module_id BIGINT,
    module_master_id BIGINT,
    effective_start_date DATE,
    effective_end_date DATE,
    status VARCHAR(50),
    remarks VARCHAR(255),
    version INT,
    creation_date DATE,
    created_by VARCHAR(50),
    modified_by VARCHAR(50),
    modified_date DATE,
    checked_by VARCHAR(50),

    FOREIGN KEY (module_id) REFERENCES module(id),
    FOREIGN KEY (module_master_id) REFERENCES module_master(id)
);






Action Reason History Table


CREATE TABLE action_reason_history (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    action_reason_id BIGINT,
    action_reason_name VARCHAR(100),
    action_reason_code VARCHAR(100),
    description VARCHAR(255),
    module VARCHAR(50),
    module_master VARCHAR(50),
    version INT,
    creation_date DATE,
    created_by VARCHAR(50),
    checked_by VARCHAR(50),
    effective_start_date DATE,
    effective_end_date DATE
);


-----------------------------------------------------------

3. Insert Lookup Data

Module Table

INSERT INTO module (module_name) VALUES ('CoreHR');
INSERT INTO module (module_name) VALUES ('Leave');
INSERT INTO module (module_name) VALUES ('Muster');


Module Master Table

INSERT INTO module_master (module_master_name) VALUES ('Manage Grade');
INSERT INTO module_master (module_master_name) VALUES ('Manage Locations');
INSERT INTO module_master (module_master_name) VALUES ('Manage Positions');






