CREATE TABLE User(
	email varchar(320) PRIMARY KEY, 
    name varchar(100) NOT NULL,
    surname varchar(100) NOT NULL,
    isAdmin boolean NOT NULL DEFAULT FALSE
);