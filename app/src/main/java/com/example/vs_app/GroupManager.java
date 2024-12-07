package com.example.vs_app;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupManager {
    private static GroupManager instance;
    private final Set<BluetoothDevice> groupMembers;
    private Context context;

    private GroupManager() {
        groupMembers = new HashSet<>();
    }

    public static synchronized GroupManager getInstance() {
        if (instance == null) {
            instance = new GroupManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.context = context;
    }

    public void addGroupMember(BluetoothDevice device) {
        if (device != null) {
            groupMembers.add(device);
            notifyGroupUpdate();
        }
    }

    public void removeGroupMember(BluetoothDevice device) {
        if (device != null) {
            groupMembers.remove(device);
            notifyGroupUpdate();
        }
    }

    private void notifyGroupUpdate() {
        if (context != null) {
            GossipController.getInstance(context).onGroupUpdate(new ArrayList<>(groupMembers));
        }
    }

    public List<BluetoothDevice> getGroupMembers() {
        return new ArrayList<>(groupMembers);
    }

    public boolean isInGroup(BluetoothDevice device) {
        return groupMembers.contains(device);
    }

    public void clearGroup() {
        groupMembers.clear();
        notifyGroupUpdate();
    }
}