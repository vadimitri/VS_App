package com.example.vs_app;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TransferManager {
    private static final String TAG = "TransferManager";
    private static TransferManager instance;
    private final List<BluetoothSocket> sockets;

    public static synchronized TransferManager getInstance() {
        if (instance == null) {
            instance = new TransferManager();
        }
        return instance;
    }

    public TransferManager() {
        this.sockets = new ArrayList<>();
    }

    public void addSocket(BluetoothSocket socket) {
        if (!sockets.contains(socket)) {
            sockets.add(socket);
            startListening(socket);
        }
    }

    private void startListening(final BluetoothSocket socket) {
        Thread thread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    InputStream inputStream = socket.getInputStream();
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        // TODO: Implement photo receiving logic
                        Log.d(TAG, "Received " + bytes + " bytes");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading from socket: " + e.getMessage());
                    break;
                }
            }
        });
        thread.start();
    }

    public void sendPhoto(BluetoothSocket socket, byte[] photoData) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(photoData);
            outputStream.flush();
            Log.d(TAG, "Photo sent successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error sending photo: " + e.getMessage());
        }
    }

    public void removeSocket(BluetoothSocket socket) {
        sockets.remove(socket);
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing socket: " + e.getMessage());
        }
    }

    public void clearSockets() {
        for (BluetoothSocket socket : new ArrayList<>(sockets)) {
            removeSocket(socket);
        }
        sockets.clear();
    }
}
