package classes.example.drawingame.room_activity.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.room_activity.RoomActivity;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.utils.Utils;

/**
 * Created by A on 25.12.2014.
 */
public class ListService extends Service {
   public static final int MESSAGE_SHOW_PB = 0;
   public static final int MESSAGE_SHOW_LIST = 1;
   public static final int MESSAGE_NOTIFY_ADAPTER = 2;
   public static final int MESSAGE_SHOW_ERROR_DIALOG = 3;
   public static final int MESSAGE_SHOW_RETRY_DIALOG = 4;
   public static final int MESSAGE_INIT = 5;
   public static final int MESSAGE_NOTIFY_SCROLL = 6;
   public static final int TIMER_MSEC = 10 * 1000;
   private static Timer timer;
   private static ServiceTimer task;
   private static NotificationManager notificationManager;
   private static Context context;
   private static Messenger messageHandler;

   public static void sendNotification(String msg, String id) {
      NotificationCompat.Builder builder = getBuilder(id);
//      Log.d("!", "notifying...... " + id);
      builder.setContentText(msg);
      builder.setAutoCancel(true);
      Notification notification = builder.build();
      notification.defaults |= Notification.DEFAULT_SOUND;
      notificationManager.notify(0, notification);
   }

   private static NotificationCompat.Builder getBuilder(String id) {
      int icon = R.drawable.ic_launcher;
      long when = System.currentTimeMillis();
      Intent intent = new Intent(context, RoomActivity.class);
      intent.putExtra("id", id);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
              Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

      PendingIntent pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

      return new NotificationCompat.Builder(context)
              .setContentTitle(Utils.stringFromRes(context, R.string.appName))
              .setContentText(Utils.stringFromRes(context, R.string.serviceStartMessage))
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
      task = new ServiceTimer(context, Utils.stringFromRes(context, R.string.serviceNotificationText));
      timer.scheduleAtFixedRate(task, TIMER_MSEC, TIMER_MSEC);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      Log.d("!", "onDestroy Service");
   }

   @Override
   public IBinder onBind(Intent intent) {
      return (null);
   }

//   public ListService() {
//      super("ListService");
//   }

   public void onCreate() {
      super.onCreate();
      Log.d("!", "onCreate service");
      context = getApplicationContext();
      SharedPreferences preferences = context.
              getSharedPreferences("preferences", Context.MODE_PRIVATE);
      if (preferences.getBoolean("destroyed", true)) {
         DataBase.init(context);
         ItemList.reloadItems(null, context);
         Log.d("!", "reloading from service");
      }
      notificationManager = (NotificationManager) context
              .getSystemService(Context.NOTIFICATION_SERVICE);
      startTimer();
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Log.d("!", "onStartCommand Service");
      super.onStartCommand(intent, flags, startId);
      if (intent != null) {
         Bundle extras = intent.getExtras();
         messageHandler = (Messenger) extras.get("MESSENGER");
         sendMessageInit();
      }
      return START_STICKY;
   }

//   @Override
//   protected void onHandleIntent(Intent intent) {
//      Log.d("!", "handling intent");
//      Bundle extras = intent.getExtras();
//      messageHandler = (Messenger) extras.get("MESSENGER");
//      sendMessageInit();
//   }

   public static void sendMessageShowPb() {
      Message message = Message.obtain();
      message.arg1 = MESSAGE_SHOW_PB;
      try {
         messageHandler.send(message);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void sendMessageShowList() {
      Message message = Message.obtain();
      message.arg1 = MESSAGE_SHOW_LIST;
      try {
         messageHandler.send(message);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void sendMessageNotifyAdapter() {
      Message message = Message.obtain();
      message.arg1 = MESSAGE_NOTIFY_ADAPTER;
      try {
         messageHandler.send(message);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void sendMessageShowErrorDialog(int resId) {
      Message message = Message.obtain();
      message.arg1 = MESSAGE_SHOW_ERROR_DIALOG;
      message.arg2 = resId;
      try {
         messageHandler.send(message);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void sendMessageInit() {
      Message message = Message.obtain();
      message.arg1 = MESSAGE_INIT;
      try {
         messageHandler.send(message);
      } catch (Exception e) {
         Log.d("!", e.toString());
      }
   }

   public static void sendMessageNotifyScroll() {
      Message message = Message.obtain();
      message.arg1 = MESSAGE_NOTIFY_SCROLL;
      try {
         messageHandler.send(message);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}