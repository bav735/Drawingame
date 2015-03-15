package classes.example.drawingame.drawing_activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.io.ByteArrayOutputStream;
import java.io.File;

import classes.example.drawingame.R;
import classes.example.drawingame.room_activity.service.ListService;
import classes.example.drawingame.utils.MyAlertDialog;
import classes.example.drawingame.utils.Utils;

/**
 * Realizes proccess of posting
 * drawing to Twitter (using
 * scribe lib)
 */

public class TwitterActivity extends FragmentActivity {
   private final static String API_KEY = "EM2v6Ak3Gigj7XywqM34gvBLk";
   private final static String API_SECRET = "5qllqQLpiLfNqe6MEZ0cxBxILKNXsYAPejZGe3cePJlCAKmbJb";
   private final static String CALLBACK = "https://drawingame.twitter";
   private WebViewClient webViewClient = new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(final WebView webView, String url) {
         if (url.startsWith(CALLBACK)) {
            String verifier = Uri.parse(url).getQueryParameter(
                    "oauth_verifier");
            final Verifier v = new Verifier(verifier);
            new Thread(new Runnable() {
               @Override
               public void run() {
                  try {
                     swapViews();
                     accessToken = service.getAccessToken(requestToken, v);
                     getInformation();
                     swapViews();
                  } catch (Exception e) {
                     onException();
                  }
               }
            }).start();
         } else {
            webView.loadUrl(url);
         }
         return true;
      }

//        @Override
//        public void onReceivedSslError(WebView view, SslErrorHandler handler,
//                                       SslError error) {
//            super.onReceivedSslError(view, handler, error);
//            handler.proceed();
//        }

      @Override
      public void onPageFinished(WebView view, String url) {
      }
   };

   private void onException() {
      twitterActivity.finish();
      ListService.sendMessageShowErrorDialog(R.string.repostDrawingFail);
   }

   public volatile boolean isDestroyed = false;
   private TwitterActivity twitterActivity = this;
   private Token requestToken;
   private Token accessToken;
   private WebView webView;
   private ProgressBar progressBar;
   private String authUrl;
   private OAuthService service;
   private String imgPath;

   private void swapViews() {
      twitterActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
            if (webView.getVisibility() == View.VISIBLE) {
               progressBar.setVisibility(View.VISIBLE);
               webView.setVisibility(View.INVISIBLE);
            } else {
               progressBar.setVisibility(View.INVISIBLE);
               webView.setVisibility(View.VISIBLE);
            }
         }
      });
   }

   @SuppressLint("SetJavaScriptEnabled")
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      try {
         getWindow().getDecorView().setBackgroundColor(Color.BLACK);
         super.onCreate(savedInstanceState);
         Intent intent = getIntent();
         imgPath = intent.getStringExtra("path");
         service = new ServiceBuilder()
                 .provider(TwitterApi.SSL.class)
                 .apiKey(API_KEY)
                 .apiSecret(API_SECRET)
                 .callback(CALLBACK)
                 .build();

         setContentView(R.layout.activity_twitter);
         progressBar = (ProgressBar) findViewById(R.id.seekBar);
         webView = (WebView) findViewById(R.id.webView);
         webView.setVisibility(View.INVISIBLE);

         webView.getSettings().setJavaScriptEnabled(true);
         webView.getSettings().setDomStorageEnabled(true);
         webView.setWebViewClient(webViewClient);
         getCredentials();
      } catch (Exception e) {
         onException();
      }
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      isDestroyed = true;
   }

   private void getCredentials() {
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               requestToken = service.getRequestToken();
               authUrl = service.getAuthorizationUrl(requestToken);
               twitterActivity.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     webView.loadUrl(authUrl);
                  }
               });
               swapViews();
            } catch (Exception e) {
               onException();
            }
         }
      }).start();
   }

   @Override
   public void onBackPressed() {
      if (webView != null && webView.canGoBack()) {
         webView.goBack();
         return;
      } else
         finish();
      super.onBackPressed();
   }

   private void getInformation() throws Exception {
      OAuthRequest request = new OAuthRequest(Verb.POST,
              "https://api.twitter.com/1.1/statuses/update_with_media.json");
//        try {
      MultipartEntity entity = new MultipartEntity();
      entity.addPart("status", new StringBody(getString(R.string.repostMessage)));
      entity.addPart("media", new FileBody(new File(imgPath)));

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      entity.writeTo(baos);

      request.addPayload(baos.toByteArray());
      request.addHeader(entity.getContentType().getName(), entity
              .getContentType().getValue());

      service.signRequest(accessToken, request);
      Response response = request.send();
      if (response.isSuccessful()) {
         Utils.toast(getApplicationContext(), getString(R.string.repostDrawingSuccess));
      } else {
         ListService.sendMessageShowErrorDialog(R.string.repostDrawingFail);
      }
      twitterActivity.finish();
   }

}
