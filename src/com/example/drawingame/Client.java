package com.example.drawingame;

import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

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
    private String clientName;
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
        this.clientName = mainActivity.deviceName;
    }

    public void run() {
        try {
            Log.d("!", "Creating client sockets and streams2\n"
                    + toString(clientSocket) + "\n" + toString(serverIn));

            clientSocket = new Socket(InetAddress.getByName(ipa), port);
            toast("Client on " + clientName + " was created");

            Log.d("!", "Creating client sockets and streams3\n"
                    + toString(clientSocket) + "\n" + toString(serverIn));

            serverOut = new DataOutputStream(clientSocket.getOutputStream());
            // serverOut.writeObject(new Sending(getDate(), clientName, "in"));
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
                final Sending sending = new Sending(fromServer);
                if (!sending.clientName.equals(clientName)) {
                    toast("Received commit from " + sending.clientName);
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.drawView.recalcFromCommit(sending);
                        }
                    });
                }
                // if (fromServer.clientName.equals(this.clientName)) {
                // disableInteraction();
                // }
                // if (!fromServer.clientName.equals(this.clientName)) {
                // enableInteraction();
                // }
            } else {
                toast("Error while reading from server");
            }
        }
    }

    public void send() {
        Log.d("!", "Writing to server1!");
        Sending sending = new Sending(clientName, mainActivity.drawView);
        Log.d("!", "Writing to server2!");
        try {
            Log.d("!", "Writing to server3!");
            serverOut.writeUTF(sending.toJsonObject().toString());
            Log.d("!", "Writing to server4!");
            serverOut.flush();
            Log.d("!", "Writing to server5!-ok");
          //  toast("Drawing was sent to server");
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
        Log.d("!", "Closing!1 " + clientName);
        if (!clientSocket.isClosed()) {
            try {
                clientSocket.close();
                Log.d("!", "Closing!2 " + clientName);
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