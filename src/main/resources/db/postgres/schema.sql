DROP TABLE IF EXISTS comments;

DROP TABLE IF EXISTS tasks;

DROP TABLE IF EXISTS users;

DROP CAST IF EXISTS (VARCHAR AS STATUS);

DROP CAST IF EXISTS (VARCHAR AS PRIORITY);

DROP CAST IF EXISTS (VARCHAR AS TIMESTAMPTZ);

DROP TYPE IF EXISTS STATUS;

DROP TYPE IF EXISTS PRIORITY;

CREATE TYPE STATUS AS ENUM ('PENDING', 'IN_PROCESS', 'DONE');

CREATE TYPE PRIORITY AS ENUM ('LOW', 'MEDIUM', 'HIGH');

CREATE CAST (VARCHAR AS STATUS) WITH INOUT AS IMPLICIT;

CREATE CAST (VARCHAR AS PRIORITY) WITH INOUT AS IMPLICIT;

CREATE CAST (VARCHAR AS TIMESTAMPTZ) WITH INOUT AS IMPLICIT;

CREATE TABLE IF NOT EXISTS tasks (
	id BIGSERIAL PRIMARY KEY,
	title VARCHAR(50) NOT NULL,
	description VARCHAR(300) NOT NULL,
	status STATUS DEFAULT 'PENDING' NOT NULL,
	priority PRIORITY DEFAULT 'LOW' NOT NULL,
	author VARCHAR(30) NOT NULL,
	performer VARCHAR(30)
);


CREATE TABLE IF NOT EXISTS comments (
	id BIGSERIAL PRIMARY KEY,
	task_id BIGINT REFERENCES tasks ON DELETE CASCADE,
	author VARCHAR(30) NOT NULL,
	text VARCHAR(300) NOT NULL,
	timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
	id BIGSERIAL PRIMARY KEY,
	email VARCHAR(30) NOT NULL UNIQUE,
	password VARCHAR(50) NOT NULL
);


