package volpis.com.garadget;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import io.particle.android.sdk.devicesetup.ParticleDeviceSetupLibrary;
import volpis.com.garadget.database.Database;
import volpis.com.garadget.screens.MainActivity;

public class App extends Application {

    private static App sInstance;
    private static Database mDatabase;
    private Activity mCurrentActivity = null;

    @Override
    public void onCreate() {
        ParticleDeviceSetupLibrary.init(this.getApplicationContext(), MainActivity.class);
        sInstance = this;
        mDatabase = Database.getInstance(getApplicationContext());
        super.onCreate();
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


}
