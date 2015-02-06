package classes.example.drawingame.utils;

import android.app.Application;

/**
 * Created by A on 17.01.2015.
 */
public class MyApplication extends Application {

    private static boolean activityCreated = false;

    public static boolean isActivityCreated() {
        return activityCreated;
    }

    public static void activityResumed() {

    }

    public static void activityPaused() {

    }

    public static void activityCreated() {
        activityCreated = true;
    }
}
