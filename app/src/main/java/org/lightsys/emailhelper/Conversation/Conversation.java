package org.lightsys.emailhelper.Conversation;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by nicholasweg on 6/28/17.
 */

public class Conversation {
    private String email, name, time,lastDate;
    private Date today;
    private Boolean newMail;

    public Conversation() {}

    public Conversation(String email, String name, String time) {
        this.email = email;
        this.name = name;
        this.time = time;
        this.lastDate = "";
        this.today = Calendar.getInstance().getTime();
        this.newMail = false;

    }
    public Conversation(String email, String name, String time, boolean newmail){
        this.email = email;
        this.name = name;
        this.time = time;
        this.lastDate = "";
        this.today = Calendar.getInstance().getTime();
        this.newMail = newmail;
    }
    public Conversation(String email, String name, String time,String lastDate, boolean newmail){
        this.email = email;
        this.name = name;
        this.time = time;
        this.lastDate = lastDate;
        this.today = Calendar.getInstance().getTime();
        this.newMail = newmail;
    }

    public String getEmail() {return email;}
    public String getName() {return name;}
    public String getTime() {return time;}
    public boolean getMailStatus(){return newMail;}
    public String getLastDate(){return lastDate;}
    public Date   getToday() {return today;}

    public void setEmail(String email) {this.email = email;}
    public void setName(String name) {this.name = name;}
    public void setTime(String time) {this.time = time;}
    public void setLastDate(String today) {this.lastDate = today;}
    public void setToday(Date today) {this.today = today;}

    public void setNewMail(){this.newMail =true;}
    public void resetNewMail(){this.newMail = false;}
}