package org.lightsys.emailhelper.Conversation;

import java.util.Date;

/**
 * The Message class is built for holding the messages.
 * Created by nicholasweg on 7/7/17.
 * Edited and Renamed from ConversationWindow by DSHADE Summer 2018.
 */

public class Message {
    private String email, name, messageId,subject, text;

    private int sent_by_me;
    private Boolean hasAttachments;
    private Date sentDate;

    public static final int SENT_BY_OTHER = 1;
    public static final int SENT_BY_ME = -1;
    public static final int TIME = 0;

    public Message() {}

    public Message(String email, String name,String subject, String message, String messageId, int sent_by_me, Boolean hasAttachments,Date sentDate) {
        this.email = email;
        this.name = name;
        this.subject = subject;
        this.text = message;
        this.messageId = messageId;
        this.sent_by_me = sent_by_me;
        this.hasAttachments = hasAttachments;
        this.sentDate = sentDate;

    }

    public String getEmail() {return email;}
    public String getName() {return name;}
    public String getSubject(){return subject;}
    public String getMessage() {return text;}
    public String getMessageId() {return messageId;}
    public int getSent() {return sent_by_me;}
    public boolean hasAttachments(){return hasAttachments;}
    public Date getSentDate(){return sentDate;}

    public void setEmail(String email) {this.email = email;}
    public void setName(String name) {this.name = name;}
    public void setSubject(String subject){this.subject = subject;}
    public void setMessage(String message) {this.text = message;}
    public void setHasAttachments(Boolean hasAttachments) {this.hasAttachments = hasAttachments;}
    public void setMessageId(String messageId) {this.messageId = messageId;}
    public void setSent(int sent_by_me) {this.sent_by_me = sent_by_me;}
    public void setSentDate(Date sentDate){this.sentDate = sentDate;}
}
