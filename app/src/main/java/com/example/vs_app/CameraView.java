package com.example.vs_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.google.common.util.concurrent.ListenableFuture;

public class CameraView extends AppCompatActivity {
    private PreviewView previewView;
    private Button captureButton;
    private CameraController cameraController;
    private GroupManager groupManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.preview_view);
        captureButton = findViewById(R.id.capture_button);
        
        // Get GroupManager instance
        groupManager = GroupManager.getInstance();
        
        cameraController = new CameraController(this);

        captureButton.setOnClickListener(v -> {
            if (groupManager.getGroupSize() > 0) {
                cameraController.capturePhoto();
            } else {
                Toast.makeText(this, "Keine Geräte in der Gruppe", Toast.LENGTH_SHORT).show();
            }
        });

        startCamera();
    }

    private void startCamera() {
        cameraController.startCamera(previewView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update UI based on group status
        updateCaptureButton();
    }

    private void updateCaptureButton() {
        boolean hasGroupMembers = groupManager.getGroupSize() > 0;
        captureButton.setEnabled(hasGroupMembers);
        captureButton.setText(hasGroupMembers ? "Foto aufnehmen" : "Keine Geräte verbunden");
    }
}