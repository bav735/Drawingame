package com.example.drawingame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class ChangeWidthDialog {

	public interface OnChangeWidthListener {
		void onOk(float strokeWidth);
	}

	private AlertDialog dialog;
	private MainActivity mainActivity;
	private SeekBar seekBar;
	private float strokeWidth;
	private OnChangeWidthListener changeWidthListener;
	private DrawView drawView;

	public ChangeWidthDialog(MainActivity mainActivity1,
			OnChangeWidthListener changeWidthListener1) {
		this.changeWidthListener = changeWidthListener1;
		this.mainActivity = mainActivity1;
		LinearLayout linearLayout = new LinearLayout(mainActivity);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		drawView = new DrawView(mainActivity);
		drawView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				200));
		drawView.strokeWidth = mainActivity.drawView.strokeWidth;
		drawView.drawingColor = mainActivity.drawView.drawingColor;
		drawView.isOnChangeWidth = true;

		seekBar = new SeekBar(mainActivity);
		seekBar.setProgress((int) mainActivity.drawView.strokeWidth);
		seekBar.setMax(50);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				strokeWidth = seekBar.getProgress();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				drawView.strokeWidth = progress;
				drawView.invalidate();
			}
		});
		linearLayout.addView(seekBar);
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
				.setNeutralButton("Clear", null)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						}).create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {
				Button button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
				button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						drawView.clear();
					}
				});
			}
		});
		dialog.setView(linearLayout, 0, 0, 0, 0);

	}

	public void show() {
		dialog.show();
	}
}
