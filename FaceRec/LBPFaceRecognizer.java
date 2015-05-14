package com.example.sameer.samplebluetoothserver;

/**
 * Created by Sameer on 4/26/2015.
 */
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.*;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import static com.googlecode.javacv.cpp.opencv_core.CV_32SC1;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_COMP_CORREL;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_INTER_LINEAR;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCalcHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCompareHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvEqualizeHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import static com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer;

public class LBPFaceRecognizer {



    private static String faceDataFolder = "data/";

    public static String imageDataFolder = faceDataFolder + "images/";
    //    private static final String CASCADE_FILE = "haarcascade_frontalface_alt.xml";
    private static final String frBinary_DataFile = faceDataFolder + "frBinary.dat";
    public static final String personNameMappingFileName = faceDataFolder + "personNumberMap.properties";
    private String externalDirectory;
    HashMap<String,opencv_imgproc.CvHistogram> nameMap = new HashMap<String, opencv_imgproc.CvHistogram>();
    private CvHaarClassifierCascade cascade;
    private Properties dataMap = new Properties();
    //    private static LBPFaceRecognizer instance = new LBPFaceRecognizer();

    public static final int NUM_IMAGES_PER_PERSON =1;    //TODO: testing cvLoadImage, please change to 10 if necessary
    double binaryTreshold = 123;
    int highConfidenceLevel = 65;

    Pointer ptr_binary = null;
    private FaceRecognizer fr_binary = null;

    public LBPFaceRecognizer(CvHaarClassifierCascade cas, String externaldir) {
        //Receive cascade file and external directory path from mainactivity; cannot be obtained in-class

        System.loadLibrary("opencv_flann");
        System.loadLibrary("opencv_features2d");
        System.loadLibrary("opencv_calib3d");
        System.loadLibrary("opencv_ml");
        System.loadLibrary("opencv_video");
        System.loadLibrary("opencv_legacy");
        System.loadLibrary("opencv_photo");
        System.loadLibrary("opencv_gpu");
        System.loadLibrary("opencv_nonfree");
        System.loadLibrary("opencv_contrib");

        System.loadLibrary("opencv_highgui");
        System.loadLibrary("opencv_core");
        System.loadLibrary("opencv_imgproc");


        cascade = cas;
        externalDirectory = externaldir;
        //Fill out folder paths
        File f = new File(externalDirectory+imageDataFolder+"training/");
        f.mkdirs();
        f = new File(externalDirectory+faceDataFolder);
        f.mkdirs();


        createModels();
        loadTrainingData();
        log("Initialization complete.");
    }

    //      public static LBPFaceRecognizer getInstance() {
    //             return instance;
    //     }
    private void log(String text)
    {
        File f = new File(externalDirectory+"log.txt");
        FileOutputStream logstream = null;
        try{
            logstream = new FileOutputStream(f, true);
        }
        catch(Exception e)
        {

        }
        text += "\n";
        byte[] output = new byte[text.length()];
        for(int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            byte b = (byte)(c);
            output[i] = b;
        }
        try
        {
            logstream.write(output);
        }
        catch(Exception e)
        {

        }
        try{
            if(logstream != null)
                logstream.close();
        }
        catch(Exception e)
        {

        }
    }
    private void createModels() {
        ptr_binary = createLBPHFaceRecognizer(1, 8, 8, 8, binaryTreshold);
        fr_binary = (FaceRecognizer)(ptr_binary);
    }

    protected CvSeq detectFace(IplImage originalImage) {
        Log.d("FaceRecAndroid","Detecting faces.");
        CvSeq faces = null;
        Loader.load(opencv_objdetect.class);
        try {
            Log.d("FaceRecAndroid","Line 1.");
            IplImage grayImage = IplImage.create(originalImage.width(), originalImage.height(), IPL_DEPTH_8U, 1);
            Log.d("FaceRecAndroid","Line 2.");
            cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
            Log.d("FaceRecAndroid","Line 3.");
            CvMemStorage storage = CvMemStorage.create();
            Log.d("FaceRecAndroid","Line 4.");
            //Rect r = new Rect();
            //cascade.detectMultiScale(new Mat(grayImage,false),r,1.1,1, 0, new Size(10,10), new Size(480,640));
            faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.1, 1, opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING);
            Log.d("FaceRecAndroid",""+originalImage.height()+" "+originalImage.width());
            Log.d("FaceRecAndroid","Line 5.");
            //face = r;

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("FaceRecAndroid","Error detecting faces.");
        }
        Log.d("FaceRecAndroid","Line 6.");
        return faces;
    }

    public String identifyFace(IplImage image) {
        Log.d("FaceRecAndroid", "=================================");
        String personName = "";

        Set keys = dataMap.keySet();

        if (keys.size() > 0) {
            int[] ids = new int[1];
            double[] distance = new double[1];
            int result = -1;

            fr_binary.predict(new opencv_core.CvMat(image), ids, distance);
            //just deriving a confidence number against treshold
            result = ids[0];
            System.out.println(distance[0]);
            if (result > -1 && distance[0]<highConfidenceLevel) {
                personName = (String) dataMap.get("" + result);
            }
        }

        return personName;
    }


    //The logic to learn a new face is to store the recorded images to a folder and retrain the model
    //will be replaced once update feature is available
    public boolean learnNewFace(String personName, IplImage[] images) throws Exception {
        int memberCounter = dataMap.size();
        if(dataMap.containsValue(personName)){
            Set keys = dataMap.keySet();
            Iterator ite = keys.iterator();
            while (ite.hasNext()) {
                String personKeyForTraining = (String) ite.next();
                String personNameForTraining = (String) dataMap.getProperty(personKeyForTraining);
                if(personNameForTraining.equals(personName)){
                    memberCounter = Integer.parseInt(personKeyForTraining);
                    log("Person already exist.. re-learning..");
                }
            }
        }
        dataMap.put("" + memberCounter, personName);
        storeTrainingImages(personName, images);
        retrainAll();

        return true;
    }


    public IplImage preprocessImage(IplImage image, CvRect r){
        IplImage gray = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
        IplImage roi = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
        CvRect r1 = new CvRect(r.x()-10, r.y()-10, r.width()+10, r.height()+10);
        cvCvtColor(image, gray, CV_BGR2GRAY);
        cvSetImageROI(gray, r1);
        cvResize(gray, roi, CV_INTER_LINEAR);
        cvEqualizeHist(roi, roi);
        return roi;
    }

    private void retrainAll() throws Exception {
        Set keys = dataMap.keySet();
        if (keys.size() > 0) {
            MatVector trainImages = new MatVector(keys.size() * NUM_IMAGES_PER_PERSON);
            CvMat trainLabels = CvMat.create(keys.size() * NUM_IMAGES_PER_PERSON, 1, CV_32SC1);
            Iterator ite = keys.iterator();
            int count = 0;

            log("Loading images for training...");
            while (ite.hasNext()) {
                String personKeyForTraining = (String) ite.next();
                String personNameForTraining = (String) dataMap.getProperty(personKeyForTraining);
                IplImage[] imagesForTraining = readImages(personNameForTraining);
                IplImage grayImage = IplImage.create(imagesForTraining[0].width(), imagesForTraining[0].height(), IPL_DEPTH_8U, 1);

                for (int i = 0; i < imagesForTraining.length; i++) {
                    trainLabels.put(count, 0, Integer.parseInt(personKeyForTraining));
                    cvCvtColor(imagesForTraining[i], grayImage, CV_BGR2GRAY);
                    trainImages.put(count,new opencv_core.CvMat(grayImage));
                    count++;
                }
                //storeNormalizedImages(personNameForTraining, imagesForTraining);
            }

            log("done.");

            log("Training Binary model ....");
            fr_binary.train(trainImages, new opencv_core.CvMat(trainLabels));
            log("done.");
            storeTrainingData();
        }

    }

    private void loadTrainingData() {

        try {
            File personNameMapFile = new File(externalDirectory+personNameMappingFileName);
            if (personNameMapFile.exists()) {
                FileInputStream fis = new FileInputStream(externalDirectory+personNameMappingFileName);
                dataMap.load(fis);
                fis.close();
            }
            File binaryDataFile = new File(externalDirectory+frBinary_DataFile);
            log("Loading Binary model ....");
            if(binaryDataFile.exists())
            {
                fr_binary.load(externalDirectory+frBinary_DataFile);
                log("done");
            }
            else
                log("No existing model.");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeTrainingData() throws Exception {
        log("Storing training models ....");

        File binaryDataFile = new File(externalDirectory+frBinary_DataFile);
        if (binaryDataFile.exists()) {
            binaryDataFile.delete();
        }


        fr_binary.save(externalDirectory+frBinary_DataFile);

        File personNameMapFile = new File(externalDirectory+personNameMappingFileName);
        if (personNameMapFile.exists()) {
            personNameMapFile.delete();
        }

        FileOutputStream fos = new FileOutputStream(personNameMapFile, false);
        dataMap.store(fos, "");
        fos.close();

        log("done.");
    }


    public void storeTrainingImages(String personName, IplImage[] images) {
        for (int i = 0; i < images.length; i++) {
            String imageFileName = externalDirectory+imageDataFolder + "training/" + personName + "_" + i + ".jpg";
            File imgFile = new File(imageFileName);
            try {
                if(imgFile.createNewFile()){
                    Log.e("file","file made");
                }
                if(imgFile.exists()){
                    System.out.println(imgFile.getAbsolutePath());
                    Log.e("file","file exists");
                }
            }catch(IOException ex){}
    /*        if (imgFile.exists()) {
                imgFile.delete();
            }*/
    //        cvSaveImage(imageFileName,images[i]);
           IplImage img = IplImage.create(images[i].width(),images[i].height(),IPL_DEPTH_8U,4);
     //      images[i] = IplImage.create(images[i].width(),images[i].height(),IPL_DEPTH_8U,4);
           if(images[i].sizeof()==0){
               Log.e("Error","Error");
           }
           System.out.println(images[i].height()+"x"+images[i].width());
            System.out.println(img.height()+"x"+img.width());
        //    rotate(images[i],-90);
          System.out.println(matchRotations(images[i],img));
         //   cvCvtColor(images[i], img,6);
            CvMat mat = images[i].asCvMat();

            if(mat.empty()){
                Log.e("Error","Error");
            }
            Bitmap b = Bitmap.createBitmap(img.width(), img.height(),Bitmap.Config.ARGB_8888);
            ByteBuffer bb = img.getByteBuffer();
            bb.rewind();
            b.copyPixelsFromBuffer(bb);

            try{
                FileOutputStream fos = new FileOutputStream(imageFileName);
                b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                if(BitmapFactory.decodeFile(imageFileName)==null){
                    Log.e("bitmap","bitmap null");
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private IplImage[] readImages(String personName) {
        File imgFolder = new File(imageDataFolder+"training/");
        //   imgFolder.mkdirs();
        IplImage[] images = null;
        //if (imgFolder.isDirectory() && imgFolder.exists()) {
        images = new IplImage[NUM_IMAGES_PER_PERSON];
        for (int i = 0; i < NUM_IMAGES_PER_PERSON; i++) {

            String imageFileName = externalDirectory+imageDataFolder + "training/" + personName + "_" + i + ".jpg";
            Bitmap b = BitmapFactory.decodeFile(imageFileName);
            if(b==null){
                Log.e("null",String.valueOf(i));
            }
            IplImage img = IplImage.create(b.getWidth(), b.getHeight(), opencv_core.IPL_DEPTH_8U, 4);
            ByteBuffer bb = img.getByteBuffer();
            b.copyPixelsToBuffer(bb);
     //       IplImage img = cvLoadImage(imageFileName);
            images[i] = img;
        }

        // }
        return images;
    }
    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    public int matchRotations(IplImage img1,IplImage img2){
        boolean noMatch = true;
        int angle = 90,timesEx=0;
        for(int i=0;i<4;i++){
            rotate(img1,angle);
            try{
            cvCvtColor(img1,img2,6);


            }catch(RuntimeException ex){
               timesEx++;
            }
            finally{
                angle+=90;
            }


        }
        return timesEx;
    }
    IplImage rotate(IplImage IplSrc,int angle) {
        IplImage img= IplImage.create(IplSrc.height(), IplSrc.width(), IplSrc.depth(), IplSrc.nChannels());
        opencv_core.cvTranspose(IplSrc, img);
        opencv_core.cvFlip(img, img, angle);
        return img;
    }
}






