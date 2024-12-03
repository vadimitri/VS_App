package com.example.vs_app;

// MediaManager.java

import android.content.Context;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MediaManager {
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static Context applicationContext;  // Application Context speichern

    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
    }

    private static File getMediaDirectory() {
        return applicationContext.getExternalFilesDir(null);
    }

    public static void storeMedia(Context context, ImageCapture imageCapture, String timestamp) {
        File photoFile = new File(
                getMediaDirectory(),  // Nutze getMediaDirectory statt context direkt
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
                        TransferManager.getInstance().queueForTransfer(photoFile);
                    }

                    @Override
                    public void onError(ImageCaptureException e) {
                        e.printStackTrace();
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
}