package com.example.drawingame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class ConnectionDialog {

	private AlertDialog dialog;
	private MainActivity mainActivity;

	public ConnectionDialog(MainActivity mainActivity1) {
		this.mainActivity = mainActivity1;

		dialog = new AlertDialog.Builder((Context) mainActivity)
				.setMessage(
						"A network error has occured. Check your internet connection.")
				.setPositiveButton("Retry",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								mainActivity.finish();
								mainActivity.startActivity(new Intent(
										(Context) mainActivity,
										MainActivity.class));
							}
						})
				.setNegativeButton("Exit",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								mainActivity.finish();
							}
						})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					// if back button is used, call back our listener.
					@Override
					public void onCancel(DialogInterface paramDialogInterface) {
					}
				}).create();
		// kill all padding from the dialog window
		// dialog.setView(view, 0, 0, 0, 0);

		// move cursor & target on first draw

	}

	public void show() {
		dialog.show();
	}
}
