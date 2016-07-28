package volpis.com.garadget.mvp.presenters;

import android.content.Context;

import java.lang.ref.WeakReference;

import volpis.com.garadget.interfaces.SettingsMVP;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.mvp.models.SettingModel;
import volpis.com.garadget.mvp.views.SettingsActivity;

public class SettingPresenter implements SettingsMVP.RequiredPresenterOps, SettingsMVP.PresenterOps {

    private WeakReference<SettingsMVP.RequiredViewOps> mView;
    private SettingsMVP.ModelOps mModel;

    public SettingPresenter(Context context, SettingsMVP.RequiredViewOps mView) {
        this.mView = new WeakReference<>(mView);
        this.mModel = new SettingModel(context, this);
    }

    /**
     * Receives {@link SettingsActivity#onDestroy()} event
     *
     * @param isChangingConfig Config change state
     */
    @Override
    public void onDestroy(boolean isChangingConfig) {
        mView = null;
        if (!isChangingConfig) {
            mModel.onDestroy();
        }
    }

    @Override
    public void updateConfig(Door door, String config) {
        if (mView != null && mView.get() != null)
            mView.get().showProgressBar(true);
        mModel.updateConfig(door.getDevice().getID(), config);
    }

    @Override
    public void updateName(String doorId, String name) {
        mModel.setDeviceName(doorId, name);
    }


    @Override
    public void onUpdatesSeved() {
        if (mView != null && mView.get() != null)
            mView.get().showProgressBar(false);
    }


    @Override
    public void onError(String errorMsg) {
        if (mView != null && mView.get() != null)
            mView.get().showProgressBar(false);
    }


}
