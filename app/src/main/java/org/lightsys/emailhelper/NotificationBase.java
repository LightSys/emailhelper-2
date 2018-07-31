package org.lightsys.emailhelper;

public class NotificationBase {
    private String Title;
    private String Subject;
    private String Email;
    NotificationBase(String title,String subject,String email){
        Title = title;
        Subject = subject;
        Email = email;
    }
    public String getTitle(){
        return Title;
    }
    public String getSubject(){
        return Subject;
    }
    public String getEmail(){return Email;}
}
