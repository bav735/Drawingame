package classes.example.drawingame;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

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
        view = drawingActivity.getLayoutInflater().inflate(R.layout.change_width_or_eraser_dialog, null);
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
        dialogDrawView.init(drawingActivity, true);
        dialogDrawView.strokeWidth = drawingActivity.drawView.strokeWidth;
        endStrokeWidth = drawingActivity.drawView.strokeWidth;
        dialogDrawView.drawingColor = drawingActivity.drawView.drawingColor;
        if (drawingActivity.drawView.isOnEraser) {
            dialogDrawView.backgroundColor = Color.BLACK;
            dialogDrawView.isOnEraser = true;
            TextView tv = (TextView) view.findViewById(R.id.textViewChangeWidthDialog);
            tv.setText(R.string.dialogChangeWidthHintOnEraser);
            tv.setTextColor(Color.WHITE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(drawingActivity)
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
                                drawingActivity.drawView.strokeWidth = endStrokeWidth;
                            }
                        }
                );
        return builder.create();
    }
}
