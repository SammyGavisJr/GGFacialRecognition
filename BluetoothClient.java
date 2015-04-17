package com.example.sameer.googleface;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
        String in;
        public Handler handler;
        boolean flag = true;
        BlockingQueue<byte[]> queue;
        BlockingQueue<String> queue2;
        Monitor monitor;
        public BluetoothClient(BluetoothSocket btSocket, BlockingQueue<byte[]> queue, BlockingQueue<String> queue2,Monitor monitor) {
            this.device = device;
            this.queue = queue;
            this.queue2 = queue2;
            this.monitor = monitor;
            socket = btSocket;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);


            } catch (IOException ex) {
            }


        }

        public void run() {


            while (true) {

                //        if(msg.what==1) {

                try {
                    //   if(queue!=null) {
                    monitor.lockQueue();
                    Log.e("sentinel", "Hey");
                    String hey = queue2.take();
                                  /*      byte[] image = queue.take();
                                        DataOutputStream data = new DataOutputStream(socket.getOutputStream());
                                        data.write(image);
                                        data.flush();
                                        socket.close();*/

                    output.write(hey);
                    output.flush();

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
        BluetoothSocket socket;
        String id;
        BluetoothDevice device;
        BluetoothClient btClient;
        Handler handler;
        HandlerThread handlerThread;
        boolean endLoop = true;
        BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>(5);
        BlockingQueue<String> queueString = new LinkedBlockingQueue<String>(5);
        Looper looper;
      final Monitor monitor;
      byte[] image;
        public AcceptConnection(BluetoothDevice device, String id,Monitor monitor) {
            BluetoothSocket tmp = null;

            this.monitor = monitor;
            this.id = id;
            this.device = device;
            try {
                // MY_UUID is the app's UUID string, also used by the server code

                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(id));

            } catch (IOException e) {
            }
            socket = tmp;
            handler = new Handler() {
                public void handleMessage(Message msg) {
                    //     while (endLoop) {

                    if (msg.what == 1) {

                        Bundle bund = msg.getData();
                        String path = bund.getString("pic");
                        System.out.println(path);
                        try {
                            image = getByteArrayFromImage(path);
                          /*  synchronized(bt){
                                queueString.add("hey");
                                btClient.flag = false;
                                bt.notify();
                            }*/

                                queue.add(image);
                                System.out.println("gggg");
                                getMonitor().readyQueue();



                              /*  DataOutputStream data = new DataOutputStream(socket.getOutputStream());
                                data.write(image);
                                data.flush();
                                socket.close();
                                endLoop = false;*/
                        } catch (FileNotFoundException ex) {
                        } catch (IOException ex) {
                        }

                    } else if (msg.what == 2) {
                     //   queueString.add("Train");
                          System.out.println("bi");

                    }
                }
                //     }
            };

        }

        public void run() {


            try {

                socket.connect();
            } catch (IOException connectException) {

                try {
                    socket.close();
                } catch (IOException closeException) {
                }
                return;
            }
            btClient = new BluetoothClient(socket, queue, queueString,monitor);
            final Thread bt = new Thread(btClient);
            bt.start();
            Looper.prepare();


            Looper.loop();


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