package volpis.com.garadget.screens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;
import volpis.com.garadget.R;
import volpis.com.garadget.adapters.DrawerAdapter;
import volpis.com.garadget.interfaces.OnLogoutListener;
import volpis.com.garadget.services.DataLayerListenerService;
import volpis.com.garadget.utils.SharedPreferencesUtils;

public abstract class DrawerActivity extends AppCompatActivity {
    public DrawerLayout myDrawerLayout;
    public ListView myDrawerList;
    ArrayList<String> navigations = new ArrayList<>();
    DrawerAdapter mAdapter;
    OnLogoutListener onLogoutListener;

    protected abstract int getLayoutId();

    protected abstract int getButtonId();

    protected abstract Context getContext();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle("");
        myDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        createNav();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(
                AdapterView<?> parent, View view, int position, long id
        ) {
            displayView(position);
        }
    }

    public void displayView(int position) {
        switch (position) {
            case 0: {
                break;
            }
            case 1: {
                logOut();
                break;
            }
            default:
                break;
        }
        myDrawerLayout.closeDrawer(myDrawerList);
    }

    public void createNav() {
        navigations.add(ParticleCloudSDK.getCloud().getLoggedInUsername());
        navigations.add(getString(R.string.logout));

        myDrawerList = (ListView) findViewById(R.id.list_drawer);
        mAdapter = new DrawerAdapter(getContext(), R.layout.item_drawer, navigations);
        myDrawerList.setAdapter(mAdapter);
        myDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private void logOut() {
        if (onLogoutListener != null)
            onLogoutListener.onLogout();

        Async.executeAsync(ParticleCloud.get(DrawerActivity.this), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                ParticleCloudSDK.getCloud().logOut();
                return -1;

            }

            @Override
            public void onSuccess(Object value) {
                SharedPreferencesUtils.getInstance().setSubscribedToEvents(false);
                Toaster.l(DrawerActivity.this, "Logged out");
                Intent intent = new Intent(DrawerActivity.this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                DataLayerListenerService.sendIsLoggedStatus(false);
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                Toaster.l(DrawerActivity.this, e.getBestMessage());
                e.printStackTrace();
                Log.d("onFailure", e.getBestMessage());
            }
        });


    }

    public void setOnLogoutListener(OnLogoutListener onLogoutListener) {
        this.onLogoutListener = onLogoutListener;
    }
}