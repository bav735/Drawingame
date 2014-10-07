package com.example.drawingame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.example.drawingame.AmbilWarnaDialog.OnAmbilWarnaListener;
import com.example.drawingame.ChangeWidthDialog.OnChangeWidthListener;

import java.io.File;

public class MainActivity extends FragmentActivity {
    public DrawView drawView;
    public Client client;

    private MainActivity mainActivity = this;
    private Menu mainActivityMenu;
    //private MenuItem menuClearDrawing;
    //private MenuItem menuContinuousCommit;
    private MenuItem menuRandomColor;
    private MenuItem menuEraser;
    private MenuItem menuChangeStrokeWidth;
    private MenuItem menuRandomStrokeWidth;
    private MenuItem menuChangeColor;
//    private boolean isServer = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mainActivityMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        menuRandomStrokeWidth = mainActivityMenu.findItem(R.id.menuRandomStrokeWidth);
        menuEraser= mainActivityMenu.findItem(R.id.menuEraser);
        menuChangeStrokeWidth= mainActivityMenu.findItem(R.id.menuChangeStrokeWidth);
        menuRandomColor= mainActivityMenu.findItem(R.id.menuRandomColor);
        menuChangeColor= mainActivityMenu.findItem(R.id.menuChangeColor);

        switch (item.getItemId()) {
            case R.id.menuUndo:
                drawView.undo();
                return true;

            case R.id.menuEraser:
                changeEraser();
                return true;

//            case R.id.menuClearDrawing:
//                drawView.clear();
//                return true;

            case R.id.menuCommitDrawing:
                if (client.ortcClient.getIsConnected()) {
                    client.commitDrawing();
                    toast("Commit was sent");
                } else
                    toast("Client is not connected, check your network");
                //if (drawView.isContinuous)                    changeContinuous();
                return true;

//            case R.id.menuContinuousCommit:
//                changeContinuous();
//                return true;

            case R.id.menuChangeColor:
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

            case R.id.menuChangeStrokeWidth:
                ChangeWidthDialog changeWidthDialog = new ChangeWidthDialog(
                        mainActivity, new OnChangeWidthListener() {
                    @Override
                    public void onOk(int strokeWidth) {
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
                drawView.save("Drawing");
                toast("Drawing was saved to /sdcard as Drawing.png");
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File("mnt/sdcard/Drawing.png");
                intent.setDataAndType(Uri.fromFile(file), "image/*");
                startActivity(intent);
                return true;

            case R.id.menuPostDrawing:
                drawView.save("tmpDrawing");
                startActivity(new Intent(mainActivity, TwitterActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isNetworkAvailable()) {
            RetryDialog retryDialog = new RetryDialog(mainActivity, "Network is unavailable, check internet connection.");
            retryDialog.show();
        } else {
            setContentView(R.layout.activity_main);
            drawView = (DrawView) findViewById(R.id.drawView1);
            drawView.init(mainActivity);
            DialogFragment dialogFragment = new ClientDialog();
            dialogFragment.setCancelable(false);
            dialogFragment.show(getSupportFragmentManager(), "!");
        }
    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//    }

    @Override
    protected void onDestroy() {
        if (client != null) {
            client.disconnect();
        }
        //stopService(new Intent(this, MyService.class));
        super.onDestroy();
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
        if (drawView.isRandomColor) {
            menuRandomColor.setTitle("Enable random color");
            drawView.isRandomColor = false;
        } else {
            menuRandomColor.setTitle("Disable random color");
            drawView.isRandomColor = true;
        }
    }

    private void changeRandomWidth() {
        if (drawView.isRandomWidth) {
            menuRandomStrokeWidth.setTitle("Enable random width");
            drawView.isRandomWidth = false;
        } else {
            menuRandomStrokeWidth.setTitle("Disable random width");
            drawView.isRandomWidth = true;
        }
    }

    private void changeEraser() {
        if (drawView.isOnEraser) {
            menuEraser.setTitle("Eraser is off");
            menuRandomColor.setVisible(true);
            menuRandomStrokeWidth.setVisible(true);
            menuChangeColor.setVisible(true);
            menuChangeStrokeWidth.setTitle("Change stroke width");
            drawView.endEraser();
        } else {
            menuEraser.setTitle("Eraser is on");
            menuRandomColor.setVisible(false);
            menuRandomStrokeWidth.setVisible(false);
            menuChangeColor.setVisible(false);
            menuChangeStrokeWidth.setTitle("Change eraser size");
            drawView.initEraser();
        }
    }

//    private void changeContinuous() {
//        menuContinuousCommit = mainActivityMenu.findItem(R.id.menuContinuousCommit);
//        if (drawView.isContinuous) {
//            menuContinuousCommit.setTitle("Enable continuous commit");
//            drawView.isContinuous = false;
//        } else {
//            menuContinuousCommit.setTitle("Disable continuous commit");
//            drawView.isContinuous = true;
//        }
//    }
}
