package com.example.globalclasses;


public class AlertActionModel {
    private String mDoorId;
    private int mAlertStatus;

    public AlertActionModel(String doorId, int alertStatus) {
        mDoorId = doorId;
        mAlertStatus = alertStatus;
    }

    public String getDoorId() {
        return mDoorId;
    }

    public void setDoorId(String doorId) {
        mDoorId = doorId;
    }

    public int getAlertStatus() {
        return mAlertStatus;
    }

    public void setAlertStatus(int alertStatus) {
        mAlertStatus = alertStatus;
    }
}
