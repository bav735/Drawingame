package classes.example.drawingame.drawing_activity.draw_view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import classes.example.drawingame.drawing_activity.Line;
import classes.example.drawingame.utils.Utils;

/**
 * Realizes canvas drawing view (private)
 */

public class DrawView extends View {
    public static int maxWidth;//= 100;
    public static int minWidth;// = 5;
    public static String tmpDrawingName = "tmpDrawing";
    public static String drawingName = "Drawing";

//    public int drawingWidth;
//    public int drawingHeight;

    public int drawingColor;
    public int backgroundColor;
    public int strokeWidth;

    public ArrayList<Line> lineList;
    public Activity drawingActivity;
    public Paint paint;
    public Bitmap drawingBitmap;

    public boolean isRandomColor;
    public boolean isRandomWidth;
    public boolean isInWidthDialog;
    public boolean isOnEraser;

//    public boolean isDrawingEnabled =true;

    private int lastColor;
    private int lastWidth;
    private boolean eraserJustEnded;

    private Rect rect;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DrawView(Activity drawingActivity) {
        super((Context) drawingActivity);
    }

    public void init(Activity ma, boolean iiwd, final Bitmap bitmap) {
        drawingActivity = ma;
        drawingBitmap = bitmap;
//        drawingWidth = drawingBitmap.getWidth();
//        drawingHeight = drawingBitmap.getHeight();
        backgroundColor = Color.WHITE;
        drawingColor = Color.BLACK;
//        committedLineList = new ArrayList<Line>();
        lineList = new ArrayList<Line>();
        isRandomColor = false;
        isRandomWidth = false;
        isInWidthDialog = iiwd;
        maxWidth = 100;//drawingWidth / 4;
        minWidth = 5;//drawingWidth / 80;
        strokeWidth = minWidth;
        isOnEraser = false;
        eraserJustEnded = false;
        paint = new Paint();
        paint.setAntiAlias(true); // enable anti aliasing
        paint.setFilterBitmap(true);
        paint.setDither(true); // enable dithering
        paint.setStyle(Paint.Style.STROKE); // set to STOKE
        paint.setStrokeJoin(Paint.Join.ROUND); // set the join to round you want
        paint.setStrokeCap(Paint.Cap.ROUND);  // set the paint cap to round too
        if (!isInWidthDialog) {
//            LinearLayout.LayoutParams params = Utils.scaleToLl(itemBitmap.getWidth(), itemBitmap.getHeight(),
//                    metrics.widthPixels, metrics.heightPixels - Utils.dpToPx(100));
//            params.gravity = Gravity.CENTER;
//            setLayoutParams(params);
//            requestLayout();
            final DrawView dv = this;
            dv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    dv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    int width = dv.getMeasuredWidth();
                    int height = dv.getMeasuredHeight();
//                   RelativeLayout.LayoutParams params = Utils.scaleToRl(itemBitmap.getWidth(), itemBitmap.getHeight(), width, height);
//                   params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    LinearLayout.LayoutParams params = Utils.scaleToLl(bitmap.getWidth(), bitmap.getHeight(), width, height);
                    params.gravity = Gravity.CENTER;
                    setLayoutParams(params);
                    requestLayout();
                    rect = new Rect(0, 0, params.width, params.height);
                }
            });
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Utils.displayWidth,
                    Utils.displayHeight / 3);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            setLayoutParams(params);
            requestLayout();
            rect = new Rect(0, 0, params.width, params.height);
        }
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

//    private void scaleDrawing() {
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        int displayWidth = metrics.widthPixels;
//        int displayHeight = metrics.heightPixels;
//        if (drawingBitmap == null) {
//            drawingWidth = 9;
//            drawingHeight = 14;
//        } else {
//            drawingWidth = drawingBitmap.getWidth();
//            drawingHeight = drawingBitmap.getHeight();
//        }
//        float k;
//        if ((float) drawingHeight * displayWidth / drawingWidth > displayHeight)
//            k = (float) displayHeight / drawingHeight;
//        else
//            k = (float) displayWidth / drawingWidth;
//        drawingHeight *= k;
//        drawingWidth *= k;
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(drawingWidth, drawingHeight);
//        layoutParams.gravity = Gravity.CENTER;
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(drawingWidth, drawingHeight);
//        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//        this.setLayoutParams(Utils.scale(drawingWidth, displayHeight, displayWidth, displayHeight));
//        rect = new Rect(0, 0, drawingWidth, drawingHeight);
//    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        drawingWidth = MeasureSpec.getSize(widthMeasureSpec);
//        drawingHeight = MeasureSpec.getSize(heightMeasureSpec);
//        this.setMeasuredDimension(drawingWidth, drawingHeight);
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    }

    @Override
    public void onDraw(Canvas canvas) {
//        if (isInWidthDialog) {
//            canvas.drawColor(backgroundColor);
//        } else {
        canvas.drawBitmap(drawingBitmap, null, rect, paint);
//        }
//        drawToCanvasFromLineList(canvas, committedLineList);
        drawToCanvasFromLineList(canvas, lineList);
    }

    private void drawToCanvasFromLineList(Canvas canvas, ArrayList<Line> list) {
        if (list != null)
            for (Line line : list) {
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

    public Bitmap getBitmap() {
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(this.getDrawingCache());
        this.setDrawingCacheEnabled(false);
        return bitmap;
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
                break;
            case MotionEvent.ACTION_MOVE:
                lineList.get(lineList.size() - 1).addPoint(event.getX(), event.getY());
                invalidate();
                break;
        }
        return true;
    }

    public void undo() {
        if (!lineList.isEmpty())
            lineList.remove(lineList.size() - 1);
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

//    public void recalcFromSending(DrawingSending drawingSending) {
//        if (drawingSending.lineList.isEmpty()) {
//            return;
//        }
//        float k = (float) drawingWidth / (float) drawingSending.drawingWidth;
//        for (Line line : drawingSending.lineList) {
//            for (int i = 0; i < line.length; i++) {
//                line.pointX.set(i, line.pointX.get(i).floatValue() * k);
//                line.pointY.set(i, line.pointY.get(i).floatValue() * k);
//            }
//            line.strokeWidth *= k;
//        }
//        for (Line line : drawingSending.lineList) {
//            committedLineList.add(line);
//        }
//        invalidate();
//    }

//    public String commit() {
//        for (Line line : lineList) {
//            committedLineList.add(line);
//        }
//        DrawingSending drawingSending = new DrawingSending(drawingActivity);
//        drawingSending.lineList = lineList;
//        lineList = new ArrayList<Line>();
//        return drawingSending.toJsonObject().toString();
//    }

    public void changeStrokeWidthFromDialog(int newSW) {
        strokeWidth = newSW;
        invalidate();
    }
}