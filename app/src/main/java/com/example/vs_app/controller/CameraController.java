package com.example.vs_app.controller;

import android.content.Context;
import android.util.Log;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.vs_app.manager.MediaManager;
import com.google.common.util.concurrent.ListenableFuture;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraController {
    private static final String TAG = "CameraController";
    private final Context context;
    private ImageCapture imageCapture;

    public CameraController(Context context) {
        this.context = context;
    }

    public void startCamera(PreviewView previewView) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        (LifecycleOwner) context,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException | IllegalStateException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Interrupted while starting camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void capturePhoto() {
        if (imageCapture == null) {
            Log.e(TAG, "Cannot capture photo - camera not initialized");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis());

        // Der context Parameter wird nicht mehr übergeben, da MediaManager bereits initialisiert wurde
        MediaManager.storeMedia(imageCapture, timestamp);
    }
}