package classes.example.drawingame.data_base;

import android.content.Context;

import java.util.LinkedHashMap;

import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.utils.Utils;
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

    public void start(Item item, Context context) {
       if (DataBase.isDisconnected()) {
          listener.onRoomAdded(false);//CHECK_RESULT_DISCONNECT);
          DataBase.connect(context);
          return;
       }
       if (Utils.isNoInternet(context)) {
          listener.onRoomAdded(false);
          return;
       }
        LinkedHashMap<String, ItemAttribute> dbItem = item.dbItem();
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
