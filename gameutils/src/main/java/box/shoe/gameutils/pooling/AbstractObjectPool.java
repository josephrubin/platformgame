package box.shoe.gameutils.pooling;

import android.support.annotation.RestrictTo;

/**
 * Created by Joseph on 2/7/2018.
 */

public abstract class AbstractObjectPool<T>
{
    private ObjectLot objectLot;

    public AbstractObjectPool(int initialSize)
    {
        if (initialSize < 0)
        {
            throw new IllegalArgumentException("Size cannot be less than 0!");
        }
        objectLot = new ObjectLot(initialSize);
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    protected void populate()
    {
        for (int i = 0; i < objectLot.size; i++)
        {
            objectLot.give(createNew());
        }
    }

    public synchronized T get()
    {
        T obj;
        if (objectLot.isEmpty())
        {
            obj = createNew();
        }
        else
        {
            obj = (T) objectLot.take();
        }

        //register.remove(obj);
        return obj;
    }

    /** //TODO: make a forgiving pool that will remember objs that were put (Register) and not cause errors if they are put again? then library could use faster pool when we know it's correct, and user can have a more forgiving option.
     * Do not put object that was already put in this pool. Unknown behavior could result. This is not checked, no error will be thrown. Silent, complete, failure. (for the sake of optimization)
     * @param obj
     * @return
     */
    public synchronized boolean put(T obj) //TODO: grow the pool (new, bigger lot) if we overflow.
    {
        if (obj == null)
        {
            return false;
        }
        if (objectLot.isFull())
        {
            return false; //TODO: grow lot....
        }

        objectLot.give(obj);
        return true;
    }

    public synchronized void cleanup()
    {
        objectLot.cleanup();
        objectLot = null;
    }

    protected abstract T createNew();
}