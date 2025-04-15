package com.example.natour.entity;

import java.sql.Timestamp;
import java.util.Date;

public class Message {
    private String sender;
    private String receiver;
    private String body;
    private Timestamp date;
    private boolean seen;


    public Message() {
    }

    public Message(String sender, String receiver, String body, boolean seen, Timestamp date){
        setSender(sender);
        setReceiver(receiver);
        setBody(body);
        setSeen(seen);
        setDate(date);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }


    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
