package com.example.vs_app;

// GroupManager.java

import android.bluetooth.BluetoothDevice;
import java.util.ArrayList;
import java.util.List;

public class GroupManager {
    private List<BluetoothDevice> groupMembers;
    private static GroupManager instance;

    public GroupManager() {
        groupMembers = new ArrayList<>();
    }

    public static synchronized GroupManager getInstance() {
        if (instance == null) {
            instance = new GroupManager();
        }
        return instance;
    }

    public void addMember(BluetoothDevice device) {
        if (!groupMembers.contains(device)) {
            groupMembers.add(device);
            notifyGroupUpdate();
        }
    }

    public void removeMember(BluetoothDevice device) {
        if (groupMembers.remove(device)) {
            notifyGroupUpdate();
        }
    }

    public List<BluetoothDevice> getGroupMembers() {
        return new ArrayList<>(groupMembers);
    }

    private void notifyGroupUpdate() {
        // Notify observers about group changes
        GossipController.getInstance().onGroupUpdate(groupMembers);
    }
}
