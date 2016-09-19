package volpis.com.garadget.models;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.globalclasses.CustomProgressBar;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import volpis.com.garadget.R;

import com.example.globalclasses.StatusConstants;

import volpis.com.garadget.utils.Utils;

public class DoorHolder {
    @Bind(R.id.image_door)
    public ImageView imageDoor;
    @Bind(R.id.image_signal)
    public ImageView imageSignal;
    @Bind(R.id.progress_door)
    public CustomProgressBar customProgressBar;
    @Bind(R.id.text_name)
    public TextView textName;
    @Bind(R.id.text_status)
    public TextView textStatus;

    Door mDoor;
    View mView;

    long statusChangeTime;

    public DoorHolder(View v) {
        mView = v;
        ButterKnife.bind(this, v);
    }

    public void fillView(Context context, Door door) {
        mDoor = door;
        String doorName = mDoor.getName();
        if (doorName != null)
            textName.setText(mDoor.getName().replace("_", " "));

        DoorStatus doorStatus = door.getDoorStatus();
        if (door.getDevice().isConnected()) {
            if (doorStatus != null && doorStatus.getStatus() != null) {
                textStatus.setText(doorStatus.getStatus() + " " + doorStatus.getTime());
                Picasso.with(context).load(doorStatus.getStatus().equals(StatusConstants.OPEN) ? R.drawable.ic_anim_garage_15 : R.drawable.ic_anim_garage_01).into(imageDoor);
                Picasso.with(context).load(Utils.getSignalStrengthDrawable(context, door.getDoorStatus().getSignalStrength())).into(imageSignal);
                //   imageDoor.setImageResource(doorStatus.getStatus().equals(StatusConstants.OPEN) ? R.drawable.ic_anim_garage_15 : R.drawable.ic_anim_garage_01);
                //   imageSignal.setImageDrawable(Utils.getSignalStrengthDrawable(context, door.getDoorStatus().getSignalStrength()));
            }
        } else {
            long lastContactMillis = System.currentTimeMillis() - door.getDevice().getLastHeard().getTime();
            if (context != null)
                textStatus.setText(context.getString(R.string.offline) + " " + Utils.toFormattedTime(lastContactMillis));
            // imageSignal.setImageDrawable(Utils.getSignalStrengthDrawable(context, null));
            Picasso.with(context).load(Utils.getSignalStrengthDrawable(context, null)).into(imageSignal);

        }
    }

    public Door getDoor() {
        return mDoor;
    }

    public View getView() {
        return mView;
    }

    public void setStatusChangeTime(long statusChangeTime) {
        this.statusChangeTime = statusChangeTime;
    }

    public long getStatusChangeTime() {
        return statusChangeTime;
    }

    Handler mHandlerAnim;
    Runnable mRunnableAnim;
    public boolean shouldRun = false;

    public void startAnim(Handler handlerAnim, Runnable runnableAnim) {
        stopAnim();
        mRunnableAnim = runnableAnim;
        mHandlerAnim = handlerAnim;
        shouldRun = true;
        mHandlerAnim.post(mRunnableAnim);
    }

    public void stopAnim() {
        shouldRun = false;
        if (mHandlerAnim != null && mRunnableAnim != null) {
            mHandlerAnim.removeCallbacks(mRunnableAnim);
            mHandlerAnim = null;
            mRunnableAnim = null;
        }
    }

}
