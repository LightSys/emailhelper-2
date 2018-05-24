package org.lightsys.emailhelper;

import android.app.NotificationChannel;

import java.util.Stack;

public class emailNotification {
    private boolean gotMail;
    private Stack<NotificationBase> newEmails;

    public emailNotification(){
        gotMail = false;
        newEmails = new Stack<>();
    }
    public boolean status(){
        return gotMail;
    }
    public int size(){
        return newEmails.size();
    }

    public void push(String title,String subject){
        newEmails.push(new NotificationBase(title,subject));
        if(newEmails.size()>0){
            gotMail = true;
        }
    }
    public NotificationBase pop(){
        if(newEmails.size()==1){
            gotMail = false;
        }
        return newEmails.pop();

    }




}
