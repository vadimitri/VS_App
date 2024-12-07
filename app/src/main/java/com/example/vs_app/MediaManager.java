package com.example.vs_app;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaManager {
    private static final String TAG = "MediaManager";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Context applicationContext;

    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
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
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        Log.d(TAG, "Photo captured: " + photoFile.getPath());
                        // Scan file to make it visible in gallery
                        MediaScannerConnection.scanFile(
                                applicationContext,
                                new String[]{photoFile.getPath()},
                                new String[]{"image/jpeg"},
                                null
                        );
                        // Queue for transfer
                        TransferManager.getInstance().queueForTransfer(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        Log.e(TAG, "Photo capture failed: " + e.getMessage());
                    }
                }
        );
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