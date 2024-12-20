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