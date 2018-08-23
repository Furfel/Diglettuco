package com.furfel.aruco.aruco;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import es.ava.aruco.Marker;

public class OverdrawView extends View {

    private Point points[] = new Point[9];
    private Paint paint = new Paint(), scorepaint= new Paint();
    private Bitmap diglet;
    private int digletPlace=-1;
    private Rect digletRect = new Rect(-100,-100,-50,-50);
    private Rect digletBmpRect;
    private int score=0;
    private ArrayList<Point> setupMarkers = new ArrayList<>();
    private Thread digletThread;

    public OverdrawView(Context context) {
        super(context);
        init();
    }

    public OverdrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        paint.setColor(Color.RED);
        scorepaint.setColor(Color.GREEN);
        scorepaint.setTextSize(64f);
        scorepaint.setStyle(Paint.Style.FILL);
        paint.setStyle(Paint.Style.FILL);
        for(int i=0;i<points.length;++i)
            points[i] = new Point(-1000,-1000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec)/4;
        diglet = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.diglet),width, width, true);
        digletBmpRect = new Rect(0,0, diglet.getWidth(), diglet.getHeight());
        Log.d("Overdraw","bitmapCreated "+diglet.getWidth()+"x"+diglet.getHeight());
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void placeMarkers(Mat frame, boolean rotated, Vector<Marker> markers) {
        float width = frame.cols();
        float height = frame.rows();
        Point p;
        for(Marker m: markers)
            if(m.getMarkerId()>=6 && m.getMarkerId()<=14) {
                p=points[m.getMarkerId()-6];
                if (p.x > 0) {
                    if (rotated)
                        p.set((int) ((float) p.x / height * getWidth()), (int) ((float) p.y / width * getHeight()));
                    else
                        p.set((int) ((float) p.x / width * getWidth()), (int) ((float) p.y / height * getHeight()));
                }
            }
    }

    public void setMarkers(Vector<Marker> markers) {
        if(markers!=null) {
            for(Marker m: markers) {
                if(m.getMarkerId()>=6 && m.getMarkerId()<=14) {
                    if(m.getCenter().x>0)
                        points[m.getMarkerId()-6].set((int)m.getCenter().x, (int)m.getCenter().y);
                }
            }
        }
    }

    private int emptyTicks=0;
    private int presentTicks=0;
    private int maxTicks = 0;
    private boolean doSwitch=true;

    private Runnable digletSwitcher = new Runnable() {
        @Override
        public void run() {
            while(doSwitch) {
                if (digletPlace != -1) {
                    presentTicks++;
                    if (presentTicks >= maxTicks) {
                        maxTicks = 8 + new Random().nextInt(10);
                        digletPlace = -1;
                    }
                } else {
                    emptyTicks++;
                    if (emptyTicks >= maxTicks) {
                        maxTicks = 20 + new Random().nextInt(20);
                        synchronized (setupMarkers) {
                            setupMarkers.clear();
                            for (Point p : points) {
                                if (p.x > 0) setupMarkers.add(p);
                            }
                            if (setupMarkers.size() > 5) {
                                digletPlace = new Random().nextInt(setupMarkers.size());
                                digletRect.set(setupMarkers.get(digletPlace).x-digletBmpRect.width()/2,setupMarkers.get(digletPlace).y-digletBmpRect.height()/2, setupMarkers.get(digletPlace).x+digletBmpRect.width()/2, setupMarkers.get(digletPlace).y+digletBmpRect.height()/2);
                            }
                        }
                    }
                }
                try { Thread.sleep(100);} catch (Exception e) {}
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(digletPlace!=-1 && digletRect.contains((int)event.getX(), (int)event.getY())) {
            digletPlace=-1;
            score++;
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        synchronized (setupMarkers) {
            for (int i = 0; i < setupMarkers.size(); ++i) {
                if (setupMarkers.get(i).x > 0) {
                    canvas.drawCircle(setupMarkers.get(i).x, setupMarkers.get(i).y, 10f, paint);
                    if (i == digletPlace)
                        canvas.drawBitmap(diglet, digletBmpRect, digletRect, null);
                }
            }
        }
        canvas.drawText("Markers set up: "+setupMarkers.size(), 32f,32f,paint);
        canvas.drawText("Score: "+score, 32f,64f, scorepaint);
    }

    public void onStart() {
        if(digletThread!=null && !digletThread.isAlive()) digletThread.start();
        else {doSwitch=true; digletThread = new Thread(digletSwitcher); digletThread.start();}
    }

    public void onDestroy() {
        doSwitch=false;
        try {
            digletThread.join();
        } catch (Exception e) {}
    }
}
