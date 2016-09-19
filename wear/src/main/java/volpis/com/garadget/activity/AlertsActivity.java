package volpis.com.garadget.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.globalclasses.AlertActionModel;
import com.example.globalclasses.BitStatesConstants;
import com.example.globalclasses.PathConstants;
import com.google.gson.Gson;

import java.util.ArrayList;

import volpis.com.garadget.App;
import volpis.com.garadget.DataLayerListenerService;
import volpis.com.garadget.R;
import volpis.com.garadget.models.DoorWearModel;


public class AlertsActivity extends Activity implements CompoundButton.OnCheckedChangeListener {
    private DoorWearModel mDoorWearModel;
    private int alertStatus;
    Switch switchReboot;
    Switch switchOnline;
    Switch switchOpen;
    Switch switchClosed;
    Switch switchStopped;
    Switch switchOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_alerts);
        mDoorWearModel = (DoorWearModel) getIntent().getSerializableExtra("doorData");
        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                switchReboot = (Switch) findViewById(R.id.switch_reboot);
                switchOnline = (Switch) findViewById(R.id.switch_online);
                switchOpen = (Switch) findViewById(R.id.switch_open);
                switchClosed = (Switch) findViewById(R.id.switch_closed);
                switchStopped = (Switch) findViewById(R.id.switch_stopped);
                switchOffline = (Switch) findViewById(R.id.switch_offline);
                fillData();
                setOnCheckedChangeListener();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().setCurrentActivity(this);
    }

    private void setOnCheckedChangeListener() {
        switchReboot.setOnCheckedChangeListener(this);
        switchOnline.setOnCheckedChangeListener(this);
        switchOpen.setOnCheckedChangeListener(this);
        switchClosed.setOnCheckedChangeListener(this);
        switchStopped.setOnCheckedChangeListener(this);
        switchOffline.setOnCheckedChangeListener(this);
    }

    private void removeOnCheckedChangeListener() {
        switchReboot.setOnCheckedChangeListener(null);
        switchOnline.setOnCheckedChangeListener(null);
        switchOpen.setOnCheckedChangeListener(null);
        switchClosed.setOnCheckedChangeListener(null);
        switchStopped.setOnCheckedChangeListener(null);
        switchOffline.setOnCheckedChangeListener(null);
    }

    private void fillData() {
        if (mDoorWearModel != null) {
            alertStatus = mDoorWearModel.getStatusAlerts();
            switchClosed.setChecked(getBit(alertStatus, BitStatesConstants.STATE_CLOSED) == 1);
            switchOpen.setChecked(getBit(alertStatus, BitStatesConstants.STATE_OPENING) == 1);
            switchStopped.setChecked(getBit(alertStatus, BitStatesConstants.STATE_STOPPED) == 1);
            switchReboot.setChecked(getBit(alertStatus, BitStatesConstants.STATE_INIT) == 1);
            switchOnline.setChecked(getBit(alertStatus, BitStatesConstants.STATE_ONLINE) == 1);
            switchOffline.setChecked(getBit(alertStatus, BitStatesConstants.STATE_OFFLINE) == 1);
            Intent result = new Intent();
            result.putExtra("door", mDoorWearModel);
            setResult(RESULT_OK, result);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.switch_reboot:
                flipBit(BitStatesConstants.STATE_INIT);
                break;
            case R.id.switch_online:
                flipBit(BitStatesConstants.STATE_ONLINE);
                break;
            case R.id.switch_open:
                flipBit(BitStatesConstants.STATE_OPENING);
                break;
            case R.id.switch_closed:
                flipBit(BitStatesConstants.STATE_CLOSED);
                break;
            case R.id.switch_stopped:
                flipBit(BitStatesConstants.STATE_STOPPED);
                break;
            case R.id.switch_offline:
                flipBit(BitStatesConstants.STATE_OFFLINE);
                break;
        }
        AlertActionModel alertActionModel = new AlertActionModel(mDoorWearModel.getDoorId(), alertStatus);
        Gson gson = new Gson();
        String alertConfig = gson.toJson(alertActionModel);
        DataLayerListenerService.sendMessage(PathConstants.UPDATE_ALERT, alertConfig.getBytes());

        Intent result = new Intent();
        result.putExtra("door", mDoorWearModel);
        setResult(RESULT_OK, result);
    }

    public static byte getBit(int number, int position) {
        return (byte) ((number >> position) & 1);
    }

    public void flipBit(int position) {
        alertStatus = alertStatus ^ 1 << position;
    }

    /**find door by id and show data*/
    public void fillDoors(ArrayList<DoorWearModel> doorWearModels) {
        for (DoorWearModel doorWearModel : doorWearModels) {
            if (doorWearModel.getDoorId().equals(mDoorWearModel.getDoorId())) {
                fillDoor(doorWearModel);
                break;
            }
        }
    }

    /**show door data*/
    public void fillDoor(DoorWearModel doorWearModel) {
        if (doorWearModel.getDoorId().equals(mDoorWearModel.getDoorId())) {
            mDoorWearModel = doorWearModel;
            removeOnCheckedChangeListener();
            fillData();
            setOnCheckedChangeListener();
        }
    }

}
