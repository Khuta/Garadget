package volpis.com.garadget.mvp.views;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.ui.Toaster;
import volpis.com.garadget.R;
import volpis.com.garadget.interfaces.DoorsMVP;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorHolder;
import volpis.com.garadget.mvp.presenters.DoorsFragmentPresenter;
import volpis.com.garadget.utils.StatusConstants;
import volpis.com.garadget.utils.Utils;
import volpis.com.garadget.screens.MainActivity;

public class DoorsFragment extends Fragment implements DoorsMVP.RequiredViewOps {
    private DoorsFragmentPresenter mPresenter;
    private LayoutInflater mLayoutInflater;

    @Bind(R.id.linear_selected_door)
    LinearLayout linearSelectedDoor;
    @Bind(R.id.linear_doors)
    LinearLayout linearDoors;
    @Bind(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefresh;
    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;

    ArrayList<DoorHolder> mDoorHolders = new ArrayList<>();

    int mSelectedDoorPosition = 0;

    Handler timerHandler;
    Runnable timerRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayoutInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_doors, null);
        ButterKnife.bind(this, view);
        mPresenter = new DoorsFragmentPresenter(getActivity(), this);
        setListeners();
        mPresenter.getListOfDevices(mDoorHolders);
        return view;
    }

    private void setListeners() {
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.getListOfDevices(mDoorHolders);
            }
        });
    }

    private int getScreenWidth() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public void setDoors(final ArrayList<Door> doors, final List<ParticleDevice> devices) {
        if (getActivity() != null) {
            linearDoors.removeAllViews();
            linearSelectedDoor.removeAllViews();

            if (mDoorHolders.size() == 0) {
                for (final Door door : doors) {
                    final View view = mLayoutInflater.inflate(R.layout.item_door, null);
                    DoorHolder doorHolder = initDoorView(view, door);
                    mDoorHolders.add(doorHolder);
                }
            }

            for (final DoorHolder doorHolder : mDoorHolders) {
                final View view = doorHolder.getView();

                if (mDoorHolders.indexOf(doorHolder) != mSelectedDoorPosition) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(getScreenWidth() / 6, getScreenWidth() / 6);
                            params.gravity = Gravity.CENTER;
                            doorHolder.customProgressBar.setLayoutParams(params);
                            doorHolder.customProgressBar.setProgressWidth(5);
                            doorHolder.customProgressBar.setOuterWidth(2);
                        }
                    });
                    view.setPadding(Utils.getPixelsFromDp(getActivity(), 4), 0, Utils.getPixelsFromDp(getActivity(), 4), 0);
                    linearDoors.addView(view);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSelectedDoorPosition = mDoorHolders.indexOf(doorHolder);
                            setDoors(((MainActivity) getActivity()).getDoors(), devices);
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(getScreenWidth() / 3, getScreenWidth() / 3);
                            params.gravity = Gravity.CENTER;
                            doorHolder.customProgressBar.setLayoutParams(params);
                            doorHolder.customProgressBar.setProgressWidth(12);
                            doorHolder.customProgressBar.setOuterWidth(15);
                        }
                    });
                    view.setPadding(Utils.getPixelsFromDp(getActivity(), 55), 0, Utils.getPixelsFromDp(getActivity(), 55), 0);
                    linearSelectedDoor.addView(view);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (doorHolder.getDoor() != null && doorHolder.getDoor().getDoorConfig() != null )
                                if (doorHolder.getStatusChangeTime() + doorHolder.getDoor().getDoorConfig().getDoorMovingTime() < System.currentTimeMillis()) {
                                    String status = doorHolder.getDoor().getDoorStatus().getStatus();
                                    startAnimation(doorHolder, (ImageView) v.findViewById(R.id.image_door), !status.equals(StatusConstants.OPEN), doorHolder.getDoor().getDoorConfig().getDoorMovingTime());
                                    doorHolder.setStatusChangeTime(System.currentTimeMillis());
                                    mPresenter.changeDoorStatus(devices.get(mSelectedDoorPosition), doorHolder, status.equals(StatusConstants.OPEN) ? StatusConstants.CLOSED : StatusConstants.OPEN);
                                }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void showSwipeRefresh(boolean show) {
        swipeRefresh.setRefreshing(show);
    }

    @Override
    public void showProgress(boolean show) {
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showToast(String message) {
        Toaster.l(getActivity(), message);
    }

    private DoorHolder initDoorView(final View view, final Door door) {
        DoorHolder doorHolder = new DoorHolder(view);
        doorHolder.fillView(getActivity(), door);
        return doorHolder;
    }

    public void startAnimation(final DoorHolder doorHolder, final ImageView imageView, boolean isOpening, long openingTime) {

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

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (doorHolder != null && doorHolder.getDoor() != null && doorHolder.getDoor().getDoorConfig() != null) {
                        doorHolder.customProgressBar.setMillis(doorHolder.getDoor().getDoorConfig().getDoorMovingTime());
                        final int[] currentFrame = {0};
                        final Handler handlerAnim = new Handler();
                        final Runnable runnableAnim = new Runnable() {
                            @Override
                            public void run() {
                                if (currentFrame[0] < sortedAnimationImages.size()) {
                                    if (isAdded()) {
                                        final int resourceId = getResources().getIdentifier(sortedAnimationImages.get(currentFrame[0]), "drawable", getActivity().getPackageName());
                                        imageView.setImageDrawable(getResources().getDrawable(resourceId));
                                        currentFrame[0]++;
                                        handlerAnim.postDelayed(this, frameDuration);
                                    }
                                }
                            }
                        };
                        handlerAnim.post(runnableAnim);
                    }
                }
            });
        }
    }


    public int getSelectedDoorPosition() {
        return mSelectedDoorPosition;
    }

    private void startTimerHandler() {
        stopTimerHandler();
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                for (DoorHolder doorHolder : mDoorHolders) {
                    Door door = doorHolder.getDoor();
                    long lastContactMillis = System.currentTimeMillis() - door.getDevice().getLastHeard().getTime();
                    doorHolder.textStatus.setText(Utils.toFormattedTime(lastContactMillis));
                }
                timerHandler.postDelayed(timerRunnable, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimerHandler() {
        if (timerHandler != null && timerRunnable != null)
            timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimerHandler();
    }

}
