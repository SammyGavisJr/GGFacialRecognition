package com.example.sameer.samplebluetoothserver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sameer on 4/1/2015.
 */
public class BluetoothServer implements Runnable
{

    PrintWriter output;
    BufferedReader input;
    static BluetoothSocket socket;
    boolean loop= true;
    String in;
   Bitmap bitmap;
    BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
    BlockingQueue<Integer> queueInteger = new LinkedBlockingQueue<Integer>();
    BlockingQueue<String> queueString = new LinkedBlockingQueue<String>();
    Monitor monitor;
    File file;
    String name = "";
    public BluetoothServer(BluetoothSocket socket,BlockingQueue<byte[]> queue, Monitor monitor, BlockingQueue<Integer> queueInteger,BlockingQueue<String> queueString){

       this.socket = socket;
       this.queue = queue;
       this.monitor = monitor;
       this.queueInteger = queueInteger;
       this.queueString = queueString;

    }
    public BluetoothServer(BluetoothSocket socket,BlockingQueue<byte[]> queue, Monitor monitor, BlockingQueue<Integer> queueInteger,BlockingQueue<String> queueString,String name){

        this.socket = socket;
        this.queue = queue;
        this.monitor = monitor;
        this.queueInteger = queueInteger;
        this.queueString = queueString;
        this.name = name;
    }

    public void run()
    {
        try{

            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataInputStream in = new DataInputStream(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            boolean receiveSize = true;
            int nullcheck =0;
            int size =0;
            while(true) {

             //   String inp = input.readLine();

          //      Log.e("input",in);
            //   System.out.println("h");

          /*      String length = input.readLine();
                int size = Integer.parseInt(length);
                System.out.println(size);
                int counter = 0,place=0;
                byte [] picture = new byte[size];
                byte[] image = new byte[size/15];
                while(place<size) {

                    in.read(image);
                    counter=image.length;
                    for(int i=place;i<place+counter;i++){
                        if(i==size){
                            break;
                        }
                        picture[i] = image[i-place];
                    }
                    place+=counter;
                }*/


            //    Log.e("sentinel",inp);
                if(name.equals("")){
                   output.println("Pulse"); }         //don't want client to block when checking for query command
                else{
                    output.println(name);
                    name = "";
                }
                    String length = input.readLine();
             /*   if(length==null){                    //close socket
                    break;
                }*/
                 if(length.substring(0,5).equals("Train")){
                     System.out.println(length);
                      queueString.add(length);
                }
                else {

                        size = Integer.parseInt(length);
                        queueString.add(length);



                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] image = new byte[1024];
                    int count = 0;
                    while (true) {
                        if (count >= size) {
                            Log.e("yea", "go");
                            break;
                        }

                        in.read(image);
                        buffer.write(image, 0, image.length);
                        count += 1024;
                    }

                    byte[] newArray = new byte[size];
                    ByteArrayInputStream tempIn = new ByteArrayInputStream(buffer.toByteArray());
                    tempIn.read(newArray);
             /*   for(int i=0;i<size;i++){
                  newArray[i] = buffer.toByteArray()[i];
                }*/

                    System.out.println(Arrays.toString(newArray));
                    Bitmap bitmap = BitmapFactory.decodeByteArray(newArray, 0, newArray.length);
                    if (bitmap != null) {
                        Log.e("sleep", "go to hell");
                    }

                    //         String root = Environment.getExternalStorageDirectory().toString();
                    //        File myDir = new File(root + "/req_images");
                    //         myDir.mkdirs();



                    //        String fname = "Image-" + n + ".jpg";
                    //          file = new File(myDir, fname);
                    System.out.println(size);
       /*           if (file.exists())
                        file.delete();
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                    System.out.println(buffer.size());
        //           monitor.readyQueue();
                    queue.add(newArray);
                    break;
               /*     try {
                      name =  queueString.take();
                      if(!name.equals("")){
                          output.println(name);
                      }

                    }catch(InterruptedException ex){}*/
                }

            }

        }catch(IOException ex){}
    }


}
class AcceptThread implements Runnable {
    private BluetoothServerSocket mmServerSocket;
    BluetoothServerSocket tmp;
    public BluetoothSocket socket;
    public volatile boolean serverWait=true;
    byte[] image,picture;
    Condition serverAvailable;
    Lock lock;
    BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
    BlockingQueue<Integer> queueInt = new LinkedBlockingQueue<Integer>();
    BlockingQueue<String> queueString = new LinkedBlockingQueue<String>();
    ArrayList<Byte> pic;
    Monitor monitor;
    BluetoothServer server;
    Thread btServer;
    Handler serverHandler;
    String name,id;
    BluetoothAdapter mBluetoothAdapter;
    MainActivity activity = new MainActivity();
    public AcceptThread(BluetoothAdapter mBluetoothAdapter,String id,Monitor monitor) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        tmp = null;
        HandlerThread handel = new HandlerThread("Handel");
        handel.start();
        serverHandler = new Handler(handel.getLooper()){
            public void handleMessage(Message msg){
                if(msg.what ==1){     //image comparison name
                    name = msg.getData().getString("name");

                }
            }
        };
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Blue", UUID.fromString(id));
            lock = new ReentrantLock();
            serverAvailable = lock.newCondition();
        } catch (IOException e) { }
        mmServerSocket = tmp;
        this.monitor = monitor;
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.id = id;
    }

    public void run() {
        socket = null;
        // Keep listening until exception occurs or a socket is returned

  //      while (true) {
            try {


                socket = mmServerSocket.accept();
                server = new BluetoothServer(socket,queue, monitor,queueInt,queueString);
                btServer = new Thread(server);
                btServer.start();
                setSocket(socket);


          //      System.out.println("l");
                // If a connection was accepted
                while(true) {
                    if(!btServer.isAlive()){
                        tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Blue", UUID.fromString(id));
                        mmServerSocket = tmp;
                        socket = mmServerSocket.accept();
                        monitor.lockQueue();   //main thread will unlock when name is processed
                        server = new BluetoothServer(socket,queue, monitor,queueInt,queueString,name);    //server thread will immediately have name
                        btServer = new Thread(server);
                        btServer.start();
                        queueString.add(name);}

                    mmServerSocket.close();


                    try {
                        //    for(int i =0;i<27;i++) {
                        String cmd = queueString.take();     //slave command
                        if(!cmd.substring(0,5).equals("Train")){           //image compare
                         image = new byte[Integer.parseInt(cmd)];
         //               monitor.lockQueue();
                        image = queue.take();
                        Bundle imgBundle = new Bundle();
                        imgBundle.putByteArray("image",image);
                        Message imgMessage = Message.obtain();
                        imgMessage.what = 1;
                        imgMessage.setData(imgBundle);
                        MainActivity.mainHandler.sendMessage(imgMessage);       //send byte array


                        System.out.println(image.length);
                            btServer.join();
                            monitor.lockQueue();
                        }
                        else{
                            String trainName = cmd.substring(6);
                            Bundle trainBundle = new Bundle();
                            trainBundle.putString("Train",trainName);
                            Message msg = Message.obtain();
                            msg.what = 2;
                            msg.setData(trainBundle);         //send name
                            MainActivity.mainHandler.sendMessage(msg);
                        }


                    } catch (InterruptedException ex) {
                    }
             //   }
                }
            }catch(IOException e){
            //    System.out.println("l");
           //    break;
            }

    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
    public BluetoothSocket getSocket(){
        return socket;
    }
    public void setSocket(BluetoothSocket socket){
        this.socket = socket;
    }
    public void setCondition(boolean condition){
        serverWait = condition;
    }
    public byte[] getImage(){
        return image;
    }

}
class Monitor{
    Lock lock = new ReentrantLock();
    Condition block = lock.newCondition();
    BlockingQueue queue;
    boolean flag = true;
    public Monitor(){

    }
    public void lockQueue()
    {
        try {
            lock.lock();
            while(flag){
               block.await();
            }

        }catch(InterruptedException ex){}
        finally{
            lock.unlock();
        }
    }
    public void readyQueue()
    {
        try {
            lock.lock();
            flag = false;
            block.signal();


        }
        finally{
            lock.unlock();
        }
    }
    public void setFlag(boolean newFlag){
        flag = newFlag;
    }
}
