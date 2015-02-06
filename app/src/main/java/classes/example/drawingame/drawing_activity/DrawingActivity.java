package classes.example.drawingame.drawing_activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import java.io.File;
import java.io.IOException;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.drawing_activity.draw_view.DrawView;
import classes.example.drawingame.drawing_activity.from_ambilwarna.AmbilWarnaDialog;
import classes.example.drawingame.drawing_activity.from_ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.utils.Utils;

/**
 * Realizes drawing interaction between user and app
 */

public class DrawingActivity extends FragmentActivity {
   public static DrawingActivity drawingActivity = null;
   //   private LinearLayout llBtn;
   private LinearLayout llBottom;
   public DrawView drawView;
   public Button drawBtn;
   //    public String showInstruction = "showInstruction";
   public volatile boolean isDestroyed = false;
   PopupMenu btnPopupMenu;
   PopupMenu bottomPopupMenu;
   //   private MenuItem menuRandomColor;
//   private MenuItem menuEraser;
//   private MenuItem menuChangeStrokeWidth;
//   private MenuItem menuRandomStrokeWidth;
//   private MenuItem menuChangeColor;
   private Item item;

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
//      MenuInflater inflater = getMenuInflater();
//      inflater.inflate(R.menu.menu_activity_drawing, menu);
//
//      menuRandomStrokeWidth = menu.findItem(R.id.menuRandomStrokeWidth);
//      menuEraser = menu.findItem(R.id.menuEraser);
//      menuChangeStrokeWidth = menu.findItem(R.id.menuChangeStrokeWidth);
//      menuRandomColor = menu.findItem(R.id.menuRandomColor);
//      menuChangeColor = menu.findItem(R.id.menuChangeColor);
//
      return super.onCreateOptionsMenu(menu);
   }

   //   @Override
//   public boolean onOptionsItemSelected(MenuItem menuItem) {
   private PopupMenu createPopupMenu(View view) {
      PopupMenu popupMenu = new PopupMenu(this, view);
      Menu menu = popupMenu.getMenu();
      popupMenu.getMenuInflater().inflate(R.menu.menu_activity_drawing,
              menu);
      popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
               case R.id.menuFinish:
                  Utils.showProgress();
                  item.itemBitmap = Utils.getResizedBitmap(drawView.getBitmap());
                  item.lastEditorDeviceId = DataBase.thisDeviceId;
                  ItemList.updateItem(item);
                  finish();
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
                     File file = new File(Utils.saveByTime(drawView.getBitmap()));
                     Utils.toast(drawingActivity, Utils.stringFromRes(R.string.savedDrawing) + " " + Utils.getSavedDir());
                     Intent intentActionView = new Intent();
                     intentActionView.setAction(Intent.ACTION_VIEW);
                     intentActionView.setDataAndType(Uri.fromFile(file), "image/*");
                     startActivity(intentActionView);
                  } catch (IOException e) {
//                     Utils.toast(drawingActivity, e.toString());
                  }
                  return true;

               case R.id.menuPostDrawing:
                  if (!isAutoTimeEnabled()) {
                     startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
                     Utils.showErrorDialog(Utils.stringFromRes(R.string.repostNetworkTime));
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
                  return false;

            }
         }
      });
      return popupMenu;
   }
//            return super.onOptionsItemSelected(menuItem);
//      }
//
//   }

   @Override
   public boolean dispatchKeyEvent(KeyEvent event) {
      if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
         if (event.getAction() == KeyEvent.ACTION_UP)
            bottomPopupMenu.show();
         return true;
      }
      return super.dispatchKeyEvent(event);
   }


//   @Override
//   public void openOptionsMenu() {
//      Configuration config = getResources().getConfiguration();
//      if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
//              > Configuration.SCREENLAYOUT_SIZE_LARGE) {
//         int originalScreenLayout = config.screenLayout;
//         config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
//         super.openOptionsMenu();
//         showPopupMenu();
//         config.screenLayout = originalScreenLayout;
//      } else {
//         super.openOptionsMenu();
//      }
//      showPopupMenu();
//   }

   @Override
   public void onBackPressed() {
      finish();
      super.onBackPressed();
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      getWindow().getDecorView().setBackgroundColor(Color.BLACK);
      super.onCreate(savedInstanceState);

      drawingActivity = this;
      Intent intent = getIntent();
      int pos = intent.getIntExtra("pos", -1);
      item = new Item(ItemList.list.get(pos));

      if (getActionBar() != null) getActionBar().hide();
      setContentView(R.layout.activity_drawing);

      llBottom = (LinearLayout) findViewById(R.id.drawingActivityLlBottom);
      bottomPopupMenu = createPopupMenu(llBottom);
      drawBtn = (Button) findViewById(R.id.drawingActivityBtn);
      btnPopupMenu = createPopupMenu(drawBtn);
      drawBtn.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            btnPopupMenu.show();
         }
      });
      drawView = (DrawView) findViewById(R.id.drawingActivityDrawView);
      drawView.init(drawingActivity, false, item.itemBitmap);
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      isDestroyed = true;
//      Utils.showList();
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
//            Show.showErrorDialog(drawingActivity, "IOException");
//            return "";
//        }
//    }

   private void changeRandomColor() {
      changeRandomColor(bottomPopupMenu.getMenu());
      changeRandomColor(btnPopupMenu.getMenu());
      drawView.isRandomColor = !drawView.isRandomColor;
   }

   private void changeRandomWidth() {
      changeRandomWidth(bottomPopupMenu.getMenu());
      changeRandomWidth(btnPopupMenu.getMenu());
      drawView.isRandomWidth = !drawView.isRandomWidth;
   }

   private void changeEraser() {
      changeEraserMenu(bottomPopupMenu.getMenu());
      changeEraserMenu(btnPopupMenu.getMenu());
      if (drawView.isOnEraser) {
         drawView.endEraser();
      } else {
         drawView.initEraser();
         showDialogFragment(new ChangeWidthDialog());
      }
   }

   private void changeRandomColor(Menu menu) {
      MenuItem menuRandomColor = menu.findItem(R.id.menuRandomColor);
      if (drawView.isRandomColor)
         menuRandomColor.setTitle(R.string.menuRandomColorEnable);
      else
         menuRandomColor.setTitle(R.string.menuRandomColorDisable);
   }

   private void changeRandomWidth(Menu menu) {
      MenuItem menuRandomStrokeWidth = menu.findItem(R.id.menuRandomStrokeWidth);
      if (drawView.isRandomWidth)
         menuRandomStrokeWidth.setTitle(R.string.menuRandomStrokeWidthEnable);
      else
         menuRandomStrokeWidth.setTitle(R.string.menuRandomStrokeWidthDisable);

   }

   private void changeEraserMenu(Menu menu) {
      MenuItem menuRandomColor = menu.findItem(R.id.menuRandomColor);
      MenuItem menuRandomStrokeWidth = menu.findItem(R.id.menuRandomStrokeWidth);
      MenuItem menuEraser = menu.findItem(R.id.menuEraser);
      MenuItem menuChangeStrokeWidth = menu.findItem(R.id.menuChangeStrokeWidth);
      MenuItem menuChangeColor = menu.findItem(R.id.menuChangeColor);
      if (drawView.isOnEraser) {
         menuEraser.setTitle(R.string.menuEraserEnable);
         menuRandomColor.setVisible(true);
         menuRandomStrokeWidth.setVisible(true);
         menuChangeColor.setVisible(true);
         menuChangeStrokeWidth.setTitle(R.string.menuChangeStrokeWidth);
      } else {
         menuEraser.setTitle(R.string.menuEraserDisable);
         menuRandomColor.setVisible(false);
         menuRandomStrokeWidth.setVisible(false);
         menuChangeColor.setVisible(false);
         menuChangeStrokeWidth.setTitle(R.string.menuChangeEraserSize);
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
