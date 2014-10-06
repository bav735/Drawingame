package com.example.drawingame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class ChangeWidthDialog {

    public interface OnChangeWidthListener {
        void onOk(int strokeWidth);
    }

    private AlertDialog dialog;
    private MainActivity mainActivity;
    private SeekBar seekBar;
    private int strokeWidth;
    private OnChangeWidthListener changeWidthListener;
    private DrawView drawView;

    public ChangeWidthDialog(MainActivity parentActivity,
                             OnChangeWidthListener changeWidthListener1) {
        this.changeWidthListener = changeWidthListener1;
        mainActivity = parentActivity;
        strokeWidth = mainActivity.drawView.strokeWidth;
        LinearLayout linearLayout = new LinearLayout(mainActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        if (!mainActivity.drawView.isOnEraser) {
            drawView = new DrawView(mainActivity);
            drawView.init(mainActivity);
            drawView.setLayoutParams(new LayoutParams(drawView.displayWidth,
                    drawView.displayHeight / 2));
            drawView.strokeWidth = mainActivity.drawView.strokeWidth;
            drawView.drawingColor = mainActivity.drawView.drawingColor;
        }

        seekBar = new SeekBar(mainActivity);
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
                if (!mainActivity.drawView.isOnEraser)
                    drawView.changeStrokeWidthFromDialog(progress);
            }
        });
        linearLayout.addView(seekBar);
        if (!mainActivity.drawView.isOnEraser)
            linearLayout.addView(drawView);

        dialog = new AlertDialog.Builder((Context) mainActivity)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (changeWidthListener != null) {
                            changeWidthListener.onOk(strokeWidth);
                        }
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                            }
                        }
                ).create();
//        if (!mainActivity.drawView.isOnEraser) {
//            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Clear", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    drawView.clear();
//
//                }
//            });
//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface d) {
//                    Button button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
//                    button.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            drawView.clear();
//                        }
//                    });
//                }
//            });
//        }
        dialog.setView(linearLayout, 0, 0, 0, 0);

    }

    public void show() {
        dialog.show();
    }
}
