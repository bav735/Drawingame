//package classes.example.drawingame.views;
//
//import android.content.Context;
//import android.graphics.*;
//import android.util.AttributeSet;
//import android.view.Display;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.RelativeLayout;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import classes.example.drawingame.activities.DrawingActivity;
//import classes.example.drawingame.DrawingSending;
//import classes.example.drawingame.Line;
//
///**
// * Realizes canvas erasing view
// */
//
//public class EraserView extends View {
//    public static int maxWidth;//= 100;
//    public static int minWidth;// = 5;
//    public static float idealDrawingRatio = (float) 14 / (float) 9;
//    public static String tmpDrawingName = "tmpDrawing";
//    public static String drawingName = "Drawing";
//
//    public int drawingWidth;
//    public int drawingHeight;
//
//    public int drawingColor;
//    public int backgroundColor;
//    public int strokeWidth;
//
//    public List<Line> committedLineList;
//    public List<Line> userLineList;
//    public DrawingActivity drawingActivity;
//    public Paint paint;
//
//    public boolean isRandomColor;
//    public boolean isRandomWidth;
//    public boolean isInWidthDialog;
//    public boolean isOnEraser;
//
//    private int lastColor;
//    private int lastWidth;
//    private boolean eraserJustEnded;
//
//    public void init(final DrawingActivity ma, boolean iiwd) {
//        drawingActivity = ma;
//        backgroundColor = Color.WHITE;
//        drawingColor = Color.BLACK;
//        committedLineList = new ArrayList<Line>();
//        userLineList = new ArrayList<Line>();
//        isRandomColor = false;
//        isRandomWidth = false;
//        isInWidthDialog = iiwd;
//        scaleDrawing();
//        maxWidth = drawingWidth / 4;
//        minWidth = 5;//drawingWidth / 80;
//        strokeWidth = minWidth;
//        isOnEraser = false;
//        eraserJustEnded = false;
//        paint = new Paint();
//        paint.setAntiAlias(true); // enable anti aliasing
//        paint.setDither(true); // enable dithering
//        paint.setStyle(Paint.Style.STROKE); // set to STOKE
//        paint.setStrokeJoin(Paint.Join.ROUND); // set the join to round you want
//        paint.setStrokeCap(Paint.Cap.ROUND);  // set the paint cap to round too
//    }
//
//    public EraserView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public EraserView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//    }
//
//    public EraserView(DrawingActivity drawingActivity) {
//        super((Context) drawingActivity);
//    }
//
//    public void initEraser() {
//        lastColor = drawingColor;
//        lastWidth = strokeWidth;
//        strokeWidth = minWidth;
//        isOnEraser = true;
//    }
//
//    public void endEraser() {
//        drawingColor = lastColor;
//        strokeWidth = lastWidth;
//        isOnEraser = false;
//        eraserJustEnded = true;
//    }
//
//    private void scaleDrawing() {
//        if (isInWidthDialog) {
//            WindowManager wm = (WindowManager) drawingActivity.getSystemService(Context.WINDOW_SERVICE);
//            Display display = wm.getDefaultDisplay();
//            drawingWidth = display.getWidth();
//            drawingHeight = display.getWidth();
//        } else {
//            drawingWidth = this.getRight();
//            drawingHeight = this.getBottom();
//            if (drawingWidth * idealDrawingRatio < drawingHeight) {
//                drawingHeight = (int) (drawingWidth * idealDrawingRatio);
//            } else {
//                drawingWidth = (int) (drawingHeight / idealDrawingRatio);
//            }
//        }
////        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(drawingWidth, drawingHeight);
////        layoutParams.gravity = Gravity.CENTER;
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(drawingWidth, drawingHeight);
//        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//        this.setLayoutParams(layoutParams);
//    }
//
//    @Override
//    public void onDraw(Canvas canvas) {
//        drawToCanvas(canvas);
//    }
//
//    private void drawToCanvas(Canvas canvas) {
//        canvas.drawColor(backgroundColor);
//        drawToCanvasFromLineList(canvas, committedLineList);
//        drawToCanvasFromLineList(canvas, userLineList);
//    }
//
//    private void drawToCanvasFromLineList(Canvas canvas, List<Line> list) {
//        if (list != null)
//            for (Line line : list) {
//                paint.setColor(line.color);
//                paint.setStrokeWidth(line.strokeWidth);
//                paint.setPathEffect(new CornerPathEffect(line.strokeWidth));
//                if (isInWidthDialog) {
//                    paint.setStrokeWidth(strokeWidth);
//                    paint.setPathEffect(new CornerPathEffect(strokeWidth));
//                }
//                Path path = new Path();
//                path.moveTo(line.pointX.get(0), line.pointY.get(0));
//                for (int i = 1; i < line.length; i++)
//                    path.lineTo(line.pointX.get(i), line.pointY.get(i));
//                canvas.drawPath(path, paint);
//            }
//    }
//
//    public void save(String drawingName) {
//        Bitmap bitmapToDisk = Bitmap.createBitmap(drawingWidth, drawingHeight, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmapToDisk);
//        drawToCanvas(canvas);
//        try {
//            bitmapToDisk.compress(Bitmap.CompressFormat.PNG, 100,
//                    new FileOutputStream(new File("/mnt/sdcard/" + drawingName + ".png")));
//        } catch (FileNotFoundException e) {
//            drawingActivity.toast("Error while writing to disk");
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                if (!isOnEraser) {
//                    if (!eraserJustEnded) {
//                        if (isRandomColor)
//                            drawingColor = randomColor();
//                        if (isRandomWidth)
//                            strokeWidth = randomWidth();
//                    } else
//                        eraserJustEnded = false;
//                } else
//                    drawingColor = Color.WHITE;
//                Line newLine = new Line();
//                newLine.strokeWidth = strokeWidth;
//                newLine.color = drawingColor;
//                newLine.addPoint(event.getX(), event.getY());
//                userLineList.add(newLine);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                userLineList.get(userLineList.size() - 1).addPoint(event.getX(), event.getY());
//                invalidate();
//                break;
//        }
//        return true;
//    }
//
//    public void undo() {
//        if (!userLineList.isEmpty())
//            userLineList.remove(userLineList.size() - 1);
//        invalidate();
//    }
//
//    private int randomColor() {
//        Random random = new Random();
//        return Color.argb(255, random.nextInt(256), random.nextInt(256),
//                random.nextInt(256));
//    }
//
//    private int randomWidth() {
//        Random random = new Random();
//        return random.nextInt(maxWidth + 1 - minWidth) + minWidth;
//    }
//
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
//
//    public String commit() {
//        for (Line line : userLineList) {
//            committedLineList.add(line);
//        }
//        DrawingSending drawingSending = new DrawingSending(drawingActivity);
//        drawingSending.lineList = userLineList;
//        userLineList = new ArrayList<Line>();
//        return drawingSending.toJsonObject().toString();
//    }
//
//    public void changeStrokeWidthFromDialog(int newSW) {
//        strokeWidth = newSW;
//        invalidate();
//    }
//}