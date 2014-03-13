package com.example.drawingame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Sending {// implements Serializable {
	// private static final long serialVersionUID = 1L;
	public String clientName;
	public List<Line> lineList;
	public int lineNum;
	public int lastLineNum;

	public Sending(String clientName, List<Line> lineList, int lineNum) {// int
		// lastLineNum)
		// {
		this.clientName = clientName;
		this.lineList = lineList;
		this.lineNum = lineNum;
		// this.lastLineNum = lastLineNum;
	}

	public Sending(String string) {
		try {
			JSONObject jsonSending;
			jsonSending = new JSONObject(string);
			clientName = jsonSending.getString("clientName");
			lineNum = jsonSending.getInt("lineNum");
			// lastLineNum = jsonSending.getInt("lastLineNum");
			JSONArray jsonLinesArray = jsonSending
					.getJSONArray("jsonLinesArray");
			lineList = new ArrayList<Line>();
			for (int i = 0; i < lineNum; i++) {
				JSONObject jsonLine = jsonLinesArray.getJSONObject(i);
				Line line = new Line(jsonLine);
				lineList.add(line);
			}

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
			// jsonSending.put("lastLineNum", lastLineNum);
			jsonSending.put("jsonLinesArray", jsonLinesArray);
		} catch (JSONException e) {
			Log.d("!", "couldnt write to jsonSending");
			e.printStackTrace();
		}
		return jsonSending;
	}

}
