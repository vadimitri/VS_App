package com.example.vs_app;

// ErrorHandler.java
import android.content.Context;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.os.Environment;
import android.os.StatFs;

public class ErrorHandler {
    private static final long MIN_STORAGE_SPACE = 50 * 1024 * 1024; // 50MB

    public static void handleBluetoothError(Context context, int errorType) {
        String message;
        String title;
        boolean isCritical = false;

        switch (errorType) {
            case BluetoothAdapter.STATE_OFF:
                title = "Bluetooth deaktiviert";
                message = "Bitte aktivieren Sie Bluetooth, um fortzufahren.";
                break;
            case BluetoothAdapter.ERROR:
                title = "Bluetooth-Fehler";
                message = "Es ist ein Fehler mit Bluetooth aufgetreten. Bitte starten Sie die App neu.";
                isCritical = true;
                break;
            default:
                title = "Verbindungsfehler";
                message = "Es ist ein unerwarteter Bluetooth-Fehler aufgetreten.";
                break;
        }

        showErrorDialog(context, title, message, isCritical);
    }

    public static void handleCameraError(Context context, String errorMessage) {
        String title = "Kamera-Fehler";
        String message = "Fehler beim Zugriff auf die Kamera: " + errorMessage;
        boolean isCritical = true;

        showErrorDialog(context, title, message, isCritical);
    }

    public static void handleStorageError(Context context, int errorType) {
        String title = "Speicher-Fehler";
        String message;
        boolean isCritical = false;

        switch (errorType) {
            case StorageError.INSUFFICIENT_SPACE:
                message = "Nicht genügend Speicherplatz verfügbar.";
                break;
            case StorageError.WRITE_ERROR:
                message = "Fehler beim Speichern der Datei.";
                break;
            case StorageError.READ_ERROR:
                message = "Fehler beim Lesen der Datei.";
                break;
            default:
                message = "Unerwarteter Speicherfehler.";
                break;
        }

        showErrorDialog(context, title, message, isCritical);
    }

    public static void handleTransferError(Context context, String deviceName, String error) {
        String message = "Fehler bei der Übertragung mit " + deviceName + ": " + error;
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private static void showErrorDialog(Context context, String title, String message,
                                        boolean isCritical) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (isCritical) {
                        // Bei kritischen Fehlern zur Hauptansicht zurückkehren
                        ((MainActivity) context).finish();
                    }
                });

        if (!isCritical) {
            builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());
        }

        builder.create().show();
    }

    // Hilfsmethoden zur Fehlerüberprüfung
    public static boolean checkStorageSpace(Context context) {
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long availableSpace = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            return availableSpace > MIN_STORAGE_SPACE;
        } catch (Exception e) {
            return false;
        }
    }
}