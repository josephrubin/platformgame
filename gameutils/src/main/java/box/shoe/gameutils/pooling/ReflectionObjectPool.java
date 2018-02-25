package box.shoe.gameutils.pooling;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Joseph on 2/25/2018.
 */

public class ReflectionObjectPool<T> extends AbstractObjectPool<T>
{
    private Constructor<T> typeParameterClassConstructor;

    public ReflectionObjectPool(int initialSize, Class<T> typeParameterClass)
    {
        super(initialSize);
        try
        {
            typeParameterClassConstructor = typeParameterClass.getConstructor();
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException("Type parameter does not have a default (zero-argument) constructor: " + typeParameterClass);
        }
        populate();
    }

    @Override
    protected T createNew()
    {
        try
        {
            return typeParameterClassConstructor.newInstance();
        }
        catch (InstantiationException e)
        {
            throw new IllegalArgumentException("InstantiationException: Could not create new instance with reflection: " + typeParameterClassConstructor);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalArgumentException("IllegalAccessException: Could not access constructor with reflection, the constructor must be public: " + typeParameterClassConstructor);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalArgumentException("InvocationTargetException: Could not create new instance with reflection. " + e.getCause() + ": " + typeParameterClassConstructor);
        }
    }
}
