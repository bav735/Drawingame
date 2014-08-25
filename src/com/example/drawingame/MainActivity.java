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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.example.drawingame.AmbilWarnaDialog.OnAmbilWarnaListener;
import com.example.drawingame.ChangeWidthDialog.OnChangeWidthListener;
import ibt.ortc.api.Ortc;
import ibt.ortc.extensibility.*;

public class MainActivity extends Activity {
    public final static String serverUrl = "http://ortc-developers.realtime.co/server/2.1/";
    public final static String connectionMetadata = "AndroidApp";
    public final static String ortcType = "IbtRealtimeSJ";
    public final static String applicationKey ="XEQyNG";
    public final static String authenticationToken = "m8vUKz6sRvzw";
    public final static String channelName = "drawingameChannel";

    public MainActivity mainActivity = this;
    public DrawView drawView;
    public Client client;

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
        setContentView(R.layout.activity_main);
        if (!isNetworkAvailable()) {
            ConnectionDialog cd = new ConnectionDialog(mainActivity);
            cd.show();
        }
        drawView = (DrawView) findViewById(R.id.drawView1);
        drawView.init(mainActivity);
        try {
            client = new Client(mainActivity);
        } catch (Exception e) {
            toast("Client create error - "+ e.toString());
            Log.d("!", "Client create error - " + e.toString());
            ConnectionDialog cd = new ConnectionDialog(mainActivity);
            cd.show();
        }

    }



    private void toast(final String s) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainActivity, s, 0).show();
            }
        });

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
