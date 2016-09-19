package volpis.com.garadget.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.FragmentGridPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import volpis.com.garadget.R;

import volpis.com.garadget.fragments.DoorFragment;
import volpis.com.garadget.models.DoorWearModel;

public class DoorGridPagerAdapter extends FragmentGridPagerAdapter {

    private final Context mContext;
    private List<DoorWearModel> models;
    private List<Fragment> fragments = new ArrayList<>();

    public DoorGridPagerAdapter(Context ctx, FragmentManager fm, ArrayList<DoorWearModel> models) {
        super(fm);
        mContext = ctx;
        this.models = models;
        for (int i = 0; i < models.size(); i++) {
            fragments.add(null);
        }
    }

    @Override
    public Fragment getFragment(int i, int i1) {
        if (fragments.get(i1) != null)
            return fragments.get(i1);
        else {
            DoorFragment fragment = new DoorFragment();
            fragment.setDoorWearModel(models.get(i1));
            fragments.set(i1, fragment);
            return fragment;
        }
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int i) {
        return models.size();
    }

    @Override
    public Drawable getBackgroundForRow(int row) {
        return mContext.getResources().getDrawable(
                (R.drawable.ic_more_horiz_24dp_wht), null);
    }

    public List<DoorWearModel> getModels() {
        ArrayList<DoorWearModel> doorWearModels = new ArrayList<>();
        for (Fragment fragment : fragments) {
            doorWearModels.add(((DoorFragment) fragment).getDoorWearModel());
        }
        return doorWearModels;
    }
}

