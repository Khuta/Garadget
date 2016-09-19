package volpis.com.garadget.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.globalclasses.StatusConstants;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import volpis.com.garadget.models.DataPayload;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorConfig;
import volpis.com.garadget.models.DoorStatus;
import volpis.com.garadget.models.NetConfig;
import volpis.com.garadget.parser.GaradgetParser;
import volpis.com.garadget.utils.EventsConstants;
import volpis.com.garadget.utils.Utils;

public class EventSubscriberService extends Service {
    private static long mSubscriptionId;
    private static ArrayList<Door> mDoors;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("EventSubscriberService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("EventSubscriberService", "onStartCommand");
        super.onStartCommand(intent, startId, startId);
        if (intent != null)
            mDoors = (ArrayList<Door>) intent.getSerializableExtra("doors");
    //    if (ParticleCloudSDK.getCloud().isLoggedIn()) {
            if (mDoors == null)
                getListOfDevices();
            else
                subscribeToEvents(mDoors);
    //    }
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void getListOfDevices() {
        Log.d("AppServiceTest", "APP Service getListOfDevices");
        if (Utils.haveInternet(EventSubscriberService.this)) {
            Thread thread = new Thread() {
                @Override
                public void run() {
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
                                    DoorConfig doorConfig = GaradgetParser.parse(EventSubscriberService.this, doorConfigString, DoorConfig.class);
                                    DoorStatus doorStatus = GaradgetParser.parse(EventSubscriberService.this, doorStatusString, DoorStatus.class);
                                    NetConfig netConfig = GaradgetParser.parse(EventSubscriberService.this, netConfigString, NetConfig.class);
                                    door = new Door(doorConfig, doorStatus, netConfig, device);
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
                        subscribeToEvents(mDoors);
                    } catch (ParticleCloudException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }

    private void subscribeToEvents(final ArrayList<Door> doors) {
        Log.d("EventSubscriberService", "subscribeToEvents");

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    mSubscriptionId = ParticleCloudSDK.getCloud().subscribeToMyDevicesEvents(null, new ParticleEventHandler() {
                                @Override
                                public void onEvent(String s, ParticleEvent particleEvent) {
                                    Log.d("EventSubscriberService", "onEvent");

                                    String deviceId = particleEvent.deviceId;
                                    for (int i = 0; i < doors.size(); i++) {
                                        Door door = doors.get(i);
                                        if (door.getDevice().getID().equals(deviceId)) {
                                            if (Utils.isJson(particleEvent.dataPayload)) {
                                                final DataPayload response = new Gson().fromJson(particleEvent.dataPayload, DataPayload.class);
                                                switch (response.getType().toLowerCase()) {
                                                    case EventsConstants.STATE:
                                                        switch (response.getData()) {
                                                            case StatusConstants.OPENING:
                                                                break;
                                                            case StatusConstants.CLOSED:
                                                                break;
                                                        }

                                                        break;
                                                    case EventsConstants.TIMEOUT:

                                                        break;
                                                    case EventsConstants.NIGHT:
                                                        break;
                                                    case EventsConstants.CONFIG:
                                                        DoorConfig doorConfig = GaradgetParser.parse(EventSubscriberService.this, response.getData(), DoorConfig.class);
                                                        door.setDoorConfig(doorConfig);
                                                        if (!DataLayerListenerService.sendDoorDataToWear(door)) {
                                                            startService(new Intent(EventSubscriberService.this, DataLayerListenerService.class));
                                                        }
                                                        break;
                                                }
                                            } else {
                                                if (door != null && door.getDoorConfig() != null) {
                                                    switch (particleEvent.dataPayload) {
                                                        case StatusConstants.OPENING:
                                                            door.getDoorStatus().setStatus(StatusConstants.CLOSED);
                                                            //  if (doorHolder.getStatusChangeTime() + doorHolder.getDoor().getDoorConfig().getDoorMovingTime() < System.currentTimeMillis()) {
                                                            if (!DataLayerListenerService.sendDoorStatusToWear(door)) {
                                                                startService(new Intent(EventSubscriberService.this, DataLayerListenerService.class));
                                                            }
                                                            //   }
                                                            break;
                                                        case StatusConstants.CLOSING:
                                                            door.getDoorStatus().setStatus(StatusConstants.OPEN);
                                                            //  if (doorHolder.getStatusChangeTime() + doorHolder.getDoor().getDoorConfig().getDoorMovingTime() < System.currentTimeMillis()) {
                                                            if (!DataLayerListenerService.sendDoorStatusToWear(door)) {
                                                                startService(new Intent(EventSubscriberService.this, DataLayerListenerService.class));
                                                            }
                                                            //   }
                                                            break;
                                                    }
                                                }

                                                break;
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onEventError(Exception e) {
                                    Log.d("error", "error:" + e.toString());
                                }
                            }
                    );
// (!DataLayerListenerService.sendDoorsToWear(mDoors)) {
                    if (!DataLayerListenerService.sendIsLoggedStatus(true)) {
                        startService(new Intent(EventSubscriberService.this, DataLayerListenerService.class));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    @Override
    public void onDestroy() {
        try {
            ParticleCloudSDK.getCloud().unsubscribeFromEventWithID(mSubscriptionId);
        } catch (ParticleCloudException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public static ArrayList<Door> getDoors() {
        return mDoors;
    }
}