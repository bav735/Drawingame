package classes.example.drawingame.room_activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.drawing_activity.DrawingActivity;
import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.room_activity.list_view.ListAdapter;
import classes.example.drawingame.room_activity.service.ListService;
import classes.example.drawingame.utils.MyAlertDialog;
import classes.example.drawingame.utils.Utils;

//import me.grantland.widget.*;

public class RoomActivity extends FragmentActivity {
   private static final int SCROLL_DURATION = 2150;
   public static boolean isNotified = false;
   public static int holderLayoutId;
   private int holderHeight;
   private int statusBarHeight = 0;
   private int actionBarHeight = 0;

   public ListAdapter tcAdapter;
   public ListView roomListView;
   private LinearLayout llBottom;
   public ProgressBar roomProgressBar;
   public volatile boolean isDestroyed = false;
   public boolean scrollFinished = true;
   PopupMenu popupMenu;
   private String notifiedId = null;

   @Override
   public void onBackPressed() {
      super.onBackPressed();
   }

   private void clearFocus() {
      if (getCurrentFocus() != null) getCurrentFocus().clearFocus();
//      llBottom.setFocusableInTouchMode(true);
//      llBottom.requestFocusFromTouch();
   }

   private void selectHolderLayout() {
      TypedValue tv = new TypedValue();
      if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
         actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
      }
      int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
      if (resourceId > 0) {
         statusBarHeight = getResources().getDimensionPixelSize(resourceId);
      }
      int holderMaxHeight = Utils.displayHeight - actionBarHeight * 5 / 2 - statusBarHeight;

      int mul = 13;
      do {
         mul--;
         holderHeight = Utils.displayWidth * mul / 9;
      } while (holderHeight > holderMaxHeight);
//      Log.d("!", "height = " + String.valueOf(Utils.displayHeight));
//      Log.d("!", "width = " + String.valueOf(Utils.displayWidth));
//      Log.d("!", "maxheight = " + String.valueOf(holderMaxHeight));
//      Log.d("!", "holderheight = " + String.valueOf(holderHeight));
//      Log.d("!", "mul = " + String.valueOf(mul));
//      Log.d("!", "statusheight = " + String.valueOf(statusBarHeight));
//      Log.d("!", "barheight = " + String.valueOf(actionBarHeight));
      switch (mul) {
         case 12:
            holderLayoutId = R.layout.holder_3_4;
            break;
         case 11:
            holderLayoutId = R.layout.holder_9_11;
            break;
         case 10:
            holderLayoutId = R.layout.holder_9_10;
            break;
         default:
            holderLayoutId = R.layout.holder_1_1;
      }
   }

   @Override
   protected void onCreate(final Bundle savedInstanceState) {
      setTheme(R.style.RoomActivityTheme);
      getWindow().getDecorView().setBackgroundColor(Color.BLACK);
      super.onCreate(savedInstanceState);
      Log.d("!", "onCreate roomAct");
      Utils.init(getApplicationContext(), this);
      DataBase.init();
      try {
         ViewConfiguration config = ViewConfiguration.get(Utils.appContext);
         Field menuKeyField = ViewConfiguration.class
                 .getDeclaredField("sHasPermanentMenuKey");
         if (menuKeyField != null) {
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
         }
      } catch (Exception e) {
         //
      }
      setContentView(R.layout.activity_room);
      selectHolderLayout();
      createListView();
      createPopupMenu();
      startService(new Intent(Utils.roomActivity, ListService.class));
      Intent intent = getIntent();
      notifiedId = intent.getStringExtra("id");
   }

   private void createPopupMenu() {
      popupMenu = new PopupMenu(this, llBottom);
      popupMenu.getMenuInflater().inflate(R.menu.menu_activity_room, popupMenu.getMenu());
      popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
            return menuSwitcher(item);
         }
      });
   }

   @Override
   protected void onDestroy() {
      if (Utils.drawingActivityExists())
         DrawingActivity.drawingActivity.finish();
      super.onDestroy();
      Log.d("!", "onDestroy");
      isDestroyed = true;
   }

   @Override
   protected void onPause() {
      super.onPause();
   }

   @Override
   protected void onNewIntent(Intent intent) {
      notifiedId = intent.getStringExtra("id");
      if (notifiedId != null)
         isNotified = true;
   }

   @Override
   protected void onResume() {
      Log.d("!", "onResume");
      super.onResume();
      if (Utils.preferences.getBoolean("ban", false))
         Utils.showErrorWithListenerDialog(Utils.stringFromRes(R.string.banMessage), new MyAlertDialog.OnDismissedListener() {
            @Override
            public void onDismissed(boolean isPositive) {
               if (Utils.roomActivityExists())
                  finish();
            }
         });
      if (roomProgressBar.getVisibility() == View.INVISIBLE)
         checkNotified();
   }

   @Override
   protected void onStart() {
      super.onStart();
      Log.d("!", "onStart roomAct");
   }

   @Override
   protected void onStop() {
      super.onStop();
      Log.d("!", "onStop roomAct");
   }

   private int curr = 0;

   @Override
   protected void onRestart() {
      super.onRestart();
   }

   @Override
   public void onWindowFocusChanged(boolean hasFocus) {
      super.onWindowFocusChanged(hasFocus);
   }

   @Override
   public boolean dispatchKeyEvent(KeyEvent event) {
      if (isNotified) {
         clearFocus();
         isNotified = false;
      }
      if (scrollFinished && !ItemList.list.isEmpty()) {
         if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            if (event.getAction() == KeyEvent.ACTION_UP)
               popupMenu.show();
         } else
            return super.dispatchKeyEvent(event);
      }
      return true;
   }

   @Override
   public boolean dispatchTouchEvent(MotionEvent event) {
      if (isNotified) {
         isNotified = false;
      }
      if (scrollFinished)
         return super.dispatchTouchEvent(event);
      return true;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu_activity_room, menu);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem menuItem) {
      boolean switcher = menuSwitcher(menuItem);
      if (switcher)
         return true;
      return super.onOptionsItemSelected(menuItem);
   }

   private boolean menuSwitcher(MenuItem menuItem) {
      switch (menuItem.getItemId()) {

         case R.id.menuActivityRoomRefresh:
            ItemList.refreshItems();
            return true;

         case R.id.menuActivityRoomAdd:
//            ListService.sendNotification("111", ItemList.get(curr).roomId);
//            curr++;
            RoomCreateDialog roomCreateDialog = new RoomCreateDialog();
            roomCreateDialog.setCancelable(false);
            roomCreateDialog.show(getSupportFragmentManager(), "!");
            return true;

         case R.id.menuActivityRoomExit:
            Utils.showConfirmActionDialog(Utils.stringFromRes(R.string.appExit),
                    new MyAlertDialog.OnDismissedListener() {
                       @Override
                       public void onDismissed(boolean isPositive) {
                          if (isPositive) {
                             ListService.stopTimer();
                             stopService(new Intent(Utils.roomActivity, ListService.class));
                             Utils.roomActivity.finish();
                          }
                       }
                    });
            return true;

         default:
            return false;
      }

   }

   @Override
   public void openOptionsMenu() {
      super.openOptionsMenu();
   }

   private void
   createListView() {
      llBottom = (LinearLayout) findViewById(R.id.roomActivityLlBottom);
      roomProgressBar = (ProgressBar) findViewById(R.id.pbActivityRoom);
      roomListView = (ListView) findViewById(R.id.roomListView);
//      roomListView.addFooterView(new View(Utils.appContext), null, true);
      tcAdapter = new ListAdapter();
      roomListView.setAdapter(tcAdapter);
      if (ItemList.list.isEmpty()) {
         showProgress();
      } else
         ItemList.reloadItems();
   }

   private void checkNotified() {
      if (notifiedId != null) {
         final String notifiedIdCopy = notifiedId;
         notifiedId = null;
         final LinkedHashMap<String, Item> map = ItemList.getMap();
         if (map.containsKey(notifiedIdCopy)) {
            closeOptionsMenu();
            popupMenu.dismiss();
            roomListView.setSelection(0);
            roomListView.postDelayed(new Runnable() {
               @Override
               public void run() {
                  scroll(map.get(notifiedIdCopy).pos);
               }
            }, 333);
         } else
            Utils.toast(Utils.roomActivity, Utils.stringFromRes(R.string.errorNotFound));
      }
   }

   private void scroll(final int pos) {
      Log.d("!", "scroll to " + String.valueOf(pos));
      scrollFinished = false;
      int postTime = 0;
      if (pos > 0) {
         int offset = (getResources().getDimensionPixelSize(R.dimen.room_activity_divider_size)
                 + holderHeight) * pos;
         if (pos < ItemList.size() - 1)
            offset -= actionBarHeight;
         roomListView.smoothScrollBy(offset, SCROLL_DURATION);
         postTime = SCROLL_DURATION;
      }
      roomListView.postDelayed(new Runnable() {
         @Override
         public void run() {
            focusItem(pos);
            scrollFinished = true;
         }
      }, postTime);
   }

   private void focusItem(int pos) {
      View view = roomListView.getChildAt(1);
      view.requestFocusFromTouch();
      roomListView.setSelectionFromTop(pos, view.getTop());
   }

   public void showProgress() {
      runOnUiThread(new Runnable() {
         @Override
         public void run() {
            if (getActionBar() != null) getActionBar().hide();
            roomProgressBar.setVisibility(View.VISIBLE);
            roomListView.setVisibility(View.INVISIBLE);
         }
      });
   }

   public void showList() {
      runOnUiThread(new Runnable() {
         @Override
         public void run() {
            if (getActionBar() != null) getActionBar().show();
            roomProgressBar.setVisibility(View.INVISIBLE);
            roomListView.setVisibility(View.VISIBLE);
            checkNotified();
         }
      });
   }

}
