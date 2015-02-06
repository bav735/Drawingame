package classes.example.drawingame.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;

import classes.example.drawingame.R;

/**
 * Created by A on 30.12.2014.
 */
public class ToastDialog {
    AlertDialog dialog = null;

    public ToastDialog(final String message) {
//        Utils.roomActivity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
        dialog = new AlertDialog.Builder(Utils.roomActivity)
                .setTitle(Utils.stringFromRes(R.string.toastDialogError))
                .setMessage(message)
                .setPositiveButton(Utils.stringFromRes(R.string.toastDialogGotIt), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                })
                .create();
//            }
//        });

    }

    public ToastDialog(final OnDismissedListener onDismissedListener) {
        dialog = new AlertDialog.Builder(Utils.roomActivity)
                .setTitle(Utils.stringFromRes(R.string.toastDialogError))
                .setMessage(Utils.stringFromRes(R.string.errorDb))
                .setPositiveButton(Utils.stringFromRes(R.string.toastDialogGotIt), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onDismissedListener.onDismissed(true);
                    }
                })
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    public ToastDialog(final String message, final OnDismissedListener listener) {
        dialog = new AlertDialog.Builder(Utils.roomActivity)
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
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    public void show() {
        if (dialog != null)
            dialog.show();
    }

    public interface OnDismissedListener {
        void onDismissed(boolean isOk);
    }
}
