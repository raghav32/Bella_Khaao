package com.proyek.rahmanjai.eatitserver.Helper;



import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.proyek.rahmanjai.eatitserver.R;

public class NotificationHelper extends ContextWrapper {

    private static final String Oop_CHANNEL_ID = "com.androideatit.OopMini";
    private static final String Oop_CHANNEL_NAME = "OopMini";

    private NotificationManager notificationManager;


    public NotificationHelper(Context base) {
        super(base);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel notificationChannel=new NotificationChannel(Oop_CHANNEL_ID,
                Oop_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.enableLights(false);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager() {
        if(notificationManager==null)
            notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getEatItChannelNotification(String title, String body, PendingIntent contentIntent){

        return new Notification.Builder(getApplicationContext(),Oop_CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false);
    }
}

