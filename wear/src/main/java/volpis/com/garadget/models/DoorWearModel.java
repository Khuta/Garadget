package volpis.com.garadget.models;

import com.google.android.gms.wearable.DataMap;

import java.io.Serializable;

public class DoorWearModel implements Serializable {
    private String mDoorId;
    private String mDoorTitle;
    private boolean mIsOpened;
    private boolean mIsConnected;
    private String mDoorStatusTime;
    private int mSignalStrength;
    private int mStatusAlerts;
    private long mLastContactMillis;
    private String mVersion;
    private String mWifiSSID;
    private String mSignalStrengthString;
    private String mIP;
    private String mGateway;
    private String mIpMask;
    private String mMAC;
    private int mDoorMovingTime;
    long statusChangeTime;

    public DoorWearModel(String doorId, String doorTitle, boolean isOpened, boolean isConnected, String doorStatusTime, int signalStrength, int statusAlerts, long lastContactMillis, String version, String wifiSSID, String signalStrengthString, String IP, String gateway, String ipMask, String MAC, int doorMovingTime) {
        mDoorId = doorId;
        mDoorTitle = doorTitle;
        mIsOpened = isOpened;
        mIsConnected = isConnected;
        mDoorStatusTime = doorStatusTime;
        mSignalStrength = signalStrength;
        mStatusAlerts = statusAlerts;
        mLastContactMillis = lastContactMillis;
        mVersion = version;
        mWifiSSID = wifiSSID;
        mSignalStrengthString = signalStrengthString;
        mIP = IP;
        mGateway = gateway;
        mIpMask = ipMask;
        mMAC = MAC;
        mDoorMovingTime = doorMovingTime;
    }

    /**
     *
     * fill data except statusChangeTime
     */
    public void setData(DoorWearModel doorWearModel) {
        mDoorId = doorWearModel.getDoorId();
        mDoorTitle = doorWearModel.getDoorTitle();
        mIsOpened = doorWearModel.isOpened();
        mIsConnected = doorWearModel.isConnected();
        mDoorStatusTime = doorWearModel.getDoorStatusTime();
        mSignalStrength = doorWearModel.getSignalStrength();
        mStatusAlerts = doorWearModel.getStatusAlerts();
        mLastContactMillis = doorWearModel.getLastContactMillis();
        mVersion = doorWearModel.getVersion();
        mWifiSSID = doorWearModel.getWifiSSID();
        mSignalStrengthString = doorWearModel.getSignalStrengthString();
        mIP = doorWearModel.getIP();
        mGateway = doorWearModel.getGateway();
        mIpMask = doorWearModel.getIpMask();
        mMAC = doorWearModel.getMAC();
        mDoorMovingTime = doorWearModel.getDoorMovingTime();
    }

    public String getDoorId() {
        return mDoorId;
    }

    public String getDoorTitle() {
        return mDoorTitle;
    }


    public DoorWearModel(DataMap map) {
        this(map.getString("doorId"),
                map.getString("doorTitle"),
                map.getBoolean("isOpened"),
                map.getBoolean("isConnected"),
                map.getString("doorStatusTime"),
                map.getInt("signalStrength"),
                map.getInt("statusAlerts"),
                map.getLong("lastContactMillis"),
                map.getString("version"),
                map.getString("wifiSSID"),
                map.getString("signalStrengthString"),
                map.getString("IP"),
                map.getString("gateway"),
                map.getString("ipMask"),
                map.getString("MAC"),
                map.getInt("doorMovingTime")
        );
    }

    public DataMap putToDataMap(DataMap map) {
        map.putString("doorId", mDoorId);
        map.putString("doorTitle", mDoorTitle);
        map.putBoolean("isOpened", mIsOpened);
        map.putBoolean("isConnected", mIsConnected);
        map.putString("doorStatusTime", mDoorStatusTime);
        map.putInt("signalStrength", mSignalStrength);
        map.putInt("statusAlerts", mStatusAlerts);
        map.putLong("lastContactMillis", mLastContactMillis);
        map.putString("version", mVersion);
        map.putString("wifiSSID", mWifiSSID);
        map.putString("signalStrengthString", mSignalStrengthString);
        map.putString("IP", mIP);
        map.putString("gateway", mGateway);
        map.putString("ipMask", mIpMask);
        map.putString("MAC", mMAC);
        map.putInt("doorMovingTime", mDoorMovingTime);
        return map;
    }

    public int getStatusAlerts() {
        return mStatusAlerts;
    }

    public long getLastContactMillis() {
        return mLastContactMillis;
    }

    public String getVersion() {
        return mVersion;
    }

    public String getWifiSSID() {
        return mWifiSSID;
    }

    public String getSignalStrengthString() {
        return mSignalStrengthString;
    }

    public String getIP() {
        return mIP;
    }

    public String getGateway() {
        return mGateway;
    }

    public String getIpMask() {
        return mIpMask;
    }

    public String getMAC() {
        return mMAC;
    }

    public int getDoorMovingTime() {
        return mDoorMovingTime;
    }

    public void setOpened(boolean opened) {
        mIsOpened = opened;
    }

    public void setSignalStrength(int signalStrength) {
        mSignalStrength = signalStrength;
    }

    public boolean isOpened() {
        return mIsOpened;
    }

    public int getSignalStrength() {
        return mSignalStrength;
    }

    public String getDoorStatusTime() {
        return mDoorStatusTime;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public long getStatusChangeTime() {
        return statusChangeTime;
    }

    public void setStatusChangeTime(long statusChangeTime) {
        this.statusChangeTime = statusChangeTime;
    }
}
