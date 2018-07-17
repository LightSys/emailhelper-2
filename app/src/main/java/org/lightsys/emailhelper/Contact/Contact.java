package org.lightsys.emailhelper.Contact;

import org.lightsys.emailhelper.CommonMethods;

import java.util.Date;

/**
 * Created by nicholasweg on 6/30/17.
 */

public class Contact {
    private String email, firstName, lastName;
    private Date createdDate, updatedDate;
    private boolean inContacts,sendNotifications;
    private int numOfReferences;

    public Contact() {
        email = "";
        firstName = "";
        lastName = "";
        createdDate = CommonMethods.getCurrentTime();
        updatedDate = CommonMethods.getCurrentTime();
        inContacts = true;
        sendNotifications = true;
        numOfReferences = 0;
    }

    public Contact(String email, String firstName, String lastName,Date createdDate,Date updatedDate,boolean sendNotifications,boolean inContacts,int numOfReferences) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.sendNotifications = sendNotifications;
        this.inContacts = inContacts;
        this.numOfReferences = numOfReferences;
    }

    public Contact(Contact contact) {
        this.email = contact.email;
        this.firstName = contact.firstName;
        this.lastName = contact.lastName;
        this.createdDate = contact.createdDate;
        this.updatedDate = contact.updatedDate;
        this.sendNotifications = contact.sendNotifications;
        this.inContacts = contact.inContacts;
        this.numOfReferences = contact.numOfReferences;
    }

    public String getEmail() {return email;}
    public String getFirstName() {return firstName;}
    public String getLastName() {return lastName;}
    public Date getCreatedDate(){return createdDate;}
    public Date getUpdatedDate(){return updatedDate;}
    public boolean getSendNotifications(){return sendNotifications;}
    public boolean getInContacts(){return inContacts;}
    public int getNumOfReferences(){return numOfReferences;}


    public void setEmail(String email) {this.email = email;}
    public void setFirstName(String firstName) {this.firstName = firstName;}
    public void setLastName(String lastName) {this.lastName = lastName;}
    public void setCreatedDate(Date createdDate){this.createdDate = createdDate;}
    public void setUpdatedDate(Date updatedDate){this.updatedDate = updatedDate;}
    public void setSendNotifications(boolean sendNotifications){this.sendNotifications = sendNotifications;}
    public void setInContacts(boolean inContacts){this.inContacts = inContacts;}
    public void setNumOfReferences(int numOfReferences){this.numOfReferences = numOfReferences;}
    public void tick(){numOfReferences++;}

    public String getName() {return firstName + " " +lastName;}
    public void increaseReferences(int numOfReferences){this.numOfReferences += numOfReferences;}
}
