package com.example.drawingame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Client extends Thread {

	public int port;
	public String ipa;
	public MainActivity mainActivity;
	public Socket clientSocket;
	public DataOutputStream serverOut;
	public DataInputStream serverIn;
	// private Button btnSave;
	// private Button btnUndo;
	// private Button btnRandom;
	private String name;
	// private DrawView drawView;
	private String fromServer;

	public Client(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
		this.port = mainActivity.port;
		this.ipa = mainActivity.ipa;
		// this.btnSave = btnClient;
		// this.btnUndo = btnUndo;
		// this.btnRandom = btnRandom;
		// this.drawView = drawView;
		this.name = mainActivity.deviceName;
	}

	public void run() {
		try {
			Log.d("!", "Creating client sockets and streams2\n"
					+ toString(clientSocket) + "\n" + toString(serverIn));

			clientSocket = new Socket(InetAddress.getByName(ipa), port);
			toast("Client on " + name + " was created");

			Log.d("!", "Creating client sockets and streams3\n"
					+ toString(clientSocket) + "\n" + toString(serverIn));

			serverOut = new DataOutputStream(clientSocket.getOutputStream());
			// serverOut.writeObject(new Sending(getDate(), name, "in"));
			// se/rverOut.flush();
			serverIn = new DataInputStream(clientSocket.getInputStream());

			Log.d("!", "Creating client sockets and streams4-ok!\n");
		} catch (IOException e) {
			Log.d("!",
					"Problems with creating client socket! - " + e.toString());
			toast("Problems with creating client socket! - " + e.toString());
		}
		Log.d("!", "Writing to server0!");

		while (!clientSocket.isClosed()) {
			boolean ok = false;
			try {
				fromServer = serverIn.readUTF();
				ok = true;
				Log.d("!", "Reading server socket0");
			} catch (IOException e) {
				Log.d("!", "Couldn't read from server! - " + e.toString());
				toast("Couldn't read from server! - " + e.toString());
				close();
			}
			if (ok) {
				Log.d("!", "Reading server sending0");
				toast("Receiving sending from server...");
				mainActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mainActivity.drawView.recalc(fromServer);
					}
				});

				// if (fromServer.clientName.equals(this.name)) {
				// disableInteraction();
				// }
				// if (!fromServer.clientName.equals(this.name)) {
				// enableInteraction();
				// }
			} else {
				toast("Error while reading from server");
			}
		}
	}

	public void send() {
		Log.d("!", "Writing to server1!");
		Sending sending = new Sending(name, mainActivity.drawView.lineList,
				mainActivity.drawView.lineNum);
		Log.d("!", "Writing to server2!");
		try {
			Log.d("!", "Writing to server3!");
			serverOut.writeUTF(sending.toJsonObject().toString());
			Log.d("!", "Writing to server4!");
			serverOut.flush();
			Log.d("!", "Writing to server5!-ok");
			toast("Drawing was sent to server");
		} catch (IOException e) {
			toast("Couldn't send data to server!");
		}
	}

	// private void enableInteraction() {
	// activity.runOnUiThread(new Runnable() {
	// @Override
	// public void run() {
	// drawView.recalcPoints(fromServer);
	// drawView.isEnabled = true;
	// btnSave.setVisibility(View.VISIBLE);
	// btnUndo.setVisibility(View.VISIBLE);
	// btnRandom.setVisibility(View.VISIBLE);
	// }
	// });
	// }
	//
	// private void disableInteraction() {
	// activity.runOnUiThread(new Runnable() {
	// @Override
	// public void run() {
	// drawView.isEnabled = false;
	// btnSave.setVisibility(View.INVISIBLE);
	// btnUndo.setVisibility(View.INVISIBLE);
	// btnRandom.setVisibility(View.INVISIBLE);
	// }
	// });
	// }

	private void close() {
		Log.d("!", "Closing!1 " + name);
		if (!clientSocket.isClosed()) {
			try {
				clientSocket.close();
				Log.d("!", "Closing!2 " + name);
			} catch (IOException e) {
				Log.d("!", "Closing error -" + e.toString());
				toast("Closing error -" + e.toString());
			} catch (Throwable e) {
				Log.d("!", "destroying client error");
				toast("destroying client error");
			}
		}

	}

	private String toString(Object o) {
		return o == null ? "null" : o.toString();
	}

	private void toast(final String s) {
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mainActivity, s, Toast.LENGTH_LONG).show();
			}
		});
	}
}