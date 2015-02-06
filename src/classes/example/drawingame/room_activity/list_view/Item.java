package classes.example.drawingame.room_activity.list_view;

import android.graphics.Bitmap;
import android.view.View;

import java.util.LinkedHashMap;

import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.utils.Utils;
import co.realtime.storage.ItemAttribute;

/**
 * Created by A on 15.12.2014.
 */
public class Item {
    public String roomId;
    public String roomName;
    public String roomImgUrl = null;
    public String lastEditorDeviceId;
    public Bitmap currentBitmap = null;
    public Bitmap bitmapToUpload = null;
    public int pos;
    public boolean onError = false;
    public boolean onProgress = false;
//    public boolean onDownloading = false;
//    public boolean onUploading = false;
//    public View view = null;

    public Item(LinkedHashMap<String, ItemAttribute> dbItem) {
        roomId = dbItem.get(DataBase.ATTRIBUTE_ROOM_ID).toString();
        roomName = Utils.nameFromCodes(dbItem.get(DataBase.ATTRIBUTE_ROOM_NAME).toString());
        roomImgUrl = dbItem.get(DataBase.ATTRIBUTE_ROOM_IMG_URL).toString();
        lastEditorDeviceId = dbItem.get(DataBase.ATTRIBUTE_LAST_EDITOR_DEVICE_ID).toString();
    }

    public Item() {//String roomId, String roomName, String roomImgUrl, String lastEditorDeviceId, Bitmap imgBitmap) {
//        this.roomId = roomId;
//        this.roomName = roomName;
//        this.roomImgUrl = roomImgUrl;
//        this.lastEditorDeviceId = lastEditorDeviceId;
//        this.bitmapToUpload = imgBitmap;
    }

    public LinkedHashMap<String, ItemAttribute> dbItem() {
        LinkedHashMap<String, ItemAttribute> dbItem = new LinkedHashMap<String, ItemAttribute>();
        dbItem.put(DataBase.ATTRIBUTE_ROOM_ID, new ItemAttribute(roomId));
        dbItem.put(DataBase.ATTRIBUTE_ROOM_NAME, new ItemAttribute(Utils.nameToCodes(roomName)));
        dbItem.put(DataBase.ATTRIBUTE_ROOM_IMG_URL, new ItemAttribute(roomImgUrl));
        dbItem.put(DataBase.ATTRIBUTE_LAST_EDITOR_DEVICE_ID, new ItemAttribute(lastEditorDeviceId));
        return dbItem;
    }
}
