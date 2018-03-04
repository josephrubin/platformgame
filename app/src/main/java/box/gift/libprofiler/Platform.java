package box.gift.libprofiler;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import box.shoe.gameutils.DisplayEntity;
import box.shoe.gameutils.Rand;
import box.shoe.gameutils.Vector;
import box.shoe.gameutils.engine.Interpolatable;

/**
 * Created by Joseph on 2/9/2018.
 */

public class Platform extends DisplayEntity
{
    private static Rand random = new Rand();
    private Paint paint;
    private RectF temporaryDrawBounds;
    private int bottomColor;

    public Platform(float initialX, float initialY, float initialWidth, float initialHeight, Vector velocity)
    {
        super(initialX, initialY, initialWidth, initialHeight, velocity);
        temporaryDrawBounds = new RectF();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Pick from various shades of green.
        int randColorPicker = random.intFrom(0, 2);
        switch (randColorPicker)
        {
            case 0:
                bottomColor = Color.parseColor("#10A700");
                break;
            case 1:
                bottomColor = Color.parseColor("#10B000");
                break;
            case 2: default:
                bottomColor = Color.parseColor("#12C000");
                break;
        }

        Interpolatable.SERVICE.addMember(this);
    }

    public void paint(Canvas canvas, Resources resources)
    {
        // Shortening our draw rectangle would reduce overdraw but we
        // need to cover up the rounded bottom, so we must overdraw.
        paint.setColor(Color.parseColor("#04ee04"));
        float rad = display.height() / 3;
        canvas.drawRoundRect(display, rad, rad, paint);

        paint.setColor(bottomColor);
        temporaryDrawBounds.set(display);
        temporaryDrawBounds.top += temporaryDrawBounds.height() / 2;
        canvas.drawRect(temporaryDrawBounds, paint);
    }
}
