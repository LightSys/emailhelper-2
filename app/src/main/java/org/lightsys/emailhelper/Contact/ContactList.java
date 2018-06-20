package org.lightsys.emailhelper.Contact;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class ContactList {
    private PriorityQueue<ContactListItem> contactList;
    public ContactList(){
        contactList = new PriorityQueue<>(8,new myComparator());
    }

    public void add(String email){
        ContactListItem temp = new ContactListItem(email);
        if(contains(email)){
            ContactListItem changer = find(email);
            changer.tick();
        }
        else{
            contactList.add(temp);
        }
    }
    public void add(String email,boolean inContacts){
        ContactListItem temp = new ContactListItem(email,inContacts);
        if(contains(email)){
            ContactListItem changer = find(email);
            changer.tick();
        }
        else{
            contactList.add(temp);
        }
    }

    private ContactListItem find(String email){
        ContactListItem ret;
        Iterator<ContactListItem> temp = contactList.iterator();
        while(temp.hasNext()){
            ret = temp.next();
            if(ret.contactEmail.equalsIgnoreCase(email)){
                return ret;
            }
        }
        return null;
    }
    public int size(){
        return contactList.size();
    }

    public boolean contains(String email) {
        Iterator<ContactListItem> temp = contactList.iterator();
        while(temp.hasNext()){
            ContactListItem item = temp.next();
            if(item.contactEmail.equalsIgnoreCase(email)){
                return true;
            }
        }
        return false;
    }
    public ContactListItem get(int index){
        Object[] myArray =  contactList.toArray();
        if(contactList.size()-1<index){
            return null;
        }
        else
            return (ContactListItem) myArray[index];
    }

    public class ContactListItem{
        String contactEmail;
        int numOfReferences;
        Boolean  inContacts;
        ContactListItem(){
            contactEmail = "";
            numOfReferences = 1;
            inContacts = false;
        }
        ContactListItem(String email){
            contactEmail = email;
            numOfReferences = 1;
            inContacts = false;
        }
        ContactListItem(String email, boolean inContacts){
            contactEmail = email;
            numOfReferences = 1;
            this.inContacts = inContacts;
        }
        public void tick(){
            numOfReferences++;
        }
        public String getContactEmail(){
            return contactEmail;
        }
        public int getNumOfReferences(){
            return numOfReferences;
        }
        public void setInContacts(){
            inContacts = true;
            numOfReferences = 0;
        }
    }
    public void clear(){
        contactList.clear();
    }
    class myComparator implements Comparator<ContactListItem> {

        @Override
        public int compare(ContactListItem o1, ContactListItem o2) {
            if(o1.contactEmail.equalsIgnoreCase(o2.contactEmail)){
                return 0;
            }
            if(o1.inContacts && !o2.inContacts){
                return 1;
            }
            else if(!o1.inContacts && o2.inContacts){
                return -1;
            }
            else{
                return o1.numOfReferences-o2.numOfReferences;
            }
        }
    }
}
