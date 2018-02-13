package box.gift.libprofiler;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;

import box.shoe.gameutils.Entity;
import box.shoe.gameutils.EntityServices;
import box.shoe.gameutils.Paintable;

/**
 * Created by Joseph on 2/9/2018.
 */

public class Enemy extends Entity implements Paintable
{
    private static Drawable still = null;

    public Enemy(float initialX, float initialY, float initialWidth, float initialHeight)
    {
        super(initialX, initialY, initialWidth, initialHeight, ProfileEngine.SCROLL_SPEED);
        EntityServices.addService(this, EntityServices.Service.INTERPOLATION);
    }

    @Override
    public void paint(Canvas canvas, Resources resources)
    {
        if (still == null)
        {
            still = resources.getDrawable(R.drawable.enemy);
        }
        still.setBounds((int) _x, (int) _y, (int) (_x + _width), (int) (_y + _height));
        still.draw(canvas);
    }
}
