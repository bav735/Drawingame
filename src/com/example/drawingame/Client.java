package com.example.drawingame;

import android.widget.Toast;
import ibt.ortc.api.Ortc;
import ibt.ortc.extensibility.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Client extends Ortc {
    private MainActivity mainActivity;
    private String clientName;
    private OrtcClient ortcClient;
    private int reconnectingTries;

    private OnConnected onConnected = new OnConnected() {
        @Override
        public void run(final OrtcClient sender) {
            // Messaging client connected

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ortcClient.subscribe(MainActivity.channelName, true,
                            new OnMessage() {
                                public void run(OrtcClient sender, String channel,
                                                String message) {
                                    final Sending sending = new Sending(message);
                                    if (!sending.clientName.equals(clientName)) {
                                        toast("Received commit from " + sending.clientName);
                                        mainActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mainActivity.drawView.recalcFromCommit(sending);
                                            }
                                        });
                                    }
                                }

                                ;
                            }
                    );
                }
            });
        }
    };

    private OnDisconnected onDisconnected = new OnDisconnected() {
        @Override
        public void run(OrtcClient arg0) {
            mainActivity.runOnUiThread(new Runnable() {

                public void run() {
                    toast("Disconnected!");
                }
            });
        }
    };

    private OnReconnected onReconnected = new OnReconnected() {

        public void run(final OrtcClient sender) {
            mainActivity.runOnUiThread(new Runnable() {

                public void run() {
                    reconnectingTries = 0;
                    toast("Reconnected!");
                }
            });
        }
    };

    private OnReconnecting onReconnecting = new OnReconnecting() {

        public void run(OrtcClient sender) {
            mainActivity.runOnUiThread(new Runnable() {

                public void run() {
                    reconnectingTries++;
                    toast("Reconnecting... Attempt #" + String.valueOf(reconnectingTries));
                }
            });
        }
    };

    private OnSubscribed onSubscribed = new OnSubscribed() {
        @Override
        public void run(OrtcClient sender, final String channel) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast("Subscribed to channel: " + channel);
                }
            });
        }
    };

    private OnUnsubscribed onUnsubscribed = new OnUnsubscribed() {

        public void run(OrtcClient sender, final String channel) {
            mainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    toast("Unsubscribed from channel: " + channel);
                }
            });
        }
    };

    public Client(MainActivity mainActivity) throws Exception {
        this.mainActivity = mainActivity;
        clientName = android.os.Build.MODEL + "(" + currentTime() + ")";
        Ortc ortc = new Ortc();
        OrtcFactory factory = ortc.loadOrtcFactory(MainActivity.ortcType);
        ortcClient = factory.createClient();
        ortcClient.setApplicationContext(mainActivity.getApplicationContext());
        ortcClient.setClusterUrl(MainActivity.serverUrl);
        ortcClient.setConnectionMetadata(MainActivity.connectionMetadata);
        ortcClient.onConnected = onConnected;
        ortcClient.onDisconnected = onDisconnected;
        ortcClient.onReconnected = onReconnected;
        ortcClient.onReconnecting = onReconnecting;
        ortcClient.onSubscribed = onSubscribed;
        ortcClient.connect(MainActivity.applicationKey, MainActivity.authenticationToken);
    }

    public void send() {
        Sending sending = new Sending(clientName, mainActivity.drawView);
        ortcClient.send(MainActivity.channelName, sending.toJsonObject().toString());
        toast("Commit was sent!");
    }

    private void toast(final String s) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainActivity, s, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String currentTime() {
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();
        String strTime = sdfTime.format(now);
        return strTime;
    }
}