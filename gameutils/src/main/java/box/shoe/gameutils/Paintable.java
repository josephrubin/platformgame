package box.shoe.gameutils;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Bundle;

/**
 * Created by Joseph on 12/9/2017.
 */

public interface Paintable //TODO: (not the place for this, but) bitmaps/drawable should be able to specify scale mode, or perhaps have levels of sizes to draw at.
{
    void paint(Canvas canvas, Resources resources);
}
