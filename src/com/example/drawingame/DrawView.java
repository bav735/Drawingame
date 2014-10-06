package com.example.drawingame;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DrawView extends View {
    public static int maxWidth = 100;
    public static int minWidth = 5;
    public static float e = (float) 0.1;

    public int lineNum;
    public int lastLineNum;
    public int drawingColor;
    public int backgroundColor;
    public int strokeWidth;
    public int displayWidth;
    public int displayHeight;

    public List<Line> lineList;
    public MainActivity mainActivity;
    public Paint paint;
//    public Path path;

    public boolean isEnabled;
    public boolean isRandomColor;
    public boolean isRandomWidth;
    //public boolean isContinuous;
    public boolean isInWidthDialog;
    public boolean isOnEraser;

    public float lastX;
    public float lastY;

    public DrawView(MainActivity mainActivity) {
        super((Context) mainActivity);
    }

    public void init(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        drawingColor = Color.BLACK;
        backgroundColor = Color.WHITE;
        strokeWidth = minWidth;
        lineNum = 0;
        lastLineNum = 0;
        lineList = new ArrayList<Line>();
        lastX = -1;
        lastY = -1;
        isEnabled = true;
        isRandomColor = false;
        //isContinuous = false;
        isInWidthDialog = false;
        isOnEraser = false;
        Display display = mainActivity.getWindowManager().getDefaultDisplay();
        displayHeight = display.getHeight();
        displayWidth = display.getWidth();
        //Point size = new Point();
        //display.getSize(size);
        //displayWidth = size.x;
        //displayHeight = size.y;
        paint = new Paint();
        paint.setAntiAlias(true); // enable anti aliasing
        paint.setDither(true); // enable dithering
        paint.setStyle(Paint.Style.STROKE); // set to STOKE
        paint.setStrokeJoin(Paint.Join.ROUND); // set the join to round you want
        paint.setStrokeCap(Paint.Cap.ROUND);  // set the paint cap to round too
//        path = new Path();
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
//        path.reset();
        if (lineList != null && !lineList.isEmpty())
            for (Line line : lineList) {
                paint.setColor(line.color);
                paint.setStrokeWidth(line.strokeWidth);
                paint.setPathEffect(new CornerPathEffect(line.strokeWidth));

                if (isInWidthDialog) {
                    paint.setStrokeWidth(strokeWidth);
                    paint.setPathEffect(new CornerPathEffect(strokeWidth));
                }

                Path path = new Path();
                path.moveTo(line.pointX.get(0), line.pointY.get(0));
                for (int i = 1; i < line.length; i++)
                    //canvas.drawLine(line.pointX.get(i - 1),                            line.pointY.get(i - 1), line.pointX.get(i),                            line.pointY.get(i), paint);
                    path.lineTo(line.pointX.get(i), line.pointY.get(i));
                canvas.drawPath(path, paint);
            }

    }

    public void save(String drawingName) {
        Bitmap toDisk = Bitmap.createBitmap(displayWidth, displayHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(toDisk);
        drawCanvas(canvas);
        try {
            toDisk.compress(Bitmap.CompressFormat.PNG, 100,
                    new FileOutputStream(new File("/mnt/sdcard/" + drawingName + ".png")));
        } catch (FileNotFoundException e) {
            toast("Error while writing to disk");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Line newLine = new Line();
                newLine.strokeWidth = strokeWidth;
                if (isRandomColor)
                    drawingColor = randomColor();
                if (isOnEraser)
                    drawingColor = Color.WHITE;
                if (isRandomWidth)
                    strokeWidth = randomWidth();
                newLine.color = drawingColor;
                newLine.addPoint(event.getX(), event.getY());
                lineList.add(newLine);
                lineNum++;
                break;
            case MotionEvent.ACTION_MOVE:
                lineList.get(lineNum - 1).addPoint(event.getX(), event.getY());
                invalidate();
//                if (isContinuous)// && isNotClose(event.getX(), event.getY()))
//                    mainActivity.client.send();
                break;
        }
        return true;
    }

//    public void clear() {
//        lineNum = 0;
//        lastLineNum = 0;
//        lineList = new ArrayList<Line>();
//        invalidate();
//    }

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

    private int randomWidth() {
        Random random = new Random();
        return random.nextInt(maxWidth + 1 - minWidth) + minWidth;
    }

    public void recalcFromSending(Sending sending) {
        if (sending.lineNum == 0) {
            //we don't update our drawing if empty drawing was committed
            return;
        }
        for (int i = 0; i < sending.lineNum; i++) {
            for (int j = 0; j < sending.lineList.get(i).length; j++) {
                float px = sending.lineList.get(i).pointX.get(j).floatValue();
                sending.lineList.get(i).pointX.set(j, px * (float) displayWidth / (float) sending.sourceDisplayWidth);
                float py = sending.lineList.get(i).pointY.get(j).floatValue();
                sending.lineList.get(i).pointY.set(j, py * (float) displayHeight / (float) sending.sourceDisplayHeight);
            }
        }
        for (int i = lastLineNum; i < lineNum; i++) {
            sending.lineList.add(lineList.get(i));
        }
        lineList = sending.lineList;
        lineNum = sending.lineNum + lineNum - lastLineNum;
        lastLineNum = sending.lineNum;
        invalidate();
    }

    private void toast(String s) {
        Toast.makeText(mainActivity, s, 0).show();
    }

    //    private boolean isNotClose(float curX, float curY) {
//        if (lastX == -1 && lastY == -1) {
//            lastX = curX;
//            lastY = curY;
//        }
//        if ((curX - lastX) * (curX - lastX) + (curY - lastY) * (curY - lastY) > e) {
//            lastX = curX;
//            lastY = curY;
//            return true;
//        } else
//            return false;
//    }
//
    public void changeStrokeWidthFromDialog(int newSW) {
        strokeWidth = newSW;
        isInWidthDialog = true;
        invalidate();
    }
}