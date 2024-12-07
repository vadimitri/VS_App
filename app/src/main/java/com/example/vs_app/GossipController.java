package com.example.vs_app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GossipController {
    private static final String TAG = "GossipController";
    private static GossipController instance;
    private final TransferManager transferManager;
    private final Context context;
    private final ConcurrentHashMap<BluetoothDevice, TransferStatus> deviceStatus;

    public enum TransferStatus {
        IDLE,
        CONNECTING,
        TRANSFERRING,
        COMPLETED,
        ERROR
    }

    private GossipController(Context context) {
        this.context = context.getApplicationContext();
        this.transferManager = TransferManager.getInstance();
        this.deviceStatus = new ConcurrentHashMap<>();
    }

    public static synchronized GossipController getInstance(Context context) {
        if (instance == null) {
            instance = new GossipController(context);
        }
        return instance;
    }

    public void handleNewSocket(BluetoothSocket socket) {
        BluetoothDevice device = socket.getRemoteDevice();
        deviceStatus.put(device, TransferStatus.CONNECTING);
        transferManager.addSocket(socket);
        Log.d(TAG, "New socket accepted for device: " + device.getAddress());
    }

    public void updateDeviceStatus(List<BluetoothDevice> members) {
        for (BluetoothDevice device : members) {
            if (!deviceStatus.containsKey(device)) {
                deviceStatus.put(device, TransferStatus.IDLE);
            }
            Log.d(TAG, "Device status updated: " + device.getAddress() + " - " + deviceStatus.get(device));
        }
    }

    public TransferStatus getDeviceStatus(BluetoothDevice device) {
        return deviceStatus.getOrDefault(device, TransferStatus.IDLE);
    }

    public void setTransferStatus(BluetoothDevice device, TransferStatus status) {
        deviceStatus.put(device, status);
        Log.d(TAG, "Transfer status changed for " + device.getAddress() + ": " + status);
    }

    public void cleanup() {
        deviceStatus.clear();
    }
}