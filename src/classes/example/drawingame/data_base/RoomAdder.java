package classes.example.drawingame.data_base;

import java.util.LinkedHashMap;

import classes.example.drawingame.room_activity.list_view.ItemList;
import co.realtime.storage.ItemAttribute;
import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;

/**
 * Created by A on 30.12.2014.
 */
public class RoomAdder {
    private onRoomAddedListener listener;

    public RoomAdder(onRoomAddedListener listener) {
        this.listener = listener;
    }

    public void start(int pos, String url) {
        LinkedHashMap<String, ItemAttribute> dbItem = ItemList.get(pos).dbItem();
        dbItem.put(DataBase.ATTRIBUTE_ROOM_IMG_URL, new ItemAttribute(url));
        dbItem.put(DataBase.ATTRIBUTE_LAST_EDITOR_DEVICE_ID, new ItemAttribute(DataBase.thisDeviceId));
        DataBase.roomTableRef.push(dbItem, new OnItemSnapshot() {
            @Override
            public void run(final ItemSnapshot itemSnapshot) {
                if (itemSnapshot != null) {
                    listener.onRoomAdded(true);
                } else {
                    listener.onRoomAdded(false);
                }
            }
        }, new OnError() {
            @Override
            public void run(Integer integer, String s) {
                listener.onRoomAdded(false);
            }
        });
    }

    public interface onRoomAddedListener {
        void onRoomAdded(boolean isAdded);
    }
}
