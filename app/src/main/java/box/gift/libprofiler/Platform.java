package box.gift.libprofiler;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import box.shoe.gameutils.DisplayEntity;
import box.shoe.gameutils.Interpolatable;
import box.shoe.gameutils.Paintable;

/**
 * Created by Joseph on 2/9/2018.
 */

public class Platform extends DisplayEntity implements Paintable
{
    private static PlatformPaintable paintable = new PlatformPaintable();

    public Platform(float initialX, float initialY, float initialWidth, float initialHeight)
    {
        super(initialX, initialY, initialWidth, initialHeight, ProfileEngine.SCROLL_SPEED);
        Interpolatable.SERVICE.addMember(this);
    }

    @Override
    public void paint(Canvas canvas, Resources resources)
    {
        paintable.paint(canvas, display);
    }

    private static class PlatformPaintable
    {
        private static Paint paint;

        private PlatformPaintable()
        {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        public void paint(Canvas canvas, RectF display)
        {
            paint.setColor(Color.rgb(0, 100, 0));
            float rad = display.height() / 4;
            canvas.drawRoundRect(display, rad, rad, paint);

            paint.setColor(Color.GREEN);
            RectF bottomPart = new RectF(display);
            bottomPart.top += bottomPart.height() / 2;
            canvas.drawRect(bottomPart, paint);
        }
    }
}
