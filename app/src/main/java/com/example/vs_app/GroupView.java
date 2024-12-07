package com.example.vs_app;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;
import java.util.List;

public class GroupView extends AppCompatActivity {
    private ListView membersList;
    private GroupManager groupManager;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        membersList = findViewById(R.id.members_list);
        groupManager = GroupManager.getInstance();

        // Erstelle den Adapter für die ListView
        adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1);
        membersList.setAdapter(adapter);

        updateMembersList();
    }

    private void updateMembersList() {
        List<BluetoothDevice> members = groupManager.getGroupDevices(); // Korrigierter Methodenname
        adapter.clear();
        
        if (members.isEmpty()) {
            adapter.add("Keine Geräte in der Gruppe");
        } else {
            for (BluetoothDevice device : members) {
                String deviceName = device.getName();
                if (deviceName == null || deviceName.isEmpty()) {
                    deviceName = device.getAddress();
                }
                adapter.add(deviceName);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMembersList(); // Liste aktualisieren wenn Activity wieder sichtbar wird
    }
}