package org.lightsys.emailhelper.Conversation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by nicholasweg on 6/28/17.
 */

public class Conversation {
    private String email, name, time;
    private Date today;

    public Conversation() {}

    /**********************************************************************************************
     *  TODO: Change the date field                                                               *
     *  I added the today field so that when pulling emails, you can stop at the day the message  *
     *  was created and not look past that. When the conversation is made, it inserts the current *
     *  date and time into the field, which is the same format that JavaMail uses as a            *
     *  ReceivedDateTerm. The time field is also going to need to change to show when the most    *
     *  recent message was received, not the time that the conversation was created.              *
     *  -Nick                                                                                     *
     *  Modifiers for the today field were added                                                  *
     *  -Shade (May 18)                                                                           *
     **********************************************************************************************/

    public Conversation(String email, String name, String time) {
        this.email = email;
        this.name = name;
        this.time = time;
        this.today = Calendar.getInstance().getTime();
    }

    public String getEmail() {return email;}
    public String getName() {return name;}
    public String getTime() {return time;}
    public Date   getToday() {return today;}

    public void setEmail(String email) {this.email = email;}
    public void setName(String name) {this.name = name;}
    public void setTime(String time) {this.time = time;}
    public void setToday(Date today) {this.today = today;}
}