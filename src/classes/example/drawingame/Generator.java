package classes.example.drawingame;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Generates identificator based on current time and random number
 */
public class Generator {
    public static String id() {
        return time() + String.valueOf(new Random().nextInt());
    }
    public static String time() {
        SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy MMMM dd HH mm ss SSS");
        Date now = new Date();
        return sdfTime.format(now);
    }
}
