package com.example.vs_app;

// DiscoveryView.java

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class DiscoveryView extends AppCompatActivity {
    private static final String TAG = "DiscoveryView";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private BluetoothController bluetoothController;
    private RecyclerView deviceList;
    private ArrayList<BluetoothDevice> discoveredDevices;
    private DeviceListAdapter adapter;
    private TextView statusTextView;

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !discoveredDevices.contains(device)) {
                    Log.d(TAG, "Found device: " + device.getName() + " [" + device.getAddress() + "]");
                    discoveredDevices.add(device);
                    updateDeviceList();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Discovery started");
                statusTextView.setText("Suche nach Geräten...");
                discoveredDevices.clear();
                updateDeviceList();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery finished");
                statusTextView.setText("Suche beendet - " + discoveredDevices.size() + " Geräte gefunden");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        statusTextView = findViewById(R.id.discovery_status);
        deviceList = findViewById(R.id.device_list);
        deviceList.setLayoutManager(new LinearLayoutManager(this));

        bluetoothController = new BluetoothController(this);
        discoveredDevices = new ArrayList<>();
        adapter = new DeviceListAdapter(this, discoveredDevices);
        deviceList.setAdapter(adapter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryReceiver, filter);

        checkPermissionsAndStartDiscovery();
    }

    private void checkPermissionsAndStartDiscovery() {
        // Bluetooth aktiviert?
        if (bluetoothController.getBluetoothAdapter() == null || !bluetoothController.getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        // Berechtigungen prüfen
        String[] permissions = new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            return;
        }

        // Discovery starten
        statusTextView.setText("Starte Suche...");
        bluetoothController.startDiscovery();
    }

    private void updateDeviceList() {
        runOnUiThread(() -> {
            Log.d(TAG, "Updating device list with " + discoveredDevices.size() + " devices");
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothController.stopDiscovery();
        unregisterReceiver(discoveryReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                checkPermissionsAndStartDiscovery();
            } else {
                Toast.makeText(this, "Benötigte Berechtigungen wurden nicht erteilt", Toast.LENGTH_SHORT).show();
                statusTextView.setText("Fehler: Berechtigungen fehlen");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                checkPermissionsAndStartDiscovery();
            } else {
                Toast.makeText(this, "Bluetooth muss aktiviert sein", Toast.LENGTH_SHORT).show();
                statusTextView.setText("Fehler: Bluetooth nicht aktiviert");
            }
        }
    }
}