package com.example.vs_app;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vs_app.manager.GroupManager;
import com.example.vs_app.view.DiscoveryView;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {
    private final List<BluetoothDevice> devices;
    private final Context context;
    private final GroupManager groupManager;

    public DeviceListAdapter(Context context, List<BluetoothDevice> devices) {
        this.context = context;
        this.devices = devices;
        this.groupManager = GroupManager.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        
        // Get device name before lambda
        final String finalDeviceName;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            String name = device.getName();
            finalDeviceName = (name == null || name.isEmpty()) ? "Unbekanntes Gerät" : name;
        } else {
            finalDeviceName = "Berechtigung fehlt";
            // Request permission if missing
            if (context instanceof DiscoveryView) {
                ((DiscoveryView) context).requestBluetoothPermissions();
            }
        }

        // Show name and MAC address
        holder.text1.setText(finalDeviceName);
        holder.text2.setText(device.getAddress());

        // Add click listener
        holder.itemView.setOnClickListener(v -> {
            if (finalDeviceName.equals("Berechtigung fehlt")) {
                if (context instanceof DiscoveryView) {
                    ((DiscoveryView) context).requestBluetoothPermissions();
                }
                return;
            }

            if (finalDeviceName.startsWith("VS_App")) {
                if (groupManager != null) {
                    groupManager.addDeviceToGroup(device);
                    Toast.makeText(context, "Gerät zur Gruppe hinzugefügt: " + finalDeviceName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Fehler: GroupManager nicht initialisiert", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Nur VS_App Geräte können hinzugefügt werden", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView text1;
        public TextView text2;

        public ViewHolder(View view) {
            super(view);
            text1 = view.findViewById(android.R.id.text1);
            text2 = view.findViewById(android.R.id.text2);
        }
    }
}