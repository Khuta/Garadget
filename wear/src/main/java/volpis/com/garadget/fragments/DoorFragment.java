package volpis.com.garadget.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.globalclasses.CustomProgressBar;
import com.example.globalclasses.StatusConstants;

import java.util.ArrayList;
import java.util.Collections;

import volpis.com.garadget.DataLayerListenerService;
import volpis.com.garadget.MainActivity;
import volpis.com.garadget.R;
import volpis.com.garadget.Utils;
import volpis.com.garadget.activity.ActionActivity;
import volpis.com.garadget.models.DoorWearModel;

public class DoorFragment extends Fragment {
    public static final int CHANGE_DOOR_CONDITION = 665;

    DoorWearModel mDoorWearModel;
    ImageView ivDoor;
    ImageView ivSignal;
    TextView tvTitle;
    TextView tvStatus;
    CustomProgressBar progressBar;
    LinearLayout mLinearLayoutDoor;
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null) {
            v = inflater.inflate(R.layout.fragment_door, null);
            ivDoor = (ImageView) v.findViewById(R.id.image_door);
            progressBar = (CustomProgressBar) v.findViewById(R.id.progress_door);
            ivSignal = (ImageView) v.findViewById(R.id.image_signal);
            tvTitle = (TextView) v.findViewById(R.id.text_name);
            tvStatus = (TextView) v.findViewById(R.id.text_status);
            mLinearLayoutDoor = (LinearLayout) v.findViewById(R.id.ll_doors);

            if (mDoorWearModel != null) {
                tvTitle.setText(mDoorWearModel.getDoorTitle());

                if (mDoorWearModel.isConnected()) {
                    if (mDoorWearModel.isMoving())
                        tvStatus.setText(mDoorWearModel.isOpened() ? "opening" + " " + mDoorWearModel.getDoorStatusTime() : "closing" + " " + mDoorWearModel.getDoorStatusTime());
                    else
                        tvStatus.setText(mDoorWearModel.isOpened() ? "open" + " " + mDoorWearModel.getDoorStatusTime() : "closed" + " " + mDoorWearModel.getDoorStatusTime());
                } else {
                    long currentTime = System.currentTimeMillis();
                    long lastContactMillis = currentTime - mDoorWearModel.getLastContactMillis();
                    tvStatus.setText("offline " + Utils.toFormattedTime(lastContactMillis));

                }

                Integer signalStrength = null;
                if (mDoorWearModel.getSignalStrengthString() != null)
                    signalStrength = mDoorWearModel.getSignalStrength();
                if (mDoorWearModel.isOpened()) {
                    ivDoor.setImageResource(R.drawable.ic_anim_garage_15_small);
                    ivSignal.setImageResource(Utils.getSignalStrengthDrawable(getActivity(), signalStrength));
                } else {
                    ivDoor.setImageResource(R.drawable.ic_anim_garage_01_small);
                    ivSignal.setImageResource(Utils.getSignalStrengthDrawable(getActivity(), signalStrength));
                }
            }
            setListeners();
        }
        return v;
    }

    public void startAnimation() {
        boolean isOpening = !mDoorWearModel.isOpened();
        long openingTime = mDoorWearModel.getDoorMovingTime();
        mDoorWearModel.setStatusChangeTime(System.currentTimeMillis());
        mDoorWearModel.setMoving(true);
        mDoorWearModel.setDoorStatusTime("0s");
        mDoorWearModel.setOpened(isOpening);

        if (isAdded()) {
            final String animationImages[] = getResources().getStringArray(R.array.animation_images);
            final ArrayList<String> sortedAnimationImages = new ArrayList<>();
            if (isOpening)
                Collections.addAll(sortedAnimationImages, animationImages);
            else
                for (int i = animationImages.length - 1; i >= 0; i--) {
                    sortedAnimationImages.add(animationImages[i]);
                }

            final int frameDuration = (int) (openingTime / animationImages.length);
            fillDoor(mDoorWearModel, true);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setMillis(mDoorWearModel.getDoorMovingTime());
                    final int[] currentFrame = {0};
                    final Handler handlerAnim = new Handler();
                    final Runnable runnableAnim = new Runnable() {
                        @Override
                        public void run() {
                            if (currentFrame[0] < sortedAnimationImages.size()) {
                                if (isAdded()) {
                                    final int resourceId = getResources().getIdentifier(sortedAnimationImages.get(currentFrame[0]), "drawable", getActivity().getPackageName());
                                    ivDoor.setImageDrawable(getResources().getDrawable(resourceId));
                                    currentFrame[0]++;
                                    handlerAnim.postDelayed(this, frameDuration);
                                }
                            } else {
                                mDoorWearModel.setMoving(false);
                                mDoorWearModel.setDoorStatusTime("0s");
                                fillDoor(mDoorWearModel, true);
                            }
                        }
                    };
                    handlerAnim.post(runnableAnim);
                }
                // }
            });
        }
    }

    private void setListeners() {
        ivDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActionActivity.class);
                intent.putExtra("doorData", mDoorWearModel);
                startActivityForResult(intent, 1);
            }
        });
    }

    public void setDoorWearModel(DoorWearModel doorWearModel) {
        if (mDoorWearModel != null)
            mDoorWearModel.setData(doorWearModel);
        else
            mDoorWearModel = doorWearModel;
    }


    public void fillDoor(DoorWearModel doorWearModel, boolean refill) {
        if (doorWearModel.getDoorId().equals(mDoorWearModel.getDoorId())) {
            mDoorWearModel = doorWearModel;
            if (mDoorWearModel != null) {
                tvTitle.setText(mDoorWearModel.getDoorTitle());

                if (mDoorWearModel.isConnected()) {
                    if (mDoorWearModel.isMoving())
                        tvStatus.setText(mDoorWearModel.isOpened() ? "opening" + " " + mDoorWearModel.getDoorStatusTime() : "closing" + " " + mDoorWearModel.getDoorStatusTime());
                    else
                        tvStatus.setText(mDoorWearModel.isOpened() ? "open" + " " + mDoorWearModel.getDoorStatusTime() : "closed" + " " + mDoorWearModel.getDoorStatusTime());
                } else {
                    long currentTime = System.currentTimeMillis();
                    long lastContactMillis = currentTime - mDoorWearModel.getLastContactMillis();
                    tvStatus.setText("offline " + Utils.toFormattedTime(lastContactMillis));
                }


                Integer signalStrength = null;
                if (mDoorWearModel.getSignalStrengthString() != null)
                    signalStrength = mDoorWearModel.getSignalStrength();
                if (refill) {
                    if (mDoorWearModel.isOpened()) {
                        ivDoor.setImageResource(R.drawable.ic_anim_garage_15_small);
                    } else {
                        ivDoor.setImageResource(R.drawable.ic_anim_garage_01_small);
                    }
                }
                ivSignal.setImageResource(Utils.getSignalStrengthDrawable(getActivity(), signalStrength));
            }
        }
    }

    public DoorWearModel getDoorWearModel() {
        return mDoorWearModel;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == CHANGE_DOOR_CONDITION) {
            startAnimation();
            ((MainActivity) getActivity()).setDoor(mDoorWearModel);
            DataLayerListenerService.changeAppDoorStatus(mDoorWearModel);
        }
    }
}
