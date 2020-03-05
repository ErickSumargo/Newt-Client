package app.newt.id.background.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.model.User;
import app.newt.id.view.activity.HomeActivity;

/**
 * Created by Erick Sumargo on 2/6/2017.
 */

public class FCMService extends FirebaseMessagingService {
    private String dateFormat = "dd MMMM yyyy, HH:mm";

    @Override
    public void onNewToken(String mToken) {
        super.onNewToken(mToken);

        Session.with(this).saveFirebaseToken(mToken);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        pushNotification(remoteMessage);
    }

    public void pushNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification.Builder notificationBuilder = new Notification.Builder(this);
        if (Build.VERSION.SDK_INT >= 21) {
            notificationBuilder.setColor(getResources().getColor(R.color.colorPrimary));
        }
        if (Build.VERSION.SDK_INT > 15) {
            String content = "";
            if (Integer.valueOf(remoteMessage.getData().get("type")) == 0) {
                content = remoteMessage.getData().get("body");
            } else {
                if (Session.with(this).getUserType().equals(Constant.STUDENT)) {
                    final String subscription = remoteMessage.getData().get("body");
                    content = "Terima kasih. Masa langganan anda telah diperpanjang s/d " + Utils.with(this).formatDate(subscription, dateFormat) + " WIB";

                    User user = Session.with(this).getUser();
                    user.setSubscription(subscription);

                    Session.with(this).saveUser(user);
                }
            }
            notificationBuilder.setSmallIcon(R.drawable.ic_notif)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setContentText(content)
                    .setStyle(new Notification.BigTextStyle(notificationBuilder)
                            .bigText(content)
                            .setBigContentTitle(remoteMessage.getData().get("title")))
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setLights(0xff00aeef, 500, 2000)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setContentIntent(pendingIntent);
            try {
                notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            } catch (Exception e) {
                Utils.with(this).playDefSound();
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel("app.newt.id.notification", "Message", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(0xff00aeef);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400});

                notificationBuilder.setChannelId("app.newt.id.notification");
                notificationManager.createNotificationChannel(notificationChannel);
            }
            Notification notification = notificationBuilder.build();
            notificationManager.notify((int) System.currentTimeMillis(), notification);
        }
    }
}