package com.example.vs_app;

// GroupView.java
package com.example.momentshare;

import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothDevice;
import java.util.List;

public class GroupView extends AppCompatActivity {
    private ListView membersList;
    private GroupManager groupManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        membersList = findViewById(R.id.members_list);
        groupManager = GroupManager.getInstance();

        updateMembersList();
    }

    private void updateMembersList() {
        List<BluetoothDevice> members = groupManager.getGroupMembers();
        // Update ListView with members
    }
}