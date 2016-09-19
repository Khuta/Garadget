package volpis.com.garadget;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import volpis.com.garadget.fragments.DoorsPagerFragment;
import volpis.com.garadget.models.DoorWearModel;

public class MainActivity extends WearableActivity {

    ArrayList<DoorWearModel> mDoorWearModels;

    TextView tvError;
    ImageView ivError;
    LinearLayout progress;
    FrameLayout frameContent;
    BoxInsetLayout llError;

    String mIsLoggedIn = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.getInstance().setMainActivity(this);
        setAmbientEnabled();

        if (isMyServiceRunning(DataLayerListenerService.class))
            stopService(new Intent(MainActivity.this, DataLayerListenerService.class));
        startService(new Intent(MainActivity.this, DataLayerListenerService.class));
//        if (DataLayerListenerService.mGoogleApiClient != null && DataLayerListenerService.mGoogleApiClient.isConnected())
//            DataLayerListenerService.getAppLoginStatus();

        tvError = (TextView) findViewById(R.id.tv_error);
        ivError = (ImageView) findViewById(R.id.iv_error);
        progress = (LinearLayout) findViewById(R.id.ll_progress);
        frameContent = (FrameLayout) findViewById(R.id.frame_content);
        llError = (BoxInsetLayout) findViewById(R.id.ll_error);

        DoorsPagerFragment doorsPagerFragment = new DoorsPagerFragment();
        FragmentManager fragmentManager = getFragmentManager();
        frameContent.removeAllViews();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction().replace(R.id.frame_content, doorsPagerFragment).commit();

    }

    public void fillDoors(ArrayList<DoorWearModel> doorWearModels, final boolean refill) {
        mDoorWearModels = doorWearModels;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Fragment currentFragment = getCurrentFragment();
                if (currentFragment instanceof DoorsPagerFragment) {
                    ((DoorsPagerFragment) currentFragment).fillDoors(mDoorWearModels, refill);
                    progress.setVisibility(View.GONE);
                }
            }
        });
    }

    public void fillNewDoorStatus(DoorWearModel doorWearModel) {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof DoorsPagerFragment) {
            ((DoorsPagerFragment) currentFragment).fillNewDoorStatus(doorWearModel);
        }
        setDoor(doorWearModel);
    }

    public void clearLoginStatus() {
        mIsLoggedIn = "";
    }

    public void fillLoginStatus(boolean isLoggedIn, boolean refill) {
        if (refill)
            mIsLoggedIn = "";
        if (mIsLoggedIn.equals("") || !mIsLoggedIn.equals(String.valueOf(isLoggedIn)))
            if (isLoggedIn) {
                hideError();
                progress.setVisibility(View.VISIBLE);
                DataLayerListenerService.getAppDoors();
            } else {
                showError(R.drawable.ic_phone, "Not logged in");
                progress.setVisibility(View.GONE);
            }
        mIsLoggedIn = String.valueOf(isLoggedIn);
    }


    public Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentById(R.id.frame_content);
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void showError(int imageRes, String error) {
        tvError.setText(error);
        ivError.setImageResource(imageRes);
        llError.setVisibility(View.VISIBLE);
//        llError.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//            }
//        });
    }

    public void hideError() {
        llError.setVisibility(View.GONE);
    }

    public void showProgress(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setDoor(DoorWearModel door) {
        if (mDoorWearModels != null)
            for (DoorWearModel doorWearModle : mDoorWearModels) {
                if (door.getDoorId().equals(doorWearModle.getDoorId())) {
                    mDoorWearModels.set(mDoorWearModels.indexOf(doorWearModle), door);
                    fillDoors(mDoorWearModels, false);
                    break;
                }
            }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(MainActivity.this, DataLayerListenerService.class));
        if (App.getInstance() != null)
            App.getInstance().setMainActivity(null);
        super.onDestroy();
    }
}
