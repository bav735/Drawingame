package classes.example.drawingame.imgur;

import android.graphics.Bitmap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Uploads img to imgur.com
 */

public class ImgurUpload {
    private final static String DESTINATION_URL = "https://api.imgur.com/3/upload.json";
    private final static String CLIENT_ID = "2f63af75626941e";
//    private final static String CLIENT_SECRET = "78717ae0d1cace8e94033b646bd290327c9d3ae6";

    //    public static boolean isUploading = false;
    private OnImgUrlReceivedListener listener;
    private HttpPost httpPost;
    private HttpClient httpClient;
    private HttpContext localContext;

    public ImgurUpload(OnImgUrlReceivedListener listener) {
        this.listener = listener;
        httpPost = new HttpPost(DESTINATION_URL);
        httpClient = new DefaultHttpClient();
        localContext = new BasicHttpContext();
    }

    public void start(final Bitmap bitmap) {
        Thread upload = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    httpPost.setHeader("Authorization", "Client-ID " + CLIENT_ID);//API_key

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    byte[] dataBytes = bos.toByteArray();
                    ByteArrayBody body = new ByteArrayBody(dataBytes, "image.jpg");

                    final MultipartEntity entity = new MultipartEntity(
                            HttpMultipartMode.BROWSER_COMPATIBLE);
                    entity.addPart("image", body);//new FileBody(new File(imgPath)));
                    entity.addPart("key", new StringBody(CLIENT_ID));//API_key

                    httpPost.setEntity(entity);
                    HttpResponse response = httpClient.execute(httpPost,
                            localContext);
                    String responseString = EntityUtils.toString(response
                            .getEntity());
                    final JSONObject json = new JSONObject(responseString);
                    final String imgUrl = json.getJSONObject("data").getString("link");
                    listener.onImgUrlReceived(imgUrl);
                } catch (Exception e) {
                    listener.onImgUrlReceived(null);
                }
            }
        });
        upload.start();
    }

    public interface OnImgUrlReceivedListener {
        void onImgUrlReceived(String imgUrl);
    }
}