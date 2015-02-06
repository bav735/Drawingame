package classes.example.drawingame.room_activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.utils.Utils;

/**
 * Created by A on 26.11.2014.
 */
public class RoomCreateDialog extends DialogFragment {
    private final static int REQUEST_CODE_GALLERY = 1;
    private final static int REQUEST_CODE_CAMERA = 2;

    private View view;
    private EditText etRoomName;
    private Button btnCamera;
    private Button btnGallery;
    private Button btnCreate;
    private Button btnCancel;
    private ImageView ivDialogRoomDrawing;
    private Bitmap imgBitmap;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Utils.roomActivity.showProgress();
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null)
            switch (requestCode) {
                case REQUEST_CODE_GALLERY:
                    Uri imgUri = data.getData();
                    String imgPath = Utils.getPath(imgUri);
                    imgBitmap = Utils.getBitmapByPath(imgPath);
                    break;
                case REQUEST_CODE_CAMERA:
                    imgBitmap = (Bitmap) data.getExtras().get("data");
            }
        setBitmapToIv();
    }


    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        view = Utils.roomActivity.getLayoutInflater().inflate(R.layout.dialog_room_create, null);
        etRoomName = (EditText) view.findViewById(R.id.roomCreateDialogEt);
        ivDialogRoomDrawing = (ImageView) view.findViewById(R.id.roomCreateDialogIv);
        imgBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.blankimg_white)).getBitmap();
        setBitmapToIv();

        btnCamera = (Button) view.findViewById(R.id.roomCreateDialogBtnCamera);
        btnGallery = (Button) view.findViewById(R.id.roomCreateDialogBtnGallery);
        btnCancel = (Button) view.findViewById(R.id.roomCreateDialogBtnCancel);
        btnCreate = (Button) view.findViewById(R.id.roomCreateDialogBtnCreate);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
            }
        });
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_GALLERY);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
//                Utils.roomActivity.showList();
            }
        });
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String roomName = etRoomName.getText().toString();
                if (!roomName.isEmpty()) {
//                    Utils.roomActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
                    Item item = new Item();
                    item.roomId = Utils.getNewId();
                    item.roomName = roomName;
//                    item.lastEditorDeviceId = DataBase.thisDeviceId;
                    item.bitmapToUpload = Utils.getResizedBitmap(imgBitmap);
                    try {
                        Utils.saveById(item.bitmapToUpload, item.roomId);
                    } catch (Exception e) {
                        item.bitmapToUpload = null;
                    }
                    item.pos = 0;
                    ItemList.add(item);
                    ItemList.uploadItem(0);
//                        }
//                    });
                    dismiss();
//                    Utils.roomActivity.showList();
                } else
                    Utils.toast(Utils.stringFromRes(R.string.dialogRoomCreateError));
            }
        });

        Dialog dialog = new Dialog(Utils.roomActivity, R.style.DialogStyle);
        dialog.setContentView(view);
        return dialog;
    }

    private void setBitmapToIv() {
        LinearLayout.LayoutParams layoutParams = Utils.scaleToLl(imgBitmap.getWidth(), imgBitmap.getHeight(),
                Utils.dpToPx(144), Utils.dpToPx(224));
        layoutParams.gravity = Gravity.CENTER;
        ivDialogRoomDrawing.setLayoutParams(layoutParams);
        ivDialogRoomDrawing.setImageBitmap(imgBitmap);
        ivDialogRoomDrawing.requestLayout();
    }
}

