package org.lightsys.emailhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

/**************************************************************************************************
 *                              Created by nicholasweg on 6/27/17.                                *
 *  Any changes made to this file regarding the database structure won't take effect unless you   *
 *  reinstall the app. The onCreate function only runs the first time the app is made. There may  *
 *  be a need to writed update functions so that won't have to happen if that situation comes up  *
 *  in the future. Otherwise you can use the onUpgrade function.                                  *
 **************************************************************************************************/

public class DatabaseHelper extends SQLiteOpenHelper {
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

    //I don't think I actually use and that may be because it doesn't work
    public String getContactName(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String name = "";
        Cursor res = db.rawQuery("select FIRSTNAME from saved_contacts where EMAIL = ?", new String[]{email}, null);
        return name;
    }


    public Integer deleteContactData(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(CONTACT_TABLE_NAME, "EMAIL = ?", new String[] {email});
    }
}
