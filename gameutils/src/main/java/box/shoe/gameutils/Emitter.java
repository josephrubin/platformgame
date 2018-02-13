package box.shoe.gameutils;

import android.graphics.Canvas;

/**
 * Created by Joseph on 1/15/2018.
 */

public interface Emitter extends Paintable
{
    void emit(float xPos, float yPos);
    void update();
}
