package box.shoe.gameutils;

import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Joseph on 2/7/2018.
 */

public class ObjectPool<T>
{
    private ObjectLot<T> objectLot;
    //private HashSet<T> register;
    private Factory<T> factory;

    public ObjectPool(int initialSize, @Nullable ObjectPool.Factory<T> factory)
    {
        if (initialSize < 0)
        {
            throw new IllegalArgumentException("Size cannot be less than 0!");
        }
        objectLot = new ObjectLot<>(initialSize);
        //register = new HashSet<>(initialSize);
        this.factory = factory;
        if (this.factory != null)
        {
            populate();
        }
    }

    private void populate()
    {
        for (int i = 0; i < objectLot.size; i++)
        {
            T obj = factory.create();
            objectLot.give(obj);
            //register.add(obj);
        }
    }

    public synchronized T get()
    {
        T obj;
        if (objectLot.isEmpty())
        {
            if (factory == null)
            {
                throw new IllegalStateException("The pool is empty and no factory was defined for object creation!");
            }
            obj = factory.create();
        }
        else
        {
            obj = objectLot.take();
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
        /*if (register.contains(obj))
        {
            // Don't put an object in twice.
            return false;
        }*/
        if (objectLot.isFull())
        {
            return false;
        }

        objectLot.give(obj);
        //register.add(obj);

        return true;
    }

    public synchronized void cleanup()
    {
        objectLot.cleanup();
        objectLot = null;
        //register = null;
    }

    public interface Factory<F>
    {
        F create();
    }
}