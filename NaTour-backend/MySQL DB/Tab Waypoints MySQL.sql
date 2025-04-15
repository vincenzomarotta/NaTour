CREATE TABLE Waypoints(
    seq_number int NOT NULL,
    latitude double NOT NULL,
    longitude double NOT NULL,
    id bigint NOT NULL,
    CONSTRAINT f4 FOREIGN KEY(id) REFERENCES Itinerary(id)
		ON UPDATE CASCADE
        ON DELETE CASCADE
);