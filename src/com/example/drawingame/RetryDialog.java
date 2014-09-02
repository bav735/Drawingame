package com.example.drawingame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class RetryDialog {

    private AlertDialog dialog;
    private MainActivity mainActivity;

    public RetryDialog(MainActivity ma, String message) {
        mainActivity = ma;

        dialog = new AlertDialog.Builder((Context) mainActivity)
                .setMessage(message)
                .setPositiveButton("Retry",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                                mainActivity.client.disconnect();
                                mainActivity.finish();
                                mainActivity.startActivity(new Intent(
                                        (Context) mainActivity,
                                        MainActivity.class));
                            }
                        }
                )
                .setNegativeButton("Exit",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                                mainActivity.finish();
                            }
                        }
                )
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                    }
                }).create();
    }

    public void show() {
        dialog.show();
    }
}
