package classes.example.drawingame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.Toast;
import classes.example.drawingame.AmbilWarnaDialog.OnAmbilWarnaListener;

import java.io.*;
import java.lang.reflect.Field;

/**
 * Realizes interaction of user and draw
 * view by options menu, has methods to
 * check internet availability
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
                    toast(getString(R.string.sendDrawing));
                } else
                    toast(getString(R.string.noInternet));
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
                drawView.save(DrawView.drawingName);
                toast(getString(R.string.saveDrawing) + " " + DrawView.drawingName + ".png");
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File("mnt/sdcard/" + DrawView.drawingName + ".png");
                intent.setDataAndType(Uri.fromFile(file), "image/*");
                startActivity(intent);
                return true;

            case R.id.menuPostDrawing:
                if (!isAutoTimeEnabled()) {
                    startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                    toast(getString(R.string.repostNetworkTime));
                    toast(getString(R.string.repostNetworkTime));
                } else {
                    drawView.save(DrawView.tmpDrawingName);
                    if (isNetworkAvailable())
                        startActivity(new Intent(drawingActivity, TwitterActivity.class));
                    else
                        toast(getString(R.string.noInternet));
                }
                return true;

            case R.id.menuInstruction:
                showDialogFragment(InstructionDialog.create(false, aboutApp));
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
        if (!isNetworkAvailable()) {
            showRetryDialogWithMessage(getString(R.string.noInternet));
        } else {
            //this TRY is to make overflow icon always visibble inside action bar
            try {
                ViewConfiguration config = ViewConfiguration.get(this);
                Field menuKeyField = ViewConfiguration.class
                        .getDeclaredField("sHasPermanentMenuKey");
                if (menuKeyField != null) {
                    menuKeyField.setAccessible(true);
                    menuKeyField.setBoolean(config, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            setContentView(R.layout.activity_drawing);
            getActionBar().setDisplayShowHomeEnabled(false);
            getWindow().getDecorView().setBackgroundColor(Color.BLACK);
            shortInstruction = getRawFileContent(R.raw.instruction_short);
            aboutApp = getRawFileContent(R.raw.about_app);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(drawingActivity);
            if (!sharedPreferences.contains(showInstruction))
                sharedPreferences.edit().putBoolean(showInstruction, true).apply();
            if (sharedPreferences.getBoolean(showInstruction, false)) {
                showDialogFragment(InstructionDialog.create(true, shortInstruction));
            } else
                showDialogFragment(new InitialDialog());
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

    public void toast(final String s) {
        drawingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(drawingActivity, s, Toast.LENGTH_LONG).show();
            }
        });
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
            toast("IOException");
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
