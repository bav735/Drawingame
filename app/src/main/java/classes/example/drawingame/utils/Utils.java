package classes.example.drawingame.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

import classes.example.drawingame.room_activity.RoomActivity;

/**
 * @author Peli
 * @author paulburke (ipaulpro)
 * @version 2013-12-11
 */

@TargetApi(19)
public class Utils {
   public static final String MIME_TYPE_AUDIO = "audio/*";
   public static final String MIME_TYPE_TEXT = "text/*";
   public static final String MIME_TYPE_IMAGE = "image/*";
   public static final String MIME_TYPE_VIDEO = "video/*";
   public static final String MIME_TYPE_APP = "application/*";
   public static final String HIDDEN_PREFIX = ".";
   /**
    * File (not directories) filter.
    *
    * @author paulburke
    */
   public static FileFilter sFileFilter = new FileFilter() {
      @Override
      public boolean accept(File file) {
         final String fileName = file.getName();
         // Return files only (not directories) and skip hidden files
         return file.isFile() && !fileName.startsWith(HIDDEN_PREFIX);
      }
   };
   /**
    * Folder (directories) filter.
    *
    * @autho r paulburke
    */
   public static FileFilter sDirFilter = new FileFilter() {
      @Override
      public boolean accept(File file) {
         final String fileName = file.getName();
         // Return directories only and skip hidden directories
         return file.isDirectory() && !fileName.startsWith(HIDDEN_PREFIX);
      }
   };
   /**
    * TAG for log messages.
    */
   static final String TAG = "FileUtils";
   private static final boolean DEBUG = false; // Set to true to enable logging
   /**
    * File and folder comparator. TODO Expose sorting option method
    *
    * @author paulburke
    */
   public static Comparator<File> sComparator = new Comparator<File>() {
      @Override
      public int compare(File f1, File f2) {
         // Sort alphabetically by lower case, which is much cleaner
         return f1.getName().toLowerCase().compareTo(
                 f2.getName().toLowerCase());
      }
   };
   //   public static Context appContext = null;
//   public static RoomActivity roomActivity = null;
//   public static SharedPreferences preferences;

   private Utils() {
   } //private constructor to enforce Singleton pattern

   /**
    * Gets the extension of a file name, like ".png" or ".jpg".
    *
    * @param uri
    * @return Extension including the dot("."); "" if there is no extension;
    * null if uri was null.
    */
   public static String getExtension(String uri) {
      if (uri == null) {
         return null;
      }

      int dot = uri.lastIndexOf(".");
      if (dot >= 0) {
         return uri.substring(dot);
      } else {
         // No extension.
         return "";
      }
   }

   /**
    * @return Whether the URI is a local one.
    */
   public static boolean isLocal(String url) {
      if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
         return true;
      }
      return false;
   }

   /**
    * @return True if Uri is a MediaStore Uri.
    * @author paulburke
    */
   public static boolean isMediaUri(Uri uri) {
      return "media".equalsIgnoreCase(uri.getAuthority());
   }

   /**
    * Convert File into Uri.
    *
    * @param file
    * @return uri
    */
   public static Uri getUri(File file) {
      if (file != null) {
         return Uri.fromFile(file);
      }
      return null;
   }

   /**
    * Returns the path only (without file name).
    *
    * @param file
    * @return
    */
   public static File getPathWithoutFilename(File file) {
      if (file != null) {
         if (file.isDirectory()) {
            // no file to be split off. Return everything
            return file;
         } else {
            String filename = file.getName();
            String filepath = file.getAbsolutePath();

            // Construct path without file name.
            String pathwithoutname = filepath.substring(0,
                    filepath.length() - filename.length());
            if (pathwithoutname.endsWith("/")) {
               pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
            }
            return new File(pathwithoutname);
         }
      }
      return null;
   }

   /**
    * @return The MIME type for the given file.
    */
   public static String getMimeType(File file) {

      String extension = getExtension(file.getName());

      if (extension.length() > 0)
         return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));

      return "application/octet-stream";
   }

   /**
    * @return The MIME type for the give Uri.
    */
   public static String getMimeType(Context context, Uri uri) {
      File file = new File(getPath(context, uri));
      return getMimeType(file);
   }

   /**
    * @param uri The Uri to check.
    * @return Whether the Uri authority is {@link LocalStorageProvider}.
    * @author paulburke
    */
   public static boolean isLocalStorageDocument(Uri uri) {
      return LocalStorageProvider.AUTHORITY.equals(uri.getAuthority());
   }

   /**
    * @param uri The Uri to check.
    * @return Whether the Uri authority is ExternalStorageProvider.
    * @author paulburke
    */
   public static boolean isExternalStorageDocument(Uri uri) {
      return "com.android.externalstorage.documents".equals(uri.getAuthority());
   }

   /**
    * @param uri The Uri to check.
    * @return Whether the Uri authority is DownloadsProvider.
    * @author paulburke
    */
   public static boolean isDownloadsDocument(Uri uri) {
      return "com.android.providers.downloads.documents".equals(uri.getAuthority());
   }

   /**
    * @param uri The Uri to check.
    * @return Whether the Uri authority is MediaProvider.
    * @author paulburke
    */
   public static boolean isMediaDocument(Uri uri) {
      return "com.android.providers.media.documents".equals(uri.getAuthority());
   }

   /**
    * @param uri The Uri to check.
    * @return Whether the Uri authority is Google Photos.
    */
   public static boolean isGooglePhotosUri(Uri uri) {
      return "com.google.android.apps.photos.content".equals(uri.getAuthority());
   }

   /**
    * Get the value of the data column for this Uri. This is useful for
    * MediaStore Uris, and other file-based ContentProviders.
    *
    * @param uri           The Uri to query.
    * @param selection     (Optional) Filter used in the query.
    * @param selectionArgs (Optional) Selection arguments used in the query.
    * @return The value of the _data column, which is typically a file path.
    * @author paulburke
    */
   public static String getDataColumn(Context context, Uri uri, String selection,
                                      String[] selectionArgs) {

      Cursor cursor = null;
      final String column = "_data";
      final String[] projection = {
              column
      };

      try {
         cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                 null);
         if (cursor != null && cursor.moveToFirst()) {
            if (DEBUG)
               DatabaseUtils.dumpCursor(cursor);

            final int column_index = cursor.getColumnIndexOrThrow(column);
            return cursor.getString(column_index);
         }
      } finally {
         if (cursor != null)
            cursor.close();
      }
      return null;
   }

   /**
    * Get a file path from a Uri. This will get the the path for Storage Access
    * Framework Documents, as well as the _data field for the MediaStore and
    * other file-based ContentProviders.<br>
    * <br>
    * Callers should check whether the path is local before assuming it
    * represents a local file.
    *
    * @param uri The Uri to query.
    * @author paulburke
    * @see #isLocal(String)
    */
   public static String getPath(Context context, final Uri uri) {

      if (DEBUG)
         Log.d(TAG + " File -",
                 "Authority: " + uri.getAuthority() +
                         ", Fragment: " + uri.getFragment() +
                         ", Port: " + uri.getPort() +
                         ", Query: " + uri.getQuery() +
                         ", Scheme: " + uri.getScheme() +
                         ", Host: " + uri.getHost() +
                         ", Segments: " + uri.getPathSegments().toString()
         );

      final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

      // DocumentProvider
      if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
         // LocalStorageProvider
         if (isLocalStorageDocument(uri)) {
            // The path is the id
            return DocumentsContract.getDocumentId(uri);
         }
         // ExternalStorageProvider
         else if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
               return Environment.getExternalStorageDirectory() + "/" + split[1];
            }

            // TODO handle non-primary volumes
         }
         // DownloadsProvider
         else if (isDownloadsDocument(uri)) {

            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            return getDataColumn(context, contentUri, null, null);
         }
         // MediaProvider
         else if (isMediaDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
               contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
               contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
               contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{
                    split[1]
            };

            return getDataColumn(context, contentUri, selection, selectionArgs);
         }
      }
      // MediaStore (and general)
      else if ("content".equalsIgnoreCase(uri.getScheme())) {

         // Return the remote address
         if (isGooglePhotosUri(uri))
            return uri.getLastPathSegment();

         return getDataColumn(context, uri, null, null);
      }
      // File
      else if ("file".equalsIgnoreCase(uri.getScheme())) {
         return uri.getPath();
      }

      return null;
   }

   /**
    * Convert Uri into File, if possible.
    *
    * @return file A local file that the Uri was pointing to, or null if the
    * Uri is unsupported or pointed to a remote resource.
    * @author paulburke
    */
   public static File getFile(Context context, Uri uri) {
      if (uri != null) {
         String path = getPath(context, uri);
         if (path != null && isLocal(path)) {
            return new File(path);
         }
      }
      return null;
   }

   /**
    * Get the file size in a human-readable string.
    *
    * @param size
    * @return
    * @author paulburke
    */
   public static String getReadableFileSize(int size) {
      final int BYTES_IN_KILOBYTES = 1024;
      final DecimalFormat dec = new DecimalFormat("###.#");
      final String KILOBYTES = " KB";
      final String MEGABYTES = " MB";
      final String GIGABYTES = " GB";
      float fileSize = 0;
      String suffix = KILOBYTES;

      if (size > BYTES_IN_KILOBYTES) {
         fileSize = size / BYTES_IN_KILOBYTES;
         if (fileSize > BYTES_IN_KILOBYTES) {
            fileSize = fileSize / BYTES_IN_KILOBYTES;
            if (fileSize > BYTES_IN_KILOBYTES) {
               fileSize = fileSize / BYTES_IN_KILOBYTES;
               suffix = GIGABYTES;
            } else {
               suffix = MEGABYTES;
            }
         }
      }
      return String.valueOf(dec.format(fileSize) + suffix);
   }

//    public static Uri getUri(Context appContext, Bitmap bitmapSoftReference) {
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        bitmapSoftReference.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(appContext.getContentResolver(), bitmapSoftReference, "img", null);
//        return Uri.parse(path);
//    }

   /**
    * Attempt to retrieve the thumbnail of given File from the MediaStore. This
    * should not be called on the UI thread.
    *
    * @param file
    * @return
    * @author paulburke
    */
   public static Bitmap getThumbnail(Context context, File file) {
      return getThumbnail(context, getUri(file), getMimeType(file));
   }

   /**
    * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
    * should not be called on the UI thread.
    *
    * @param uri
    * @return
    * @author paulburke
    */
   public static Bitmap getThumbnail(Context context, Uri uri) {
      return getThumbnail(context, uri, getMimeType(context, uri));
   }

   /**
    * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
    * should not be called on the UI thread.
    *
    * @param uri
    * @param mimeType
    * @return
    * @author paulburke
    */
   public static Bitmap getThumbnail(Context context, Uri uri, String mimeType) {
      if (DEBUG)
         Log.d(TAG, "Attempting to get thumbnail");

      if (!isMediaUri(uri)) {
         Log.e(TAG, "You can only retrieve thumbnails for images and videos.");
         return null;
      }

      Bitmap bm = null;
      if (uri != null) {
         final ContentResolver resolver = context.getContentResolver();
         Cursor cursor = null;
         try {
            cursor = resolver.query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
               final int id = cursor.getInt(0);
               if (DEBUG)
                  Log.d(TAG, "Got thumb ID: " + id);

               if (mimeType.contains("video")) {
                  bm = MediaStore.Video.Thumbnails.getThumbnail(
                          resolver,
                          id,
                          MediaStore.Video.Thumbnails.MINI_KIND,
                          null);
               } else if (mimeType.contains(Utils.MIME_TYPE_IMAGE)) {
                  bm = MediaStore.Images.Thumbnails.getThumbnail(
                          resolver,
                          id,
                          MediaStore.Images.Thumbnails.MINI_KIND,
                          null);
               }
            }
         } catch (Exception e) {
            if (DEBUG)
               Log.e(TAG, "getThumbnail", e);
         } finally {
            if (cursor != null)
               cursor.close();
         }
      }
      return bm;
   }

   /**
    * Get the Intent for selecting content to be used in an Intent Chooser.
    *
    * @return The intent for opening a file with Intent.createChooser()
    * @author paulburke
    */
   public static Intent createGetContentIntent() {
      // Implicitly allow the user to select a particular kind of data
      final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
      // The MIME data type filter
      intent.setType("*/*");
      // Only return URIs that can be opened with ContentResolver
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      return intent;
   }

   public static String saveByTime(Bitmap bitmap) throws IOException {
      saveBitmap(bitmap, getSavedDir(), getTime());
      return getSavedDir() + "/" + getTime() + ".png";
   }

   public static File saveBitmap(Bitmap bitmap, String dirPath, String fileName) throws IOException {
      File dir = new File(dirPath);
      dir.mkdirs();
      File file = new File(dir, fileName + ".png");
      if (file.exists())
         file.delete();
      FileOutputStream fOut = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
      fOut.flush();
      fOut.close();
      return file;
   }

   public static Bitmap getBitmapByPath(String path) throws Exception {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inPreferredConfig = Bitmap.Config.ARGB_8888;
      return BitmapFactory.decodeFile(path, options);
   }

//   public static boolean roomActivityExists(Context context) {
//      SharedPreferences preferences = context.
//              getSharedPreferences("preferences", Context.MODE_PRIVATE);
//      return Utils.preferences.getBoolean("destroyed", true);
//   }

   public static String getSavedDir() {
      return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Drawingame/saved";
   }

   public static String getCachedDir() {
      return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Drawingame/cached";
   }

   public static void toast(final Context context, final String s) {
//      if (activity == null) //|| activity.isDestroyed)
//         return;
//      activity.runOnUiThread(new Runnable() {
//         @Override
//         public void run() {
      Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
//         }
//      });
   }

//   public static void toast(final DrawingActivity activity, final String s) {
//      if (activity == null || activity.isDestroyed)
//         return;
//      activity.runOnUiThread(new Runnable() {
//         @Override
//         public void run() {
//            Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
//         }
//      });
//   }

   public static void showErrorWithListenerDialog(final String message, final
   MyAlertDialog.OnDismissedListener onDismissedListener, final RoomActivity roomActivity) {
//      if (roomActivityExists())
      roomActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
            MyAlertDialog.showErrorWithListener(message, onDismissedListener, roomActivity);
         }
      });
   }

   public static void showErrorDialog(final String message, final RoomActivity roomActivity) {
//      if (roomActivityExists())
      roomActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
            MyAlertDialog.showError(message, roomActivity);
         }
      });
   }

   public static void showConfirmActionDialog(final String message, final
   MyAlertDialog.OnDismissedListener onDismissedListener, final RoomActivity roomActivity) {
//      if (roomActivityExists())
      roomActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
            MyAlertDialog.showConfirmAction(message, onDismissedListener, roomActivity);
         }
      });
   }

   public static void showRetryActionDialog(final String message, final
   MyAlertDialog.OnDismissedListener onDismissedListener, final RoomActivity roomActivity) {
//      if (roomActivityExists())
      roomActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
            MyAlertDialog.showRetryAction(message, onDismissedListener, roomActivity);
         }
      });
   }

   public static boolean isNoInternet(Context context) {
      try {
         ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
         return activeNetwork == null || !activeNetwork.isAvailable()
                 || !activeNetwork.isConnected();
      } catch (Exception e) {
         return true;
      }
   }

   public static int dpToPx(int dp, Context context) {
      return Math.round(dp * (context.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
   }

   //picture of w*h -> picture of W*h1 or w1*H (proportional scaling)
   public static LinearLayout.LayoutParams scaleToLl(int w, int h, int W, int H) {
      float k;
      if ((float) h * W / w > H)
         k = (float) H / h;
      else
         k = (float) W / w;
      h *= k;
      w *= k;
      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(w, h);
      return layoutParams;
   }

//   public static RelativeLayout.LayoutParams scaleToRl(int w, int h, int W, int H) {
//      float k;
//      if ((float) h * W / w > H)
//         k = (float) H / h;
//      else
//         k = (float) W / w;
//      h *= k;
//      w *= k;
//      RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(w, h);
//      return layoutParams;
//   }

   public static int displayWidth;
   public static int displayHeight;

   public static void getDisplaySize(RoomActivity roomActivity) {
      Display display = roomActivity.getWindowManager().getDefaultDisplay();

      if (Build.VERSION.SDK_INT >= 17) {
         //new pleasant way to get real metrics
         DisplayMetrics realMetrics = new DisplayMetrics();
         display.getRealMetrics(realMetrics);
         displayWidth = realMetrics.widthPixels;
         displayHeight = realMetrics.heightPixels;

      } else if (Build.VERSION.SDK_INT >= 14) {
         //reflection for this weird in-between time
         try {
            Method mGetRawH = Display.class.getMethod("getRawHeight");
            Method mGetRawW = Display.class.getMethod("getRawWidth");
            displayWidth = (Integer) mGetRawW.invoke(display);
            displayHeight = (Integer) mGetRawH.invoke(display);
         } catch (Exception e) {
            //this may not be 100% accurate, but it's all we've got
            displayWidth = display.getWidth();
            displayHeight = display.getHeight();
//            Log.d("!", "Couldn't use reflection to get the real display metrics.");
         }

      } else {
         //This should be close, as lower API devices should not have window navigation bars
         displayWidth = display.getWidth();
         displayHeight = display.getHeight();
      }
   }

   public static Bitmap getResizedBitmap(Bitmap bitmap) {
      if (bitmap == null)
         return null;
      int maxSize = 1000;
      int width = bitmap.getWidth();
      int height = bitmap.getHeight();

      float bitmapRatio = (float) width / (float) height;
      if (bitmapRatio > 0) {
         width = maxSize;
         height = (int) (width / bitmapRatio);
      } else {
         height = maxSize;
         width = (int) (height * bitmapRatio);
      }
      return Bitmap.createScaledBitmap(bitmap, width, height, true);
   }

   public static String getNewId() {
      return getTime() + String.valueOf(new Random().nextInt());
   }

   public static String getTime() {
      SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy MMMM dd HH mm ss SSS");
      Date now = new Date();
      return sdfTime.format(now);
   }

//   public static void notifyAdapter(final RoomActivity roomActivity) {
//      if (roomActivityExists())
//      roomActivity.runOnUiThread(new Runnable() {
//         @Override
//         public void run() {
//            roomActivity.tcAdapter.notifyDataSetChanged();
//         }
//      });
//   }

   public static String nameToCodes(String s) {
      JSONArray charCodes = new JSONArray();
      for (int i = 0; i < s.length(); i++) {
         JSONObject charCode = new JSONObject();
         try {
            charCode.put("", (int) s.charAt(i));
         } catch (JSONException e) {
//                Log.d("!", "couldnt write to jsonPoint");
            e.printStackTrace();
         }
         charCodes.put(charCode);
      }
//        JSONObject string = new JSONObject();
//        try {
//            string.put("string", charCodes);
//        } catch (JSONException e) {
//            Log.d("!", "couldnt write to jsonLine");
//            e.printStackTrace();
//        }
      return charCodes.toString();
   }

   public static String nameFromCodes(String sCharCodes) {
      String s = "";
      try {
         JSONArray charCodes = new JSONArray(sCharCodes);
         for (int i = 0; i < charCodes.length(); i++) {
            JSONObject charCode = charCodes.getJSONObject(i);
            s += (char) charCode.getInt("");
         }
      } catch (JSONException e) {
//            Log.d("!", "couldnt create Line");
         e.printStackTrace();
      }
      return s;
   }

   public static String stringFromRes(Context context, int id) {
      return context.getString(id);
   }

   public static boolean banDateHasPast(String unbanDateStr) {
      if (unbanDateStr == null) return true;
      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
      try {
         Date unbanDate = sdf.parse(unbanDateStr);
         return new Date().after(unbanDate);
      } catch (ParseException e) {
         Log.d("!", "wrong date format");
         return true;
      }
   }

//   public static void showList(RoomActivity roomActivity) {
//      if (roomActivityExists())
//         roomActivity.showList();
//   }

//   public static void showProgress(RoomActivity roomActivity) {
//      if (roomActivityExists())
//         roomActivity.showProgress();
//   }
}
