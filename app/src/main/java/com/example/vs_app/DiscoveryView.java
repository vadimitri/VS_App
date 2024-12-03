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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class DiscoveryView extends AppCompatActivity {


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private BluetoothController bluetoothController;
    private RecyclerView deviceList;
    private ArrayList<BluetoothDevice> discoveredDevices;
    private DeviceListAdapter adapter;


    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !discoveredDevices.contains(device)) {
                    discoveredDevices.add(device);
                    // Update UI
                    updateDeviceList();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        deviceList = findViewById(R.id.device_list);
        deviceList.setLayoutManager(new LinearLayoutManager(this));


        bluetoothController = new BluetoothController(this);
        discoveredDevices = new ArrayList<>();
        adapter = new DeviceListAdapter(this, discoveredDevices);
        deviceList.setAdapter(adapter);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryReceiver, filter);

        checkPermissionsAndStartDiscovery();
    }

//    private void checkPermissionsAndStartDiscovery() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
//                    1);
//        } else {
//            bluetoothController.startDiscovery();
//        }
//    }

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

        // Discovery starten und Status aktualisieren
        TextView status = findViewById(R.id.discovery_status);
        status.setText("Suche nach Geräten...");

        bluetoothController.startDiscovery();
    }

    private void updateDeviceList() {
        runOnUiThread(() -> {
            if (adapter == null) {
                adapter = new DeviceListAdapter(this, discoveredDevices);
                deviceList.setAdapter(adapter);
                deviceList.setLayoutManager(new LinearLayoutManager(this));
            } else {
                adapter.notifyDataSetChanged();
            }
        });    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            }
        }
    }
}