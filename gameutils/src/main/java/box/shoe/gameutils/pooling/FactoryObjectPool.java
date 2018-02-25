package box.shoe.gameutils.pooling;

import android.support.annotation.NonNull;

/**
 * Created by Joseph on 2/25/2018.
 */

public class FactoryObjectPool<T> extends AbstractObjectPool<T>
{
    private Factory<? extends T> factory;

    public FactoryObjectPool(int initialSize, @NonNull Factory<? extends T> factory)
    {
        super(initialSize);
        this.factory = factory;
        populate();
    }

    @Override
    protected T createNew()
    {
        return factory.create();
    }

    public interface Factory<F>
    {
        F create();
    }
}
