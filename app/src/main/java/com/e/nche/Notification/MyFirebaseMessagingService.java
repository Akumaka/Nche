package com.e.nche.Notification;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.e.nche.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String channelId = "Vicab App";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String complainTitle = remoteMessage.getNotification().getTitle();
        String complainBody = remoteMessage.getNotification().getBody();

        String click_action = remoteMessage.getNotification().getClickAction();

        String dataName = remoteMessage.getData().get("name");
        String dataId = remoteMessage.getData().get("notification_id");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        // Creates an Intent for the Activity
        Intent pendingIntent = new Intent(click_action);
        // Sets the Activity to start in a new, empty task
        pendingIntent.putExtra("notification_id", dataId);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        builder.setSmallIcon(R.mipmap.icon_square)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(complainTitle)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setContentText(complainBody)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);

        int mNotificationId = (int) System.currentTimeMillis();

        builder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(mNotificationId, builder.build());

        Log.println(Log.ASSERT, "Title: ", "" + complainTitle);
        Log.println(Log.ASSERT, "Body: ", "" + complainBody);
        Log.println(Log.ASSERT, "Name: ", "" + dataName);
        Log.println(Log.ASSERT, "Id: ", "" + dataId);

        //showNotification(complainTitle, complainBody, dataId);
    }

    @SuppressLint("WrongConstant")
    /*private void showNotification(String title, String body, String id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        // Creates an Intent for the Activity
        Intent pendingIntent = new Intent(this, NotificationActivity.class);
        // Sets the Activity to start in a new, empty task
        pendingIntent.putExtra("notification_id", id);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        builder.setSmallIcon(R.drawable.app_icon)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText("Complain by -- " + body)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);

        int mNotificationId = (int) System.currentTimeMillis();

        builder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(mNotificationId, builder.build());
    }*/


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
