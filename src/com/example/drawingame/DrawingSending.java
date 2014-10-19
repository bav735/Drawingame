package com.example.drawingame;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Realizes covertion of
 * DrawingSending to Json object
 * Json object to DrawingSending
 * constructs from main activity
 * drawing
 **/

public class DrawingSending {
    public List<Line> lineList;
    public int lineNum;
    public int drawingWidth;
    public String senderId;
    public boolean isRequest;
    public boolean isAnswer;
    public String receiverId;
    public String senderName;

    public DrawingSending(DrawingActivity drawingActivity) {
        senderId = drawingActivity.client.ortcClient.getConnectionMetadata();
        senderName = drawingActivity.client.clientName;
        lineList = drawingActivity.drawView.lineList;
        lineNum = drawingActivity.drawView.lineNum;
        drawingWidth = drawingActivity.drawView.drawingWidth;
        isAnswer = false;
        isRequest = false;
        receiverId = "";
    }

    public DrawingSending(String string) {
        try {
            JSONObject jsonSending;
            jsonSending = new JSONObject(string);
            lineNum = jsonSending.getInt("lineNum");
            drawingWidth = jsonSending.getInt("drawingWidth");
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
            Log.d("!", "couldnt create DrawingSending" + e.toString());
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
            jsonSending.put("drawingWidth", drawingWidth);
        } catch (JSONException e) {
            Log.d("!", "couldnt create json of DrawingSending" + e.toString());
        }
        return jsonSending;
    }
}
