package com.example.vs_app;

// BluetoothController.java
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.util.UUID;

public class BluetoothController {
    private static final String APP_NAME = "MomentShare";
    private static final UUID APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private final Handler mainHandler;
    private AcceptThread acceptThread;
    private final GroupManager groupManager;

    public BluetoothController(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.groupManager = new GroupManager();
    }

    public void startDiscovery() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
    }

    public void makeDiscoverable() {
        if (bluetoothAdapter != null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, APP_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }

                if (socket != null) {
                    manageConnection(socket);
                }
            }
        }
    }

    private void manageConnection(BluetoothSocket socket) {
        BluetoothDevice device = socket.getRemoteDevice();
        groupManager.addMember(device);

        // Notify GossipController about new connection
        GossipController.getInstance().initializeTransfer(socket);
    }
}