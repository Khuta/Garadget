package volpis.com.garadget.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.particle.android.sdk.cloud.ParticleDevice;

public class Door implements Parcelable {
    DoorConfig doorConfig;
    DoorStatus doorStatus;
    NetConfig netConfig;
    ParticleDevice device;

    public Door() {
    }

    public Door(Door door) {
        this.doorConfig = door.doorConfig;
        this.doorStatus = door.doorStatus;
        this.netConfig = door.netConfig;
        this.device = door.device;
    }

    public Door(DoorConfig doorConfig, DoorStatus doorStatus, NetConfig netConfig, ParticleDevice device) {
        this.doorConfig = doorConfig;
        this.doorStatus = doorStatus;
        this.netConfig = netConfig;
        this.device = device;
    }

    public ParticleDevice getDevice() {
        return device;
    }

    public void setDevice(ParticleDevice device) {
        this.device = device;
    }

    public DoorConfig getDoorConfig() {
        return doorConfig;
    }

    public void setDoorConfig(DoorConfig doorConfig) {
        this.doorConfig = doorConfig;
    }

    public DoorStatus getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(DoorStatus doorStatus) {
        this.doorStatus = doorStatus;
    }

    public NetConfig getNetConfig() {
        return netConfig;
    }

    public void setNetConfig(NetConfig netConfig) {
        this.netConfig = netConfig;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.doorConfig, flags);
        dest.writeParcelable(this.doorStatus, flags);
        dest.writeParcelable(this.netConfig, flags);
        dest.writeParcelable(this.device, flags);
    }

    protected Door(Parcel in) {
        this.doorConfig = in.readParcelable(DoorConfig.class.getClassLoader());
        this.doorStatus = in.readParcelable(DoorStatus.class.getClassLoader());
        this.netConfig = in.readParcelable(NetConfig.class.getClassLoader());
        this.device = in.readParcelable(ParticleDevice.class.getClassLoader());
    }

    public static final Creator<Door> CREATOR = new Creator<Door>() {
        @Override
        public Door createFromParcel(Parcel source) {
            return new Door(source);
        }

        @Override
        public Door[] newArray(int size) {
            return new Door[size];
        }
    };

    public String getName(){
        return  getDoorConfig() != null ? getDoorConfig().getName() : getDevice().getName();
    }

}
