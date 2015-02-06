package classes.example.drawingame.room_activity.service;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TimerTask;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.BanChecker;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.data_base.RoomsGetter;
import classes.example.drawingame.room_activity.RoomActivity;
import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.utils.MyAlertDialog;
import classes.example.drawingame.utils.Utils;

/**
 * Created by A on 30.12.2014.
 */
public class ServiceTimer extends TimerTask {
   public static boolean isDialogShown = false;

   @Override
   public void run() {
      new BanChecker(new BanChecker.OnBanCheckedListener() {
         @Override
         public void onBanChecked(boolean isBanned) {
            if (isBanned) {
               Utils.preferences.edit().putBoolean("ban", true).commit();
               Utils.showErrorWithListenerDialog(Utils.stringFromRes(R.string.banMessage), new MyAlertDialog.OnDismissedListener() {
                  @Override
                  public void onDismissed(boolean isPositive) {
                     Utils.roomActivity.finish();
                  }
               });
            } else
               Utils.preferences.edit().putBoolean("ban", false).commit();
         }
      }).start();
      if (ItemList.isOnProgress() || ItemList.listIsSetting || RoomActivity.isNotified) return;
      new RoomsGetter(new RoomsGetter.OnRoomsGotListener() {
         @Override
         public void onRoomsGot(final ArrayList<Item> dbList) {
            if (dbList == null) {
               if (ItemList.list.isEmpty() && !isDialogShown) {
                  if (Utils.roomActivityExists()) isDialogShown = true;
                  Utils.showRetryActionDialog(Utils.stringFromRes(R.string.errorDb), new MyAlertDialog.OnDismissedListener() {
                     @Override
                     public void onDismissed(boolean isPositive) {
                        isDialogShown = false;
                        if (!isPositive)
                           Utils.roomActivity.finish();
                     }
                  });
               }
               return;
            }
            LinkedHashMap<String, Item> map = ItemList.getMap();
            boolean theSame = true;
            if (ItemList.size() == dbList.size())
               for (Item dbItem : dbList) {
                  Item item = map.get(dbItem.roomId);
                  if (item == null || !item.roomImgUrl.equals(dbItem.roomImgUrl))
                     theSame = false;
               }
            else
               theSame = false;

//            Log.d("!", "from service timer, the same="+String.valueOf(theSame)
//                    +", list size="+String.valueOf(ItemList.size())
//                    +", dblist size="+String.valueOf(dbList.size()));
            if (!ItemList.isOnProgress() && !theSame && !ItemList.listIsSetting) {
               String idNotify = null;
               for (Item dbItem : dbList) {
                  Item item = map.get(dbItem.roomId);
                  if (item != null
                          && !item.roomImgUrl.equals(dbItem.roomImgUrl)
                          && item.lastEditorDeviceId.equals(DataBase.thisDeviceId)
                          && !dbItem.lastEditorDeviceId.equals(DataBase.thisDeviceId)) {
                     idNotify = item.roomId;
                  }
               }
               if (idNotify != null) {
                  ListService.sendNotification(Utils.stringFromRes(R.string.serviceNotificationText), idNotify);
               }
               Utils.showProgress();
               ItemList.setNewList(dbList);
            }
         }
      }).start();
   }

}