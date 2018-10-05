package com.miguel.student.r6dokaebiapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.logging.Handler;

import static android.content.ContentValues.TAG;

public class ConnectionService {

    private static final String appName = "Dokkaebi Bomb";

    private static final UUID LOGIC_BOMB_UUID = UUID.fromString("f41e3e40-425b-11e8-b566-0800200c9a66");


    private final BluetoothAdapter mmBtAdapter;
    private BluetoothDevice mmDevice;

    private ConnectedThread mmConnectedThread;
    private ConnectThread mConnectThread;
    private AcceptThread mAcceptThread;

    private UUID deviceUUID;

    Context mContext;

    public ConnectionService(Context context){
        mContext = context;
        mmBtAdapter = BluetoothAdapter.getDefaultAdapter();
        startAcceptThread();
    }

    //___________________________________________________________________ CONNECT THREAD ______________________________________________________

    /**
     * Connect Thread. Actively sends out a request for a connection to be made. Blocks the thread, make sure to stop sending request
     * if a connection is made.
     */

    public class ConnectThread extends Thread {

        private BluetoothSocket mmSocket;

        public ConnectThread (BluetoothDevice device, UUID uuid){
            //assign a temp for the socket.
            mmDevice = device;
            deviceUUID = uuid;

        }

        public void run(){
            //Cancel Discovery to save on resource.
            mmBtAdapter.cancelDiscovery();
            BluetoothSocket tmp = null;


            try{
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.

                tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);

            }catch (IOException e){

                Log.e("CONNECT THREAD", "Failed to create RFcommSocket", e);

            }

            mmSocket = tmp;

            try{
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.

                Log.d("CONNECT THREAD", "Attempting to connect to socket");
                mmSocket.connect();
            }catch(IOException connectException){
                Log.e("CONNECT THREAD", "Connection failed, attmepting to close connection", connectException);
                try{
                    mmSocket.close();
                }catch (IOException closeException){
                    Log.e("CONNECT THREAD", "Failed to close connection", closeException );
                }
            }

            //TODO Add Connection Management part.

            connected(mmSocket, mmDevice);


        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException closeFailure){
                Log.e("CONNECT THREAD", "Failed to close connection manually", closeFailure);
            }

        }
    }


//------------------------------------------------------------------- ACCEPT THREAD  ------------------------------------------------------------

    /**
     * Accept Thread. Actively listens for a connection Request when it is called. onCall will block the entire thread so make sure to
     * stop it as soon as a connection has been received.
     */

    public class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;


        public AcceptThread() {

            BluetoothServerSocket tmp = null;

            try{
                tmp = mmBtAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, LOGIC_BOMB_UUID);
            } catch (IOException e){
                Log.e("ACCEPT THREAD", "Socket Listen Method Fail", e);
            }

            mmServerSocket = tmp;
        }

        public void run(){
            BluetoothSocket mmbluetoothSocket = null;

                try {

                    Log.d("ACCEPT THREAD", "attempting to receive socket ");

                    mmbluetoothSocket = mmServerSocket.accept();

                    Log.d("ACCEPT THREAD", "Succesfully Created a Accepted a Socket");

                } catch (IOException e) {

                    Log.e("ACCEPT THREAD", "Socket accept() method failed", e);


                }


                if (mmbluetoothSocket != null) {
                    connected(mmbluetoothSocket , mmDevice);
                }
        }



        public void cancel(){
            try {
                Log.d("ACCEPT THREAD", "cancel: CANCELING ACCEPT THREAD");
                mmServerSocket.close();
            }catch (IOException e){
                Log.e("ACCEPT THREAD","Failed to close the socket" + e.getMessage());
            }

        }
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ CONNECTED THREAD ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Handles post connection requests. also handles write and read of data streams.
     */

    public class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        public final InputStream mmInputStream;
        private final OutputStream mmOutputStream;

        public ConnectedThread(BluetoothSocket socket){

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;





            try{
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();

            }catch (IOException e){
                e.printStackTrace();
            }

            mmInputStream = tmpIn;
            mmOutputStream = tmpOut;

        }

        public void run(){
            byte[] buffer = new byte[1024]; // buffer store for the stream

            int bytes;

            while (true){
                try {
                    bytes = mmInputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);

                    Intent incomingMessageIntetnt = new Intent("Incoming Message");
                    incomingMessageIntetnt.putExtra("theMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntetnt);

                    Log.d(TAG, "InputStream: " + incomingMessage);

                }catch (IOException e){
                    Log.e(TAG,"write: Error reading inputstream: " + e.getMessage());
                    break;
                }
            }
        }

        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to Output Stream: " + text );

            try {
                mmOutputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG,"write: Error writing to outputstream: " + e.getMessage());
            }

        }

        public void cancel(){

            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


// _+_+_+_+_+_++_+_++___+_+_++__++_+__++__+_++_+_+__++__+_+_+_+__+_+_+_ FUNCTIONS _+_+_+_+_+__+_+_++_+__++_+_++__+_+_+_+_++_+_+_+_+_+_+_

    /**
     * Functons needed to be called by the threads.
     */



    public synchronized void startAcceptThread(){
        Log.d("START BT", "Starting Connection" );

        if(mConnectThread != null ){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if( mAcceptThread == null){
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();

        }
    }

    public void startClient(BluetoothDevice btdevice, UUID uuid ){

        Log.d("START BT CLIENT", "Starting up Client Side");

        mConnectThread = new ConnectThread(btdevice, uuid);
        mConnectThread.start();

    }


    private void connected(BluetoothSocket btsocket, BluetoothDevice mmDevcie){
        Log.d("Conncted Thread", "connected: starting." );

        mmConnectedThread = new ConnectedThread(btsocket);
        mmConnectedThread.start();
    }

    public void write(byte[] out){

        Log.d("Connected Thread", "write: Write Called");
        mmConnectedThread.write(out);
    }
}
