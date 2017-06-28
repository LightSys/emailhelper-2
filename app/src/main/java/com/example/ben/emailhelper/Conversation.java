package com.example.ben.emailhelper;

/**
 * Created by nicholasweg on 6/28/17.
 */

public class Conversation {
    private String email, name, time;

    public Conversation() {}

    public Conversation(String email, String name, String time) {
        this.email = email;
        this.name = name;
        this.time = time;
    }

    public String getEmail() {return email;}
    public String getName() {return name;}
    public String getTime() {return time;}

    public void setEmail(String email) {this.email = email;}
    public void setName(String name) {this.name = name;}
    public void setTime(String time) {this.time = time;}
}