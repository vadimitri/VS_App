// CameraView.java
package com.example.vs_app;


import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.widget.Button;
import com.google.common.util.concurrent.ListenableFuture;

public class CameraView extends AppCompatActivity {
    private PreviewView previewView;
    private Button captureButton;
    private CameraController cameraController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.preview_view);
        captureButton = findViewById(R.id.capture_button);

        cameraController = new CameraController(this);

        captureButton.setOnClickListener(v -> cameraController.capturePhoto());

        startCamera();
    }

    private void startCamera() {
        cameraController.startCamera(previewView);
    }
}