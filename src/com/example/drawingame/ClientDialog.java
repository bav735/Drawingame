package com.example.drawingame;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class ClientDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        final MainActivity mainActivity = (MainActivity) getActivity();
        final View view = mainActivity.getLayoutInflater().inflate(R.layout.client_dialog, null);
        final EditText etClientName = (EditText) view.findViewById(R.id.etClientName);
        final EditText etChannelName = (EditText) view.findViewById(R.id.etChannelName);
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity)
                .setView(view)
                .setTitle("Enter initial data:")
                .setNegativeButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                mainActivity.finish();
                            }
                        }
                )
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String channelName = etChannelName.getText().toString();
                                String clientName = etClientName.getText().toString();
                                try {
                                    mainActivity.client = new Client(mainActivity, "channel_" + channelName, clientName);
                                } catch (Exception e) {
                                    Log.d("!", "Client create error - " + e.toString());
                                    RetryDialog cd = new RetryDialog(mainActivity, "Couldn't create client on your device.");
                                    cd.show();
                                }
                            }
                        }
                );
        return builder.create();
    }
}


