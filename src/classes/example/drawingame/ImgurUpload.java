package classes.example.drawingame;

import android.app.Activity;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;

/**
 * Uploads img to imgur.com
 */

public class ImgurUpload {
    private final static String DESTINATION_URL = "https://api.imgur.com/3/upload.json";
    private final static String CLIENT_ID = "2f63af75626941e";
    private final static String CLIENT_SECRET = "78717ae0d1cace8e94033b646bd290327c9d3ae6";

    public interface OnImgUrlReceivedListener {
        void onImgUrlReceived(String imgUrl);
    }

    private Activity activity;
    private OnImgUrlReceivedListener listener;
    private HttpPost httpPost;
    private HttpClient httpClient;
    private HttpContext localContext;

    public ImgurUpload(Activity activity, OnImgUrlReceivedListener listener) {
        this.activity = activity;
        this.listener = listener;
        httpPost = new HttpPost(DESTINATION_URL);
        httpClient = new DefaultHttpClient();
        localContext = new BasicHttpContext();
    }

    public void start(final String imgPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    Toaster.toast(activity, "started upload");
                    httpPost.setHeader("Authorization", "Client-ID " + CLIENT_ID);//API_key
                    final MultipartEntity entity = new MultipartEntity(
                            HttpMultipartMode.BROWSER_COMPATIBLE);
                    entity.addPart("image", new FileBody(new File(imgPath)));
                    entity.addPart("key", new StringBody(CLIENT_ID));//API_key
                    httpPost.setEntity(entity);
                    HttpResponse response = httpClient.execute(httpPost,
                            localContext);
                    String responseString = EntityUtils.toString(response
                            .getEntity());
                    final JSONObject json = new JSONObject(responseString);
                    String imgUrl = json.getJSONObject("data").getString("link");
                    listener.onImgUrlReceived(imgUrl);
//                    Show.toast(activity, "uploaded!");
                } catch (Exception e) {
                    Show.toast(activity, "exception : " + e.toString());
                }
            }
        }).start();
    }
}
