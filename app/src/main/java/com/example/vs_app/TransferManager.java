package com.example.vs_app;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferManager {
    private static final String TAG = "TransferManager";
    private static final int BUFFER_SIZE = 8192; // 8KB buffer
    private static TransferManager instance;
    private final CopyOnWriteArrayList<BluetoothSocket> sockets;
    private final ExecutorService executor;
    private volatile boolean isRunning;

    private TransferManager() {
        this.sockets = new CopyOnWriteArrayList<>();
        this.executor = Executors.newCachedThreadPool();
        this.isRunning = true;
    }

    public static synchronized TransferManager getInstance() {
        if (instance == null) {
            instance = new TransferManager();
        }
        return instance;
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

            while (isRunning && socket.isConnected()) {
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
        // TODO: Implement protocol for receiving photos
        // For now, just log the received data
        Log.d(TAG, "Received " + bytes + " bytes");
    }

    public void queueForTransfer(File file) {
        if (!file.exists()) {
            Log.e(TAG, "File does not exist: " + file.getPath());
            return;
        }

        executor.execute(() -> {
            try {
                byte[] fileData = readFileToBytes(file);
                for (BluetoothSocket socket : sockets) {
                    if (socket.isConnected()) {
                        sendData(socket, fileData);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading file: " + e.getMessage());
            }
        });
    }

    private byte[] readFileToBytes(File file) throws IOException {
        byte[] fileData = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileData);
        }
        return fileData;
    }

    private void sendData(BluetoothSocket socket, byte[] data) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            // TODO: Implement proper protocol with headers
            outputStream.write(data);
            outputStream.flush();
            Log.d(TAG, "Data sent successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error sending data: " + e.getMessage());
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

    public void shutdown() {
        isRunning = false;
        executor.shutdown();
        clearSockets();
    }

    public void clearSockets() {
        for (BluetoothSocket socket : sockets) {
            removeSocket(socket);
        }
        sockets.clear();
    }
}