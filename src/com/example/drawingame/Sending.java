package com.example.drawingame;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Sending {// implements Serializable {
    // private static final long serialVersionUID = 1L;
    public String clientName;
    public List<Line> lineList;
    public int lineNum;
    public int lastLineNum;
    public float strokeWidth;
    public int sourceDisplayWidth;
    public int sourceDisplayHeight;

    public Sending(String clientName, DrawView drawView) {
        this.clientName = clientName;
        this.lineList = drawView.lineList;
        this.lineNum = drawView.lineNum;
        this.sourceDisplayHeight = drawView.displayHeight;
        this.sourceDisplayWidth = drawView.displayWidth;
    }

    public Sending(String string) {
        try {
            JSONObject jsonSending;
            jsonSending = new JSONObject(string);
            clientName = jsonSending.getString("clientName");
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
            Log.d("!", "Sending created =)");
        } catch (JSONException e) {
            Log.d("!", "couldnt create Sending");
            e.printStackTrace();
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
            jsonSending.put("clientName", clientName);
            jsonSending.put("lineNum", lineNum);
            jsonSending.put("jsonLinesArray", jsonLinesArray);
            jsonSending.put("sourceDisplayHeight", sourceDisplayHeight);
            jsonSending.put("sourceDisplayWidth", sourceDisplayWidth);
            Log.d("!", "Json of Sending created =)");
        } catch (JSONException e) {
            Log.d("!", "couldnt create json of Sending");
            e.printStackTrace();
        }
        return jsonSending;
    }

}
