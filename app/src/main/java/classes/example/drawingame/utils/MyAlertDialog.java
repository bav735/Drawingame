package classes.example.drawingame.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;

import classes.example.drawingame.R;
import classes.example.drawingame.room_activity.RoomActivity;

/**
 * Created by A on 30.12.2014.
 */
public class MyAlertDialog {

   public static void showError(final String message, RoomActivity roomActivity) {
      AlertDialog dialog = new AlertDialog.Builder(roomActivity)
              .setTitle(Utils.stringFromRes(roomActivity, R.string.toastDialogErrorTitle))
              .setMessage(message)
              .setPositiveButton(Utils.stringFromRes(roomActivity, R.string.toastDialogGotIt),
                      new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                         }
                      })
              .setCancelable(false)
//              .setOnCancelListener(new DialogInterface.OnCancelListener() {
//                 @Override
//                 public void onCancel(DialogInterface dialog) {
//                    dialog.dismiss();
//                 }
//              })
              .create();
      dialog.show();
   }

   public static void showErrorWithListener(String message, final OnDismissedListener onDismissedListener,
                                            RoomActivity roomActivity) {
      AlertDialog dialog = new AlertDialog.Builder(roomActivity)
              .setTitle(Utils.stringFromRes(roomActivity, R.string.toastDialogErrorTitle))
              .setMessage(message)
              .setPositiveButton(Utils.stringFromRes(roomActivity, R.string.toastDialogGotIt),
                      new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            onDismissedListener.onDismissed(true);
                         }
                      })
              .setCancelable(false)
//              .setOnCancelListener(new DialogInterface.OnCancelListener() {
//                 @Override
//                 public void onCancel(DialogInterface dialog) {
//                    dialog.dismiss();
//                 }
//              })
              .create();
      dialog.show();
   }

   public static void showConfirmAction(final String message, final OnDismissedListener listener
           , RoomActivity roomActivity) {
      AlertDialog dialog = new AlertDialog.Builder(roomActivity)
              .setTitle(Utils.stringFromRes(roomActivity,R.string.toastDialogConfirm))
              .setMessage(message)
              .setPositiveButton(Utils.stringFromRes(roomActivity,R.string.toastDialogOk), new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    listener.onDismissed(true);
                 }
              })
              .setNegativeButton(Utils.stringFromRes(roomActivity,R.string.toastDialogNo), new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    listener.onDismissed(false);
                 }
              })
              .setCancelable(false)
//              .setOnCancelListener(new DialogInterface.OnCancelListener() {
//                 @Override
//                 public void onCancel(DialogInterface dialog) {
//                    dialog.dismiss();
//                 }
//              })
              .create();
      dialog.show();
   }

   public static void showRetryAction(String message, final OnDismissedListener listener
           , RoomActivity roomActivity) {
      AlertDialog dialog = new AlertDialog.Builder(roomActivity)
              .setTitle(Utils.stringFromRes(roomActivity,R.string.toastDialogRetryTitle))
              .setMessage(message)
              .setPositiveButton(Utils.stringFromRes(roomActivity,R.string.toastDialogRetry), new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    listener.onDismissed(true);
                 }
              })
              .setNegativeButton(Utils.stringFromRes(roomActivity,R.string.toastDialogDeny), new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    listener.onDismissed(false);
                 }
              })
              .setCancelable(false)
//              .setOnCancelListener(new DialogInterface.OnCancelListener() {
//                 @Override
//                 public void onCancel(DialogInterface dialog) {
//                    dialog.dismiss();
//                 }
//              })
              .create();
      dialog.show();
   }

   public interface OnDismissedListener {
      void onDismissed(boolean isPositive);
   }
}
