package box.gift.libprofiler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import box.shoe.gameutils.Entity;

/**
 * Created by Joseph on 2/28/2018.
 */

public class PlayButton extends Entity
{
    public PlayButton(float initialX, float initialY, float initialWidth, float initialHeight)
    {
        super(initialX, initialY, initialWidth, initialHeight);
    }

    public void paint(Canvas canvas, Bitmap sprite)
    {
        canvas.drawBitmap(sprite, null, body, null);
    }
}
