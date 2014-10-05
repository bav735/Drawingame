package com.example.drawingame;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Sending {
    public List<Line> lineList;
    public int lineNum;
    public int sourceDisplayWidth;
    public int sourceDisplayHeight;
    public String senderId;
    public boolean isRequest;
    public boolean isAnswer;
    public String receiverId;
    public String senderName;

    public Sending(MainActivity mainActivity) {
        senderId = mainActivity.client.ortcClient.getConnectionMetadata();
        senderName = mainActivity.client.clientName;
        lineList = mainActivity.drawView.lineList;
        lineNum = mainActivity.drawView.lineNum;
        sourceDisplayHeight = mainActivity.drawView.displayHeight;
        sourceDisplayWidth = mainActivity.drawView.displayWidth;
        isAnswer = false;
        isRequest = false;
        receiverId = "";
    }

    public Sending(String string) {
        try {
            JSONObject jsonSending;
            jsonSending = new JSONObject(string);
            lineNum = jsonSending.getInt("lineNum");
            sourceDisplayHeight = jsonSending.getInt("sourceDisplayHeight");
            sourceDisplayWidth = jsonSending.getInt("sourceDisplayWidth");
            senderId = jsonSending.getString("senderId");
            senderName = jsonSending.getString("senderName");
            isRequest = jsonSending.getBoolean("isRequest");
            isAnswer = jsonSending.getBoolean("isAnswer");
            receiverId = jsonSending.getString("receiverId");
            JSONArray jsonLinesArray = jsonSending
                    .getJSONArray("jsonLinesArray");
            lineList = new ArrayList<Line>();
            for (int i = 0; i < lineNum; i++) {
                JSONObject jsonLine = jsonLinesArray.getJSONObject(i);
                Line line = new Line(jsonLine);
                lineList.add(line);
            }
        } catch (JSONException e) {
            Log.d("!", "couldnt create Sending" + e.toString());
        }

    }

    public JSONObject toJsonObject() {
        JSONArray jsonLinesArray = new JSONArray();
        for (int i = 0; i < lineNum; i++) {
            JSONObject jsonLine = lineList.get(i).toJsonObject();
            jsonLinesArray.put(jsonLine);
        }
        JSONObject jsonSending = new JSONObject();
        try {
            jsonSending.put("senderId", senderId);
            jsonSending.put("senderName", senderName);
            jsonSending.put("isRequest", isRequest);
            jsonSending.put("isAnswer", isAnswer);
            jsonSending.put("receiverId", receiverId);
            jsonSending.put("lineNum", lineNum);
            jsonSending.put("jsonLinesArray", jsonLinesArray);
            jsonSending.put("sourceDisplayHeight", sourceDisplayHeight);
            jsonSending.put("sourceDisplayWidth", sourceDisplayWidth);
        } catch (JSONException e) {
            Log.d("!", "couldnt create json of Sending" + e.toString());
        }
        return jsonSending;
    }
}
