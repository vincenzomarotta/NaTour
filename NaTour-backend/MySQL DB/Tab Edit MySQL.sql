CREATE TABLE Edit(
	date timestamp NOT NULL,
    email varchar(320),
    id bigint,
    CONSTRAINT f2 FOREIGN KEY(email) REFERENCES User(email)
		ON DELETE CASCADE
        ON UPDATE CASCADE,
	CONSTRAINT f3 FOREIGN KEY(id) REFERENCES Itinerary(id)
		ON DELETE CASCADE
        ON UPDATE CASCADE
);
