package com.miguel.student.r6dokaebiapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by student on 10/04/2018.
 */

public class DeviceListAdapter extends ArrayAdapter<Device_Item>{
    private Context context;
    private BluetoothAdapter bTAdapter;

    public DeviceListAdapter(Context context, List items, BluetoothAdapter bTAdapter) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.bTAdapter = bTAdapter;
        this.context = context;
    }

    /**
     * Holder for the list items.
     */
    private class ViewHolder{
        TextView titleText;
    }

    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View line = null;
        Device_Item item = (Device_Item)getItem(position);
        final String name = item.getDeviceName();
        TextView macAddress = null;
        View viewToUse = null;

        // This block exists to inflate the settings list item conditionally based on whether
        // we want to support a grid or list view.
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        viewToUse = mInflater.inflate(R.layout.device_list_item, null);
        holder = new ViewHolder();
        holder.titleText = (TextView)viewToUse.findViewById(R.id.titleTextView);
        viewToUse.setTag(holder);

        macAddress = (TextView)viewToUse.findViewById(R.id.macAddress);
        line = (View)viewToUse.findViewById(R.id.line);
        holder.titleText.setText(item.getDeviceName());
        macAddress.setText(item.getAddress());


        /**
         * Checks the names of the devices and Displays them. If the devices have no names the app will crash.
         * do not remove the first outer layer of "if" statesment.
         */

        if(item.getDeviceName() != null){

            if ( item.getDeviceName().toString() == "No Devices") {
                macAddress.setVisibility(View.INVISIBLE);
                line.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                        ((int) RelativeLayout.LayoutParams.WRAP_CONTENT, (int) RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                holder.titleText.setLayoutParams(params);
            }


        }


        return viewToUse;
    }

}
