package com.example.vs_app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GroupManager {
    private static final String TAG = "GroupManager";
    private static GroupManager instance;
    private final CopyOnWriteArrayList<BluetoothDevice> groupDevices;
    private final CopyOnWriteArrayList<BluetoothSocket> connectedSockets;
    private final TransferManager transferManager;
    private Context applicationContext;
    private boolean isInitialized = false;

    private GroupManager() {
        groupDevices = new CopyOnWriteArrayList<>();
        connectedSockets = new CopyOnWriteArrayList<>();
        transferManager = TransferManager.getInstance();
    }

    public static synchronized GroupManager getInstance() {
        if (instance == null) {
            instance = new GroupManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (!isInitialized) {
            this.applicationContext = context.getApplicationContext();
            transferManager.initialize(applicationContext);
            isInitialized = true;
            Log.d(TAG, "GroupManager initialized");
        }
    }

    public void addDeviceToGroup(BluetoothDevice device) {
        checkInitialization();
        if (!groupDevices.contains(device)) {
            groupDevices.add(device);
            connectToDevice(device);
            Log.d(TAG, "Added device to group: " + device.getName());
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

    public void removeDevice(BluetoothDevice device) {
        checkInitialization();
        groupDevices.remove(device);
        for (BluetoothSocket socket : connectedSockets) {
            try {
                if (socket.getRemoteDevice().equals(device)) {
                    transferManager.removeSocket(socket);
                    socket.close();
                    connectedSockets.remove(socket);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket: " + e.getMessage());
            }
        }
    }

    public List<BluetoothDevice> getGroupDevices() {
        checkInitialization();
        return new ArrayList<>(groupDevices);
    }

    public int getGroupSize() {
        checkInitialization();
        return groupDevices.size();
    }

    public void sendPhotoToGroup(byte[] photoData) {
        checkInitialization();
        for (BluetoothSocket socket : connectedSockets) {
            try {
                transferManager.sendPhoto(socket, photoData);
            } catch (Exception e) {
                Log.e(TAG, "Error sending photo: " + e.getMessage());
                try {
                    socket.close();
                } catch (IOException closeError) {
                    Log.e(TAG, "Error closing socket: " + closeError.getMessage());
                }
                connectedSockets.remove(socket);
            }
        }
    }

    private void checkInitialization() {
        if (!isInitialized) {
            throw new IllegalStateException("GroupManager must be initialized before use");
        }
    }

    public void cleanup() {
        for (BluetoothSocket socket : connectedSockets) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket during cleanup: " + e.getMessage());
            }
        }
        connectedSockets.clear();
        groupDevices.clear();
        transferManager.cleanup();
        isInitialized = false;
    }
}