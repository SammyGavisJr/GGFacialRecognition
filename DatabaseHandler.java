package com.example.sameer.googleface;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sameer on 2/7/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    SQLiteDatabase database;
    public DatabaseHandler(Context application)
    {
        super(application, "androidsqlite_4.db", null, 1);

    }

    public void onCreate(SQLiteDatabase db)
    {

        String query;
        query = "CREATE TABLE placeFaces (_id INTEGER PRIMARY KEY,name TEXT,myFace TEXT)";      //byte array will be stored as TEXT not BLOB
        db.execSQL(query);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){}

    public ArrayList<HashMap<String,String>> getList()
    {

        ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String, String>>();
        String query = "SELECT id,name FROM storedFaces";
        database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query,null);



        do {
            HashMap map = new HashMap();
            map.put(cursor.getString(0), cursor.getString(1));
            list.add(map);

        }while (cursor.moveToNext());
        return list;
    }
    public Cursor getInfoCursor() {

        Cursor mCursor = database.query("storedFaces", new String[] {"id","name","myFace"},
                null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

}
