package classes.example.drawingame.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import classes.example.drawingame.Generator;
import classes.example.drawingame.ImgurUpload;
import classes.example.drawingame.R;
import classes.example.drawingame.Show;
import classes.example.drawingame.activities.ChooseRoomActivity;
import classes.example.drawingame.fromafilechooser.FileUtils;

/**
 * Created by A on 26.11.2014.
 */
public class RoomCreateDialog extends DialogFragment {
    private final static int REQUEST_CODE_GALLERY = 1;
    private final static int REQUEST_CODE_CAMERA = 2;

    //    private RoomCreateDialog roomCreateDialog = this;
    private ChooseRoomActivity chooseRoomActivity;
    private View view;
    private EditText etRoomName;
    private Button btnCamera;
    private Button btnGallery;
    private ImageView ivRoomDrawing;
    private String imgPath;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK) {
        Uri imgUri = null;
        switch (requestCode) {
            case REQUEST_CODE_GALLERY:
                imgUri = data.getData();
                break;
            case REQUEST_CODE_CAMERA:
                imgUri = FileUtils.getUri(chooseRoomActivity, (Bitmap) data.getExtras().get("data"));
        }
        Picasso.with(chooseRoomActivity).load(imgUri).into(ivRoomDrawing);
//        }
    }

//    protected void onActivityResult(String newImgPath) {
//        Picasso.with(chooseRoomActivity).load(new File(newImgPath)).into(ivRoomDrawing);
//        this.imgPath = newImgPath;
//    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        chooseRoomActivity = (ChooseRoomActivity) getActivity();
//        try {
//            imgPath = FileUtils.getPathOfBlankImg(chooseRoomActivity);
//        } catch (IOException e) {
//            Show.toast(chooseRoomActivity, "File system error : " + e.toString());
//        }
        view = chooseRoomActivity.getLayoutInflater().inflate(R.layout.dialog_room_create, null);
        etRoomName = (EditText) view.findViewById(R.id.etRoomName);
        ivRoomDrawing = (ImageView) view.findViewById(R.id.ivHolderRoomDrawing);
        btnCamera = (Button) view.findViewById(R.id.btnCamera);
        btnGallery = (Button) view.findViewById(R.id.btnGallery);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                chooseRoomActivity.startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                chooseRoomActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_GALLERY);
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(chooseRoomActivity)
                .setView(view)
                .setTitle("Creating room")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog alertDialog = (AlertDialog) getDialog();
        if (alertDialog != null) {
            Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            if (positiveButton != null)
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            final String roomName = etRoomName.getText().toString();
                            if (!roomName.isEmpty() || !chooseRoomActivity.roomExists(roomName)) {
                                Bitmap imgBitmap = ((BitmapDrawable) ivRoomDrawing.getDrawable()).getBitmap();
                                String imgPath = FileUtils.save(chooseRoomActivity,
                                        imgBitmap, FileUtils.getCachedDir(), roomName);
                                new ImgurUpload(chooseRoomActivity, new ImgurUpload.OnImgUrlReceivedListener() {
                                    @Override
                                    public void onImgUrlReceived(String imgUrl) {
                                        chooseRoomActivity.addRoomToDB(Generator.id(), roomName, imgUrl);
                                    }
                                }).start(imgPath);
                                dismiss();
                            } else
                                Show.toast(chooseRoomActivity, "Room name is empty or such room already exists");
                        } catch (IOException e) {
                            Show.toast(chooseRoomActivity, e.toString());
                        }
                    }
                });
            Button negativeButton = alertDialog.getButton(Dialog.BUTTON_NEGATIVE);
            if (negativeButton != null)
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
        }
    }
}

