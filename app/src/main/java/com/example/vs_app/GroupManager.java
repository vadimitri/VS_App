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
    private final TransferManager transferManager;

    private GroupManager() {
        groupDevices = new ArrayList<>();
        connectedSockets = new ArrayList<>();
        transferManager = TransferManager.getInstance();
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
            connectToDevice(device);
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        try {
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BluetoothController.APP_UUID);
            socket.connect();
            connectedSockets.add(socket);
            transferManager.addSocket(socket);
            Log.d(TAG, "Successfully connected to device: " + device.getName());
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to device: " + e.getMessage());
            groupDevices.remove(device);
        }
    }

    public void sendPhotoToGroup(byte[] photoData) {
        for (BluetoothSocket socket : new ArrayList<>(connectedSockets)) {
            try {
                transferManager.sendPhoto(socket, photoData);
            } catch (Exception e) {
                Log.e(TAG, "Error sending photo to socket: " + e.getMessage());
                removeDevice(socket);
            }
        }
    }

    private void removeDevice(BluetoothSocket socket) {
        connectedSockets.remove(socket);
        transferManager.removeSocket(socket);
        for (BluetoothDevice device : new ArrayList<>(groupDevices)) {
            try {
                if (socket.getRemoteDevice().equals(device)) {
                    groupDevices.remove(device);
                    break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error removing device: " + e.getMessage());
            }
        }
    }

    public List<BluetoothDevice> getGroupDevices() {
        return new ArrayList<>(groupDevices);
    }

    public void clearGroup() {
        for (BluetoothSocket socket : new ArrayList<>(connectedSockets)) {
            removeDevice(socket);
        }
        groupDevices.clear();
        connectedSockets.clear();
        Log.d(TAG, "Cleared all devices from group");
    }

    public int getGroupSize() {
        return groupDevices.size();
    }
}
