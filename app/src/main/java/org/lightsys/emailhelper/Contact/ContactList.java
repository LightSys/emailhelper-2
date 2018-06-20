package org.lightsys.emailhelper.Contact;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ContactList {
    private emailHelperList contactList;
    public ContactList(){
        contactList = new emailHelperList();
    }
    public void add(String email){
       contactList.add(email,false);
    }
    public void add(String email,boolean inContacts){
        contactList.add(email,inContacts);
    }

    private ContactListItem find(String email){
        for(int i = 0;i<contactList.size();i++){
            ContactListItem temp = contactList.get(i);
            if(temp != null){
                return temp;
            }
        }
        return null;
    }
    public int size(){
        return contactList.size();
    }

    public boolean contains(String email) {
        for(int i = 0;i<contactList.size();i++){
            ContactListItem temp = contactList.get(i);
            if(temp != null){
                return true;
            }
        }
        return false;
    }
    public ContactListItem get(int index){
        return contactList.get(index);
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

    private class emailHelperList{
        private int size;
        private int maxSize;
        ContactListItem[] array;

        public emailHelperList(){
            size = 0;
            maxSize = 10;
            array = new ContactListItem[maxSize];
        }
        public int size(){
            return size;
        }
        private void resize(){
            maxSize *=2;
            ContactListItem[] tempArray = new ContactListItem[maxSize];
            for(int i = 0;i<size;i++){
                tempArray[i] = array[i];
            }
            array = tempArray;
        }
        public void clear(){
            array = new ContactListItem[10];
            maxSize = 10;
            size = 0;
        }

        public void add(String email, boolean inContacts) {
            size++;
            if(size+1 >maxSize){
                resize();
            }
            for(int i = 0;i<size;i++){
                if(array[i] == null){
                    array[i] = new ContactListItem(email,inContacts);

                    return;
                }
                if(array[i].getContactEmail().equalsIgnoreCase(email)){
                    array[i].tick();
                    size--;
                    sort(i);
                    return;
                }
            }
            int tmep = 1;
        }
        private void sort(int size){//Merge sort? Optimize?
            for(int i=size;i>0;i--){
                if(array[i].numOfReferences>array[i-1].numOfReferences){
                    ContactListItem temp = array[i];
                    array[i]=array[i-1];
                    array[i-1]=temp;
                    int pause = 1;
                }else{
                    return;
                }
            }
        }
        public ContactListItem get(int index){
            if(index >= size || index <0){
                return null;
            }else{
                return array[index];
            }
        }
    }
}
