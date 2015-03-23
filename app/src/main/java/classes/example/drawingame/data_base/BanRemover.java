package classes.example.drawingame.data_base;

import android.content.Context;

import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.utils.Utils;
import co.realtime.storage.ItemAttribute;
import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;

/**
 * Created by A on 29.12.2014.
 */
public class BanRemover {
   private OnBanRemovedListener listener;

   public BanRemover(OnBanRemovedListener listener) {
      this.listener = listener;
   }

   public void start(Context context) {
      if (Utils.isNoInternet(context)) {
         listener.onBanRemoved("error");
         return;
      }

      DataBase.banTableRef.item(new ItemAttribute(DataBase.thisDeviceId)).del(new OnItemSnapshot() {
         @Override
         public void run(ItemSnapshot itemSnapshot) {
            if (itemSnapshot != null) {
               listener.onBanRemoved("removed");
            } else
               listener.onBanRemoved("removed");
         }
      }, new OnError() {
         @Override
         public void run(Integer integer, String s) {
            listener.onBanRemoved("error");
         }
      });
   }

   public interface OnBanRemovedListener {
      void onBanRemoved(String response);
   }
}
