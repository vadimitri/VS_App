package com.example.vs_app;

// TransferManager.java

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.Manifest;
import androidx.core.app.ActivityCompat;

import androidx.core.app.ActivityCompat;

import java.io.*;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Map;

public class TransferManager {
    private static final String TAG = "TransferManager";
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1;
    private static TransferManager instance;
    private final ExecutorService transferExecutor;
    private final Map<BluetoothDevice, TransferStatus> transferStatuses;
    private final BlockingQueue<File> transferQueue;


    private TransferManager() {
        transferExecutor = Executors.newCachedThreadPool();
        transferStatuses = new ConcurrentHashMap<>();
        transferQueue = new LinkedBlockingQueue<>();
    }

    public static synchronized TransferManager getInstance() {
        if (instance == null) {
            instance = new TransferManager();
        }
        return instance;
    }

    public void queueForTransfer(File file) {
        transferQueue.offer(file);
        processQueue();
    }

    public void acceptTransfer(BluetoothSocket socket) {
        transferExecutor.execute(new ReceiveTask(socket));
    }

    public void updateTransferStatus(BluetoothDevice device) {
        TransferStatus status = transferStatuses.get(device);
        if (status != null) {
            // Überprüfe und aktualisiere den Transfer-Status
            status.checkProgress();
        }
    }

    public int getTransferProgress(BluetoothDevice device) {
        TransferStatus status = transferStatuses.get(device);
        return status != null ? status.getProgress() : 0;
    }

    private void processQueue() {
        transferExecutor.execute(() -> {
            while (!transferQueue.isEmpty()) {
                File file = transferQueue.poll();
                if (file != null) {
                    for (BluetoothDevice device : GroupManager.getInstance().getGroupMembers()) {
                        sendFile(device, file);
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void sendFile(BluetoothDevice device, File file) {
        try {
            @SuppressLint("MissingPermission") BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BluetoothController.APP_UUID);
            transferExecutor.execute(new SendTask(socket, file));
        } catch (IOException e) {
            Log.e(TAG, "Error creating socket for device: " + device.getName(), e);
        }
    }

    private class SendTask implements Runnable {
        private final BluetoothSocket socket;
        private final File file;

        SendTask(BluetoothSocket socket, File file) {
            this.socket = socket;
            this.file = file;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            try {

                socket.connect();

                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                // Sende Datei-Metadaten
                dataOutputStream.writeUTF(file.getName());
                dataOutputStream.writeLong(file.length());

                // Sende Dateiinhalt
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    updateProgress(socket.getRemoteDevice(),
                            (int)((totalBytesRead * 100) / file.length()));
                }

                fileInputStream.close();
                socket.close();
            }  catch (IOException e) {
                Log.e(TAG, "Error connecting to socket", e);
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close socket", closeException);
                }
            }
        }
    }

    private class ReceiveTask implements Runnable {
        private final BluetoothSocket socket;


        ReceiveTask(BluetoothSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inputStream);

                // Lese Datei-Metadaten
                String fileName = dataInputStream.readUTF();
                long fileSize = dataInputStream.readLong();

                // Erstelle Ausgabedatei
                BluetoothDevice sender = socket.getRemoteDevice();
                @SuppressLint("MissingPermission") File outputFile = MediaManager.createReceivedFile(
                        sender.getName() + "_" + fileName
                );                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

                // Empfange Dateiinhalt
                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize &&
                        (bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    updateProgress(socket.getRemoteDevice(),
                            (int)((totalBytesRead * 100) / fileSize));
                }

                fileOutputStream.close();
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error receiving file", e);
            }
        }
    }

    private void updateProgress(BluetoothDevice device, int progress) {
        TransferStatus status = transferStatuses.computeIfAbsent(device,
                k -> new TransferStatus());
        status.setProgress(progress);
    }

    private static class TransferStatus {
        private volatile int progress;
        private long lastUpdate;

        public void setProgress(int progress) {
            this.progress = progress;
            this.lastUpdate = System.currentTimeMillis();
        }

        public int getProgress() {
            return progress;
        }

        public void checkProgress() {
            // Überprüft, ob der Transfer noch aktiv ist
            if (progress < 100 &&
                    System.currentTimeMillis() - lastUpdate > 30000) { // 30 Sekunden Timeout
                progress = -1; // Markiere als fehlgeschlagen
            }
        }
    }
}