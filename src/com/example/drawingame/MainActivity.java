package com.example.drawingame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drawingame.AmbilWarnaDialog.OnAmbilWarnaListener;

public class MainActivity extends Activity {

	public MainActivity mainActivity = this;
	public DrawView drawView;
	public String ipa;
	public final int port = 4445;
	public String deviceName = android.os.Build.MODEL;
	public Client client;

	private LinearLayout llMain;
	private LinearLayout llServer;
	private LinearLayout llScroll;
	private Button btnSend;
	private Button btnUndo;
	private Button btnRandom;
	private Button btnPickColor;
	private Button btnContinuous;
	private Button btnClear;
	private Button btnSaveDraw;
	private Button btnTwitter;

	private boolean isServer = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!isNetworkAvailable()) {
			ConnectionDialog cd = new ConnectionDialog(mainActivity);
			cd.show();
		}

		llMain = new LinearLayout(mainActivity);
		llMain.setOrientation(LinearLayout.VERTICAL);
		setContentView(llMain);

		llServer = new LinearLayout(mainActivity);
		llMain.addView(llServer);

		TextView tvSetServer = new TextView(mainActivity);
		tvSetServer.setText("Set device as a server?");
		llServer.addView(tvSetServer);

		Button btnServerYes = new Button(mainActivity);
		btnServerYes.setText("Yes");
		btnServerYes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ipa = getLocalIpAddress();
				new Server(mainActivity, port).start();
				isServer = true;
				createClient();
				llMain.removeView(llServer);
			}
		});
		llServer.addView(btnServerYes);

		Button btnServerNo = new Button(mainActivity);
		btnServerNo.setText("No");
		btnServerNo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				llMain.removeView(llServer);

				final EditText etAdress = new EditText(mainActivity);
				etAdress.setHint("Set network IP address");
				llMain.addView(etAdress);

				final Button btnSetAddress = new Button(mainActivity);
				btnSetAddress.setText("Set");
				btnSetAddress.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String s = etAdress.getText().toString();
						if (new IPAddressValidator().validate(s)) {
							ipa = s;
							toast("IP was set: " + s);
							llMain.removeView(btnSetAddress);
							llMain.removeView(etAdress);
							createClient();
						} else {
							toast("Enter correct IP address");
						}
					}
				});
				llMain.addView(btnSetAddress);

			}
		});
		llServer.addView(btnServerNo);
	}

	// private void disableInteraction() {
	// btnSave.setVisibility(View.INVISIBLE);
	// btnUndo.setVisibility(View.INVISIBLE);
	// btnRandom.setVisibility(View.INVISIBLE);
	// drawView.isEnabled = false;
	// }

	private void createClient() {
		drawView = new DrawView(mainActivity);
		// drawView = (DrawView) findViewById(R.id.drawView);
		// drawView.init(mainActivity);
		// Display display = getWindowManager().getDefaultDisplay();
		drawView.setLayoutParams(new LayoutParams(400, 400));

		llMain.addView(drawView);

		client = new Client(mainActivity);
		client.start();

		ScrollView scrollView = new ScrollView(mainActivity);
		llMain.addView(scrollView);

		llScroll = new LinearLayout(mainActivity);
		llScroll.setOrientation(LinearLayout.VERTICAL);
		scrollView.addView(llScroll);

		btnUndo = new Button(mainActivity);
		btnUndo.setText("Undo");
		btnUndo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				drawView.undo();
			}
		});
		llScroll.addView(btnUndo);

		if (isServer) {
			btnClear = new Button(mainActivity);
			btnClear.setText("Clear drawing");
			btnClear.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					drawView.clear();
					client.send();
				}
			});
			llScroll.addView(btnClear);
		}

		btnSend = new Button(mainActivity);
		btnSend.setText("Send");
		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				client.send();
			}
		});
		llScroll.addView(btnSend);

		btnContinuous = new Button(mainActivity);
		btnContinuous.setText("Continuous drawing is off");
		btnContinuous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeBtnContinuous();
			}
		});
		llScroll.addView(btnContinuous);

		btnRandom = new Button(mainActivity);
		btnRandom.setText("Random color pick is off");
		btnRandom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeBtnRandom();
			}
		});
		llScroll.addView(btnRandom);

		btnPickColor = new Button(mainActivity);
		btnPickColor.setText("Pick color");
		btnPickColor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(mainActivity,
						drawView.drawingColor, new OnAmbilWarnaListener() {
							@Override
							public void onOk(AmbilWarnaDialog dialog, int color) {
								if (drawView.isRandom)
									changeBtnRandom();
								drawView.drawingColor = color;
							}

							@Override
							public void onCancel(AmbilWarnaDialog dialog) {
							}
						});
				dialog.show();
			}
		});
		llScroll.addView(btnPickColor);

		btnSaveDraw = new Button(mainActivity);
		btnSaveDraw.setText("Save drawing to disk");
		btnSaveDraw.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("!", "clicked!!!");
				drawView.save();
			}
		});
		llScroll.addView(btnSaveDraw);

		btnTwitter = new Button(mainActivity);
		btnTwitter.setText("Tweet");
		btnTwitter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				drawView.save();
				startActivity(new Intent(mainActivity, TwitterActivity.class));
			}
		});
		llScroll.addView(btnTwitter);
	}

	private void toast(String s) {
		Toast.makeText(mainActivity, s, 0).show();
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork == null || !activeNetwork.isAvailable()
				|| !activeNetwork.isConnected()) {
			return false;
		}
		return true;
	}

	public String getLocalIpAddress() {
		WifiManager wm = (WifiManager) mainActivity
				.getSystemService(WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo()
				.getIpAddress());
		return ip;
	}

	private void changeBtnRandom() {
		if (drawView.isRandom) {
			btnRandom.setText("Random color pick is off");
			drawView.isRandom = false;
		} else {
			btnRandom.setText("Random color pick is on");
			drawView.isRandom = true;
		}
	}

	private void changeBtnContinuous() {
		if (drawView.isContinuous) {
			btnContinuous.setText("Continuous drawing is off");
			drawView.isContinuous = false;
		} else {
			btnContinuous.setText("Continuous drawing is on");
			drawView.isContinuous = true;
		}
	}

}
