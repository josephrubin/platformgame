package box.shoe.gameutils;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Joseph on 12/31/2017.
 * The job of a GameState is two fold.
 * 1) Keep track of all data that the Engine wants to pass along to the Screen for painting.
 * 2) Keep track of all Entities that exist at the update that generated this GameState
 *      so that they can be interpolated.
 */

public class GameState
{
    /*pack*/ static ObjectPool<GameState> POOL = new ObjectPool<>(6, new GameState.Factory());

    // The time at which the update which generated this GameState occurred.
    private volatile long timeStamp;

    // All data necessary for painting of this GameState.
    private Map<String, Object> data;

    // Storage of all Entities along with their provided Interpolatables.
    // Used to interpolate values for the Entities at this GameState.
    /*pack*/ WeakHashMap<Entity, InterpolatablesCarrier> interps;

    /*pack*/ GameState()
    {
        data = new HashMap<>();
        interps = new WeakHashMap<>();
    }

    /*pack*/ void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    /*pack*/ long getTimeStamp()
    {
        return timeStamp;
    }

    public void put(String key, Object value)
    {
        data.put(key, value);
    }

    public <T> T get(String key)
    {
        return (T) data.get(key);
    }

    public void cleanup()
    {
        data.clear();

        for (InterpolatablesCarrier interpolatablesCarrier : interps.values())
        {
            interpolatablesCarrier.cleanup();
            InterpolatablesCarrier.POOL.put(interpolatablesCarrier);
        }
        interps.clear();
    }

    private static class Factory implements ObjectPool.Factory
    {
        @Override
        public GameState create()
        {
            return new GameState();
        }
    }
}
