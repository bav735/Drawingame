package classes.example.drawingame.data_base;

import android.util.Log;

import java.util.LinkedHashMap;

import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.utils.Utils;
import co.realtime.storage.ItemAttribute;
import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;

/**
 * Created by A on 30.12.2014.
 */
public class RoomUpdater {
    private onRoomUpdatedListener listener;

    public RoomUpdater(onRoomUpdatedListener listener) {
        this.listener = listener;
    }

    public void start(Item item, String newUrl) {
        LinkedHashMap<String, ItemAttribute> dbItem = item.dbItem();
        dbItem.put(DataBase.ATTRIBUTE_ROOM_IMG_URL, new ItemAttribute(newUrl));
        dbItem.put(DataBase.ATTRIBUTE_LAST_EDITOR_DEVICE_ID, new ItemAttribute(DataBase.thisDeviceId));
        DataBase.roomTableRef.item(dbItem.get(DataBase.ATTRIBUTE_ROOM_ID)).set(dbItem, new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot itemSnapshot) {
                if (itemSnapshot != null) {
                    listener.onRoomUpdated(true);
                } else {
                    listener.onRoomUpdated(false);
                }
            }
        }, new OnError() {
            @Override
            public void run(Integer integer, String s) {
                listener.onRoomUpdated(false);
            }
        });
    }

    public interface onRoomUpdatedListener {
        void onRoomUpdated(boolean isUpdated);
    }
}
