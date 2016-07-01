package volpis.com.garadget.utils;

import android.content.Context;
import android.content.SharedPreferences;

import volpis.com.garadget.App;

public class SharedPreferencesUtils {

    private static final String SHARED_PREFERENCES_KEY_TAG = App.getInstance().getPackageName();
    public static final String SENT_TOKEN_TO_SERVER = App.getInstance().getPackageName() + "." + "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = App.getInstance().getPackageName() + "." + "registrationComplete";
    public static final String REGISTRATION_TOKEN = App.getInstance().getPackageName() + "." + "registrationToken";
    public static final String SUBSCRIBED_FOR_EVENTS = App.getInstance().getPackageName() + "." + "subscribed_for_events";

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static volatile SharedPreferencesUtils instance;

    public static SharedPreferencesUtils getInstance() {
        SharedPreferencesUtils localInstance = instance;
        if (localInstance == null) {
            synchronized (SharedPreferencesUtils.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new SharedPreferencesUtils(App.getInstance().getApplicationContext());
                }
            }
        }
        return localInstance;
    }

    private SharedPreferencesUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY_TAG, context.MODE_PRIVATE);
    }

    public String getRegistrationToken() {
        return sharedPreferences.getString(REGISTRATION_TOKEN, "");
    }

    public void setRegistrationToken(String registrationToken) {
        sharedPreferences.edit().putString(REGISTRATION_TOKEN, registrationToken).commit();
    }

    public boolean isSubscribedForEvents() {
        return sharedPreferences.getBoolean(SUBSCRIBED_FOR_EVENTS, false);
    }

    public void setSubscribedToEvents(boolean b) {
        sharedPreferences.edit().putBoolean(SUBSCRIBED_FOR_EVENTS, b).commit();
    }

}
