package com.example.drawingame;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class InitialDialog extends DialogFragment {
    private MainActivity mainActivity;
    private View view;
    private EditText etClientName;
    private EditText etChannelName;

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        mainActivity = (MainActivity) getActivity();
        view = mainActivity.getLayoutInflater().inflate(R.layout.initial_dialog, null);
        etClientName = (EditText) view.findViewById(R.id.etClientName);
        etChannelName = (EditText) view.findViewById(R.id.etChannelName);
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity)
                .setView(view)
                .setTitle(getString(R.string.enterInitialData))
                .setNegativeButton(getString(R.string.exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                mainActivity.finish();
                            }
                        }
                )
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String channelName = etChannelName.getText().toString();
                                String clientName = etClientName.getText().toString();
                                try {
                                    mainActivity.client = new Client(mainActivity, "channel_" + channelName, clientName);
                                    DrawView dv = (DrawView) mainActivity.findViewById(R.id.drawViewMain);
                                    dv.init(mainActivity, false);
                                    mainActivity.drawView = dv;
                                } catch (Exception e) {
                                    Log.d("!", "Client create error - " + e.toString());
                                    RetryDialog cd = new RetryDialog(mainActivity, getString(R.string.noClient));
                                    cd.show();
                                }
                            }
                        }
                );
        return builder.create();
    }
}


