package classes.example.drawingame.data_base;

import android.content.Context;

import classes.example.drawingame.utils.Utils;
import co.realtime.storage.ItemAttribute;
import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;

/**
 * Created by A on 29.12.2014.
 */
public class BanChecker {
   public static final String BAN_REASON = "ban_reason";
   public static final String UNBAN_DATE = "unban_date";
   public static final String CHECK_RESULT_ERROR = "check_error";
   //   public static final String CHECK_RESULT_DISCONNECT = "check_disconnect";
   public static final String CHECK_RESULT_OK = "check_ok";
   private OnBanCheckedListener listener;

   public BanChecker(OnBanCheckedListener listener) {
      this.listener = listener;
   }

   public void start(Context context) {
      if (DataBase.isDisconnected()) {
         listener.onBanChecked(CHECK_RESULT_ERROR, null);//CHECK_RESULT_DISCONNECT);
         DataBase.connect(context);
         return;
      }
      if (Utils.isNoInternet(context)) {
         listener.onBanChecked(CHECK_RESULT_ERROR, null);
         return;
      }
      DataBase.banTableRef.item(new ItemAttribute(DataBase.thisDeviceId)).get(new OnItemSnapshot() {
         @Override
         public void run(ItemSnapshot itemSnapshot) {
            if (itemSnapshot != null && itemSnapshot.val() != null && !itemSnapshot.val().isEmpty()) {
               String banReason = itemSnapshot.val().get(DataBase.ATTRIBUTE_BAN_REASON).toString();
               String unbanDate = itemSnapshot.val().get(DataBase.ATTRIBUTE_UNBAN_DATE).toString();
               listener.onBanChecked(banReason, unbanDate);
            } else {
               listener.onBanChecked(CHECK_RESULT_OK, null);
            }
         }
      }, new OnError() {
         @Override
         public void run(Integer integer, String s) {
            listener.onBanChecked(CHECK_RESULT_ERROR, null);
         }
      });
   }

   //checkResult = banReason if device is banned, otherwise it's OK or ERROR
   public interface OnBanCheckedListener {
      void onBanChecked(String result, String unbanDate);
   }
}
