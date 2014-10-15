package com.example.drawingame;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import ibt.ortc.api.OnEnablePresence;
import ibt.ortc.api.OnPresence;
import ibt.ortc.api.Ortc;
import ibt.ortc.api.Presence;
import ibt.ortc.extensibility.*;
import ibt.ortc.extensibility.exception.OrtcNotConnectedException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class Client {
    private final static String serverUrl = "http://ortc-developers.realtime.co/server/2.1/";
    private final static String ortcType = "IbtRealtimeSJ";
    private final static String applicationKey = "XEQyNG";
    private final static String privateKey = "m8vUKz6sRvzw";

    public String clientName;
    public OrtcClient ortcClient;
    public String channelName;

    private MainActivity mainActivity;

    private OnMessage onMessage = new OnMessage() {
        public void run(OrtcClient sender, String channel, String message) {
            Sending sending = new Sending(message);
//            toast(sender.getConnectionMetadata());
            if (ortcClient.getConnectionMetadata().equals(sending.senderId))
                return;
            if (sending.isRequest) {//message.charAt(0) == '>') {
//                toast("Received request from " + sending.senderName);
                if (ortcClient.getConnectionMetadata().equals(sending.receiverId)) {
                    int currentLineNum = mainActivity.drawView.lineNum;
                    mainActivity.drawView.lineNum = mainActivity.drawView.lastCommitLineNum;
                    Sending answer = new Sending(mainActivity);
                    answer.isAnswer = true;
                    answer.receiverId = sending.senderId;
//                    toast("Sending answer to " + answer.receiverId);
                    ortcClient.send(channelName, answer.toJsonObject().toString());
                    mainActivity.drawView.lineNum = currentLineNum;
                }
            }
            if (sending.isAnswer) {
                if (ortcClient.getConnectionMetadata().equals(sending.receiverId)) {
//                    toast("Received answer from " + sending.senderName);
                    updateDrawing(sending);
                }
            }
            if (!sending.isAnswer && !sending.isRequest) {
                sendNotification("Drawingame", mainActivity.getString(R.string.receivedDrawing) + " " + sending.senderName);
                updateDrawing(sending);
            }
        }
    };

    private OnConnected onConnected = new OnConnected() {
        @Override
        public void run(final OrtcClient sender) {
            //toast("Client " + clientName + " connected!");
            if (!ortcClient.isSubscribed(channelName)) {
                //   toast("was not subscribed!");
                ortcClient.subscribe(channelName, true, onMessage);
            } else {
//                toast("was subscribed!");
                getPresence();
            }
        }

    };

    private OnDisconnected onDisconnected = new OnDisconnected() {
        @Override
        public void run(OrtcClient arg0) {
            //sendNotification("Drawingame", "You was disconnected");
        }
    };

    private OnReconnected onReconnected = new OnReconnected() {
        public void run(final OrtcClient sender) {
            //toast("Client reconnected!");
            if (!ortcClient.isSubscribed(channelName)) {
//                toast("was not subscribed!");
                ortcClient.subscribe(channelName, true, onMessage);
            } else {
//                toast("was subscribed!");
                getPresence();
            }
        }
    };


    private OnSubscribed onSubscribed = new OnSubscribed() {
        @Override
        public void run(final OrtcClient sender, final String channel) {
            //toast("Subscribed to channel: " + channel);
            //toast("enabling presence!");
            try {
                ortcClient.enablePresence(privateKey, channelName, true,
                        new OnEnablePresence() {
                            @Override
                            public void run(final Exception exception, final String result) {
                                if (exception != null) {
                                    toast("Couldn't enable presence! - " + exception);
                                    Log.d("!", exception.toString());
                                } else {
                                    getPresence();
                                }
                            }
                        }
                );
            } catch (OrtcNotConnectedException e) {
                toast("OrtcNotConnectedException!");
                Log.d("!", e.toString());
            }
        }
    };

    private OnUnsubscribed onUnsubscribed = new OnUnsubscribed() {
        public void run(OrtcClient sender, final String channel) {
            //toast("Unsubscribed from channel: " + channel);
        }
    };

    public void disconnect() {
        ortcClient.disconnect();
    }

//    public void connect() {
//        ortcClient.connect(applicationKey, privateKey);
//    }

    public Client(MainActivity mainActivity, String channelName, String clientName) throws Exception {
        this.mainActivity = mainActivity;
        this.channelName = channelName;
        this.clientName = clientName;
        Ortc ortc = new Ortc();
        OrtcFactory factory = ortc.loadOrtcFactory(ortcType);
        ortcClient = factory.createClient();
        ortcClient.setApplicationContext(mainActivity.getApplicationContext());
        ortcClient.setClusterUrl(serverUrl);
        ortcClient.setConnectionMetadata(currentTime() + clientName);
        ortcClient.onConnected = onConnected;
        ortcClient.onDisconnected = onDisconnected;
        ortcClient.onReconnected = onReconnected;
        ortcClient.onSubscribed = onSubscribed;
        ortcClient.onUnsubscribed = onUnsubscribed;
        ortcClient.connect(applicationKey, privateKey);
    }

    private void updateDrawing(final Sending sending) {
//        toast("updating");
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.drawView.recalcFromSending(sending);//new Sending(json));
            }
        });
    }

    void sendNotification(String title, String text) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager nm = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        Context context = mainActivity.getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notification;
        if (Build.VERSION.SDK_INT < 11) {
            notification = new Notification(icon, title, when);
            notification.setLatestEventInfo(context, title, text, pending);
        } else {
            notification = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pending)
                    .setWhen(when)
                    .setAutoCancel(true)
                    .build();
        }
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        nm.notify(0, notification);
    }

    public void commitDrawing() {
        ortcClient.send(channelName, (new Sending(mainActivity)).toJsonObject().toString());
        mainActivity.drawView.lastCommitLineNum = mainActivity.drawView.lineNum;
    }

    private void getPresence() {
//        toast("getting presence!");
        try {
            ortcClient.presence(channelName, new OnPresence() {
                        @Override
                        public void run(final Exception exception, final Presence presenceData) {
                            if (exception != null) {
                                toast("Couldn't get presence! - " + exception.toString());
                                Log.d("!", exception.toString());
                            } else {
                                Iterator<String> iterator = presenceData.getMetadata().keySet().iterator();
                                String receiverId = null;
                                while (iterator.hasNext()) {
                                    receiverId = iterator.next();
                                    if (!receiverId.equals(ortcClient.getConnectionMetadata()))
                                        break;
                                }
                                if (receiverId != null) {
                                    Sending sending = new Sending(mainActivity);
                                    sending.isRequest = true;
                                    sending.receiverId = receiverId;
//                                    toast("Sending request to " + sending.receiverId);
                                    ortcClient.send(channelName, sending.toJsonObject().toString());
                                }
                            }
                        }
                    }
            );
        } catch (OrtcNotConnectedException e) {
            toast("OrtcNotConnectedException!");
            Log.d("!", e.toString());
            //this cannot be because it's connected
        }
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
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss:SS");
        Date now = new Date();
        return sdfTime.format(now);
    }

}