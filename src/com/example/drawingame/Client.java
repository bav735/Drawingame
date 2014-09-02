package com.example.drawingame;

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

public class Client {
    private MainActivity mainActivity;
    private String clientName;
    private OrtcClient ortcClient;
    private String channelName;

    private OnConnected onConnected = new OnConnected() {
        @Override
        public void run(final OrtcClient sender) {
            toast("Client connected!");
            ortcClient.subscribe(channelName, true,
                    new OnMessage() {
                        public void run(OrtcClient sender, String channel, String message) {
                            if (message.charAt(0) == '>') {
                                String receiver = message.substring(1);
                                toast("Request from " + sender.getConnectionMetadata());
                                if (clientName.equals(receiver)) {
                                    int lineNum = mainActivity.drawView.lineNum;
                                    mainActivity.drawView.lineNum = mainActivity.drawView.lastLineNum;
                                    send();
                                    mainActivity.drawView.lineNum = lineNum;
                                }
                            } else {
                                updateDrawing(message);//drawing sender updates own drawing twice: think about it
                                if (!sender.getConnectionMetadata().equals(clientName)) {
                                    toast("Received commit from " + sender.getConnectionMetadata());
                                }
                            }
                        }
                    }
            );

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
        }
    };


    private OnSubscribed onSubscribed = new OnSubscribed() {
        @Override
        public void run(final OrtcClient sender, final String channel) {
            toast("Subscribed to channel: " + channel);
            try {
                ortcClient.enablePresence(MainActivity.privateKey, channelName, true,
                        new OnEnablePresence() {
                            @Override
                            public void run(final Exception exception, final String result) {
                                if (exception != null) {
                                    toast("Couldn't enable presence! - " + exception);
                                } else {
                                    try {
                                        ortcClient.presence(MainActivity.channelName,
                                                new OnPresence() {
                                                    @Override
                                                    public void run(final Exception exception, final Presence presenceData) {
                                                        if (exception != null) {
                                                            toast("Couldn't get presence! - " + exception.toString());
                                                            Log.d("!", exception.toString());
                                                        } else {
                                                            toast(String.valueOf(presenceData.getSubscriptions())+" users are online!");
                                                            if (presenceData.getMetadata().isEmpty())
                                                                toast("Empty presence!");
                                                            else {
                                                                String receiver = presenceData.getMetadata().keySet().iterator().next();
                                                                toast("Sending request to " + receiver);
                                                                ortcClient.send(channelName, ">" + receiver);
                                                            }
                                                        }
                                                    }
                                                }
                                        );
                                    } catch (OrtcNotConnectedException e) {
                                        Log.d("!", e.toString());
                                        //this cannot be because it's connected
                                    }
                                }
                            }
                        }
                );
            } catch (OrtcNotConnectedException e) {
                Log.d("!", e.toString());
                //this cannot be because it's connected
            }


        }
    };

    private OnUnsubscribed onUnsubscribed = new OnUnsubscribed() {
        public void run(OrtcClient sender, final String channel) {
            toast("Unsubscribed from channel: " + channel);
        }
    };

    public Client(MainActivity mainActivity) throws Exception {
        this.mainActivity = mainActivity;
        clientName = android.os.Build.MODEL + "(at " + currentTime() + ")";
        channelName = MainActivity.channelName;
        Ortc ortc = new Ortc();
        OrtcFactory factory = ortc.loadOrtcFactory(MainActivity.ortcType);
        ortcClient = factory.createClient();
        ortcClient.setApplicationContext(mainActivity.getApplicationContext());
        ortcClient.setConnectionMetadata(clientName);
        ortcClient.setClusterUrl(MainActivity.serverUrl);
        ortcClient.onConnected = onConnected;
        ortcClient.onDisconnected = onDisconnected;
        ortcClient.onReconnected = onReconnected;
        ortcClient.onSubscribed = onSubscribed;
        ortcClient.onUnsubscribed = onUnsubscribed;
        ortcClient.connect(MainActivity.applicationKey, MainActivity.privateKey);
    }

    private void updateDrawing(final String json) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.drawView.recalcFromSending(new Sending(json));
            }
        });
        // toast("updated");
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

    public void disconnect() {
        ortcClient.disconnect();
    }
}

//Implementing Presence, max Metadata size is 256(!) that's why it's useless
//            // try {
//            // ortcClient.enablePresence(MainActivity.privateKey, MainActivity.channelName, true, new OnEnablePresence() {
//            Ortc.enablePresence(MainActivity.serverUrl,
//                    true,
//                    MainActivity.applicationKey,
//
//                    MainActivity.privateKey,
//                    MainActivity.channelName,
//                    true,
//                    new OnEnablePresence() {
//                        @Override
//                        public void run(final Exception exception, final String result) {
//                            if (exception != null) {
//                                toast("Couldn't enable presence! - " + exception);
//                            } else {
//                                toast(result);
//                                //   try {
//                                //ortcClient.presence(MainActivity.channelName,
//                                Ortc.presence(MainActivity.serverUrl,
//                                        true,
//                                        MainActivity.applicationKey,
//                                        MainActivity.privateKey,
//                                        MainActivity.channelName,
//                                        new OnPresence() {
//                                            @Override
//                                            public void run(final Exception exception, final Presence presenceData) {
//                                                if (exception != null) {
//                                                    toast("Couldn't get presence! - " + exception.toString());
//                                                    Log.d("!", exception.toString());
//                                                } else {
//                                                    toast(String.valueOf(presenceData.getSubscriptions()));
//                                                    if (presenceData.getMetadata().isEmpty())
//                                                        toast("empty");
//                                                    else {
//                                                        updateMetadata(presenceData.getMetadata().keySet().iterator().next());
//                                                    }
//                                                }
//                                            }
//                                        }
//                                );
////                                    } catch (OrtcNotConnectedException e) {
////                                        Log.d("!", e.toString());
////                                        //this cannot be because it's connected
////                                    }
//                            }
//                        }
//                    }
//            );
////            } catch (OrtcNotConnectedException e) {
////                Log.d("!", e.toString());
////                //this cannot be because it's connected
////            }
//                                    Iterator<?> metadataIterator = presenceData.getMetadata().entrySet().iterator();
//                                    while (metadataIterator.hasNext()) {
//                                        Map.Entry<String, Long> entry = (Map.Entry<String, Long>) metadataIterator
//                                                .next();
//                                        System.out.println(entry.getKey() + " - " + entry.getValue());
//                                    }

