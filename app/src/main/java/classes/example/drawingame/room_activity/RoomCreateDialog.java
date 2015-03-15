package classes.example.drawingame.room_activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.room_activity.list_view.Item;
import classes.example.drawingame.room_activity.list_view.ItemList;
import classes.example.drawingame.room_activity.service.ListService;
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
   private Bitmap newImgBitmap;

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Utils.roomActivity.showProgress();
      super.onActivityResult(requestCode, resultCode, data);
      if (resultCode == Activity.RESULT_OK && data != null)
         switch (requestCode) {
            case REQUEST_CODE_GALLERY:
               Uri imgUri = data.getData();
               String imgPath = Utils.getPath(getActivity(), imgUri);
               try {
                  newImgBitmap = Utils.getBitmapByPath(imgPath);
               } catch (Exception e) {
                  Utils.toast(getActivity(), "Error while image pick!");
               }
               break;
            case REQUEST_CODE_CAMERA:
               newImgBitmap = (Bitmap) data.getExtras().get("data");
         }
      setBitmapToIv();
   }


   @Override
   public Dialog onCreateDialog(Bundle bundle) {
      view = getActivity().getLayoutInflater().
              inflate(R.layout.dialog_room_create, null);
      etRoomName = (EditText) view.findViewById(R.id.roomCreateDialogEt);
      ivDialogRoomDrawing = (ImageView) view.findViewById(R.id.roomCreateDialogIv);
      newImgBitmap = ((BitmapDrawable) getResources().
              getDrawable(R.drawable.blankimg_white)).getBitmap();
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
               ListService.sendMessageShowPb();
               Item item = new Item();
               item.roomId = Utils.getNewId();
               item.lastEditorDeviceId = DataBase.thisDeviceId;
               item.roomName = roomName;
               ItemList.startAddItemToDB(item, Utils.getResizedBitmap(newImgBitmap));
               dismiss();
            } else
               ListService.sendMessageShowErrorDialog(R.string.dialogRoomCreateError);
         }
      });

      Dialog dialog = new Dialog(getActivity(), R.style.DialogStyle);
      dialog.setContentView(view);
      WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
      lp.dimAmount = 0.73f;
      return dialog;
   }

   private void setBitmapToIv() {
      LinearLayout.LayoutParams layoutParams = Utils.scaleToLl(newImgBitmap.getWidth(), newImgBitmap.getHeight(),
              Utils.dpToPx(144, getActivity()), Utils.dpToPx(224, getActivity()));
      layoutParams.gravity = Gravity.CENTER;
      ivDialogRoomDrawing.setLayoutParams(layoutParams);
      ivDialogRoomDrawing.setImageBitmap(newImgBitmap);
      ivDialogRoomDrawing.requestLayout();
   }

}

