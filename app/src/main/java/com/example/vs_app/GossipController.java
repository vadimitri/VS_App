package com.example.vs_app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import java.util.List;

public class GossipController {
    private static GossipController instance;
    private final TransferManager transferManager;
    private final Context context;

    private GossipController(Context context) {
        this.context = context;
        this.transferManager = TransferManager.getInstance();
    }

    public static synchronized GossipController getInstance(Context context) {
        if (instance == null) {
            instance = new GossipController(context);
        }
        return instance;
    }

    public void initializeTransfer(BluetoothSocket socket) {
        transferManager.acceptTransfer(socket);
    }

    public void updateGroupMembers(List<BluetoothDevice> members) {
        for (BluetoothDevice device : members) {
            transferManager.updateTransferStatus(device);
        }
    }

    public void getProgress(BluetoothDevice device) {
        transferManager.getTransferProgress(device);
    }
}