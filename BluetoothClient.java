package com.example.sameer.googleface;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import java.util.UUID;

/**
 * Created by Sameer on 3/28/2015.
 */
public class BluetoothClient implements Runnable
{

    UUID uuid;
    public BluetoothClient(BluetoothDevice device)
    {
        ParcelUuid[] parcel = device.getUuids();
        
    }
    public void run()
    {

    }
}
