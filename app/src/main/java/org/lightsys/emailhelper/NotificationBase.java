package org.lightsys.emailhelper;

public class NotificationBase {
    private String Title;
    private String Subject;
    NotificationBase(String title,String subject){
        Title = title;
        Subject = subject;
    }
    public String getTitle(){
        return Title;
    }
    public String getSubject(){
        return Subject;
    }
}
