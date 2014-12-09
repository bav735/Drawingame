package classes.example.drawingame.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.io.ByteArrayOutputStream;
import java.io.File;

import classes.example.drawingame.R;
import classes.example.drawingame.views.DrawView;

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
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        imgPath = intent.getStringExtra("path");
        getActionBar().setDisplayShowHomeEnabled(false);
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
    }

    private void getCredentials() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    requestToken = service.getRequestToken();
                } catch (OAuthException e) {
                    toast(getString(R.string.repostDrawingFail));
                    toast("FROM GET_CREDENTIALS");
                    toast(e.toString());
                    toast(e.toString());
                    twitterActivity.finish();
                }

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
                toast(getString(R.string.repostDrawingSuccess));
            } else {
                toast(getString(R.string.repostDrawingFail));
            }
        } catch (Exception e) {
            toast(getString(R.string.repostDrawingFail));
            toast("FROM GET_INFORMATION");
            twitterActivity.finish();
        }

        twitterActivity.finish();
    }

    private void toast(final String string) {
        twitterActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(twitterActivity, string, Toast.LENGTH_LONG).show();
            }
        });
    }
}
