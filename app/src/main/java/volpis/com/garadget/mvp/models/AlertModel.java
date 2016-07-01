package volpis.com.garadget.mvp.models;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;
import volpis.com.garadget.App;
import volpis.com.garadget.R;
import volpis.com.garadget.database.Database;
import volpis.com.garadget.interfaces.AlertsMVP;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorLocation;
import volpis.com.garadget.requests.PushNotificationSignUp;
import volpis.com.garadget.utils.FunctionConstants;
import volpis.com.garadget.utils.Utils;

public class AlertModel implements AlertsMVP.ModelOps {
    private LocationManager mLocationManager;
    private Context mContext;
    private AlertsMVP.RequiredPresenterOps mPresenter;
    private RequestQueue mRequestQueue;
    private Database mDatabase;
    private Door mDoor;
    private DoorLocation mDoorLocation;
    private int aev;
    private int selectedRadius = 0;

    public AlertModel(Context context, AlertsMVP.RequiredPresenterOps presenter) {
        mContext = context;
        mPresenter = presenter;
        mRequestQueue = Volley.newRequestQueue(mContext);
        mDatabase = App.getDatabase();
        mDoor = ((Activity) context).getIntent().getParcelableExtra("door");
        mDoorLocation = mDatabase.getDoorLocation(mDoor.getDevice().getID());
        aev = mDoor.getDoorConfig().getStatusAlerts();

        if (mDoorLocation != null)
            setSelectedRadius(mDoorLocation.getRadius());
        mPresenter.fillData(mDoor, mDoorLocation);
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            mPresenter.moveMap(new LatLng(location.getLatitude(), location.getLongitude()));
            mLocationManager.removeUpdates(mLocationListener);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public void notifyBackend(String action) {
        String doorId = mDoor.getDevice().getID();
        PushNotificationSignUp request = new PushNotificationSignUp(mContext, doorId, action, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (mContext != null)
                    mPresenter.onUpdatesSaved(mContext.getString(R.string.alerts_updated));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("onErrorResponse", error.toString());
                if (mContext != null)
                    mPresenter.onError(mContext.getString(R.string.error_sending_command));
            }
        });
        mRequestQueue.add(request);
    }

    @Override
    public void setLocationChangedListener() {
        mLocationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
    }

    @Override
    public void writeMapData(LatLng latLng) {
        mDoorLocation = new DoorLocation(mDoor.getDevice().getID(), latLng.latitude, latLng.longitude, selectedRadius, true);
        mDatabase.updateOrInsertDoorLocation(mDoorLocation);
    }

    @Override
    public void removeDoorLocation() {
        mDatabase.removeDoorLocation(mDoor.getDevice().getID());
    }

    @Override
    public void setDoor(Door door) {
        mDoor = door;
        mDoorLocation = mDatabase.getDoorLocation(mDoor.getDevice().getID()); 
        if (mDoorLocation != null)       
        setSelectedRadius(mDoorLocation.getRadius());
        mPresenter.fillData(mDoor, mDoorLocation);
        aev = mDoor.getDoorConfig().getStatusAlerts();
    }

    @Override
    public void updateConfig(final String newConfig) {
        if (Utils.haveInternet(mContext)) {
            Async.executeAsync(ParticleCloud.get(mContext), new Async.ApiWork<ParticleCloud, Object>() {
                @Override
                public Object callApi(ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                    ArrayList<String> params = new ArrayList<String>();
                    params.add(newConfig);
                    try {
                        ParticleCloudSDK.getCloud().getDevice(mDoor.getDevice().getID()).callFunction(FunctionConstants.FUNCTION_SET_CONFIG, params);
                    } catch (ParticleDevice.FunctionDoesNotExistException | IOException e) {
                        e.printStackTrace();
                    } catch (ParticleCloudException e) {
                        e.printStackTrace();
                    }
                    return -1;
                }

                @Override
                public void onSuccess(Object value) {
                    if (mContext != null)
                        mPresenter.onUpdatesSaved(mContext.getString(R.string.alerts_updated));
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
    public void onMapReady() {
        if (mDoorLocation != null && mDoorLocation.isEnabled()) {
            LatLng mDoorLatLng = new LatLng(mDoorLocation.getLatitude(), mDoorLocation.getLongitude());
            mPresenter.showMarkers(mDoor.getName(), mDoorLatLng, mDoorLocation.getRadius());
        }
    }

    @Override
    public void radiusSelected(int radius) {
        selectedRadius = radius;
        if (mDoorLocation != null) {
            LatLng latLng = new LatLng(mDoorLocation.getLatitude(), mDoorLocation.getLongitude());
            writeMapData(latLng);
            mPresenter.showMarkers(mDoor.getName(), latLng, radius);
            mPresenter.fillRadiusText(radius);
        }
    }

    public void flipBit(int position) {
        aev = aev ^ 1 << position;
        updateConfig("aev=" + aev);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        writeMapData(latLng);
        mPresenter.showMarkers(mDoor.getName(), latLng, selectedRadius);
    }

    public void setSelectedRadius(int selectedRadius) {
        this.selectedRadius = selectedRadius;
    }
}
