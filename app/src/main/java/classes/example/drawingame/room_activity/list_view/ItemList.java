package classes.example.drawingame.room_activity.list_view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.data_base.RoomAdder;
import classes.example.drawingame.data_base.RoomGetter;
import classes.example.drawingame.data_base.RoomRemover;
import classes.example.drawingame.data_base.RoomUpdater;
import classes.example.drawingame.data_base.RoomsGetter;
import classes.example.drawingame.imgur.ImgurDownload;
import classes.example.drawingame.imgur.ImgurUpload;
import classes.example.drawingame.room_activity.service.ListService;

/**
 * Created by A on 25.12.2014.
 */
public class ItemList {
   public static final String ITEM_PROGRESS = "progress";
   public static final String ITEM_ERROR_DB = "error_db";
   public static final String ITEM_ERROR_IMGUR = "error_imgur";
   public static final String ITEM_ERROR_DISK_ADD = "error_disk_add";
   public static final String ITEM_ERROR_MEMORY_ADD = "error_memory_add";

   public static ArrayList<Item> list = new ArrayList<Item>();
   public static boolean listIsSetting = false;
//   public static ConcurrentHashMap<String, String> busyItems = new ConcurrentHashMap<>();

   private static void addItemToList(final Item itemAdd) {
      for (int i = 0; i < list.size(); i++)
         list.get(i).pos++;
      itemAdd.pos = 0;
      list.add(0, itemAdd);
      ListService.sendMessageNotifyAdapter();
   }

//   private static void removeItemFromList(final int pos) {
//      Utils.roomActivity.runOnUiThread(new Runnable() {
//         @Override
//         public void run() {
//            for (int i = pos; i < list.size(); i++)
//               list.get(i).pos--;
//            list.remove(pos);
//            Utils.notifyAdapter();
//         }
//      });
//   }

   private static void saveToPref(final Context context, final ArrayList<Item> saveList, final OnReloadListener listener) {
//      new Thread(new Runnable() {
//         @Override
//         public void run() {
      try {
         JSONArray jsonList = new JSONArray();
         for (Item item : saveList) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put("roomId", item.roomId);
            jsonItem.put("roomName", item.roomName);
            jsonItem.put("roomImgUrl", item.roomImgUrl);
            jsonItem.put("lastEditorDeviceId", item.lastEditorDeviceId);
            jsonList.put(jsonItem);
         }
         SharedPreferences preferences = context.
                 getSharedPreferences("preferences", Context.MODE_PRIVATE);
         preferences.edit().putString("list", jsonList.toString()).commit();
         listIsSetting = false;
         if (listener != null) listener.onReloaded(true);
      } catch (JSONException e) {
//         Log.d("!", "couldn't save new list to prefs");
         listIsSetting = false;
         if (listener != null) listener.onReloaded(false);
      }
//         }
//      }).start();
   }

   public static void getFromPref(final Context context, final String s, final OnReloadListener listener) {
      try {
//         Log.d("!", "getting list from prefs");
         JSONArray jsonList = new JSONArray(s);
         ArrayList<Item> listFromJson = new ArrayList<Item>();
         for (int i = 0; i < jsonList.length(); i++) {
            Item item = new Item();
            JSONObject jsonItem = jsonList.getJSONObject(i);
            item.roomId = jsonItem.getString("roomId");
            item.roomName = jsonItem.getString("roomName");
            item.roomImgUrl = jsonItem.getString("roomImgUrl");
            item.lastEditorDeviceId = jsonItem.getString("lastEditorDeviceId");
            item.pos = i;
            listFromJson.add(item.pos, item);
         }
         setNewList(context, listFromJson, listener);
      } catch (JSONException e) {
         ItemList.listIsSetting = false;
         if (listener != null) listener.onReloaded(false);
      }
   }

   public static void setNewList(Context context, ArrayList<Item> newList, OnReloadListener listener) {
//      Log.d("!", "setting new list!");
      list = newList;
      if (listener == null) {
         ListService.sendMessageNotifyAdapter();
         ListService.sendMessageShowList();
      }
      saveToPref(context, newList, listener);
   }

   public static LinkedHashMap<String, Item> getMap() {
      return getMap(list);
   }

   private static LinkedHashMap<String, Item> getMap(ArrayList<Item> arrayList) {
      LinkedHashMap<String, Item> tMap = new LinkedHashMap<String, Item>();
      for (Item item : arrayList)
         tMap.put(item.roomId, item);
      return tMap;
   }

   public static boolean isOnProgress() {
      for (Item item : list)
         if (item.busyState != null && item.busyState.equals(ITEM_PROGRESS))
            return true;
      return false;
   }

   public static void refreshItemFromDB(final Item item, Context context) {
      if (item.busyState != null) return;
      ItemList.setBusyState(item, ITEM_PROGRESS);
      new RoomGetter(new RoomGetter.OnRoomGotListener() {
         @Override
         public void onRoomGot(final Item dbItem, boolean isError) {
            if (isError) {
               ItemList.setBusyState(item, ITEM_ERROR_DB);
               showErrorDialog("holder_refresh");
               return;
            }
            if (dbItem == null) {
               ListService.sendMessageShowErrorDialog(R.string.errorNotFound);
               ItemList.setBusyState(item, null);
               return;
            }
            if (!item.roomImgUrl.equals(dbItem.roomImgUrl)) {
               ItemList.setBusyState(item, null);
               item.roomImgUrl = dbItem.roomImgUrl;
               item.lastEditorDeviceId = dbItem.lastEditorDeviceId;
               downloadItemFromImgur(item, "holder_refresh");
            } else
               setBusyState(item, null);
         }
      }).start(item.roomId, context);
   }

   public static void setBusyState(Item item, String state) {
      item.busyState = state;
      ListService.sendMessageNotifyAdapter();
   }

   public static void downloadItemFromImgur(final Item item, final String from) {
      setBusyState(item, ITEM_PROGRESS);
      new ImgurDownload(new ImgurDownload.OnImgReceivedListener() {
         @Override
         public void onImgReceived(Bitmap bitmap) {
            if (bitmap == null) {
               setBusyState(item, ITEM_ERROR_IMGUR);
               showErrorDialog(from);
            } else {
               Disk.add(item, bitmap, new Disk.OnAddListener() {
                  @Override
                  public void OnAdd(boolean isError) {
                     if (isError)
                        setBusyState(item, ITEM_ERROR_DISK_ADD);
                     else
                        setBusyState(item, null);
                  }
               });
            }
         }
      }).start(item.roomImgUrl);
   }

   private static void showErrorDialog(String from) {
      if (from.equals("holder_refresh")) {
         ListService.sendMessageShowErrorDialog(R.string.errorRefreshOne);
         return;
      }
      if (from.equals("holder_retry")) {
         ListService.sendMessageShowErrorDialog(R.string.errorDownload);
      }
//      if (from.equals("menu_item") && !MyAlertDialog.isShown) {
//         MyAlertDialog.isShown = true;
//         Utils.showErrorDialog(Utils.stringFromRes(R.string.errorRefreshAll));
//      }
   }

   public static void removeItem(final Item item, Context context) {
      listIsSetting = true;
      new RoomRemover(new RoomRemover.OnRoomRemovedListener() {
         @Override
         public void onRoomRemoved(String response) {
            if (response.equals("error")) {
               ListService.sendMessageShowErrorDialog(R.string.errorDb);
            }
            if (response.equals("not found")) {
               ListService.sendMessageShowErrorDialog(R.string.errorNotFound);
            }
//            if (response.equals("removed")) {
//            }
            listIsSetting = false;
         }
      }).start(item, context);
   }

   public static void startUpdateItemToDB(Item item, Bitmap bitmap, Context context) {
      updateItemToDBCheckFromDB(item, bitmap, context);
   }

   private static void updateItemToDBCheckFromDB(final Item item, final Bitmap bitmap, final Context context) {
      new RoomGetter(new RoomGetter.OnRoomGotListener() {
         @Override
         public void onRoomGot(Item dbItem, boolean isError) {
            if (isError) {
               ListService.sendMessageShowErrorDialog(R.string.errorUpload);
               setBusyState(item, null);
            } else {
               if (dbItem == null) {
                  ListService.sendMessageShowErrorDialog(R.string.errorNotFound);
                  setBusyState(item, null);
               } else
                  updateItemToDBUploadToImgur(item, bitmap, context);
            }
         }
      }).start(item.roomId, context);
   }

   private static void updateItemToDBUploadToImgur(final Item item, final Bitmap bitmap,
                                                   final Context context) {
      new ImgurUpload(new ImgurUpload.OnImgUrlReceivedListener() {
         @Override
         public void onImgUrlReceived(final String newImgUrl) {
            if (newImgUrl != null) {
               finishUpdateItemToDB(item, bitmap, newImgUrl, context);
            } else {
               ListService.sendMessageShowErrorDialog(R.string.errorUpload);
               setBusyState(item, null);
            }
         }
      }).start(bitmap);
   }

   private static void finishUpdateItemToDB(final Item item, final Bitmap bitmap,
                                            final String newUrl,Context context) {
      new RoomUpdater(new RoomUpdater.onRoomUpdatedListener() {
         @Override
         public void onRoomUpdated(boolean isUpdated) {
            if (isUpdated) {
               item.roomImgUrl = newUrl;
               item.lastEditorDeviceId = DataBase.thisDeviceId;
               Disk.add(item, bitmap, new Disk.OnAddListener() {
                  @Override
                  public void OnAdd(boolean isError) {
                     list.set(item.pos, item);
                     if (isError)
                        setBusyState(item, ITEM_ERROR_DISK_ADD);
                     else
                        setBusyState(item, null);
                  }
               });
            } else {
               ListService.sendMessageShowErrorDialog(R.string.errorUpload);
               setBusyState(item, null);
            }
         }
      }).start(item, newUrl,context);
   }

   public static void startAddItemToDB(Item item, Bitmap bitmap, Context context) {
      uploadItemToImgur(item, bitmap, context);
   }

   public static void uploadItemToImgur(final Item item, final Bitmap bitmap, final Context context) {
      new ImgurUpload(new ImgurUpload.OnImgUrlReceivedListener() {
         @Override
         public void onImgUrlReceived(final String newImgUrl) {
            if (newImgUrl != null) {
               item.roomImgUrl = newImgUrl;
               finishAddItemToDB(item, bitmap, context);
            } else {
               ListService.sendMessageShowErrorDialog(R.string.errorUpload);
               ListService.sendMessageShowList();
            }
         }
      }).start(bitmap);
   }

   private static void finishAddItemToDB(final Item item, final Bitmap bitmap, Context context) {
      new RoomAdder(new RoomAdder.onRoomAddedListener() {
         @Override
         public void onRoomAdded(boolean isAdded) {
            if (isAdded) {
               Disk.add(item, bitmap, new Disk.OnAddListener() {
                  @Override
                  public void OnAdd(boolean isError) {
                     if (isError)
                        item.busyState = ITEM_ERROR_DISK_ADD;
                     ItemList.addItemToList(item);
                     ListService.sendMessageShowList();
                  }
               });
            } else {
               ListService.sendMessageShowErrorDialog(R.string.errorUpload);
               ListService.sendMessageShowList();
            }
         }
      }).start(item, context);
   }

   public static void reloadItems(final OnReloadListener listener, final Context context) {
      new Thread(new Runnable() {
         @Override
         public void run() {
            SharedPreferences preferences = context.
                    getSharedPreferences("preferences", Context.MODE_PRIVATE);
            if (preferences.contains("list"))
               getFromPref(context, preferences.getString("list", null), listener);
            else {
               Log.d("!", "getting from web");
               new RoomsGetter(new RoomsGetter.OnRoomsGotListener() {
                  @Override
                  public void onRoomsGot(final ArrayList<Item> dbList) {
                     if (dbList == null) {
                        if (listener != null) listener.onReloaded(false);
                     } else
                        ItemList.setNewList(context, dbList, listener);
                  }
               }).start(context);
            }
//            } else if (listener != null) listener.onReloaded(false);
         }
      }).start();
   }

   public interface OnReloadListener {
      void onReloaded(boolean isReloaded);
   }
}
