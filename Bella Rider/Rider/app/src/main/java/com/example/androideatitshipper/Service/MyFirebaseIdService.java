package com.example.androideatitshipper.Service;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.example.androideatitshipper.Common.Common;
import com.example.androideatitshipper.Helper.NotificationHelper;
import com.example.androideatitshipper.HomeActivity;
import com.example.androideatitshipper.MainActivity;
import com.example.androideatitshipper.Model.Token;
import com.example.androideatitshipper.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;


public class MyFirebaseIdService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        String refreshedToken= FirebaseInstanceId.getInstance().getToken();
        updateToServer(refreshedToken);
    }
    private void updateToServer(String refreshedToken) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token data=new Token(refreshedToken,true);   //true because server side
        tokens.child(Common.currentShipper.getPhone()).setValue(data);
    }
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            sendNotificationAPI26(remoteMessage);
        else
            sendNotification(remoteMessage);
    }
    private void sendNotificationAPI26(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification=remoteMessage.getNotification();
        String title=notification.getTitle();
        String content=notification.getBody();

        Intent intent =new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        NotificationHelper helper=new NotificationHelper(this);
        Notification.Builder builder=helper.getEatItChannelNotification(title,content,pendingIntent);

        helper.getManager().notify(new Random().nextInt(),builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification=remoteMessage.getNotification();
        Intent intent =new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_local_shipping_black_24dp)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager noti=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(0,builder.build());
    }


}
