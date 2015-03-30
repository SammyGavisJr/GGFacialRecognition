package com.example.sameer.googleface;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by Sameer on 2/7/2015.
 */
public class DBContentProvider extends ContentProvider
{
    DatabaseHandler db;
    String authority = "com.example.sameer.googleface.dbcontentprovider";
    Uri contentURI = Uri.parse("content://"+authority+"/storedFace");
    public DBContentProvider()
    {
        this.contentURI = contentURI;
    }
    public boolean onCreate()
    {
        db = new DatabaseHandler(this.getContext());
        return false;
    }
    public String getType(Uri uri)
    {
        return null;
    }
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        return null;
    }
    public Uri insert(Uri uri, ContentValues values){return null;}
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){return 0;}
    public int delete(Uri uri, String selection, String[] selectionArgs){return 0;}

    public Uri getUri()
    {
        return contentURI;
    }
}
