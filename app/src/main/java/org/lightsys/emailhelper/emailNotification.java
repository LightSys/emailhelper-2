package org.lightsys.emailhelper;

import java.util.Stack;

public class emailNotification {
    private boolean gotMail;
    private boolean invalid_credentials;
    private Stack<NotificationBase> newEmails;

    public emailNotification(){
        gotMail = false;
        newEmails = new Stack<>();
        invalid_credentials = false;
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
    public NotificationBase pop() {
        if (newEmails.size() == 1) {
            gotMail = false;
        }
        return newEmails.pop();
    }
    public void setInvalid_Credentials() {
        this.invalid_credentials = true;
    }
    public void resetInvalid_Credentials(){
        this.invalid_credentials = false;
    }
    public boolean getInvalid_Credentials(){
        return invalid_credentials;
    }
}
