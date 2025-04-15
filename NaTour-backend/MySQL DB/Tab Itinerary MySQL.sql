CREATE TABLE Itinerary(
	id  bigint AUTO_INCREMENT PRIMARY KEY,
    title varchar(255) NOT NULL,
    description varchar(5000),
    difficulty int NOT NULL CHECK (difficulty>0 AND difficulty<6),
    access_dis boolean NOT NULL DEFAULT FALSE,
    private boolean NOT NULL DEFAULT TRUE,
    state varchar(255) NOT NULL,
    region varchar(255) NOT NULL,
    city varchar(255) NOT NULL,
    length double NOT NULL,
    duration time NOT NULL,
    email varchar(320),
    CONSTRAINT f1 FOREIGN KEY(email) REFERENCES User(email)
		ON UPDATE CASCADE
        ON DELETE CASCADE
);