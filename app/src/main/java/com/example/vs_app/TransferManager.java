package com.example.vs_app;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
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
    private static final int HEADER_SIZE = 8;
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
        checkInitialization();
        if (!sockets.contains(socket)) {
            sockets.add(socket);
            startListening(socket);
        }
    }

    private void startListening(final BluetoothSocket socket) {
        executor.execute(() -> {
            byte[] header = new byte[HEADER_SIZE];
            
            while (isRunning.get() && socket.isConnected()) {
                try {
                    // Read header first
                    InputStream inputStream = socket.getInputStream();
                    if (readExactly(inputStream, header) != HEADER_SIZE) {
                        continue;
                    }

                    // Parse header
                    int dataSize = parseHeader(header);
                    if (dataSize <= 0) {
                        continue;
                    }

                    // Read data
                    byte[] data = new byte[dataSize];
                    if (readExactly(inputStream, data) == dataSize) {
                        handleReceivedData(data);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading from socket: " + e.getMessage());
                    removeSocket(socket);
                    break;
                }
            }
        });
    }

    private int readExactly(InputStream inputStream, byte[] buffer) throws IOException {
        int totalRead = 0;
        while (totalRead < buffer.length) {
            int read = inputStream.read(buffer, totalRead, buffer.length - totalRead);
            if (read == -1) {
                break;
            }
            totalRead += read;
        }
        return totalRead;
    }

    private int parseHeader(byte[] header) {
        // Simple header format: 8 bytes for size
        return (header[0] & 0xFF) << 24 | 
               (header[1] & 0xFF) << 16 | 
               (header[2] & 0xFF) << 8  | 
               (header[3] & 0xFF);
    }

    private void createHeader(byte[] header, int size) {
        header[0] = (byte) (size >> 24);
        header[1] = (byte) (size >> 16);
        header[2] = (byte) (size >> 8);
        header[3] = (byte) size;
    }

    private void handleReceivedData(byte[] data) {
        File receivedFile = MediaManager.createReceivedFile("photo.jpg");
        try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
            fos.write(data);
            fos.flush();
            
            // Make visible in gallery
            MediaScannerConnection.scanFile(
                applicationContext,
                new String[]{receivedFile.getPath()},
                new String[]{"image/jpeg"},
                null
            );
        } catch (IOException e) {
            Log.e(TAG, "Failed to save received file: " + e.getMessage());
        }
    }

    public void sendPhoto(BluetoothSocket socket, byte[] photoData) {
        checkInitialization();
        try {
            OutputStream outputStream = socket.getOutputStream();
            
            // Send header
            byte[] header = new byte[HEADER_SIZE];
            createHeader(header, photoData.length);
            outputStream.write(header);
            
            // Send photo data
            outputStream.write(photoData);
            outputStream.flush();
            
            Log.d(TAG, "Photo sent successfully: " + photoData.length + " bytes");
        } catch (IOException e) {
            Log.e(TAG, "Error sending photo: " + e.getMessage());
            removeSocket(socket);
        }
    }

    private void checkInitialization() {
        if (!isInitialized) {
            throw new IllegalStateException("TransferManager must be initialized before use");
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