package volpis.com.garadget.services;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.globalclasses.AlertActionModel;
import com.example.globalclasses.PathConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.ui.Toaster;
import volpis.com.garadget.App;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorConfig;
import volpis.com.garadget.models.DoorStatus;
import volpis.com.garadget.models.DoorWearModel;
import volpis.com.garadget.models.NetConfig;
import volpis.com.garadget.models.PendingData;
import volpis.com.garadget.parser.GaradgetParser;
import volpis.com.garadget.screens.MainActivity;
import volpis.com.garadget.utils.FunctionConstants;

import com.example.globalclasses.StatusConstants;

import volpis.com.garadget.utils.Utils;

public class DataLayerListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GaradgetLogs";
    public static GoogleApiClient mGoogleApiClient;
    static ArrayList<Door> mDoors;
    MessageEvent lastMessageEvent;
    boolean isRequestEnded;
    static PendingData pendingData;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mGoogleApiClient != null)
            if (pendingData != null)
                mGoogleApiClient.connect();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * download all doors data from particle and send to wear
     */
    public void getListOfDevices(boolean processMessage) {
        Log.d("AppServiceTest", "APP Service getListOfDevices");
        if (Utils.haveInternet(DataLayerListenerService.this)) {
            isRequestEnded = false;
            List<ParticleDevice> mDevices = null;
            ArrayList<Door> doors = new ArrayList<Door>();

            try {
                mDevices = ParticleCloudSDK.getCloud().getDevices();
                for (ParticleDevice device : mDevices) {
                    Door door = new Door();
                    if (device.isConnected()) {
                        try {
                            Log.d("AppServiceTest", "device connected");
                            String doorConfigString = device.getStringVariable("doorConfig");
                            String doorStatusString = device.getStringVariable("doorStatus");
                            String netConfigString = device.getStringVariable("netConfig");
                            DoorConfig doorConfig = GaradgetParser.parse(DataLayerListenerService.this, doorConfigString, DoorConfig.class);
                            DoorStatus doorStatus = GaradgetParser.parse(DataLayerListenerService.this, doorStatusString, DoorStatus.class);
                            NetConfig netConfig = GaradgetParser.parse(DataLayerListenerService.this, netConfigString, NetConfig.class);
                            door = new Door(doorConfig, doorStatus, netConfig, device);
                            isRequestEnded = true;
                        } catch (ParticleDevice.VariableDoesNotExistException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    door.setDevice(device);
                    doors.add(door);
                }
                mDoors = doors;
                if (!Utils.isServiceRunning(DataLayerListenerService.this, EventSubscriberService.class)) {
                    Intent serviceIntent = new Intent(DataLayerListenerService.this, EventSubscriberService.class);
                    serviceIntent.putExtra("doors", doors);
                    startService(serviceIntent);
                }
                if (processMessage)
                    processMessage();
                else
                    sendDoorsToWear(doors, false);
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(DataLayerListenerService.this, "No internet", Toast.LENGTH_SHORT).show();
        }
    }

    public static void setDoors(ArrayList<Door> doors) {
        mDoors = doors;
    }

    /**
     * open close door request
     */
    public void changeDoorStatus(final Door door, final ParticleDevice device, final String newStatus) {
        Async.executeAsync(ParticleCloud.get(DataLayerListenerService.this), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                ArrayList<String> params = new ArrayList<String>();
                params.add(newStatus);
                try {
                    device.callFunction(FunctionConstants.FUNCTION_SET_STATE, params);
                } catch (ParticleDevice.FunctionDoesNotExistException | ParticleCloudException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return -1;
            }

            @Override
            public void onSuccess(Object value) {
                door.getDoorStatus().setStatus(newStatus);
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                Toaster.l(DataLayerListenerService.this, e.getBestMessage());
                e.printStackTrace();
                Log.d("onFailure", e.getBestMessage());
            }
        });
    }

    public static boolean sendDoorsToWear(ArrayList<Door> doors, boolean fromRefresh) {
        ArrayList<DataMap> doorsToSend = new ArrayList<>();
        for (Door door : doors) {
            DataMap dataMap = new DataMap();
            String id = door.getDevice().getID();
            String name = door.getName();

            DoorConfig doorConfig = door.getDoorConfig();
            int statusAlerts = 0;
            String version = null;
            int doorMovingTime = 0;
            if (doorConfig != null) {
                statusAlerts = doorConfig.getStatusAlerts();
                version = doorConfig.getVersion();
                doorMovingTime = doorConfig.getDoorMovingTime();
            }
            boolean isConnected = false;
            long lastContact = 0;
            if (door.getDevice() != null) {
                lastContact = door.getDevice().getLastHeard().getTime();
                isConnected = door.getDevice().isConnected();
            }

            NetConfig netConfig = door.getNetConfig();
            String wifiSSID = null;
            String IP = null;
            String gateway = null;
            String ipMask = null;
            String MAC = null;
            if (netConfig != null) {
                wifiSSID = netConfig.getSsid();
                IP = netConfig.getIpAddress();
                gateway = netConfig.getGateway();
                ipMask = netConfig.getSubnet();
                MAC = netConfig.getMacAddress();
            }

            DoorStatus doorStatus = door.getDoorStatus();
            boolean isOpen = false;
            String doorStatusTime = null;
            int signalStrength = 0;
            String signalStrengthString = null;
            if (doorStatus != null) {
                signalStrengthString = doorStatus.getSignalString();
                isOpen = doorStatus.getStatus().equals(StatusConstants.OPEN) || doorStatus.getStatus().equals(StatusConstants.OPENING);
                signalStrength = doorStatus.getSignalStrength();
                doorStatusTime = doorStatus.getTime();
            }

            DoorWearModel doorWearModel = new DoorWearModel(id, name, isOpen, isConnected, doorStatusTime, signalStrength, statusAlerts, lastContact, version, wifiSSID, signalStrengthString, IP, gateway, ipMask, MAC, doorMovingTime);
            dataMap.putBoolean("fromRefresh", fromRefresh);
            doorsToSend.add(doorWearModel.putToDataMap(dataMap));
        }

        return sendDataArrayToWear(PathConstants.GET_DEVICES_PATH, PathConstants.DOORS_KEY, doorsToSend);
    }

    /**
     * send new door status to wear (called: after door click on phone, after new status received)
     */
    public static boolean sendDoorStatusToWear(Door door) {
        DataMap dataMap = new DataMap();
        String id = door.getDevice().getID();
        String name = door.getName();
        boolean isOpen = (door.getDoorStatus().getStatus().equals(StatusConstants.OPEN) || door.getDoorStatus().getStatus().equals(StatusConstants.OPENING));
        int signalStrength = door.getDoorStatus().getSignalStrength();

        DoorConfig doorConfig = door.getDoorConfig();
        int statusAlerts = doorConfig.getStatusAlerts();
        long lastContact = door.getDevice().getLastHeard().getTime();
        String version = doorConfig.getVersion();
        int doorMovingTime = doorConfig.getDoorMovingTime();

        NetConfig netConfig = door.getNetConfig();
        String wifiSSID = netConfig.getSsid();
        String IP = netConfig.getIpAddress();
        String gateway = netConfig.getGateway();
        String ipMask = netConfig.getSubnet();
        String MAC = netConfig.getMacAddress();
        boolean isConnected = door.getDevice().isConnected();

        DoorStatus doorStatus = door.getDoorStatus();
        String signalStrengthString = doorStatus.getSignalString();
        String doorStatusTime = doorStatus.getTime();

        DoorWearModel doorWearModel = new DoorWearModel(id, name, isOpen, isConnected, doorStatusTime, signalStrength, statusAlerts, lastContact, version, wifiSSID, signalStrengthString, IP, gateway, ipMask, MAC, doorMovingTime);
        doorWearModel.putToDataMap(dataMap);
        return sendDataToWear(PathConstants.CHANGE_DOOR_STATUS_PATH, PathConstants.CHANGE_DOOR_KEY, dataMap);
    }

    public static boolean sendDoorDataToWear(Door door) {
        DataMap dataMap = new DataMap();
        String id = door.getDevice().getID();
        String name = door.getName();
        boolean isOpen = door.getDoorStatus().getStatus().equals(StatusConstants.OPEN) || door.getDoorStatus().getStatus().equals(StatusConstants.OPENING);
        int signalStrength = door.getDoorStatus().getSignalStrength();

        DoorConfig doorConfig = door.getDoorConfig();
        int statusAlerts = doorConfig.getStatusAlerts();
        long lastContact = door.getDevice().getLastHeard().getTime();
        String version = doorConfig.getVersion();
        int doorMovingTime = doorConfig.getDoorMovingTime();

        NetConfig netConfig = door.getNetConfig();
        String wifiSSID = netConfig.getSsid();
        String IP = netConfig.getIpAddress();
        String gateway = netConfig.getGateway();
        String ipMask = netConfig.getSubnet();
        String MAC = netConfig.getMacAddress();
        boolean isConnected = door.getDevice().isConnected();

        DoorStatus doorStatus = door.getDoorStatus();
        String signalStrengthString = doorStatus.getSignalString();
        String doorStatusTime = doorStatus.getTime();

        DoorWearModel doorWearModel = new DoorWearModel(id, name, isOpen, isConnected, doorStatusTime, signalStrength, statusAlerts, lastContact, version, wifiSSID, signalStrengthString, IP, gateway, ipMask, MAC, doorMovingTime);
        doorWearModel.putToDataMap(dataMap);
        return sendDataToWear(PathConstants.DOOR_DATA_CHANGE_PATH, PathConstants.DOOR_DATA_CHANGE_KEY, dataMap);
    }

    public static void sendErrorToWear(int errorCode, String error) {
        Log.d("AppServiceTest", "sendErrorToWear");

        DataMap dataMap = new DataMap();
        dataMap.putInt("errorCode", errorCode);
        dataMap.putString("error", error);
        sendDataToWear(PathConstants.ERROR_PATH, PathConstants.ERROR_KEY, dataMap);
    }

    /**
     * send data to wear
     * add timestamp to data so data will be received on wear even if not changed
     */
    public static boolean sendDataToWear(String path, String key, DataMap dataMap) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            // Log.d("AppServiceTest", " APP Service " + path + " " + mGoogleApiClient.isConnected() + " " + mGoogleApiClient.isConnecting());
            dataMap.putLong("timestamp", System.currentTimeMillis());
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
            putDataMapReq.getDataMap().putDataMap(key, dataMap);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            putDataReq.setUrgent();
            Log.d("sendDataToWear", "sendDataToWear");
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
            pendingResult.setResultCallback(new ResultCallbacks<DataApi.DataItemResult>() {
                @Override
                public void onSuccess(@NonNull DataApi.DataItemResult dataItemResult) {
                    Log.d("AppServiceTest", "sendDataToWear SUCCESS");
                }

                @Override
                public void onFailure(@NonNull Status status) {
                    Log.d("AppServiceTest", "sendDataToWear onFailure");

                }
            });
            return true;
        } else {
            pendingData = new PendingData();
            pendingData.dataMap = dataMap;
            pendingData.path = path;
            pendingData.key = key;
            return false;
        }
    }

    /**
     * send data array to wear
     * add timestamp to data so data will be received on wear even if not changed
     */
    public static boolean sendDataArrayToWear(String path, String key, ArrayList<DataMap> dataMapArray) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.d("AppServiceTest", " APP Service " + path + " " + mGoogleApiClient.isConnected() + " " + mGoogleApiClient.isConnecting());
            for (DataMap dataMap : dataMapArray)
                dataMap.putLong("timestamp", System.currentTimeMillis());
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
            putDataMapReq.getDataMap().putDataMapArrayList(key, dataMapArray);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            putDataReq.setUrgent();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
            pendingResult.setResultCallback(new ResultCallbacks<DataApi.DataItemResult>() {
                @Override
                public void onSuccess(@NonNull DataApi.DataItemResult dataItemResult) {
                    Log.d("AppServiceTest", "sendDataArrayToWear SUCCESS");
                }

                @Override
                public void onFailure(@NonNull Status status) {
                    Log.d("AppServiceTest", "sendDataArrayToWear onFailure");

                }
            });
            return true;
        } else {
            pendingData = new PendingData();
            pendingData.dataMapArray = dataMapArray;
            pendingData.path = path;
            pendingData.key = key;
            return false;
        }
    }

    public void updateAlertConfig(final String deviceId, final String newConfig) {
        if (Utils.haveInternet(DataLayerListenerService.this)) {
            Async.executeAsync(ParticleCloud.get(DataLayerListenerService.this), new Async.ApiWork<ParticleCloud, Object>() {
                @Override
                public Object callApi(ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                    ArrayList<String> params = new ArrayList<String>();
                    params.add(newConfig);
                    try {
                        ParticleCloudSDK.getCloud().getDevice(deviceId).callFunction(FunctionConstants.FUNCTION_SET_CONFIG, params);
                    } catch (ParticleDevice.FunctionDoesNotExistException | IOException e) {
                        e.printStackTrace();
                    } catch (ParticleCloudException e) {
                        e.printStackTrace();
                    }
                    return -1;
                }

                @Override
                public void onSuccess(Object value) {
                    Log.d("onSuccess", "onSuccess");

                }

                @Override
                public void onFailure(ParticleCloudException e) {
                    e.printStackTrace();
                    Log.d("onFailure", e.getBestMessage());
                }
            });
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("AppServiceTest", "onConnected");
        processMessage();
        if (pendingData != null) {
            if (pendingData.dataMap != null)
                sendDataToWear(pendingData.path, pendingData.key, pendingData.dataMap);
            if (pendingData.dataMapArray != null)
                sendDataArrayToWear(pendingData.path, pendingData.key, pendingData.dataMapArray);
            pendingData = null;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, " APP Service onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, " APP Service onConnectionFailed");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("AppServiceTest", "onMessageReceived");
        lastMessageEvent = messageEvent;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            processMessage();
        else {
            mGoogleApiClient.connect();
        }
    }


    private void processMessage() {
        MessageEvent messageEvent = lastMessageEvent;
        if (messageEvent != null && messageEvent.getPath() != null) {
            Log.d("AppServiceTest", " APP Service onMessageReceived " + messageEvent.getPath());
            if (messageEvent.getPath().equals(PathConstants.GET_DEVICES_PATH)) {
                getListOfDevices(false);
            } else if (messageEvent.getPath().equals(PathConstants.CHANGE_DOOR_STATUS_PATH)) {
                //find door by id and change door status
                String doorStatusData = new String(messageEvent.getData());
                String doorId = doorStatusData.split("\\|")[0];
                String doorNewStatus = doorStatusData.split("\\|")[1];
                if (mDoors != null) {
                    for (Door door : mDoors) {
                        if (door.getDevice().getID().equals(doorId)) {
                            Activity currentActivity = App.getInstance().getCurrentActivity();
                            if (currentActivity instanceof MainActivity) {
                                ((MainActivity) currentActivity).getDoorsFragment().startAnimation(doorId, doorNewStatus);
                            }
                            changeDoorStatus(door, door.getDevice(), doorNewStatus);
                            break;
                        }
                    }
                } else {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            getListOfDevices(true);
                        }
                    };
                    thread.start();
                }
            } else if (messageEvent.getPath().equals(PathConstants.LOGIN_STATUS_PATH)) {
                sendIsLoggedStatus(false);

            } else if (messageEvent.getPath().equals(PathConstants.UPDATE_ALERT)) {
                String alertConfigJson = new String(messageEvent.getData());
                Gson gson = new Gson();
                AlertActionModel alertActionModel = gson.fromJson(alertConfigJson, AlertActionModel.class);
                updateAlertConfig(alertActionModel.getDoorId(), "aev=" + alertActionModel.getAlertStatus());
            }
        }
    }

    public static boolean sendIsLoggedStatus(boolean refill) {
        boolean isLoggedIn = ParticleCloudSDK.getCloud().isLoggedIn();
        DataMap dataMap = new DataMap();
        dataMap.putBoolean("login_status", isLoggedIn);
        dataMap.putBoolean("refill", refill);
        Log.d("isLogged", isLoggedIn + "");
        return sendDataToWear(PathConstants.LOGIN_STATUS_PATH, PathConstants.LOGIN_STATUS_KEY, dataMap);
    }

    @Override
    public void onDestroy() {
        Log.d("AppServiceTest", "onDestroy  " + isRequestEnded);
        super.onDestroy();
    }
}