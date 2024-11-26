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

    public static void storeMedia(Context context, ImageCapture imageCapture, String timestamp) {
        File photoFile = new File(
                context.getExternalFilesDir(null),
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
                        // Notify TransferManager about new media
                        TransferManager.getInstance().queueForTransfer(photoFile);
                    }

                    @Override
                    public void onError(ImageCaptureException e) {
                        e.printStackTrace();
                    }
                }
        );
    }
}