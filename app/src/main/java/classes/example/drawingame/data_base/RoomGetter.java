package classes.example.drawingame.data_base;

import android.content.Context;

import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.utils.Utils;
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

   public void start(final String id, Context context) {
      if (DataBase.isDisconnected()) {
         listener.onRoomGot(null, true);//CHECK_RESULT_DISCONNECT);
         DataBase.connect(context);
         return;
      }
      if (Utils.isNoInternet(context)) {
         listener.onRoomGot(null, true);
         return;
      }
      DataBase.roomTableRef.item(new ItemAttribute(id)).get(new OnItemSnapshot() {
         @Override
         public void run(ItemSnapshot itemSnapshot) {
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
            listener.onRoomGot(null, true);
         }
      });
   }

   public interface OnRoomGotListener {
      void onRoomGot(Item item, boolean isError);
   }
}
