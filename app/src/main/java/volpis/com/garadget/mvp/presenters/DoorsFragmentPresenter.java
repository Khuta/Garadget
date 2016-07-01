package volpis.com.garadget.mvp.presenters;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleDevice;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorHolder;
import volpis.com.garadget.interfaces.DoorsMVP;
import volpis.com.garadget.mvp.models.DoorModel;

public class DoorsFragmentPresenter implements DoorsMVP.RequiredPresenterOps, DoorsMVP.PresenterOps {

    private WeakReference<DoorsMVP.RequiredViewOps> mView;
    private DoorsMVP.ModelOps mModel;

    public DoorsFragmentPresenter(Context context, DoorsMVP.RequiredViewOps mView) {
        this.mView = new WeakReference<>(mView);
        this.mModel = new DoorModel(context, this);
    }

    public void changeDoorStatus(ParticleDevice device, DoorHolder doorHolder, String newStatus) {
        mModel.changeDoorStatus(device, doorHolder, newStatus);
    }

    @Override
    public void setDoors(ArrayList<Door> doors, List<ParticleDevice> devices) {
        mView.get().setDoors(doors, devices);
    }

    @Override
    public void onSuccess() {
        showSwipeRefresh(false);
        showProgress(false);
    }

    @Override
    public void onFailure(String errorMessage) {
        showSwipeRefresh(false);
        showToast(errorMessage);
        Log.d("onFailure", errorMessage);
    }

    @Override
    public void showSwipeRefresh(boolean show) {
        mView.get().showSwipeRefresh(show);
    }

    @Override
    public void showProgress(boolean show) {
        mView.get().showProgress(show);
    }

    @Override
    public void showToast(String message) {
        mView.get().showToast(message);
    }

    @Override
    public void startAnimation(DoorHolder doorHolder, ImageView imageView, boolean isOpening, long openingTime) {
        mView.get().startAnimation(doorHolder, imageView, isOpening, openingTime);
    }


    @Override
    public void getListOfDevices(ArrayList<DoorHolder> doorHolder) {
        mModel.getListOfDevices(doorHolder);
    }

    @Override
    public void onDestroy() {
        mModel.onDestroy();
    }
}