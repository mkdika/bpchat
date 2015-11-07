package com.mkdika.bpchat.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author Maikel Chandika <mkdika@gmail.com>
 */
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;
    private String sender;
    private Date received;
    private Set<String> nicknames;

    public ChatMessage() {
        super();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public Set<String> getNicknames() {
        return nicknames;
    }

    public void setNicknames(Set<String> nicknames) {
        this.nicknames = nicknames;
    }
}
