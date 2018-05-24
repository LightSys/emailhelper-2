package org.lightsys.emailhelper.Conversation;

/**
 * Created by nicholasweg on 7/7/17.
 */

public class ConversationWindow {
    private String email, name, messageId, message;

    private Boolean sent_by_me;

    public ConversationWindow() {}

    public ConversationWindow(String email, String name, String message, String messageId, Boolean sent_by_me) {
        this.email = email;
        this.name = name;
        this.message = message;
        this.messageId = messageId;
        this.sent_by_me = sent_by_me;
    }

    public String getEmail() {return email;}
    public String getName() {return name;}
    public String getMessage() {return message;}
    public String getMessageId() {return messageId;}
    public Boolean getSent() {return sent_by_me;}

    public void setEmail(String email) {this.email = email;}
    public void setName(String name) {this.name = name;}
    public void setMessage(String message) {this.message = message;}
    public void setMessageId(String messageId) {this.messageId = messageId;}
    public void setSent(Boolean sent_by_me) {this.sent_by_me = sent_by_me;}
}
