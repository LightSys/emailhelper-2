package com.example.ben.emailhelper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by nicholasweg on 6/27/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "EmailHelper.db";
    public static final String TABLE_NAME = "active_messages";
    public static final String COL_1 = "EMAIL";
    public static final String COL_2 = "NAME";
    public static String COL_3 = "TIME";

    public DatabaseHelper(Context context) {
        super (context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate (SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (EMAIL TEXT PRIMARY KEY, NAME TEXT, TIME TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String email, String name, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, email);
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, time);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public boolean checkIfExists(String column, String argument) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + column + " = " + argument, null);
        if(res.getCount() > 0)
            return true;
        else return false;
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    public Integer deleteData (String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "EMAIL = ?", new String[] {email});
    }
}
