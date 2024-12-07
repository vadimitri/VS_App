package com.example.vs_app;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class BluetoothController {
    private static final String TAG = "BluetoothController";
    private static final String APP_NAME = "MomentShare";
    public static final UUID APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds

    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private final Handler mainHandler;
    private AcceptThread acceptThread;
    private final GroupManager groupManager;
    private final AtomicBoolean isRunning;
    private BluetoothCallback bluetoothCallback;

    public interface BluetoothCallback {
        void onDeviceConnected(BluetoothDevice device);
        void onConnectionFailed(BluetoothDevice device, String reason);
        void onBluetoothNotAvailable();
    }

    public BluetoothController(Context context, BluetoothCallback callback) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.groupManager = GroupManager.getInstance();
        this.isRunning = new AtomicBoolean(false);
        this.bluetoothCallback = callback;

        if (bluetoothAdapter == null) {
            mainHandler.post(() -> bluetoothCallback.onBluetoothNotAvailable());
        }
    }

    @SuppressLint("MissingPermission")
    public boolean startDiscovery() {
        if (!isBluetoothAvailable()) {
            return false;
        }

        // Falls bereits eine Suche läuft, diese stoppen
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Neue Suche starten
        return bluetoothAdapter.startDiscovery();
    }

    public void makeDiscoverable() {
        if (!isBluetoothAvailable()) {
            return;
        }

        stopAcceptThread();
        acceptThread = new AcceptThread();
        acceptThread.start();
        isRunning.set(true);
    }

    public void stop() {
        isRunning.set(false);
        stopAcceptThread();
    }

    private void stopAcceptThread() {
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
    }

    private boolean isBluetoothAvailable() {
        if (bluetoothAdapter == null) {
            mainHandler.post(() -> bluetoothCallback.onBluetoothNotAvailable());
            return false;
        }
        return true;
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;
        private boolean shouldContinue;

        @SuppressLint("MissingPermission")
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            shouldContinue = true;

            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, APP_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket listen() failed", e);
                mainHandler.post(() -> bluetoothCallback.onConnectionFailed(null, "Failed to create server socket"));
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (shouldContinue && isRunning.get()) {
                try {
                    if (serverSocket != null) {
                        socket = serverSocket.accept(CONNECTION_TIMEOUT);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Socket accept() failed", e);
                    break;
                }

                if (socket != null) {
                    manageConnection(socket);
                    try {
                        serverSocket.close(); // Accept one connection at a time
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close server socket", e);
                    }
                    break;
                }
            }
        }

        public void cancel() {
            shouldContinue = false;
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not close server socket", e);
            }
        }
    }

    private void manageConnection(BluetoothSocket socket) {
        BluetoothDevice device = socket.getRemoteDevice();
        if (groupManager.addMember(device)) {
            mainHandler.post(() -> bluetoothCallback.onDeviceConnected(device));
            // Notify GossipController about new connection
            GossipController.getInstance().initializeTransfer(socket);
        } else {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close socket after failed group addition", e);
            }
            mainHandler.post(() -> bluetoothCallback.onConnectionFailed(device, "Failed to add to group"));
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }
}
