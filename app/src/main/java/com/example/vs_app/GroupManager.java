package com.example.vs_app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupManager {
    private static final String TAG = "GroupManager";
    private static GroupManager instance;
    private final List<BluetoothDevice> groupDevices;
    private final List<BluetoothSocket> connectedSockets;
    private final TransferManager transferManager;

    private GroupManager() {
        groupDevices = new ArrayList<>();
        connectedSockets = new ArrayList<>();
        transferManager = new TransferManager();
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
            connectToDevice(device);
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        try {
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BluetoothController.APP_UUID);
            connectedSockets.add(socket);
            transferManager.addSocket(socket);
            socket.connect();
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to device: " + e.getMessage());
        }
    }

    public void sendPhotoToGroup(byte[] photoData) {
        for (BluetoothSocket socket : connectedSockets) {
            transferManager.sendPhoto(socket, photoData);
        }
    }

    public List<BluetoothDevice> getGroupDevices() {
        return new ArrayList<>(groupDevices);
    }

    public void clear() {
        for (BluetoothSocket socket : connectedSockets) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket: " + e.getMessage());
            }
        }
        connectedSockets.clear();
        groupDevices.clear();
    }
}