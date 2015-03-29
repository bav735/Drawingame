package classes.example.drawingame.room_activity.list_view;

import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EntryWeigher;
import com.googlecode.concurrentlinkedhashmap.EvictionListener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import classes.example.drawingame.utils.Utils;

/**
 * Created by A on 20.02.2015.
 */
public class Disk {
   public static final int SIZE_IN_MB = 80;
   public static ConcurrentMap<String, File> cache;

   public static void init() {
      EntryWeigher<String, File> memoryUsageWeigher = new EntryWeigher<String, File>() {
         @Override
         public int weightOf(String key, File file) {
            int bytes = (int) file.length();
            return bytes;
         }
      };

      EvictionListener<String, File> deleteListener = new EvictionListener<String, File>() {
         @Override
         public void onEviction(String key, File file) {
            if (file.exists())
               file.delete();
         }
      };

      cache = new ConcurrentLinkedHashMap.Builder<String, File>()
              .maximumWeightedCapacity(1024 * 1024 * SIZE_IN_MB)
              .weigher(memoryUsageWeigher)
              .listener(deleteListener)
              .build();

      File dir = new File(Utils.getCachedDir());
      if (dir.exists()) {
         File files[] = dir.listFiles();
         for (File file : files) {
            String name = file.getName();
            int pos = name.lastIndexOf(".");
            name = name.substring(0, pos);
            cache.put(name, file);
         }
      }
   }

   public static void add(final Item item, final Bitmap bitmap, final OnAddListener listener) {
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               cache.put(item.getImgFileName(), Utils.saveBitmap(bitmap,
                       Utils.getCachedDir(), item.getImgFileName()));
               ItemList.setBusyState(item, null);
               listener.OnAdd(false);
            } catch (IOException e) {
               ItemList.setBusyState(item, ItemList.ITEM_ERROR_DISK_ADD);
               listener.OnAdd(true);
            }
         }
      }).start();
   }

   public interface OnAddListener {
      void OnAdd(boolean isError);
   }
}
