package classes.example.drawingame.drawing_activity.draw_view;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.example.drawingame.drawing_activity.Line;

/**
 * Realizes covertion of
 * drawing to Json object
 * Json object to drawing
 */

public class Drawing {
   public ArrayList<Line> lineList = new ArrayList<Line>();

   public void fromJsonString(String s) {
      try {
         JSONArray jsonLinesArray = new JSONArray(s);
         lineList = new ArrayList<Line>();
         for (int i = 0; i < jsonLinesArray.length(); i++) {
            JSONObject jsonLine = jsonLinesArray.getJSONObject(i);
            Line line = new Line(jsonLine);
            lineList.add(line);
         }
      } catch (JSONException e) {
         Log.d("!", "couldnt create DrawingSending" + e.toString());
      }
   }

   public String toJsonString() {
      JSONArray jsonLinesArray = new JSONArray();
      for (Line line : lineList) {
         JSONObject jsonLine = line.toJsonObject();
         jsonLinesArray.put(jsonLine);
      }
      return jsonLinesArray.toString();
   }
}