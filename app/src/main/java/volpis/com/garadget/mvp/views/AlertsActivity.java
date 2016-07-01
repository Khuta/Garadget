package volpis.com.garadget.mvp.views;

import android.app.TimePickerDialog;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import volpis.com.garadget.App;
import volpis.com.garadget.R;
import volpis.com.garadget.adapters.SpinnerMapAdapter;
import volpis.com.garadget.interfaces.AlertsMVP;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorLocation;
import volpis.com.garadget.mvp.presenters.AlertPresenter;
import volpis.com.garadget.utils.BitStatesConstants;
import volpis.com.garadget.utils.FunctionConstants;
import volpis.com.garadget.utils.SameItemClickSpinner;
import volpis.com.garadget.utils.Utils;
import volpis.com.garadget.views.PrimaryTouchMapFragment;

public class AlertsActivity extends AppCompatActivity implements AlertsMVP.RequiredViewOps, CompoundButton.OnCheckedChangeListener, OnMapReadyCallback {

    //TODO http://stackoverflow.com/questions/13397933/android-spinner-avoid-onitemselected-calls-during-initialization/13398044#13398044

    private static final int DEFAULT_TIME_FROM = 1320;
    private static final int DEFAULT_TIME_TO = 360;
    private static final int NUMBER_OF_SPINNERS = 3;

    private AlertPresenter mPresenter;
    private int spinnersFirstTime = NUMBER_OF_SPINNERS;
    private Polyline mPolyline;
    private Circle mCircle;
    private PrimaryTouchMapFragment mMapFragment;
    private GoogleMap mGoogleMap;

    private LinkedHashMap<String, Integer> mTimeOutTimes = new LinkedHashMap<>();
    private LinkedHashMap<String, String> mTimezones = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> mRadiuses = new LinkedHashMap<>();

    String[] radiuses;
    /*
     alert for night time start in minutes from midnight
     */
    private int mNightAlertTimeFrom = 0;
    private int mNightAlertTimeTo = 0;
    LatLng mDoorLatLng;

    @Bind(R.id.image_drawer)
    ImageView imageDrawer;
    @Bind(R.id.text_title)
    TextView textTitle;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.switch_reboot)
    Switch switchReboot;
    @Bind(R.id.switch_online)
    Switch switchOnline;
    @Bind(R.id.switch_open)
    Switch switchOpen;
    @Bind(R.id.switch_closed)
    Switch switchClosed;
    @Bind(R.id.switch_stopped)
    Switch switchStopped;
    @Bind(R.id.switch_offline)
    Switch switchOffline;
    @Bind(R.id.switch_timeout_enabled)
    Switch switchTimeout;
    @Bind(R.id.switch_night_time)
    Switch switchNightTime;
    @Bind(R.id.switch_location)
    SwitchCompat switchLocation;
    @Bind(R.id.layout_timeout_times)
    LinearLayout mLayoutTimeoutTimes;
    @Bind(R.id.layout_night_alerts)
    LinearLayout mLayoutNightAlerts;
    @Bind(R.id.layout_location)
    LinearLayout mLayoutLocation;
    @Bind(R.id.spinner_timeout_times)
    Spinner mTimeOutTimesSpinner;
    @Bind(R.id.text_night_alert_time_to)
    TextView mTextTimeTo;
    @Bind(R.id.text_night_alert_time_from)
    TextView mTextTimeFrom;
    @Bind(R.id.spinner_timezones)
    Spinner mTimezonesSpinner;
    @Bind(R.id.spinner_radiuses)
    SameItemClickSpinner mSpinnerRadiuses;
    @Bind(R.id.text_radiuses)
    TextView textRadiuses;
    @Bind(R.id.scroll_alerts)
    ScrollView scrollAlerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        initValues();
        fillArrays();
        setOnClickListeners();
        setAdapters();
        mPresenter = new AlertPresenter(this, this);
        setOnCheckedChangeListener();
    }
    
    private void initValues() {
        radiuses = getResources().getStringArray(R.array.radiuses);
        textTitle.setText(getString(R.string.title_activity_alerts));
        mMapFragment = (PrimaryTouchMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().setCurrentActivity(this);
    }

    private void setOnClickListeners() {
        imageDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mTextTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(mTextTimeFrom);
            }
        });

        mTextTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(mTextTimeTo);
            }
        });
        mSpinnerRadiuses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnersFirstTime <= 0) {
                    int radius = (int) ((Map.Entry) mSpinnerRadiuses.getSelectedItem()).getValue();
                    mPresenter.radiusSelected(radius);
                    switchLocation.setChecked(true);
                }
                spinnersFirstTime--;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mTimezonesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnersFirstTime <= 0) {
                    Object value = ((Map.Entry) adapterView.getSelectedItem()).getValue();
                    mPresenter.updateConfig("tzo=" + value);
                }
                spinnersFirstTime--;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mTimeOutTimesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnersFirstTime <= 0) {
                    if (i == 0) {
                        switchTimeout.setChecked(false);
                        mLayoutTimeoutTimes.setVisibility(View.GONE);
                    }
                    Object value = ((Map.Entry) adapterView.getSelectedItem()).getValue();
                    mPresenter.updateConfig("aot=" + value);
                }
                spinnersFirstTime--;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        textRadiuses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpinnerRadiuses.performClick();
            }
        });
    }

    private void setOnCheckedChangeListener() {
        switchReboot.setOnCheckedChangeListener(this);
        switchOnline.setOnCheckedChangeListener(this);
        switchOpen.setOnCheckedChangeListener(this);
        switchClosed.setOnCheckedChangeListener(this);
        switchStopped.setOnCheckedChangeListener(this);
        switchOffline.setOnCheckedChangeListener(this);
        switchTimeout.setOnCheckedChangeListener(this);
        switchLocation.setOnCheckedChangeListener(this);
        switchNightTime.setOnCheckedChangeListener(this);
    }

    private void removeOnCheckedChangeListener() {
        switchReboot.setOnCheckedChangeListener(null);
        switchOnline.setOnCheckedChangeListener(null);
        switchOpen.setOnCheckedChangeListener(null);
        switchClosed.setOnCheckedChangeListener(null);
        switchStopped.setOnCheckedChangeListener(null);
        switchOffline.setOnCheckedChangeListener(null);
        switchTimeout.setOnCheckedChangeListener(null);
        switchLocation.setOnCheckedChangeListener(null);
        switchNightTime.setOnCheckedChangeListener(null);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.switch_reboot:
                mPresenter.flipBit(BitStatesConstants.STATE_INIT);
                mPresenter.notifyBackend(b);
                break;
            case R.id.switch_online:
                mPresenter.flipBit(BitStatesConstants.STATE_ONLINE);
                mPresenter.notifyBackend(b);
                break;
            case R.id.switch_open:
                mPresenter.flipBit(BitStatesConstants.STATE_OPENING);
                mPresenter.notifyBackend(b);
                break;
            case R.id.switch_closed:
                mPresenter.flipBit(BitStatesConstants.STATE_CLOSED);
                mPresenter.notifyBackend(b);
                break;
            case R.id.switch_stopped:
                mPresenter.flipBit(BitStatesConstants.STATE_STOPPED);
                mPresenter.notifyBackend(b);
                break;
            case R.id.switch_offline:
                mPresenter.flipBit(BitStatesConstants.STATE_OFFLINE);
                mPresenter.notifyBackend(b);
                break;
            case R.id.switch_timeout_enabled:
                if (mTimeOutTimesSpinner.getSelectedItemPosition() == 0)
                    mTimeOutTimesSpinner.setSelection(1);
                showTimeoutLayout(b);
                mPresenter.notifyBackend(b);
                break;
            case R.id.switch_location:
                mLayoutLocation.setVisibility(b ? View.VISIBLE : View.GONE);
                if (!b) {
                    mGoogleMap.clear();
                    mPresenter.removeDoorLocation();
                } else {
                    scrollAlerts.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollAlerts.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                    mSpinnerRadiuses.setSelection(0);
                    mPresenter.setSelectedRadius(Integer.parseInt(radiuses[0]));
                    fillRadiusText(Integer.parseInt(radiuses[0]));
                }
                break;
            case R.id.switch_night_time:
                showNightAlertsLayout(b);
                mPresenter.updateConfig("ans=" + mNightAlertTimeFrom + "|ane=" + mNightAlertTimeTo);
                mPresenter.notifyBackend(b);
                break;
        }
    }

    private void showTimeoutLayout(boolean b) {
        mLayoutTimeoutTimes.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    private void showNightAlertsLayout(boolean b) {
        mLayoutNightAlerts.setVisibility(b ? View.VISIBLE : View.GONE);
        if (b) {
            if (mNightAlertTimeFrom == 0 && mNightAlertTimeTo == 0) {
                mNightAlertTimeFrom = DEFAULT_TIME_FROM;
                mNightAlertTimeTo = DEFAULT_TIME_TO;
            }
            mTextTimeFrom.setText(Utils.getTimeInString(mNightAlertTimeFrom));
            mTextTimeTo.setText(Utils.getTimeInString(mNightAlertTimeTo));
        } else {
            mNightAlertTimeTo = 0;
            mNightAlertTimeFrom = 0;
        }
    }

    private void setAdapters() {
        SpinnerMapAdapter spinnerMapAdapter = new SpinnerMapAdapter(this, android.R.layout.simple_spinner_item, mTimeOutTimes, 0);
        spinnerMapAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTimeOutTimesSpinner.setAdapter(spinnerMapAdapter);

        SpinnerMapAdapter adapter = new SpinnerMapAdapter(this, android.R.layout.simple_spinner_item, mTimezones, 0);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTimezonesSpinner.setAdapter(adapter);

        SpinnerMapAdapter spinnerRadiusesAdapter = new SpinnerMapAdapter(this, android.R.layout.simple_spinner_item, mRadiuses, 0);
        spinnerRadiusesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerRadiuses.setAdapter(spinnerRadiusesAdapter);
    }

    void fillArrays() {
        String[] timeoutTimes = getResources().getStringArray(R.array.alert_timeout_times);
        for (String s : timeoutTimes) {
            String[] keyValue = s.split("\\|");
            mTimeOutTimes.put(keyValue[0], Integer.valueOf(keyValue[1]));
        }

        String[] timezones = getResources().getStringArray(R.array.timezones);
        for (String s : timezones) {
            String[] keyValue = s.split("\\|");
            mTimezones.put(keyValue[0], keyValue[1]);
        }

        String[] radiuses = getResources().getStringArray(R.array.radiuses);
        for (String s : radiuses) {
            mRadiuses.put(s + " " + getString(R.string.meters), Integer.valueOf(s));
        }
    }

    private void showTimePicker(final TextView textView) {
        String timeInView = textView.getText().toString();
        /*
                05:04 AM - length=8
         */
        int selectedHour = 0;
        int selectedMinute = 0;

        if (timeInView.length() == 8 && timeInView.contains(":")) {
            selectedHour = Integer.valueOf(timeInView.subSequence(0, timeInView.indexOf(":")).toString());
            selectedMinute = Integer.valueOf(timeInView.subSequence(timeInView.indexOf(":") + 1, timeInView.indexOf(" ")).toString());
            if (timeInView.subSequence(timeInView.indexOf(" ") + 1, timeInView.length()).equals("PM"))
                selectedHour += 12;
        }
        new TimePickerDialog(AlertsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                switch (textView.getId()) {
                    case R.id.text_night_alert_time_from:
                        mNightAlertTimeFrom = (hour * 60) + minute;
                        textView.setText(Utils.getTimeInString(mNightAlertTimeFrom));
                        mPresenter.updateConfig("ans=" + mNightAlertTimeFrom);
                        break;
                    case R.id.text_night_alert_time_to:
                        mNightAlertTimeTo = (hour * 60) + minute;
                        textView.setText(Utils.getTimeInString(mNightAlertTimeTo));
                        mPresenter.updateConfig("ane=" + mNightAlertTimeTo);
                        break;
                }
                /*
                If these variables are the same, it means that we should disable night alert
                 */
                if (mNightAlertTimeFrom == mNightAlertTimeTo)
                    switchNightTime.setChecked(false);
            }
        }, selectedHour, selectedMinute, false).show();
    }

    public void setDoor(Door door) {
        mPresenter.setDoor(door);
        setOnCheckedChangeListener();
    }

    @Override
    public void fillData(Door door, DoorLocation doorLocation) {
        if (doorLocation == null) {
            switchLocation.setChecked(false);
            mLayoutLocation.setVisibility(View.GONE);
        }
        removeOnCheckedChangeListener();
        int timeOutValue = door.getDoorConfig().getOpenTimeout();
        String timezoneValue = door.getDoorConfig().getTzo();
        int status = door.getDoorConfig().getStatusAlerts();
        switchClosed.setChecked(Utils.getBit(status, BitStatesConstants.STATE_CLOSED) == 1);
        switchOpen.setChecked(Utils.getBit(status, BitStatesConstants.STATE_OPENING) == 1);
        switchStopped.setChecked(Utils.getBit(status, BitStatesConstants.STATE_STOPPED) == 1);
        switchReboot.setChecked(Utils.getBit(status, BitStatesConstants.STATE_INIT) == 1);
        switchOnline.setChecked(Utils.getBit(status, BitStatesConstants.STATE_ONLINE) == 1);
        switchOffline.setChecked(Utils.getBit(status, BitStatesConstants.STATE_OFFLINE) == 1);

        mNightAlertTimeFrom = door.getDoorConfig().getNightTimeStart();
        mNightAlertTimeTo = door.getDoorConfig().getNightTimeEnd();

        if (mNightAlertTimeFrom == mNightAlertTimeTo) {
            switchNightTime.setChecked(false);
            showNightAlertsLayout(false);
        } else {
            switchNightTime.setChecked(true);
            showNightAlertsLayout(true);
            mTextTimeFrom.setText(Utils.getTimeInString(mNightAlertTimeFrom));
            mTextTimeTo.setText(Utils.getTimeInString(mNightAlertTimeTo));
        }

        if (doorLocation != null)
            switchLocation.setChecked(doorLocation.isEnabled());
        else
            switchLocation.setChecked(false);

        switchTimeout.setChecked(timeOutValue != 0);
        showTimeoutLayout(timeOutValue != 0);

        for (int i = 0; i < mTimeOutTimesSpinner.getCount(); i++) {
            if (timeOutValue == ((int) (((Map.Entry) mTimeOutTimesSpinner.getItemAtPosition(i)).getValue()))) {
                mTimeOutTimesSpinner.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < mTimezonesSpinner.getCount(); i++) {
            if (timezoneValue.equals(((Map.Entry) mTimezonesSpinner.getItemAtPosition(i)).getValue())) {
                mTimezonesSpinner.setSelection(i);
                break;
            }
        }

        if (doorLocation != null) {
            mSpinnerRadiuses.setSelection(SettingsActivity.getIndexByValue(radiuses, doorLocation.getRadius()));
            fillRadiusText(doorLocation.getRadius());
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ViewGroup.LayoutParams params = mMapFragment.getView().getLayoutParams();
        params.height = metrics.widthPixels;
        mMapFragment.getView().setLayoutParams(params);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mPresenter.onMapReady();
        mPresenter.setLocationChangedListener();
        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mPresenter.onMapLongClick(latLng);
            }
        });
        mMapFragment.setListener(new PrimaryTouchMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scrollAlerts.requestDisallowInterceptTouchEvent(true);
            }
        });
        mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
                // TODO Auto-generated method stub
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                // TODO Auto-generated method stub
                LatLng latLng = new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude);
                mPolyline.remove();
                mPolyline = mGoogleMap.addPolyline(new PolylineOptions().add(mDoorLatLng, latLng).color(0xFF0000FF));

                Location locationA = new Location("A");
                locationA.setLatitude(mDoorLatLng.latitude);
                locationA.setLongitude(mDoorLatLng.longitude);
                Location locationB = new Location("B");
                locationB.setLatitude(latLng.latitude);
                locationB.setLongitude(latLng.longitude);
                float distance = locationA.distanceTo(locationB);
                mCircle.remove();
                mCircle = mGoogleMap.addCircle(new CircleOptions()
                        .center(mDoorLatLng)
                        .radius(distance)
                        .strokeColor(0xFFFF0000)
                        .fillColor(0x30FF0000)
                        .strokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));
                mPresenter.setSelectedRadius((int) distance);
                fillRadiusText((int) distance);
                mPresenter.writeMapData(mDoorLatLng);
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
                // TODO Auto-generated method stub
                Log.i("System out", "onMarkerDrag...");
            }
        });
    }

    public void showMarkers(String doorName, LatLng doorLatLng, double radius) {
        mDoorLatLng = doorLatLng;
        mGoogleMap.clear();

        mGoogleMap.addMarker(new MarkerOptions()
                .position(mDoorLatLng)
                .title(doorName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        LatLng radiusLatLng = SphericalUtil.computeOffset(mDoorLatLng, radius, 90);

        mGoogleMap.addMarker(new MarkerOptions()
                .position(radiusLatLng)
                .title(getResources().getString(R.string.radius))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))).setDraggable(true);
        mPolyline = mGoogleMap.addPolyline(new PolylineOptions().add(mDoorLatLng, radiusLatLng).color(0xFF0000FF));
        mCircle = mGoogleMap.addCircle(new CircleOptions()
                .center(doorLatLng)
                .radius(radius)
                .strokeColor(0xFFFF0000)
                .fillColor(0x30FF0000)
                .strokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(doorLatLng, 14));
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showAlert(String msg) {

    }

    @Override
    public void showProgressBar(boolean visible) {

    }

    @Override
    public void moveMap(LatLng latLng) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
    }

    @Override
    public String getAction(boolean toggleStatus) {
        String action = null;
        if (toggleStatus)
            action = FunctionConstants.FUNCTION_ADD;
        else if (!switchReboot.isChecked() && !switchOnline.isChecked() && !switchOpen.isChecked() && !switchClosed.isChecked() && !switchStopped.isChecked() && !switchOffline.isChecked() && !switchNightTime.isChecked())
            action = FunctionConstants.FUNCTION_REMOVE;
        return action;
    }

    public void fillRadiusText(int radius) {
        String myCounty = Locale.getDefault().getCountry();
        if (myCounty.equals("US") || myCounty.equals("GB")) {
            if (radius < 1609) {
                textRadiuses.setText(radius + getString(R.string.meters));
            } else {
                textRadiuses.setText(String.format("%.3f", radius / 1609.0) + getString(R.string.miles));
            }
        } else {
            if (radius < 1000) {
                textRadiuses.setText(radius + getString(R.string.meters));
            } else {
                textRadiuses.setText(String.format("%.3f", radius / 1000.0) + getString(R.string.kilometers));
            }
        }
    }

}
