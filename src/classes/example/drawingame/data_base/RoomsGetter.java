package classes.example.drawingame.data_base;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.utils.Utils;
import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;

/**
 * Created by A on 30.12.2014.
 */
public class RoomsGetter {
    private OnRoomsGotListener listener;
    private ArrayList<Item> dbList = new ArrayList<Item>();

    public RoomsGetter(OnRoomsGotListener listener) {
        this.listener = listener;
    }

    public /*synchronized*/ void start() {
        if (!Utils.isNetworkAvailable()) {
            listener.onRoomsGot(null, true);
            return;
        }

        DataBase.roomTableRef.getItems(new OnItemSnapshot() {
            @Override
            public void run(final ItemSnapshot itemSnapshot) {
                if (itemSnapshot != null) {
                    Item dbItem = new Item(itemSnapshot.val());
                    dbItem.pos = dbList.size();
                    dbList.add(dbItem);
                } else {
                    if (ItemList.size() == dbList.size()) {
                        boolean theSame = true;
                        LinkedHashMap<String, Item> map = ItemList.getMap();
                        for (Item dbItem : dbList)
                            if (!map.containsKey(dbItem.roomId) ||
                                    !map.get(dbItem.roomId).roomImgUrl.equals(dbItem.roomImgUrl))
                                theSame = false;
                        if (theSame) {
                            listener.onRoomsGot(null, false);
                            return;
                        }
                    }
                    listener.onRoomsGot(dbList, false);
                }
            }
        }, new OnError() {
            @Override
            public void run(Integer integer, String s) {
                listener.onRoomsGot(null, true);
            }
        });
    }

    public interface OnRoomsGotListener {
        void onRoomsGot(ArrayList<Item> list, boolean isError);
    }
}
