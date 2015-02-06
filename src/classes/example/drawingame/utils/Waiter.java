package classes.example.drawingame.utils;

import android.os.SystemClock;

/**
 * Created by A on 08.01.2015.
 */
public class Waiter implements Runnable {
    private static final int TIME_LIMIT_SEC = 1;
    private boolean isCancelled = false;
    public boolean timedOut = false;
    OnTimeOutListener listener;
    private long tStart;
    private long tEnd;

    public Waiter(OnTimeOutListener listener) {
        this.listener = listener;
        tStart = SystemClock.elapsedRealtime();
        tEnd = tStart;
    }

    @Override
    public void run() {
        while (tEnd - tStart < TIME_LIMIT_SEC * 1000 && !isCancelled)
            tEnd = SystemClock.elapsedRealtime();
        timedOut = true;
        if (!isCancelled)
            listener.onTimeOut();
    }

    public interface OnTimeOutListener {
        void onTimeOut();
    }

    public void cancel() {
        isCancelled = true;
    }
}
