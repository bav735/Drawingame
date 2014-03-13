package com.example.drawingame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class DrawView extends View {

	public int lineNum;
	private int lastLineNum;
	public int drawingColor;
	private int backgroundColor;
	private float strokeWidth;
	public List<Line> lineList;
	private MainActivity mainActivity;
	private Paint paint = new Paint();
	public boolean isEnabled;
	public boolean isRandom;
	public boolean isContinuous;

	private float lastX;
	private float lastY;
	private float e;

	public DrawView(MainActivity mainActivity) {
		super((Context) mainActivity);
		this.mainActivity = mainActivity;
		paint.setStrokeWidth(strokeWidth);
		drawingColor = Color.BLACK;
		backgroundColor = Color.WHITE;
		strokeWidth = (float) 5;
		lineNum = 0;
		lastLineNum = 0;
		lineList = new ArrayList<Line>();
		lastX = -1;
		lastY = -1;
		e = (float) 0.1;
		isEnabled = true;
		isRandom = false;
		isContinuous = false;
	}

	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DrawView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onDraw(Canvas canvas) {
		drawCanvas(canvas);
	}

	private void drawCanvas(Canvas canvas) {
		canvas.drawColor(backgroundColor);
		if (lineList != null && !lineList.isEmpty())
			for (Line line : lineList) {
				paint.setColor(line.color);
				for (int i = 1; i < line.length; i++)
					canvas.drawLine(line.pointX.get(i - 1),
							line.pointY.get(i - 1), line.pointX.get(i),
							line.pointY.get(i), paint);
			}
	}

	public void save() {
		Bitmap toDisk = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
		// Bitmap toDisk = Bitmap.createBitmap(canvas.getWidth(),
		// canvas.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(toDisk);
		drawCanvas(canvas);
		try {
			toDisk.compress(Bitmap.CompressFormat.PNG, 100,
					new FileOutputStream(new File("/mnt/sdcard/Drawing.png")));
			toast("Drawing was saved");
		} catch (FileNotFoundException e) {
			toast("Error while writing to disk");
			e.printStackTrace();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// if (!isEnabled) return true;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Line newLine = new Line();
			if (isRandom)
				drawingColor = randomColor();
			newLine.color = drawingColor;
			newLine.addPoint(event.getX(), event.getY());
			lineList.add(newLine);
			lineNum++;
			break;
		case MotionEvent.ACTION_MOVE:
			lineList.get(lineNum - 1).addPoint(event.getX(), event.getY());
			invalidate();
			if (isContinuous && isNotClose(event.getX(), event.getY()))
				mainActivity.client.send();
			break;
		}
		return true;
	}

	public void clear() {
		lineNum = 0;
		lastLineNum = 0;
		lineList = new ArrayList<Line>();
	}

	public void undo() {
		if (lineNum > lastLineNum) {
			lineList.remove(lineNum - 1);
			lineNum--;
		}
		invalidate();
	}

	private int randomColor() {
		Random random = new Random();
		return Color.argb(255, random.nextInt(256), random.nextInt(256),
				random.nextInt(256));
	}

	public void recalc(String string) {
		Sending sending = new Sending(string);
		// if (sending.lineNum > 0) {
		lineList = sending.lineList;
		lineNum = sending.lineNum;
		lastLineNum = sending.lineNum;
		invalidate();
		// } else {
		// toast("Received empty drawing");
		// }
	}

	private void toast(String s) {
		Toast.makeText(mainActivity, s, 0).show();
	}

	private boolean isNotClose(float curX, float curY) {
		if (lastX == -1 && lastY == -1) {
			lastX = curX;
			lastY = curY;
		}
		if ((curX - lastX) * (curX - lastX) + (curY - lastY) * (curY - lastY) > e) {
			lastX = curX;
			lastY = curY;
			return true;
		} else
			return false;
	}
}