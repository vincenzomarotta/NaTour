CREATE TABLE Message(
	emailDest varchar(320) NOT NULL,
    emailSend varchar(320) NOT NULL,
    message varchar(5000) NOT NULL,
    date timestamp NOT NULL,
    isSeen boolean NOT NULL DEFAULT FALSE,
    CONSTRAINT f9 FOREIGN KEY(emailDest) REFERENCES User(email)
		ON UPDATE CASCADE
        ON DELETE CASCADE,
	CONSTRAINT f10 FOREIGN KEY(emailSend) REFERENCES User(email)
		ON UPDATE CASCADE
        ON DELETE CASCADE
);