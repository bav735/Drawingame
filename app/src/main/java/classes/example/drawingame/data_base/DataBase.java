package classes.example.drawingame.data_base;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import co.realtime.storage.StorageRef;
import co.realtime.storage.TableRef;

/**
 * Created by A on 25.12.2014.
 */
public class DataBase {
   public final static String DEVELOPER_DEVICE_ID = "358095051343562";
   public final static String TABLE_ROOM = "room";
   public final static String TABLE_BAN = "ban";

   public final static String ATTRIBUTE_ROOM_NAME = "roomName";
   public final static String ATTRIBUTE_ROOM_IMG_URL = "roomImageUrl";
   public final static String ATTRIBUTE_ROOM_ID = "roomId";
   public final static String ATTRIBUTE_LAST_EDITOR_DEVICE_ID = "lastEditorDeviceId";
   public final static String ATTRIBUTE_BAN_REASON = "banReason";
   public final static String ATTRIBUTE_UNBAN_DATE = "unbanDate";

   private final static String APPLICATION_KEY = "XEQyNG";
   private final static String PRIVATE_KEY = "m8vUKz6sRvzw";
   private final static String STORAGE_URL = "https://storage-balancer.realtime.co/server/ssl/1.0";
   private final static String AUTH_TOKEN = "PM.Anonymous";

   private static boolean isCreating = false;
   public static String thisDeviceId;
   public static StorageRef storage;
   public static TableRef roomTableRef;
   public static TableRef banTableRef;

   public static void connect(final Context context) {
      if (isCreating) return;
      isCreating = true;
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               storage = new StorageRef(APPLICATION_KEY, PRIVATE_KEY, AUTH_TOKEN, true, false, STORAGE_URL);
               thisDeviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
               roomTableRef = storage.table(TABLE_ROOM);
               banTableRef = storage.table(TABLE_BAN);
               roomTableRef.asc();
               isCreating = false;
               Log.d("!", "connected DB");
            } catch (Exception e) {
               isCreating = false;
               Log.d("!", "couldb't connect DB");
            }
         }
      }).start();
   }

   public static boolean isDisconnected() {
      return (storage == null || roomTableRef == null || banTableRef == null);
   }
}