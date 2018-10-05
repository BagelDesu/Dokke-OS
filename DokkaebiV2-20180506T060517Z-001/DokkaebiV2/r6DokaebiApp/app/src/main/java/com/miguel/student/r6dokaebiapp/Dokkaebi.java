package com.miguel.student.r6dokaebiapp;


import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.VideoView;

import java.util.UUID;


public class Dokkaebi extends AppCompatActivity implements fragment_deviceitem_list.OnFragmentInteractionListener{

    //-------------------------------------INITIALIZATION------------------------------------------------------

    VideoView myV;
    Button deployButton;
    //Sets the vibration System.
    Vibrator v;
    String videoPath;
    Uri uri;
    FrameLayout Fragment1;

    //--------------------------BT VER.2 ------------------------------

    /**
     * required variables used by BT Ver.2
     */

    BluetoothAdapter BTAdapter;
    public static int REQUEST_BLUETOOTH = 1;
    android.support.v4.app.FragmentManager fragmentManager;
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    fragment_deviceitem_list f1;
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    ConnectionService mBTService;

    String payLoadCode = "enablePayload";
    String incomingMessage;
    boolean prePayloadCheck = false;

    //--------------------------BT VER.1 TRIAL-------------------------

    /**
     * Required Variables used by BT Ver.1
     */

   /* private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PAIRED_DEVICE = 2;

    Button btnListPairedDevices;
    BluetoothAdapter bluetoothAdapter;
    TextView stateBluetooth;*/

   //--------------------------Logic Bomb-------------------------

    //Pattern used by the VibrationSystem.
    private long[] vibratePattern = {1000, 100, 1000, 100, 1000, 100, 1000, 100};
    private float tapCount;
    private boolean enablePayload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dokkaebi);
        myV = (VideoView) findViewById(R.id.videoView);
        //deployButton = findViewById(R.id.payloadEnableBttn);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        videoPath = "android.resource://com.miguel.student.r6dokaebiapp/" + R.raw.dok2;
        uri = Uri.parse(videoPath);
        Fragment1 = findViewById(R.id.targetcontainer1);



        /**
         * Required for bluetooth discovery for devices 6.x
         * Make sure to add:
         * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
         * or
         * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
         * in App Manifest.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        }


        //blueToothOnCreate();
        btOnCreate();
        tapCounter();
    }


    public void deployLogicBomb(View v){
        enablePayload = true;
    }

//--------------------------------------------------UI ITEMS--------------------------------------------------
    //Tracks the taps from the user and records them.
    public void screenTapped(View view){

        tapCount = tapCount + 1;

    }

//Tap Counter, Handles the User Interaction with the app.
    private void tapCounter(){
        final Handler Update = new Handler();
        Update.post(new Runnable() {
            @Override
            public void run() {

                if(prePayloadCheck && incomingMessage.equals(payLoadCode)){
                    enablePayload = true;
                    incomingMessage = "payLoadDisabled";
                    prePayloadCheck = false;
                }

                //Checks if the user has tapped 5 times and closes the project.
                if(tapCount == 5){
                    enablePayload = false;
                    myV.stopPlayback();
                    Fragment1.setVisibility(View.VISIBLE);
                    myV.setVisibility(View.INVISIBLE);
                    v.cancel();
                    tapCount = 0;
                    //finish();
                   // System.exit(0);
                }
                if(enablePayload){
                    myV.setVisibility(View.VISIBLE);
                    Fragment1.setVisibility(View.INVISIBLE);
                    videoHandler();
                    enablePayload = false;
                }
                Update.postDelayed(this, 48);
            }
        });

    }



    //--------------------------------------PAYLOAD----------------------------------------------------

    //Handles the Video and the vibration.
    private void videoHandler(){

        //Find the video.
        //Turn it into a "Path" for the view to read.
        //Sets the video to loop
        myV.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        //Sets the path to video
        myV.setVideoURI(uri);
        //Starts the video.
        myV.start();
        //Hides the Deploy Button on Click

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(vibratePattern,VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            v.vibrate(vibratePattern, 1);
        }

    }

//--------------------------------------BLUETOOTH MECHANIC---------------------------------------------


//------------------------------BT_VER.2------------------------------------

    public void btOnCreate(){


        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        if(BTAdapter == null){
            new AlertDialog.Builder(this)
                    .setTitle("Not Compatible")
                    .setMessage("Your Phone Does Not Support BT")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            System.exit(0);

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if(!BTAdapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }
        //Testing out my way.

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        f1 = new fragment_deviceitem_list();
        fragment_deviceitem_list.newInstance(BTAdapter);
        fragmentTransaction.add(R.id.targetcontainer1, f1);
        fragmentTransaction.commit();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("Incoming Message"));
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            incomingMessage =  intent.getStringExtra("theMessage");


            Log.d("mReceiver", incomingMessage);
            if(incomingMessage.equals(payLoadCode)){

                Log.d("PAYLOAD", "Enabling Payload from Remote Position Using Code: " + incomingMessage);
                prePayloadCheck = true;
            }
        }
    };


    @Override
    public void onFragmentInteraction(String id) {



    }


    //--------------------------BT VER.1 TRIAL-------------------------

    /**
     * Attempting to find a better Bluetooth that constantly Updates and uses a fragment instead of reinitializing
     * an activity. This BT Scanner works but only displays already Paired devices. Unusable for the intent
     * of app.
     */



  /*  private void blueToothOnCreate(){





        btnListPairedDevices = (Button)findViewById(R.id.listPairedDevices);

        stateBluetooth = (TextView)findViewById(R.id.Status);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        CheckBlueToothState();

        btnListPairedDevices.setOnClickListener(btnListPairedDevicesOnClickListener);
    }

    private void CheckBlueToothState(){
        if (bluetoothAdapter == null){
            stateBluetooth.setText("Bluetooth NOT support");
        }else{
            if (bluetoothAdapter.isEnabled()){
                if(bluetoothAdapter.isDiscovering()){
                    stateBluetooth.setText("Bluetooth is currently in device discovery process.");
                }else{
                    stateBluetooth.setText("Bluetooth is Enabled.");
                    btnListPairedDevices.setEnabled(true);
                }
            }else{
                stateBluetooth.setText("Bluetooth is NOT Enabled!");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }


    private Button.OnClickListener btnListPairedDevicesOnClickListener
            = new Button.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            intent.setClass(Dokkaebi.this, ListPairedDevices.class);
            startActivityForResult(intent, REQUEST_PAIRED_DEVICE);
        }};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            CheckBlueToothState();
        }if (requestCode == REQUEST_PAIRED_DEVICE){
            if(resultCode == RESULT_OK){

            }
        }
    }*/



    //--------------------------BT VER.1 TRIAL-------------------------


}
