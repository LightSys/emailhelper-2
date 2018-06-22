package org.lightsys.emailhelper.Contact;

/**
 * Created by nicholasweg on 6/30/17.
 */

public class Contact {
    private String email, firstName, lastName;
    boolean inContacts;
    int numOfReferences;

    public Contact() {}

    public Contact(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        inContacts = true;
        numOfReferences = 0;
    }
    public Contact(String email, String firstName,String lastName, int numOfReferences,boolean inContacts){
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.inContacts = inContacts;
        this.numOfReferences = numOfReferences;
    }

    public Contact(Contact contact) {
        this.email = contact.email;
        this.firstName = contact.firstName;
        this.lastName = contact.lastName;
        this.inContacts = contact.inContacts;
        this.numOfReferences = contact.numOfReferences;
    }

    public String getEmail() {return email;}
    public String getFirstName() {return firstName;}
    public String getLastName() {return lastName;}
    public boolean getInContacts(){return inContacts;}
    public int getNumOfReferences(){return numOfReferences;}


    public void setEmail(String email) {this.email = email;}
    public void setFirstName(String firstName) {this.firstName = firstName;}
    public void setLastName(String lastName) {this.lastName = lastName;}
    public void tick(){numOfReferences++;}

    public String getName() {
        return firstName + " " +lastName;
    }

    public void increaseReferences(int numOfReferences) {
        this.numOfReferences += numOfReferences;
    }
}
