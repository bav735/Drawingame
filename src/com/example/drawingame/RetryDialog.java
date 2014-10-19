package com.example.drawingame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

/**
 * Prompts user for action if
 * something gone wrong (for
 * example, it's no internet)
 **/

public class RetryDialog {

    private AlertDialog dialog;
    private DrawingActivity drawingActivity;

    public RetryDialog(DrawingActivity ma, String message) {
        drawingActivity = ma;

        dialog = new AlertDialog.Builder(drawingActivity)
                .setMessage(message)
                .setPositiveButton(drawingActivity.getString(R.string.retry),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                                drawingActivity.finish();
                                drawingActivity.startActivity(new Intent(
                                        drawingActivity,
                                        DrawingActivity.class));
                            }
                        }
                )
                .setNegativeButton(drawingActivity.getString(R.string.exit),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                drawingActivity.finish();
                            }
                        }
                )
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                        dialog.cancel();
                        drawingActivity.finish();
                    }
                }).create();
    }

    public void show() {
        dialog.show();
    }
}
