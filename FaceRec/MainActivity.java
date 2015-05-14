package com.example.sameer.samplebluetoothserver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import com.googlecode.javacv.cpp.opencv_core;

import com.googlecode.javacv.cpp.opencv_objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


//@SuppressWarnings("deprecation")
public class MainActivity extends Activity {


    public String extdir;
    //	private Camera mCamera;
//	private CameraPreview mPreview;
    final Context context = this;
    static Handler mainHandler;
    byte [] image;
    String name, trainName;
    BluetoothAdapter adapter;
    private String id;
    static AcceptThread accept;
    LBPFaceRecognizer frec;
    Monitor monitor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String state = Environment.getExternalStorageState();

        if(state.equals(Environment.MEDIA_MOUNTED)){
            Log.e("sd","not mounted");
        }

        //Get the allotted external storage directory path for this application for future use
        extdir = getExternalFilesDir("").getAbsolutePath()+"/";
      //  extdir = getApplicationContext().getFilesDir().getAbsolutePath()+"/";
        adapter = BluetoothAdapter.getDefaultAdapter();
        monitor = new Monitor();
        id = "a6e08050-d8f7-11e4-8830-0800200c9a66";     //this is our Bluetooth UUID generated randomly from https://www.famkruithof.net/uuid/uuidgen
        accept = new AcceptThread(adapter,id,monitor);
        if(!adapter.isEnabled()){
            adapter.enable();
        }



        // ... And make the folder in question.
        (new File(extdir)).mkdirs();
        // If we haven't already, copy over the facial detection cascade template from the assets folder so it can be loaded by non-activity classes
        if(!(new File(extdir+"haarcascade_frontalface_alt.xml")).exists())
        {
            //log("Copying over haar face cascade...");
            try{
                AssetManager assets = getAssets();
                InputStream in = assets.open("haarcascade_frontalface_alt.xml");
                OutputStream out = new FileOutputStream(extdir+"haarcascade_frontalface_alt.xml");
                byte[] buffer = new byte[1024];
                int len = in.read(buffer);
                while (len != -1) {
                    out.write(buffer, 0, len);
                    len = in.read(buffer);
                }
                in.close();
                out.close();
                //assets.close();
            }
            catch(Exception e)
            {
                //log("ERROR: Couldn't copy over haar cascade file!");
            }

        }
        try{
     //       log("Loading haar cascade...");
            CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(opencv_core.cvLoad(extdir+"haarcascade_frontalface_alt.xml"));
      //      log("Setting up facerecognizer...");
            frec = new LBPFaceRecognizer(cascade, extdir);
     //       log("Setup complete.");
        }
        catch(Exception e)
        {
        //    Log.v("ERROR: Couldn't load haar cascade!");
        }
      //  Log.v("Sequence complete. Deploying yay message.");
     //   log("Message deployed.");

        mainHandler = new Handler(Looper.getMainLooper()){
            public void handleMessage(Message msg){
                if(msg.what== 1){          //image compare
                    Bundle imgBundle = msg.getData();
                    image = imgBundle.getByteArray("image");
                    Bitmap b = BitmapFactory.decodeByteArray(image,0,image.length);
                    IplImage img = IplImage.create(b.getWidth(), b.getHeight(), opencv_core.IPL_DEPTH_8U, 4);
                    ByteBuffer buffer = img.getByteBuffer();
                    b.copyPixelsToBuffer(buffer);
                    CvSeq seq = frec.detectFace(img);
                    CvRect rect = new CvRect(opencv_core.cvGetSeqElem(seq,0));
                    if(rect.isNull()){
                        Message message = Message.obtain();
                        Bundle noNameBund = new Bundle();
                        noNameBund.putString("name","Unknown");
                        message.setData(noNameBund);
                        message.what = 1;
                        accept.serverHandler.sendMessage(message);
                    }
                    else{
                    name = frec.identifyFace(frec.preprocessImage(img,rect));
                    Log.v("nameMatch",name);
                    Bundle nameBundle = new Bundle();
                    nameBundle.putString("name",name);
                    Message message = Message.obtain();
                    message.setData(nameBundle);
                    message.what = 1;
                    accept.serverHandler.sendMessage(message);
                    Bundle display = new Bundle();
                    display.putString("name",name);
                    Intent displayIntent = new Intent(getApplicationContext(),DisplayName.class);
                     displayIntent.putExtras(display);
                    startActivity(displayIntent);
                    monitor.readyQueue();  }  //unlock accept thread, uses same monitor used to initialize thread
                }
                else if(msg.what==2)          //training set
                {
                    Bundle trainBundle = msg.getData();
                    trainName = trainBundle.getString("Train");
                    Intent intent = new Intent(getApplicationContext(),TrainingSet.class);
                    intent.putExtra("name", trainName);
                    startActivity(intent);
                }
            }
        };

    }


  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
     //   getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    public void startThread(View v)
    {
      /*  String name = "sammyboy9000";
        Bundle bundle = new Bundle();
        bundle.putString("name",name);

    	Intent intent = new Intent(this, TrainingSet.class);
        intent.putExtras(bundle);
    	startActivity(intent);*/

        Thread acceptThread = new Thread(accept);
        acceptThread.start();
    }   //modify xml

/*    public void gotoCompare(View v)
    {
    	Intent intent = new Intent(this, ComparisonShot.class);
    	startActivity(intent);
    }  */ //modify xml


}
