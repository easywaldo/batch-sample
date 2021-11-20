DROP TABLE member IF EXISTS;

CREATE TABLE member  (
                       member_seq BIGINT IDENTITY NOT NULL PRIMARY KEY,
                       name VARCHAR(20),
                       create_dt timestamp,
                       login_dt timestamp,
                       is_active boolean
);

DROP TABLE deal IF EXISTS;

CREATE TABLE deal  (
                         num varchar(200) NOT NULL PRIMARY KEY,
                         name VARCHAR(20),
                         hott_id varchar(100)
);