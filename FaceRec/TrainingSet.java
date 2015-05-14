package com.example.sameer.samplebluetoothserver;

/**
 * Created by Sameer on 4/26/2015.
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import com.googlecode.javacv.cpp.opencv_objdetect.*;
import com.googlecode.javacv.cpp.opencv_core;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;




public class TrainingSet extends Activity {
    private static int TAKE_PICTURE = 1;
    private Uri imageUri;
    private final int constant = 1;
    private int count = 0;
//    private IplImage[] photos = {null,null,null,null,null,null,null,null};
 private IplImage[] photos = {null};
    private TextView nameT, counter;
    private Button button1;
    private LBPFaceRecognizer frec;
    private String name, add_info;
    static File [] files = new File[2];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_set);
        nameT = (TextView)findViewById(R.id.textViewT);
        counter = (TextView)findViewById(R.id.textViewL);
        button1 = (Button)findViewById(R.id.button_TS);
        updateCounter(0);
        Bundle extras = getIntent().getExtras();
        name = extras.getString("name");
   //     add_info = extras.getString("additional_info");
        nameT.setText("Name: "+name);
   //     String extdir = Environment.getExternalStorageDirectory().getParent() + "/extSdCard" + "/myDirectory";
      String extdir =   getExternalFilesDir("").getAbsolutePath()+"/";
        try{
            AssetManager ass = getApplicationContext().getAssets();
            InputStream assStream = ass.open("haarcascade_frontalface_alt.xml");

            File casc = new File(extdir+"haarcascade_frontalface_alt.xml");
            FileOutputStream out = new FileOutputStream(casc);
            byte [] buffer = new byte[4096];
            int bytes = 0;
            while((bytes=assStream.read(buffer))!=-1){
                out.write(buffer,0,bytes);
            }
            assStream.close();
            out.close();
            CvHaarClassifierCascade cascade = opencv_objdetect.cvLoadHaarClassifierCascade(casc.getAbsolutePath(), new CvSize(640, 480));
            Log.d("FaceRecAndroid","["+cascade.orig_window_size().width()+","+cascade.orig_window_size().height()+"]");
            //CvHaarClassifierCascade test = new CvHaarClassifierCascade(cascade);
            if(cascade.isNull())
                Log.d("FaceRecAndroid","Couldn't load Haar Cascade!");
            frec = new LBPFaceRecognizer(cascade, extdir);
        }
        catch(Exception e)
        {
            Log.d("FaceRecAndroid","ERROR: Couldn't load haar cascade!");
        }
    }

    public void updateCounter(int i)
    {
        count = i;
        counter.setText("Photos taken: "+count+"/10");
        if(count == constant)
        {
            button1.setText("Finish");
        }
    }

    public void buttonPush(View v)
    {
        if(count < constant)     //TODO: testing cvLoadImage, please change to 10 if necessary
        {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        //    File photo = getOutputFile(count+1);
            File photo = getOutputFile(count+1);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photo));
            imageUri = Uri.fromFile(photo);
            startActivityForResult(intent, TAKE_PICTURE);
            button1.setEnabled(false);
        }
        else if(count == constant)   //testing cvLoadImage, please change to 10 if necessary
        {
            nameT.setVisibility(TextView.INVISIBLE);
            counter.setText("Training...");
            button1.setEnabled(false);
            initTraining();


        }
    }

    public void prepImages()
    {
        for(int i = 0; i < constant; i++)   //TODO: testing cvLoadImage, please change to 10 if necessary
        {
            Bitmap b = BitmapFactory.decodeFile(getOutputFile(i + 1).getPath());
            b = Bitmap.createScaledBitmap(b,640,480,false);
           // b = rotateBitmap(b, -90);
            IplImage img = IplImage.create(b.getWidth(), b.getHeight(), opencv_core.IPL_DEPTH_8U, 4);
            ByteBuffer buffer = img.getByteBuffer();
            b.copyPixelsToBuffer(buffer);
            photos[i] = img;
        }
    }

    public void initTraining()
    {
        prepImages();

        if(frec == null)
        {
            Log.d("FaceRecAndroid","Error: Face recognizer not initialized!");
        }
        else
        {
            try{
                for(int i = 0; i < constant; i++)   //TODO: testing cvLoadImage, please change to 10 if necessary
                {
                    IplImage img = photos[i];
                    if(img == null || img.width() == 0 || img.height() == 0)
                        Log.d("FaceRecAndroid","Training image not loaded properly!");
                    //IplImage snapshot = opencv_core.cvCreateImage(opencv_core.cvGetSize(img), img.depth(), img.nChannels());
                    CvSeq faces = frec.detectFace(img);
                    //Log.d("FaceRecAndroid","Face detected.");
                    CvRect r = new CvRect(opencv_core.cvGetSeqElem(faces,0));
                    if(r.width()==0||r.height()==0){
                        Log.d("FaceRecAndroid","height or width is zero");
                    }
                    //Log.d("FaceRecAndroid","Rect pulled.");
                    if(r.isNull())
                    {
                        Log.d("FaceRecAndroid","No Face Detected!");
                    }
                    else
                    {

                        Log.d("FaceRecAndroid","Image "+i+" processed");
                        img = frec.preprocessImage(img,r);
                        photos[i] = img;
                    }

                }

                String sName = name.replaceAll(" ", "_");
                frec.learnNewFace(sName, photos);     //TODO: learnnewFace goes into retrainAll in LBPFaceRecognizer
                counter.setText("Training successful!");
            }catch(Exception e)
            {
                Log.d("FaceRecAndroid","Error: Training failed!");
                Log.d("FaceRecAndroid", ""+e.getClass()+":"+e.getLocalizedMessage());
                StackTraceElement[] s = e.getStackTrace();
                for(StackTraceElement x : s)
                {
                    Log.d("FaceRecAndroid",x.toString());
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    updateCounter(count+1);
                    button1.setEnabled(true);
                }
                else
                    button1.setEnabled(true);
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private File getOutputFile(int c)
    {
    //    String extdir = Environment.getExternalStorageDirectory().getParent() + "/extSdCard" + "/myDirectory"+"/";
       String extdir = getExternalFilesDir("").getAbsolutePath()+"/";
        File extStorageDir = new File(extdir+"training/");

        if(!extStorageDir.exists())
            if(!extStorageDir.mkdirs())
            {
                Log.d("FaceRecAndroid","Failed to create compareshot directory!");
                return null;
            }

        File out = new File(extStorageDir.getAbsolutePath() + "/"+(c)+".jpg");
        return out;
    }



