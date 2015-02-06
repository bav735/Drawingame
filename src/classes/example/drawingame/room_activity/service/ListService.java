package classes.example.drawingame.room_activity.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.room_activity.RoomActivity;
import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.utils.Utils;

/**
 * Created by A on 25.12.2014.
 */
public class ListService extends Service {
    private final static int SERVICE_ID = 1;
    static Timer timer;
    static ServiceTimer task;
    //    public static int posNotify = -1;
    //    public static boolean isRunning = false;
    private static NotificationManager notificationManager;

    public static void sendNotification(String msg, String id) {
        NotificationCompat.Builder builder = getBuilder(id);
//        builder.setContentTitle(msg);
//        builder.setSubText(msg);
        Log.d("!", "notifying...... " + id);
        builder.setContentText(msg);
        builder.setAutoCancel(true);
        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notificationManager.notify(0, notification);
    }

    private static NotificationCompat.Builder getBuilder(String id) {
        int icon = R.drawable.ic_launcher_complete;
        long when = System.currentTimeMillis();
        Intent intent = new Intent(Utils.appContext, RoomActivity.class);
        intent.putExtra("id", id);
//        intent.setAction(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pending = PendingIntent.getActivity(Utils.appContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(Utils.appContext)
                .setContentTitle(Utils.stringFromRes(R.string.appName))
                .setContentText(Utils.stringFromRes(R.string.serviceStartMessage))
                .setSmallIcon(icon)
                .setContentIntent(pending)
                .setWhen(when)
                .setSound(null);
    }

    public static void stopTimer() {
        task.cancel();
        timer.cancel();
    }

    public static void startTimer() {
        timer = new Timer();
        task = new ServiceTimer();
        timer.scheduleAtFixedRate(task, ServiceTimer.TIMER_SEC * 500, ServiceTimer.TIMER_SEC * 1000);
    }

//    @Override
//    public void onDestroy() {
//        stopForeground(true);
//        super.onDestroy();
//        Log.d("!", "onDestroy");
//    }

    public void onCreate() {
        super.onCreate();
        Log.d("!", "onCreate service");
        if (!Utils.roomActivityExists()) {
            Utils.init(getApplicationContext(), null);
            DataBase.init();
            if (Utils.preferences.contains("list") && ItemList.list == null) {
                Log.d("!", "fromJson from service");
                ItemList.list = new ArrayList<Item>();
                ItemList.fromJson(Utils.preferences.getString("list", null));
            }
        }
        notificationManager = (NotificationManager) Utils.appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        startTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("!", "onStartCommand");
        return START_STICKY;
        //startForeground(SERVICE_ID, getBuilder().build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return (null);
    }
}