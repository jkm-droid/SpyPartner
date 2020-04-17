package com.spypartner.jmtechnologies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by JoshT on 30/01/2019.
 */

public class InsertMessagesDataBaseHelper extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "messages.db";
    public static final String TABLE_NAME = "read_messages";

    public static final String COLUMN_1 = "ID";
    public static final String COLUMN_2 = "ADDRESS";
    public static final String COLUMN_3 = "USERNAME";
    public static final String COLUMN_4 = "BODY";
    public static final String COLUMN_5 = "DATE";

    public InsertMessagesDataBaseHelper(Context context){
        super(context,DATABASE_NAME, null, 3);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, ADDRESS TEXT, USERNAME TEXT, BODY TEXT, DATE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i1, int i3) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    //inserting all the messages into the database
    public boolean insertAllMessages(String address,String username,String body,String date){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_2,address);
        contentValues.put(COLUMN_3,username);
        contentValues.put(COLUMN_4,body);
        contentValues.put(COLUMN_5,date);

        long result = sqLiteDatabase.insert(TABLE_NAME,null,contentValues);
        //sqLiteDatabase.close();

        if(result == -1){
            return false;
        }else {
            return true;
        }
    }
    //retrieving all the messages from the database
    public Cursor getAllMessages() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * from " + TABLE_NAME, null);
        return cursor;
    }
    //deleting all the messages from the database
    public void deleteAllMessages(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("delete from "+ TABLE_NAME);
        sqLiteDatabase.close();
        System.out.println("--------------------messages deleted from sql---------------");
    }
}
