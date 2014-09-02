package com.example.drawingame;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.example.drawingame.AmbilWarnaDialog.OnAmbilWarnaListener;
import com.example.drawingame.ChangeWidthDialog.OnChangeWidthListener;

public class MainActivity extends FragmentActivity {
    public final static String serverUrl = "http://ortc-developers.realtime.co/server/2.1/";
    public final static String ortcType = "IbtRealtimeSJ";
    public final static String applicationKey = "XEQyNG";
    public final static String privateKey = "m8vUKz6sRvzw";
    public static String clientName;
    public static String channelName;

    public DrawView drawView;
    public Client client;

    private MainActivity mainActivity = this;
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
        inflater.inflate(R.menu.activity_main_menu, menu);
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
            RetryDialog cd = new RetryDialog(mainActivity, "Network is unavailable, check internet connection.");
            cd.show();
        } else {
            drawView = (DrawView) findViewById(R.id.drawView1);
            drawView.init(mainActivity);
            DialogFragment dialogFragment = new ClientDialog();
        dialogFragment.setCancelable(false);
        dialogFragment.show(getSupportFragmentManager(), "!");
        }
    });

//            try {
//                client = new Client(mainActivity);
////        } catch (OrtcNotConnectedException e) {
////            Log.d("!", "OrtcNotConnectedException - " + e.toString());
////            RetryDialog cd = new RetryDialog(mainActivity, "OrtcNotConnectedException");
////            cd.show();
//            } catch (Exception e) {
//                Log.d("!", "Client create error - " + e.toString());
//                RetryDialog cd = new RetryDialog(mainActivity, "Couldn't create client on your device.");
//                cd.show();
//            }
        /*try {
            storage = new Storage(mainActivity);
        } catch (StorageException e) {
            Log.d("!", "Storage create error - " + e.toString());
            RetryDialog cd = new RetryDialog(mainActivity, "Couldn't create storage on your device.");
            cd.show();
        }*/
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
}
