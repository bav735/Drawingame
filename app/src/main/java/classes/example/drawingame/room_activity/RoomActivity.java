package classes.example.drawingame.room_activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.data_base.RoomsGetter;
import classes.example.drawingame.room_activity.list_view.Disk;
import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.room_activity.list_view.ListAdapter;
import classes.example.drawingame.room_activity.list_view.Memory;
import classes.example.drawingame.room_activity.service.ListService;
import classes.example.drawingame.room_activity.service.ServiceTimer;
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

   private SharedPreferences preferences;
   public ListAdapter tcAdapter;
   public ListView roomListView;
   private LinearLayout llBottom;
   public ProgressBar roomProgressBar;
   public boolean scrollFinished = true;
   private PopupMenu popupMenu;
   private String notifiedId = null;

   public class MessageHandler extends Handler {
      @Override
      public void handleMessage(Message message) {
         int state = message.arg1;
         switch (state) {
            case ListService.MESSAGE_SHOW_PB:
               showProgress();
               break;
            case ListService.MESSAGE_SHOW_LIST:
               showList();
               break;
            case ListService.MESSAGE_NOTIFY_ADAPTER:
               runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     if (tcAdapter != null)
                        tcAdapter.notifyDataSetChanged();
                  }
               });
               break;
            case ListService.MESSAGE_SHOW_ERROR_DIALOG:
               Utils.showErrorDialog(Utils.stringFromRes(getApplicationContext()
                       , message.arg2), RoomActivity.this);
               break;
            case ListService.MESSAGE_INIT:
               if (ItemList.list.isEmpty())
                  reloadItemList();
               else
                  finishInitialize();
               break;
            case ListService.MESSAGE_NOTIFY_SCROLL:
               if (roomProgressBar.getVisibility() == View.INVISIBLE)
                  checkNotified();
               break;
         }
      }
   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
   }

   private void clearFocus() {
      if (getCurrentFocus() != null) getCurrentFocus().clearFocus();
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
      initializeOverflow();
      preferences = getApplicationContext().
              getSharedPreferences("preferences", Context.MODE_PRIVATE);
      preferences.edit().putBoolean("destroyed", false).commit();
      Log.d("!", "onCreate roomAct");
      setContentView(R.layout.activity_room);
      roomProgressBar = (ProgressBar) findViewById(R.id.pbActivityRoom);
      showProgress();
      initializeInBackground();
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

   private void initializeOverflow() {
      try {
         ViewConfiguration config = ViewConfiguration.get(getApplicationContext());
         Field menuKeyField = ViewConfiguration.class
                 .getDeclaredField("sHasPermanentMenuKey");
         if (menuKeyField != null) {
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
         }
      } catch (Exception e) {
         Log.d("!", "overflow is invisible");
      }
   }

   private void initializeInBackground() {
      new Thread(new Runnable() {
         @Override
         public void run() {
//            Log.d("!", "started initializeInBackground");
            DataBase.init(getApplicationContext());
            Disk.init();
            Memory.init();
            Utils.getDisplaySize(RoomActivity.this);
            selectHolderLayout();
            RoomActivity.this.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  Intent serviceIntent = new Intent(RoomActivity.this, ListService.class);
                  serviceIntent.putExtra("MESSENGER", new Messenger(new MessageHandler()));
                  RoomActivity.this.startService(serviceIntent);
               }
            });
         }
      }).start();
   }

   private void reloadItemList() {
//      Log.d("!", "reloading");
      ItemList.reloadItems(new ItemList.OnReloadListener() {
         @Override
         public void onReloaded(boolean isReloaded) {
            if (!isReloaded) {
//               Log.d("!", "not reloaded, liist size = " + String.valueOf(ItemList.list.size()));
               ServiceTimer.isDialogShown = true;
               Utils.showRetryActionDialog(Utils.stringFromRes(getApplicationContext(), R.string.errorDb),
                       new MyAlertDialog.OnDismissedListener() {
                          @Override
                          public void onDismissed(boolean isPositive) {
                             ServiceTimer.isDialogShown = false;
                             if (!isPositive)
                                RoomActivity.this.finish();
                             else
                                reloadItemList();
                          }
                       }, RoomActivity.this);
            } else {
//               Log.d("!", "reloaded, liist size = " + String.valueOf(ItemList.list.size()));
               RoomActivity.this.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     finishInitialize();
                  }
               });
            }
         }
      }, getApplicationContext());
   }

   private void finishInitialize() {
      createListView();
      createPopupMenu();
      showList();
      Intent intent = getIntent();
      notifiedId = intent.getStringExtra("id");
      ListService.sendMessageNotifyScroll();
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      Log.d("!", "onDestroy");
      preferences.edit().putBoolean("destroyed", true).commit();
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
      checkBan();
      ListService.sendMessageNotifyScroll();
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

//   private int curr = 0;

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
            if (event.getAction() == KeyEvent.ACTION_UP && popupMenu != null)
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
      if (menuSwitcher(menuItem))
         return true;
      return super.onOptionsItemSelected(menuItem);
   }

   private boolean menuSwitcher(MenuItem menuItem) {
      switch (menuItem.getItemId()) {

         case R.id.menuActivityRoomRefresh:
            showProgress();
            new RoomsGetter(new RoomsGetter.OnRoomsGotListener() {
               @Override
               public void onRoomsGot(final ArrayList<Item> dbList) {
                  if (dbList == null) {
                     Utils.showErrorDialog(Utils.stringFromRes(getApplicationContext(),
                             R.string.errorDb), RoomActivity.this);
                     showList();
                  } else {
                     if (!ItemList.listIsSetting && !ItemList.isOnProgress())
                        ItemList.setNewList(getApplicationContext(), dbList, null);
                     else
                        showList();
                  }
               }
            }).start(getApplicationContext());
            return true;

         case R.id.menuActivityRoomAdd:
//            ListService.sendNotification("111", ItemList.list.get(curr).roomId);
//            curr++;
            RoomCreateDialog roomCreateDialog = new RoomCreateDialog();
            roomCreateDialog.setCancelable(false);
            roomCreateDialog.show(getSupportFragmentManager(), "!");
            return true;

         case R.id.menuActivityRoomExit:
            Utils.showConfirmActionDialog(Utils.stringFromRes(getApplicationContext(),
                    R.string.appExit), new MyAlertDialog.OnDismissedListener() {
               @Override
               public void onDismissed(boolean isPositive) {
                  if (isPositive) {
                     ListService.stopTimer();
                     stopService(new Intent(RoomActivity.this, ListService.class));
                     RoomActivity.this.finish();
                  }
               }
            }, RoomActivity.this);
            return true;

         default:
            return false;
      }

   }

   @Override
   public void openOptionsMenu() {
      super.openOptionsMenu();
   }

   private void createListView() {
      llBottom = (LinearLayout) findViewById(R.id.roomActivityLlBottom);
      roomListView = (ListView) findViewById(R.id.roomListView);
      tcAdapter = new ListAdapter(getApplicationContext(), RoomActivity.this, roomListView);
      roomListView.setAdapter(tcAdapter);
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
            }, 500);
         } else
            Utils.toast(RoomActivity.this, Utils.stringFromRes(getApplicationContext(),
                    R.string.errorNotFound));
      }
   }

   public void checkBan() {
      String banReason = preferences.getString("ban", "");
      if (!banReason.isEmpty())
         Utils.showErrorWithListenerDialog(Utils.stringFromRes(getApplicationContext(),
                 R.string.banMessage) + " " + banReason, new MyAlertDialog.OnDismissedListener() {
            @Override
            public void onDismissed(boolean isPositive) {
               RoomActivity.this.finish();
            }
         }, RoomActivity.this);
   }

   private void scroll(final int pos) {
//      Log.d("!", "scroll to " + String.valueOf(pos));
      scrollFinished = false;
      int postTime = 0;
      if (pos > 0) {
         int offset = (getResources().getDimensionPixelSize(R.dimen.room_activity_divider_size)
                 + holderHeight) * pos;
         if (pos < ItemList.list.size() - 1)
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
            if (roomProgressBar != null) roomProgressBar.setVisibility(View.VISIBLE);
            if (roomListView != null) roomListView.setVisibility(View.INVISIBLE);
         }
      });
   }

   public void showList() {
      runOnUiThread(new Runnable() {
         @Override
         public void run() {
            if (getActionBar() != null) getActionBar().show();
            if (roomProgressBar != null) roomProgressBar.setVisibility(View.INVISIBLE);
            if (roomListView != null) roomListView.setVisibility(View.VISIBLE);
         }
      });
   }

}
