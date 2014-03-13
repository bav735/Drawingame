package com.example.chadro;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Client extends Thread {

	public int port;
	public String ipa;
	public Activity activity;
	public Socket clientSocket;
	public ObjectOutputStream serverOut;
	public ObjectInputStream serverIn;
	public EditText clientEt;
	public Button clientBtn;
	// private LinearLayout clientLl;
	public String clientName;
	public TextView clientTv;

	public Client(Activity a, String i, int p, EditText et, Button btn,
			TextView tv, LinearLayout ll, String cn) throws IOException {
		activity = a;
		port = p;
		ipa = i;
		clientEt = et;
		clientBtn = btn;
		// clientLl = ll;
		clientName = cn;
		clientTv = tv;
	}

	public void run() {
		try {
			Log.d("!", "Creating client sockets and streams2\n"
					+ toString(clientSocket) + "\n" + toString(serverIn));

			clientSocket = new Socket(InetAddress.getByName(ipa), port);

			Log.d("!", "Creating client sockets and streams3\n"
					+ toString(clientSocket) + "\n" + toString(serverIn));

			serverOut = new ObjectOutputStream(clientSocket.getOutputStream());
			serverOut.writeObject(new Sending(getDate(), clientName, "in"));
			serverOut.flush();
			serverIn = new ObjectInputStream(clientSocket.getInputStream());

			Log.d("!", "Creating client sockets and streams4-ok!\n");
		} catch (IOException e) {
			Log.d("!",
					"Problems with creating client socket! - " + e.toString());
			printToScreen("Problems with creating client socket! - "
					+ e.toString());
		}
		Log.d("!", "Writing to server0!");
		clientBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("!", "Writing to server1!");
				Sending toServer = new Sending();
				toServer.client = clientName;
				toServer.message = clientEt.getText().toString();
				toServer.date = getDate();
				Log.d("!", "Writing to server2!");
				try {
					Log.d("!", "Writing to server3!");
					serverOut.writeObject(toServer);
					Log.d("!", "Writing to server4!");
					serverOut.flush();
					Log.d("!", "Writing to server5!-ok");
				} catch (IOException e) {
					printToScreen("Couldn't send data to server!");
				}
				clientEt.setText("");

			}
		});
		while (!clientSocket.isClosed()) {
			Sending fromServer = new Sending();
			boolean ok = false;
			try {
				fromServer = (Sending) serverIn.readObject();
				ok = true;
				Log.d("!", "Reading server socket0");
			} catch (IOException e) {
				Log.d("!", "Couldn't read from server! - " + e.toString());
				// printToScreen("Couldn't read from server! - " +
				// e.toString());
				close();
			} catch (ClassNotFoundException e) {
				printToScreen("Sending not found - " + e.toString());
				Log.d("!", "Sending not found - " + e.toString());
			}
			// if (fromServer == null)
			// printToScreen("Null sending from server!");
			if (ok) {
				Log.d("!", "Reading server sending0");
				if (fromServer.str.equals("in") || fromServer.str.equals("out")) {
					if (fromServer.str.equals("in")) {
						if (fromServer.client.equals(clientName))
							fromServer.client += " (You)";
						else
							printToScreen("Client " + fromServer.client
									+ " logged in");
					}
					if (fromServer.str.equals("out"))
						if (fromServer.client.equals(clientName)) {
							fromServer.client += " (You)";
							close();
						} else {// !fromServer.client.equals(clientName))
							printToScreen("Client " + fromServer.client
									+ " logged out");
							Log.d("!", "Reading server sending1");
						}
					ClientsStatusesDB.db(activity).write(fromServer);

				}
				if (fromServer.str.equals("")) {
					if (fromServer.client.equals(clientName)) {
						fromServer.client += " (You)";
						printToScreen("Message was sent");
					} else
						printToScreen(fromServer.client + " wrote a message - "
								+ fromServer.message);
					ChatHistoryDB.db(activity).write(fromServer);
					Log.d("!", "Reading server sending2");
				}

			}
		}
	}

	public void logout() {
		try {
			serverOut.writeObject(new Sending(getDate(), clientName, "out"));
			serverOut.flush();
		} catch (IOException e) {
			Log.d("!", "logout error - " + e.toString());
		}

	}

	public synchronized void close() {

		Log.d("!", "Closing!1 " + clientName);
		if (!clientSocket.isClosed()) {
			try {
				clientSocket.close();
				Log.d("!", "Closing!2 " + clientName);

				if (clientBtn != null && clientEt != null) {
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							LinearLayout ll = (LinearLayout) activity
									.findViewById(R.id.LL);
							ll.removeView(clientBtn);
							ll.removeView(clientEt);
							ll.removeView(clientTv);
							Log.d("!", "Closing!3 " + clientName);
						}
					});

				}
			} catch (IOException e) {
				Log.d("!", "Closing error -" + e.toString());
			} catch (Throwable e) {
				Log.d("!", "destroying client error");
			}
		}

	}

	private String toString(Object o) {
		return o == null ? "null" : o.toString();
	}

	private String getDate() {
		Calendar c = Calendar.getInstance();//
		return new SimpleDateFormat("dd.mm.yyyy (hh:mm:ss)")
				.format(c.getTime());
	}

	// private void printToLayout(String s, final LinearLayout l) {
	// final TextView tv = new TextView(activity);
	// tv.setText(s);
	// activity.runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// l.addView(tv);
	// }
	// });
	// }

	private void printToScreen(final String s) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
			}
		});
	}
}