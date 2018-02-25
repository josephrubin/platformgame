package box.shoe.gameutils;

import android.support.annotation.CallSuper;
import android.util.Log;

/**
 * Created by Joseph on 12/9/2017.
 * A game object which occupies position and space in the game and can move around.
 * An Entity is not necessarily fit for rendering.
 * See DisplayEntity for an Entity which is meant to be displayed
 * during the game (occupies position and space on the screen).
 */ //TODO: type of short-lived entity that exists only for a number of frames? (particle)
public class Entity/* implements Poolable*/
{
    // The game-space which is occupied by this Entity.
    public AABB body;

    // Vector which represents how many x and y units the position will change by per update.
    public Vector velocity;
    // Vector which represents how many x and y units the velocity will change by per update.
    public Vector acceleration;

    // Enforce cleanup method call.
    private boolean cleaned = false;

    /**
     * Creates an Entity with the specified x and y coordinates,
     * with no width or height, with no velocity or acceleration.
     * @param initialX the starting x coordinate.
     * @param initialY the starting y coordinate.
     */
    public Entity(float initialX, float initialY) //TODO: version of these with RectF's? Or at least with params in the form of left/top/right/bot?
    {
        this(initialX, initialY, 0, 0, Vector.ZERO, Vector.ZERO);
    }

    /**
     * Creates an Entity with the specified x and y coordinates,
     * width and height, with no velocity or acceleration.
     * @param initialX the starting x coordinate.
     * @param initialY the starting y coordinate.
     * @param initialWidth the starting width.
     * @param initialHeight the starting height.
     */
    public Entity(float initialX, float initialY, float initialWidth, float initialHeight)
    {
        this(initialX, initialY, initialWidth, initialHeight, Vector.ZERO, Vector.ZERO);
    }

    /**
     * Creates an Entity with the specified x and y coordinates and velocity, with no acceleration.
     * @param initialX the starting x coordinate.
     * @param initialY the starting y coordinate.
     * @param initialWidth the starting width.
     * @param initialHeight the starting height.
     * @param initialVelocity the starting velocity.
     */
    public Entity(float initialX, float initialY, float initialWidth, float initialHeight, Vector initialVelocity)
    {
        this(initialX, initialY, initialWidth, initialHeight, initialVelocity, Vector.ZERO);
    }

    /**
     * Creates an Entity with the specified x and y coordinates, velocity and acceleration.
     * @param initialX the starting x coordinate.
     * @param initialY the starting y coordinate.
     * @param initialWidth the starting width.
     * @param initialHeight the starting height.
     * @param initialVelocity the starting velocity.
     * @param initialAcceleration the starting acceleration.
     */
    public Entity(float initialX, float initialY, float initialWidth, float initialHeight, Vector initialVelocity, Vector initialAcceleration)
    {
        // Do some sanity checking.... removing these checks could potentially be interesting,
        // but would probably not lead to behavior that is intended most of the time.
        if (initialWidth < 0)
        {
            throw new IllegalArgumentException("Width cannot be less than 0: " + initialWidth);
        }
        if (initialHeight < 0)
        {
            throw new IllegalArgumentException("Height cannot be less than 0: " + initialHeight);
        }
        body = new AABB(initialX, initialY, initialX + initialWidth, initialY + initialHeight);
        velocity = initialVelocity;
        acceleration = initialAcceleration;
    }

    /**
     * Updates velocity based on current acceleration,
     * and then updates position based on new velocity.
     * We do not need to multiply by dt because every time-step is of equal length.
     */
    @CallSuper
    public void update() //TODO: return some data? like boolean which says if this should be destroyed? or have that a Weaver event?
    {
        // We will update velocity based on acceleration first,
        // and update position based on velocity second.
        // This is apparently called Semi-Implicit Euler and is a more accurate form of integration
        // when acceleration is not constant.

        // Update velocity first based on current acceleration.
        velocity = velocity.add(acceleration);

        // Update position based on new velocity.
        body.offset(velocity.getX(), velocity.getY());
    }

    /**
     * Cleanup should always be called before an Entity is eligible to be Garbage Collected.
     * After a cleanup call, the Entity is no longer usable, and should be de-referenced immediately.
     * Any use of an Entity object or its aggregates after cleanup is called is undefined.
     * This should have no effect if called more than once for any particular Entity.
     * i.e. calls after the first one should be idempotent.
     * Turn on debug mode get warnings when cleanup was not called prior to an Entity being GC'd.
     */
    @CallSuper
    public void cleanup()
    {
        cleaned = true;
    }

    /**
     * When trying to debug, let the user know when they have de-referenced an Entity that was
     * not cleaned up. Not cleaning up an Entity can lead to sub-optimal performance
     * in the time before it is garbage collected, because they will still be in the
     * lists of services that will operate on them, without any real reason.
     * The services use weak data structures so the Entities may still be eventually GC'd,
     * at which point, this finalize may be run to alert the user that cleanup was not called.
     * Of course, it is up to each Entity subclass to override cleanup() to remove the services
     * they register for (and also to throw the Entity back in a Pool if it came from one).
     */
    @Override
    protected void finalize() throws Throwable
    {
        try
        {
            if (!cleaned) //TODO: only in debug mode
            {
                // We log a warning because throwing an error here will not stop program execution
                // anyway and there is technically no 'error' here. For all we know, the Entities do not
                // need to be cleaned up. This warning exists for debug purposes only.
                Log.w("Entity Finalizer", getClass().getName() + "|" + this + " was garbage collected before being cleaned! Try calling cleanup() on Entities that you are done with before de-referenceing them.");
            }
        }
        finally
        {
            super.finalize();
        }
    }

    // We mark this method as final to enforce all Collections of Entities to be identity collections.
    @Override
    public final boolean equals(Object other)
    {
        return super.equals(other);
    }

    // We mark this method as final to enforce all Collections of Entities to be identity collections.
    @Override
    public final int hashCode()
    {
        return super.hashCode();
    }
}
