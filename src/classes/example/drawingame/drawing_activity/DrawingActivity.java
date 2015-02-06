package classes.example.drawingame.drawing_activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

import classes.example.drawingame.R;
import classes.example.drawingame.drawing_activity.draw_view.DrawView;
import classes.example.drawingame.drawing_activity.from_ambilwarna.AmbilWarnaDialog;
import classes.example.drawingame.drawing_activity.from_ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.utils.Utils;

/**
 * Realizes drawing interaction between user and app
 */

public class DrawingActivity extends FragmentActivity {
   public static DrawingActivity drawingActivity = null;
   public DrawView drawView;
   public Button drawBtn;
   //    public String showInstruction = "showInstruction";
   public volatile boolean isDestroyed = false;
   private MenuItem menuRandomColor;
   private MenuItem menuEraser;
   private MenuItem menuChangeStrokeWidth;
   private MenuItem menuRandomStrokeWidth;
   private MenuItem menuChangeColor;
   private int pos;

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu_activity_drawing, menu);

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
         case R.id.menuFinish:
            finishEdit();
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
               String pathImgSaved = Utils.saveByTime(drawView.getBitmap());
               Utils.toast(drawingActivity, Utils.stringFromRes(R.string.savedDrawing) + " " + Utils.getSavedDir());
               Intent intentActionView = new Intent();
               intentActionView.setAction(android.content.Intent.ACTION_VIEW);
               File file = new File(pathImgSaved);
               intentActionView.setDataAndType(Uri.fromFile(file), "image/*");
               startActivity(intentActionView);
            } catch (IOException e) {
               Utils.toast(drawingActivity, e.toString());
            }
            return true;

         case R.id.menuPostDrawing:
            if (!isAutoTimeEnabled()) {
               startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
               Utils.toast(Utils.stringFromRes(R.string.repostNetworkTime));
            } else {
               try {
                  String pathImgTwitter = Utils.saveByTime(drawView.getBitmap());
                  Intent intentTwitter = new Intent(drawingActivity, TwitterActivity.class);
                  intentTwitter.putExtra("path", pathImgTwitter);
                  startActivity(intentTwitter);
               } catch (IOException e) {
                  Utils.toast(drawingActivity, e.toString());
               }
            }
            return true;

//            case R.id.menuInstruction:
//                showDialogFragment(InstructionDialog.create(false, aboutApp));
//                return true;

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
   public void onBackPressed() {
//        if (Utils.roomActivityExists())
      Utils.roomActivity.showList();
      finish();
      super.onBackPressed();
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      getWindow().getDecorView().setBackgroundColor(Color.BLACK);
      super.onCreate(savedInstanceState);

      drawingActivity = this;
      Intent intent = getIntent();
      pos = intent.getIntExtra("position", -1);

      if (getActionBar() != null) getActionBar().hide();
      setContentView(R.layout.activity_drawing);

      drawView = (DrawView) findViewById(R.id.drawingActivityDrawView);
      drawView.init(drawingActivity, false, ItemList.get(pos).currentBitmap);

      drawBtn = (Button) findViewById(R.id.drawingActivityBtn);
      drawBtn.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            openOptionsMenu();
         }
      });
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      isDestroyed = true;
      if (Utils.roomActivityExists())
         Utils.roomActivity.showList();
   }

   public void showDialogFragment(DialogFragment dialogFragment) {
      dialogFragment.setCancelable(false);
      dialogFragment.show(getSupportFragmentManager(), "!");
   }

//    public void showRetryDialogWithMessage(String message) {
//        new RetryDialog(drawingActivity, message).show();
//    }

//    private String getRawFileContent(int fileId) {
//        InputStream inputStream = getResources().openRawResource(fileId);
//        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//        try {
//            String instruction = br.readLine();
//            String readLine;
//            while ((readLine = br.readLine()) != null) {
//                instruction += "\n" + readLine;
//            }
//            return instruction;
//        } catch (IOException e) {
//            Show.toast(drawingActivity, "IOException");
//            return "";
//        }
//    }


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

   private void finishEdit() {
      finish();
      Utils.roomActivity.showList();
      ItemList.get(pos).bitmapToUpload = drawView.getBitmap();
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               Utils.saveById(drawView.getBitmap(), ItemList.get(pos).roomId);
               ItemList.uploadItem(pos);
               Log.d("!", "saved edited bitmap!");
            } catch (Exception e) {
               ItemList.get(pos).bitmapToUpload = null;
               Utils.toast(Utils.stringFromRes(R.string.errorEditing));
               Log.d("!", "couldnt save edited bitmap!");
            }
         }
      }).start();
   }
}
