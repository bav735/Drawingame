package classes.example.drawingame.imgur;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
        Thread download = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL img_value = null;
                    img_value = new URL(url);
                    Bitmap bitmap = null;
                    bitmap = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
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
