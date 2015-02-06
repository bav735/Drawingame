package classes.example.drawingame.room_activity.list_view;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.RoomAdder;
import classes.example.drawingame.data_base.RoomGetter;
import classes.example.drawingame.data_base.RoomRemover;
import classes.example.drawingame.data_base.RoomUpdater;
import classes.example.drawingame.imgur.ImgurDownload;
import classes.example.drawingame.imgur.ImgurUpload;
import classes.example.drawingame.utils.MyAlertDialog;
import classes.example.drawingame.utils.Utils;

/**
 * Created by A on 25.12.2014.
 */
public class ItemList {
   public static ArrayList<Item> list = new ArrayList<Item>();
   public static boolean listIsSetting = false;

   public static void setItem(final int pos, final Item item) {
      if (Utils.roomActivityExists())
         Utils.roomActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
               list.get(pos).itemBitmap = null;
               list.set(pos, item);
               Utils.notifyAdapter();
            }
         });
      else {
         list.get(pos).itemBitmap = null;
         list.set(pos, item);
      }
   }

   public static void setProgressToPos(final int pos, final boolean value) {
      if (list.get(pos).onProgress == value) return;
      if (Utils.roomActivityExists())
         Utils.roomActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
               list.get(pos).onProgress = value;
               Utils.notifyAdapter();
            }
         });
      else
         list.get(pos).onProgress = value;
   }

   public static int size() {
      return list.size();
   }

   private static void addItemToList(final Item itemAdd) {
      Utils.roomActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
            for (int i = 0; i < list.size(); i++)
               list.get(i).pos++;
            itemAdd.pos = 0;
            list.add(0, itemAdd);
            Utils.notifyAdapter();
         }
      });
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

   private static void saveToPref(final ArrayList<Item> saveList) {
      new Thread(new Runnable() {
         @Override
         public void run() {
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
               Utils.preferences.edit().putString("list", jsonList.toString()).commit();
            } catch (JSONException e) {
               //
            }
         }
      }).start();
   }

   public static void getFromPref(final String s) {
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
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
               setNewList(listFromJson);
            } catch (JSONException e) {
               //
            }
         }
      }).start();
   }

   public static void setNewList(final ArrayList<Item> newList) {
//      Log.d("!", "seting new list");
      listIsSetting = true;
      if (Utils.roomActivityExists())
         setNewListOnUI(newList);
      else {
         list = newList;
         listIsSetting = false;
      }
      removeItemsFromMemory(getListToRemove(newList));
      saveToPref(newList);
   }

   public static void setNewListOnUI(final ArrayList<Item> newList) {
//      new Thread(new Runnable() {
//         @Override
//         public void run() {
      Utils.roomActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
            list = newList;
            Utils.notifyAdapter();
            Utils.showList();
            listIsSetting = false;
            reloadItems();
         }
      });
//         }
//      }).start();
   }

   private static ArrayList<Item> getListToRemove(ArrayList<Item> newList) {
      final ArrayList<Item> toRemove = new ArrayList<Item>();
      LinkedHashMap<String, Item> newMap = getMap(newList);
      for (int i = 0; i < list.size(); i++) {
         Item item = list.get(i);
         if (!newMap.containsKey(item.roomId)) {
            item.itemBitmap = null;
            toRemove.add(new Item(item));
         } else {
            Item newItem = newList.get(newMap.get(item.roomId).pos);
            if (newItem.roomImgUrl.equals(item.roomImgUrl))
               newItem.itemBitmap = item.itemBitmap;
         }
      }
      return toRemove;
   }

   private static void removeItemsFromMemory(final ArrayList<Item> toRemove) {
//      Log.d("!", "removing - " + String.valueOf(toRemove.size()) + " items");
      new Thread(new Runnable() {
         @Override
         public void run() {
            for (Item item : toRemove) {
               Utils.preferences.edit().remove(item.roomId).commit();
               Utils.deleteFile(item.getImgFilePath());
            }
         }
      }).start();
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
         if (item.onProgress)
            return true;
      return false;
   }

   public static void refreshItems() {
//      MyAlertDialog.isShown = false;
      for (Item item : list)
         if (!item.onProgress)
            refreshItem(item, "menu_item");
   }

   public static void refreshItem(final Item item, final String from) {
      setProgressToPos(item.pos, true);
      new RoomGetter(new RoomGetter.OnRoomGotListener() {
         @Override
         public void onRoomGot(final Item dbItem, boolean isError) {
            if (isError) {
               showErrorDialog(from);
               setProgressToPos(item.pos, false);
               return;
            }
            if (dbItem == null) {
               if (from.equals("holder_refresh"))
                  Utils.showErrorDialog(Utils.stringFromRes(R.string.errorNotFound));
               setProgressToPos(item.pos, false);
               return;
            }
            if (!item.roomImgUrl.equals(dbItem.roomImgUrl)) {
               item.roomImgUrl = dbItem.roomImgUrl;
               item.lastEditorDeviceId = dbItem.lastEditorDeviceId;
               downloadItemFromImgur(item, from);
            } else {
               reloadItem(item);
            }
         }
      }).start(item.roomId);
   }

   public static void reloadItems() {
      for (Item item : list) {
         if (!item.onProgress && item.itemBitmap == null)
            reloadItem(item);
      }
   }

   private static void reloadItem(Item item) {
      setProgressToPos(item.pos, true);
      if (item.isImgSaved())
         loadItemFromDisk(item.pos);
      else
         downloadItemFromImgur(item, "set_list");
   }

   public static void downloadItemFromImgur(final Item item, final String from) {
      setProgressToPos(item.pos, true);
      new ImgurDownload(new ImgurDownload.OnImgReceivedListener() {
         @Override
         public void onImgReceived(Bitmap bitmap) {
            if (bitmap == null)
               showErrorDialog(from);
            else {
               item.itemBitmap = null;
               item.itemBitmap = bitmap;
               saveItem(item);
            }
            setProgressToPos(item.pos, false);
         }
      }).start(item.roomImgUrl);
   }

   private static void showErrorDialog(String from) {
      if (from.equals("holder_refresh")) {
         Utils.showErrorDialog(Utils.stringFromRes(R.string.errorRefreshOne));
         return;
      }
      if (from.equals("holder_retry")) {
         Utils.showErrorDialog(Utils.stringFromRes(R.string.errorDownload));
      }
//      if (from.equals("menu_item") && !MyAlertDialog.isShown) {
//         MyAlertDialog.isShown = true;
//         Utils.showErrorDialog(Utils.stringFromRes(R.string.errorRefreshAll));
//      }
   }

   public static void removeItem(final Item item) {
      setProgressToPos(item.pos, true);
      Utils.showConfirmActionDialog(Utils.stringFromRes(R.string.removeMessage), new MyAlertDialog.OnDismissedListener() {
         @Override
         public void onDismissed(boolean isPositive) {
            if (isPositive)
               new RoomRemover(new RoomRemover.OnRoomRemovedListener() {
                  @Override
                  public void onRoomRemoved(String response) {
                     if (response.equals("error")) {
                        Utils.showErrorDialog(Utils.stringFromRes(R.string.errorDb));
                     }
                     if (response.equals("not found")) {
                        Utils.showErrorDialog(Utils.stringFromRes(R.string.errorNotFound));
                     }
                     if (response.equals("removed")) {
                        Utils.toast(Utils.roomActivity, "removed");
//                        removeItemFromList(pos);
                     }
                     setProgressToPos(item.pos, false);
                  }
               }).start(item);
            else {
               setProgressToPos(item.pos, false);
            }
         }
      });
   }

   public static void updateItem(final Item item) {
      setProgressToPos(item.pos,true);
      new RoomGetter(new RoomGetter.OnRoomGotListener() {
         @Override
         public void onRoomGot(Item dbItem, boolean isError) {
            if (isError) {
               Utils.showRetryActionDialog(Utils.stringFromRes(R.string.errorUpload),
                       new MyAlertDialog.OnDismissedListener() {
                          @Override
                          public void onDismissed(boolean isPositive) {
                             if (isPositive)
                                updateItem(item);
                             else {
                                setProgressToPos(item.pos, false);
                                Utils.showList();
                             }
                          }
                       });
              return;
            }
            if (dbItem == null) {
               Utils.showErrorDialog(Utils.stringFromRes(R.string.errorNotFound));
               setProgressToPos(item.pos, false);
               Utils.showList();
            } else
               updateItemImgurUpload(item);
         }
      }).start(item.roomId);
   }

   private static void updateItemImgurUpload(final Item item) {
      setProgressToPos(item.pos, true);
      new ImgurUpload(new ImgurUpload.OnImgUrlReceivedListener() {
         @Override
         public void onImgUrlReceived(final String newImgUrl) {
            if (newImgUrl != null) {
               item.roomImgUrl = newImgUrl;
               updateItemToDB(item);
            } else {
               //сказать что возможно эта картинка уже обновилась извне
               Utils.showRetryActionDialog(Utils.stringFromRes(R.string.errorUpload),
                       new MyAlertDialog.OnDismissedListener() {
                          @Override
                          public void onDismissed(boolean isPositive) {
                             if (isPositive)
                                updateItemImgurUpload(item);
                             else {
                                item.itemBitmap = null;
                                setProgressToPos(item.pos, false);
                                Utils.showList();
                             }
                          }
                       });
            }
         }
      }).start(item.itemBitmap);
   }

   private static void updateItemToDB(final Item item) {
      setProgressToPos(item.pos, true);
      new RoomUpdater(new RoomUpdater.onRoomUpdatedListener() {
         @Override
         public void onRoomUpdated(boolean isUpdated) {
            if (isUpdated) {
               setItem(item.pos, item);
               Utils.showList();
            } else
               Utils.showRetryActionDialog(Utils.stringFromRes(R.string.errorUpload),
                       new MyAlertDialog.OnDismissedListener() {
                          @Override
                          public void onDismissed(boolean isPositive) {
                             if (isPositive)
                                updateItemToDB(item);
                             else {
                                item.itemBitmap = null;
                                setProgressToPos(item.pos, false);
                                Utils.showList();
                             }
                          }
                       });
         }
      }).start(item);
   }

   public static void addNewItem(final Item item) {
      new ImgurUpload(new ImgurUpload.OnImgUrlReceivedListener() {
         @Override
         public void onImgUrlReceived(final String newImgUrl) {
            if (newImgUrl != null) {
               item.roomImgUrl = newImgUrl;
               addItemToDB(item);
            } else {
               Utils.showRetryActionDialog(Utils.stringFromRes(R.string.errorUpload),
                       new MyAlertDialog.OnDismissedListener() {
                          @Override
                          public void onDismissed(boolean isPositive) {
                             if (isPositive)
                                addNewItem(item);
                             else {
                                item.itemBitmap = null;
                                Utils.showList();
                             }
                          }
                       });
            }
         }
      }).start(item.itemBitmap);
   }

   private static void addItemToDB(final Item item) {
      new RoomAdder(new RoomAdder.onRoomAddedListener() {
         @Override
         public void onRoomAdded(boolean isAdded) {
            if (isAdded) {
               ItemList.addItemToList(item);
               saveItem(item);
               Utils.showList();
            } else
               Utils.showRetryActionDialog(Utils.stringFromRes(R.string.errorUpload),
                       new MyAlertDialog.OnDismissedListener() {
                          @Override
                          public void onDismissed(boolean isPositive) {
                             if (isPositive)
                                addItemToDB(item);
                             else {
                                item.itemBitmap = null;
                                Utils.showList();
                             }
                          }
                       });
         }
      }).start(item);
   }

   public static void loadItemFromDisk(final int pos) {
      setProgressToPos(pos, true);
      new Thread(new Runnable() {
         @Override
         public void run() {
            final Bitmap bitmap = Utils.getBitmapByItem(list.get(pos));
            if (Utils.roomActivityExists())
               Utils.roomActivity.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     list.get(pos).itemBitmap = null;
                     list.get(pos).itemBitmap = bitmap;
                     setProgressToPos(pos, false);
                  }
               });
            else {
               list.get(pos).itemBitmap = null;
               list.get(pos).itemBitmap = bitmap;
               setProgressToPos(pos, false);
            }
         }
      }).start();
   }

   private static void saveItem(Item item) {
      try {
         Utils.saveBitmap(item.itemBitmap, Utils.getCachedDir(), item.getImgFileName());
         Utils.preferences.edit().putString(item.roomId, item.roomImgUrl).commit();
      } catch (IOException e) {
         Log.d("!", "picture wasn't saved - " + e.toString());
      }
   }
}
