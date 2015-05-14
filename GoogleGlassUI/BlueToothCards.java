package com.example.sameer.googleface;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Sameer on 3/28/2015.
 */
public class BlueToothCards extends Activity
{
    private MenuAdapterList menuAdapterList;
    private CardScrollView mCardScroller;
    BluetoothAdapter adapter;

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        Bundle extras = getIntent().getExtras();
        final ArrayList<String> names = extras.getStringArrayList("devices");
        ArrayList<CardBuilder> devices = new ArrayList<CardBuilder>();
        getListDevices(devices, names);

        menuAdapterList = new MenuAdapterList(devices);


        //     cardBuilders[1].addImage(img);
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(menuAdapterList);
        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent goBackIntent = new Intent();
                Bundle extra = new Bundle();
                extra.putString("name", names.get(position));
                goBackIntent.putExtras(extra);
                setResult(RESULT_OK,goBackIntent);
                  finish();


            }



            });

        //  detector = createGestureDetector(this.getApplicationContext());
        setContentView(mCardScroller);

    }
    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }
  /*  public Loader<Cursor> onCreateLoader(int id, Bundle args) throws IllegalArgumentException
    {
        // Uri CONTENT_URI = ContactsContract.RawContacts.CONTENT_URI;

        CursorLoader loader = new CursorLoader(this,DBContentProvider.getUri(),PROJECTION, null, null, null);

        return loader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {adapter.swapCursor(data);  }

    public void onLoaderReset(Loader<Cursor> loader) {adapter.swapCursor(null);  }*/

    private void getListDevices(ArrayList<CardBuilder> img, ArrayList<String> names)
    {


        for(int i=0;i<names.size();i++){


                //    bitmaps.add(BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length));
                CardBuilder card = new CardBuilder(this,CardBuilder.Layout.TEXT);

                card.setText(names.get(i));
                img.add(card);


        }

    }

}
