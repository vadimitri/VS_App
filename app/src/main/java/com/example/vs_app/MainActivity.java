package com.example.vs_app;

// Beispiel für die Verwendung in einer Activity:
// MainActivity.java
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vs_app.controller.BluetoothController;
import com.example.vs_app.controller.GossipController;
import com.example.vs_app.handler.ErrorHandler;
import com.example.vs_app.handler.PermissionHandler;
import com.example.vs_app.manager.MediaManager;
import com.example.vs_app.view.CameraView;
import com.example.vs_app.view.DiscoveryView;
import com.example.vs_app.view.GroupView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PermissionHandler permissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaManager.init(this);

        permissionHandler = new PermissionHandler(this);
        checkPermissions();

        // Buttons finden und Listener setzen
        Button cameraButton = findViewById(R.id.camera_button);
        Button groupButton = findViewById(R.id.group_button);
        Button discoveryButton = findViewById(R.id.discovery_button);

        cameraButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraView.class);
            startActivity(intent);
        });

        groupButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GroupView.class);
            startActivity(intent);
        });

        discoveryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DiscoveryView.class);
            startActivity(intent);
        });
    }

    private void checkPermissions() {
        permissionHandler.checkAndRequestPermissions(new PermissionHandler.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                // Alle Berechtigungen wurden gewährt - App kann starten
                startApp();
            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                // Einige Berechtigungen wurden verweigert
                handleDeniedPermissions(deniedPermissions);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startApp() {
        // Hier die eigentliche App-Logik starten
        Toast.makeText(this, "Alle Berechtigungen gewährt - App startet",
                Toast.LENGTH_SHORT).show();
        BluetoothController bluetoothController = new BluetoothController(this);
        GossipController gossipController = new GossipController(this);

    }

    private void handleDeniedPermissions(List<String> deniedPermissions) {
        // Informiere den Benutzer über fehlende Berechtigungen
        StringBuilder message = new StringBuilder("Folgende Berechtigungen fehlen:\n");
        for (String permission : deniedPermissions) {
            message.append("- ").append(getReadablePermissionName(permission)).append("\n");
        }
        Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();
        // Optional: App beenden oder eingeschränkte Funktionalität anbieten
    }

    private String getReadablePermissionName(String permission) {
        switch (permission) {
            case android.Manifest.permission.CAMERA:
                return "Kamera";
            case android.Manifest.permission.BLUETOOTH_SCAN:
            case android.Manifest.permission.BLUETOOTH:
                return "Bluetooth";
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Dateizugriff";
            case android.Manifest.permission.ACCESS_FINE_LOCATION:
                return "Standort";
            default:
                return permission;
        }
    }

}