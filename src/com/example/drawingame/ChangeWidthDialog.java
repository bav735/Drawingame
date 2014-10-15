package com.example.drawingame;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.SeekBar;

public class ChangeWidthDialog extends DialogFragment {

    private MainActivity mainActivity;
    private View view;
    private SeekBar seekBar;
    private int strokeWidth;
    private DrawView dialogDrawView;

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        mainActivity = (MainActivity) getActivity();
        view = mainActivity.getLayoutInflater().inflate(R.layout.change_width_dialog, null);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setProgress(mainActivity.drawView.strokeWidth);
        seekBar.setMax(DrawView.maxWidth);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                strokeWidth = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dialogDrawView.changeStrokeWidthFromDialog(progress);
            }
        });

        dialogDrawView = (DrawView) view.findViewById(R.id.drawViewChangeWidthDialog);
        dialogDrawView.init(mainActivity, true);
        dialogDrawView.strokeWidth = mainActivity.drawView.strokeWidth;
        dialogDrawView.drawingColor = mainActivity.drawView.drawingColor;
        if (mainActivity.drawView.isOnEraser) {
            dialogDrawView.backgroundColor = Color.BLACK;
            dialogDrawView.isOnEraser = true;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity)
                .setView(view)
                .setNegativeButton(getString(R.string.exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }
                )
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mainActivity.drawView.strokeWidth = strokeWidth;
                            }
                        }
                );
        return builder.create();
    }
}
