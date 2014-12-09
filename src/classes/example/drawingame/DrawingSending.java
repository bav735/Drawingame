package classes.example.drawingame;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.example.drawingame.activities.DrawingActivity;

/**
 * Realizes covertion of
 * DrawingSending to Json object
 * Json object to DrawingSending
 * constructs from main activity
 * drawing
 */

public class DrawingSending {
    public List<Line> lineList;

    public int drawingWidth;
    private static String drawingWidthName = "drawingWidth";
    public String senderId;
    private static String senderIdName = "senderId";
    public boolean isRequest;
    private static String isRequestName = "isRequest";
    public boolean isAnswer;
    private static String isAnswerName = "isAnswer";
    public String receiverId;
    private static String receiverIdName = "receiverId";
    public String senderName;
    private static String senderNameName = "senderName";

    private JSONArray jsonLinesArray;
    private static String jsonLinesArrayName = "jsonLinesArray";
    private int lineNum;
    private static String lineNumName = "lineNum";

    public DrawingSending(DrawingActivity drawingActivity) {
        senderId = drawingActivity.client.ortcClient.getConnectionMetadata();
        senderName = drawingActivity.client.clientName;
        drawingWidth = drawingActivity.drawView.drawingWidth;
        isAnswer = false;
        isRequest = false;
        receiverId = "";
    }

    public DrawingSending(String string) {
        try {
            JSONObject jsonSending;
            jsonSending = new JSONObject(string);
            lineNum = jsonSending.getInt(lineNumName);
            drawingWidth = jsonSending.getInt(drawingWidthName);
            senderId = jsonSending.getString(senderIdName);
            senderName = jsonSending.getString(senderNameName);
            isRequest = jsonSending.getBoolean(isRequestName);
            isAnswer = jsonSending.getBoolean(isAnswerName);
            receiverId = jsonSending.getString(receiverIdName);
            JSONArray jsonLinesArray = jsonSending.getJSONArray(jsonLinesArrayName);
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
        jsonLinesArray = new JSONArray();
        for (Line line : lineList) {
            JSONObject jsonLine = line.toJsonObject();
            jsonLinesArray.put(jsonLine);
        }
        JSONObject jsonSending = new JSONObject();
        try {
            jsonSending.put(senderIdName, senderId);
            jsonSending.put(senderNameName, senderName);
            jsonSending.put(isRequestName, isRequest);
            jsonSending.put(isAnswerName, isAnswer);
            jsonSending.put(receiverIdName, receiverId);
            jsonSending.put(lineNumName, lineList.size());
            jsonSending.put(jsonLinesArrayName, jsonLinesArray);
            jsonSending.put(drawingWidthName, drawingWidth);
        } catch (JSONException e) {
            Log.d("!", "couldnt create json of DrawingSending" + e.toString());
        }
        return jsonSending;
    }
}
