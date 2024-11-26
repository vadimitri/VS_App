package com.example.vs_app;

// GossipController.java

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.util.List;

public class GossipController {
    private static GossipController instance;
    private final TransferManager transferManager;

    private GossipController() {
        transferManager = TransferManager.getInstance();
    }

    public static synchronized GossipController getInstance() {
        if (instance == null) {
            instance = new GossipController();
        }
        return instance;
    }

    public void initializeTransfer(BluetoothSocket socket) {
        transferManager.acceptTransfer(socket);
    }

    public void onGroupUpdate(List<BluetoothDevice> members) {
        // Update transfer status for all members
        for (BluetoothDevice device : members) {
            transferManager.updateTransferStatus(device);
        }
    }

    public void getProgress(BluetoothDevice device) {
        transferManager.getTransferProgress(device);
    }
}