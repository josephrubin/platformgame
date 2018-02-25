package box.shoe.gameutils.camera;

import android.graphics.Canvas;

import box.shoe.gameutils.DisplayEntity;

/**
 * Created by Joseph on 1/1/2018.
 * Cameras by themselves don't cull Entities for which !isVisible. //TODO: Complete description.
 */
public interface Camera
{
    /**
     * Rolls the camera by preconfiguring the supplied canvas
     * so that draws occur through the lens of the camera.
     * @param canvas the canvas to view through the camera.
     */
    void roll(Canvas canvas);

    /**
     * Returns true iff the supplied DisplayEntity's display bounds indicate that it
     * is seen through this camera. Must give consistent results between calls to roll().
     * @param displayEntity the DisplayEntity to check for visibility.
     * @return true if displayEntity is visible, and false otherwise.
     */
    boolean isVisible(DisplayEntity displayEntity);
}