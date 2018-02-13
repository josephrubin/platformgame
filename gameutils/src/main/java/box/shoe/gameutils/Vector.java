package box.shoe.gameutils;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * Created by Joseph on 10/21/2017.
 * A magnitude and a direction, described by x and y length values.
 * This class is IMMUTABLE.
 */

public final class Vector
{
    // Length in the +x (rightward) direction.
    private final float x;
    // Length in the +y (downward) direction.
    private final float y;

    // Consts.
    // The zero vector.
    public static final Vector ZERO = new Vector(0, 0);

    /**
     * Constructs a new Vector.
     * @param x the x length
     * @param y the y length
     */
    public Vector(final float x, final float y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs a new Vector after casting the double parameters to floats.
     * @param x the x length
     * @param y the y length
     */
    public Vector(final double x, final double y)
    {
        this.x = (float) x;
        this.y = (float) y;
    }

    /**
     * Calculates the magnitude (length) of this Vector
     * @return the Vector's magnitude
     */
    public double magnitude()
    {
        return Math.sqrt((x * x) + (y * y));
    }

    /**
     * Constructs a Vector in the same direction as this one, but with a magnitude of 1.
     * @return this Vector's unit Vector.
     */
    @NonNull
    public Vector unit()
    {
        double magnitude = magnitude();
        if (magnitude == 0)
        {
            throw new ArithmeticException("Vector magnitude is 0. Cannot create unit vector.");
        }
        return this.scale(1 / magnitude);
    }

    /**
     * Adds another Vector to this one.
     * @param otherVector the Vector to add to this one.
     * @return a Vector representing the addition of the two supplied Vectors.
     */
    @NonNull
    public Vector add(@NonNull Vector otherVector)
    {
        if (this.equals(ZERO)) return otherVector;
        if (otherVector.equals(ZERO)) return this;
        return new Vector(x + otherVector.getX(), y + otherVector.getY());
    }

    /**
     * Subtracts another Vector from this Vector
     * @param otherVector the Vector to subtract.
     * @return a Vector representing the subtraction of the other Vector from this one.
     */
    @NonNull
    public Vector subtract(@NonNull Vector otherVector)
    {
        if (otherVector.equals(ZERO)) return this;
        return new Vector(x - otherVector.getX(), y - otherVector.getY());
    }

    /**
     * Scales this vector's magnitude by a factor.
     * @param factor the factor to scale by.
     * @return a new, scaled Vector.
     */
    @NonNull
    public Vector scale(double factor)
    {
        if (factor == 1) return this;
        return new Vector(x * factor, y * factor);
    }

    /**
     * Takes the dot product of two Vectors.
     * @param otherVector the Vector to multiply with.
     * @return the dot product of the two Vectors.
     */
    @Contract(pure = true)
    public double dot(@NonNull Vector otherVector)
    {
        return (x * otherVector.x) + (y * otherVector.y);
    }

    /**
     * Projects this Vector onto another Vector.
     * @param target the Vector to project onto.
     * @return the Vector projection.
     */
    @NonNull
    public Vector projectOnto(@NonNull Vector target)
    {
        return target.unit().scale(this.dot(target.unit()));
    }

    /**
     * Creates a new Vector with only the x length of this one, and y set to 0.
     * @return the horizontal Vector.
     */
    @NonNull
    public Vector onlyX()
    {
        if (y == 0) return this;
        return new Vector(x, 0);
    }

    /**
     * Creates a new Vector with only the y length of this one, and x set to 0.
     * @return the vertical Vector.
     */
    @NonNull
    public Vector onlyY()
    {
        if (x == 0) return this;
        return new Vector(0, y);
    }

    /**
     * Rotates this Vector by a number of radians to create a new Vector.
     * @param radians the degree in radians to rotate by.
     * @return the rotated Vector.
     */
    @NonNull
    public Vector rotateBy(double radians)
    {
        return new Vector(x * Math.cos(radians) - y * Math.sin(radians), x * Math.sin(radians) + y * Math.cos(radians));
    }

    /**
     * Creates a Vector perpendicular to this one.
     * @return the perpendicular Vector
     */
    @NonNull
    public Vector perpendicular()
    {
        return this.rotateBy(Math.PI / 2);
    }

    /**
     * Get the x length of this Vector.
     * @return the x length.
     */
    @Contract(pure = true)
    public float getX()
    {
        return x;
    }

    /**
     * Get the y length of this Vector.
     * @return the y length.
     */
    @Contract(pure = true)
    public float getY()
    {
        return y;
    }

    @NonNull
    public static Vector fromPolarDegrees(double r, double theta)
    {
        return Vector.fromPolarRadians(r, Math.toRadians(theta));
    }

    @NonNull
    public static Vector fromPolarRadians(double r, double theta)
    {
        return new Vector(r * Math.cos(theta), r * Math.sin(theta));
    }

    @NonNull
    @Contract(pure = true)
    @Override
    public String toString()
    {
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Vector vector = (Vector) o;

        return Float.compare(vector.x, x) == 0 && Float.compare(vector.y, y) == 0;
    }

    @Override
    public int hashCode()
    {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }
}
