# Stories schema

# --- !Ups

CREATE TABLE STORIES ()
    id bigint NOT NULL AUTO_INCREMENT,
    eventType varchar(255) NOT NULL,
    eventBody varchar(2000) NOT NULL UNIQUE,
    importDate DATE NOT NULL,
    isBroadcast boolean NOT NULL,
    PRIMARY KEY (id)
    );

# --- !Downs

DROP TABLE STORIES;