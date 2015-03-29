package classes.example.drawingame.room_activity.service;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TimerTask;

import classes.example.drawingame.data_base.BanChecker;
import classes.example.drawingame.data_base.BanRemover;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.data_base.RoomsGetter;
import classes.example.drawingame.room_activity.RoomActivity;
import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.utils.Utils;

/**
 * Created by A on 30.12.2014.
 */
public class ServiceTimer extends TimerTask {
   public static boolean isDialogShown = false;
   private String notificationMessage;
   private Context context;

   public ServiceTimer(Context context, String notificationMessage) {
      this.context = context;
      this.notificationMessage = notificationMessage;
   }

   @Override
   public void run() {
      final SharedPreferences preferences = context.
              getSharedPreferences("preferences", Context.MODE_PRIVATE);
      if (preferences.contains(BanChecker.BAN_REASON)) {
         if (Utils.banDateHasPast(preferences.getString(BanChecker.UNBAN_DATE, null)))
            new BanRemover(new BanRemover.OnBanRemovedListener() {
               @Override
               public void onBanRemoved(String response) {
                  if (response.equals("removed")) {
                     preferences.edit().remove(BanChecker.BAN_REASON);
                     preferences.edit().remove(BanChecker.UNBAN_DATE);
                  }
               }
            }).start(context);
      } else
         new BanChecker(new BanChecker.OnBanCheckedListener() {
            @Override
            public void onBanChecked(String result, String unbanDate) {
               switch (result) {
                  case BanChecker.CHECK_RESULT_OK:
                     break;
                  case BanChecker.CHECK_RESULT_ERROR:
                     return;
                  default:
                     preferences.edit().putString(BanChecker.BAN_REASON, result).commit();
                     preferences.edit().putString(BanChecker.UNBAN_DATE, unbanDate).commit();
                     break;
               }

            }
         }).start(context);
      new RoomsGetter(new RoomsGetter.OnRoomsGotListener() {
         @Override
         public void onRoomsGot(final ArrayList<Item> dbList) {
            if (dbList == null) return;
            LinkedHashMap<String, Item> map = ItemList.getMap();
            boolean theSame = true;
            String idNotify = null;
            if (ItemList.list.size() == dbList.size())
               for (Item dbItem : dbList) {
                  Item item = map.get(dbItem.roomId);
                  if (item == null || !item.roomImgUrl.equals(dbItem.roomImgUrl))
                     theSame = false;
                  if (item != null
                          && !item.roomImgUrl.equals(dbItem.roomImgUrl)
                          && item.lastEditorDeviceId.equals(DataBase.thisDeviceId)
                          && !dbItem.lastEditorDeviceId.equals(DataBase.thisDeviceId)) {
                     idNotify = item.roomId;
                  }
               }
            else
               theSame = false;

            if (ItemList.list.isEmpty() || ItemList.isOnProgress() || ItemList.listIsSetting
                    || RoomActivity.isNotified || theSame) return;
            if (idNotify != null)
               ListService.sendNotification(notificationMessage, idNotify);
            ListService.sendMessageShowPb();
            ItemList.listIsSetting = true;
            ItemList.setNewList(context, dbList, null);
         }
      }).start(context);
   }

}