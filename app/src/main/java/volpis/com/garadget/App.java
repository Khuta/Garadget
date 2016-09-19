package volpis.com.garadget;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;

import io.fabric.sdk.android.Fabric;
import io.particle.android.sdk.devicesetup.ParticleDeviceSetupLibrary;
import volpis.com.garadget.database.Database;
import volpis.com.garadget.screens.MainActivity;

public class App extends Application {
    private static GoogleApiClient mGoogleApiClient;
    private static App sInstance;
    private static Database mDatabase;
    private Activity mCurrentActivity = null;

    @Override
    public void onCreate() {
        ParticleDeviceSetupLibrary.init(this.getApplicationContext(), MainActivity.class);
        sInstance = this;
        mDatabase = Database.getInstance(getApplicationContext());
        super.onCreate();
        Log.d("serviceTest", "App onCreate");
        Fabric.with(this, new Crashlytics());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    static public App getInstance() {
        return sInstance;
    }

    public static Database getDatabase() {
        return mDatabase;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    public static GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public static void setGoogleApiClient(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }

}
