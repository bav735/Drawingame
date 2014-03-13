package com.example.drawingame;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

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

public class TwitterActivity extends Activity {// extends BaseActivity {

	// private static final String JSON_TWEETS = "json tweets";
	private static final String TOKEN_SECRET = "token secret";
	private static final String ACCESS_TOKEN = "access token";
	// private static final String TAG = "tag";

	private final static String API_KEY = "mjIZHMMse8FOjBBhpSoNDA";
	private final static String API_SECRET = "8eOITf6gzf5sKbNPvZGjgIMr3nvNY9nB0UwkBZbkgQ";
	private final static String CALLBACK = "http://google.com";

	private TwitterActivity twitterActivity = this;
	// private SharedPreferences prefs;
	private Token requestToken;
	private Token accessToken;
	private WebView webView;
	private ProgressBar progressBar;
	private String authUrl;
	private OAuthService service;

	private WebViewClient client = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(final WebView webView,
				String url) {
			if (url.startsWith(CALLBACK)) {
				String verifier = Uri.parse(url).getQueryParameter(
						"oauth_verifier");
				final Verifier v = new Verifier(verifier);
				new Thread(new Runnable() {
					@Override
					public void run() {
						accessToken = service.getAccessToken(requestToken, v);
						// prefs.edit()
						// .putString(ACCESS_TOKEN, accessToken.getToken())
						// .putString(TOKEN_SECRET,
						// accessToken.getSecret()).commit();

						twitterActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								swapViews();
							}
						});
						getInformation();
						twitterActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								swapViews();
							}
						});
					}
				}).start();
			} else {
				swapViews();
				webView.loadUrl(url);
			}
			return true;
		};

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			super.onReceivedSslError(view, handler, error);
			handler.proceed();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			swapViews();
		}
	};

	private void swapViews() {
		if (webView.getVisibility() == View.VISIBLE) {
			progressBar.setVisibility(View.VISIBLE);
			webView.setVisibility(View.INVISIBLE);
		} else {
			progressBar.setVisibility(View.INVISIBLE);
			webView.setVisibility(View.VISIBLE);
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prefs = PreferenceManager.getDefaultSharedPreferences(this);
		service = new ServiceBuilder().provider(TwitterApi.SSL.class)
				.apiKey(API_KEY).apiSecret(API_SECRET).callback(CALLBACK)
				.build();

		// if (prefs.contains(ACCESS_TOKEN)) {
		// accessToken = new Token(prefs.getString(ACCESS_TOKEN, null),
		// prefs.getString(TOKEN_SECRET, null));
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// getInformation();
		// }
		// }).start();
		// } else {
		setContentView(R.layout.activity_twitter);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		webView = (WebView) findViewById(R.id.webView);
		swapViews();

		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.setWebViewClient(client);
		getCredentials();
		// }
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
				authUrl = service.getAuthorizationUrl(requestToken);
				// toast(authUrl);
				twitterActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						webView.loadUrl(authUrl);
					}
				});
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
		OAuthRequest request = new OAuthRequest(Verb.GET,
				"https://api.twitter.com/1.1/account/verify_credentials.json");
		service.signRequest(accessToken, request);
		Response response = request.send();
		try {
			JSONObject jsonUserInfo = new JSONObject(response.getBody());
			toast("You logged in as " + jsonUserInfo.getString("name"));
		} catch (JSONException e) {
			toast(e.toString());
			Log.d("!", e.toString());
		}

		request = new OAuthRequest(Verb.POST,
				"https://api.twitter.com/1.1/statuses/update_with_media.json");
		try {
			MultipartEntity entity = new MultipartEntity();
			entity.addPart("status", new StringBody(
					"A picture was posted by Android app Drawingame:"));
			entity.addPart("media", new FileBody(new File(
					"/mnt/sdcard/Drawing.png")));

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			entity.writeTo(out);

			request.addPayload(out.toByteArray());
			request.addHeader(entity.getContentType().getName(), entity
					.getContentType().getValue());

			service.signRequest(accessToken, request);
			response = request.send();
			if (response.isSuccessful()) {
				toast("Drawing was posted");
			} else {
				toast("Error while posting drawing");
				Log.d("!", response.getBody());
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		twitterActivity.finish();
		// Intent intent = new Intent(this, ShowTweets.class);
		// intent.putExtra("jsonUserInfo", response.getBody());

		// request = new OAuthRequest(Verb.GET,
		// "https://api.twitter.com/1.1/statuses/home_timeline.json");
		// service.signRequest(accessToken, request);
		// response = request.send();
		// try {
		// JSONObject jsonUserInfo = new JSONObject(response.getBody());
		// toast("You logged in as ");
		// // jsonUserInfo.getString("screen_name"));
		// // toast(jsonUserInfo.toString());
		// } catch (JSONException e) {
		// toast(e.toString());
		// Log.d("!", e.toString());
		// }

		// startActivity(intent);
		// finish();

	}

	private void toast(final String string) {
		twitterActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(twitterActivity, string, 0).show();
			}
		});
	}

	// private void sleep() {
	// try {
	// Thread.sleep(2500);
	// } catch (Exception e) {
	// }
	// }
}
