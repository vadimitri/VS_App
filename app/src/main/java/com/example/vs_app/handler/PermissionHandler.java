package com.example.vs_app.handler;

// PermissionHandler.java

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionHandler {
    private static final int PERMISSION_REQUEST_CODE = 123;

    // Benötigte Berechtigungen für verschiedene Android Versionen
    private static final String[] PERMISSIONS_ANDROID_12_PLUS = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE   // Für Datei-Lesen

    };

    private static final String[] PERMISSIONS_ANDROID_11_AND_BELOW = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE   // Für Datei-Lesen

    };

    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied(List<String> deniedPermissions);
    }

    private final Activity activity;
    private PermissionCallback callback;

    public PermissionHandler(Activity activity) {
        this.activity = activity;
    }

    public void checkAndRequestPermissions(PermissionCallback callback) {
        this.callback = callback;
        // String Array mit nötigen Berechtigungen
        String[] requiredPermissions = getRequiredPermissions();
        // Leerer Array mit fehlenden Berechtigungen
        List<String> missingPermissions = new ArrayList<>();

        // Prüfe jede benötigte Berechtigung
        for (String permission : requiredPermissions) {
            // Checkt, welche Berechtigungen nicht im Context zu finden sind, fügt diese zu den fehlenden hinzu
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (missingPermissions.isEmpty()) {
            // Alle Berechtigungen bereits vorhanden
            callback.onPermissionsGranted();
        } else {
            // Fordere fehlende Berechtigungen an
            ActivityCompat.requestPermissions(
                    activity,
                    missingPermissions.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PERMISSIONS_ANDROID_12_PLUS;
        } else {
            return PERMISSIONS_ANDROID_11_AND_BELOW;
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            List<String> deniedPermissions = new ArrayList<>();

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }

            if (deniedPermissions.isEmpty()) {
                callback.onPermissionsGranted();
            } else {
                callback.onPermissionsDenied(deniedPermissions);
            }
        }
    }

    // Hilfsmethode zum Prüfen einzelner Berechtigungen
    public static boolean hasPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }
}

