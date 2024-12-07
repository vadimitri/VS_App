package com.example.vs_app;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransferManager {
    private static final String TAG = "TransferManager";
    private static final int BUFFER_SIZE = 8192;
    private static TransferManager instance;
    private final CopyOnWriteArrayList<BluetoothSocket> sockets;
    private final ExecutorService executor;
    private final AtomicBoolean isRunning;
    private Context applicationContext;
    private boolean isInitialized = false;

    private TransferManager() {
        this.sockets = new CopyOnWriteArrayList<>();
        this.executor = Executors.newCachedThreadPool();
        this.isRunning = new AtomicBoolean(true);
    }

    public static synchronized TransferManager getInstance() {
        if (instance == null) {
            instance = new TransferManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (!isInitialized) {
            this.applicationContext = context.getApplicationContext();
            isInitialized = true;
        }
    }

    public void addSocket(BluetoothSocket socket) {
        if (!sockets.contains(socket)) {
            sockets.add(socket);
            startListening(socket);
        }
    }

    private void startListening(final BluetoothSocket socket) {
        executor.execute(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes;

            while (isRunning.get() && socket.isConnected()) {
                try {
                    InputStream inputStream = socket.getInputStream();
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        handleReceivedData(buffer, bytes);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading from socket: " + e.getMessage());
                    removeSocket(socket);
                    break;
                }
            }
        });
    }

    private void handleReceivedData(byte[] buffer, int bytes) {
        // Implementiere Protokoll für Foto-Empfang
        File receivedFile = MediaManager.createReceivedFile("received_photo.jpg");
        // TODO: Implementiere Datei-Speicherung
    }

    public void sendPhoto(BluetoothSocket socket, byte[] photoData) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            // TODO: Implementiere Protokoll-Header
            outputStream.write(photoData);
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error sending photo: " + e.getMessage());
            removeSocket(socket);
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

    public void cleanup() {
        isRunning.set(false);
        executor.shutdown();
        for (BluetoothSocket socket : sockets) {
            removeSocket(socket);
        }
        sockets.clear();
        isInitialized = false;
    }
}