package sd2526.trab.impl;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class InboxEntry {

    @Id
    private String id;

    private String owner;
    private String messageId;


    public InboxEntry() {
    }

    public InboxEntry(String owner, String messageId) {
        this.owner = owner;
        this.messageId = messageId;
        this.id = owner+"_"+messageId;
    }

    // getters/setters
    public String getId() { return id; }
    public String getOwner() { return owner; }
    public String getMessageId() { return messageId; }

    public void setId(String id) { this.id = id; }
    public void setOwner(String owner) { this.owner = owner; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
}