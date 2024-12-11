package com.example.vs_app.handler;

// ExceptionHandler.java
import android.content.Context;
import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.example.vs_app.BuildConfig;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "ExceptionHandler";
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    public ExceptionHandler(Context context) {
        this.context = context;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            // Stack trace in String umwandeln
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            String stackTrace = sw.toString();

            // Log-Eintrag erstellen
            Log.e(TAG, "Unbehandelte Exception: " + stackTrace);

            // Bei Debug-Build kompletten Stacktrace anzeigen
            if (BuildConfig.DEBUG) {
                ErrorHandler.handleCameraError(context, stackTrace);
            } else {
                // Bei Release-Build benutzerfreundliche Nachricht anzeigen
                ErrorHandler.handleCameraError(context,
                        "Ein unerwarteter Fehler ist aufgetreten.");
            }
        } catch (Exception e) {
            // Wenn die eigene Fehlerbehandlung fehlschlägt,
            // zum Standard-Handler zurückfallen
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        }
    }
}