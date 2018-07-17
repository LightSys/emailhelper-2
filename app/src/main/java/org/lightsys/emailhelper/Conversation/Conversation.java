package org.lightsys.emailhelper.Conversation;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by nicholasweg on 6/28/17.
 */

public class Conversation {
    private String email, name;
    private Boolean newMail;

    public Conversation() {}

    public Conversation(String email, String name) {
        this.email = email;
        this.name = name;
        this.newMail = false;

    }
    public Conversation(String email, String name,boolean newmail){
        this.email = email;
        this.name = name;
        this.newMail = newmail;
    }
    public Conversation(String email, String name, String time,String lastDate, boolean newmail){
        this.email = email;
        this.name = name;
        this.newMail = newmail;
    }

    public String getEmail() {return email;}
    public String getName() {return name;}
    public boolean getMailStatus(){return newMail;}

    public void setEmail(String email) {this.email = email;}
    public void setName(String name) {this.name = name;}

    public void setNewMail(){this.newMail =true;}
    public void resetNewMail(){this.newMail = false;}
}