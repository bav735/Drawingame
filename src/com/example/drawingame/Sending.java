package com.example.drawingame;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Sending {
//    public String clientName;
//    public String clientId;
    public List<Line> lineList;
    public int lineNum;
    public int sourceDisplayWidth;
    public int sourceDisplayHeight;

    public Sending(MainActivity mainActivity) {
//        this.clientName = mainActivity.client.clientName;
//        this.clientId = mainActivity.client.clientId;
        this.lineList = mainActivity.drawView.lineList;
        this.lineNum = mainActivity.drawView.lineNum;
        this.sourceDisplayHeight = mainActivity.drawView.displayHeight;
        this.sourceDisplayWidth = mainActivity.drawView.displayWidth;
    }

    public Sending(String string) {
        try {
            JSONObject jsonSending;
            jsonSending = new JSONObject(string);
//            clientName = jsonSending.getString("clientName");
//            clientId = jsonSending.getString("clientId");
            lineNum = jsonSending.getInt("lineNum");
            sourceDisplayHeight = jsonSending.getInt("sourceDisplayHeight");
            sourceDisplayWidth = jsonSending.getInt("sourceDisplayWidth");
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
//            jsonSending.put("clientName", clientName);
//            jsonSending.put("clientId", clientId);
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
