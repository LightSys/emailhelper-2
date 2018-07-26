package org.lightsys.emailhelper.Contact;

/**
 * This class was designed as a list that would automatically sort based on the numReferences trait
 * of the Contact class.
 * @author DSHADE
 */
public class ContactList {
    private int size;
    private int maxSize;
    Contact[] array;
    public ContactList(){
        size = 0;
        maxSize = 10;
        array = new Contact[maxSize];
    }

    public ContactList(ContactList contactList) {
        size = contactList.size;
        maxSize = contactList.maxSize;
        array = new Contact[maxSize];
        for(int i = 0;i<size;i++){
            array[i] = contactList.array[i];
        }
    }

    /**
     * This function adds the contact to the list with 1 reference.
     */
    public void add(String name,String email){
        //This function is for adding inbox contacts
        size++;
        if(size+1 >maxSize){
            resize();
        }
        for(int i = 0;i<size;i++){
            if(array[i] == null){
                array[i] = new Contact(email,name,"",null,null,false,false,1);
                return;
            }
            if(array[i].getEmail().equalsIgnoreCase(email)){
                array[i].tick();
                size--;
                sort(i);
                return;
            }
        }
    }


    /**
     * Adds the contact to the list
     */
    public void add(Contact contact){
        size++;
        if(size+1 >maxSize){
            resize();
        }
        for(int i = 0;i<size;i++){
            if(array[i] == null){
                array[i] = new Contact(contact);
                sort(i);
                return;
            }
            if(array[i].getEmail().equalsIgnoreCase(contact.getEmail())){
                array[i].increaseReferences(contact.getNumOfReferences());
                if(contact.getInContacts()){
                    array[i].setInContacts(contact.getInContacts());
                }
                size--;
                sort(i);
                return;
            }
        }
    }

    /**
     * Addes a contact list to the list by adding the individual contacts.
     */
    public void add(ContactList contactList){
        for(int i = 0;i<contactList.size();i++){
            add(contactList.get(i));
        }
    }
    public int size(){
        return size;
    }
    public Contact get(int index){
        if(index >= size || index <0){
            return null;
        }else{
            return array[index];
        }
    }
    public void delete(Contact contact){
        int hold = 0;
        for(int i = 0;i<size;i++){
            if(array[i].getEmail().equalsIgnoreCase(contact.getEmail())){
                array[i] = null;
                hold = i;
                i = size;
            }
        }
        for(int i = hold;i<size-1;i++){
            array[i] = array[i+1];
        }
        size--;
    }
    public void resetInDatabase(Contact contact){
        for(int i = 0;i<size;i++){
            if(array[i].getEmail().equalsIgnoreCase(contact.getEmail())){
                array[i].setInContacts(false);
                if(array[i].getNumOfReferences()==0){
                    delete(contact);
                }
                return;
            }
        }
    }

    private void resize(){
        maxSize *=2;
        Contact[] tempArray = new Contact[maxSize];
        for(int i = 0;i<size;i++){
            tempArray[i] = array[i];
        }
        array = tempArray;
    }
    private void sort(int size){
        for(int i=size;i>0;i--){
            if(array[i].getNumOfReferences()>array[i-1].getNumOfReferences()){
                Contact temp = array[i];
                array[i]=array[i-1];
                array[i-1]=temp;
            }else{
                return;
            }
        }
    }



}
