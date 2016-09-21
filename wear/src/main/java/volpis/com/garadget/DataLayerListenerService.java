package volpis.com.garadget;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.globalclasses.ErrorConstants;
import com.example.globalclasses.PathConstants;
import com.example.globalclasses.StatusConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.android.gms.wearable.WearableStatusCodes;

import java.util.ArrayList;
import java.util.Random;

import volpis.com.garadget.activity.ActionActivity;
import volpis.com.garadget.activity.AlertsActivity;
import volpis.com.garadget.activity.SettingsActivity;
import volpis.com.garadget.models.DoorWearModel;

public class DataLayerListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GaradgetLogs";
    private static boolean mIsDoorsLoaded = false;
    private static Handler mHandler;
    private static Runnable mRunnable;
    public static GoogleApiClient mGoogleApiClient;
    public static DataApi.DataListener mDataListener;
    public static NodeApi.NodeListener mNodeListener;
    public static GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener;
    public static GoogleApiClient.ConnectionCallbacks mConnectionCallbacks;

    @Override
    public void onCreate() {
        super.onCreate();

        if (mGoogleApiClient != null) {
            Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener);
            Wearable.NodeApi.removeListener(mGoogleApiClient, mNodeListener);
            mGoogleApiClient.unregisterConnectionCallbacks(mConnectionCallbacks);
            mGoogleApiClient.unregisterConnectionFailedListener(mOnConnectionFailedListener);
        }
        mDataListener = this;
        mNodeListener = new NodeApi.NodeListener() {
            @Override
            public void onPeerConnected(Node node) {
                mIsDoorsLoaded = false;
                hideError();
                getAppLoginStatus();
            }

            @Override
            public void onPeerDisconnected(Node node) {
                Activity mainActivity = App.getInstance().getMainActivity();
                if (mainActivity != null)
                    if (mainActivity instanceof MainActivity)
                        ((MainActivity) mainActivity).clearLoginStatus();
                showError("Not connected to handheld");
            }
        };

        mConnectionCallbacks = this;
        mOnConnectionFailedListener = this;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .build();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        // disconnect old if connected
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting())
            mGoogleApiClient.disconnect();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, mDataListener);
        //check if connected to handheld
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                boolean isPeerConnected = getConnectedNodesResult.getNodes().size() > 0;
                if (!isPeerConnected)
                    showError("Not connected to handheld");
            }
        });
        //handheld connection change listener
        Wearable.NodeApi.addListener(mGoogleApiClient, mNodeListener);

        mIsDoorsLoaded = false;
        getAppLoginStatus();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, " APP Service onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, " APP Service onConnectionFailed");
    }

    /**
     * send message to handheld and wait for data
     * if data not received for 20 seconds then send message again
     */
    public static void getAppDoors() {
        mIsDoorsLoaded = false;
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mIsDoorsLoaded) {
                    Log.d(TAG, "Will try to load data again");
                    sendMessage(PathConstants.GET_DEVICES_PATH, null);
                    if (!mIsDoorsLoaded)
                        mHandler.postDelayed(mRunnable, 20000);
                }
            }
        };
        mHandler.post(mRunnable);
    }

    /**
     * open, close door
     */
    public static void changeAppDoorStatus(final DoorWearModel doorWearModel) {
        String doorStatusData = doorWearModel.getDoorId() + "|" + (doorWearModel.isOpened() ? StatusConstants.OPEN : StatusConstants.CLOSED);
        sendMessage(PathConstants.CHANGE_DOOR_STATUS_PATH, doorStatusData.getBytes());
    }

    public static void getAppLoginStatus() {
        sendMessage(PathConstants.LOGIN_STATUS_PATH, null);
    }

    /**
     * send message to handheld
     */
    public static void sendMessage(final String path, final byte[] data) {
        PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                for (int i = 0; i < result.getNodes().size(); i++) {
                    Node node = result.getNodes().get(i);
                    PendingResult<MessageApi.SendMessageResult> messageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, data);
                    messageResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Status status = sendMessageResult.getStatus();
                            Log.d("myLogs", "Status: " + status.toString());
                            if (status.getStatusCode() != WearableStatusCodes.SUCCESS) {

                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            Log.d(TAG, "WEAR onDataChanged " + event.getDataItem().getUri().getPath());
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(PathConstants.GET_DEVICES_PATH) == 0) {
                    Activity mainActivity = App.getInstance().getMainActivity();
                    ArrayList<DoorWearModel> doorWearModels = new ArrayList<>();
                    ArrayList<DataMap> dataMapArrayList = DataMapItem.fromDataItem(item).getDataMap().getDataMapArrayList(PathConstants.DOORS_KEY);

                    boolean fromRefresh = false;
                    for (DataMap dataMap : dataMapArrayList) {
                        doorWearModels.add(new DoorWearModel(dataMap));
                        fromRefresh = dataMap.getBoolean("fromRefresh", false);
                    }

                    if (!mIsDoorsLoaded || fromRefresh) {
                        mIsDoorsLoaded = true;
                        Activity currentActivity = App.getInstance().getCurrentActivity();
                        if (currentActivity != null)
                            if (currentActivity instanceof AlertsActivity)
                                ((AlertsActivity) currentActivity).fillDoors(doorWearModels);
                            else if (currentActivity instanceof SettingsActivity)
                                ((SettingsActivity) currentActivity).fillDoors(doorWearModels);
                            else if (currentActivity instanceof ActionActivity)
                                ((ActionActivity) currentActivity).fillDoors(doorWearModels);
                        if (mainActivity != null) {
                            ((MainActivity) mainActivity).showProgress(true);
                            ((MainActivity) mainActivity).fillDoors(doorWearModels, !fromRefresh);
                        }
                    }
                } else if (item.getUri().getPath().compareTo(PathConstants.CHANGE_DOOR_STATUS_PATH) == 0) { //door status changed
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap().getDataMap(PathConstants.CHANGE_DOOR_KEY);
                    DoorWearModel changedDoorWearModel = new DoorWearModel(dataMap);
                    Activity mainActivity = App.getInstance().getMainActivity();
                    if (mainActivity != null)
                        if (mainActivity instanceof MainActivity) {
                            ((MainActivity) mainActivity).fillNewDoorStatus(changedDoorWearModel);
                        }
                } else if (item.getUri().getPath().compareTo(PathConstants.DOOR_DATA_CHANGE_PATH) == 0) { //door data changed (settings, alerts)
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap().getDataMap(PathConstants.DOOR_DATA_CHANGE_KEY);
                    DoorWearModel changedDoorWearModel = new DoorWearModel(dataMap);
                    Activity mainActivity = App.getInstance().getMainActivity();
                    if (mainActivity != null)
                        if (mainActivity instanceof MainActivity) {
                            ((MainActivity) mainActivity).setDoor(changedDoorWearModel);
                        }
                    Activity currentActivity = App.getInstance().getCurrentActivity();
                    if (currentActivity != null)
                        if (currentActivity instanceof AlertsActivity)
                            ((AlertsActivity) currentActivity).fillDoor(changedDoorWearModel);
                        else if (currentActivity instanceof SettingsActivity)
                            ((SettingsActivity) currentActivity).fillDoor(changedDoorWearModel);
                        else if (currentActivity instanceof ActionActivity)
                            ((ActionActivity) currentActivity).fillDoor(changedDoorWearModel);
                } else if (item.getUri().getPath().compareTo(PathConstants.LOGIN_STATUS_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap().getDataMap(PathConstants.LOGIN_STATUS_KEY);
                    boolean isLoggedIn = dataMap.getBoolean("login_status");
                    boolean refill = dataMap.getBoolean("refill");
                    Activity mainActivity = App.getInstance().getMainActivity();
                    if (mainActivity != null)
                        if (mainActivity instanceof MainActivity)
                            ((MainActivity) mainActivity).fillLoginStatus(isLoggedIn/*.equals("true")*/, refill);
                } else if (item.getUri().getPath().compareTo(PathConstants.NOTIFICATION_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap().getDataMap(PathConstants.NOTIFICATION_KEY);
                    String message = dataMap.getString("message");
                    Activity mainActivity = App.getInstance().getMainActivity();
                    if (mainActivity == null)
                        createNotification(message);
                } else if (item.getUri().getPath().compareTo(PathConstants.ERROR_PATH) == 0) {
                    Log.d(TAG, "GET ERROR ON A WEAR");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap().getDataMap(PathConstants.ERROR_KEY);
                    String error = dataMap.getString("error");
                    int errorCode = dataMap.getInt("errorCode");
                    if (errorCode == ErrorConstants.REQUEST_NOT_ENDED) {
                        //getAppDoors();
                    } else
                        showError(error);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    private void showError(String error) {
        Activity mainActivity = App.getInstance().getMainActivity();
        if (mainActivity != null)
            if (mainActivity instanceof MainActivity)
                ((MainActivity) mainActivity).showError(R.drawable.ic_error, error);
    }

    private void hideError() {
        Activity mainActivity = App.getInstance().getMainActivity();
        if (mainActivity != null)
            if (mainActivity instanceof MainActivity) {
                ((MainActivity) mainActivity).hideError();
                ((MainActivity) mainActivity).showProgress(true);
            }
    }

    private void createNotification(String message) {
        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher1)
                        .setContentTitle("Garadget")
                        .setContentText(message)
                        .setLocalOnly(true)
                        .setContentIntent(viewPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (App.getInstance() == null || App.getInstance().getMainActivity() == null)
            notificationManager.notify(generateId(), notificationBuilder.build());
    }

    private int generateId() {
        return new Random().nextInt();
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting())
            mGoogleApiClient.disconnect();
        mIsDoorsLoaded = true;
        if (mHandler != null && mRunnable != null)
            mHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }
}