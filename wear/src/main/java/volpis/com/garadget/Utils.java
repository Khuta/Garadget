package volpis.com.garadget;

import android.content.Context;
import android.util.Log;

public class Utils {

    //time in that status 0-119s, 2-119m, 2-47h, 2+d
    public static String toFormattedTime(long time) {
        if (time <= 120 * 1000)
            return (int) (time / 1000) + " s";
        else if (time <= 120 * 60 * 1000)
            return (int) (time / 1000 / 60) + " m";
        else if (time <= 48 * 60 * 60 * 1000)
            return (int) (time / 1000 / 60 / 60) + " h";
        else
            return (int) (time / 1000 / 60 / 60 / 24) + " d";
    }

    public static int getSignalStrengthDrawable(Context context, Integer dB) {
        int imageResource;
        if (dB == null)
            imageResource = R.drawable.ic_signal_01_small;
        else if (dB < -99)
            imageResource = R.drawable.ic_signal_02_small;
        else if (dB < -84)
            imageResource = R.drawable.ic_signal_03_small;
        else if (dB < -75)
            imageResource = R.drawable.ic_signal_04_small;
        else if (dB < -59)
            imageResource = R.drawable.ic_signal_05_small;
        else
            imageResource = R.drawable.ic_signal_06_small;
        if (context != null)
            return imageResource;
        else {
            Log.d("myLogs", "getSignalStrengthDrawable NULL");
            return -1;
        }
    }

}
