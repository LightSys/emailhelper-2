package org.lightsys.emailhelper.Contact;

public class ContactList {
    private emailHelperList addressList;
    public ContactList(){
        addressList = new emailHelperList();
    }

    public ContactList(ContactList contactList) {
        addressList = new emailHelperList(contactList.addressList);
    }

    public void add(String email){
        addressList.add(email);
    }


    public void add(Contact contact){
        addressList.add(contact);
    }
    public void add(ContactList contactList){
        for(int i = 0;i<contactList.size();i++){
            addressList.add(contactList.get(i));
        }
    }
    public int size(){
        return addressList.size();
    }
    public Contact get(int index){
        return addressList.get(index);
    }


    private class emailHelperList{
        private int size;
        private int maxSize;
        Contact[] array;

        emailHelperList(){
            size = 0;
            maxSize = 10;
            array = new Contact[maxSize];
        }
        emailHelperList(emailHelperList e){
            size = e.size;
            maxSize = e.maxSize;
            array = new Contact[maxSize];
            for(int i = 0;i<size;i++){
                array[i] = e.array[i];
            }
        }
        public int size(){
            return size;
        }
        private void resize(){
            maxSize *=2;
            Contact[] tempArray = new Contact[maxSize];
            for(int i = 0;i<size;i++){
                tempArray[i] = array[i];
            }
            array = tempArray;
        }

        public void add(Contact contact) {
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
                    size--;
                    sort(i);
                    return;
                }
            }
        }
        public void add(String email) {
            size++;
            if(size+1 >maxSize){
                resize();
            }
            for(int i = 0;i<size;i++){
                if(array[i] == null){
                    array[i] = new Contact(email,"","",1,false);
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

        private void sort(int size){
            for(int i=size;i>0;i--){
                if(array[i].numOfReferences>array[i-1].numOfReferences){
                    Contact temp = array[i];
                    array[i]=array[i-1];
                    array[i-1]=temp;
                }else{
                    return;
                }
            }
        }
        public Contact get(int index){
            if(index >= size || index <0){
                return null;
            }else{
                return array[index];
            }
        }
    }
}
