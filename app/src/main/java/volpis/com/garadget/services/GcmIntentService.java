package volpis.com.garadget.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.globalclasses.PathConstants;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.wearable.DataMap;

import java.util.Random;

import volpis.com.garadget.App;
import volpis.com.garadget.R;
import volpis.com.garadget.screens.SplashActivity;

public class GcmIntentService extends IntentService {
    private Random random = new Random();
    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.i("myLogs", "Completed work @ " + SystemClock.elapsedRealtime());
                Log.i("myLogs", "Received: " + extras.toString());
                sendNotification(intent.getExtras().getString("default"));
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public static void sendTestMessage() {

        Context context = App.getInstance().getApplicationContext();
        int notificationId = 101;
        Intent viewIntent = new Intent(context, SplashActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(context, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Demo")
                        .setContentText("It's demo of simple notification")
                        .setContentIntent(viewPendingIntent);

        // instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        // Build the notification and notify it using notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
        sendNotificationToWear("asd");
    }

    private void sendNotification(String msg) {
        if (msg != null) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //  mNotificationManager = NotificationManagerCompat.from(this);
            mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);

//            NotificationCompat.Builder mBuilder =
//                    new NotificationCompat.Builder(this)
//                            .setSmallIcon(R.drawable.ic_launcher)
//                            .setStyle(new NotificationCompat.BigTextStyle()
//                                    .bigText(msg))
//                            .setContentText(msg).setSound(uri);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(msg))
                            .setLocalOnly(true)
                            .setContentText(msg).setSound(uri);


            mBuilder.setContentTitle(getString(R.string.app_name));
            mBuilder.setContentIntent(contentIntent);
            mBuilder.setAutoCancel(true);
            try {
                Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                mBuilder.setLargeIcon(largeIcon);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mNotificationManager.notify(generateId(), mBuilder.build());
            sendNotificationToWear(msg);
        }
    }

    private int generateId() {
        return random.nextInt();
    }


    private static void sendNotificationToWear(String msg) {
        DataMap dataMap = new DataMap();
        dataMap.putString("message", msg);
        DataLayerListenerService.sendDataToWear(PathConstants.NOTIFICATION_PATH, PathConstants.NOTIFICATION_KEY, dataMap);
    }
}