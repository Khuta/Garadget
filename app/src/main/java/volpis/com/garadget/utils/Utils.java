package volpis.com.garadget.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import volpis.com.garadget.R;

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

    public static boolean isJson(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    public static boolean haveInternet(Context context) {
        if (context == null) {
            return false;
        }

        NetworkInfo info = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        if (info.isRoaming()) {
            // here is the roaming option you can change it if you want to disable
            // internet while roaming, just return false
            return true;
        }
        return true;
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static byte getBit(int number, int position) {
        return (byte) ((number >> position) & 1);
    }

    /*
   @param timeAfterMidnight - in minutes
    */
    public static String getTimeInString(int timeAfterMidnight) {
        int hour = timeAfterMidnight / 60;
        int minute = timeAfterMidnight - (hour * 60);
        boolean isAm = (hour < 12);
        if (!isAm)
            hour -= 12;
        return ((hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute) + " " + (isAm ? "AM" : "PM"));
    }

    public static Drawable getSignalStrengthDrawable(Context context, Integer dB) {
        int imageResource;
        if (dB == null)
            imageResource = R.drawable.ic_signal_01;
        else if (dB < -99)
            imageResource = R.drawable.ic_signal_02;
        else if (dB < -84)
            imageResource = R.drawable.ic_signal_03;
        else if (dB < -75)
            imageResource = R.drawable.ic_signal_04;
        else if (dB < -59)
            imageResource = R.drawable.ic_signal_05;
        else
            imageResource = R.drawable.ic_signal_06;
        if (context != null)
            return context.getResources().getDrawable(imageResource);
        else {
            Log.d("myLogs", "getSignalStrengthDrawable NULL");
            return null;
        }
    }



    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
