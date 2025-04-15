CREATE TRIGGER open_lists
AFTER INSERT ON User
FOR EACH ROW
	INSERT INTO List(idList,type,email)
    	(SELECT NULL,LTE.type,NEW.email
	 FROM ListTypeEnum LTE);