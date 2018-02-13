package box.shoe.gameutils;

import android.support.annotation.CallSuper;
import android.util.Log;

import org.jetbrains.annotations.Contract;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by Joseph on 12/9/2017.
 * A game object which holds a position and space on the screen and can move around.
 * Technically: a Game object with x and y coordinates, which can be fractional, width and height,
 * which can be fractional (or 0 to indicate no space taken up)
 * and Vector objectsfor velocity and acceleration.
 * Width and height are different from display-width and display-height.
 */ //TODO: type of short-lived entity that exists only for a number of frames? (particle)
public class Entity //TODO: have methods for getting bounds? (instead of x+wid, use getRight) etc. getCenterX etc.
{
    // Width represents how much horizontal space this takes up.
    public float width;
    // Height represents how much vertical space this takes up.
    public float height;

    // Represents where this is on the screen. Positive direction indicates how far right and down this is on the screen.
    // (In other words, displacement from the (top-left) origin in the x (rightward) and y (downward) directions.)
    public float x;
    public float y;
    // Vector which represents how many x and y units the position will change by per update.
    public Vector velocity;
    // Vector which represents how many x and y units the velocity will change by per update.
    public Vector acceleration;

    // Relatively from the position, where to find the point of origin from which all positioning of this object is calculated.
    public Vector registration; //TODO: make final somehow

    public float _width;
    public float _height;
    public float _x;
    public float _y;

    // Enforce cleanup method call.
    private boolean cleaned = false;

    /**
     * Creates an Entity with the specified x and y coordinates,
     * with no width or height, with no velocity or acceleration.
     * @param initialX the starting x coordinate.
     * @param initialY the starting y coordinate.
     */
    public Entity(float initialX, float initialY)
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
        width = _width = initialWidth;
        height = _height = initialHeight;
        x = _x = initialX; //TODO: we are setting the visual fields equal to the normal ones for display before the first couple frames (before it has been interpolated). the other option may be better -- do not paint entities until they can be interpolated ?
        y = _y = initialY;
        velocity = initialVelocity;
        acceleration = initialAcceleration;
        registration = new Vector(0, 0);
    }

    public Entity wizard(float initialX, float initialY, float initialWidth, float initialHeight, Vector initialVelocity)
    {
        width = _width = initialWidth;
        height = _height = initialHeight;
        x = _x = initialX; //TODO: we are setting the visual fields equal to the normal ones for display before the first couple frames (before it has been interpolated). the other option may be better -- do not paint entities until they can be interpolated ?
        y = _y = initialY;
        velocity = initialVelocity;
        acceleration = Vector.ZERO;
        registration = new Vector(0, 0);
        return this;
    }//TODO: do for other constructors.

    /**
     * Updates velocity based on current acceleration, and then updates position based on new velocity.
     * We do not need to multiply by dt because every timestep is of equal length.
     */
    @CallSuper
    public void update() //TODO: return some data? like boolean which says if this should be destroyed? or have that a Weaver event?
    {
        // We will update velocity based on acceleration first,
        // and update position based on velocity second.
        // This is apparently called Semi-Implicit Euler and is a more accurate form of integration
        // when acceleration is not constant.
        // More importantly though, we do it because we subscribe to the holy faith
        // of Gaffer On Games, whose recommendations are infallible.

        // Update velocity first based on current acceleration.
        velocity = velocity.add(acceleration);

        // Update position based on new velocity.
        x += velocity.getX();
        y += velocity.getY();
    }

//TODO: interpolation as a service/system should be interface? and not just for entities? (if services work for all objects)
    @CallSuper
    protected void provideInterpolatables(InterpolatablesCarrier in)
    {
        in.provide(x);
        in.provide(y);
        in.provide(width);
        in.provide(height);
    }

    @CallSuper
    protected void recallInterpolatables(InterpolatablesCarrier out)
    {
        _x = out.recall();
        _y = out.recall();
        _width = out.recall();
        _height = out.recall();
    }

    @CallSuper
    public void cleanup()
    {
        cleaned = true;
        EntityServices.removeAllServices(this);
    }

    @Override
    protected void finalize()
    {
        if (!cleaned) //TODO: only in debug mode
        {
            Log.w("Entity Finalizer", getClass().getName() + "|" + this + " was garbage collected before being cleaned! Try calling cleanup() on Entities that you are done with before dereferenceing them. (REASON: this will remove services, which will increase performance before the Entity is GC'd.)");
        }
    }

    @Override
    public final boolean equals(Object other)
    {
        return super.equals(other);
    }

    @Override
    public final int hashCode()
    {
        return super.hashCode();
    }
}
