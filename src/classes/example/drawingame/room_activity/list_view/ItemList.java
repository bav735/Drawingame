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
import classes.example.drawingame.data_base.BanChecker;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.data_base.RoomAdder;
import classes.example.drawingame.data_base.RoomGetter;
import classes.example.drawingame.data_base.RoomRemover;
import classes.example.drawingame.data_base.RoomUpdater;
import classes.example.drawingame.imgur.ImgurDownload;
import classes.example.drawingame.imgur.ImgurUpload;
import classes.example.drawingame.utils.ToastDialog;
import classes.example.drawingame.utils.Utils;

/**
 * Created by A on 25.12.2014.
 */
public class ItemList {
    public static ArrayList<Item> list = null;//new ArrayList<Item>();

    public static Item get(int pos) {
        return list.get(pos);
    }

    private static Item list(int pos) {
        return list.get(pos);
    }

    public static int size() {
        return list.size();
    }

    public static void add(Item item) {
        list.add(item.pos, item);
    }

    private static void remove(int pos) {
        for (int i = pos; i < list.size(); i++)
            list.get(i).pos--;
        list.remove(pos);
    }

    public static String toJson() {
        try {
            JSONArray jsonList = new JSONArray();
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("roomId", list.get(i).roomId);
                jsonItem.put("roomName", list.get(i).roomName);
                jsonItem.put("roomImgUrl", list.get(i).roomImgUrl);
                jsonItem.put("lastEditorDeviceId", list.get(i).lastEditorDeviceId);
                jsonList.put(jsonItem);

            }
            saveBitmaps();
            return jsonList.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static void fromJson(final String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonList = new JSONArray(s);
                    for (int i = 0; i < jsonList.length(); i++) {
                        Item item = new Item();
                        JSONObject jsonItem = jsonList.getJSONObject(i);
                        item.roomId = jsonItem.getString("roomId");
                        item.roomName = jsonItem.getString("roomName");
                        item.roomImgUrl = jsonItem.getString("roomImgUrl");
                        item.lastEditorDeviceId = jsonItem.getString("lastEditorDeviceId");
                        item.pos = i;
                        item.currentBitmap = Utils.getBitmapById(item.roomId);
                        add(item);
                        if (item.currentBitmap == null)
                            downloadItem(item.pos);
                    }
                   Utils.notifyAdapter();
                   if (Utils.roomActivityExists()) {
                      Utils.roomActivity.showList();
                   }
                } catch (JSONException e) {
                    Utils.toast(Utils.stringFromRes(R.string.errorDb));
                }
            }
        }).start();
//        if (Uti+ls.roomActivityExists())
//            Utils.roomActivity.showList();
    }

    private static void saveBitmaps() {
//        Utils.clearCache();
        for (Item item : list) {
            try {
                Utils.saveById(item.currentBitmap, item.roomId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("!", "saveBitmaps");
    }


//    public static Item get(String id) {
//        if (!map.containsKey(id))
//            return null;
//        return list.get(map.get(id));
//    }

    public static void setList(ArrayList<Item> tList) {
        LinkedHashMap<String, Item> map = getMap(list);
        LinkedHashMap<String, Item> tMap = getMap(tList);
        ArrayList<Item> toRemove = new ArrayList<Item>();
        for (Item item : list) {
            if (tMap.containsKey(item.roomId)) {
                Item tItem = tMap.get(item.roomId);
                if (!tItem.roomImgUrl.equals(item.roomImgUrl)) {
                    list.get(item.pos).roomImgUrl = tItem.roomImgUrl;
                    list.get(item.pos).lastEditorDeviceId = tItem.lastEditorDeviceId;
                    downloadItem(item.pos);
                }
                list.get(item.pos).pos = tItem.pos;
            } else
                toRemove.add(item);
        }
        for (Item item : toRemove)
            list.remove(item.pos);
        for (Item tItem : tList)
            if (!map.containsKey(tItem.roomId)) {
                add(tItem);
                downloadItem(tItem.pos);
            }
        Utils.notifyAdapter();
        if (Utils.roomActivityExists()) {
            Utils.roomActivity.showList();
        }
    }

    private static LinkedHashMap<String, Item> getMap(ArrayList<Item> tList) {
        LinkedHashMap<String, Item> tMap = new LinkedHashMap<String, Item>();
        for (Item item : tList)
            tMap.put(item.roomId, item);
        return tMap;
    }

    public static LinkedHashMap<String, Item> getMap() {
        return getMap(list);
    }

    public static void uploadItem(final int pos) {
        list(pos).onProgress = true;
        Utils.notifyAdapter();
        new ImgurUpload(new ImgurUpload.OnImgUrlReceivedListener() {
            @Override
            public void onImgUrlReceived(final String imgUrl) {
                if (imgUrl != null) {
                    if (list(pos).currentBitmap == null)
                        new RoomAdder(new RoomAdder.onRoomAddedListener() {
                            @Override
                            public void onRoomAdded(final boolean isAdded) {
                                afterUpload(isAdded, imgUrl, pos);
                            }
                        }).start(pos, imgUrl);
                    else
                        new RoomUpdater(new RoomUpdater.onRoomUpdatedListener() {
                            @Override
                            public void onRoomUpdated(final boolean isUpdated) {
                                afterUpload(isUpdated, imgUrl, pos);
                            }
                        }).start(pos, imgUrl);
                } else {
                    list(pos).onProgress = false;
                    list(pos).onError = true;
                    Utils.notifyAdapter();
                }
            }
        }).start(ItemList.get(pos).bitmapToUpload);
    }

    public static void downloadItem(final int pos) {
        list(pos).onProgress = true;
        Utils.notifyAdapter();
        new ImgurDownload(new ImgurDownload.OnImgReceivedListener() {
            @Override
            public void onImgReceived(final Bitmap bitmap) {
//                Utils.roomActivity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
                list(pos).onProgress = false;
                if (bitmap != null) {
                    Log.d("!", "downloaded #" + String.valueOf(pos));
                    list(pos).currentBitmap = null;
                    ItemList.get(pos).currentBitmap = bitmap;
                    list(pos).onError = false;
                } else
                    list(pos).onError = true;
//                    }
//                });
                Utils.notifyAdapter();
            }
        }).start(ItemList.get(pos).roomImgUrl);
    }

    private static void afterUpload(boolean uploadSucc, String imgUrl, int pos) {
        list(pos).onProgress = false;
        if (uploadSucc) {
            Bitmap bitmap = Utils.getBitmapById(list(pos).roomId);
            if (bitmap != null)
                list(pos).currentBitmap = bitmap;
            list(pos).bitmapToUpload = null;
            list(pos).roomImgUrl = imgUrl;
            list(pos).lastEditorDeviceId = DataBase.thisDeviceId;
            list(pos).onError = false;
        } else {
            list(pos).onError = true;
        }
        Utils.notifyAdapter();
    }

    public static void refreshItem(final int pos) {
        //нет ошибки, т.е. видна картинка и ее можно обновить
        if (!list(pos).onError) {
            list(pos).onProgress = true;
            Utils.notifyAdapter();
            //реализуется процесс обновления, сначала получаем картинку из БД
            new RoomGetter(new RoomGetter.OnRoomGotListener() {
                @Override
                public void onRoomGot(final Item item, boolean isError) {
                    if (isError) {
                        list(pos).onError = true;
                        list(pos).onProgress = false;
                        Utils.notifyAdapter();
                        return;
                    }
                    if (item == null) {
                        list(pos).onError = true;
                        list(pos).onProgress = false;
                        Utils.toast(Utils.stringFromRes(R.string.errorNotFound));
                        Utils.notifyAdapter();
                        return;
                    }
                    //затем, если наша картинка устарела, ставим в статус новую картинку на скачивание
//                Utils.roomActivity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
                    if (!list(pos).roomImgUrl.equals(item.roomImgUrl)) {
                        ItemList.get(pos).roomImgUrl = item.roomImgUrl;
                        ItemList.get(pos).lastEditorDeviceId = item.lastEditorDeviceId;
                        downloadItem(pos);
                    } else {
                        list(pos).onProgress = false;
                        Utils.notifyAdapter();
                    }
//                    }
//                });
                }
            }).start(pos);
        } else {
            //если же находимся в состоянии ошибки, есть случаи
            if (list(pos).currentBitmap == null) {
                //картинка не была скачана из БД или добавленная картинка не была загружена в БД
                String msg;
                if (list(pos).bitmapToUpload == null)
                    //не была скачана из БД
                    msg = Utils.stringFromRes(R.string.errorDownload);
                else
                    //не была загружена в БД
                    msg = Utils.stringFromRes(R.string.errorUpload);
                new ToastDialog(msg, new ToastDialog.OnDismissedListener() {
                    @Override
                    public void onDismissed(boolean isOk) {
                        if (isOk) {
//                            Utils.roomActivity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
                            ////такую картинку предлагаем удалить из списка
                            remove(pos);
                            Utils.notifyAdapter();
//                                }
//                            });
                        }
                    }
                }).show();
            } else {
                //иначе картинка была добавлена\скачана в\из БД, но также есть случаи
                if (list(pos).bitmapToUpload != null)
                    //не удалось отредактировать картинку
                    new ToastDialog(Utils.stringFromRes(R.string.errorEdit), new ToastDialog.OnDismissedListener() {
                        //предлагаем вернуть предыдущую копию, предупреждаем что изменения будут утеряны
                        @Override
                        public void onDismissed(boolean isOk) {
                            if (isOk) {
                                list(pos).onError = false;
                                list(pos).bitmapToUpload = null;
                                Utils.notifyAdapter();
                            }
                        }
                    }).show();
                else {
                    //не удалось обновить картинку вручную
                    //возвращаем предыдущую копию, возможно не актуальную
                    new ToastDialog(Utils.stringFromRes(R.string.errorRefreshItem), new ToastDialog.OnDismissedListener() {
                        //предлагаем вернуть предыдущую копию, предупреждаем что вернется предыдущая картинка
                        @Override
                        public void onDismissed(boolean isOk) {
                            if (isOk) {
                                list(pos).onError = false;
                                Utils.notifyAdapter();
                            }
                        }
                    }).show();
                }
            }
        }
    }

    public static void removeItem(final int pos) {
        list(pos).onProgress = true;
        Utils.notifyAdapter();
        new BanChecker(new BanChecker.OnBanCheckedListener() {
            @Override
            public void onBanChecked(boolean isBanned) {
                if (isBanned) {
                    Utils.toast(Utils.stringFromRes(R.string.banMessage));
                    list(pos).onProgress = false;
                    Utils.notifyAdapter();
                } else {
                    Log.d("!", "got ban, removing..");
                    Utils.toast(Utils.stringFromRes(R.string.removeMessage), new ToastDialog.OnDismissedListener() {
                        @Override
                        public void onDismissed(boolean isOk) {
                            if (isOk)
                                new RoomRemover(pos, new RoomRemover.OnRoomRemovedListener() {
                                    @Override
                                    public void onRoomRemoved(String response) {
                                        list(pos).onProgress = false;
                                        if (response.equals("error")) {
                                            Utils.toast(Utils.stringFromRes(R.string.errorDb));
                                        } /*else*/
                                        if (response.equals("not found")) {
                                            Utils.toast(Utils.stringFromRes(R.string.errorNotFound));
                                        }
                                        if (response.equals("removed")) {
//                                            Utils.roomActivity.runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
                                            remove(pos);
//                                                }
//                                            });
                                        }
                                        Utils.notifyAdapter();
                                    }
                                }).start();
                            else {
                                list(pos).onProgress = false;
                                Utils.notifyAdapter();
                            }
                        }
                    });
                }
            }
        }).start();
    }

    public static boolean isBusy() {
//        if (list.isEmpty()) return true;
        for (Item item : list)
            if (item.onProgress || item.bitmapToUpload != null)// || item.onError)
                return true;
        return false;
    }
}
