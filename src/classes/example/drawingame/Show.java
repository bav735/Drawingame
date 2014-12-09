package classes.example.drawingame;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

/**
 * Shows toast
 */
public class Show {
    public static void toast(final Activity activity, final String s) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
            }
        });
    }
    public static void dialog(FragmentActivity activity, DialogFragment dialogFragment) {
        dialogFragment.setCancelable(false);
        dialogFragment.show(activity.getSupportFragmentManager(), "!");
    }
}
