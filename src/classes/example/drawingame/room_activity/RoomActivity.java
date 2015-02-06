package classes.example.drawingame.room_activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.BanChecker;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.data_base.RoomsGetter;
import classes.example.drawingame.drawing_activity.DrawingActivity;
import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.room_activity.list_view.ListAdapter;
import classes.example.drawingame.room_activity.service.ListService;
import classes.example.drawingame.utils.ToastDialog;
import classes.example.drawingame.utils.Utils;

//import me.grantland.widget.*;

public class RoomActivity extends FragmentActivity {
   private static final int SCROLL_DURATION = 2150;
   //    private static final int HOLDER_HEIGHT = 500;
   public ListAdapter tcAdapter;
   public ListView roomListView;
   //   public RelativeLayout relativeLayout;
   public ProgressBar roomProgressBar;
   public volatile boolean isDestroyed = false;
   public boolean scrollFinished = true;
   private RoomCreateDialog roomCreateDialog;
   private LinearLayout llUnderline;
   private Menu menu;
   private String notifiedId = null;
//    private boolean gotNotification = false;
//    private boolean launchShown = false;

   @Override
   public void onBackPressed() {
      super.onBackPressed();
      if (getCurrentFocus() != null) getCurrentFocus().clearFocus();
   }

   @Override
   protected void onCreate(final Bundle savedInstanceState) {
//        if (!launchShown)
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    launchShown = true;
//                    onCreate(savedInstanceState);
//                }
//            }, 500);
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
      }
      setContentView(R.layout.activity_room);
      createList();
      startService(new Intent(Utils.roomActivity, ListService.class));
      Intent intent = getIntent();
      notifiedId = intent.getStringExtra("id");
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      Log.d("!", "onDestroy");
      isDestroyed = true;
      if (Utils.drawingActivityExists())
         DrawingActivity.drawingActivity.finish();
      Utils.preferences.edit().putString("list", ItemList.toJson()).commit();
   }

   @Override
   protected void onPause() {
      super.onPause();
//        MyApplication.activityPaused();
   }

   @Override
   protected void onNewIntent(Intent intent) {
      notifiedId = intent.getStringExtra("id");
   }

   @Override
   protected void onResume() {
      super.onResume();
      if (notifiedId != null) {
         LinkedHashMap<String, Item> map = ItemList.getMap();
         if (map.containsKey(notifiedId)) {
            roomListView.setSelection(0);
            scroll(map.get(notifiedId).pos);
         }
      }
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

   private static void cycleRoomsGetter() {
      Utils.roomActivity.showProgress();
      new RoomsGetter(new RoomsGetter.OnRoomsGotListener() {
         @Override
         public void onRoomsGot(final ArrayList<Item> list, final boolean isError) {
            if (list == null) {
               Utils.toast(new ToastDialog.OnDismissedListener() {
                  @Override
                  public void onDismissed(boolean isOk) {
                     cycleRoomsGetter();
                  }
               });
            } else {
               Log.d("!", "setting list after cyclinggetter");
//                    ItemList.list = new ArrayList<Item>();
               ItemList.setList(list);
            }
         }
      }).start();
   }

   private void getRooms() {
      new RoomsGetter(new RoomsGetter.OnRoomsGotListener() {
         @Override
         public void onRoomsGot(final ArrayList<Item> dbList, final boolean isError) {
            if (dbList == null) {
               Log.d("!", "got the same list");
               if (isError)
                  Utils.toast(Utils.stringFromRes(R.string.errorDb));
               showList();
            } else {
               ItemList.setList(dbList);
            }
         }
      }).start();
   }

   @Override
   protected void onRestart() {
      super.onRestart();
//      createList();
   }

   @Override
   public void onWindowFocusChanged(boolean hasFocus) {
      super.onWindowFocusChanged(hasFocus);
   }

   @Override
   public boolean dispatchKeyEvent(KeyEvent event) {
//        Log.d("!", "touched");
      if (notifiedId != null) {
         setUnderlineVisible();
//         if (event.getKeyCode() != KeyEvent.KEYCODE_MENU)
         if (getCurrentFocus() != null) getCurrentFocus().clearFocus();
         notifiedId = null;
      }
      if (scrollFinished && event.getKeyCode() != KeyEvent.KEYCODE_MENU) {
//         if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
//            if (event.getAction() == KeyEvent.ACTION_DOWN) {
//               Log.d("!", "down");
//               if (getCurrentFocus() != null) getCurrentFocus().clearFocus();
//               return true;
//            }
//            if (event.getAction() == KeyEvent.ACTION_UP) {
//               Log.d("!", "up");
//               openOptionsMenu();
//            }
//         }
//               Log.d("!", "menu down");
//               dispatchTouchEvent(MotionEvent.obtain(
//                       SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
//                       MotionEvent.ACTION_PDOWN, Utils.displayMetrics.widthPixels - 1, 1, 0));
//               dispatchTouchEvent(MotionEvent.obtain(
//                       SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
//                       MotionEvent.ACTION_UP, Utils.displayMetrics.widthPixels - 1, 1, 0));
//               return true;
//            }
         return super.dispatchKeyEvent(event);
      }
      return true;
   }

   @Override
   public boolean dispatchTouchEvent(MotionEvent event) {
      Log.d("!", "touched");
      if (notifiedId != null) {
         setUnderlineVisible();
         notifiedId = null;
      }
      if (scrollFinished)
         return super.dispatchTouchEvent(event);
      return true;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu m) {
      menu = m;
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu_activity_room, menu);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem menuItem) {
      switch (menuItem.getItemId()) {

         case R.id.menuActivityRoomRefresh:
            showProgress();
            if (ItemList.isBusy())
               Utils.toast(Utils.stringFromRes(R.string.errorRefreshList), new ToastDialog.OnDismissedListener() {
                  @Override
                  public void onDismissed(boolean isOk) {
                     if (isOk) getRooms();
                     else showList();
                  }
               });
            else
               getRooms();
            return true;

         case R.id.menuActivityRoomAdd:
//            ListService.sendNotification("111", ItemList.get(curr).roomId);
//            curr++;
//            if (false)
            new BanChecker(new BanChecker.OnBanCheckedListener() {
               @Override
               public void onBanChecked(boolean isBanned) {
                  if (isBanned) {
                     Utils.toast(Utils.stringFromRes(R.string.banMessage));
                  } else {
                     roomCreateDialog = new RoomCreateDialog();
                     Utils.showDialog(Utils.roomActivity, roomCreateDialog);
                  }
               }
            }).start();
            return true;

         case R.id.menuActivityRoomExit:
            ListService.stopTimer();
            stopService(new Intent(Utils.roomActivity, ListService.class));
            Utils.roomActivity.finish();
            return true;

         default:
            return super.onOptionsItemSelected(menuItem);
      }

   }

   @Override
   public void openOptionsMenu() {
      super.openOptionsMenu();
   }

   private void createList() {
      boolean createdList = false;
      if (ItemList.list == null) {
         ItemList.list = new ArrayList<Item>();
         createdList = true;
      }
      roomProgressBar = (ProgressBar) findViewById(R.id.pbActivityRoom);
      roomListView = (ListView) findViewById(R.id.roomListView);
//      relativeLayout = (RelativeLayout) findViewById(R.id.rlActivityRoom);
      if (createdList)
         showProgress();
      llUnderline = (LinearLayout) findViewById(R.id.roomActivityLlUnderline);
      roomListView.addFooterView(new View(Utils.appContext), null, true);
      roomListView.addHeaderView(new View(Utils.appContext), null, true);
      tcAdapter = new ListAdapter();
      roomListView.setAdapter(tcAdapter);
      Utils.notifyAdapter();

      if (createdList)
         if (Utils.preferences.contains("list")) {
            Log.d("!", "fromJson roomAct");
            ItemList.fromJson(Utils.preferences.getString("list", null));
         } else if (Utils.roomActivityExists()) {
            Log.d("!", "cycleGetter");
            cycleRoomsGetter();
         }
   }

   private void scroll(final int pos) {
      scrollFinished = false;
      int postTime = 0;
      if (pos > 0) {
         roomListView.smoothScrollBy((getResources().getDimensionPixelSize(R.dimen.room_activity_divider_size)
                 + Utils.displayMetrics.heightPixels * 3 / 4) * pos, SCROLL_DURATION);
         postTime = SCROLL_DURATION;
      }
      roomListView.postDelayed(new Runnable() {
         @Override
         public void run() {
            focusItem(pos);
            setUnderlineInvisible();
            scrollFinished = true;
         }
      }, postTime);
   }

   private void focusItem(int pos) {
      View view = roomListView.getChildAt(1);
      view.requestFocusFromTouch();
      roomListView.setSelectionFromTop(pos + 1, view.getTop());
   }

   private void setUnderlineInvisible() {
      runOnUiThread(new Runnable() {
         @Override
         public void run() {
//                llUnderline.setBackgroundResource(R.drawable.room_create_dialog_et_underline_holo);
            llUnderline.setVisibility(View.INVISIBLE);
//                llUnderline.requestLayout();
         }
      });
   }

   private void setUnderlineVisible() {
      runOnUiThread(new Runnable() {
         @Override
         public void run() {
//                llUnderline.setBackgroundResource(R.drawable.room_create_dialog_et_underline);
            llUnderline.setVisibility(View.VISIBLE);
//                llUnderline.requestLayout();
         }
      });
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
         }
      });
   }

}
