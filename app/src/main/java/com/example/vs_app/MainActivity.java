package com.example.vs_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PermissionHandler permissionHandler;
    private GroupManager groupManager;
    private TransferManager transferManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize managers
        MediaManager.init(this);
        groupManager = GroupManager.getInstance();
        groupManager.initialize(this);
        transferManager = TransferManager.getInstance();
        transferManager.initialize(this);

        permissionHandler = new PermissionHandler(this);
        checkPermissions();

        // Initialize UI
        setupButtons();
    }

    private void setupButtons() {
        Button cameraButton = findViewById(R.id.camera_button);
        Button groupButton = findViewById(R.id.group_button);
        Button discoveryButton = findViewById(R.id.discovery_button);

        cameraButton.setOnClickListener(v -> {
            if (groupManager.getGroupSize() > 0) {
                startActivity(new Intent(this, CameraView.class));
            } else {
                Toast.makeText(this, "Fügen Sie zuerst Geräte zur Gruppe hinzu", Toast.LENGTH_SHORT).show();
            }
        });

        groupButton.setOnClickListener(v -> 
            startActivity(new Intent(this, GroupView.class)));

        discoveryButton.setOnClickListener(v -> 
            startActivity(new Intent(this, DiscoveryView.class)));
    }

    private void checkPermissions() {
        permissionHandler.checkAndRequestPermissions(new PermissionHandler.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                startApp();
            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                handleDeniedPermissions(deniedPermissions);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        groupManager.cleanup();
        transferManager.cleanup();
    }

    // ... Rest der Klasse bleibt gleich
}