package classes.example.drawingame.room_activity.list_view;

import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EntryWeigher;
import com.googlecode.concurrentlinkedhashmap.EvictionListener;

import java.util.concurrent.ConcurrentMap;

import classes.example.drawingame.imgur.ImgurDownload;
import classes.example.drawingame.utils.Utils;

/**
 * Created by A on 20.02.2015.
 */
public class Memory {
   public static final int SIZE_IN_MB = 20;
   public static ConcurrentMap<String, Bitmap> cache;

   public static void init() {
      EntryWeigher<String, Bitmap> memoryUsageWeigher = new EntryWeigher<String, Bitmap>() {
         @Override
         public int weightOf(String key, Bitmap bitmap) {
            int bytes = bitmap.getRowBytes() * bitmap.getHeight();
            return bytes;
         }
      };

      EvictionListener<String, Bitmap> deleteListener = new EvictionListener<String, Bitmap>() {
         @Override
         public void onEviction(String key, Bitmap value) {
         }
      };

      cache = new ConcurrentLinkedHashMap.Builder<String, Bitmap>()
              .maximumWeightedCapacity(1024 * 1024 * SIZE_IN_MB)
              .weigher(memoryUsageWeigher)
              .listener(deleteListener)
              .build();
   }

   public static void add(final Item item) {
      ItemList.setBusyState(item, ItemList.ITEM_PROGRESS);
      new Thread(new Runnable() {
         @Override
         public void run() {
            if (Disk.cache.containsKey(item.getImgFileName())) {
               try {
                  cache.put(item.getImgFileName(), Utils.getBitmapByPath(item.getImgFilePath()));
                  ItemList.setBusyState(item, null);
               } catch (Exception e) {
                  ItemList.setBusyState(item, ItemList.ITEM_ERROR_MEMORY_ADD);
               }
            } else
               new ImgurDownload(new ImgurDownload.OnImgReceivedListener() {
                  @Override
                  public void onImgReceived(final Bitmap bitmap) {
                     if (bitmap == null)
                        ItemList.setBusyState(item, ItemList.ITEM_ERROR_IMGUR);
                     else {
                        Disk.add(item, bitmap, new Disk.OnAddListener() {
                           @Override
                           public void OnAdd(boolean isError) {
                              if (isError)
                                 ItemList.setBusyState(item, ItemList.ITEM_ERROR_DISK_ADD);
                              else {
                                 cache.put(item.getImgFileName(), bitmap);
                                 ItemList.setBusyState(item, null);
                              }
                           }
                        });
                     }
                  }
               }).start(item.roomImgUrl);
         }
      }).start();
   }
}