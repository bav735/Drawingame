package com.example.drawingame;

import android.widget.Toast;
import ibt.ortc.api.OnEnablePresence;
import ibt.ortc.api.OnPresence;
import ibt.ortc.api.Ortc;
import ibt.ortc.api.Presence;
import ibt.ortc.extensibility.*;
import ibt.ortc.extensibility.exception.OrtcNotConnectedException;

import java.util.Iterator;
import java.util.Map;

public class Client {
    private MainActivity mainActivity;
    private String clientName;
    private OrtcClient ortcClient;

    private OnConnected onConnected = new OnConnected() {
        @Override
        public void run(final OrtcClient sender) {
            toast("Client connected!");
            try {
                enablePresence();
                updateDrawView();
            } catch (OrtcNotConnectedException e) {
                //this cannot be because it's connected
            }

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
                                                mainActivity.drawView.recalcFromSending(sending);
                                            }
                                        });
                                    }
                                }
                            }
                    );
                }
            });
        }
    };

    private OnDisconnected onDisconnected = new OnDisconnected() {
        @Override
        public void run(OrtcClient arg0) {
            toast("Disconnected!");
        }
    };

    private OnReconnected onReconnected = new OnReconnected() {
        public void run(final OrtcClient sender) {
            toast("Reconnected!");
            try {
                updateDrawView();
            } catch (OrtcNotConnectedException e) {
                //this cannot be because it's connected
            }
        }
    };

    private OnSubscribed onSubscribed = new OnSubscribed() {
        @Override
        public void run(final OrtcClient sender, final String channel) {
            toast("Subscribed to channel: " + channel);
        }
    };

    private OnUnsubscribed onUnsubscribed = new OnUnsubscribed() {
        public void run(OrtcClient sender, final String channel) {
            toast("Unsubscribed from channel: " + channel);
        }
    };

    public Client(MainActivity mainActivity) throws Exception {
        this.mainActivity = mainActivity;
        clientName = android.os.Build.MODEL;
        Ortc ortc = new Ortc();
        OrtcFactory factory = ortc.loadOrtcFactory(MainActivity.ortcType);
        ortcClient = factory.createClient();
        ortcClient.setApplicationContext(mainActivity.getApplicationContext());
        ortcClient.setClusterUrl(MainActivity.serverUrl);
        ortcClient.setConnectionMetadata(clientName);//delete then
        ortcClient.onConnected = onConnected;
        ortcClient.onDisconnected = onDisconnected;
        ortcClient.onReconnected = onReconnected;
        ortcClient.onSubscribed = onSubscribed;
        ortcClient.connect(MainActivity.applicationKey, MainActivity.authenticationToken);
    }

    private void updateDrawView() throws OrtcNotConnectedException {
        ortcClient.presence(MainActivity.channelName,
                new OnPresence() {
                    @Override
                    public void run(final Exception exception, final Presence presenceData) {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (exception != null) {
                                    toast("Couldn't get presence! - " + exception);
                                } else {
                                    Iterator<?> metadataIterator = presenceData.getMetadata().entrySet().iterator();
                                    String maxMetaData = ortcClient.getConnectionMetadata();
                                    toast("Subscribers:");//delete then
                                    while (metadataIterator.hasNext()) {
                                        @SuppressWarnings("unchecked")
                                        Map.Entry<String, Long> entry = (Map.Entry<String, Long>) metadataIterator.next();
                                        toast(entry.getKey());//delete then
                                        if (maxMetaData.length() < entry.getKey().length())
                                            maxMetaData = entry.getKey();
                                    }
                                    //  mainActivity.drawView.recalcFromSending(new Sending(maxMetaData));
                                }
                            }
                        });
                    }
                }
        );
    }

    private void enablePresence() throws OrtcNotConnectedException {
        ortcClient.enablePresence(
                MainActivity.authenticationToken,
                MainActivity.channelName,
                true,
                new OnEnablePresence() {
                    @Override
                    public void run(final Exception exception, final String result) {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (exception != null) {
                                    toast("Couldn't enable presence! - " + exception);
                                }
                            }
                        });
                    }
                }
        );
    }

    public void send() {
        Sending sending = new Sending(clientName, mainActivity.drawView);
        ortcClient.send(MainActivity.channelName, sending.toJsonObject().toString());
        //ortcClient.setConnectionMetadata(sending.toJsonObject().toString());
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

    /*private String currentTime() {
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();
        String strTime = sdfTime.format(now);
        return strTime;
    }*/
}