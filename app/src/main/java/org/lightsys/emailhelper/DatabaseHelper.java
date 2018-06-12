package org.lightsys.emailhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.lightsys.emailhelper.Contact.Contact;
import org.lightsys.emailhelper.Conversation.Conversation;
import org.lightsys.emailhelper.Conversation.ConversationWindow;

import java.util.Date;

/**************************************************************************************************
 *                              Created by nicholasweg on 6/27/17.                                *
 *  Any changes made to this file regarding the database structure won't take effect unless you   *
 *  reinstall the app. The onCreate function only runs the first time the app is made. There may  *
 *  be a need to writed update functions so that won't have to happen if that situation comes up  *
 *  in the future. Otherwise you can use the onUpgrade function.                                  *
 **************************************************************************************************/

public class DatabaseHelper extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "EmailHelper.db";
    // Conversation variables
    public static final String CONVERSATION_TABLE_NAME = "active_messages";
    public static String CONVO_COL_1 = "EMAIL";
    public static String CONVO_COL_2 = "NAME";
    public static String CONVO_COL_3 = "TIME";
    public static String CONVO_COL_4 = "CREATED_DATE";
    public static String CONVO_COL_5 = "NEW_MAIL";
    // Contact variables
    public static final String CONTACT_TABLE_NAME = "saved_contacts";
    public static String CONTACT_COL_1 = "EMAIL";
    public static String CONTACT_COL_2 = "FIRSTNAME";
    public static String CONTACT_COL_3 = "LASTNAME";
    // Conversation Window variables
    public static final String CONVERSATION_WINDOW_NAME = "conversation_window";
    public static String WINDOW_COL_1 = "EMAIL";
    public static String WINDOW_COL_2 = "NAME";
    public static String WINDOW_COL_3 = "MESSAGE";
    public static String WINDOW_COL_4 = "ATTACHMENT";
    public static String WINDOW_COL_5 = "MESSAGE_ID";
    public static String WINDOW_COL_6 = "SENT_BY_ME";
    public static String WINDOW_COL_7 = "DB_ID";
    public static final String NOTIFICATION_SEND_LIST = "notification_send_list";
    public static String NOTIFICATION_COL_PRIMARY = "EMAIL";
    public static String NOTIFICATION_COL_BOOL    = "NOTIFICATION_BOOL";

    public DatabaseHelper(Context context) {
        super (context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate (SQLiteDatabase db) {
        String conversationQuery = String.format("create table " + CONVERSATION_TABLE_NAME + " ( %s TEXT PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT, %s BOOL)", CONVO_COL_1, CONVO_COL_2, CONVO_COL_3, CONVO_COL_4,CONVO_COL_5);
        String contactQuery = String.format("create table " + CONTACT_TABLE_NAME + " ( %s TEXT PRIMARY KEY, %s TEXT, %s TEXT)", CONTACT_COL_1, CONTACT_COL_2, CONTACT_COL_3);
        String windowQuery = String.format("create table " + CONVERSATION_WINDOW_NAME + " ( %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s BOOLEAN, %s INTEGER PRIMARY KEY AUTOINCREMENT)", WINDOW_COL_1, WINDOW_COL_2, WINDOW_COL_3, WINDOW_COL_4, WINDOW_COL_5, WINDOW_COL_6, WINDOW_COL_7);
        String notiQuery = String.format("create table "+NOTIFICATION_SEND_LIST + " ( %s TEXT PRIMARY KEY, %s BOOLEAN)",NOTIFICATION_COL_PRIMARY,NOTIFICATION_COL_BOOL);
        db.execSQL(conversationQuery);
        db.execSQL(contactQuery);
        db.execSQL(windowQuery);
        db.execSQL(notiQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CONTACT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_WINDOW_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATION_SEND_LIST);
        onCreate(db);
    }
    public boolean insertNotifications(String email){
        return insertNotifications(email,true);
    }
    public boolean insertNotifications(String email,boolean send){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues c = new ContentValues();
        c.put(NOTIFICATION_COL_PRIMARY,email);
        c.put(NOTIFICATION_COL_BOOL,send);
        long result = db.insert(NOTIFICATION_SEND_LIST,null,c);
        return -1 != result;
    }
    public boolean getNotificationSettings(String email){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+ NOTIFICATION_SEND_LIST + " where EMAIL = ?";
        Cursor res = db.rawQuery(query,new String[] {email});
        res.moveToNext();
        int result = res.getInt(res.getColumnIndex(NOTIFICATION_COL_BOOL));
        return result == 1;
    }
    public void setNotificationSettings(String email,boolean send) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NOTIFICATION_SEND_LIST, "EMAIL = ?",new String[]{email});
        insertNotifications(email,true);
    }


    public boolean insertConversationData(String email, String name, String time, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, email);
        contentValues.put(CONVO_COL_2, name);
        contentValues.put(CONVO_COL_3, time);
        contentValues.put(CONVO_COL_4, date);
        contentValues.put(CONVO_COL_5, false);
        long result = db.insert(CONVERSATION_TABLE_NAME, null, contentValues);
        return result != -1;
    }
    public boolean insertConversationData(String email, String name, String time, String date, boolean newmail){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, email);
        contentValues.put(CONVO_COL_2, name);
        contentValues.put(CONVO_COL_3, time);
        contentValues.put(CONVO_COL_4, date);
        contentValues.put(CONVO_COL_5, newmail);
        long result = db.insert(CONVERSATION_TABLE_NAME, null, contentValues);
        return result != -1;
    }
    public boolean insertConversationData(Contact newContact, String time, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, newContact.getEmail());
        contentValues.put(CONVO_COL_2, newContact.getFirstName()+" "+newContact.getLastName());
        contentValues.put(CONVO_COL_3, time);
        contentValues.put(CONVO_COL_4, date);
        contentValues.put(CONVO_COL_5, false);
        long result = db.insert(CONVERSATION_TABLE_NAME, null, contentValues);
        return result != -1;
    }
    public boolean insertConversationData(Contact newContact, String time, String date,boolean newmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, newContact.getEmail());
        contentValues.put(CONVO_COL_2, newContact.getFirstName()+" "+newContact.getLastName());
        contentValues.put(CONVO_COL_3, time);
        contentValues.put(CONVO_COL_4, date);
        contentValues.put(CONVO_COL_5, newmail);
        long result = db.insert(CONVERSATION_TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public void resetNewMailBoolean(String email){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+ CONVERSATION_TABLE_NAME + " where EMAIL = ?";
        Cursor res = db.rawQuery(query,new String[] {email});
        res.moveToNext();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, email);//Use of email so there is not an extra function call
        contentValues.put(CONVO_COL_2,res.getString(res.getColumnIndex(CONVO_COL_2)));
        contentValues.put(CONVO_COL_3,res.getString(res.getColumnIndex(CONVO_COL_3)));//Updates the time
        contentValues.put(CONVO_COL_4,res.getString(res.getColumnIndex(CONVO_COL_4)));//Leaves the created date
        contentValues.put(CONVO_COL_5,false);
        db.update(CONVERSATION_TABLE_NAME,contentValues, "EMAIL = ?", new String[] {email});
        res.close();

    }

    public Integer deleteConversationData(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(CONVERSATION_WINDOW_NAME,"EMAIL = ?",new String[]{email});
        return db.delete(CONVERSATION_TABLE_NAME, "EMAIL = ?", new String[] {email});
    }

    public boolean insertContactData(String email, String firstName, String lastName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACT_COL_1, email);
        contentValues.put(CONTACT_COL_2, firstName);
        contentValues.put(CONTACT_COL_3, lastName);
        insertNotifications(email);
        long result = db.insert(CONTACT_TABLE_NAME, null, contentValues);
        return result != -1;
    }
    public boolean insertContactData(Contact newContact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACT_COL_1, newContact.getEmail());
        contentValues.put(CONTACT_COL_2, newContact.getFirstName());
        contentValues.put(CONTACT_COL_3, newContact.getLastName());
        long result = db.insert(CONTACT_TABLE_NAME, null, contentValues);
        insertNotifications(newContact.getEmail());
        return result == -1;
    }

    public boolean willInsertWindowData(String messageID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * from " + CONVERSATION_WINDOW_NAME + " where " + WINDOW_COL_5 + " = " + messageID;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            cursor.close();
            return false;
        }
        return true;
    }

    public boolean insertWindowData(String email, String name, String message, boolean sent_by_me, String messageID,String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * from " + CONVERSATION_WINDOW_NAME + " where " + WINDOW_COL_5 + " = " + messageID;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            cursor.close();
            return false;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(WINDOW_COL_1, email);
        contentValues.put(WINDOW_COL_2, name);
        contentValues.put(WINDOW_COL_3, message);
        contentValues.put(WINDOW_COL_4, filePath);
        contentValues.put(WINDOW_COL_5, messageID);
        contentValues.put(WINDOW_COL_6, sent_by_me);
        long result = db.insert(CONVERSATION_WINDOW_NAME, null, contentValues);
        return result != -1;
    }
    public boolean insertWindowData(ConversationWindow convo) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * from " + CONVERSATION_WINDOW_NAME + " where " + WINDOW_COL_5 + " = " + convo.getMessageId();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            cursor.close();
            return false;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(WINDOW_COL_1, convo.getEmail());
        contentValues.put(WINDOW_COL_2, convo.getName());
        contentValues.put(WINDOW_COL_3, convo.getMessage());
        contentValues.put(WINDOW_COL_4,convo.getAttachmentFilePath());
        contentValues.put(WINDOW_COL_5, convo.getMessageId());
        contentValues.put(WINDOW_COL_6, convo.getSent());
        long result = db.insert(CONVERSATION_WINDOW_NAME, null, contentValues);
        return result != -1;
    }

    public Cursor getWindowData(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + CONVERSATION_WINDOW_NAME + " where EMAIL = ?", new String[]{email}, null);
        return res;
    }

    public Cursor getContactData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + CONTACT_TABLE_NAME, null);
    }
    public Contact getContact(String email){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res =  db.rawQuery("select * from " + CONTACT_TABLE_NAME + " WHERE EMAIL = ?", new String[]{email});
        res.moveToNext();
        return new Contact(res.getString(0),res.getString(1),res.getString(2));
    }
    public Cursor getConversationData(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + CONVERSATION_TABLE_NAME, null);
    }

    public String getContactName(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String name = "";
        Cursor res = db.rawQuery("select * from saved_contacts where EMAIL = ?", new String[]{email}, null);
        if(res.moveToNext()){
            String firstname = res.getString(res.getColumnIndex(CONTACT_COL_2));
            String lastname  = res.getString(res.getColumnIndex(CONTACT_COL_3));
            name = firstname + " "+ lastname;
        }
        res.close();
        return name;
    }


    public Integer deleteContactData(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(CONTACT_TABLE_NAME, "EMAIL = ?", new String[] {email});
    }
    public void updateConversation(String email, String currentTime){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+ CONVERSATION_TABLE_NAME + " where EMAIL = ?";
        Cursor res = db.rawQuery(query,new String[] {email});
        res.moveToNext();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, email);//Use of email so there is not an extra function call
        contentValues.put(CONVO_COL_2,res.getString(res.getColumnIndex(CONVO_COL_2)));
        contentValues.put(CONVO_COL_3,currentTime);//Updates the time
        contentValues.put(CONVO_COL_4,res.getString(res.getColumnIndex(CONVO_COL_4)));//Leaves the created date
        contentValues.put(CONVO_COL_5,true);
        db.delete(CONVERSATION_TABLE_NAME,"EMAIL = ?",new String[]{email});//why not update?
        db.insert(CONVERSATION_TABLE_NAME,null,contentValues);//This way the panels will reorder when a new time is given
        res.close();
    }
    public void updateConversation(String email, Contact updatedContact){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+ CONVERSATION_TABLE_NAME + " where EMAIL = ?";
        Cursor res = db.rawQuery(query,new String[] {email});
        res.moveToNext();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, updatedContact.getEmail());//Use of email so there is not an extra function call
        contentValues.put(CONVO_COL_2,updatedContact.getFirstName()+" "+updatedContact.getLastName());
        contentValues.put(CONVO_COL_3,res.getString(res.getColumnIndex(CONVO_COL_3)));//Updates the time
        contentValues.put(CONVO_COL_4,res.getString(res.getColumnIndex(CONVO_COL_4)));//Leaves the created date
        contentValues.put(CONVO_COL_5,res.getString(res.getColumnIndex(CONVO_COL_5)));
        db.update(CONVERSATION_TABLE_NAME,contentValues,"EMAIL = ?",new String[]{email});
        res.close();
    }
    public void updateConversationWindowWithDifferentEmail(String email, Contact updatedContact){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+ CONVERSATION_WINDOW_NAME + " where EMAIL = ?";
        Cursor res = db.rawQuery(query,new String[] {email});
        String updatedEmail = updatedContact.getEmail();
        String updatedName = updatedContact.getFirstName()+" "+updatedContact.getLastName();
        res.moveToNext();
        ContentValues contentValues = new ContentValues();
        contentValues.put(WINDOW_COL_1,updatedEmail);
        contentValues.put(WINDOW_COL_2,updatedName);
        contentValues.put(WINDOW_COL_3,res.getString(res.getColumnIndex(WINDOW_COL_3)));
        contentValues.put(WINDOW_COL_4,res.getString(res.getColumnIndex(WINDOW_COL_4)));
        contentValues.put(WINDOW_COL_5,res.getString(res.getColumnIndex(WINDOW_COL_5)));
        contentValues.put(WINDOW_COL_6,res.getInt(res.getColumnIndex(WINDOW_COL_6)));
        db.update(CONVERSATION_WINDOW_NAME,contentValues,"EMAIL = ?",new String[]{email});
    }
    public void changeConversationData(String email, Contact updatedContact){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+ CONVERSATION_WINDOW_NAME + " where EMAIL = ?";
        Cursor res = db.rawQuery(query,new String[] {email});
        res.moveToNext();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, updatedContact.getEmail());//Use of email so there is not an extra function call
        contentValues.put(CONVO_COL_2,updatedContact.getFirstName()+" "+updatedContact.getLastName());
        contentValues.put(CONVO_COL_3,res.getString(res.getColumnIndex(CONVO_COL_3)));//Updates the time
        contentValues.put(CONVO_COL_4,res.getString(res.getColumnIndex(CONVO_COL_4)));//Leaves the created date
        contentValues.put(CONVO_COL_5,res.getString(res.getColumnIndex(CONVO_COL_5)));
        db.update(CONVERSATION_TABLE_NAME,contentValues, "EMAIL = ?", new String[] {email});
        res.close();
    }
    public Date getContactDate(String email){
        //The goal of this function is to take in a email string.
        //Then search the database for the entry.
        //The entry created date is converted to a special formatted output string used in getMail.
        Date today;
        String date = "";
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+ CONVERSATION_TABLE_NAME + " where EMAIL = ?";
        Cursor res = db.rawQuery(query,new String[] {email});
        res.moveToNext();
        String Date = res.getString(res.getColumnIndex(CONVO_COL_4));
        res.close();
        String day = Date.substring(0,Date.indexOf(" "));
        if(day.length()==1){
            day = "0" + day;
        }
        date +=day;
        Date = Date.substring(Date.indexOf(" ")+1);
        String month = Date.substring(0,3);
        int Month = 0;
        switch (month){//the data falls through the switch which increases the count to the right spot
            case "Dec":
                Month++;
            case "Nov":
                Month++;
            case "Oct":
                Month++;
            case "Sep":
                Month++;
            case "Aug":
                Month++;
            case "Jul":
                Month++;
            case "Jun":
                Month++;
            case "May":
                Month++;
            case "Apr":
                Month++;
            case "Mar":
                Month++;
            case "Feb":
                Month++;
        }
        month = Integer.toString(Month);
        if(month.length()==1){
            month = "0" + month;
        }
        date += month;
        String year = Date.substring(Date.indexOf(" ")+1);
        int Year = Integer.valueOf(year);
        Year -= 1900;
        year = Integer.toString(Year);
        date +=year;
        //The final string appears DDMMYYY
        //Where DD is normal
        //MM indexes starting at 0 for Jan
        //YYY is the years from 1900;
        today = new Date(0,0,0,0,0,0);//Empties the date
        today.setDate(Integer.valueOf(date.substring(0,2)));
        today.setMonth(Integer.valueOf(date.substring(2,4)));
        today.setYear(Integer.valueOf(date.substring(4)));
        return today;
    }

}
