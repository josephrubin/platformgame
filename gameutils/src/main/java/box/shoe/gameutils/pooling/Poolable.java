package box.shoe.gameutils.pooling;

/**
 * Created by Joseph on 2/14/2018.
 * Implementations of this interface can be placed into an AbstractObjectPool.
 */
//TODO: unused, perhaps not needed. Delete? or use?
public interface Poolable
{
    /**
     * Called when an object is about to be placed into the pool.
     * This should assign to fields default values and default states.
     * Either null references, or put them back into pools if they came from them.
     */
    void reset();
}
