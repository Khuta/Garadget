package volpis.com.garadget.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import volpis.com.garadget.App;
import volpis.com.garadget.R;
import volpis.com.garadget.fragments.DoorFragment;
import volpis.com.garadget.models.DoorWearModel;


public class ActionActivity extends Activity {
    private DoorWearModel mDoorWearModel;
    LinearLayout llChangeStatus;
    LinearLayout llSettings;
    LinearLayout llAlerts;
    TextView tvDoorAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_actions);
        mDoorWearModel = (DoorWearModel) getIntent().getSerializableExtra("doorData");
        llChangeStatus = (LinearLayout) findViewById(R.id.ll_change_status);
        llSettings = (LinearLayout) findViewById(R.id.ll_settings);
        llAlerts = (LinearLayout) findViewById(R.id.ll_alerts);
        tvDoorAction = (TextView) findViewById(R.id.tv_door_action);
        showActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().setCurrentActivity(this);
    }

    public void showActions() {
        //check current door status and change textView
        if (mDoorWearModel.getStatusChangeTime() + mDoorWearModel.getDoorMovingTime() + 1000 < System.currentTimeMillis()) {
            tvDoorAction.setText(mDoorWearModel.isOpened() ? getString(R.string.close_door) : getString(R.string.open_door));
            llChangeStatus.setAlpha(1f);
        } else {
            tvDoorAction.setText(mDoorWearModel.isOpened() ? "Opening" : "Closing");
            llChangeStatus.setEnabled(false);
            llChangeStatus.setAlpha(0.5f);
        }
        llChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(DoorFragment.CHANGE_DOOR_CONDITION, new Intent());
                finish();
            }
        });
        llSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActionActivity.this, SettingsActivity.class);
                intent.putExtra("doorData", mDoorWearModel);
                startActivityForResult(intent, 200);
            }
        });
        llAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActionActivity.this, AlertsActivity.class);
                intent.putExtra("doorData", mDoorWearModel);
                startActivityForResult(intent, 201);
            }
        });
    }

    /**
     * find door by id and show data
     */
    public void fillDoors(ArrayList<DoorWearModel> doorWearModels) {
        for (DoorWearModel doorWearModel : doorWearModels) {
            if (doorWearModel.getDoorId().equals(mDoorWearModel.getDoorId())) {
                mDoorWearModel = doorWearModel;
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 || requestCode == 201) {
            mDoorWearModel = (DoorWearModel) data.getSerializableExtra("door"); //rewrite door because it can be changed in settings or alerts
        }
    }
}
