package com.example.vs_app;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MediaManager {
    private static final String TAG = "MediaManager";
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static Context applicationContext;
    private static GroupManager groupManager;
    
    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
        groupManager = GroupManager.getInstance();
        createMediaDirectory();
    }

    private static void createMediaDirectory() {
        File mediaDir = getMediaDirectory();
        if (!mediaDir.exists() && !mediaDir.mkdirs()) {
            Log.e(TAG, "Failed to create media directory");
        }
    }

    private static File getMediaDirectory() {
        File baseDir = applicationContext.getExternalFilesDir(null);
        return new File(baseDir, "MomentShare");
    }

    public static void storeMedia(ImageCapture imageCapture, String timestamp) {
        File photoFile = new File(
                getMediaDirectory(),
                "Moment_" + timestamp + ".jpg"
        );

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputFileOptions,
                executor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        Log.d(TAG, "Photo saved: " + photoFile.getPath());
                        
                        // Scan file to make it visible in gallery
                        MediaScannerConnection.scanFile(
                            applicationContext,
                            new String[]{photoFile.getPath()},
                            new String[]{"image/jpeg"},
                            null
                        );

                        // Send to group members
                        try {
                            byte[] photoData = readFileToBytes(photoFile);
                            groupManager.sendPhotoToGroup(photoData);
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to read photo file: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(ImageCaptureException e) {
                        Log.e(TAG, "Photo capture failed: " + e.getMessage());
                    }
                }
        );
    }

    private static byte[] readFileToBytes(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            if (fis.read(bytes) == -1) {
                throw new IOException("Could not read file completely");
            }
        }
        return bytes;
    }

    public static File createReceivedFile(String fileName) {
        return new File(
                getMediaDirectory(),
                "Received_" + System.currentTimeMillis() + "_" + fileName
        );
    }

    public static void cleanup() {
        executor.shutdown();
    }
}