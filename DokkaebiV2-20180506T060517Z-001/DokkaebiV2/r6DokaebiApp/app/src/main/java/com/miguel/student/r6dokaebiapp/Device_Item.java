package com.miguel.student.r6dokaebiapp;

/**
 * Created by student on 10/04/2018.
 */

public class Device_Item {

    private String deviceName;
    private String address;
    private boolean connected;

    public String getDeviceName(){
        return  deviceName;
    }

    public boolean getConnected(){
        return connected;
    }

    public String getAddress(){

        return address;
    }


    public void setDeviceName(String deviceName){

        this.deviceName = deviceName;

    }

    public Device_Item(String name, String address, String connected){

        this.deviceName = name;
        this.address = address;
        if(connected == "true"){

            this.connected = true;

        }else{

            this.connected = false;
        }
    }
}
