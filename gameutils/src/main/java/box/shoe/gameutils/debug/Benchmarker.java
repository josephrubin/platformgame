package box.shoe.gameutils.debug;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Joseph on 2/22/2018.
 */

public class Benchmarker
{
    private static long currentThreadTimeMS;
    private static long currentOverallTimeMS;

    public static void start()
    {
        currentThreadTimeMS = SystemClock.currentThreadTimeMillis();
        currentOverallTimeMS = SystemClock.uptimeMillis();
    }

    public static void resultThread()
    {
        Log.d("BENCHMARKER", String.valueOf(SystemClock.currentThreadTimeMillis() - currentThreadTimeMS));
    }

    public static void resultOverall()
    {
        Log.d("BENCHMARKER", String.valueOf(SystemClock.uptimeMillis() - currentOverallTimeMS));
    }
}
