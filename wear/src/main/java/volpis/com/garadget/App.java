package volpis.com.garadget;

import android.app.Activity;
import android.app.Application;

public class App extends Application {
    private Activity mCurrentActivity = null;
    private Activity mMainActivity = null;
    private static App sInstance;

    @Override
    public void onCreate() {
        sInstance = this;
        super.onCreate();
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        if (mCurrentActivity instanceof MainActivity)
            mMainActivity = mCurrentActivity;
        else
            this.mCurrentActivity = mCurrentActivity;
    }

    public Activity getMainActivity() {
        return mMainActivity;
    }

    static public App getInstance() {
        return sInstance;
    }

    public void setMainActivity(Activity mainActivity) {
        this.mMainActivity = mainActivity;
    }
}
