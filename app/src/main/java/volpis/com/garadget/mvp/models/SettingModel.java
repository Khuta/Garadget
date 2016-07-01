package volpis.com.garadget.mvp.models;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;
import volpis.com.garadget.interfaces.SettingsMVP;
import volpis.com.garadget.mvp.presenters.SettingPresenter;
import volpis.com.garadget.utils.FunctionConstants;
import volpis.com.garadget.utils.Utils;

public class SettingModel implements SettingsMVP.ModelOps {
    private SettingsMVP.RequiredPresenterOps mPresenter;
    private Context mContext;

    public SettingModel(Context context, SettingsMVP.RequiredPresenterOps mPresenter) {
        this.mPresenter = mPresenter;
        this.mContext = context;
    }

    @Override
    public void updateConfig(final String doorId, final String newConfig) {
        if (Utils.haveInternet(mContext)) {
            Async.executeAsync(ParticleCloud.get(mContext), new Async.ApiWork<ParticleCloud, Object>() {
                @Override
                public Object callApi(ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                    ArrayList<String> params = new ArrayList<String>();
                    params.add(newConfig);
                    try {
                        ParticleCloudSDK.getCloud().getDevice(doorId).callFunction(FunctionConstants.FUNCTION_SET_CONFIG, params);
                    } catch (ParticleDevice.FunctionDoesNotExistException | ParticleCloudException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return -1;
                }

                @Override
                public void onSuccess(Object value) {
                    mPresenter.onUpdatesSeved();
                }

                @Override
                public void onFailure(ParticleCloudException e) {
                    e.printStackTrace();
                    Log.d("onFailure", e.getBestMessage());
                    mPresenter.onError(e.getBestMessage());
                }
            });
        }else
        {
            mPresenter.onError(null);
        }
    }

    @Override
    public void setDeviceName(final String doorId, final String name) {
        Async.executeAsync(ParticleCloud.get(mContext), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                ParticleCloudSDK.getCloud().getDevice(doorId).setName(name);
                return -1;
            }

            @Override
            public void onSuccess(Object value) {
                mPresenter.onUpdatesSeved();
                updateConfig(doorId, "nme" + "=" + name);
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                e.printStackTrace();
                Log.d("onFailure", e.getBestMessage());
            }
        });
    }



    /**
     * Sent from {@link SettingPresenter#onDestroy(boolean)}
     * Should stop/kill operations that could be running
     * and aren't needed anymore
     */
    @Override
    public void onDestroy() {
        // destroying actions
    }

}
