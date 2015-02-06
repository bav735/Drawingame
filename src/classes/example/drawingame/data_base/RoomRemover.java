package classes.example.drawingame.data_base;

import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.utils.Utils;
import co.realtime.storage.ItemAttribute;
import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;

/**
 * Created by A on 30.12.2014.
 */
public class RoomRemover {
    private int pos;
    private OnRoomRemovedListener listener;

    public RoomRemover(int pos, OnRoomRemovedListener listener) {
        this.listener = listener;
        this.pos = pos;
    }

    public void start() {
        if (!Utils.isNetworkAvailable() || ItemList.size() < 5) {
            listener.onRoomRemoved("error");
            return;
        }

        DataBase.roomTableRef.item(new ItemAttribute(ItemList.get(pos).roomId)).del(new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot itemSnapshot) {
                if (itemSnapshot != null) {
                    listener.onRoomRemoved("removed");
                } else
                    listener.onRoomRemoved("not found");
            }
        }, new OnError() {
            @Override
            public void run(Integer integer, String s) {
                listener.onRoomRemoved("error");
            }
        });
    }

    public interface OnRoomRemovedListener {
        void onRoomRemoved(String response);
    }
}
