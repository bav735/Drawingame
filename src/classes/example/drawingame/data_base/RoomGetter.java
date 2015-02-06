package classes.example.drawingame.data_base;

import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import co.realtime.storage.ItemAttribute;
import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;

/**
 * Created by A on 30.12.2014.
 */
public class RoomGetter {
    private OnRoomGotListener listener;

    public RoomGetter(OnRoomGotListener listener) {
        this.listener = listener;
    }

    public void start(final int pos) {
//        final Waiter waiter = new Waiter(new Waiter.OnTimeOutListener() {
//            @Override
//            public void onTimeOut() {
//                listener.onRoomGot(null, true);
//            }
//        });
//        new Thread(waiter).start();

        DataBase.roomTableRef.item(new ItemAttribute(ItemList.get(pos).roomId)).get(new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot itemSnapshot) {
//                if (waiter.timedOut) return;
//                else waiter.cancel();
                if (itemSnapshot != null && itemSnapshot.val() != null && !itemSnapshot.val().isEmpty()) {
                    Item dbItem = new Item(itemSnapshot.val());
                    listener.onRoomGot(dbItem, false);
                } else {
                    listener.onRoomGot(null, false);
                }
            }
        }, new OnError() {
            @Override
            public void run(Integer integer, String s) {
//                if (waiter.timedOut) return;
//                else waiter.cancel();
                listener.onRoomGot(null, true);
            }
        });
    }

    public interface OnRoomGotListener {
        void onRoomGot(Item item, boolean isError);
    }
}
