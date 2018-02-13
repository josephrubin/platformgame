package box.shoe.gameutils;

/**
 * Created by Joseph on 2/3/2018.
 */
//TODO: when pools are filled in library pools, should not throw error!
    //TODO: entities should use special kind of pool which removes interpolatablecarriers, and also removes sleeping ones from the global lists kept by gamestates!
public class ObjectLot<T> //TODO: if object is sleeping, the GameState weak refs should clear, or at least dont copy sleeping obects to the next GameState.... (for Entity pool, not ObjectPool)
{ //TODO: auto allocate to fill pools at the start?
    //TODO: pick smart values for size of all library pools. depending on how many are created vs how many are deleted. if in equal/similar number, smaller values are needed.
    /*pack*/ T[] sleeping;
    /*pack*/ int currentIndex;
    /*pack*/ int size;

    /**
     * Creates an object pool of the specified size.
     * @param size the number of spots that the pool has to hold sleeping objects.
     */
    @SuppressWarnings("unchecked")
    public ObjectLot(int size)
    {
        sleeping = (T[]) new Object[size];
        currentIndex = -1;
        this.size = sleeping.length;
    }

    public T take()
    {
        if (isEmpty())
        {
            throw new IllegalStateException("Pool is empty!");
        }
        T obj = sleeping[currentIndex];
        currentIndex--;
        return obj;
    }

    /**
     * Adds an object to the pool.
     * (Precondition: the object is not already in the pool. This is not checked. Unknown behavior may follow if this condition is not fulfilled.)
     * @param obj the object to throw back into the pool.
     */
    public void give(T obj)
    {
        if (obj == null)
        {
            throw new IllegalArgumentException("Obj is null!");
        }
        if (isFull())
        {
            throw new IllegalStateException("Pool is full!");
        }
        currentIndex++;
        sleeping[currentIndex] = obj;
    }

    public boolean isEmpty()
    {
        return currentIndex < 0;
    }

    public boolean isFull()
    {
        return currentIndex + 1 >= size;
    }

    public void cleanup()
    {
        sleeping = null;
    }
}
