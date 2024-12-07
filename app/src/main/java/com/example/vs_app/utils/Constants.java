package com.example.vs_app.utils;

import java.util.UUID;

public class Constants {
    // Bluetooth Constants
    public static final String APP_NAME = "MomentShare";
    public static final UUID APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final int BT_CONNECTION_TIMEOUT = 10000; // 10 seconds
    public static final int BT_RETRY_ATTEMPTS = 3;
    public static final int BT_RETRY_DELAY = 1000; // 1 second

    // File Transfer Constants
    public static final int TRANSFER_BUFFER_SIZE = 8192;
    public static final int MAX_TRANSFER_RETRIES = 3;
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    public static final int TRANSFER_TIMEOUT = 30000; // 30 seconds

    // Error Messages
    public static final String ERROR_BLUETOOTH_NOT_AVAILABLE = "Bluetooth is not available on this device";
    public static final String ERROR_BLUETOOTH_NOT_ENABLED = "Bluetooth is not enabled";
    public static final String ERROR_CONNECTION_FAILED = "Failed to establish connection";
    public static final String ERROR_TRANSFER_FAILED = "File transfer failed";
    public static final String ERROR_FILE_TOO_LARGE = "File is too large to transfer";
    public static final String ERROR_INVALID_FILE = "Invalid file for transfer";
}
