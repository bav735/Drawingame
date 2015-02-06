package classes.example.drawingame.room_activity.service;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TimerTask;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.data_base.RoomsGetter;
import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.utils.Utils;

/**
 * Created by A on 30.12.2014.
 */
public class ServiceTimer extends TimerTask {
    public static final int TIMER_SEC = 10;

    @Override
    public void run() {
        if (ItemList.list == null || ItemList.isBusy()) return;
        new RoomsGetter(new RoomsGetter.OnRoomsGotListener() {
            @Override
            public void onRoomsGot(final ArrayList<Item> dbList, boolean isError) {
                if (dbList == null) {
                    return;
                }

//                boolean idNotify = false;
                String idNotify = null;
                LinkedHashMap<String, Item> map = ItemList.getMap();
                for (Item dbItem : dbList) {
                    if (map.containsKey(dbItem.roomId)) {
                        Item item = map.get(dbItem.roomId);
                        if (!item.roomImgUrl.equals(dbItem.roomImgUrl) && item.lastEditorDeviceId.equals(DataBase.thisDeviceId)
                                && !dbItem.lastEditorDeviceId.equals(DataBase.thisDeviceId)) {
//                        doNotify = true;
                            idNotify = dbItem.roomId;
                            Log.d("!", "notifying.. "+idNotify);
                        }
                    }
                }
//                if (idNotify)
                if (idNotify != null)
                    ListService.sendNotification(Utils.stringFromRes(R.string.serviceNotificationText), idNotify);

                ItemList.setList(dbList);
            }
        }).start();
    }
}