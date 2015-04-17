package com.example.sameer.googleface;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Sameer on 2/7/2015.
 */
public class ListActivity extends Activity
{
    String[] PROJECTION = new String[] {"_id","name","myFace"};
    private MenuAdapterList menuAdapterList;
    private CardScrollView mCardScroller;
    File databaseFile;
    ArrayList<CardBuilder> cardList = new ArrayList<CardBuilder>();
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
       databaseFile = this.getApplicationContext().getDatabasePath("androidsqlite_4.db");
        ArrayList<CardBuilder> imgCards = new ArrayList<CardBuilder>();
        getListImages(imgCards);

        menuAdapterList = new MenuAdapterList(imgCards);


        //     cardBuilders[1].addImage(img);
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(menuAdapterList);
        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Plays disallowed sound to indicate that TAP actions are not supported.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.DISALLOWED);



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

    private void getListImages(ArrayList<CardBuilder> img)
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(databaseFile.toString(),null,1);
        String query = "SELECT _id,name,myFace FROM placeFaces";
        Cursor cursor = db.rawQuery(query,null);
        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
        ArrayList<String> imgPaths = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();
          cursor.moveToFirst();

            while(cursor.moveToNext()){
                imgPaths.add(cursor.getString(cursor.getColumnIndex("myFace")));
                names.add(cursor.getString(cursor.getColumnIndex("name")));
            }

        for(int i=0;i<imgPaths.size();i++){
        try {
            byte[] imgArray = getByteArray(imgPaths.get(i));
        //    bitmaps.add(BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length));
            CardBuilder card = new CardBuilder(this,CardBuilder.Layout.COLUMNS);
            card.addImage(BitmapFactory.decodeByteArray(imgArray,0,imgArray.length));
            card.setText(names.get(i));
            img.add(card);
            menuAdapterList.notifyDataSetChanged();

        }catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
    }

    }
    private byte[] getByteArray(String filePath) throws FileNotFoundException, IOException {

        File file = new File(filePath);


        FileInputStream fis = new FileInputStream(file);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                bos.write(buf, 0, readNum);

            }
        } catch (IOException ex) {
            Log.d("error", "error");
        }
        byte[] imageBytes = bos.toByteArray();

        return imageBytes;

    }

}
