package volpis.com.garadget.fragments;


import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import volpis.com.garadget.R;
import volpis.com.garadget.adapters.DoorGridPagerAdapter;
import volpis.com.garadget.models.DoorWearModel;

public class DoorsPagerFragment extends Fragment {

    View v;
    GridViewPager pager;
    DoorGridPagerAdapter mAdapter;
    BoxInsetLayout mLinearLayoutActions;
    LinearLayout llChangeStatus;
    LinearLayout llSettings;
    LinearLayout llAlerts;
    TextView tvDoorAction;
    private List<ImageView> dots;

    int selectedPage = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null) {
            v = inflater.inflate(R.layout.fragment_doors_pager, null);

            WatchViewStub stub = (WatchViewStub) v.findViewById(R.id.watch_view_stub);
            stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
                @Override
                public void onLayoutInflated(WatchViewStub stub) {
                    pager = (GridViewPager) v.findViewById(R.id.pager);
                    mLinearLayoutActions = (BoxInsetLayout) v.findViewById(R.id.ll_actions);
                    llChangeStatus = (LinearLayout) v.findViewById(R.id.ll_change_status);
                    llSettings = (LinearLayout) v.findViewById(R.id.ll_settings);
                    llAlerts = (LinearLayout) v.findViewById(R.id.ll_alerts);
                    tvDoorAction = (TextView) v.findViewById(R.id.tv_door_action);
                }
            });
        }
        return v;
    }

    public void fillDoors(ArrayList<DoorWearModel> doorWearModels, boolean refill) {
        if (mAdapter == null) {
            DoorWearModel selectedDoorWearModel = null;
            if (mAdapter != null)
                selectedDoorWearModel = ((DoorFragment) mAdapter.getFragment(0, selectedPage)).getDoorWearModel();
            mAdapter = new DoorGridPagerAdapter(getActivity(), getChildFragmentManager(), doorWearModels);
            pager.setAdapter(mAdapter);
            addDots(doorWearModels.size());
            selectDot(selectedPage);
        } else {
            for (int i = 0; i < mAdapter.getColumnCount(0); i++) {
                ((DoorFragment) mAdapter.getFragment(0, i)).fillDoor(doorWearModels.get(i), refill);
            }
        }
    }

    public void fillNewDoorStatus(DoorWearModel changedDoorWearModel) {
        if (mAdapter != null) {
            ArrayList<DoorWearModel> doorWearModels = (ArrayList<DoorWearModel>) mAdapter.getModels();
            //find doorWearModel by id, change status and refillFragment
            for (int i = 0; i < doorWearModels.size(); i++) {
                DoorWearModel doorWearModel = doorWearModels.get(i);
                if (doorWearModel.getDoorId().equals(changedDoorWearModel.getDoorId())) {
                    if (doorWearModel.isOpened() != changedDoorWearModel.isOpened()) {
                        doorWearModels.set(i, changedDoorWearModel);
                        ((DoorFragment) mAdapter.getFragment(0, i)).startAnimation();
                    } else {
                        doorWearModels.set(i, changedDoorWearModel);
                    }
                    ((DoorFragment) mAdapter.getFragment(0, i)).setDoorWearModel(changedDoorWearModel);
                }
            }
        }
    }

    public void addDots(int itemsCount) {
        dots = new ArrayList<>();
        LinearLayout dotsLayout = (LinearLayout) v.findViewById(R.id.dots);
        dotsLayout.removeAllViews();

        for (int i = 0; i < itemsCount; i++) {
            ImageView dot = new ImageView(getActivity());
            dot.setImageDrawable(getResources().getDrawable(R.drawable.gray_ligth_circle));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(2, 0, 2, 0);
            dotsLayout.addView(dot, params);
            dots.add(dot);
        }

        pager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, int i1, float v, float v1, int i2, int i3) {

            }

            @Override
            public void onPageSelected(int i, int i1) {
                selectDot(i1);
                selectedPage = i1;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    public void selectDot(int idx) {
        Resources res = getResources();
        for (int i = 0; i < mAdapter.getColumnCount(0); i++) {
            int drawableId = (i == idx) ? (R.drawable.green_circle) : (R.drawable.gray_ligth_circle);
            Drawable drawable = res.getDrawable(drawableId);
            dots.get(i).setImageDrawable(drawable);
        }
    }


}
