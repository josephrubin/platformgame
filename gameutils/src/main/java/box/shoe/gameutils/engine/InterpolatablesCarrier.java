package box.shoe.gameutils.engine;

import java.util.ArrayList;
import java.util.Iterator;

import box.shoe.gameutils.pooling.AbstractObjectPool;
import box.shoe.gameutils.pooling.FactoryObjectPool;

/**
 * Created by Joseph on 12/31/2017.
 */
//TODO: remove this class, just pass arrays instead, and the interface should include a method for the number of interps it will provide, as well as the provide/recall methods.
public class InterpolatablesCarrier
{//TODO: use object lot rather than a pool, for speed? //initialSize should probably be way lower, or at the very least configurable.
    /*pack*/ static FactoryObjectPool<InterpolatablesCarrier> POOL = new FactoryObjectPool<>(2000, new InterpolatablesCarrier.Factory());

    private ArrayList<Float> interps; //TODO: should probably use array. see above.

    public InterpolatablesCarrier()
    {
        interps = new ArrayList<>();
    }

    public void provide(float value)
    {
        interps.add(value);
    }

    public void cleanup()
    {
        interps.clear();
    }

    public float recall()
    {
        return interps.remove(0);
    }

    private int size()
    {
        return interps.size();
    }

    /*pack*/ InterpolatablesCarrier interpolateTo(InterpolatablesCarrier other, double interpolationRatio)
    {
        // First, check size compatibility
        if (size() != other.size())
        {
            throw new IllegalStateException("InterpolatablesCarrier not compatible.");
        }

        InterpolatablesCarrier toReturn = InterpolatablesCarrier.POOL.get();

        Iterator<Float> iterator = interps.iterator();
        Iterator<Float> otherIterator = other.interps.iterator();
        while (iterator.hasNext())
        {
            toReturn.provide((float) (iterator.next() * (1 - interpolationRatio) + otherIterator.next() * interpolationRatio));
        }

        return toReturn;
    }

    /*pack*/ boolean isEmpty()
    {
        return interps.isEmpty();
    }

    private static class Factory implements FactoryObjectPool.Factory<InterpolatablesCarrier>
    {

        @Override
        public InterpolatablesCarrier create()
        {
            //System.out.println("factory for interps called");
            return new InterpolatablesCarrier();
        }
    }
}
