package box.shoe.gameutils;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by Joseph on 2/15/2018.
 */

public class AABB extends RectF
{
    // >> Add some new methods.
    /**
     * Offset to a specific (top) position,
     * keeping its width and height the same.
     *
     * @param newTop    The new "top" coordinate
     */
    public void offsetTopTo(float newTop) {
        bottom += newTop - top;
        top = newTop;
    }

    /**
     * Offset to a specific (right) position,
     * keeping its width and height the same.
     *
     * @param newRight    The new "right" coordinate
     */
    public void offsetRightTo(float newRight) {
        left += newRight - right;
        right = newRight;
    }

    /**
     * Offset to a specific (bottom) position,
     * keeping its width and height the same.
     *
     * @param newBottom    The new "bottom" coordinate
     */
    public void offsetBottomTo(float newBottom) {
        top += newBottom - bottom;
        bottom = newBottom;
    }

    /**
     * Offset to a specific (left) position,
     * keeping its width and height the same.
     *
     * @param newLeft    The new "left" coordinate
     */
    public void offsetLeftTo(float newLeft) {
        right += newLeft - left;
        left = newLeft;
    }

    // >> Natural extension of the great RectF constructors.
    /**
     * Create a new empty AABB. All coordinates are initialized to 0.
     */
    public AABB()
    {

    }

    /**
     * Create a new AABB with the specified coordinates. Note: no range
     * checking is performed, so the caller must ensure that left <= right and
     * top <= bottom.
     *
     * @param left   The X coordinate of the left side of the AABB
     * @param top    The Y coordinate of the top of the AABB
     * @param right  The X coordinate of the right side of the AABB
     * @param bottom The Y coordinate of the bottom of the AABB
     */
    public AABB(float left, float top, float right, float bottom)
    {
        super(left, top, right, bottom);
    }

    /**
     * Create a new AABB, initialized with the values in the specified
     * AABB (which is left unmodified).
     *
     * @param aabb The AABB whose coordinates are copied into the new
     *          rectangle.
     */
    public AABB(AABB aabb)
    {
        super(aabb);
    }

    public AABB(Rect r)
    {
        super(r);
    }
}
