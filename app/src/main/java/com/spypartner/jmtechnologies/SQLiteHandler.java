package com.spypartner.jmtechnologies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

/**
 * Created by JoshT on 07/02/2019.
 */

public class SQLiteHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "spypartner";

    // Login table name
    private static final String TABLE_USER = "user";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASS = "password";

    public SQLiteHandler(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_USER +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT)");

        /*String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_USERNAME + " TEXT";
        sqLiteDatabase.execSQL(CREATE_LOGIN_TABLE);*/

        System.out.println("--------------------------------Database tables created--------------------------------------");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Create tables again
        onCreate(sqLiteDatabase);
    }

    /**
     * Storing user details in database
     * */
    public boolean addLoginUserDetails(String username, String password) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_USERNAME, username); // username
        contentValues.put(KEY_PASS, password); // password

        // Inserting Row
        long id = sqLiteDatabase.insert(TABLE_USER, null, contentValues);
        sqLiteDatabase.close(); // Closing database connection

        System.out.println("------------------------Login details added to SQLite db ------------------------------" + id);

        if (id == -1){
            return false;
        }else {
            return true;
        }

    }

    /**
     * Get all the user login details
     * */
    public Cursor getAllUserDetails(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * from " + TABLE_USER, null);
        System.out.println("-----------------------------user details read---------------------------------------------");
        return cursor;
    }

    /**
     * Re create database Delete all tables and create them again
     * */
    public void deleteUser() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        // Delete All Rows
        sqLiteDatabase.delete(TABLE_USER, null, null);
        sqLiteDatabase.close();

        System.out.println("--------------------------------Deleted all user info from sqlite---------------------------------------");
    }
}
