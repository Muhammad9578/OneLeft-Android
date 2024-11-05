package com.oneleft.app.utils;

import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.oneleft.app.R;
import com.oneleft.app.activities.RoomActivity;
import com.oneleft.app.activities.RoomActivity2;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            //String roomID = data.get("roomID");
            String gameType = data.get("gameType");

            Intent intent = new Intent(this, gameType.equals("game2") ? RoomActivity2.class : RoomActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.noti_channel_id))
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle(data.get("title"))
                    .setContentText(data.get("body"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1001, builder.build());
        }

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        App.saveFCMToken(MyFirebaseMessagingService.this, s);
    }
}
