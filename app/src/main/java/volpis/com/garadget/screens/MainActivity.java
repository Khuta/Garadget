package volpis.com.garadget.screens;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.devicesetup.ParticleDeviceSetupLibrary;
import volpis.com.garadget.App;
import volpis.com.garadget.R;
import volpis.com.garadget.adapters.BottomPanelAdapter;
import volpis.com.garadget.interfaces.OnLogoutListener;
import volpis.com.garadget.models.BottomPanelItem;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorConfig;
import volpis.com.garadget.models.DoorLocation;
import volpis.com.garadget.mvp.views.AlertsActivity;
import volpis.com.garadget.mvp.views.DoorsFragment;
import volpis.com.garadget.mvp.views.SettingsActivity;
import volpis.com.garadget.requests.PushNotificationSignUp;
import volpis.com.garadget.utils.FunctionConstants;
import volpis.com.garadget.utils.SharedPreferencesUtils;
import volpis.com.garadget.utils.StatusConstants;

public class MainActivity extends DrawerActivity {

    private BottomPanelAdapter mBottomPanelAdapter;
    ArrayList<Door> doors;
    DoorsFragment doorsFragment;
    LocationListener mLocationListener;
    LocationManager mLocationManager;
    private String mToken = "";

    @Bind(R.id.linear_bottom_panel)
    LinearLayout linearBottomPanel;
    @Bind(R.id.frame_content)
    FrameLayout frameContent;
    @Bind(R.id.image_drawer)
    ImageView imageDrawer;
    @Bind(R.id.text_title)
    TextView textTitle;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getButtonId() {
        return 0;
    }

    @Override
    protected Context getContext() {
        return MainActivity.this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        textTitle.setText(getString(R.string.title_main_activity));
        initTabs();
        imageDrawer.setImageResource(R.drawable.ic_tab_settings);
        //    doors = (ArrayList<Door>) getIntent().getSerializableExtra("doors");
        doorsFragment = new DoorsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        frameContent.removeAllViews();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction().replace(R.id.frame_content, doorsFragment).commit();
        setListeners();
        createAndSendDeviceToken(this);
    }

    private void setListeners() {
        imageDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDrawerLayout.openDrawer(myDrawerList);
            }
        });
        setOnLogoutListener(new OnLogoutListener() {
            @Override
            public void onLogout() {
                removeSubscriptions();
            }
        });
    }

    public void initTabs() {
        ArrayList<BottomPanelItem> bottomPanelItems = new ArrayList<>();

        bottomPanelItems.add(new BottomPanelItem("settings", R.drawable.ic_tab_settings));
        bottomPanelItems.add(new BottomPanelItem("alerts", R.drawable.ic_tab_alerts));
        bottomPanelItems.add(new BottomPanelItem("add door", R.drawable.ic_tab_add_door));
        bottomPanelItems.add(new BottomPanelItem("help", R.drawable.ic_tab_help));

        if (mBottomPanelAdapter == null) {
            mBottomPanelAdapter = new BottomPanelAdapter(MainActivity.this, R.layout.item_bottom_panel, bottomPanelItems);
        } else {
            mBottomPanelAdapter.notifyDataSetChanged();
        }

        linearBottomPanel.removeAllViews();

        for (int i = 0; i < bottomPanelItems.size(); i++) {
            View v = mBottomPanelAdapter.getView(i, null, null);
            final int finalI = i;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (finalI) {
                        case 0:
                            if (doors != null && doors.size() > 0) {
                                Door door = doors.get(doorsFragment.getSelectedDoorPosition());
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                intent.putExtra("door", door);
                                startActivity(intent);
                            }
                            break;
                        case 1:
                            if (doors != null && doors.size() > 0) {
                                Door door = doors.get(doorsFragment.getSelectedDoorPosition());
                                if (door.getDoorConfig() != null) {
                                    Intent intent = new Intent(MainActivity.this, AlertsActivity.class);
                                    intent.putExtra("door", door);
                                    startActivity(intent);
                                } else {
                                    new AlertDialog.Builder(MainActivity.this).setTitle(getString(R.string.alert)).setMessage(door.getName().replace("_", " ") + " is offline!").setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                                }
                            }
                            break;
                        case 2:
                            ParticleDeviceSetupLibrary.startDeviceSetup(MainActivity.this);
                            break;
                        case 3:
                            startActivity(new Intent(MainActivity.this, HelpActivity.class));

                            break;
                    }
                }
            });
            linearBottomPanel.addView(v);
        }
    }

    public ArrayList<Door> getDoors() {
        return doors;
    }

    public void setDoors(ArrayList<Door> doors) {
        this.doors = doors;
        /*
        We'll use this ids for subscribing to push notification
         */
        if (!SharedPreferencesUtils.getInstance().isSubscribedForEvents()) {
            String deviceIds = "";
            for (Door door : doors) {
                if (door != null && door.getDevice() != null && door.getDevice().isConnected()) {
                    DoorConfig doorConfig = door.getDoorConfig();
                    if (doorConfig != null && (doorConfig.getStatusAlerts() != 0 || (doorConfig.getOpenTimeout() != 0) || doorConfig.getNightTimeStart() != doorConfig.getNightTimeEnd())) {
                        if (deviceIds.length() > 0)
                            deviceIds += ",";
                        deviceIds += door.getDevice().getID();

                    }
                }
            }
            EventsHandlerRequest request = new EventsHandlerRequest(MainActivity.this, "add", deviceIds, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray jsonObject) {
                    SharedPreferencesUtils.getInstance().setSubscribedToEvents(true);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            });
            Volley.newRequestQueue(MainActivity.this).add(request);
        }
    }

    public void checkLocationListenerStart() {
        boolean needLocListener = false;
        for (Door door : doors) {
            if (door != null && door.getDoorStatus() != null && door.getDoorStatus().getStatus() != null)
                if ((door.getDoorStatus().getStatus().equals(StatusConstants.OPEN) ||
                        door.getDoorStatus().getStatus().equals(StatusConstants.OPENING) ||
                        door.getDoorStatus().getStatus().equals(StatusConstants.STOPPED)) &&
                        App.getDatabase().getDoorLocation(door.getDevice().getID()) != null) {
                    needLocListener = true;
                    break;
                }
        }
        if (needLocListener) {
            startLocationListener();
        } else {
            stopLocationListener();
        }
    }

    private void stopLocationListener() {
        if (mLocationManager != null && mLocationListener != null)
            mLocationManager.removeUpdates(mLocationListener);
    }

    private void startLocationListener() {
        stopLocationListener();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location myLocation) {
                ArrayList<DoorLocation> doorLocations = App.getDatabase().getDoorLocations();
                for (DoorLocation doorLoc : doorLocations) {
                    Location doorLocation = new Location("doorLocation");
                    doorLocation.setLatitude(doorLoc.getLatitude());
                    doorLocation.setLongitude(doorLoc.getLongitude());
                    if (doorLocation.distanceTo(myLocation) < doorLoc.getRadius()) {
                        createNotification(doorLoc.getDoorId());
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30, 10, mLocationListener);
        }
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 10, mLocationListener);
        }

    }

    private void createNotification(String doorID) {
        Door door = null;
        if (doors != null)
            for (Door d : doors) {
                if (d.getDevice().getID().equals(doorID)) {
                    door = d;
                    break;
                }
            }
        if (door != null && door.getDoorStatus() != null && (door.getDoorStatus().getStatus().equals(StatusConstants.OPEN) ||
                door.getDoorStatus().getStatus().equals(StatusConstants.OPENING) ||
                door.getDoorStatus().getStatus().equals(StatusConstants.STOPPED))) {
            int mNotificationId = doorID.hashCode();
            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(door.getName())
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(door.getDoorStatus().getStatus()))
                            .setContentText(door.getDoorStatus().getStatus());
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
    }

    public void createAndSendDeviceToken(final Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return createDeviceToken(context);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s != null) {
                    mToken = s;
                    SharedPreferencesUtils.getInstance().setRegistrationToken(mToken);
                }
            }
        }.execute();
    }

    public String createDeviceToken(Context context) {
        String token = SharedPreferencesUtils.getInstance().getRegistrationToken();
        if (token == null || token.equals("")) {
            try {
                token = InstanceID.getInstance(context).getToken(context.getString(R.string.project_id), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return token;
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().setCurrentActivity(this);


    }

    private class EventsHandlerRequest extends Request<JSONArray> {
        private String mAction;
        private String mDevices;
        private final Response.Listener<JSONArray> listener;

        public EventsHandlerRequest(Context context, String action, String devices, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
            super(Method.POST, context.getString(R.string.push_url), errorListener);
            mAction = action;
            mDevices = devices;
            this.listener = listener;
        }

        @Override
        protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(
                        response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                JSONArray jsonObject = new JSONArray(json);
                return Response.success(jsonObject,
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JsonSyntaxException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException e) {
                return Response.error(new ParseError(e));
            }
        }

        @Override
        protected void deliverResponse(JSONArray jsonObject) {
            listener.onResponse(jsonObject);
        }

        @Override
        public Map<String, String> getParams() {
            Map<String, String> params = new HashMap<String, String>();
            params.put("action", mAction);
            params.put("platform", "gcm");
            params.put("subscriber", SharedPreferencesUtils.getInstance().getRegistrationToken());
            params.put("device", mDevices);
            params.put("authtoken", ParticleCloudSDK.getCloud().getAccessToken());
            return params;
        }

        @Override
        protected VolleyError parseNetworkError(VolleyError volleyError) {
            return super.parseNetworkError(volleyError);
        }

    }

    private void removeSubscriptions() {
        String authToken = ParticleCloudSDK.getCloud().getAccessToken();
//        StringBuilder stringBuilder = new StringBuilder();
//        for (Door door : doors) {
//            int index = doors.indexOf(door);
//            String doorId = door.getDevice().getID();
//            stringBuilder.append(doorId);
//            if (index != doors.size() - 1)
//                stringBuilder.append(",");
//        }

        PushNotificationSignUp request = new PushNotificationSignUp(MainActivity.this, "", FunctionConstants.FUNCTION_REMOVE, authToken, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("removeSub success", "success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("onErrorResponse", error.toString());

            }
        });
        Volley.newRequestQueue(this).add(request);
    }

}