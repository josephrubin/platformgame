package box.shoe.gameutils.emitter;

import box.shoe.gameutils.Entity;

/**
 * Created by Joseph on 1/15/2018.
 * Puts out objects of type given by the type parameter at the position specified by
 * the methods {@link #setX(float)} and {@link #setY(float)}.
 * What exactly it means to put out an object at the specified position is up to the
 * implementors to decide.
 */

public interface Emitter<T>
{
    void setX(float x); //TODO: rename to something like setFocusX (because not all emmitters emit at a specific point).
    void setY(float y);
    T emit();
}