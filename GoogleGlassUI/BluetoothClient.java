package com.example.sameer.googleface;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sameer on 3/28/2015.
 */


    public class BluetoothClient implements Runnable {

        UUID uuid;
        private String android_id;
        BluetoothDevice device;
        BluetoothSocket socket;
        BufferedReader input = null;
        PrintWriter output = null;
        DataOutputStream byteData = null;
        BufferedWriter writer;
        String in;
        public Handler handler;
        boolean flag = true,socketOpen=true;
        BlockingQueue<byte[]> queue;
        BlockingQueue<String> queue2;
    BlockingQueue<String> queue3;
        Monitor monitor;

        public BluetoothClient(BluetoothSocket btSocket, BlockingQueue<byte[]> queue, BlockingQueue<String> queue2,Monitor monitor,BlockingQueue<String> queue3) {
            this.device = device;
            this.queue = queue;
            this.queue2 = queue2;
            this.queue3 = queue3;
            this.monitor = monitor;
            socket = btSocket;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                byteData = new DataOutputStream(socket.getOutputStream());
               writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            } catch (IOException ex) {
            }


        }

        public void run() {


            while (socketOpen) {

                //        if(msg.what==1) {

                try {
                    //   if(queue!=null) {
                    try {
                        String pulseOrQuery = input.readLine();
                        if(pulseOrQuery.equals("Pulse")){
                            queue3.add("");
                        }
                        else{


                            queue3.add(pulseOrQuery);    //could be ""
                        }
                    }catch (IOException ec){}
             //       monitor.lockQueue();

                    String hey = queue2.take();
                    if(hey.equals("Train")){
                        String full = queue2.take();
                        output.println("Train"+" "+full);
                        output.flush();
                        monitor.readyQueue();
                        Log.v("sentinel", hey);
                    }
                    else if(hey.equals("Path")){
                        Log.v("sentinel", hey);
                        byte[] img = queue.take();
                    //    System.out.println(img.length);
                       try {
                            System.out.println(img.length);
                           Bitmap bitmap = BitmapFactory.decodeByteArray(img,0,img.length);
                           ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
                           System.out.println(Arrays.toString(img));
                            output.println(img.length);
                            output.flush();
                    //       BufferedOutputStream outFromClient = new BufferedOutputStream(socket.getOutputStream());
                      //    WritableByteChannel channel = Channels.newChannel(socket.getOutputStream());
                     //      bitmap.copyPixelsToBuffer(byteBuffer);
                      //     channel.write(byteBuffer);

                          byteData.write(img,0,img.length);
                            byteData.flush();
                           byteData.close();
                            socket.close();
                           socketOpen = false;
                           monitor.readyQueue();
                       }catch(IOException ex){}

                    }

                                  /*      byte[] image = queue.take();
                                        DataOutputStream data = new DataOutputStream(socket.getOutputStream());
                                        data.write(image);
                                        data.flush();
                                        socket.close();*/



                    //       endLoop = false;

                }//catch(FileNotFoundException ex){}
                //  catch(IOException ex){}
                catch (InterruptedException ex) {
                }
                //          }
                      /*     else if(msg.what==2){
                                try {
                                    String train = queue2.take();
                                }catch(InterruptedException ex){}
                            }*/
            }


        }


    }

  class AcceptConnection implements Runnable {
        BluetoothSocket socket,tmp;
        String id;
        BluetoothDevice device;
        BluetoothClient btClient;
        Handler handler;
        HandlerThread handlerThread;
        boolean endLoop = true;
        BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>(5);
        BlockingQueue<String> queueString = new LinkedBlockingQueue<String>(5);
      BlockingQueue<String> queueQuery = new LinkedBlockingQueue<String>(5);
        Looper looper;
        boolean initial = true;
      Thread bt;
      final Monitor monitor;
      byte[] image;
      MainActivity activity = new MainActivity();
        public AcceptConnection(BluetoothDevice device, String id,Monitor monitor) {
            tmp = null;

            this.monitor = monitor;
            this.id = id;
            this.device = device;
            try {
                // MY_UUID is the app's UUID string, also used by the server code

                this.tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(id));

            } catch (IOException e) {
            }
            socket = tmp;
            HandlerThread handlerThread = new HandlerThread("Something");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper()) {
                public void handleMessage(Message msg) {
                    //     while (endLoop) {

                    if (msg.what == 1) {

                        Bundle bund = msg.getData();

                        String path = bund.getString("pic");
                        System.out.println(path);
                        try {
                            image = getByteArrayFromImage(path);
                            System.out.println(image);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(image,0,image.length);

                          /*  synchronized(bt){
                                queueString.add("hey");
                                btClient.flag = false;
                                bt.notify();
                            }*/
                                queueString.add("Path");
                                queue.add(image);

                     //           getMonitor().readyQueue();



                              /*  DataOutputStream data = new DataOutputStream(socket.getOutputStream());
                                data.write(image);
                                data.flush();
                                socket.close();
                                endLoop = false;*/
                        } catch (FileNotFoundException ex) {
                        } catch (IOException ex) {
                        }

                    } else if (msg.what == 2) {
                        Bundle bund = msg.getData();
                        String fullName = bund.getString("fullName");
                        queueString.add("Train");
                        queueString.add(fullName);
                      //    System.out.println("bi");
             //           getMonitor().readyQueue();
                    }
                }
                //     }
            };

        }

        public void run() {
            connect();
            btClient = new BluetoothClient(socket, queue, queueString, monitor,queueQuery);
             bt = new Thread(btClient);
            bt.start();

            while (true) {

                try {
                    String query = queueQuery.take();

                    if(!query.equals("")){                    //name query database
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putString("query",query);
                        msg.setData(bundle);
                        msg.what = 1;
                        MainActivity.handler.sendMessage(msg);
                    }

                }catch(InterruptedException ex){}
                getMonitor().lockQueue();     //monitor is necessary here, otherwise thread will wait at queue at beginning of loop
                try {
                    bt.join();       //time should be enough
                }catch(InterruptedException ex){}
                if(!bt.isAlive()){
                connectAgain();
                btClient = new BluetoothClient(socket, queue, queueString, monitor,queueQuery);
                bt = new Thread(btClient);
                bt.start();}
          /*      try {
                    String query = queueString.take();

                }catch(InterruptedException ex){}*/

            }
        }
        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

        public Monitor getMonitor() {
            return monitor;
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

        public BluetoothSocket getSocket() {
            return socket;
        }
        private void connect(){

            try {

                socket.connect();
            } catch (IOException connectException) {

                try {
                    socket.close();
                } catch (IOException closeException) {
                }
                return;
            }
        }
      private void connectAgain(){
          try{
              tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(id));
          } catch (IOException e) {
          }
          socket = tmp;
          try {

              socket.connect();
          } catch (IOException connectException) {

              try {
                  socket.close();
              } catch (IOException closeException) {
              }
              return;
          }
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
    public void readyQueue(){
        try{
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
