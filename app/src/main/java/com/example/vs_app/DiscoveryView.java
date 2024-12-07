package com.example.vs_app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private PermissionHandler permissionHandler;

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
            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Device name changed: " + device.getName());
                updateDeviceList();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        permissionHandler = new PermissionHandler(this);
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
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        registerReceiver(discoveryReceiver, filter);

        checkPermissionsAndStartDiscovery();
        
        // Mache das eigene Gerät sichtbar und setze einen Namen
        bluetoothController.setDeviceName("VS_App_" + Build.MODEL);
        bluetoothController.makeDiscoverable();
    }

    private void checkPermissionsAndStartDiscovery() {
        // Überprüfe zuerst Bluetooth
        if (bluetoothController.getBluetoothAdapter() == null) {
            statusTextView.setText("Fehler: Bluetooth nicht verfügbar");
            return;
        }

        if (!bluetoothController.getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        // Überprüfe und fordere Berechtigungen an
        ArrayList<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            Log.d(TAG, "Requesting permissions: " + permissions);
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            startDiscovery();
        }
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
                startDiscovery();
            } else {
                statusTextView.setText("Fehler: Berechtigungen fehlen");
                Toast.makeText(this, "Benötigte Berechtigungen wurden nicht erteilt", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Permissions denied");
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
                statusTextView.setText("Fehler: Bluetooth nicht aktiviert");
                Toast.makeText(this, "Bluetooth muss aktiviert sein", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startDiscovery() {
        statusTextView.setText("Starte Suche...");
        Log.d(TAG, "Starting discovery");
        bluetoothController.startDiscovery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.discovery_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            checkPermissionsAndStartDiscovery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}