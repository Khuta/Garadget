package volpis.com.garadget.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import volpis.com.garadget.App;
import volpis.com.garadget.R;
import volpis.com.garadget.models.DoorWearModel;


public class SettingsActivity extends Activity {

    TextView textDeviceId;
    TextView editDeviceName;
    TextView textStatus;
    TextView textLastContact;
    TextView textFirmwareVersion;
    TextView textWifiSsid;
    TextView textSignalStrength;
    TextView textIpAddress;
    TextView textGateway;
    TextView textIpMask;
    TextView textMacAddress;

    private DoorWearModel mDoorWearModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);
        mDoorWearModel = (DoorWearModel) getIntent().getSerializableExtra("doorData");
        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                textDeviceId = (TextView) findViewById(R.id.text_device_id);
                editDeviceName = (TextView) findViewById(R.id.edit_device_name);
                textStatus = (TextView) findViewById(R.id.text_status);
                textSignalStrength = (TextView) findViewById(R.id.text_signal_strength);
                textIpAddress = (TextView) findViewById(R.id.text_ip_address);
                textGateway = (TextView) findViewById(R.id.text_gateway);
                textIpMask = (TextView) findViewById(R.id.text_ip_mask);
                textMacAddress = (TextView) findViewById(R.id.text_mac_address);
                textWifiSsid = (TextView) findViewById(R.id.text_wifi_ssid);
                textLastContact = (TextView) findViewById(R.id.text_last_contact);
                textFirmwareVersion = (TextView) findViewById(R.id.text_firmware_version);
                if (mDoorWearModel != null)
                    fillData();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().setCurrentActivity(this);
    }

    private void fillData() {
        textDeviceId.setText(mDoorWearModel.getDoorId());
        editDeviceName.setText(mDoorWearModel.getDoorTitle());
        textStatus.setText(mDoorWearModel.isOpened() ? "opened" : "closed");
        textSignalStrength.setText(mDoorWearModel.getSignalStrengthString());
        textFirmwareVersion.setText(mDoorWearModel.getVersion());
        textIpAddress.setText(mDoorWearModel.getIP());
        textGateway.setText(mDoorWearModel.getGateway());
        textIpMask.setText(mDoorWearModel.getIpMask());
        textMacAddress.setText(mDoorWearModel.getMAC());
        textWifiSsid.setText(mDoorWearModel.getWifiSSID());
        long lastContactMillis = System.currentTimeMillis() - mDoorWearModel.getLastContactMillis();
        long lastContactSecs = TimeUnit.MILLISECONDS.toSeconds(lastContactMillis);
        textLastContact.setText(lastContactSecs + " sec ago");
        Intent result = new Intent();
        result.putExtra("door", mDoorWearModel);
        setResult(RESULT_OK, result);
    }

    /**
     * find door by id and show data
     */
    public void fillDoors(ArrayList<DoorWearModel> doorWearModels) {
        for (DoorWearModel doorWearModel : doorWearModels) {
            if (doorWearModel.getDoorId().equals(mDoorWearModel.getDoorId())) {
                mDoorWearModel = doorWearModel;
                fillData();
                break;
            }
        }
    }

    /**
     * show door data
     */
    public void fillDoor(DoorWearModel doorWearModel) {
        if (doorWearModel.getDoorId().equals(mDoorWearModel.getDoorId())) {
            mDoorWearModel = doorWearModel;
            fillData();
        }
    }

}