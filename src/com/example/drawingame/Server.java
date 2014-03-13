package com.example.drawingame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class Server extends Thread {

	private ServerSocket serverSocket = null; // сам сервер-сокет
	private Thread serverThread; // главная нить обработки сервер-сокета
	private int port;
	private Activity activity;
	private BlockingQueue<SocketProcessor> socketProcessors = new LinkedBlockingQueue<SocketProcessor>();
	private String lastSendingString;

	public Server(Activity a, int p) {
		port = p;
		activity = a;
	}

	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			toast("Server was created");
		} catch (IOException e) {
			Log.d("!", "Could not create server! - " + e.toString());
			toast("Could not create server! - " + e.toString());
		}

		serverThread = Thread.currentThread();
		while (!serverThread.isInterrupted()) {
			Log.d("!", "Running receiving of sockets!");

			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
				// how to detect which client this socket belong to?
				Log.d("!", "accepted socket -" + toString(clientSocket));
			} catch (IOException e) {
				Log.d("!", "Could not get socket! - " + e.toString());
				toast("Could not get socket! - " + e.toString());
				// shutdownServer();
			}

			if (clientSocket != null) {
				SocketProcessor sp = new SocketProcessor(clientSocket);
				socketProcessors.add(sp);
				sp.start();
				if (lastSendingString != null)
					sp.send(lastSendingString);
				toast("Server: \nNew client was added");
				Log.d("!", "Processing socket - " + clientSocket.toString());
			} else {
				Log.d("!", "Socket is null!");
				toast("Socket is null!");
			}
		}
	}

	private String toString(Object o) {
		return o == null ? "null" : o.toString();
	}

	private class SocketProcessor extends Thread {
		Socket clientSocket;
		DataInputStream socketIn;
		DataOutputStream socketOut;

		SocketProcessor(Socket s) {
			clientSocket = s;
			Log.d("!", "Initialized socket for processing - " + toString(s));
		}

		public void run() {
			try {
				socketIn = new DataInputStream(clientSocket.getInputStream());
				socketOut = new DataOutputStream(clientSocket.getOutputStream());
				Log.d("!", "created data streams! - " + toString(socketIn));
			} catch (IOException e) {
				Log.d("!", "Couldnt create data streams! - " + e.toString());
				toast("Couldnt create data streams! - " + e.toString());
			}

			while (!clientSocket.isClosed()) {
				Log.d("!", "Sending data to client!");
				String toClient = null;
				try {
					toClient = socketIn.readUTF();
					Log.d("!", "We received - " + toString(toClient));
				} catch (IOException e) {
					Log.d("!", "Couldnt read socket! - " + e.toString());
					toast("Couldnt read socket! - " + e.toString());
					close();
				}

				if (toClient == null) {
					close();
				} else {
					lastSendingString = toClient;
					for (SocketProcessor sp : socketProcessors) {
						sp.send(toClient);
					}
				}
			}
		}

		private String toString(Object o) {
			return o == null ? "null" : o.toString();
		}

		public synchronized void send(String s) {
			try {
				socketOut.writeUTF(s);
				socketOut.flush();
				toast("Sending...");
			} catch (IOException e) {
				close();
				Log.d("!", "Error while writing to socket - " + e.toString());
				toast("Error while writing to socket - " + e.toString());
			}
		}

		public synchronized void close() {
			socketProcessors.remove(this); // убираем из списка
			if (!clientSocket.isClosed()) {
				try {
					clientSocket.close(); // закрываем
				} catch (IOException e) {
					toast("Couldn't close socket! - " + e.toString());
				}
			}
		}

	}

	private void toast(final String s) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
			}
		});
	}
}