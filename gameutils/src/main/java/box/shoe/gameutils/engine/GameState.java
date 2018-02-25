package box.shoe.gameutils.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import box.shoe.gameutils.pooling.AbstractObjectPool;
import box.shoe.gameutils.pooling.FactoryObjectPool;

/**
 * Created by Joseph on 12/31/2017.
 * The job of a GameState is two fold.
 * 1) Keep track of all data that the Engine wants to pass along to the Screen for painting.
 * 2) Keep track of all Interpolatables that exist at the update that generated this GameState
 *      so that they can be interpolated.
 */

public class GameState
{//TODO: use object lot rather than a pool, for speed?
    //TODO: move library game states to the game engine as normal fields?
    /*pack*/ static FactoryObjectPool<GameState> POOL = new FactoryObjectPool<>(6, new Factory());

    // The time at which the update which generated this GameState occurred.
    private volatile long timeStamp;

    // All data necessary for painting this GameState. //TODO: this may be removed if we change how we pass info from engine to screen.
    private Map<String, Object> data;

    // Storage of all Interpolatables along with their provided InterpolatablesCarriers.
    // Used to interpolate values for the Interpolatables at this GameState.
    /*pack*/ WeakHashMap<Interpolatable, InterpolatablesCarrier> interps;

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

    private static class Factory implements FactoryObjectPool.Factory<GameState>
    {
        @Override
        public GameState create()
        {
            return new GameState();
        }
    }
}
