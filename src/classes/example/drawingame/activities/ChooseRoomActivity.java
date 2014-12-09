package classes.example.drawingame.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import classes.example.drawingame.ImgurUpload;
import classes.example.drawingame.R;
import classes.example.drawingame.Show;
import classes.example.drawingame.dialogs.RetryDialog;
import classes.example.drawingame.dialogs.RoomCreateDialog;
import classes.example.drawingame.fromrealtimetodo.TodoCustomAdapter;
import co.realtime.storage.ItemAttribute;
import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.StorageRef;
import co.realtime.storage.TableRef;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;
import co.realtime.storage.ext.OnReconnected;
import co.realtime.storage.ext.StorageException;

public class ChooseRoomActivity extends FragmentActivity implements TodoCustomAdapter.TodoCustomAdapterReceiver {
    public final static String TABLE_ROOM = "room";
    public final static String ATTRIBUTE_ROOM_NAME = "roomName";
    public final static String ATTRIBUTE_ROOM_IMAGE_URL = "roomImageUrl";
    public final static String ATTRIBUTE_ROOM_ID = "roomId";

    private final static String APPLICATION_KEY = "XEQyNG";
    private final static String PRIVATE_KEY = "m8vUKz6sRvzw";
    private final static String STORAGE_URL = "https://storage-balancer.realtime.co/server/ssl/1.0";
    private final static String AUTH_TOKEN = "PM.Anonymous";

    private final static int REQUEST_CODE_IMG_EDITED = 3;

    private ChooseRoomActivity chooseRoomActivity = this;
    private Button btnAddRoom;
    private Button btnRefresh;
    private TableRef tableRef;
    private OnError onError;
    private ListView roomListView;
    private TodoCustomAdapter tcAdapter;
    private ArrayList<LinkedHashMap<String, ItemAttribute>> roomList;
    private RoomCreateDialog roomCreateDialog;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_IMG_EDITED) {
                final int position = data.getIntExtra("position", 0);
                String path = data.getStringExtra("path");
                new ImgurUpload(chooseRoomActivity, new ImgurUpload.OnImgUrlReceivedListener() {
                    @Override
                    public void onImgUrlReceived(String newImgUrl) {
                        LinkedHashMap<String, ItemAttribute> item = roomList.get(position);
                        item.put(ATTRIBUTE_ROOM_IMAGE_URL, new ItemAttribute(newImgUrl));
                        tableRef.item(item.get(ATTRIBUTE_ROOM_ID), item.get(ATTRIBUTE_ROOM_NAME)).set(item, new OnItemSnapshot() {
                            @Override
                            public void run(ItemSnapshot itemSnapshot) {
                                if (itemSnapshot != null) {
//                                    int pos = getRoomPosition((String) itemSnapshot.val().get(ATTRIBUTE_ROOM_NAME).get());
//                                    Show.toast(chooseRoomActivity, String.valueOf(position == pos));
                                    if (position == -1)
                                        Show.toast(chooseRoomActivity, "error during update");
                                    else {
                                        Show.toast(chooseRoomActivity, "updated");
                                        roomList.set(position, itemSnapshot.val());
                                        Log.d("!", "new url = "+roomList.get(position).get(ATTRIBUTE_ROOM_IMAGE_URL).toString());
                                        updateListView();
                                    }
                                } else
                                    Show.toast(chooseRoomActivity, "error during update");
                            }
                        }, onError);
                    }
                }).start(path);
            } else
                roomCreateDialog.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_room);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);

        onError = new OnError() {
            @Override
            public void run(Integer code, String errorMessage) {
                Show.toast(chooseRoomActivity, String.format("st:: error: %d (%s)", code, errorMessage));
            }
        };

        StorageRef storage = null;
        try {
            storage = new StorageRef(APPLICATION_KEY, PRIVATE_KEY, AUTH_TOKEN, true, false, STORAGE_URL);
        } catch (StorageException e) {
            new RetryDialog(chooseRoomActivity, "Couldn't create storage on your device").show();
        }
        tableRef = storage.table(TABLE_ROOM);
//        tableRef.asc();

        storage.onReconnected(new OnReconnected() {
            @Override
            public void run(StorageRef sender) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getItems();
                    }
                });
            }
        });

        btnAddRoom = (Button) findViewById(R.id.btnAddRoom);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
        roomListView = (ListView) findViewById(R.id.roomListView);

        btnAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomCreateDialog = new RoomCreateDialog();
                Show.dialog(chooseRoomActivity, roomCreateDialog);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getItems();
            }
        });

        roomList = new ArrayList<LinkedHashMap<String, ItemAttribute>>();
        tcAdapter = new TodoCustomAdapter(chooseRoomActivity, R.layout.holder, roomList);
        tcAdapter.setActionsReceiver(chooseRoomActivity);
        roomListView.setAdapter(tcAdapter);
        getItems();
    }

    void getItems() {
        roomList.clear();
        tableRef.getItems(new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot item) {
                addRoomToList(item);
            }
        }, onError);
    }

    public void addRoomToDB(final String newRoomId, final String newRoomName, final String newRoomImageUrl) {
        tableRef.item(new ItemAttribute(newRoomId), new ItemAttribute(newRoomName)).get(new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot is) {
                if (is == null || is.val() == null) return;
                if (!is.val().isEmpty())
                    Show.toast(chooseRoomActivity, "Service is currently unavailable, try again later");
                else {
                    LinkedHashMap<String, ItemAttribute> newItem = new LinkedHashMap<String, ItemAttribute>();
                    newItem.put(ATTRIBUTE_ROOM_ID, new ItemAttribute(newRoomId));
                    newItem.put(ATTRIBUTE_ROOM_NAME, new ItemAttribute(newRoomName));
                    newItem.put(ATTRIBUTE_ROOM_IMAGE_URL, new ItemAttribute(newRoomImageUrl));
                    tableRef.push(newItem, new OnItemSnapshot() {
                        @Override
                        public void run(ItemSnapshot item) {
                            addRoomToList(item);
                        }
                    }, onError);
                }
            }
        }, onError);
    }


    private void addRoomToList(ItemSnapshot item) {
        if (item != null) {
            roomList.add(item.val());
            updateListView();
        }
//        else    toast("error during add" + String.valueOf(getPos));
    }

    public void removeRoom(int position) {
        LinkedHashMap<String, ItemAttribute> item = roomList.get(position);
        tableRef.item(item.get(ATTRIBUTE_ROOM_ID), item.get(ATTRIBUTE_ROOM_NAME)).del(new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot item) {
                if (item != null) {
                    int pos = getRoomPosition((String) item.val().get(ATTRIBUTE_ROOM_NAME).get());
                    if (pos == -1)
                        Show.toast(chooseRoomActivity, "error during remove");
                    else {
                        Show.toast(chooseRoomActivity, "removed");
                        roomList.remove(pos);
                        updateListView();
                    }
                } else
                    Show.toast(chooseRoomActivity, "error during remove");
            }
        }, onError);
    }

    public void editRoom(int position) {
        Intent intent = new Intent(chooseRoomActivity, DrawingActivity.class);
        intent.putExtra("position", position);
        String url = roomList.get(position).get(ATTRIBUTE_ROOM_IMAGE_URL).toString();
        intent.putExtra("url", url);
        String roomName = roomList.get(position).get(ATTRIBUTE_ROOM_NAME).toString();
        intent.putExtra("roomName", roomName);
        startActivityForResult(intent, REQUEST_CODE_IMG_EDITED);
    }

    private void updateListView() {
        chooseRoomActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tcAdapter.notifyDataSetChanged();
            }
        });
    }

    private int getRoomPosition(String roomName) {
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).get(ATTRIBUTE_ROOM_NAME).get().equals(roomName))
                return i;
        }
        return -1;
    }

    public boolean roomExists(String roomName) {
        if (getRoomPosition(roomName) == -1)
            return true;
        return false;
    }
}
