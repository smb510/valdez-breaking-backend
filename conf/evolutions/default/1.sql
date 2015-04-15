# Stories schema

# --- !Ups

CREATE TABLE stories (
    id serial PRIMARY KEY,
    eventType varchar(255) NOT NULL,
    eventBody varchar(2000) NOT NULL UNIQUE,
    importDate DATE NOT NULL,
    isBroadcast boolean NOT NULL
);

# --- !Downs

DROP TABLE STORIES;