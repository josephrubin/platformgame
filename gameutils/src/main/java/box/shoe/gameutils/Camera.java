package box.shoe.gameutils;

import android.graphics.Canvas;

/**
 * Created by Joseph on 1/1/2018.
 */
//TODO: lots of work to be done to make this usable.
public interface Camera
{
    void view(Canvas canvas); //TODO: change the method names.
    void unview(Canvas canvas);

    boolean isVisible(Paintable paintable);
    boolean isInbounds(Entity entity);
}
