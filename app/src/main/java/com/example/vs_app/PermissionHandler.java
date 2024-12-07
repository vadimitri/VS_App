package com.example.vs_app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionHandler {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private final Context context;
    private final Activity activity;

    public PermissionHandler(Activity activity) {
        this.activity = activity;
        this.context = activity;
    }

    public boolean checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Bluetooth Berechtigungen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ Berechtigungen
            checkPermission(permissionsNeeded, Manifest.permission.BLUETOOTH_SCAN);
            checkPermission(permissionsNeeded, Manifest.permission.BLUETOOTH_ADVERTISE);
            checkPermission(permissionsNeeded, Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            // Ältere Android Versionen
            checkPermission(permissionsNeeded, Manifest.permission.BLUETOOTH);
            checkPermission(permissionsNeeded, Manifest.permission.BLUETOOTH_ADMIN);
        }

        // Standort Berechtigungen
        checkPermission(permissionsNeeded, Manifest.permission.ACCESS_FINE_LOCATION);
        checkPermission(permissionsNeeded, Manifest.permission.ACCESS_COARSE_LOCATION);

        // Kamera Berechtigungen
        checkPermission(permissionsNeeded, Manifest.permission.CAMERA);

        // Speicher Berechtigungen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkPermission(permissionsNeeded, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        } else {
            checkPermission(permissionsNeeded, Manifest.permission.READ_EXTERNAL_STORAGE);
            checkPermission(permissionsNeeded, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    private void checkPermission(List<String> permissionsNeeded, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(permission);
        }
    }

    public boolean handlePermissionResult(int requestCode, String[] permissions,
                                        int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            return grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
}