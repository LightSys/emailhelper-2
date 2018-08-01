package org.lightsys.emailhelper;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.lightsys.emailhelper.Contact.Contact;
import org.lightsys.emailhelper.Contact.ContactList;
import org.lightsys.emailhelper.Conversation.Conversation;
import org.lightsys.emailhelper.Conversation.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import xdroid.toaster.Toaster;

/**************************************************************************************************
 *                              Created by nicholasweg on 6/27/17.                                *
 *  Any changes made to this file regarding the database structure won't take effect unless you   *
 *  reinstall the app. The onCreate function only runs the first time the app is made. There may  *
 *  be a need to writed update functions so that won't have to happen if that situation comes up  *
 *  in the future. Otherwise you can use the onUpgrade function.                                  *
 **************************************************************************************************
 *
 * Correction to the above comment. While changes to the app while developement should be used with
 * a reinstall, you must watch for changes to the database on a new version. This changes should be
 * onUpgrade as a new case statement.
 * -DSHADE
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "EmailHelper.db";
    // Conversation variables
    public static final String CONVERSATION_TABLE_NAME = "active_messages";
    public static String CONVO_COL_1 = "EMAIL";
    public static String CONVO_COL_2 = "NAME";
    public static String CONVO_COL_3 = "NEW_MAIL";
    // Contact variables
    public static final String CONTACT_TABLE_NAME = "saved_contacts";
    public static String CONTACT_COL_1 = "EMAIL";
    public static String CONTACT_COL_2 = "FIRSTNAME";
    public static String CONTACT_COL_3 = "LASTNAME";
    public static String CONTACT_COL_4 = "CREATED_DATE";
    public static String CONTACT_COL_5 = "UPDATED_DATE";
    public static String CONTACT_COL_6 = "NOTIFICATION_BOOL";

    // Conversation Window variables
    public static final String MESSAGE_TABLE_NAME = "conversation_window";
    public static String MESSAGE_COL_1 = "EMAIL";
    public static String MESSAGE_COL_2 = "NAME";
    public static String MESSAGE_COL_3 = "SUBJECT";
    public static String MESSAGE_COL_4 = "MESSAGE";
    public static String MESSAGE_COL_5 = "HAS_ATTACHMENT";
    public static String MESSAGE_COL_6 = "MESSAGE_ID";
    public static String MESSAGE_COL_7 = "SENT_BY_ME";
    public static String MESSAGE_COL_8 = "SENT_DATE";
    public static String MESSAGE_COL_9 = "DB_ID";

    // Attachment variables
    public static final String ATTACHMENT_DATABASE = "attachment_database";
    public static String ATTACHMENT_COL_1 = "DATABASE_ID";
    public static String ATTACHMENT_COL_2 = "EMAIL";
    public static String ATTACHMENT_COL_3 = "ATTACHMENT";
    public static String ATTACHMENT_COL_4 = "MESSAGE_ID";
    private Resources r;

    private static final int versionNumber = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, versionNumber);
        r = context.getResources();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //This function only runs the first time the app is run. See comment above.
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //new code will go here for the upgrade
        //Use the Alter Table table_name ADD new_column_name column_default_data
        //find out more at https://www.techonthenet.com/sqlite/tables/alter_table.php
        switch(newVersion){
            case 2:
                db.execSQL("ALTER TABLE "+MESSAGE_TABLE_NAME+" ADD "+MESSAGE_COL_8+" TEXT");
        }
    }

    private void createTables(SQLiteDatabase db){
        String conversationQuery = String.format("create table " + CONVERSATION_TABLE_NAME + " ( %s TEXT PRIMARY KEY, %s TEXT, %s BOOL)", CONVO_COL_1, CONVO_COL_2, CONVO_COL_3);
        String contactQuery = String.format("create table " + CONTACT_TABLE_NAME + " ( %s TEXT PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT, %s TEXT,%s BOOL)", CONTACT_COL_1, CONTACT_COL_2, CONTACT_COL_3, CONTACT_COL_4,CONTACT_COL_5,CONTACT_COL_6);
        String windowQuery = String.format("create table " + MESSAGE_TABLE_NAME + " ( %s TEXT, %s TEXT, %s TEXT, %s TEXT,%s BOOLEAN, %s TEXT, %s BOOLEAN, %s TEXT, %s INTEGER PRIMARY KEY AUTOINCREMENT)", MESSAGE_COL_1, MESSAGE_COL_2, MESSAGE_COL_3, MESSAGE_COL_4, MESSAGE_COL_5, MESSAGE_COL_6, MESSAGE_COL_7,MESSAGE_COL_8, MESSAGE_COL_9);
        String attachmentQuery = String.format("create table " + ATTACHMENT_DATABASE + " ( %s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT)", ATTACHMENT_COL_1, ATTACHMENT_COL_2, ATTACHMENT_COL_3, ATTACHMENT_COL_4);
        db.execSQL(conversationQuery);
        db.execSQL(contactQuery);
        db.execSQL(windowQuery);
        db.execSQL(attachmentQuery);
    }

    //<editor-fold>  Conversation Functions
    public void insertConversationData(String email, String name) {

        insertConversationData(email,name,false);
    }

    /**
     * @param email   email of the new Conversation
     * @param name    name of the new Conversation
     * @param newmail whether there is newMail from this email
     * @return returns if the conversation was inserted into the Database
     */
    public void insertConversationData(String email, String name,boolean newmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, email);
        contentValues.put(CONVO_COL_2, name);
        contentValues.put(CONVO_COL_3, newmail);
        long result = db.insert(CONVERSATION_TABLE_NAME, null, contentValues);
        if( result != -1){
            Toaster.toast(r.getString(R.string.conversation_added_prestring)+name+r.getString(R.string.conversation_added_poststring));
        }
        else{
            Toaster.toast(r.getString(R.string.conversation_not_added_prestring)+name+r.getString(R.string.conversation_not_added_poststring));
        }
    }

    /**
     * @param newContact contact info of the new Conversation
     * @return returns if the conversation was inserted into the Database
     */
    public void insertConversationData(Contact newContact) {
        insertConversationData(newContact,false);
    }

    public void insertConversationData(Contact newContact,boolean newmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, newContact.getEmail());
        contentValues.put(CONVO_COL_2, newContact.getFirstName() + " " + newContact.getLastName());
        contentValues.put(CONVO_COL_3, newmail);
        long result = db.insert(CONVERSATION_TABLE_NAME, null, contentValues);
        if( result != -1){
            Toaster.toast(r.getString(R.string.conversation_added_prestring)+newContact.getName()+r.getString(R.string.conversation_added_poststring));
        }
        else{
            Toaster.toast(r.getString(R.string.conversation_not_added_prestring)+newContact.getName()+r.getString(R.string.conversation_not_added_poststring));
        }
    }

    /**
     * @param email email for the settings to be reset
     */
    public void resetNewMailBoolean(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from " + CONVERSATION_TABLE_NAME + " where EMAIL = ?";
        Cursor res = db.rawQuery(query, new String[]{email});
        res.moveToNext();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, email);//Use of email so there is not an extra function call
        contentValues.put(CONVO_COL_2, res.getString(res.getColumnIndex(CONVO_COL_2)));
        contentValues.put(CONVO_COL_3, false);//Updates the time
        db.update(CONVERSATION_TABLE_NAME, contentValues, "EMAIL = ?", new String[]{email});
        res.close();
    }
    /**
     * @param email email for the settings to be reset
     */
    public void setNewMailBoolean(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from " + CONVERSATION_TABLE_NAME + " where EMAIL = ?";
        Cursor res = db.rawQuery(query, new String[]{email});
        res.moveToNext();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, email);//Use of email so there is not an extra function call
        contentValues.put(CONVO_COL_2, res.getString(res.getColumnIndex(CONVO_COL_2)));
        contentValues.put(CONVO_COL_3, true);//Updates the time
        db.update(CONVERSATION_TABLE_NAME, contentValues, "EMAIL = ?", new String[]{email});
        res.close();
    }

    /**
     * Deletes the conversation from the Database
     *
     * @param email email to be deleted
     * @return returns if the conversation was deleted from the Database
     */
    public Integer deleteConversationData(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGE_TABLE_NAME, "EMAIL = ?", new String[]{email});
        deleteMessages(email);//delete the messages in the Conversation from app
        if(hasAttachments(email)){
            deleteAttachments(email);
        }
        return db.delete(CONVERSATION_TABLE_NAME, "EMAIL = ?", new String[]{email});
    }


    public List<Conversation> getConversations() {
        Log.d("Database","Conversations Accessed");
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + CONVERSATION_TABLE_NAME, null);
        Stack<Conversation> temp = new Stack<>();
        while (res.moveToNext()) {
            Conversation conversation = new Conversation();
            try{
                conversation.setEmail(res.getString(res.getColumnIndex(CONVO_COL_1)));
                conversation.setName(res.getString(res.getColumnIndex(CONVO_COL_2)));
                conversation.setNewMail(1==res.getInt(res.getColumnIndex(CONVO_COL_3)));
            }
            catch (Exception e){

            }
            finally {
                temp.push(conversation);
            }
        }
        List conversationList = new ArrayList<>();
        while (!temp.isEmpty()) {
            conversationList.add(temp.pop());
        }
        res.close();
        return conversationList;
    }
    public boolean hasConversationWith(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from " + CONVERSATION_TABLE_NAME + " where EMAIL = ?";
        Cursor res = db.rawQuery(query, new String[]{email});
        int temp = res.getCount();
        res.close();
        if(temp>0){
            return true;
        }
        else{
            return false;
        }

    }
    private void updateConversation(String originalEmail, Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from " + CONVERSATION_TABLE_NAME + " where EMAIL = ?";
        Cursor res = db.rawQuery(query, new String[]{originalEmail});
        res.moveToNext();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, contact.getEmail());
        contentValues.put(CONVO_COL_2, contact.getName());
        contentValues.put(CONVO_COL_3, res.getString(res.getColumnIndex(CONVO_COL_3)));
        db.update(CONVERSATION_TABLE_NAME,contentValues,"EMAIL = ?",new String[]{originalEmail});
    }

    //</editor-fold>

    //<editor-fold>  Contact Functions

    /**
     * Adds the Contact to Database
     *
     * @param newContact the contact to be added to the database
     */
    public void insertContact(Contact newContact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACT_COL_1, newContact.getEmail());
        contentValues.put(CONTACT_COL_2, newContact.getFirstName());
        contentValues.put(CONTACT_COL_3, newContact.getLastName());
        contentValues.put(CONTACT_COL_4, CommonMethods.dateToString(newContact.getCreatedDate()));
        contentValues.put(CONTACT_COL_5, CommonMethods.dateToString(newContact.getUpdatedDate()));
        contentValues.put(CONTACT_COL_6,newContact.getSendNotifications());
        long result = db.insert(CONTACT_TABLE_NAME, null, contentValues);
        if (result != -1) {
            Toaster.toast(r.getString(R.string.contact_added_prestring)+newContact.getName()+r.getString(R.string.contact_added_poststring));
        }else{
            Toaster.toast(r.getString(R.string.contact_not_added_prestring)+newContact.getName()+r.getString(R.string.contact_not_added_poststring));
        }
    }


    public boolean containsContact(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        try {
            cursor = db.rawQuery("select * from saved_contacts where EMAIL = ?", new String[]{email}, null);
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
        } catch (Exception e) {
            System.out.print(e);
            return false;
        }
        return false;
    }

    /**
     * This function get returns the contact info for a given email.
     *
     * @return
     */
    public Contact getContact(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + CONTACT_TABLE_NAME + " WHERE EMAIL = ?", new String[]{email});
        res.moveToNext();
        if(res.getCount()<=0){
            return null;
        }
        Contact contact = new Contact();
        contact.setEmail(email);
        contact.setFirstName(res.getString(res.getColumnIndex(CONTACT_COL_2)));
        contact.setLastName(res.getString(res.getColumnIndex(CONTACT_COL_3)));
        contact.setCreatedDate(CommonMethods.stringToDate(res.getString(res.getColumnIndex(CONTACT_COL_4))));
        contact.setUpdatedDate(CommonMethods.stringToDate(res.getString(res.getColumnIndex(CONTACT_COL_5))));
        contact.setSendNotifications(1==res.getInt(res.getColumnIndex(CONTACT_COL_6)));
        contact.setInContacts(true);
        contact.setNumOfReferences(0);
        return contact;
    }

    /**
     * @return the name associated with the given email
     */
    public String getContactName(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String name = "";
        Cursor res = db.rawQuery("select * from saved_contacts where EMAIL = ?", new String[]{email}, null);
        if (res.moveToNext()) {
            String firstname = res.getString(res.getColumnIndex(CONTACT_COL_2));
            String lastname = res.getString(res.getColumnIndex(CONTACT_COL_3));
            name = firstname + " " + lastname;
        }
        res.close();
        return name;
    }


    public void deleteContactData(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(CONTACT_TABLE_NAME, "EMAIL = ?", new String[]{email});
        if(hasConversationWith(email)){
            deleteConversationData(email);
            deleteMessages(email);//delete the messages in the Conversation from app
        }
        if(hasAttachments(email)){
            deleteAttachments(email);
        }

    }

    /**
     * @return The contacts from the database in a list
     */
    public ContactList getContactList() {
        Log.d("Database","Contacts Accessed");
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + CONTACT_TABLE_NAME, null);
        ContactList contactList = new ContactList();
        while(res.moveToNext()){
            Contact contact = new Contact();
            try{
                contact.setEmail(res.getString(res.getColumnIndex(CONTACT_COL_1)));
                contact.setFirstName(res.getString(res.getColumnIndex(CONTACT_COL_2)));
                contact.setLastName(res.getString(res.getColumnIndex(CONTACT_COL_3)));
                contact.setCreatedDate(CommonMethods.stringToDate(res.getString(res.getColumnIndex(CONTACT_COL_4))));
                contact.setUpdatedDate(CommonMethods.stringToDate(res.getString(res.getColumnIndex(CONTACT_COL_5))));
                contact.setSendNotifications(1==res.getInt(res.getColumnIndex(CONTACT_COL_6)));
                contact.setInContacts(true);
                contact.setNumOfReferences(0);
            }
            finally{
                contactList.add(contact);
            }
        }
        res.close();
        return contactList;
    }


    /**
     * @return the createdDate for the given email
     */
    public Date getContactCreatedDate(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from " + CONTACT_TABLE_NAME + " where EMAIL = ?";
        Cursor res = db.rawQuery(query, new String[]{email});
        res.moveToNext();
        String Date = res.getString(res.getColumnIndex(CONTACT_COL_4));
        res.close();
        return CommonMethods.stringToDate(Date);
    }

    public boolean getNotificationSettings(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select "+CONTACT_COL_6+" from "+CONTACT_TABLE_NAME+" where EMAIL = ?";
        Cursor res = db.rawQuery(query,new String[]{email});
        res.moveToNext();
        return 1== res.getInt(res.getColumnIndex(CONTACT_COL_6));
    }
    public void setNotificationSettings(String email,boolean set) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from " + CONTACT_TABLE_NAME + " where EMAIL = ? ";
        Cursor res = db.rawQuery(query, new String[]{email});
        res.moveToNext();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACT_COL_1, email);//Use of email so there is not an extra function call
        contentValues.put(CONTACT_COL_2, res.getString(res.getColumnIndex(CONTACT_COL_2)));
        contentValues.put(CONTACT_COL_3, res.getString(res.getColumnIndex(CONTACT_COL_3)));
        contentValues.put(CONTACT_COL_4, res.getString(res.getColumnIndex(CONTACT_COL_4)));
        contentValues.put(CONTACT_COL_5, res.getString(res.getColumnIndex(CONTACT_COL_5)));
        contentValues.put(CONTACT_COL_6, set);
        db.update(CONTACT_TABLE_NAME,contentValues,"EMAIL = ?",new String[]{email});
    }

    public void updateContact(String originalEmail, Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from " + CONTACT_TABLE_NAME + " where EMAIL=?";
        Cursor res = db.rawQuery(query, new String[]{originalEmail});
        res.moveToNext();
        String originalFirst = res.getString(res.getColumnIndex(CONTACT_COL_2));
        String originalLast = res.getString(res.getColumnIndex(CONTACT_COL_3));
        if(!originalEmail.equalsIgnoreCase(contact.getEmail())){
            updateAttachments(originalEmail,contact.getEmail());
            updateMessages(originalEmail,contact);
            updateConversation(originalEmail,contact);
        }
        else if(!originalFirst.equalsIgnoreCase(contact.getFirstName()) || !originalLast.equalsIgnoreCase(contact.getLastName())){
            updateMessages(originalEmail,contact);
            updateConversation(originalEmail,contact);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACT_COL_1, contact.getEmail());//Use of email so there is not an extra function call
        contentValues.put(CONTACT_COL_2, contact.getFirstName());
        contentValues.put(CONTACT_COL_3, contact.getLastName());
        contentValues.put(CONTACT_COL_4, CommonMethods.dateToString(contact.getCreatedDate()));
        Date oldUpdatedDate = CommonMethods.stringToDate(res.getString(res.getColumnIndex(CONTACT_COL_5)));
        if(oldUpdatedDate.after(contact.getUpdatedDate())&& !res.getString(res.getColumnIndex(CONTACT_COL_4)).equalsIgnoreCase(res.getString(res.getColumnIndex(CONTACT_COL_5)))){
            //if old update is after and created date != updated
            //some protection so that it doesn't get in the wrong order.
            contact.setUpdatedDate(oldUpdatedDate);
        }
        contentValues.put(CONTACT_COL_5, CommonMethods.dateToString(contact.getUpdatedDate()));
        contentValues.put(CONTACT_COL_6, contact.getSendNotifications());
        db.update(CONTACT_TABLE_NAME,contentValues,"EMAIL = ?",new String[]{originalEmail});
    }

    public Date getContactUpdatedDate(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select "+CONTACT_COL_5+" from " + CONTACT_TABLE_NAME + " where EMAIL=?";
        Cursor res = db.rawQuery(query, new String[]{email});
        res.moveToNext();
        Date date = CommonMethods.stringToDate(res.getString(res.getColumnIndex(CONTACT_COL_5)));
        res.close();
        return date;
    }

    //</editor-fold>

    //<editor-fold>  Message Functions

    /**
     * This function adds the message into the database
     */
    public boolean insertMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(!message.getMessageId().equalsIgnoreCase("")) {
            String query = "Select * from " + MESSAGE_TABLE_NAME + " where " + MESSAGE_COL_6 + " = " + message.getMessageId();
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.close();
                return false;
            }
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGE_COL_1, message.getEmail());
        contentValues.put(MESSAGE_COL_2, message.getName());
        contentValues.put(MESSAGE_COL_3, message.getSubject());
        contentValues.put(MESSAGE_COL_4, message.getMessage());
        contentValues.put(MESSAGE_COL_5, message.hasAttachments());
        contentValues.put(MESSAGE_COL_6, message.getMessageId());
        contentValues.put(MESSAGE_COL_7, message.getSent());
        contentValues.put(MESSAGE_COL_8, CommonMethods.dateToString(message.getSentDate()));
        long result = db.insert(MESSAGE_TABLE_NAME, null, contentValues);
        return result != -1;
    }
    public void deleteIncompleteMessage(String content){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGE_TABLE_NAME,MESSAGE_COL_3+" = ? and "+MESSAGE_COL_4+" = ?",new String[]{r.getString(R.string.getSubjectLine),content});
    }
    public List<Message> getMessages(String email) {
        Log.d("Database","Messages Accessed");
        List messageList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + MESSAGE_TABLE_NAME + " where EMAIL = ?", new String[]{email}, null);
        while (res.moveToNext()) {
            String name = res.getString(res.getColumnIndex(MESSAGE_COL_2));
            String subject = res.getString(res.getColumnIndex(MESSAGE_COL_3));
            String text = res.getString(res.getColumnIndex(MESSAGE_COL_4));
            boolean hasAttach = (1 == res.getInt(res.getColumnIndex(MESSAGE_COL_5)));
            String messageID = res.getString(res.getColumnIndex(MESSAGE_COL_6));
            int sentValue = res.getInt(res.getColumnIndex(MESSAGE_COL_7));
            Date sentDate = CommonMethods.stringToDate(res.getString(res.getColumnIndex(MESSAGE_COL_8)));
            Message message = new Message(email,name,subject,text,messageID,sentValue,hasAttach,sentDate);
            messageList.add(message);
        }
        res.close();
        return messageList;
    }
    private void updateMessages(String originalEmail, Contact contact) {
        Log.d("Database","Messages Accessed");
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+MESSAGE_TABLE_NAME+" where EMAIL = ?";
        String[] args = {originalEmail};
        Cursor res = db.rawQuery(query,args);
        while(res.moveToNext()){
            ContentValues contentValues = new ContentValues();
            contentValues.put(MESSAGE_COL_1,contact.getEmail());
            contentValues.put(MESSAGE_COL_2,contact.getName());
            contentValues.put(MESSAGE_COL_3,res.getString(res.getColumnIndex(MESSAGE_COL_3)));
            contentValues.put(MESSAGE_COL_4,res.getString(res.getColumnIndex(MESSAGE_COL_4)));
            contentValues.put(MESSAGE_COL_5,1==res.getInt(res.getColumnIndex(MESSAGE_COL_5)));
            contentValues.put(MESSAGE_COL_6,res.getString(res.getColumnIndex(MESSAGE_COL_6)));
            contentValues.put(MESSAGE_COL_7,1==res.getInt(res.getColumnIndex(MESSAGE_COL_7)));
            contentValues.put(MESSAGE_COL_8,res.getString(res.getColumnIndex(MESSAGE_COL_8)));
            contentValues.put(MESSAGE_COL_9,res.getString(res.getColumnIndex(MESSAGE_COL_9)));
            db.update(MESSAGE_TABLE_NAME,contentValues,"EMAIL = ?",new String[]{originalEmail});
        }
        res.close();
    }
    public Message getMessage(String messageID) {
        Message message;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + MESSAGE_TABLE_NAME + " where "+MESSAGE_COL_6+" = ?", new String[]{messageID}, null);
        res.moveToNext();

        String email = res.getString(res.getColumnIndex(MESSAGE_COL_1));
        String name = res.getString(res.getColumnIndex(MESSAGE_COL_2));
        String subject = res.getString(res.getColumnIndex(MESSAGE_COL_3));
        String text = res.getString(res.getColumnIndex(MESSAGE_COL_4));
        boolean hasAttach = (1 == res.getInt(res.getColumnIndex(MESSAGE_COL_5)));
        int sentValue = res.getInt(res.getColumnIndex(MESSAGE_COL_7));
        Date sentDate = CommonMethods.stringToDate(res.getString(res.getColumnIndex(MESSAGE_COL_8)));
        message = new Message(email,name,subject,text,messageID,sentValue,hasAttach,sentDate);

        res.close();
        return message;
    }
    public boolean willInsertMessage(String email, String messageID){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + MESSAGE_TABLE_NAME + " where "+MESSAGE_COL_6+" = ? and "+MESSAGE_COL_1+" = ?", new String[]{messageID,email}, null);
        if(res.getCount()>0){
            return false;
        }
        return true;
    }
    public void deleteMessages(String email){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGE_TABLE_NAME,"EMAIL = ?",new String[]{email});

    }
    public boolean hasRecentTime(String email, Date test){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select "+MESSAGE_COL_8+" from " + MESSAGE_TABLE_NAME + " where EMAIL = ? and "+MESSAGE_COL_2+" = ?", new String[]{email,"TIME"}, null);
        while(res.moveToNext()){
            Date lastDate = CommonMethods.stringToDate(res.getString(res.getColumnIndex(MESSAGE_COL_8)));
            Calendar testDate = Calendar.getInstance();
            testDate.setTime(test);
            if(testDate.get(Calendar.HOUR_OF_DAY)>=1){
                testDate.set(Calendar.HOUR_OF_DAY,testDate.get(Calendar.HOUR_OF_DAY)-1);
            }else{
                testDate.set(Calendar.HOUR_OF_DAY,testDate.get(Calendar.HOUR_OF_DAY)+23);
                if(testDate.get(Calendar.DAY_OF_YEAR)>=2){
                    testDate.set(Calendar.DAY_OF_YEAR,testDate.get(Calendar.DAY_OF_YEAR)-1);
                }else{
                    testDate.set(Calendar.DAY_OF_YEAR,testDate.get(Calendar.DAY_OF_YEAR)-1);
                    testDate.set(Calendar.YEAR,testDate.get(Calendar.YEAR)-1);
                }
            }
            if(lastDate.after(testDate.getTime())){
                return true;
            }
        }
        return false;
    }
    //</editor-fold>

    //<editor-fold> Attachment functions

    /**
     * This function gets the attachments for the email
     *
     * @param email the email with which the attachment it associated
     * @return a list of attachment file paths.
     */
    public List<String> getAttachments(String email) {
        Log.d("Database","Attachments Accessed");
        List<String> attachments = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select " + ATTACHMENT_COL_3 + " from " + ATTACHMENT_DATABASE + " where " + ATTACHMENT_COL_2 + " = ?";

        Cursor res = db.rawQuery(query, new String[]{email});
        while(res.moveToNext()){
            String temp = res.getString(0);
            attachments.add(temp);
        }
        res.close();
        return attachments;
    }

    public List<String> getAttachmentsforConvo(String messageID) {
        List<String> attachments = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select " + ATTACHMENT_COL_3 + " from " + ATTACHMENT_DATABASE + " where " + ATTACHMENT_COL_4 + " = ?";
        Cursor res = db.rawQuery(query, new String[]{messageID});
        while(res.moveToNext()){
            String temp = res.getString(0);
            attachments.add(temp);
        }
        res.close();
        return attachments;
    }

    /**
     * This function inserts an attachment with an email and the file path for the saved attachment
     *
     * @param email     The email associated with the file
     * @param filePath  The filepath for the file
     * @param messageID The message id of the messeage
     * @return The number of files in the database
     */
    public boolean insertAttachment(String email, String filePath, String messageID) {
        SQLiteDatabase db = this.getWritableDatabase();
        //Checks for it in the data base
        String query = "select * from attachment_database where EMAIL = ? and ATTACHMENT = ? and MESSAGE_ID = ?";
        String[] args = {email, filePath, messageID};
        Cursor res = db.rawQuery(query,args);
        if(res.getCount()>0){
            res.close();
            return false;
        }
        res.close();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ATTACHMENT_COL_2, email);
        contentValues.put(ATTACHMENT_COL_3, filePath);
        contentValues.put(ATTACHMENT_COL_4, messageID);
        long ret = db.insert(ATTACHMENT_DATABASE, null, contentValues);
        return ret != -1;
    }

    /**
     * This function deletes the attachment from the database
     *
     * @param email    The email associated with the attachment
     * @param filePath The filepath stored in the database
     * @return The number of files in the database
     */
    public boolean deleteAttachment(String email, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "EMAIL = ? AND ATTACHMENT = ?";
        long ret = db.delete(ATTACHMENT_DATABASE, where, new String[]{email, filePath});
        return ret != -1;
    }
    public boolean deleteAttachments(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "EMAIL = ?";
        long ret = db.delete(ATTACHMENT_DATABASE, where, new String[]{email});
        return ret != -1;
    }

    public boolean hasAttachments(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select " + ATTACHMENT_COL_3 + " from " + ATTACHMENT_DATABASE + " where " + ATTACHMENT_COL_2 + " = ?";
        Cursor res = db.rawQuery(query, new String[]{email});
        if(res.getCount()>0){
            res.close();
            return true;
        }
        res.close();
        return false;
    }

    public boolean hasAttachment(String email, String filePath, String messageID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from attachment_database where EMAIL = ? and ATTACHMENT = ? and MESSAGE_ID = ?";
        String[] args = {email, filePath, messageID};
        Cursor res = db.rawQuery(query,args);
        boolean hasAttachment = res.getCount()>0;
        res.close();
        return hasAttachment;

    }
    private void updateAttachments(String originalEmail, String email) {
        Log.d("Database","Messages Accessed");
        if(hasAttachments(originalEmail)){
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "select * from attachment_database where EMAIL = ?";
            String[] args = {originalEmail};
            Cursor res = db.rawQuery(query,args);
            while(res.moveToNext()){
                ContentValues contentValues = new ContentValues();
                contentValues.put(ATTACHMENT_COL_1,res.getString(res.getColumnIndex(ATTACHMENT_COL_1)));
                contentValues.put(ATTACHMENT_COL_2,email);
                contentValues.put(ATTACHMENT_COL_3,res.getString(res.getColumnIndex(ATTACHMENT_COL_3)));
                contentValues.put(ATTACHMENT_COL_4,res.getString(res.getColumnIndex(ATTACHMENT_COL_4)));
                db.update(ATTACHMENT_DATABASE,contentValues,"EMAIL = ?",new String[]{originalEmail});
            }
            res.close();
        }
    }


    //</editor-fold

}