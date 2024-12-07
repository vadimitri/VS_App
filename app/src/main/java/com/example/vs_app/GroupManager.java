package com.example.vs_app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupManager {
    private static final String TAG = "GroupManager";
    private static GroupManager instance;
    private final List<BluetoothDevice> groupDevices;
    private final List<BluetoothSocket> connectedSockets;

    private GroupManager() {
        groupDevices = new ArrayList<>();
        connectedSockets = new ArrayList<>();
    }

    public static synchronized GroupManager getInstance() {
        if (instance == null) {
            instance = new GroupManager();
        }
        return instance;
    }

    public void addDeviceToGroup(BluetoothDevice device) {
        if (!groupDevices.contains(device)) {
            groupDevices.add(device);
            Log.d(TAG, "Added device to group: " + device.getName());
            // TODO: Implement connection logic here
        }
    }

    public void removeDeviceFromGroup(BluetoothDevice device) {
        groupDevices.remove(device);
        Log.d(TAG, "Removed device from group: " + device.getName());
    }

    public List<BluetoothDevice> getGroupDevices() {
        return new ArrayList<>(groupDevices);
    }

    public boolean isDeviceInGroup(BluetoothDevice device) {
        return groupDevices.contains(device);
    }

    public void clearGroup() {
        groupDevices.clear();
        Log.d(TAG, "Cleared all devices from group");
    }

    public int getGroupSize() {
        return groupDevices.size();
    }
}