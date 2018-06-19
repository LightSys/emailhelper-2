package org.lightsys.emailhelper.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactList {
    private List<ContactListItem> contactList;
    public ContactList(){
        contactList = new ArrayList<>();
    }

    public void add(String email){
        ContactListItem temp = new ContactListItem(email);
        if(contactList.contains(temp)){
            ContactListItem changer = contactList.get(contactList.indexOf(temp));
            changer.tick();
        }
        else{
            contactList.add(temp);
        }
    }

    public ContactListItem get(int index){
        return contactList.get(index);
    }

    public class ContactListItem{
        String contactEmail;
        int numOfReferences;
        ContactListItem(){
            contactEmail = "";
            numOfReferences = 0;
        }
        ContactListItem(String email){
            contactEmail = email;
            numOfReferences = 0;
        }
        public void tick(){
            numOfReferences++;
        }
        public int getNumOfReferences(){
            return numOfReferences;
        }
    }
}
