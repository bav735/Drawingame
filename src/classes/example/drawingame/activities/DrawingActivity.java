package classes.example.drawingame.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import classes.example.drawingame.Client;
import classes.example.drawingame.Generator;
import classes.example.drawingame.R;
import classes.example.drawingame.Show;
import classes.example.drawingame.dialogs.ChangeWidthDialog;
import classes.example.drawingame.dialogs.InstructionDialog;
import classes.example.drawingame.dialogs.RetryDialog;
import classes.example.drawingame.fromafilechooser.FileUtils;
import classes.example.drawingame.fromambilwarna.AmbilWarnaDialog;
import classes.example.drawingame.fromambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import classes.example.drawingame.views.DrawView;

/**
 * Realizes drawing interaction between user and app
 */

public class DrawingActivity extends FragmentActivity {
    public DrawView drawView;
    public Client client;
    public String aboutApp;
    public String shortInstruction;
    public String showInstruction = "showInstruction";
    private DrawingActivity drawingActivity = this;
    private Menu menu;
    private MenuItem menuRandomColor;
    private MenuItem menuEraser;
    private MenuItem menuChangeStrokeWidth;
    private MenuItem menuRandomStrokeWidth;
    private MenuItem menuChangeColor;

    private String imgUrl;
    private int imgPosition;
    private String roomName;
    private Target target;

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        menu = m;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        menuRandomStrokeWidth = menu.findItem(R.id.menuRandomStrokeWidth);
        menuEraser = menu.findItem(R.id.menuEraser);
        menuChangeStrokeWidth = menu.findItem(R.id.menuChangeStrokeWidth);
        menuRandomColor = menu.findItem(R.id.menuRandomColor);
        menuChangeColor = menu.findItem(R.id.menuChangeColor);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menuCommitDrawing:
                if (client.ortcClient.getIsConnected()) {
                    client.commitDrawing();
                    Show.toast(drawingActivity, getString(R.string.sendDrawing));
                } else
                    Show.toast(drawingActivity, getString(R.string.noInternet));
                return true;

            case R.id.menuUndo:
                drawView.undo();
                return true;

            case R.id.menuEraser:
                changeEraser();
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
                });
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
                try {
                    String pathImgSaved = FileUtils.save(drawingActivity, drawView.getBitmap(),
                            FileUtils.getSavedDir(), roomName + "(" + Generator.time() + ")");
                    Show.toast(drawingActivity, "Saved to " + FileUtils.getSavedDir());
                    Intent intentActionView = new Intent();
                    intentActionView.setAction(android.content.Intent.ACTION_VIEW);
                    File file = new File(pathImgSaved);
                    intentActionView.setDataAndType(Uri.fromFile(file), "image/*");
                    startActivity(intentActionView);
                } catch (IOException e) {
                    Show.toast(drawingActivity, e.toString());
                }
                return true;

            case R.id.menuPostDrawing:
                if (!isAutoTimeEnabled()) {
                    startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                    Show.toast(drawingActivity, getString(R.string.repostNetworkTime));
                    Show.toast(drawingActivity, getString(R.string.repostNetworkTime));
                } else {
                    try {
                        String pathImgTwitter = FileUtils.save(drawingActivity, drawView.getBitmap(),
                                FileUtils.getCachedDir(), Generator.time());
                        Intent intentTwitter = new Intent(drawingActivity, TwitterActivity.class);
                        intentTwitter.putExtra("path", pathImgTwitter);
                        if (isNetworkAvailable())
                            startActivity(intentTwitter);
                        else
                            Show.toast(drawingActivity, getString(R.string.noInternet));
                    } catch (IOException e) {
                        Show.toast(drawingActivity, e.toString());
                    }
                }
                return true;

            case R.id.menuInstruction:
                showDialogFragment(InstructionDialog.create(false, aboutApp));
                return true;

            case R.id.menuFinish:
                try {
                    String pathImg = FileUtils.save(drawingActivity, drawView.getBitmap(),
                            FileUtils.getCachedDir(), roomName);
                    Intent intentChooseRoom = new Intent();
                    intentChooseRoom.putExtra("path", pathImg);
                    intentChooseRoom.putExtra("position", imgPosition);
                    setResult(RESULT_OK, intentChooseRoom);
                    finish();
                } catch (IOException e) {
                    Show.toast(drawingActivity, e.toString());
                }
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }

    }

    @Override
    public void openOptionsMenu() {
        Configuration config = getResources().getConfiguration();
        if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                > Configuration.SCREENLAYOUT_SIZE_LARGE) {
            int originalScreenLayout = config.screenLayout;
            config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
            super.openOptionsMenu();
            config.screenLayout = originalScreenLayout;
        } else {
            super.openOptionsMenu();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (!isNetworkAvailable()) {
//            showRetryDialogWithMessage(getString(R.string.noInternet));
//        } else {

        //this TRY is to make overflow icon always visible inside action bar
        try {
            Intent intent = getIntent();
            imgPosition = intent.getIntExtra("position", 0);
            roomName = intent.getStringExtra("roomName");
            imgUrl = intent.getStringExtra("url");

            ViewConfiguration config = ViewConfiguration.get(drawingActivity);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }

            setContentView(R.layout.activity_drawing);
            getActionBar().setDisplayShowHomeEnabled(false);
            getWindow().getDecorView().setBackgroundColor(Color.BLACK);
//                            shortInstruction = getRawFileContent(R.raw.instruction_short);
            aboutApp = getRawFileContent(R.raw.about_app);

            getActionBar().hide();

            drawView = (DrawView) findViewById(R.id.drawViewMain);
            target = new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    drawingActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getActionBar().show();
                            pbDrawing.setVisibility(View.INVISIBLE);
                            drawView.setVisibility(View.VISIBLE);
                            drawView.init(drawingActivity, false, bitmap);
                        }
                    });
                }

                @Override
                public void onBitmapFailed(Drawable drawable) {
                    Show.toast(drawingActivity, "Download failed ");
                }

                @Override
                public void onPrepareLoad(Drawable drawable) {

                }

            };
//            Show.toast(drawingActivity, imgUrl);
            Picasso.with(drawingActivity).load(imgUrl).into(target);
        } catch (Exception e) {
            Show.toast(drawingActivity, e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        if (client != null)
            client.destroy();
        super.onDestroy();
    }

    public void showDialogFragment(DialogFragment dialogFragment) {
        dialogFragment.setCancelable(false);
        dialogFragment.show(getSupportFragmentManager(), "!");
    }

    public void showRetryDialogWithMessage(String message) {
        new RetryDialog(drawingActivity, message).show();
    }

    private String getRawFileContent(int fileId) {
        InputStream inputStream = getResources().openRawResource(fileId);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String instruction = br.readLine();
            String readLine;
            while ((readLine = br.readLine()) != null) {
                instruction += "\n" + readLine;
            }
            return instruction;
        } catch (IOException e) {
            Show.toast(drawingActivity, "IOException");
            return "";
        }
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
            showDialogFragment(new ChangeWidthDialog());
        }
    }

    private boolean isAutoTimeEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // For JB+
            return Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0) > 0;
        }
        // For older Android versions
        return Settings.System.getInt(getContentResolver(), Settings.System.AUTO_TIME, 0) > 0;
    }
}
