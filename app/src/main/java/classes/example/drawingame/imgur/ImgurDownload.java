package classes.example.drawingame.imgur;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.net.URL;

/**
 * Created by A on 26.12.2014.
 */
public class ImgurDownload {
   private OnImgReceivedListener listener;

   public ImgurDownload(OnImgReceivedListener listener) {
      this.listener = listener;
   }

   public void start(final String url) {
//      Log.d("!", "downloaded item " + url);
      Thread download = new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               URL img_value = new URL(url);
//               SoftReference<Bitmap> bitmapSoftReference = new SoftReference<>(BitmapFactory.
//                       decodeStream(img_value.openConnection().getInputStream()));
               Bitmap bitmap = BitmapFactory.decodeStream(
                       img_value.openConnection().getInputStream());
               listener.onImgReceived(bitmap);
            } catch (Exception e) {
               listener.onImgReceived(null);
            }
         }
      });
      download.start();
   }

   public interface OnImgReceivedListener {
      void onImgReceived(Bitmap bitmap);
   }
}
