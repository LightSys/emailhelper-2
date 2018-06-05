package org.lightsys.emailhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.lightsys.emailhelper.Contact.Contact;

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

    public DatabaseHelper(Context context) {
        super (context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate (SQLiteDatabase db) {
        String conversationQuery = String.format("create table " + CONVERSATION_TABLE_NAME + " ( %s TEXT PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT)", CONVO_COL_1, CONVO_COL_2, CONVO_COL_3, CONVO_COL_4);
        String contactQuery = String.format("create table " + CONTACT_TABLE_NAME + " ( %s TEXT PRIMARY KEY, %s TEXT, %s TEXT)", CONTACT_COL_1, CONTACT_COL_2, CONTACT_COL_3);
        String windowQuery = String.format("create table " + CONVERSATION_WINDOW_NAME + " ( %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s BOOLEAN, %s INTEGER PRIMARY KEY AUTOINCREMENT)", WINDOW_COL_1, WINDOW_COL_2, WINDOW_COL_3, WINDOW_COL_4, WINDOW_COL_5, WINDOW_COL_6, WINDOW_COL_7);
        db.execSQL(conversationQuery);
        db.execSQL(contactQuery);
        db.execSQL(windowQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CONTACT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_WINDOW_NAME);
        onCreate(db);
    }

    public boolean insertConversationData(String email, String name, String time, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, email);
        contentValues.put(CONVO_COL_2, name);
        contentValues.put(CONVO_COL_3, time);
        contentValues.put(CONVO_COL_4, date);
        long result = db.insert(CONVERSATION_TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }
    public boolean insertConversationData(Contact newContact, String time, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, newContact.getEmail());
        contentValues.put(CONVO_COL_2, newContact.getFirstName()+" "+newContact.getLastName());
        contentValues.put(CONVO_COL_3, time);
        contentValues.put(CONVO_COL_4, date);
        long result = db.insert(CONVERSATION_TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getConversationData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + CONVERSATION_TABLE_NAME, null);
        return res;
    }

    public Integer deleteConversationData(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(CONVERSATION_TABLE_NAME, "EMAIL = ?", new String[] {email});
    }

    public boolean insertContactData(String email, String firstName, String lastName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACT_COL_1, email);
        contentValues.put(CONTACT_COL_2, firstName);
        contentValues.put(CONTACT_COL_3, lastName);
        long result = db.insert(CONTACT_TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }
    public boolean insertContactData(Contact newContact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACT_COL_1, newContact.getEmail());
        contentValues.put(CONTACT_COL_2, newContact.getFirstName());
        contentValues.put(CONTACT_COL_3, newContact.getLastName());
        long result = db.insert(CONTACT_TABLE_NAME, null, contentValues);
        return result == -1;
    }

    public boolean willInsertWindowData(String email, String name, String message, boolean sent_by_me, String messageID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * from " + CONVERSATION_WINDOW_NAME + " where " + WINDOW_COL_5 + " = " + messageID;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            cursor.close();
            return false;
        }
        return true;
    }

    public boolean insertWindowData(String email, String name, String message, boolean sent_by_me, String messageID) {
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
        contentValues.put(WINDOW_COL_5, messageID);
        contentValues.put(WINDOW_COL_6, sent_by_me);
        long result = db.insert(CONVERSATION_WINDOW_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getWindowData(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + CONVERSATION_WINDOW_NAME + " where EMAIL = ?", new String[]{email}, null);
        return res;
    }

    public Cursor getContactData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + CONTACT_TABLE_NAME, null);
        return res;
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
        query = "delete from "+CONVERSATION_TABLE_NAME+" where EMAIL = ?";
        res.moveToNext();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVO_COL_1, email);//Use of email so there is not an extra function call
        contentValues.put(CONVO_COL_2,res.getString(res.getColumnIndex(CONVO_COL_2)));
        contentValues.put(CONVO_COL_3,currentTime);//Updates the time
        contentValues.put(CONVO_COL_4,res.getString(res.getColumnIndex(CONVO_COL_4)));//Leaves the created date
        db.delete(CONVERSATION_TABLE_NAME, "EMAIL = ?", new String[] {email});
        db.insert(CONVERSATION_TABLE_NAME, null, contentValues);
        res.close();
    }
    public Date getContactDate(String email){
        //The goal of this function is to take in a email string.
        //Then serach the database for the entry.
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
