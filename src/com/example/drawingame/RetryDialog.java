package com.example.drawingame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class RetryDialog {

    private AlertDialog dialog;
    private MainActivity mainActivity;

    public RetryDialog(MainActivity ma, String message) {
        mainActivity = ma;

        dialog = new AlertDialog.Builder(mainActivity)
                .setMessage(message)
                .setPositiveButton(mainActivity.getString(R.string.retry),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                                mainActivity.finish();
                                mainActivity.startActivity(new Intent(
                                        mainActivity,
                                        MainActivity.class));
                            }
                        }
                )
                .setNegativeButton(mainActivity.getString(R.string.exit),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                mainActivity.finish();
                            }
                        }
                )
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                        dialog.cancel();
                        mainActivity.finish();
                    }
                }).create();
    }

    public void show() {
        dialog.show();
    }
}
