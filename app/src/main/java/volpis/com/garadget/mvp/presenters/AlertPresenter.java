package volpis.com.garadget.mvp.presenters;


import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;

import volpis.com.garadget.interfaces.AlertsMVP;
import volpis.com.garadget.mvp.models.AlertModel;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorLocation;

public class AlertPresenter implements AlertsMVP.PresenterOps, AlertsMVP.RequiredPresenterOps {
    private WeakReference<AlertsMVP.RequiredViewOps> mView;
    private AlertsMVP.ModelOps mModel;

    public AlertPresenter(Context context, AlertsMVP.RequiredViewOps view) {
        mView = new WeakReference<>(view);
        mModel = new AlertModel(context, this);
    }

    @Override
    public void onDestroy(boolean isChangingConfig) {

    }

    @Override
    public void moveMap(LatLng latLng) {
        mView.get().moveMap(latLng);
    }

    @Override
    public void fillData(Door door, DoorLocation doorLocation) {
        mView.get().fillData(door, doorLocation);
    }

    @Override
    public void fillRadiusText(int radius) {
        mView.get().fillRadiusText(radius);
    }

    @Override
    public void showMarkers(String doorName, LatLng doorLatLng, double radius) {
        mView.get().showMarkers(doorName, doorLatLng, radius);
    }

    @Override
    public void notifyBackend(boolean toggleStatus) {
        String action = mView.get().getAction(toggleStatus);
        mModel.notifyBackend(action);
    }

    @Override
    public void setLocationChangedListener() {
        mModel.setLocationChangedListener();
    }

    @Override
    public void writeMapData(LatLng latLng) {
        mModel.writeMapData(latLng);
    }

    @Override
    public void removeDoorLocation() {
        mModel.removeDoorLocation();
    }

    @Override
    public void setDoor(Door door) {
        mModel.setDoor(door);
    }

    @Override
    public void updateConfig(String newConfig) {
        mModel.updateConfig(newConfig);
    }

    @Override
    public void onMapReady() {
        mModel.onMapReady();
    }

    @Override
    public void radiusSelected(int radius) {
        mModel.radiusSelected(radius);
    }

    @Override
    public void flipBit(int position) {
        mModel.flipBit(position);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mModel.onMapLongClick(latLng);
    }

    @Override
    public void setSelectedRadius(int radius) {
        mModel.setSelectedRadius(radius);
    }

    @Override
    public void showToast(String msg) {

    }

    @Override
    public void onUpdatesSaved(String message) {
        mView.get().showToast(message);
    }

    @Override
    public void onError(String errorMsg) {
        mView.get().showToast(errorMsg);
    }

}
