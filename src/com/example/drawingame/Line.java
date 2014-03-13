package com.example.drawingame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Line {// implements Serializable {
	// private static final long serialVersionUID = 2L;
	public List<Float> pointX;
	public List<Float> pointY;
	public int color;
	public int length;

	public Line() {
		pointX = new ArrayList<Float>();
		pointY = new ArrayList<Float>();
		length = 0;
	}

	public Line(JSONObject jsonLine) {
		try {
			length = jsonLine.getInt("length");
			color = jsonLine.getInt("color");
			JSONArray jsonPointsArray = jsonLine
					.getJSONArray("jsonPointsArray");
			pointX = new ArrayList<Float>();
			pointY = new ArrayList<Float>();
			for (int i = 0; i < length; i++) {
				JSONObject jsonPoint = jsonPointsArray.getJSONObject(i);
				pointX.add((float) jsonPoint.getDouble("pointX"));
				pointY.add((float) jsonPoint.getDouble("pointY"));
			}
		} catch (JSONException e) {
			Log.d("!", "couldnt create Line");
			e.printStackTrace();
		}

	}

	public void addPoint(float x, float y) {
		pointX.add(x);
		pointY.add(y);
		length++;
	}

	public JSONObject toJsonObject() {
		JSONArray jsonPointsArray = new JSONArray();
		for (int i = 0; i < length; i++) {
			JSONObject jsonPoint = new JSONObject();
			try {
				jsonPoint.put("pointX", pointX.get(i));
				jsonPoint.put("pointY", pointY.get(i));
			} catch (JSONException e) {
				Log.d("!", "couldnt write to jsonPoint");
				e.printStackTrace();
			}
			jsonPointsArray.put(jsonPoint);
		}
		JSONObject jsonLine = new JSONObject();
		try {
			jsonLine.put("length", length);
			jsonLine.put("color", color);
			jsonLine.put("jsonPointsArray", jsonPointsArray);
		} catch (JSONException e) {
			Log.d("!", "couldnt write to jsonLine");
			e.printStackTrace();
		}

		return jsonLine;
	}

}
