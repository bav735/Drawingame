package classes.example.drawingame;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import ibt.ortc.api.*;
import ibt.ortc.extensibility.*;
import ibt.ortc.extensibility.exception.OrtcNotConnectedException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Realizes interaction with server
 */

public class Client {
    private final static String serverUrl = "http://ortc-developers.realtime.co/server/2.1/";
    private final static String ortcType = "IbtRealtimeSJ";
    private final static String applicationKey = "XEQyNG";
    private final static String privateKey = "m8vUKz6sRvzw";

    public String clientName;
    public OrtcClient ortcClient;
    public String channelName;

    private DrawingActivity drawingActivity;

    private int usersOnline;
    private boolean isOnFirstConnection;

    private OnMessage onMessage = new OnMessage() {
        public void run(OrtcClient sender, String channel, String message) {
            DrawingSending receivedSending = new DrawingSending(message);
            if (ortcClient.getConnectionMetadata().equals(receivedSending.senderId))
                return;
            if (receivedSending.isRequest) {
                if (ortcClient.getConnectionMetadata().equals(receivedSending.receiverId)) {
                    DrawingSending answer = new DrawingSending(drawingActivity);
                    answer.lineList = drawingActivity.drawView.committedLineList;
                    answer.isAnswer = true;
                    answer.receiverId = receivedSending.senderId;
                    ortcClient.send(channelName, answer.toJsonObject().toString());
                    drawingActivity.toast("received request, sending answer to" + answer.receiverId);
                }
            }
            if (receivedSending.isAnswer) {
                if (ortcClient.getConnectionMetadata().equals(receivedSending.receiverId)) {
                    updateDrawingFromSending(receivedSending);
                    sendingReceiveTimer.cancel();
                }
            }
            if (!receivedSending.isAnswer && !receivedSending.isRequest) {
                sendNotification("Drawingame", drawingActivity.getString(R.string.receivedDrawing) + " " + receivedSending.senderName);
                updateDrawingFromSending(receivedSending);
            }
        }
    };

    private OnConnected onConnected = new OnConnected() {
        @Override
        public void run(final OrtcClient sender) {
            if (!ortcClient.isSubscribed(channelName)) {
                ortcClient.subscribe(channelName, true, onMessage);
            }
        }
    };

    private OnDisconnected onDisconnected = new OnDisconnected() {
        @Override
        public void run(OrtcClient arg0) {
        }
    };

    private OnReconnected onReconnected = new OnReconnected() {
        public void run(final OrtcClient sender) {
            if (!ortcClient.isSubscribed(channelName)) {
                ortcClient.subscribe(channelName, true, onMessage);
            }
        }
    };


    private OnSubscribed onSubscribed = new OnSubscribed() {
        @Override
        public void run(final OrtcClient sender, final String channel) {
            if (isOnFirstConnection)
                try {
                    ortcClient.enablePresence(privateKey, channelName, true,
                            new OnEnablePresence() {
                                @Override
                                public void run(final Exception exception, final String result) {
                                    if (exception != null) {
//                                        drawingActivity.toast(drawingActivity.getString(R.string.presenceGettingError));
                                    } else {
                                        getPresence();
                                    }
                                }
                            }
                    );
                } catch (OrtcNotConnectedException e) {
//                    drawingActivity.toast(drawingActivity.getString(R.string.presenceGettingError));
                }
        }
    };

    private OnUnsubscribed onUnsubscribed = new OnUnsubscribed() {
        public void run(OrtcClient sender, final String channel) {
        }
    };

    private OnDisablePresence onDisablePresence = new OnDisablePresence() {
        @Override
        public void run(Exception e, String s) {
        }
    };

    private CountDownTimer sendingReceiveTimer = new CountDownTimer(3000, 3001) {
        public void onTick(long l) {
        }

        public void onFinish() {

            //drawingActivity.toast(drawingActivity.getString(R.string.presenceGettingError));
        }
    };

    public void destroy() {
        try {
            ortcClient.disablePresence(privateKey, channelName, onDisablePresence);
            ortcClient.unsubscribe(channelName);
            ortcClient.disconnect();
        } catch (OrtcNotConnectedException e) {
            drawingActivity.toast("OrtcNotConnectedException");
        }
    }

    public Client(DrawingActivity drawingActivity, String channelName, String clientName) throws Exception {
        this.drawingActivity = drawingActivity;
        this.channelName = channelName;
        this.clientName = clientName;
        isOnFirstConnection = true;
        Ortc ortc = new Ortc();
        OrtcFactory factory = ortc.loadOrtcFactory(ortcType);
        ortcClient = factory.createClient();
        ortcClient.setApplicationContext(drawingActivity.getApplicationContext());
        ortcClient.setClusterUrl(serverUrl);
        ortcClient.setConnectionMetadata(currentTime() + clientName);
        ortcClient.onConnected = onConnected;
        ortcClient.onDisconnected = onDisconnected;
        ortcClient.onReconnected = onReconnected;
        ortcClient.onSubscribed = onSubscribed;
        ortcClient.onUnsubscribed = onUnsubscribed;
        ortcClient.connect(applicationKey, privateKey);
    }

    private void updateDrawingFromSending(final DrawingSending drawingSending) {
        drawingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawingActivity.drawView.recalcFromSending(drawingSending);
            }
        });
    }

    void sendNotification(String title, String text) {
        int icon = R.drawable.ic_launcher_complete;
        long when = System.currentTimeMillis();
        NotificationManager nm = (NotificationManager) drawingActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        Context context = drawingActivity.getApplicationContext();
        Intent intent = new Intent(context, DrawingActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notification;
        if (Build.VERSION.SDK_INT < 11) {
            notification = new Notification(icon, title, when);
            notification.setLatestEventInfo(context, title, text, pending);
        } else {
            notification = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(icon)
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
        ortcClient.send(channelName, drawingActivity.drawView.commit());
    }

    private void getPresence() {
        isOnFirstConnection = false;
        try {
            ortcClient.presence(channelName, new OnPresence() {
                        @Override
                        public void run(final Exception exception, final Presence presenceData) {
                            if (exception != null) {
//                                drawingActivity.toast(drawingActivity.getString(R.string.presenceGettingError));
                            } else {
                                usersOnline = (int) presenceData.getSubscriptions() + 1;
                                //drawingActivity.toast(drawingActivity.getString(R.string.usersOnline) + " " + usersOnline);
                                if (usersOnline > 1) {
                                    Iterator<String> iterator = presenceData.getMetadata().keySet().iterator();
                                    String receiverId = null;
                                    while (iterator.hasNext()) {
                                        receiverId = iterator.next();
                                        if (!receiverId.equals(ortcClient.getConnectionMetadata()))
                                            break;
                                    }
                                    if (!ortcClient.getIsConnected() || receiverId == null) {
//                                        drawingActivity.toast(drawingActivity.getString(R.string.presenceGettingError));
                                    }
                                    DrawingSending drawingSending = new DrawingSending(drawingActivity);
                                    drawingSending.isRequest = true;
                                    drawingSending.receiverId = receiverId;
                                    drawingSending.lineList = new ArrayList<Line>();
                                    ortcClient.send(channelName, drawingSending.toJsonObject().toString());
                                    sendingReceiveTimer.start();
                                }
                            }
                        }
                    }
            );
        } catch (OrtcNotConnectedException e) {
//            drawingActivity.toast(drawingActivity.getString(R.string.presenceGettingError));
        }
    }

    private String currentTime() {
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss:SS");
        Date now = new Date();
        return sdfTime.format(now);
    }

}