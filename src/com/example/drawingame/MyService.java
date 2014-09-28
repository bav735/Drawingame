package com.example.drawingame;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.util.concurrent.TimeUnit;

public class MyService extends Service {
    private final int serviceId = 1;
    private NotificationManager nm;

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //pause for 5 sec
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    void sendNotification() {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Context context = getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notification;
        if (Build.VERSION.SDK_INT < 11) {
            notification = new Notification(icon, "Title", when);
            notification.setLatestEventInfo(context, "Title", "Text", pending);
        } else {
            notification = new Notification.Builder(context)
                    .setContentTitle("Title")
                    .setContentText("Text")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pending)
                    .setWhen(when)
                    .setAutoCancel(true)
                    .build();
        }
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        nm.notify(0, notification);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }
}