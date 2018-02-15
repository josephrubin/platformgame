package box.shoe.gameutils;

/**
 * Created by Joseph on 2/14/2018.
 * Implementations of this interface can be placed into an ObjectPool.
 * In addition to the required methods, each implementation must provide
 * a default constructor.
 */
//TODO: unused, perhaps not needed. Delete? or use?
public interface Poolable
{
    /**
     * Called when an object is about to be placed into the pool.
     * This should assign to fields default values and default states.
     */
    void reset();
}
