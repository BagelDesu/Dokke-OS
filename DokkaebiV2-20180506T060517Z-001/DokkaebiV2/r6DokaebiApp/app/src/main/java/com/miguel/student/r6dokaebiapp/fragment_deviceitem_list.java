package com.miguel.student.r6dokaebiapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link fragment_deviceitem_list.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link fragment_deviceitem_list#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_deviceitem_list extends Fragment implements AbsListView.OnItemClickListener{

    private ArrayList <Device_Item>deviceItemList;
    private ArrayList<BluetoothDevice> mBtDevices = new ArrayList<>();
    private EditText mCommandLine;

    private OnFragmentInteractionListener mListener;
    private static BluetoothAdapter bTadapter;

    private ArrayAdapter<Device_Item> mAdapter;
    private AbsListView mListView;

    ConnectionService mConnectionService;

    private BluetoothDevice mDevice;

    private static final UUID LOGIC_BOMB_UUID = UUID.fromString("f41e3e40-425b-11e8-b566-0800200c9a66");

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Log.d("DEVICE LIST", "Bluetooth device found\n ");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Create a new device item

                Device_Item newDevice = new Device_Item(device.getName(), device.getAddress(), "false");
                mBtDevices.add(device);
                //add it to our adapter

                mAdapter.add(newDevice);
                mAdapter.notifyDataSetChanged();
            }
        }
    };


    public static fragment_deviceitem_list newInstance(BluetoothAdapter adapter){
        fragment_deviceitem_list fragment = new fragment_deviceitem_list();
        bTadapter = adapter;
        return fragment;

    }


    public fragment_deviceitem_list() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        Log.d("DEVICELIST", "Super called for fragment_deviceitem_list onCreate\n");

        deviceItemList = new ArrayList<Device_Item>();

        Set<BluetoothDevice> pairedDevices = bTadapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            for (BluetoothDevice device : pairedDevices){
                Device_Item newDevice = new Device_Item(device.getName(), device.getAddress(), "false");
                deviceItemList.add(newDevice);
                mBtDevices.add(device);
            }
        }

        //If there are no devices, add an item that states so. It will be handled in the view
        if(deviceItemList.size() == 0){
            deviceItemList.add(new Device_Item("No Devices","", "false"));
        }

        Log.d("DEVICELIST", "DeviceList populated\n");

        mAdapter = new DeviceListAdapter(getActivity(), deviceItemList, bTadapter);

        Log.d("DEVICELIST", "Adapter created\n");

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fragment_deviceitem_list, container, false);

        ToggleButton scan = (ToggleButton)view.findViewById(R.id.scan);
        //set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mCommandLine = view.findViewById(R.id.commandLine);
        //Set OnItemClickListener so we can be notified of item clicks
        mListView.setOnItemClickListener(this);

        Button deployButton = view.findViewById(R.id.connectBtn);
        final Button sendButton = view.findViewById(R.id.Send);

        deployButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                mCommandLine.setText("");
            }
        });

        scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                if(b){
                    mAdapter.clear();
                    getActivity().registerReceiver(bReciever, filter);
                    bTadapter.startDiscovery();
                }else{
                    getActivity().unregisterReceiver(bReciever);
                    bTadapter.cancelDiscovery();
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       try{
           mListener = (OnFragmentInteractionListener) activity;
       }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + "must implement OnFragmentInteractionListener");
       }
    }




    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void startConnection(){
        startBTConnnection(mDevice, LOGIC_BOMB_UUID);
    }

    public void startBTConnnection(BluetoothDevice device, UUID uuid){ mConnectionService.startClient(device, uuid);}

    public void sendMessage(){
        byte[] bytes = mCommandLine.getText().toString().getBytes(Charset.defaultCharset());
        mConnectionService.write(bytes);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("DEVICELIST", "onItemClick position: " + position +
                " id: " + id + " name: " + deviceItemList.get(position).getDeviceName() + "\n");
        mBtDevices.get(position).createBond();
        mDevice = mBtDevices.get(position);

        mConnectionService = new ConnectionService(getContext());


        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(deviceItemList.get(position).getDeviceName());
        }

    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String id);
    }
}
