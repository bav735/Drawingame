package classes.example.drawingame.drawing_activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import classes.example.drawingame.R;
import classes.example.drawingame.drawing_activity.draw_view.DrawView;

/**
 * Prompts user to choose stroke width
 */

public class ChangeWidthDialog extends DialogFragment {

    private DrawingActivity drawingActivity;
    private View view;
    private SeekBar seekBar;
    private int endStrokeWidth;
    private DrawView dialogDrawView;

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        drawingActivity = (DrawingActivity) getActivity();
        view = drawingActivity.getLayoutInflater().inflate(R.layout.dialog_change_width, null);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setProgress(drawingActivity.drawView.strokeWidth);
        seekBar.setMax(DrawView.maxWidth);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                endStrokeWidth = seekBar.getProgress();
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
        dialogDrawView.init(drawingActivity, true,
                ((BitmapDrawable) drawingActivity.getResources().getDrawable(R.drawable.blankimg_white_without_frame)).getBitmap());
        dialogDrawView.strokeWidth = drawingActivity.drawView.strokeWidth;
        endStrokeWidth = drawingActivity.drawView.strokeWidth;
        dialogDrawView.drawingColor = drawingActivity.drawView.drawingColor;
        if (drawingActivity.drawView.isOnEraser) {
            dialogDrawView.drawingBitmap = ((BitmapDrawable) drawingActivity.getResources().
                    getDrawable(R.drawable.blackimg_black)).getBitmap();
            dialogDrawView.isOnEraser = true;
            TextView tv = (TextView) view.findViewById(R.id.textViewChangeWidthDialog);
            tv.setText(R.string.dialogChangeWidthHintOnEraser);
            tv.setTextColor(Color.WHITE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(drawingActivity)
                .setView(view)
                .setPositiveButton(getString(R.string.dialogChangeWidthOk),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                drawingActivity.drawView.strokeWidth = endStrokeWidth;
                                dismiss();
                            }
                        }
                );
        return builder.create();
    }
}
