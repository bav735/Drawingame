package com.example.drawingame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class ClientDialog extends DialogFragment {
    private MainActivity mainActivity;
//
//    public ClientDialog(MainActivity ma) {
//        mainActivity = ma;
//        dialogFragment = new DialogFragment().
//        dialogFragment.setCancelable(false);
//        dialogFragment.show(mainActivity.getSupportFragmentManager(), "!");
//    }

    @Override
    public void onAttach(Activity activity) {
        mainActivity = (MainActivity) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.client_dialog, null);
        final EditText etClientName = (EditText) view.findViewById(R.id.etClientName);
        final EditText etChannelName = (EditText) view.findViewById(R.id.etChannelName);
        AlertDialog.Builder builder = new AlertDialog.Builder((Context) mainActivity)
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
                                mainActivity.channelName = etChannelName.getText().toString();
                                mainActivity.clientName = etClientName.getText().toString();
                                try {
                                    mainActivity.client = new Client(mainActivity);
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


