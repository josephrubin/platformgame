package box.shoe.gameutils;

import android.annotation.SuppressLint;

import box.shoe.gameutils.engine.Interpolatable;
import box.shoe.gameutils.engine.InterpolatablesCarrier;

/**
 * Created by Joseph on 2/14/2018.
 */
//TODO: auto add interpolation service on creation, and remove it on cleanup?
public class DisplayEntity extends Entity implements Interpolatable
{
    // The screen-space which is occupied by this DisplayEntity.
    public AABB display;

    public DisplayEntity(float initialX, float initialY)
    {
        this(initialX, initialY, 0, 0, Vector.ZERO, Vector.ZERO);
    }

    public DisplayEntity(float initialX, float initialY, float initialWidth, float initialHeight)
    {
        this(initialX, initialY, initialWidth, initialHeight, Vector.ZERO, Vector.ZERO);
    }

    public DisplayEntity(float initialX, float initialY, float initialWidth, float initialHeight, Vector initialVelocity)
    {
        this(initialX, initialY, initialWidth, initialHeight, initialVelocity, Vector.ZERO);
    }

    public DisplayEntity(float initialX, float initialY, float initialWidth, float initialHeight, Vector initialVelocity, Vector initialAcceleration)
    {
        super(initialX, initialY, initialWidth, initialHeight, initialVelocity, initialAcceleration);
        display = new AABB(body); //TODO: we just set the interps to the creation vals. is this right? should we sintead not paint displayentities that have not yet been interpolated?
    }

    // Suppress the @CallSuper because this is the top level implementor.
    @SuppressLint("MissingSuperCall")
    @Override
    public void provideInterpolatables(InterpolatablesCarrier in)
    {
        in.provide(body.left);
        in.provide(body.top);
        in.provide(body.right);
        in.provide(body.bottom);
    }

    // Suppress the @CallSuper because this is the top level implementor.
    @SuppressLint("MissingSuperCall")
    @Override
    public void recallInterpolatables(InterpolatablesCarrier out)
    {
        display.left = out.recall();
        display.top = out.recall();
        display.right = out.recall();
        display.bottom = out.recall();
    }

    public interface Factory
    {
        public DisplayEntity create();
    }
}
