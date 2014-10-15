package com.example.drawingame;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DrawView extends View {
    public static int maxWidth;//= 100;
    public static int minWidth;// = 5;
    public static float idealDrawingRatio = (float) 14 / (float) 9;
    public static String tmpDrawingName = "tmpDraw";

    public int drawingWidth;
    public int drawingHeight;

    public int lineNum;
    public int lastCommitLineNum;
    public int drawingColor;
    public int backgroundColor;
    public int strokeWidth;

    public List<Line> lineList;
    public MainActivity mainActivity;
    public Paint paint;

    public boolean isRandomColor;
    public boolean isRandomWidth;
    public boolean isInWidthDialog;
    public boolean isOnEraser;

    private int lastColor;
    private int lastWidth;
    private boolean eraserJustEnded;

    public void init(MainActivity ma, boolean iiwd) {
        mainActivity = ma;
        backgroundColor = Color.WHITE;
        drawingColor = Color.BLACK;
        lineNum = 0;
        lastCommitLineNum = 0;
        lineList = new ArrayList<Line>();
        isRandomColor = false;
        isRandomWidth = false;
        isInWidthDialog = iiwd;
        scaleDrawing();
        maxWidth = drawingWidth / 4;
        minWidth = drawingWidth / 80;
        strokeWidth = minWidth;
        isOnEraser = false;
        eraserJustEnded = false;
        paint = new Paint();
        paint.setAntiAlias(true); // enable anti aliasing
        paint.setDither(true); // enable dithering
        paint.setStyle(Paint.Style.STROKE); // set to STOKE
        paint.setStrokeJoin(Paint.Join.ROUND); // set the join to round you want
        paint.setStrokeCap(Paint.Cap.ROUND);  // set the paint cap to round too
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DrawView(MainActivity mainActivity) {
        super((Context) mainActivity);
    }

    public void initEraser() {
        lastColor = drawingColor;
        lastWidth = strokeWidth;
        strokeWidth = minWidth;
        isOnEraser = true;
    }

    public void endEraser() {
        drawingColor = lastColor;
        strokeWidth = lastWidth;
        isOnEraser = false;
        eraserJustEnded = true;
    }

    private void scaleDrawing() {
        if (isInWidthDialog) {
            WindowManager wm = (WindowManager) mainActivity.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            drawingWidth = display.getWidth();
            drawingHeight = display.getWidth();
        } else {
            drawingWidth = this.getRight();
            drawingHeight = this.getBottom();
            if (drawingWidth * idealDrawingRatio < drawingHeight) {
                drawingHeight = (int) (drawingWidth * idealDrawingRatio);
            } else {
                drawingWidth = (int) (drawingHeight / idealDrawingRatio);
            }
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(drawingWidth, drawingHeight);
        layoutParams.gravity = Gravity.CENTER;
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(drawingWidth, drawingHeight);
//        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.setLayoutParams(layoutParams);
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
                paint.setStrokeWidth(line.strokeWidth);
                paint.setPathEffect(new CornerPathEffect(line.strokeWidth));

                if (isInWidthDialog) {
                    paint.setStrokeWidth(strokeWidth);
                    paint.setPathEffect(new CornerPathEffect(strokeWidth));
                }

                Path path = new Path();
                path.moveTo(line.pointX.get(0), line.pointY.get(0));
                for (int i = 1; i < line.length; i++)
                    path.lineTo(line.pointX.get(i), line.pointY.get(i));
                canvas.drawPath(path, paint);
            }

    }

    public void save(String drawingName) {
        Bitmap toDisk = Bitmap.createBitmap(drawingWidth, drawingHeight, Bitmap.Config.ARGB_8888);
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
                if (!isOnEraser) {
                    if (!eraserJustEnded) {
                        if (isRandomColor)
                            drawingColor = randomColor();
                        if (isRandomWidth)
                            strokeWidth = randomWidth();
                    } else
                        eraserJustEnded = false;
                } else
                    drawingColor = Color.WHITE;
                Line newLine = new Line();
                newLine.strokeWidth = strokeWidth;
                newLine.color = drawingColor;
                newLine.addPoint(event.getX(), event.getY());
                lineList.add(newLine);
                lineNum++;
                break;
            case MotionEvent.ACTION_MOVE:
                lineList.get(lineNum - 1).addPoint(event.getX(), event.getY());
                invalidate();
                break;
        }
        return true;
    }

//    public void clear() {
//        lineNum = 0;
//        lastCommitLineNum = 0;
//        lineList = new ArrayList<Line>();
//        invalidate();
//    }

    public void undo() {
        if (lineNum > lastCommitLineNum) {
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

        float k = (float) drawingWidth / (float) sending.drawingWidth;
        toast("k = " + String.valueOf(k));
        for (int i = 0; i < sending.lineNum; i++) {
            for (int j = 0; j < sending.lineList.get(i).length; j++) {
                float newX = sending.lineList.get(i).pointX.get(j).floatValue() * k;
                sending.lineList.get(i).pointX.set(j, newX);
                float newY = sending.lineList.get(i).pointY.get(j).floatValue() * k;
                sending.lineList.get(i).pointY.set(j, newY);
            }
            sending.lineList.get(i).strokeWidth *= k;
        }

        for (int i = lastCommitLineNum; i < lineNum; i++) {
            sending.lineList.add(lineList.get(i));
        }
        lineList = sending.lineList;
        lineNum = sending.lineNum + lineNum - lastCommitLineNum;
        lastCommitLineNum = sending.lineNum;
        invalidate();
    }

    private void toast(String s) {
        Toast.makeText(mainActivity, s, 0).show();
    }

    public void changeStrokeWidthFromDialog(int newSW) {
        strokeWidth = newSW;
        invalidate();
    }
}