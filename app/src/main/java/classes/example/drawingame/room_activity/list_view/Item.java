package classes.example.drawingame.room_activity.list_view;

import android.graphics.Bitmap;

import java.io.File;
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
   public Bitmap itemBitmap = null;
   //    public Drawing drawing = null;
   public int pos;
   //    public boolean onError = false;
   public boolean onProgress = false;

   public Item(LinkedHashMap<String, ItemAttribute> dbItem) {
      roomId = dbItem.get(DataBase.ATTRIBUTE_ROOM_ID).toString();
      roomName = Utils.nameFromCodes(dbItem.get(DataBase.ATTRIBUTE_ROOM_NAME).toString());
      roomImgUrl = dbItem.get(DataBase.ATTRIBUTE_ROOM_IMG_URL).toString();
      lastEditorDeviceId = dbItem.get(DataBase.ATTRIBUTE_LAST_EDITOR_DEVICE_ID).toString();
   }

   public Item(Item item) {
      this.roomId = item.roomId;
      this.roomName = item.roomName;
      this.roomImgUrl = item.roomImgUrl;
      this.lastEditorDeviceId = item.lastEditorDeviceId;
      this.pos = item.pos;
//      this.onProgress = item.onProgress;
      this.itemBitmap = item.itemBitmap;
   }

   public Item() {
   }

   public LinkedHashMap<String, ItemAttribute> dbItem() {
      LinkedHashMap<String, ItemAttribute> dbItem = new LinkedHashMap<String, ItemAttribute>();
      dbItem.put(DataBase.ATTRIBUTE_ROOM_ID, new ItemAttribute(roomId));
      dbItem.put(DataBase.ATTRIBUTE_ROOM_NAME, new ItemAttribute(Utils.nameToCodes(roomName)));
      dbItem.put(DataBase.ATTRIBUTE_ROOM_IMG_URL, new ItemAttribute(roomImgUrl));
      dbItem.put(DataBase.ATTRIBUTE_LAST_EDITOR_DEVICE_ID, new ItemAttribute(lastEditorDeviceId));
      return dbItem;
   }

   public String toString() {
      return roomId + "\n" + roomName + "\n" + roomImgUrl + "\n" + lastEditorDeviceId;
   }

//   public String toJsonString() {
//      try {
//         JSONObject jsonObject = new JSONObject();
//         jsonObject.put("roomId", roomId);
//         jsonObject.put("roomImgUrl", roomImgUrl);
//         return jsonObject.toString();
//      } catch (JSONException e) {
//         return null;
//      }
//   }

   public boolean isImgSaved() {
      return Utils.preferences.getString(roomId, "").equals(roomImgUrl) &&
              new File(getImgFilePath()).exists();
   }

   public String getImgFilePath() {
      return Utils.getCachedDir() + "/" + getImgFileName() + ".png";
   }

   public String getImgFileName() {
      return roomId;
   }
}
