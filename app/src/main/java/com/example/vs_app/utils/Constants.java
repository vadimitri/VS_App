package com.example.vs_app.utils;

public class Constants {
    // Bluetooth
    public static final String APP_NAME = "MomentShare";
    public static final String APP_UUID = "fa87c0d0-afac-11de-8a39-0800200c9a66";
    
    // Timeouts
    public static final int BLUETOOTH_CONNECT_TIMEOUT = 10000; // 10 seconds
    public static final int TRANSFER_TIMEOUT = 30000; // 30 seconds
    
    // File Transfer
    public static final int BUFFER_SIZE = 8192; // 8KB buffer
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    
    // Permission Request Codes
    public static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    public static final int REQUEST_CAMERA_PERMISSION = 2;
    public static final int REQUEST_STORAGE_PERMISSION = 3;
    
    private Constants() {
        // Prevent instantiation
    }
}