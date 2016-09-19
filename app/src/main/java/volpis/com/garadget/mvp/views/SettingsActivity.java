package volpis.com.garadget.mvp.views;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import volpis.com.garadget.App;
import volpis.com.garadget.R;
import volpis.com.garadget.adapters.SpinnerMapAdapter;
import volpis.com.garadget.interfaces.SettingsMVP;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorConfig;
import volpis.com.garadget.models.DoorStatus;
import volpis.com.garadget.models.NetConfig;
import volpis.com.garadget.mvp.presenters.SettingPresenter;

public class SettingsActivity extends AppCompatActivity implements SettingsMVP.RequiredViewOps {

    //TODO http://stackoverflow.com/questions/13397933/android-spinner-avoid-onitemselected-calls-during-initialization/13398044#13398044
    private final static int SPINNERS_AMOUNT = 6;
    private int spinnersFirstTime = SPINNERS_AMOUNT;
    private SettingPresenter mPresenter;
    private Door mDoor;
    private LinkedHashMap<String, Integer> mScanPeriods = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> mDoorMotionTimes = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> mRelayOnTimes = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> mRelayOffTimes = new LinkedHashMap<>();
    private TextWatcher titleTextWatcher;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.image_drawer)
    ImageView imageDrawer;
    @Bind(R.id.text_title)
    TextView textTitle;

    @Bind(R.id.layout_settings_device)
    ViewGroup layoutSettingsDevice;
    @Bind(R.id.layout_settings_wifi)
    ViewGroup layoutSettingsWifi;
    @Bind(R.id.layout_settings_sensor)
    ViewGroup layoutSettingsSensor;
    @Bind(R.id.layout_settings_door)
    ViewGroup layoutSettingsDoor;
    @Bind(R.id.linear_firmware_version)
    ViewGroup linearFirmwareVersion;

    @Bind(R.id.text_device_id)
    TextView textDeviceId;
    @Bind(R.id.edit_device_name)
    EditText editDeviceName;
    @Bind(R.id.text_status)
    TextView textStatus;
    @Bind(R.id.text_last_contact)
    TextView textLastContact;
    @Bind(R.id.text_firmware_version)
    TextView textFirmwareVersion;

    @Bind(R.id.text_wifi_ssid)
    TextView textWifiSsid;
    @Bind(R.id.text_signal_strength)
    TextView textSignalStrength;
    @Bind(R.id.text_ip_address)
    TextView textIpAddress;
    @Bind(R.id.text_gateway)
    TextView textGateway;
    @Bind(R.id.text_ip_mask)
    TextView textIpMask;
    @Bind(R.id.text_mac_address)
    TextView textMacAddress;

    @Bind(R.id.text_reflection)
    TextView textReflection;
    @Bind(R.id.spinner_scan_period)
    Spinner mScanPeriodsSpinner;
    @Bind(R.id.spinner_sensor_reads)
    Spinner mSensorReadsSpinner;
    @Bind(R.id.spinner_sensor_threshold)
    Spinner mSensorThresholdSpinner;

    @Bind(R.id.spinner_door_motion_time)
    Spinner mDoorMotionTimeSpinner;
    @Bind(R.id.spinner_relay_on_time)
    Spinner mRelayOnTimeSpinner;
    @Bind(R.id.spinner_relay_off_time)
    Spinner mRelayOffTimeSpinner;
    @Bind(R.id.progress_setting)
    ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        textTitle.setText(getString(R.string.title_activity_settings));
        mPresenter = new SettingPresenter(this, this);
        mDoor = getIntent().getParcelableExtra("door");
        fillArrays();
        setDoorData(mDoor);
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().setCurrentActivity(this);
    }

    void fillArrays() {

        String[] scanPeriods = getResources().getStringArray(R.array.scan_periods);
        String[] doorMotionTimes = getResources().getStringArray(R.array.door_motion_times);
        String[] relayOnTimes = getResources().getStringArray(R.array.relay_on_times);
        String[] relayOffTimes = getResources().getStringArray(R.array.relay_off_times);

        for (String s : scanPeriods) {
            String[] keyValue = s.split("\\|");
            mScanPeriods.put(keyValue[0], Integer.valueOf(keyValue[1]));
        }

        for (String s : doorMotionTimes) {
            String[] keyValue = s.split("\\|");
            mDoorMotionTimes.put(keyValue[0], Integer.valueOf(keyValue[1]));
        }

        for (String s : relayOnTimes) {
            String[] keyValue = s.split("\\|");
            mRelayOnTimes.put(keyValue[0], Integer.valueOf(keyValue[1]));
        }

        for (String s : relayOffTimes) {
            String[] keyValue = s.split("\\|");
            mRelayOffTimes.put(keyValue[0], Integer.valueOf(keyValue[1]));
        }

        SpinnerMapAdapter spinnerMapAdapter = new SpinnerMapAdapter(this, android.R.layout.simple_spinner_item, mScanPeriods, 0);
        spinnerMapAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mScanPeriodsSpinner.setAdapter(spinnerMapAdapter);


        SpinnerMapAdapter spinnerDoorMotionAdapter = new SpinnerMapAdapter(this, android.R.layout.simple_spinner_item, mDoorMotionTimes, 0);
        spinnerDoorMotionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDoorMotionTimeSpinner.setAdapter(spinnerDoorMotionAdapter);

        SpinnerMapAdapter spinnerRelayOnTimeAdapter = new SpinnerMapAdapter(this, android.R.layout.simple_spinner_item, mRelayOnTimes, 0);
        spinnerRelayOnTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRelayOnTimeSpinner.setAdapter(spinnerRelayOnTimeAdapter);

        SpinnerMapAdapter spinnerRelayOffTimeAdapter = new SpinnerMapAdapter(this, android.R.layout.simple_spinner_item, mRelayOffTimes, 0);
        spinnerRelayOffTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRelayOffTimeSpinner.setAdapter(spinnerRelayOffTimeAdapter);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sensor_reads, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSensorReadsSpinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.sensor_thresholds, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSensorThresholdSpinner.setAdapter(adapter1);


    }

    private void setDoorData(Door door) {
        DoorConfig doorConfig = door.getDoorConfig();
        DoorStatus doorStatus = door.getDoorStatus();
        NetConfig netConfig = door.getNetConfig();
        textDeviceId.setText(door.getDevice().getID());
        long lastContactMillis = System.currentTimeMillis() - door.getDevice().getLastHeard().getTime();
        long lastContactSecs = TimeUnit.MILLISECONDS.toSeconds(lastContactMillis);
        textLastContact.setText(lastContactSecs + " sec ago");
        String doorName = mDoor.getName();
        if (doorName != null)
            editDeviceName.setText(mDoor.getName().replace("_", " "));

        if (doorConfig != null) {

            textFirmwareVersion.setText(doorConfig.getVersion());
            String[] scanPeriods = getResources().getStringArray(R.array.scan_periods);
            String[] sensorReads = getResources().getStringArray(R.array.sensor_reads);
            String[] sensorThresholds = getResources().getStringArray(R.array.sensor_thresholds);
            String[] doorMotionTimes = getResources().getStringArray(R.array.door_motion_times);
            String[] relayOnTimes = getResources().getStringArray(R.array.relay_on_times);
            String[] relayOffTimes = getResources().getStringArray(R.array.relay_off_times);
            mScanPeriodsSpinner.setSelection(getIndexByValue(scanPeriods, doorConfig.getSensorScanInterval()));
            mSensorReadsSpinner.setSelection(getIndexByValue(sensorReads, doorConfig.getSensorReadsAmount()));
            mSensorThresholdSpinner.setSelection(getIndexByValue(sensorThresholds, doorConfig.getReflectionThreshold()));
            mDoorMotionTimeSpinner.setSelection(getIndexByValue(doorMotionTimes, doorConfig.getDoorMovingTime()));
            mRelayOnTimeSpinner.setSelection(getIndexByValue(relayOnTimes, doorConfig.getButtonPressTime()));
            mRelayOffTimeSpinner.setSelection(getIndexByValue(relayOffTimes, doorConfig.getButtonPressesDelay()));
        }

        if (doorStatus != null) {
            textStatus.setText(doorStatus.getStatus());
            textReflection.setText(String.valueOf(doorStatus.getReflectionRate()));
            textSignalStrength.setText(doorStatus.getSignalString());
        }
        if (netConfig != null) {
            textIpAddress.setText(netConfig.getIpAddress());
            textGateway.setText(netConfig.getGateway());
            textIpMask.setText(netConfig.getSubnet());
            textMacAddress.setText(netConfig.getMacAddress());
            textWifiSsid.setText(netConfig.getSsid());
        }
        if (!door.getDevice().isConnected()) {
            Toast.makeText(SettingsActivity.this, getString(R.string.currently_offline), Toast.LENGTH_SHORT).show();
            layoutSettingsWifi.setVisibility(View.GONE);
            layoutSettingsSensor.setVisibility(View.GONE);
            layoutSettingsDoor.setVisibility(View.GONE);
            linearFirmwareVersion.setVisibility(View.GONE);
            textStatus.setText(getString(R.string.normal));
        }
    }

    public static int getIndexByValue(String[] data, Integer value) {
        for (int i = 0; i < data.length; i++) {
            String[] splitData = data[i].split("\\|");
            Integer v = splitData.length == 1 ? Integer.valueOf(splitData[0]) : Integer.valueOf(splitData[1]);
            if (v.equals(value)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showAlert(String msg) {
        if (msg != null)
            Toast.makeText(SettingsActivity.this, "ERROR " + msg, Toast.LENGTH_SHORT).show();
        else
            new AlertDialog.Builder(SettingsActivity.this).setTitle(getString(R.string.network_error_title)).setMessage(getString(R.string.network_error_message)).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
    }

    @Override
    public void showProgressBar(boolean visible) {
        mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setListeners() {
        imageDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleTextWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                showProgressBar(true);
                mPresenter.updateName(mDoor.getDevice().getID(), s.toString());
            }
        };

        editDeviceName.addTextChangedListener(titleTextWatcher);

        mScanPeriodsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnersFirstTime <= 0) {
                    Object value = ((Map.Entry) adapterView.getSelectedItem()).getValue();
                    mPresenter.updateConfig(mDoor, "rdt" + "=" + value);
                }
                spinnersFirstTime--;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSensorThresholdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnersFirstTime <= 0) {
                    mPresenter.updateConfig(mDoor, "srt" + "=" + adapterView.getSelectedItem());
                }
                spinnersFirstTime--;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mSensorReadsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnersFirstTime <= 0) {
                    mPresenter.updateConfig(mDoor, "srr" + "=" + adapterView.getSelectedItem());
                }
                spinnersFirstTime--;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mRelayOffTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnersFirstTime <= 0) {
                    Object value = ((Map.Entry) adapterView.getSelectedItem()).getValue();
                    mPresenter.updateConfig(mDoor, "rlp" + "=" + value);
                }
                spinnersFirstTime--;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mRelayOnTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnersFirstTime <= 0) {
                    Object value = ((Map.Entry) adapterView.getSelectedItem()).getValue();
                    mPresenter.updateConfig(mDoor, "rlt" + "=" + value);
                }
                spinnersFirstTime--;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mDoorMotionTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnersFirstTime <= 0) {
                    Object value = ((Map.Entry) adapterView.getSelectedItem()).getValue();
                    mPresenter.updateConfig(mDoor, "mtt" + "=" + value);
                }
                spinnersFirstTime--;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void removeListeners() {
        editDeviceName.removeTextChangedListener(titleTextWatcher);
        mScanPeriodsSpinner.setOnItemSelectedListener(null);
        mSensorThresholdSpinner.setOnItemSelectedListener(null);
        mSensorReadsSpinner.setOnItemSelectedListener(null);
        mRelayOffTimeSpinner.setOnItemSelectedListener(null);
        mRelayOnTimeSpinner.setOnItemSelectedListener(null);
        mDoorMotionTimeSpinner.setOnItemSelectedListener(null);
    }

    public void setDoor(Door door) {
        initSpinnersFirstTimeValue(door);
        mDoor = new Door(door);
        removeListeners();
        setDoorData(mDoor);
        setListeners();
    }

    private void initSpinnersFirstTimeValue(Door door) {
        spinnersFirstTime = 0;
        if (mDoor != null && door.getDoorConfig() != null) {
            if (door.getDoorConfig().getSensorScanInterval() != (int) ((Map.Entry) mScanPeriodsSpinner.getSelectedItem()).getValue())
                spinnersFirstTime++;
            if (door.getDoorConfig().getSensorReadsAmount() != Integer.parseInt((String) mSensorReadsSpinner.getSelectedItem()))
                spinnersFirstTime++;
            if (door.getDoorConfig().getReflectionThreshold() != Integer.parseInt((String) mSensorThresholdSpinner.getSelectedItem()))
                spinnersFirstTime++;
            if (door.getDoorConfig().getDoorMovingTime() != (int) ((Map.Entry) mDoorMotionTimeSpinner.getSelectedItem()).getValue())
                spinnersFirstTime++;
            if (door.getDoorConfig().getButtonPressTime() != (int) ((Map.Entry) mRelayOnTimeSpinner.getSelectedItem()).getValue())
                spinnersFirstTime++;
            if (door.getDoorConfig().getButtonPressesDelay() != (int) ((Map.Entry) mRelayOffTimeSpinner.getSelectedItem()).getValue())
                spinnersFirstTime++;
        } else
            spinnersFirstTime = SPINNERS_AMOUNT;
    }

}
