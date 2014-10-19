package com.example.drawingame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import java.io.File;

/**
 * Realizes interaction of user and draw
 * view by options menu, has methods to
 * check internet availability
 */

public class DrawingActivity extends FragmentActivity {
    public DrawView drawView;
    public Client client;

    private DrawingActivity drawingActivity = this;
    private Menu mainActivityMenu;
    private MenuItem menuRandomColor;
    private MenuItem menuEraser;
    private MenuItem menuChangeStrokeWidth;
    private MenuItem menuRandomStrokeWidth;
    private MenuItem menuChangeColor;

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
        menuEraser = mainActivityMenu.findItem(R.id.menuEraser);
        menuChangeStrokeWidth = mainActivityMenu.findItem(R.id.menuChangeStrokeWidth);
        menuRandomColor = mainActivityMenu.findItem(R.id.menuRandomColor);
        menuChangeColor = mainActivityMenu.findItem(R.id.menuChangeColor);

        switch (item.getItemId()) {
            case R.id.menuUndo:
                drawView.undo();
                return true;

            case R.id.menuEraser:
                changeEraser();
                return true;

            case R.id.menuCommitDrawing:
                if (client.ortcClient.getIsConnected()) {
                    client.commitDrawing();
                    toast(getString(R.string.sendDrawing));
                } else
                    toast(getString(R.string.noInternet));
                return true;

            case R.id.menuChangeColor:
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(drawingActivity,
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
                showDialogFragment(new ChangeWidthDialog());
                if (drawView.isRandomWidth)
                    changeRandomWidth();
                return true;

            case R.id.menuRandomStrokeWidth:
                changeRandomWidth();
                return true;

            case R.id.menuSaveDrawing:
                drawView.save("Drawing");
                toast(getString(R.string.saveDrawing));
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File("mnt/sdcard/Drawing.png");
                intent.setDataAndType(Uri.fromFile(file), "image/*");
                startActivity(intent);
                return true;

            case R.id.menuPostDrawing:
                drawView.save(DrawView.tmpDrawingName);
                if (isNetworkAvailable())
                    startActivity(new Intent(drawingActivity, TwitterActivity.class));
                else
                    toast(getString(R.string.noInternet));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isNetworkAvailable()) {
            RetryDialog retryDialog = new RetryDialog(drawingActivity, getString(R.string.noInternet));
            retryDialog.show();
        } else {
            setContentView(R.layout.activity_drawing);
            getWindow().getDecorView().setBackgroundColor(Color.BLACK);
            showDialogFragment(new InitialDialog());
        }
    }

    @Override
    protected void onDestroy() {
        if (client != null) {
            client.disconnect();
        }
        super.onDestroy();
    }

    //private <T> void showDialogFragment(T dialogFragment){
    private void showDialogFragment(DialogFragment dialogFragment) {
        dialogFragment.setCancelable(false);
        dialogFragment.show(getSupportFragmentManager(), "!");
    }

    private void toast(final String s) {
        drawingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(drawingActivity, s, 0).show();
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
            menuRandomColor.setTitle(R.string.menuRandomColorEnable);
            drawView.isRandomColor = false;
        } else {
            menuRandomColor.setTitle(R.string.menuRandomColorDisable);
            drawView.isRandomColor = true;
        }
    }

    private void changeRandomWidth() {
        if (drawView.isRandomWidth) {
            menuRandomStrokeWidth.setTitle(R.string.menuRandomStrokeWidthEnable);
            drawView.isRandomWidth = false;
        } else {
            menuRandomStrokeWidth.setTitle(R.string.menuRandomStrokeWidthDisable);
            drawView.isRandomWidth = true;
        }
    }

    private void changeEraser() {
        if (drawView.isOnEraser) {
            menuEraser.setTitle(R.string.menuEraserEnable);
            menuRandomColor.setVisible(true);
            menuRandomStrokeWidth.setVisible(true);
            menuChangeColor.setVisible(true);
            menuChangeStrokeWidth.setTitle(R.string.menuChangeStrokeWidth);
            drawView.endEraser();
        } else {
            menuEraser.setTitle(R.string.menuEraserDisable);
            menuRandomColor.setVisible(false);
            menuRandomStrokeWidth.setVisible(false);
            menuChangeColor.setVisible(false);
            menuChangeStrokeWidth.setTitle(R.string.menuChangeEraserSize);
            drawView.initEraser();
        }
    }

}
