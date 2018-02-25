package box.shoe.gameutils;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by Joseph on 2/15/2018.
 * Unclear yet what this represents.
 * But for now, the Axis-Aligned-Bonding-Box class serves to add
 * some functionality to a bare RectF.
 */

public class AABB extends RectF
{
    // >> Add some new methods.

    // > Collisions.
    /**
     * Returns true if this AABB intersects the specified AABB.
     * In no event is this AABB modified. No check is performed to see
     * if either AABB is empty. To record the intersection, use intersect()
     * or setIntersect().
     *
     * @param other the AABB to check for intersection against.
     * @return true iff the specified AABB intersects this AABB. In
     *              no event is this AABB modified.
     */
    public boolean intersects(RectF other) {
        return this.left < other.right && other.left < this.right
                && this.top < other.bottom && other.top < this.bottom;
    }

    /**
     * Offset to a specific (top) position,
     * keeping width and height the same.
     *
     * @param newTop    The new "top" coordinate
     */
    public void offsetTopTo(float newTop) {
        bottom += newTop - top;
        top = newTop;
    }

    /**
     * Offset to a specific (right) position,
     * keeping width and height the same.
     *
     * @param newRight    The new "right" coordinate
     */
    public void offsetRightTo(float newRight) {
        left += newRight - right;
        right = newRight;
    }

    /**
     * Offset to a specific (bottom) position,
     * keeping width and height the same.
     *
     * @param newBottom    The new "bottom" coordinate
     */
    public void offsetBottomTo(float newBottom) {
        top += newBottom - bottom;
        bottom = newBottom;
    }

    /**
     * Offset to a specific (left) position,
     * keeping width and height the same.
     *
     * @param newLeft    The new "left" coordinate
     */
    public void offsetLeftTo(float newLeft) {
        right += newLeft - left;
        left = newLeft;
    }

    /**
     * Offset to a specific (center) position,
     * keeping width and height the same.
     *
     * @param newCenterX    The new "center" x coordinate
     * @param newCenterY    The new "center" y coordinate
     */
    public void offsetCenterTo(float newCenterX, float newCenterY) {
        offsetTo(newCenterX - width() / 2, newCenterY - height() / 2);
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

    public AABB(RectF r)
    {
        super(r);
    }

    public AABB(Rect r)
    {
        super(r);
    }
}
