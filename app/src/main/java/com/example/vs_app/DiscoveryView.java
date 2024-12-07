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
    private GroupManager groupManager;

    // ... bisheriger Code bleibt gleich ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        permissionHandler = new PermissionHandler(this);
        statusTextView = findViewById(R.id.discovery_status);
        deviceList = findViewById(R.id.device_list);
        deviceList.setLayoutManager(new LinearLayoutManager(this));

        bluetoothController = new BluetoothController(this);
        groupManager = GroupManager.getInstance();
        discoveredDevices = new ArrayList<>();
        adapter = new DeviceListAdapter(this, discoveredDevices);
        deviceList.setAdapter(adapter);

        // ... Rest des onCreate bleibt gleich ...
    }

    public void requestBluetoothPermissions() {
        ArrayList<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissions.add(Manifest.permission.BLUETOOTH);
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
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
        }
    }

    // ... Rest der bisherigen Methoden bleiben gleich ...
}