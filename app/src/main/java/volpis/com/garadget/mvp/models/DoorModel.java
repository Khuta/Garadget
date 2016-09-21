package volpis.com.garadget.mvp.models;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.ui.Toaster;
import volpis.com.garadget.App;
import volpis.com.garadget.R;
import volpis.com.garadget.models.DataPayload;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorConfig;
import volpis.com.garadget.models.DoorHolder;
import volpis.com.garadget.models.DoorStatus;
import volpis.com.garadget.models.NetConfig;
import volpis.com.garadget.parser.GaradgetParser;
import volpis.com.garadget.services.DataLayerListenerService;
import volpis.com.garadget.utils.EventsConstants;

import com.example.globalclasses.StatusConstants;

import volpis.com.garadget.utils.Utils;
import volpis.com.garadget.mvp.views.AlertsActivity;
import volpis.com.garadget.interfaces.DoorsMVP;
import volpis.com.garadget.utils.FunctionConstants;
import volpis.com.garadget.screens.MainActivity;
import volpis.com.garadget.mvp.views.SettingsActivity;

public class DoorModel implements DoorsMVP.ModelOps {
    private DoorsMVP.RequiredPresenterOps mPresenter;
    private Context mContext;
    List<ParticleDevice> mDevices = new ArrayList<>();
    Gson mGson = new Gson();
    private long mSubscriptionId;

    public DoorModel(Context context, DoorsMVP.RequiredPresenterOps mPresenter) {
        this.mPresenter = mPresenter;
        this.mContext = context;
    }

    @Override
    public void changeDoorStatus(final ParticleDevice device, final DoorHolder doorHolder, final String newStatus) {

        Async.executeAsync(ParticleCloud.get(mContext), new Async.ApiWork<ParticleCloud, Object>() {
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
                doorHolder.getDoor().getDoorStatus().setStatus(newStatus);
                ((MainActivity) mContext).checkLocationListenerStart();
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                Toaster.l(mContext, e.getBestMessage());
                e.printStackTrace();
                Log.d("onFailure", e.getBestMessage());
            }
        });

    }

    public void getListOfDevices(final ArrayList<DoorHolder> doorHolders, final boolean fromRefresh) {
        if (Utils.haveInternet(mContext)) {
            Async.executeAsync(ParticleCloud.get(mContext), new Async.ApiWork<ParticleCloud, Object>() {
                @Override
                public Object callApi(ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                    final ArrayList<Door> doors = new ArrayList<Door>();
                    try {
                        mDevices = ParticleCloudSDK.getCloud().getDevices();
                        for (ParticleDevice device : mDevices) {
                            Door door = new Door();
                            if (device.isConnected()) {
                                try {
                                    String doorConfigString = device.getStringVariable("doorConfig");
                                    String doorStatusString = device.getStringVariable("doorStatus");
                                    String netConfigString = device.getStringVariable("netConfig");
                                    DoorConfig doorConfig = GaradgetParser.parse(mContext, doorConfigString, DoorConfig.class);
                                    DoorStatus doorStatus = GaradgetParser.parse(mContext, doorStatusString, DoorStatus.class);
                                    NetConfig netConfig = GaradgetParser.parse(mContext, netConfigString, NetConfig.class);
                                    door = new Door(doorConfig, doorStatus, netConfig, device);
                                } catch (ParticleDevice.VariableDoesNotExistException e) {
                                    e.printStackTrace();
                                }
                            }
                            door.setDevice(device);
                            doors.add(door);
                        }

                        subscribeToEvents(doorHolders);
                        if (mContext != null && !((Activity) mContext).isFinishing())
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((MainActivity) mContext).setDoors(doors);
                                    ((MainActivity) mContext).checkLocationListenerStart();
                                    mPresenter.setDoors(doors, mDevices);
                                    if (fromRefresh)
                                        DataLayerListenerService.sendDoorsToWear(doors, true);
                                }
                            });

                    } catch (ParticleCloudException e) {
                        e.printStackTrace();
                    }
                    return -1;

                }

                @Override
                public void onSuccess(Object value) {
                    mPresenter.onSuccess();
                }

                @Override
                public void onFailure(ParticleCloudException e) {
                    mPresenter.onFailure(e.getBestMessage());
                }
            });
        } else {
            new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.network_error_title)).setMessage(mContext.getString(R.string.network_error_message)).setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mPresenter.showSwipeRefresh(false);
                    mPresenter.showProgress(false);
                    dialogInterface.dismiss();
                }
            }).show();
        }
    }

    private void subscribeToEvents(final ArrayList<DoorHolder> doorHolders) {
        try {
            mSubscriptionId = ParticleCloudSDK.getCloud().subscribeToMyDevicesEvents(null, new ParticleEventHandler() {
                        @Override
                        public void onEvent(String s, ParticleEvent particleEvent) {
                            String deviceId = particleEvent.deviceId;
                            for (int i = 0; i < doorHolders.size(); i++) {
                                final DoorHolder doorHolder = doorHolders.get(i);
                                if (doorHolder.getDoor().getDevice().getID().equals(deviceId)) {
                                    if (Utils.isJson(particleEvent.dataPayload)) {
                                        final DataPayload response = mGson.fromJson(particleEvent.dataPayload, DataPayload.class);
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
                                                ((Activity) mContext).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        DoorConfig doorConfig = GaradgetParser.parse(mContext, response.getData(), DoorConfig.class);
                                                        doorHolder.getDoor().setDoorConfig(doorConfig);
                                                        doorHolder.fillView(mContext, doorHolder.getDoor());

                                                        final Activity currentActivity = App.getInstance().getCurrentActivity();
                                                        if (currentActivity instanceof AlertsActivity) {
                                                            ((AlertsActivity) currentActivity).setDoor(doorHolder.getDoor());
                                                        } else if (currentActivity instanceof SettingsActivity) {
                                                            ((SettingsActivity) currentActivity).setDoor(doorHolder.getDoor());
                                                        }
                                                        ArrayList<Door> doors = new ArrayList<Door>();
                                                        for (DoorHolder doorHolderI : doorHolders)
                                                            doors.add(doorHolderI.getDoor());
                                                        DataLayerListenerService.sendDoorDataToWear(doorHolder.getDoor());
                                                    }
                                                });
                                                break;
                                        }
                                    } else {
                                        if (doorHolder != null && doorHolder.getDoor() != null && doorHolder.getDoor().getDoorConfig() != null)
                                            switch (particleEvent.dataPayload) {
                                                case StatusConstants.OPENING:
                                                    if (doorHolder.getStatusChangeTime() + doorHolder.getDoor().getDoorConfig().getDoorMovingTime() < System.currentTimeMillis()) {
                                                        mPresenter.startAnimation(doorHolder, (ImageView) doorHolder.getView().findViewById(R.id.image_door), true, doorHolder.getDoor().getDoorConfig().getDoorMovingTime());
                                                        DataLayerListenerService.sendDoorStatusToWear(doorHolder.getDoor());
                                                    }
                                                    doorHolder.setStatusChangeTime(System.currentTimeMillis());
                                                    doorHolder.getDoor().getDoorStatus().setStatus(StatusConstants.OPEN);
                                                    ((MainActivity) mContext).checkLocationListenerStart();
                                                    break;
                                                case StatusConstants.CLOSING:
                                                    if (doorHolder.getStatusChangeTime() + doorHolder.getDoor().getDoorConfig().getDoorMovingTime() < System.currentTimeMillis()) {
                                                        mPresenter.startAnimation(doorHolder, (ImageView) doorHolder.getView().findViewById(R.id.image_door), false, doorHolder.getDoor().getDoorConfig().getDoorMovingTime());
                                                        DataLayerListenerService.sendDoorStatusToWear(doorHolder.getDoor());
                                                    }
                                                    doorHolder.setStatusChangeTime(System.currentTimeMillis());
                                                    doorHolder.getDoor().getDoorStatus().setStatus(StatusConstants.CLOSED);
                                                    ((MainActivity) mContext).checkLocationListenerStart();
                                                    break;
                                            }

                                        break;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onEventError(Exception e) {

                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        try {
            ParticleCloudSDK.getCloud().unsubscribeFromEventWithID(mSubscriptionId);
        } catch (ParticleCloudException e) {
            e.printStackTrace();
        }
    }

}
