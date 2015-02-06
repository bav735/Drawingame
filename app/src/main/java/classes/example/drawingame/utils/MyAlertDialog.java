package classes.example.drawingame.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;

import classes.example.drawingame.R;

/**
 * Created by A on 30.12.2014.
 */
public class MyAlertDialog {

   public static void showError(final String message) {
      AlertDialog dialog = new AlertDialog.Builder(Utils.roomActivity)
              .setTitle(Utils.stringFromRes(R.string.toastDialogErrorTitle))
              .setMessage(message)
              .setPositiveButton(Utils.stringFromRes(R.string.toastDialogGotIt), new DialogInterface.OnClickListener() {
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

   public static void showErrorWithListener(String message, final OnDismissedListener onDismissedListener) {
      AlertDialog dialog = new AlertDialog.Builder(Utils.roomActivity)
              .setTitle(Utils.stringFromRes(R.string.toastDialogErrorTitle))
              .setMessage(message)
              .setPositiveButton(Utils.stringFromRes(R.string.toastDialogGotIt), new DialogInterface.OnClickListener() {
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

   public static void showConfirmAction(final String message, final OnDismissedListener listener) {
      AlertDialog dialog = new AlertDialog.Builder(Utils.roomActivity)
              .setTitle(Utils.stringFromRes(R.string.toastDialogConfirm))
              .setMessage(message)
              .setPositiveButton(Utils.stringFromRes(R.string.toastDialogOk), new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    listener.onDismissed(true);
                 }
              })
              .setNegativeButton(Utils.stringFromRes(R.string.toastDialogNo), new DialogInterface.OnClickListener() {
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

   public static void showRetryAction(String message, final OnDismissedListener listener) {
      AlertDialog dialog = new AlertDialog.Builder(Utils.roomActivity)
              .setTitle(Utils.stringFromRes(R.string.toastDialogRetryTitle))
              .setMessage(message)
              .setPositiveButton(Utils.stringFromRes(R.string.toastDialogRetry), new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    listener.onDismissed(true);
                 }
              })
              .setNegativeButton(Utils.stringFromRes(R.string.toastDialogDeny), new DialogInterface.OnClickListener() {
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
