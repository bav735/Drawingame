package classes.example.drawingame.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import classes.example.drawingame.Client;
import classes.example.drawingame.Show;
import classes.example.drawingame.activities.DrawingActivity;
import classes.example.drawingame.R;
import classes.example.drawingame.views.DrawView;

/**
 * Appears at the very beginning,
 * needs for further app initialization
 * (inits client, DrawView)
 */

public class InitialDialog extends DialogFragment {
    private DrawingActivity drawingActivity;
    private View view;
    private EditText etClientName;
    private EditText etChannelName;

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        drawingActivity = (DrawingActivity) getActivity();
        view = drawingActivity.getLayoutInflater().inflate(R.layout.dialog_initial, null);
        etClientName = (EditText) view.findViewById(R.id.etClientName);
        etChannelName = (EditText) view.findViewById(R.id.etChannelName);
        AlertDialog.Builder builder = new AlertDialog.Builder(drawingActivity)
                .setView(view)
                .setTitle(getString(R.string.enterInitialData))
                .setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                        String channelName = etChannelName.getText().toString();
                        String clientName = etClientName.getText().toString();
                        if (!channelName.isEmpty() && !clientName.isEmpty())
                            try {
                                drawingActivity.client = new Client(drawingActivity, channelName, clientName);
                                DrawView dv = (DrawView) drawingActivity.findViewById(R.id.drawViewMain);
                                dv.init(drawingActivity, false,null);
                                drawingActivity.drawView = dv;
                                dismiss();
                            } catch (Exception e) {
                                drawingActivity.showRetryDialogWithMessage(getString(R.string.noClient));
                            }
                        else
                            Show.toast(drawingActivity, drawingActivity.getString(R.string.initialDataError));
                    }
                });
            Button negativeButton = alertDialog.getButton(Dialog.BUTTON_NEGATIVE);
            if (negativeButton != null)
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        drawingActivity.finish();
                    }
                });
        }
    }
}

