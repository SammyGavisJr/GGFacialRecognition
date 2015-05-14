package com.example.sameer.googleface;

import com.google.android.glass.app.Card;
import com.google.android.glass.content.Intents;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.speech.RecognizerIntent;
/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p/>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class MainActivity extends Activity {

    /**
     * {@link CardScrollView} to use as the main content view.
     */
    private CardScrollView mCardScroller;
    private String android_id;


    BluetoothAdapter adapter;
    BluetoothDevice selectedDevice;
    String bluetooth;
    private Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    File databaseFile;
    private GestureDetector detector;
    private MenuAdapter menuAdapter;
    private DatabaseHandler dbHandler;
    private ArrayList<BluetoothDevice> remoteDevices;
    SQLiteDatabase db;
    private File path = Environment.getExternalStorageDirectory();
    private String dbPath = "/data/data/com.example.sameer.googleface/databases/";
    Bitmap img;
    Uri imageUri;
    int imageFileNO= 0;
    String thumbnailPath,fullName;
    BroadcastReceiver receiver;
    Thread bt;
    BluetoothClient btClient;
    IntentFilter filter;
    String compareShot;
    ArrayList<CardBuilder> blueList = new ArrayList<CardBuilder>();
    ArrayList<String> deviceNames;
    ArrayList<String> remote;
    static AcceptConnection client;
    final CardBuilder[] cardBuilders = new CardBuilder[4];
    int bluetoothInt;
    BluetoothDevice[] array;
    static Handler handler;
    /**
     * "Hello World!" {@link View} generated by {@link }.
     */
    private View mView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

      databaseFile = this.getApplicationContext().getDatabasePath("androidsqlite_4.db");
        dbHandler = new DatabaseHandler(getApplicationContext());
        android_id = "a6e08050-d8f7-11e4-8830-0800200c9a66";    //generated at random for this application
        if(!databaseFile.exists()){
        db = dbHandler.getWritableDatabase();
        }
        else
        {
            db = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
       }

        String[] cardTitles = {"List", "Add", "Training","Compare"};
        for (int i = 0; i < cardBuilders.length; i++) {
            cardBuilders[i] = new CardBuilder(this, CardBuilder.Layout.TEXT);
            cardBuilders[i].setText(cardTitles[i]);

        }
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(!adapter.isEnabled()){
            adapter.enable();
        }

        handler = new Handler(Looper.getMainLooper()){
            public void handleMessage(Message msg){
                if(msg.what==1){
                    Bundle queryBundle = new Bundle();
                    queryBundle = msg.getData();
                    String query = queryBundle.getString("query");
                    queryDatabase(query);
                }
            }
        };






        menuAdapter = new MenuAdapter(cardBuilders);
   //     cardBuilders[1].addImage(img);
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(menuAdapter);
                // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 1) {
            //        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            //        startActivityForResult(speechIntent,2);
                    String name = "image_" + String.valueOf(imageFileNO) + ".jpg";
                    File imageFile = new File(path, name);
                    imageFileNO++;
                    imageUri = Uri.fromFile(imageFile);
                    camera.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    startActivityForResult(camera, 1);
                }
                else if(position == 0){
                    Intent intent = new Intent(getApplicationContext(),ListActivity.class);
                    intent.putExtra("list",true);
                    startActivity(intent);
                }
                else if(position==2){

                   /* if(adapter.getBondedDevices().size()==0){

                        Log.v("string","Not Connected");
                    }*/


                    if(adapter.getBondedDevices().size()>0) {


                        array = new BluetoothDevice[adapter.getBondedDevices().size()];

                        deviceNames = new ArrayList<String>();
                        array = adapter.getBondedDevices().toArray(array);
                        for (int i = 0; i < array.length; i++) {
                            deviceNames.add(array[i].getName());
                        }
                        Intent bluetoothIntent = new Intent(getApplicationContext(), BlueToothCards.class);
                        Bundle extra = new Bundle();
                        extra.putStringArrayList("devices", deviceNames);
                        bluetoothIntent.putExtras(extra);
                        startActivityForResult(bluetoothIntent, 3);



                        Log.i("string", array[bluetoothInt].getAddress());
                      /*  for (int i = 0; i < array.length; i++) {
                            if (array[i].getAddress().equals(bluetooth)) {
                                selectedDevice = array[i];
                                //      Log.e("string","Let's go");
                                break;
                            }
                        }*/

                    }


                   // BluetoothClient client = new BluetoothClient();
                }
                else if(position == 3){
                    compareShot = "compare.jpg";
                    File compareFile = new File(path, compareShot);

                    imageUri = Uri.fromFile(compareFile);
                    camera.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    startActivityForResult(camera, 4);
                }
             /*   else if(position ==4){
                    bluetoothDiscovery();
                    Log.e("name",remote.get(0));
                    Intent bluetoothDiscoverIntent = new Intent(getApplicationContext(), BlueToothCards.class);
                    Bundle extraBTDiscover = new Bundle();
                    extraBTDiscover.putStringArrayList("devices", remote);
                    bluetoothDiscoverIntent.putExtras(extraBTDiscover);
                    startActivityForResult(bluetoothDiscoverIntent, 5);
                    remoteDevices.get(Integer.parseInt(bluetooth)).createBond();

                }*/



            }
        });
      //  detector = createGestureDetector(this.getApplicationContext());
        setContentView(mCardScroller);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1) {
            thumbnailPath = data.getStringExtra(Intents.EXTRA_THUMBNAIL_FILE_PATH);
            Log.v("string", thumbnailPath);
            //   String thumbnailPath = path.toString() + imageUri.toString();
            ContentValues imgVal = new ContentValues();
            fullName = "samwell";
            imgVal.put("name",fullName);
            imgVal.put("myFace", thumbnailPath);
            db.insert("placeFaces", null, imgVal);
         //   processThumbnail(thumbnailPath,fullName);

            menuAdapter.notifyDataSetChanged();
        }
        else if(requestCode==2){
        //    List<String> nameOfFace = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            fullName="samwell";
        /*    for(int i=0;i<nameOfFace.size();i++){
                String name = nameOfFace.get(i);
                fullName = fullName+" "+name;
            }*/

        }
        else if(requestCode==3){

            Bundle bluetoothName = data.getExtras();
            bluetoothInt = bluetoothName.getInt("name");
            selectedDevice = array[bluetoothInt];


            if (selectedDevice != null) {
                System.out.println("g");
                client = new AcceptConnection(selectedDevice, android_id,new Monitor());
                Thread t = new Thread(client);

                t.start();

                fullName = "samwell";

                Bundle nameFull = new Bundle();
                nameFull.putString("fullName", fullName);
                Message message = Message.obtain();
                message.what = 2;
                message.setData(nameFull);
                client.handler.sendMessage(message);
            }
       //     System.out.println(bluetoothInt);


        }
        else if(requestCode ==4){

            final Bundle bundler = new Bundle();
            thumbnailPath = data.getStringExtra(Intents.EXTRA_THUMBNAIL_FILE_PATH);

            bundler.putString("pic",thumbnailPath);

       /*     client.handler.post(new Runnable(){
                 public void run(){
                    Message message = Message.obtain();
                    message.what = 1;
                   message.setData(bundler);

                }
            });*/
            Message message = Message.obtain();
            message.what = 1;
            message.setData(bundler);
            client.handler.sendMessage(message);
        }
        else if(requestCode==5){
            adapter.cancelDiscovery();
            Bundle bluetoothName = data.getExtras();
            bluetooth = bluetoothName.getString("name");
        }
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
    protected void onDestroy(){
        super.onDestroy();

    }
    /**
     * Builds a Glass styled "Hello World!" view using the {@link CardBuilder} class.
     */
  /*  private View buildView() {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);

        card.setText(R.string.hello_world);
        return card.getView();
    }*/
    public void processThumbnail(String path, String name) {
        try {
            byte[] image = getByteArrayFromImage(path);
            CardBuilder newCard = new CardBuilder(this, CardBuilder.Layout.COLUMNS);
            img = BitmapFactory.decodeByteArray(image, 0, image.length);
            newCard.addImage(img);
            newCard.setText(name);
            cardBuilders[1] = newCard;

        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }

    }

    private byte[] getByteArrayFromImage(String filePath) throws FileNotFoundException, IOException {

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
    public void queryDatabase(String query){
       Intent listIntent = new Intent(getApplicationContext(),ListActivity.class) ;
        Bundle listBundle = new Bundle();
        listBundle.putBoolean("list",false);
        listBundle.putString("query", query);
        listIntent.putExtras(listBundle);
        startActivity(listIntent);
    }

  /*  private void bluetoothDiscovery(){
        adapter.startDiscovery();
        deviceNames = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(dev.getName()!=null){
                        remote.add(dev.getName());
                        remoteDevices.add(dev);
                    }

                }
            }
        };
        registerReceiver(receiver,filter);
    }
*/
  /*  private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);

        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {

                    return true;
                } else if (gesture == Gesture.SWIPE_DOWN) {
                    finish();
                }
                return false;
            }
        });
        gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
            @Override
            public void onFingerCountChanged(int previousCount, int currentCount) {
                // do something on finger count changes
            }
        });

        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
            @Override
            public boolean onScroll(float displacement, float delta, float velocity) {
                // do something on scrolling
                return true;
            }
        });
        return gestureDetector;
    }*/
}


