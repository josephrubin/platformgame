package box.shoe.gameutils;

import android.util.Log;
import java.util.HashMap;

/**
 * Logs debug output to the console on multiple channels.
 * Each channel can be disabled separately to prevent output being sent on them (L.disableChannel).
 * In addition, all logging can be disabled (L.LOG = false).
 * Note: this class is for debugging. Simply disabling logging from this class is not enough for
 * release. The function calls will still slow down your program.
 * (And Strings, and StringBuilders implicitly created from concatenation will be a huge memory/cpu waste).
 * So please remove the L.d statements before release.
 */
public class L
{
    // Whether or not we will send any output to the console.
    public static volatile boolean LOG = true;

    // Saves log channels along with their state of activity (enabled:true or disabled:false).
    private static volatile HashMap<String, Boolean> logChannels = new HashMap<>();

    /**
     * Output a debug message to the console along a given channel.
     * @param msg the message to output. Any object can be given, and its toString method will be called.
     * @param channel the channel to send the output along. A channel will be created if it does not already exist.
     */
    public static void d(Object msg, String channel)
    {
        // If the channel does not exist, create it and set enabled.
        if (!logChannels.containsKey(channel))
        {
            logChannels.put(channel, true);
        }

        // If the channel is enabled, logcat the message, using the channel name as the logging tag.
        if (LOG && logChannels.get(channel))
        {
            Log.d(channel, msg.toString());
        }
    }

    /**
     * Disable a particular output channel so that its messages will not be sent to the console.
     * @param channel the channel to disable.
     */
    public static void disableChannel(String channel)
    {
        logChannels.put(channel, false);
    }
}
