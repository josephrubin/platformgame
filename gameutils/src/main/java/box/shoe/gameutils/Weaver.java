package box.shoe.gameutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Joseph on 11/30/2017.
 * Global messaging system.
 */
//TODO: are global events even smart??
public class Weaver //TODO: rename
{
    private static Map<String, List<Runnable>> hooks = new HashMap<>();

    public static void hook(String eventType, Runnable runnable)
    {
        if (hooks.containsKey(eventType))
        {
            hooks.get(eventType).add(runnable);
        }
        else
        {
            List<Runnable> runnables = new ArrayList<>(); //TODO: Perhaps change to LinkedList if removal is implemented?
            runnables.add(runnable);
            hooks.put(eventType, runnables);
        }
    }


    public static boolean unhook(String eventType, Runnable runnable)
    {
        return hooks.containsKey(eventType) && hooks.get(eventType).remove(runnable);
    }


    public static void tug(String eventType)
    {
        if (hooks.containsKey(eventType))
        {
            List<Runnable> runnables = hooks.get(eventType);
            for (Runnable runnable : runnables)
            {
                runnable.run();
            }
        }
    }
}
