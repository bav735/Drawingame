package com.example.drawingame;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Realizes proccess of posting
 * drawing to Twitter (using
 * scribe lib)
 **/

public class TwitterActivity extends Activity {
    private final static String API_KEY = "R186VuHyi4iIgRwY5bwQ9YpKH";
    private final static String API_SECRET = "nkhldkKFqbizBabWBL1fQ8yPBF7WMCqam8dkS0lqjm9TjPm2Q7";
    private final static String CALLBACK = "http://drawingame.twitter";

    private TwitterActivity twitterActivity = this;
    private Token requestToken;
    private Token accessToken;
    private WebView webView;
    private ProgressBar progressBar;
    private String authUrl;
    private OAuthService service;

    private WebViewClient client = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(final WebView webView, String url) {
            if (url.startsWith(CALLBACK)) {
                String verifier = Uri.parse(url).getQueryParameter(
                        "oauth_verifier");
                final Verifier v = new Verifier(verifier);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        swapViews();
                        accessToken = service.getAccessToken(requestToken, v);
                        getInformation();
                        swapViews();
                    }
                }).start();
            } else {
                webView.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            super.onReceivedSslError(view, handler, error);
            handler.proceed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
        }
    };

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
        super.onCreate(savedInstanceState);
        service = new ServiceBuilder()
                .provider(TwitterApi.SSL.class)
                .apiKey(API_KEY)
                .apiSecret(API_SECRET)
                .callback(CALLBACK)
                .build();

        setContentView(R.layout.activity_twitter);
        progressBar = (ProgressBar) findViewById(R.id.seekBar);
        //progressBar.setVisibility(View.INVISIBLE);
        webView = (WebView) findViewById(R.id.webView);
        webView.setVisibility(View.INVISIBLE);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(client);
        getCredentials();
    }

    private void getCredentials() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    requestToken = service.getRequestToken();
                } catch (Exception e) {
                    toast(e.toString());
                    Log.d("!", e.toString());
                }

                Log.d("!", requestToken.toString());
                authUrl = service.getAuthorizationUrl(requestToken);

                twitterActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(authUrl);
                    }
                });
                swapViews();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    private void getInformation() {
        OAuthRequest request = new OAuthRequest(Verb.POST,
                "https://api.twitter.com/1.1/statuses/update_with_media.json");
        try {
            MultipartEntity entity = new MultipartEntity();
            entity.addPart("status", new StringBody(
                    "A picture was posted by Android app Drawingame:"));
            entity.addPart("media", new FileBody(new File(
                    "/mnt/sdcard/" + DrawView.tmpDrawingName + ".png")));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            entity.writeTo(out);

            request.addPayload(out.toByteArray());
            request.addHeader(entity.getContentType().getName(), entity
                    .getContentType().getValue());

            service.signRequest(accessToken, request);
            Response response = request.send();
            if (response.isSuccessful()) {
                toast(getString(R.string.repostDrawingSuccess));
            } else {
                toast(getString(R.string.repostDrawingFail));
                Log.d("!", response.getBody());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        twitterActivity.finish();
    }

    private void toast(final String string) {
        twitterActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(twitterActivity, string, 0).show();
            }
        });
    }
}
