package box.gift.libprofiler;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import box.shoe.gameutils.Entity;
import box.shoe.gameutils.Paintable;

/**
 * Created by Joseph on 2/11/2018.
 */
//TODO: move vertically with the player? (So when he is fighting in the air....)
public class Attack extends Entity implements Paintable
{
    private static Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    static
    {
        paint.setColor(Color.GREEN);
    }

    public Attack(float initialX, float initialY, float initialWidth, float initialHeight)
    {
        super(initialX, initialY, initialWidth, initialHeight);
    }

    @Override
    public void paint(Canvas canvas, Resources resources)
    {
        canvas.drawRect(x, y, x + width, y + height, paint);
    }
}
