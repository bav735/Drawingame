package com.example.drawingame;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.*;
import android.widget.*;
import com.example.drawingame.AmbilWarnaDialog.OnAmbilWarnaListener;
import com.example.drawingame.ChangeWidthDialog.OnChangeWidthListener;

public class MainActivity extends Activity {

    public MainActivity mainActivity = this;
    public DrawView drawView;
    public String ipa;
    public final int port = 4445;
    public String deviceName = android.os.Build.MODEL;
    public Client client;

    private HorizontalScrollView scrollView;
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
    private Button btnChangeWidth;

    private Menu mainActivityMenu;
    private MenuItem menuClearDrawing;
    private MenuItem menuContinuousCommit;
    private MenuItem menuRandomColor;
    private MenuItem menuRandomStrokeWidth;

    private boolean isServer = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mainActivityMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menuUndo:
                drawView.undo();
                return true;

            case R.id.menuClearDrawing:
                drawView.clear();
                return true;

            case R.id.menuCommitDrawing:
                client.send();
                if (drawView.isContinuous)
                    changeContinuous();
                return true;

            case R.id.menuContinuousCommit:
                changeContinuous();
                return true;

            case R.id.menuChooseColor:
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(mainActivity,
                        drawView.drawingColor, new OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        if (drawView.isRandomColor)
                            changeRandomColor();
                        drawView.drawingColor = color;
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                    }
                }
                );
                dialog.show();
                if (drawView.isRandomColor)
                    changeRandomColor();
                return true;

            case R.id.menuRandomColor:
                changeRandomColor();
                return true;

            case R.id.menuChooseStrokeWidth:
                ChangeWidthDialog changeWidthDialog = new ChangeWidthDialog(
                        mainActivity, new OnChangeWidthListener() {
                    @Override
                    public void onOk(float strokeWidth) {
                        drawView.strokeWidth = strokeWidth;
                    }
                }
                );
                changeWidthDialog.show();
                if (drawView.isRandomWidth)
                    changeRandomWidth();
                return true;

            case R.id.menuRandomStrokeWidth:
                changeRandomWidth();
                return true;

            case R.id.menuSaveDrawing:
                drawView.save();
                return true;

            case R.id.menuPostDrawing:
                drawView.save();
                startActivity(new Intent(mainActivity, TwitterActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();

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
                deviceName += " (Server)";
                createClient();
                if (isServer) {
                    changeClear();
                }
                getActionBar().show();
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
                            getActionBar().show();
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
        setContentView(R.layout.activity_main);
        // drawView = new DrawView(mainActivity);
        drawView = (DrawView) findViewById(R.id.drawView1);
        drawView.init(mainActivity);
        drawView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollView.setVisibility(View.INVISIBLE);
                return false;
            }
        });
        // drawView.init(mainActivity);
        // Display display = getWindowManager().getDefaultDisplay();

        // drawView.setLayoutParams(new LayoutParams(400, 400));

        // llMain.addView(drawView);

        client = new Client(mainActivity);
        client.start();

        // ScrollView scrollView = new ScrollView(mainActivity);
        scrollView = (HorizontalScrollView) findViewById(R.id.scrollView1);
        scrollView.setVisibility(View.INVISIBLE);
        // llMain.addView(scrollView);

        // llScroll = new LinearLayout(mainActivity);
        // llScroll.setOrientation(LinearLayout.VERTICAL);
        // scrollView.addView(llScroll);
        llScroll = (LinearLayout) findViewById(R.id.linearLayout);


    }

    /*@Override
    protected void onS
menuClearDrawing.setVisible(false);
    }
*/
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

    private void changeRandomColor() {
        menuRandomColor = mainActivityMenu.findItem(R.id.menuRandomColor);
        if (drawView.isRandomColor) {
            menuRandomColor.setTitle("Enable random color");
            drawView.isRandomColor = false;
        } else {
            menuRandomColor.setTitle("Disable random color");
            drawView.isRandomColor = true;
        }
    }

    private void changeRandomWidth() {
        menuRandomStrokeWidth = mainActivityMenu.findItem(R.id.menuRandomStrokeWidth);
        if (drawView.isRandomWidth) {
            menuRandomStrokeWidth.setTitle("Enable random width");
            drawView.isRandomWidth = false;
        } else {
            menuRandomStrokeWidth.setTitle("Disable random width");
            drawView.isRandomWidth = true;
        }
    }

    private void changeContinuous() {
        menuContinuousCommit = mainActivityMenu.findItem(R.id.menuContinuousCommit);
        if (drawView.isContinuous) {
            menuContinuousCommit.setTitle("Enable continuous commit");
            drawView.isContinuous = false;
        } else {
            menuContinuousCommit.setTitle("Disable continuous commit");
            drawView.isContinuous = true;
        }
    }

    private void changeClear() {
        menuClearDrawing = mainActivityMenu.findItem(R.id.menuClearDrawing);
        if (menuClearDrawing.isVisible())
            menuClearDrawing.setVisible(false);
        else
            menuClearDrawing.setVisible(true);
    }
}
