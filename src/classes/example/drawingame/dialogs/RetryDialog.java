package classes.example.drawingame.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import classes.example.drawingame.activities.DrawingActivity;
import classes.example.drawingame.R;

/**
 * Prompts user for action if
 * something gone wrong (for
 * example, it's no internet)
 */

public class RetryDialog {

    private AlertDialog dialog;
    private Activity activity;

    public RetryDialog(Activity ma, String message) {
        activity = ma;

        dialog = new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(activity.getString(R.string.retry),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                                activity.finish();
                                activity.startActivity(new Intent(
                                        activity,
                                        DrawingActivity.class));
                            }
                        }
                )
                .setNegativeButton(activity.getString(R.string.exit),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                activity.finish();
                            }
                        }
                )
                .setCancelable(false)
                .create();
    }

    public void show() {
        dialog.show();
    }
}
