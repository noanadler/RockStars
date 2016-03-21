-- Users
DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE IF NOT EXISTS users (
    id serial PRIMARY KEY,
    name varchar(255),
    email varchar(255),
    gender varchar(255),
    birthdate timestamp,
	registered_at timestamp,
	countrys text[]
);

-- Travel Items - main table with country information
DROP TABLE IF EXISTS travel_information CASCADE;
CREATE TABLE IF NOT EXISTS travel_information (
    country text PRIMARY KEY,
    full_name text,
    vaccines text[],
    packing_list text[]
);

-- Alerts
DROP TABLE IF EXISTS alerts;
CREATE TABLE IF NOT EXISTS alerts (
    id serial PRIMARY KEY,
    country_id text,
    title varchar(255),
    description text,
    started_at timestamp,
    ended_at timestamp,
    FOREIGN KEY ( country_id ) REFERENCES travel_information (country)
);

-- Vaccines
DROP TABLE IF EXISTS vaccines CASCADE;
CREATE TABLE IF NOT EXISTS vaccines (
    name text PRIMARY KEY,
    category text,
    notes text
);

-- Packing List Items
DROP TABLE IF EXISTS packing_list_items CASCADE;
CREATE TABLE IF NOT EXISTS packing_list_items (
    name varchar(255) PRIMARY KEY,
    category text,
    notes text
);

-- Completed Vaccines
DROP TABLE IF EXISTS completed_vaccines;
CREATE TABLE IF NOT EXISTS completed_vaccines (
    user_id integer,
    vaccine_id text,
    FOREIGN KEY ( user_id ) REFERENCES users (id),
    FOREIGN KEY ( vaccine_id ) REFERENCES vaccines (name)
);

-- Completed Packed Items
DROP TABLE IF EXISTS packed_items;
CREATE TABLE IF NOT EXISTS packed_items (
    user_id integer,
    country text,
    item_id varchar(255),
    FOREIGN KEY ( user_id ) REFERENCES users (id),
    FOREIGN KEY ( item_id ) REFERENCES packing_list_items (name)
);

-- Country Lists
DROP TABLE IF EXISTS country_lists;
CREATE TABLE IF NOT EXISTS country_lists (
    country text PRIMARY KEY,
    emails text[],
    FOREIGN KEY ( country ) REFERENCES travel_information (country)
);