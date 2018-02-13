package box.gift.libprofiler;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;

import box.shoe.gameutils.Entity;
import box.shoe.gameutils.EntityServices;
import box.shoe.gameutils.Paintable;

/**
 * Created by Joseph on 2/9/2018.
 */

public class Platform extends Entity implements Paintable
{
    private static PlatformPaintable paintable = new PlatformPaintable();

    public Platform(float initialX, float initialY, float initialWidth, float initialHeight)
    {
        super(initialX, initialY, initialWidth, initialHeight, ProfileEngine.SCROLL_SPEED);
        EntityServices.addService(this, EntityServices.Service.INTERPOLATION);
    }

    @Override
    public void paint(Canvas canvas, Resources resources)
    {
        paintable.paint(canvas, _x, _y, _width, _height);
    }

    private static class PlatformPaintable
    {
        private static Paint paint;

        private PlatformPaintable()
        {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        public void paint(Canvas canvas, float _x, float _y, float _width, float _height)
        {
            paint.setColor(Color.rgb(0, 100, 0));
            float rad = _height / 4;
            canvas.drawRoundRect(new RectF(_x, _y, _x + _width, _y + _height), rad, rad, paint);

            paint.setColor(Color.GREEN);
            canvas.drawRect(_x, _y + _height / 2, _x + _width, _y + _height, paint);
        }
    }
}
