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

   public void start() {
      if (!Utils.isNetworkAvailable()) {
         listener.onRoomsGot(null);
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
               listener.onRoomsGot(dbList);
            }
         }
      }, new OnError() {
         @Override
         public void run(Integer integer, String s) {
            listener.onRoomsGot(null);
         }
      });
   }

   public interface OnRoomsGotListener {
      void onRoomsGot(ArrayList<Item> dbList);
   }
}